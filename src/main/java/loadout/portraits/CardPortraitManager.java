package loadout.portraits;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.LoadoutMod;

public class CardPortraitManager {
    // Slay the Spire card portrait region size (adjust if you want a different target).
    public static final int TARGET_WIDTH = 250;
    public static final int TARGET_HEIGHT = 190;
    public static final String ASSET_PREFIX = "sha256_";

    private final Path portraitsDir;
    private final Path assetsDir;
    private final Path mapsDir;
    private final Path metaDir;
    private final Path permanentMapPath;
    private final Path tempMapPath;
    private final Path assetsMetaPath;
    private final boolean persistTemp;

    private final Map<String, String> permanentOverrides = new HashMap<>();
    private final Map<String, String> tempOverrides = new HashMap<>();
    private final Map<String, PortraitAssetMeta> assetMeta = new HashMap<>();

    private final LinkedHashMap<String, CachedRegion> regionCache = new LinkedHashMap<>(16, 0.75f, true);
    private int maxCacheSize = 128;

    public static final CardPortraitManager INSTANCE = new CardPortraitManager("loadoutMod", true);

    public CardPortraitManager() {
        this("loadoutMod", false);
    }

    public CardPortraitManager(String configId, boolean persistTemp) {
        this.persistTemp = persistTemp;
        Path configDir = Paths.get(SpireConfig.makeFilePath(configId, "CardPortraits", "json")).getParent();
        this.portraitsDir = configDir.resolve("portraits");
        this.assetsDir = portraitsDir.resolve("assets");
        this.mapsDir = portraitsDir.resolve("maps");
        this.metaDir = portraitsDir.resolve("meta");
        this.permanentMapPath = mapsDir.resolve("permanent.json");
        this.tempMapPath = mapsDir.resolve("temp.json");
        this.assetsMetaPath = metaDir.resolve("assets.json");
    }

    public void load() {
        ensureDirectories();
        permanentOverrides.clear();
        permanentOverrides.putAll(readStringMap(permanentMapPath));
        tempOverrides.clear();
        if (persistTemp) {
            tempOverrides.putAll(readStringMap(tempMapPath));
        }
        assetMeta.clear();
        assetMeta.putAll(readAssetMetaMap(assetsMetaPath));

        refreshCardLibrary();
    }

    public void save() {
        ensureDirectories();
        writeJsonAtomic(permanentMapPath, permanentOverrides);
        if (persistTemp) {
            writeJsonAtomic(tempMapPath, tempOverrides);
        }
        writeJsonAtomic(assetsMetaPath, assetMeta);
    }

    private void refreshCardLibrary() {
        for (String cardId : permanentOverrides.keySet()) {
            AbstractCard card = CardLibrary.getCard(cardId);
            if (card != null) {
                LoadoutMod.logger.info("Refreshing portrait for card: " + cardId);
                CardPortraitManager.applyPortraitOverride(card);
            }
        }
    }

