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
import javax.swing.SwingUtilities;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
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
    // Large portrait size for inspect/1024Portraits style usage (adjust if needed).
    public static final int LARGE_WIDTH = 500;
    public static final int LARGE_HEIGHT = 380;
    public static final String ASSET_PREFIX = "sha256_";
    private static final String SMALL_SUFFIX = "_small";
    private static final String LARGE_SUFFIX = "_large";
    // Debug: skip decoding/crop UI to isolate file dialog lag.
    private static final boolean DEBUG_SKIP_CROP_DECODE = true;

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
        upgradeLegacyMeta();

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

        BufferedImage src = ImageUtil.readImage(file);
        ImageUtil.CropRect crop = ImageUtil.centerCropRect(src, getPortraitAspect());
        BufferedImage large = ImageUtil.cropAndScale(src, crop, LARGE_WIDTH, LARGE_HEIGHT);
        return storePortraitAssets(large, sourceNameOptional);
    }

    public String importPortraitWithCropUI(File file, String sourceNameOptional) throws IOException {
        return importPortraitWithCropUI(file, sourceNameOptional, PortraitFrameType.ATTACK);
    }

    public String importPortraitWithCropUI(File file, String sourceNameOptional, PortraitFrameType defaultFrameType) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("Portrait source file missing.");
        }

        BufferedImage src = ImageUtil.readImage(file);
        PortraitCropDialog.CropResult cropResult = PortraitCropDialog.showDialog(src, defaultFrameType);
        if (cropResult == null) {
            return null;
        }

        BufferedImage large = ImageUtil.cropAndScale(src, cropResult.cropRect, LARGE_WIDTH, LARGE_HEIGHT);
        return storePortraitAssets(large, sourceNameOptional);
    }

    private String storePortraitAssets(BufferedImage largeImage, String sourceNameOptional) throws IOException {
        // Hash the LARGE PNG bytes so identical crops map to the same assetId; small is derived deterministically.
        byte[] largePngBytes = toPngBytes(largeImage);
        String assetId = ASSET_PREFIX + sha256Hex(largePngBytes);
        Path largePath = assetPathLarge(assetId);
        Path smallPath = assetPathSmall(assetId);
        ensureDirectories();

        if (!Files.exists(largePath)) {
            writeBytesAtomic(largePath, largePngBytes);
        }
        if (!Files.exists(smallPath)) {
            BufferedImage smallImage = ImageUtil.scale(largeImage, TARGET_WIDTH, TARGET_HEIGHT);
            byte[] smallPngBytes = toPngBytes(smallImage);
            writeBytesAtomic(smallPath, smallPngBytes);
        }

        upsertAssetMeta(assetId, sourceNameOptional, true, true);
        return assetId;
    }

    private static double getPortraitAspect() {
        return (double) TARGET_WIDTH / (double) TARGET_HEIGHT;
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

    public String getResolvedAssetId(AbstractCard card) {
        if (card == null) {
            return null;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(card);
        if (assetId == null || assetId.isEmpty()) {
            assetId = permanentOverrides.get(card.cardID);
        }
        return (assetId == null || assetId.isEmpty()) ? null : assetId;
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
        Path largePath = assetPathLarge(assetId);
        if (!Files.exists(largePath)) {
            Path sourcePath = assetPathLegacy(assetId);
            if (!Files.exists(sourcePath)) {
                sourcePath = assetPathSmall(assetId);
            }
            if (Files.exists(sourcePath)) {
                try {
                    BufferedImage src = ImageUtil.readImage(sourcePath.toFile());
                    BufferedImage largeImage = src;
                    if (src.getWidth() != LARGE_WIDTH || src.getHeight() != LARGE_HEIGHT) {
                        largeImage = ImageUtil.scale(src, LARGE_WIDTH, LARGE_HEIGHT);
                    }
                    writeBytesAtomic(largePath, toPngBytes(largeImage));
                    upsertAssetMeta(assetId, null, Files.exists(assetPathSmall(assetId)) || Files.exists(assetPathLegacy(assetId)), true);
                } catch (Exception e) {
                    LoadoutMod.logger.info("Failed to upgrade large portrait for asset id: " + assetId, e);
                }
            }
        }

        Path assetPath = resolveLargePath(assetId);
        if (assetPath == null) {
            LoadoutMod.logger.info("Portrait asset missing: " + assetId);
            return null;
        }

        try {
            Texture texture = new Texture(new FileHandle(assetPath.toFile()));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        } catch (Exception e) {
            LoadoutMod.logger.info("Failed to load portrait for asset id: " + assetId);
            return null;
        }
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
        Set<String> removedAssets = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(assetsDir, ASSET_PREFIX + "*.png")) {
            for (Path assetPath : stream) {
                String assetId = extractAssetId(assetPath.getFileName().toString());
                if (assetId == null) {
                    Files.delete(assetPath);
                    continue;
                }
                if (!referenced.contains(assetId)) {
                    disposeCached(assetId);
                    assetMeta.remove(assetId);
                    Files.delete(assetPath);
                    removedAssets.add(assetId);
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

    private Path assetPathSmall(String assetId) {
        return assetsDir.resolve(assetId + SMALL_SUFFIX + ".png");
    }

    private Path assetPathLarge(String assetId) {
        return assetsDir.resolve(assetId + LARGE_SUFFIX + ".png");
    }

    private Path assetPathLegacy(String assetId) {
        return assetsDir.resolve(assetId + ".png");
    }

    private Path resolveSmallPath(String assetId) {
        Path small = assetPathSmall(assetId);
        if (Files.exists(small)) {
            return small;
        }
        Path legacy = assetPathLegacy(assetId);
        if (Files.exists(legacy)) {
            return legacy;
        }
        Path large = assetPathLarge(assetId);
        if (Files.exists(large)) {
            return large;
        }
        return null;
    }

    private Path resolveLargePath(String assetId) {
        Path large = assetPathLarge(assetId);
        if (Files.exists(large)) {
            return large;
        }
        Path legacy = assetPathLegacy(assetId);
        if (Files.exists(legacy)) {
            return legacy;
        }
        Path small = assetPathSmall(assetId);
        if (Files.exists(small)) {
            return small;
        }
        return null;
    }

    private boolean assetExists(String assetId) {
        return Files.exists(assetPathSmall(assetId))
            || Files.exists(assetPathLarge(assetId))
            || Files.exists(assetPathLegacy(assetId));
    }

    private String extractAssetId(String fileName) {
        if (!fileName.endsWith(".png") || !fileName.startsWith(ASSET_PREFIX)) {
            return null;
        }
        String base = fileName.substring(0, fileName.length() - 4);
        if (base.endsWith(SMALL_SUFFIX)) {
            return base.substring(0, base.length() - SMALL_SUFFIX.length());
        }
        if (base.endsWith(LARGE_SUFFIX)) {
            return base.substring(0, base.length() - LARGE_SUFFIX.length());
        }
        return base;
    }

    private void upsertAssetMeta(String assetId, String sourceNameOptional, boolean hasSmall, boolean hasLarge) {
        PortraitAssetMeta meta = assetMeta.get(assetId);
        if (meta == null) {
            meta = new PortraitAssetMeta();
            meta.assetId = assetId;
            meta.createdAt = Instant.now().toString();
            assetMeta.put(assetId, meta);
        }

        if (sourceNameOptional != null) {
            meta.sourceName = sourceNameOptional;
        }

        if (hasSmall) {
            meta.hasSmall = true;
            meta.width = TARGET_WIDTH;
            meta.height = TARGET_HEIGHT;
            meta.smallWidth = TARGET_WIDTH;
            meta.smallHeight = TARGET_HEIGHT;
        }

        if (hasLarge) {
            meta.hasLarge = true;
            meta.largeWidth = LARGE_WIDTH;
            meta.largeHeight = LARGE_HEIGHT;
        }
    }

    private void upgradeLegacyMeta() {
        for (PortraitAssetMeta meta : assetMeta.values()) {
            if (meta == null || meta.assetId == null) {
                continue;
            }
            if (meta.smallWidth == 0 && meta.width > 0) {
                meta.smallWidth = meta.width;
                meta.smallHeight = meta.height;
                meta.hasSmall = true;
            }
            if (!meta.hasSmall && Files.exists(assetPathSmall(meta.assetId))) {
                meta.hasSmall = true;
                meta.smallWidth = TARGET_WIDTH;
                meta.smallHeight = TARGET_HEIGHT;
            }
            if (!meta.hasSmall && Files.exists(assetPathLegacy(meta.assetId))) {
                meta.hasSmall = true;
                meta.smallWidth = meta.width > 0 ? meta.width : TARGET_WIDTH;
                meta.smallHeight = meta.height > 0 ? meta.height : TARGET_HEIGHT;
            }
            if (!meta.hasLarge && Files.exists(assetPathLarge(meta.assetId))) {
                meta.hasLarge = true;
                meta.largeWidth = LARGE_WIDTH;
                meta.largeHeight = LARGE_HEIGHT;
            }
        }
    }

    private CachedRegion getCachedRegion(String assetId) {
        CachedRegion cached = regionCache.get(assetId);
        if (cached != null) {
            return cached;
        }

        Path assetPath = resolveSmallPath(assetId);
        if (assetPath == null) {
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
            if (!assetExists(entry.getValue())) {
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
        // Deterministic PNG encoding for hashing (no metadata, just pixel content).
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
                ReflectionHacks.setPrivate(card, AbstractCard.class, "portraitImg", INSTANCE.getLargeDisposableTexture(assetId));
            }
        }
    }

    private static PortraitFrameType getDefaultFrameType(AbstractCard card) {
        if (card == null || card.type == null) {
            return PortraitFrameType.SKILL;
        }
        switch (card.type) {
            case ATTACK:
                return PortraitFrameType.ATTACK;
            case POWER:
                return PortraitFrameType.POWER;
            default:
                return PortraitFrameType.SKILL;
        }
    }

    public static void chooseFileAndSetPermanentPortrait(String cardId) {
        File selected = choosePortraitFile();
        if (selected == null) {
            return;
        }
        if (DEBUG_SKIP_CROP_DECODE) {
            LoadoutMod.logger.info("Portrait import skipped (DEBUG_SKIP_CROP_DECODE).");
            return;
        }
        try {
            String assetId = INSTANCE.importPortraitWithCropUI(selected, selected.getName(), PortraitFrameType.ATTACK);
            if (assetId == null) {
                return;
            }
            INSTANCE.setPermanentPortrait(cardId, assetId);
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait", e);
        }
    }

    public static boolean chooseFileAndSetTempPortrait(AbstractCard card) {
        File selected = choosePortraitFile();
        if (selected == null) {
            return false;
        }

        try {
            PortraitFrameType defaultFrame = getDefaultFrameType(card);
            String assetId = INSTANCE.importPortraitWithCropUI(selected, selected.getName(), defaultFrame);
            if (assetId == null) {
                return false;
            }
            INSTANCE.setTempPortrait(card, assetId);
//            System.out.println("Portrait imported: " + assetId + " and card now has custom portrait id of: " + AbstractCardPatch.getCustomPortraitId(card));
            //set card modded
            AbstractCardPatch.setCardModified(card, true);
            applyPortraitOverride(card);
            return true;
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait", e);
            return false;
        }
    }

    public static void chooseFileAndSetTempPortraitAsync(AbstractCard card, Runnable onApplied) {
        if (card == null) {
            return;
        }
        Thread worker = new Thread(() -> {
            File selected = choosePortraitFile();
            if (selected == null) {
                return;
            }
            String assetId;
            try {
                PortraitFrameType defaultFrame = getDefaultFrameType(card);
                assetId = INSTANCE.importPortraitWithCropUI(selected, selected.getName(), defaultFrame);
                if (assetId == null) {
                    return;
                }
            } catch (Exception e) {
                LoadoutMod.logger.error("Failed to import portrait", e);
                return;
            }
            Gdx.app.postRunnable(() -> {
                INSTANCE.setTempPortrait(card, assetId);
                applyPortraitOverride(card);
                if (onApplied != null) {
                    onApplied.run();
                }
            });
        }, "LoadoutPortraitPicker");
        worker.setDaemon(true);
        worker.start();
    }

    public static void chooseFileAndSetPermanentPortraitAsync(String cardId, Runnable onApplied) {
        if (cardId == null) {
            return;
        }
        Thread worker = new Thread(() -> {
            File selected = choosePortraitFile();
            if (selected == null) {
                return;
            }
            String assetId;
            try {
                assetId = INSTANCE.importPortraitWithCropUI(selected, selected.getName(), PortraitFrameType.ATTACK);
                if (assetId == null) {
                    return;
                }
            } catch (Exception e) {
                LoadoutMod.logger.error("Failed to import portrait", e);
                return;
            }
            Gdx.app.postRunnable(() -> {
                INSTANCE.setPermanentPortrait(cardId, assetId);
                if (onApplied != null) {
                    onApplied.run();
                }
            });
        }, "LoadoutPortraitPicker");
        worker.setDaemon(true);
        worker.start();
    }

    private static File choosePortraitFile() {
        if (SwingUtilities.isEventDispatchThread()) {
            return showFileDialog();
        }
        final File[] selected = new File[1];
        try {
            SwingUtilities.invokeAndWait(() -> selected[0] = showFileDialog());
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to open portrait picker", e);
            return null;
        }
        return selected[0];
    }

    private static Frame fileDialogOwner;
//    private static FileDialog fileDialog;

    private static Frame getFileDialogOwner() {
        if (fileDialogOwner == null) {
            Frame owner = new Frame();
            owner.setUndecorated(true);
            owner.setSize(0, 0);
            owner.setLocationRelativeTo(null);
            owner.setAlwaysOnTop(true);
            owner.setVisible(false);
            fileDialogOwner = owner;
        }

        return fileDialogOwner;
    }

    private static File showFileDialog() {
        Frame owner = getFileDialogOwner();

        owner.setAlwaysOnTop(true);
        owner.toFront();
        owner.requestFocus();
        FileDialog dialog = getFileDialog(owner);
        dialog.setVisible(true);
        File selected = null;
        if (dialog.getFile() != null) {
            selected = new File(dialog.getDirectory(), dialog.getFile());
        }
        dialog.setFile(null);
        owner.setAlwaysOnTop(false);

        return selected;
    }

    private static FileDialog getFileDialog(Frame owner) {

        FileDialog fileDialog = new FileDialog(owner, "Select portrait", FileDialog.LOAD);
        fileDialog.setModal(true);
        fileDialog.setAlwaysOnTop(true);

        return fileDialog;
    }

    public static void makeTempPortraitPermanent(AbstractCard card) {
        if (card == null) {
            return;
        }
        String assetId = AbstractCardPatch.getCustomPortraitId(card);
        if (assetId != null) {
            INSTANCE.setPermanentPortrait(card.cardID, assetId);
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
