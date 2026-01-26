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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import basemod.ReflectionHacks;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import loadout.savables.CardLoadouts;
import loadout.savables.SerializableCard;

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
    private final Path assetsMetaPath;

    private final Map<String, String> permanentOverrides = new HashMap<>();
    private final Map<String, PortraitAssetMeta> assetMeta = new HashMap<>();

    private final LinkedHashMap<String, CachedRegion> regionCache = new LinkedHashMap<>(16, 0.75f, true);
    private int maxCacheSize = 128;

    public static final CardPortraitManager INSTANCE = new CardPortraitManager("loadoutMod", true);

    public CardPortraitManager() {
        this("loadoutMod", false);
    }

    public CardPortraitManager(String configId, boolean persistTemp) {
        Path configDir = Paths.get(SpireConfig.makeFilePath(configId, "CardPortraits", "json")).getParent();
        this.portraitsDir = configDir.resolve("portraits");
        this.assetsDir = portraitsDir.resolve("assets");
        this.mapsDir = portraitsDir.resolve("maps");
        this.metaDir = portraitsDir.resolve("meta");
        this.permanentMapPath = mapsDir.resolve("permanent.json");
        this.assetsMetaPath = metaDir.resolve("assets.json");
    }

    public void load() {
        ensureDirectories();
        permanentOverrides.clear();
        permanentOverrides.putAll(readStringMap(permanentMapPath));
        assetMeta.clear();
        assetMeta.putAll(readAssetMetaMap(assetsMetaPath));

        refreshCardLibrary();
    }

    public void save() {
        ensureDirectories();
        writeJsonAtomic(permanentMapPath, permanentOverrides);
        writeJsonAtomic(assetsMetaPath, assetMeta);
    }

    private void refreshCardLibrary() {
        for (AbstractCard card : CardLibrary.cards.values()) {
            if (hasTempPortrait(card)) {
                LoadoutMod.logger.info("Refreshing portrait for card: " + card.cardID);
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



    public void setTempPortrait(AbstractCard card, String assetId) {
        if (card == null || assetId == null) {
            return;
        }
        AbstractCardPatch.setCustomPortraitId(card, assetId);
    }

    public void clearTempPortrait(AbstractCard card) {
        if (card == null) {
            return;
        }
        AbstractCardPatch.setCustomPortraitId(card, "");
    }

    public TextureAtlas.AtlasRegion getPortrait(String cardId, String tempAssetId, TextureAtlas.AtlasRegion fallback) {
        // Precedence: temp override (custom field) > permanent override (cardId) > fallback.
        String assetId = tempAssetId;
        if ((assetId == null || assetId.isEmpty()) && cardId != null) {
            assetId = permanentOverrides.get(cardId);
        }

        if (assetId == null) {
            return fallback;
        }

        CachedRegion cachedRegion = getCachedRegion(assetId);

        if (cachedRegion == null) {
            LoadoutMod.logger.info("Failed to load portrait for card id: " + cardId);
            // Missing/corrupt asset -> drop mapping and fall back.
            if (cardId != null) {
                permanentOverrides.remove(cardId, assetId);
            }
            return fallback;
        }
        return cachedRegion.region;
    }

    public Texture getTexture(String assetId) {
        CachedRegion cachedRegion = getCachedRegion(assetId);

        if (cachedRegion == null) {
            LoadoutMod.logger.info("Failed to load portrait for asset id: " + assetId);
            return null;
        }
        return cachedRegion.texture;
    }

    public Texture getLargeDisposableTexture(String assetId) {
        Path assetPath = assetsDir.resolve(assetId + ".png");
        if (!Files.exists(assetPath)) {
            LoadoutMod.logger.info("Portrait asset missing: " + assetId);
            return null;
        }

        Texture texture;
        try {
            texture = new Texture(new FileHandle(assetPath.toFile()));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            LoadoutMod.logger.info("Failed to load portrait for asset id: " + assetId);
            return null;
        }
        return texture;
    }


    public TextureAtlas.AtlasRegion getTempPortrait(AbstractCard card) {
        String assetId = card != null ? AbstractCardPatch.getCustomPortraitId(card) : null;
        return getPortrait(null, assetId, card != null ? card.portrait : null);
    }

    public static void copyTempPortrait(AbstractCard from, AbstractCard to) {
        if (from == null || to == null) {
            return;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(from);
        
        AbstractCardPatch.setCustomPortraitId(to, assetId);
        applyPortraitOverride(to);
    }

    public void garbageCollectAssets() {
        ensureDirectories();
        Set<String> referenced = new HashSet<>();
        
        // Remove mappings to missing files.
        removeMissingMappings(permanentOverrides);
        referenced.addAll(permanentOverrides.values());

        //Check temp references in deck loadouts
        for(ArrayList<SerializableCard> deck : CardLoadouts.loadouts.values()) {
            for(SerializableCard card : deck) {
                if (card.customPortraitId != null) {
                    referenced.add(card.customPortraitId);
                }
            }
        }

        // Remove unreferenced asset files and metadata.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(assetsDir, ASSET_PREFIX + "*.png")) {
            for (Path assetPath : stream) {
                String fileName = assetPath.getFileName().toString();
                String assetId = fileName.substring(0, fileName.length() - 4); // Remove .png
                if (!referenced.contains(assetId)) {
                    Files.delete(assetPath);
                    disposeCached(assetId);
                    assetMeta.remove(assetId);
                }
            }
        } catch (IOException e) {
            LoadoutMod.logger.error("Failed to garbage collect portrait assets", e);;
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

    private CachedRegion getCachedRegion(String assetId) {
        CachedRegion cached = regionCache.get(assetId);
        if (cached != null) {
            return cached;
        }

        Path assetPath = assetsDir.resolve(assetId + ".png");
        if (!Files.exists(assetPath)) {
            LoadoutMod.logger.info("Portrait asset missing: " + assetId);
            return null;
        }

        try {
            Texture texture = new Texture(new FileHandle(assetPath.toFile()));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
            CachedRegion cachedRegion = new CachedRegion(texture, region);
            regionCache.put(assetId, cachedRegion);
            return cachedRegion;
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

    public static void applyPortraitOverride(AbstractCard card) {
        if (card == null) {
            return;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(card);

        if (assetId != null && !assetId.isEmpty()) {
            CachedRegion region = INSTANCE.getCachedRegion(assetId);
            if (region != null) {
                card.portrait = region.region;
                ReflectionHacks.setPrivate(card, AbstractCard.class, "portraitImg", region.texture);
            }
        }
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
            INSTANCE.setTempPortrait(card, assetId);
            //set card modded
            AbstractCardPatch.setCardModified(card, true);
            applyPortraitOverride(card);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait", e);
        }
    }

    public static void makeTempPortraitPermanent(AbstractCard card) {
        if (card == null) {
            return;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(card);
        if (assetId != null) {
            INSTANCE.setPermanentPortrait(card.cardID, assetId);
            INSTANCE.clearTempPortrait(card);
        }
    }

    public static boolean hasTempPortrait(AbstractCard card) {
        if (card == null) {
            return false;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(card);
        return assetId != null && !assetId.isEmpty();
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


    public static void clearAllPortraits(AbstractCard card) {
        if (card == null) {
            return;
        }
        INSTANCE.clearTempPortrait(card);
        INSTANCE.clearPermanentPortrait(card.cardID);
    }
}