    public String importPortrait(File file, String sourceNameOptional) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("Portrait source file missing.");
        }

        BufferedImage normalized = normalizeImage(file, TARGET_WIDTH, TARGET_HEIGHT);
        byte[] pngBytes = toPngBytes(normalized);
        // Hash-based asset IDs dedupe identical images and keep mappings portable.
        String assetId = ASSET_PREFIX + sha256Hex(pngBytes);
        Path assetPath = assetsDir.resolve(assetId + ".png");
        ensureDirectories();

        if (!Files.exists(assetPath)) {
            writeBytesAtomic(assetPath, pngBytes);
        }

        if (!assetMeta.containsKey(assetId)) {
            PortraitAssetMeta meta = new PortraitAssetMeta();
            meta.assetId = assetId;
            meta.width = TARGET_WIDTH;
            meta.height = TARGET_HEIGHT;
            meta.createdAt = Instant.now().toString();
            meta.sourceName = sourceNameOptional;
            assetMeta.put(assetId, meta);
        }

        return assetId;
    }

    public void setPermanentPortrait(String cardId, String assetId) {
        if (cardId == null || assetId == null) {
            return;
        }
        permanentOverrides.put(cardId, assetId);
    }

    public void clearPermanentPortrait(String cardId) {
        if (cardId == null) {
            return;
        }
        permanentOverrides.remove(cardId);
    }



    public void setTempPortrait(UUID cardUuid, String assetId) {
        if (cardUuid == null || assetId == null) {
            return;
        }
        tempOverrides.put(cardUuid.toString(), assetId);
    }

    public void clearTempPortrait(UUID cardUuid) {
        if (cardUuid == null) {
            return;
        }
        tempOverrides.remove(cardUuid.toString());
    }

    public TextureAtlas.AtlasRegion getPortrait(String cardId, UUID cardUuid, TextureAtlas.AtlasRegion fallback) {
        // Precedence: temp override (cardUuid) > permanent override (cardId) > fallback.
        String assetId = null;
        if (cardUuid != null) {
            assetId = tempOverrides.get(cardUuid.toString());
        }
        if (assetId == null && cardId != null) {
            assetId = permanentOverrides.get(cardId);
        }

        if (assetId == null) {
            return fallback;
        }

        TextureAtlas.AtlasRegion region = getCachedRegion(assetId);
        if (region == null) {
            LoadoutMod.logger.info("Failed to load portrait for card id: " + cardId + ",  and UUID: " + cardUuid);
            // Missing/corrupt asset -> drop mapping and fall back.
            if (cardUuid != null) {
                tempOverrides.remove(cardUuid.toString(), assetId);
            }
            if (cardId != null) {
                permanentOverrides.remove(cardId, assetId);
            }
            return fallback;
        }
        return region;
    }

    public TextureAtlas.AtlasRegion getTempPortrait(AbstractCard card) {
        return getPortrait(null, card.uuid, card.portrait);
    }

    public void copyTempPortrait(AbstractCard from, AbstractCard to) {
        tempOverrides.put(to.uuid.toString(), tempOverrides.get(from.uuid.toString()));
        to.portrait = from.portrait;
    }

    public void garbageCollectAssets() {
        ensureDirectories();
        Set<String> referenced = new HashSet<>();
        referenced.addAll(permanentOverrides.values());
        referenced.addAll(tempOverrides.values());

        // Remove mappings to missing files.
        removeMissingMappings(permanentOverrides);
        removeMissingMappings(tempOverrides);

        referenced.clear();
        referenced.addAll(permanentOverrides.values());
        referenced.addAll(tempOverrides.values());

        // Garbage collection deletes assets not referenced by any mapping.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(assetsDir, "*.png")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                String assetId = fileName.substring(0, fileName.length() - ".png".length());
                if (!referenced.contains(assetId)) {
                    Files.deleteIfExists(file);
                    disposeCached(assetId);
                    assetMeta.remove(assetId);
                }
            }
        } catch (IOException e) {
            LoadoutMod.logger.error("Portrait GC failed", e);
        }

        pruneCache(referenced);
    }

    public void dispose() {
        for (CachedRegion cached : regionCache.values()) {
            cached.dispose();
        }
        regionCache.clear();
    }

    public Map<String, String> getPermanentOverrides() {
        return Collections.unmodifiableMap(permanentOverrides);
    }

    public Map<UUID, String> getTempOverrides() {
        Map<UUID, String> ret = new HashMap<>();
        for (Map.Entry<String, String> entry : tempOverrides.entrySet()) {
            try {
                ret.put(UUID.fromString(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ret;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        if (maxCacheSize < 1) {
            return;
        }
        this.maxCacheSize = maxCacheSize;
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(assetsDir);
            Files.createDirectories(mapsDir);
            Files.createDirectories(metaDir);
        } catch (IOException e) {
            LoadoutMod.logger.error("Failed to create portrait directories", e);
        }
    }

    private TextureAtlas.AtlasRegion getCachedRegion(String assetId) {
        CachedRegion cached = regionCache.get(assetId);
        if (cached != null) {
            return cached.region;
        }

        Path assetPath = assetsDir.resolve(assetId + ".png");
        if (!Files.exists(assetPath)) {
            return null;
        }

        try {
            Texture texture = new Texture(new FileHandle(assetPath.toFile()));
            TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
            regionCache.put(assetId, new CachedRegion(texture, region));
            return region;
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to load portrait asset: " + assetId, e);
            return null;
        }
    }

    private void disposeCached(String assetId) {
        CachedRegion cached = regionCache.remove(assetId);
        if (cached != null) {
            cached.dispose();
        }
    }

    private void pruneCache(Set<String> referenced) {
        if (regionCache.size() <= maxCacheSize) {
            return;
        }
        Iterator<Map.Entry<String, CachedRegion>> it = regionCache.entrySet().iterator();
        while (it.hasNext() && regionCache.size() > maxCacheSize) {
            Map.Entry<String, CachedRegion> entry = it.next();
            if (!referenced.contains(entry.getKey())) {
                entry.getValue().dispose();
                it.remove();
            }
        }
    }

    private void removeMissingMappings(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            Path assetPath = assetsDir.resolve(entry.getValue() + ".png");
            if (!Files.exists(assetPath)) {
                it.remove();
            }
        }
    }

    private Map<String, String> readStringMap(Path path) {
        if (!Files.exists(path)) {
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, String>>() { }.getType();
            Map<String, String> map = CustomSavable.saveFileGson.fromJson(reader, type);
            if (map == null) {
                return new HashMap<>();
            }
            return new HashMap<>(map);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to read portrait map: " + path, e);
            return new HashMap<>();
        }
    }

    private Map<String, PortraitAssetMeta> readAssetMetaMap(Path path) {
        if (!Files.exists(path)) {
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, PortraitAssetMeta>>() { }.getType();
            Map<String, PortraitAssetMeta> map = CustomSavable.saveFileGson.fromJson(reader, type);
            if (map == null) {
                return new HashMap<>();
            }
            return new HashMap<>(map);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to read portrait meta map: " + path, e);
            return new HashMap<>();
        }
    }

    private void writeJsonAtomic(Path path, Object obj) {
        ensureDirectories();
        Path tempPath = path.resolveSibling(path.getFileName().toString() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
            CustomSavable.saveFileGson.toJson(obj, writer);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to write portrait JSON: " + path, e);
            return;
        }

        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                LoadoutMod.logger.error("Failed to finalize portrait JSON: " + path, ex);
            }
        }
    }

    private void writeBytesAtomic(Path path, byte[] bytes) throws IOException {
        ensureDirectories();
        Path tempPath = path.resolveSibling(path.getFileName().toString() + ".tmp");
        Files.write(tempPath, bytes);
        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static BufferedImage normalizeImage(File file, int targetW, int targetH) throws IOException {
        // BufferedImage path is safe for desktop Slay the Spire; swap to Pixmap if needed for other runtimes.
        BufferedImage src = ImageIO.read(file);
        if (src == null) {
            throw new IOException("Unsupported image format.");
        }

        double scale = Math.max((double) targetW / src.getWidth(), (double) targetH / src.getHeight());
        int scaledW = (int) Math.ceil(src.getWidth() * scale);
        int scaledH = (int) Math.ceil(src.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gScaled = scaled.createGraphics();
        gScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gScaled.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gScaled.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gScaled.drawImage(src, 0, 0, scaledW, scaledH, null);
        gScaled.dispose();

        int cropX = Math.max(0, (scaledW - targetW) / 2);
        int cropY = Math.max(0, (scaledH - targetH) / 2);

        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gOut = out.createGraphics();
        gOut.drawImage(scaled, -cropX, -cropY, null);
        gOut.dispose();

        return out;
    }

    private static byte[] toPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", out)) {
            throw new IOException("Failed to encode PNG.");
        }
        return out.toByteArray();
    }

    private static String sha256Hex(byte[] bytes) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("Failed to hash portrait bytes.", e);
        }
    }

    private static class CachedRegion {
        private final Texture texture;
        private final TextureAtlas.AtlasRegion region;

        private CachedRegion(Texture texture, TextureAtlas.AtlasRegion region) {
            this.texture = texture;
            this.region = region;
        }

        private void dispose() {
            // Dispose textures when we evict or dispose the manager to avoid leaks.
            texture.dispose();
        }
    }

    public static TextureAtlas.AtlasRegion resolvePortrait(AbstractCard card, TextureAtlas.AtlasRegion fallback) {
        UUID uuid = card != null ? card.uuid : null;
        String cardId = card != null ? card.cardID : null;
        return INSTANCE.getPortrait(cardId, uuid, fallback);
    }

    public static void applyPortraitOverride(AbstractCard card) {
        if (card == null) {
            return;
        }
        TextureAtlas.AtlasRegion fallback = card.portrait;
        TextureAtlas.AtlasRegion override = resolvePortrait(card, fallback);
        card.portrait = override;
    }

    public static void chooseFileAndSetPermanentPortrait(String cardId) {
        // UI selection belongs outside the manager.
        FileDialog dialog = new FileDialog((Frame) null, "Select portrait", FileDialog.LOAD);
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        File selected = new File(dialog.getDirectory(), dialog.getFile());
        try {
            String assetId = INSTANCE.importPortrait(selected, dialog.getFile());
            INSTANCE.setPermanentPortrait(cardId, assetId);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait", e);
        }
    }

    public static void chooseFileAndSetTempPortrait(AbstractCard card) {
        // UI selection belongs outside the manager.
        FileDialog dialog = new FileDialog((Frame) null, "Select portrait", FileDialog.LOAD);
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        File selected = new File(dialog.getDirectory(), dialog.getFile());
        try {
            String assetId = INSTANCE.importPortrait(selected, dialog.getFile());
            INSTANCE.setTempPortrait(card.uuid, assetId);
            applyPortraitOverride(card);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait", e);
        }
    }

    public static void makeTempPortraitPermanent(AbstractCard card) {
        if (card == null) {
            return;
        }
        String assetId = INSTANCE.tempOverrides.get(card.uuid.toString());
        if (assetId != null) {
            INSTANCE.setPermanentPortrait(card.cardID, assetId);
            INSTANCE.clearTempPortrait(card.uuid);
        }
    }

    public static boolean hasTempPortrait(AbstractCard card) {
        if (card == null) {
            return false;
        }
        return INSTANCE.tempOverrides.containsKey(card.uuid.toString());
    }

    public static boolean hasPermanentPortrait(AbstractCard card) {
        if (card == null) {
            return false;
        }
        return INSTANCE.permanentOverrides.containsKey(card.cardID);
    }

    public static boolean hasPermanentPortrait(String cardID) {
        return INSTANCE.permanentOverrides.containsKey(cardID);
    }

    public static boolean hasAnyPortrait(AbstractCard card) {
        return hasTempPortrait(card) || hasPermanentPortrait(card);
    }

    public static void clearAllPortraits(AbstractCard card) {
        if (card == null) {
            return;
        }
        INSTANCE.clearTempPortrait(card.uuid);
        INSTANCE.clearPermanentPortrait(card.cardID);
    }
}
