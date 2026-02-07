package loadout.portraits;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;

import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import loadout.savables.CardLoadouts;
import loadout.savables.CardModifications;
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
    private static final String PORTRAIT_PACKAGE_EXTENSION = ".lpp";
    private static final String PORTRAIT_PACKAGE_MANIFEST = "manifest.json";
    private static final String PORTRAIT_PACKAGE_ASSETS_PREFIX = "assets/";
    private static final int PORTRAIT_PACKAGE_VERSION = 1;
    private static final String DEFAULT_EXPORT_FILE_NAME = "loadout_portraits" + PORTRAIT_PACKAGE_EXTENSION;
    private static final String[] PORTRAIT_PACKAGE_TEXT_FALLBACK = new String[] {
        "Import Portraits",
        "A portrait for \"%s\" already exists. How do you want to handle it?",
        "Overwrite All",
        "Overwrite",
        "Skip All",
        "Skip",
        "Abort",
        "Export Portraits",
        "Portrait export completed successfully.",
        "Portrait import completed successfully."
    };
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

    public boolean exportPortraitPackage() {
        File selected = choosePortraitPackageFile(true);
        if (selected == null) {
            return false;
        }

        Path outputPath = ensurePortraitPackageExtension(selected.toPath());
        try {
            exportPortraitPackage(outputPath);
            showPortraitPackageSuccessDialog(true);
            return true;
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to export portrait package", e);
            return false;
        }
    }

    public boolean importPortraitPackage() {
        File selected = choosePortraitPackageFile(false);
        if (selected == null) {
            return false;
        }
        Path inputPath = selected.toPath();
        if (!hasPortraitPackageExtension(inputPath)) {
            LoadoutMod.logger.warn("Selected portrait package has invalid extension: " + inputPath);
            return false;
        }

        try {
            PortraitPackageData data = readPortraitPackageData(inputPath);
            if (data == null) {
                LoadoutMod.logger.warn("Portrait package missing manifest: " + inputPath);
                return false;
            }

            Map<String, String> incomingOverrides = data.permanentOverrides != null ? data.permanentOverrides : Collections.emptyMap();
            Map<String, PortraitAssetMeta> incomingMeta = data.assetMeta != null ? data.assetMeta : Collections.emptyMap();
            Set<String> assetIds = new HashSet<>();
            assetIds.addAll(incomingOverrides.values());
            assetIds.addAll(incomingMeta.keySet());

            if (!mergePermanentOverridesWithPrompt(incomingOverrides)) {
                return false;
            }

            ensureDirectories();
            extractPortraitPackageAssets(inputPath, assetIds);
            mergeAssetMeta(incomingMeta);
            for (String assetId : assetIds) {
                if (assetId == null || assetId.isEmpty()) {
                    continue;
                }
                boolean hasSmall = Files.exists(assetPathSmall(assetId)) || Files.exists(assetPathLegacy(assetId));
                boolean hasLarge = Files.exists(assetPathLarge(assetId));
                upsertAssetMeta(assetId, null, hasSmall, hasLarge);
            }
            removeMissingMappings(permanentOverrides);
            upgradeLegacyMeta();
            save();
            showPortraitPackageSuccessDialog(false);
            return true;
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to import portrait package", e);
            return false;
        }
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

    private enum ImportConflictDecision {
        OVERWRITE_ALL,
        OVERWRITE,
        SKIP_ALL,
        SKIP,
        ABORT
    }

    private boolean mergePermanentOverridesWithPrompt(Map<String, String> incomingOverrides) {
        if (incomingOverrides == null || incomingOverrides.isEmpty()) {
            return true;
        }

        Map<String, String> merged = new HashMap<>(permanentOverrides);
        ImportConflictDecision defaultDecision = null;

        for (Map.Entry<String, String> entry : incomingOverrides.entrySet()) {
            String cardId = entry.getKey();
            String incomingAssetId = entry.getValue();
            if (cardId == null || incomingAssetId == null) {
                continue;
            }

            String existingAssetId = merged.get(cardId);
            if (existingAssetId != null && !Objects.equals(existingAssetId, incomingAssetId)) {
                ImportConflictDecision decision = defaultDecision;
                if (decision == null) {
                    decision = showImportConflictDialog(cardId);
                    if (decision == ImportConflictDecision.OVERWRITE_ALL || decision == ImportConflictDecision.SKIP_ALL) {
                        defaultDecision = decision;
                    }
                }

                if (decision == ImportConflictDecision.ABORT) {
                    return false;
                }
                if (decision == ImportConflictDecision.SKIP || decision == ImportConflictDecision.SKIP_ALL) {
                    continue;
                }
            }

            saveAsPermanentPortrait(cardId, incomingAssetId);
            merged.put(cardId, incomingAssetId);
        }

        permanentOverrides.clear();
        permanentOverrides.putAll(merged);
        return true;
    }

    private static ImportConflictDecision showImportConflictDialog(String cardId) {
        final ImportConflictDecision[] out = new ImportConflictDecision[1];
        Runnable task = () -> {
            String[] text = getPortraitPackageText();
            String title = text[0];
            String message;
            try {
                message = String.format(text[1], cardId);
            } catch (Exception e) {
                message = text[1] + " " + cardId;
            }
            String[] options = new String[] { text[2], text[3], text[4], text[5], text[6] };

            Frame owner = getFileDialogOwner();
            owner.setAlwaysOnTop(true);
            owner.toFront();
            owner.requestFocus();

            int choice = JOptionPane.showOptionDialog(
                owner,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]
            );

            owner.setAlwaysOnTop(false);

            switch (choice) {
                case 0:
                    out[0] = ImportConflictDecision.OVERWRITE_ALL;
                    break;
                case 1:
                    out[0] = ImportConflictDecision.OVERWRITE;
                    break;
                case 2:
                    out[0] = ImportConflictDecision.SKIP_ALL;
                    break;
                case 3:
                    out[0] = ImportConflictDecision.SKIP;
                    break;
                case 4:
                    out[0] = ImportConflictDecision.ABORT;
                    break;
                default:
                    out[0] = ImportConflictDecision.ABORT;
                    break;
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (Exception e) {
                LoadoutMod.logger.error("Failed to show portrait import conflict dialog", e);
                return ImportConflictDecision.ABORT;
            }
        }

        return out[0] != null ? out[0] : ImportConflictDecision.ABORT;
    }

    private static void showPortraitPackageSuccessDialog(boolean export) {
        Runnable task = () -> {
            String[] text = getPortraitPackageText();
            String title = export ? text[7] : text[0];
            String message = export ? text[8] : text[9];

            Frame owner = getFileDialogOwner();
            owner.setAlwaysOnTop(true);
            owner.toFront();
            owner.requestFocus();
            JOptionPane.showMessageDialog(owner, message, title, JOptionPane.INFORMATION_MESSAGE);
            owner.setAlwaysOnTop(false);
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (Exception e) {
                LoadoutMod.logger.error("Failed to show portrait import/export success dialog", e);
            }
        }
    }

    private void saveAsPermanentPortrait(String cardId, String assetId) {
        if( CardModifications.cardMap.get(cardId)  != null ) {
            CardModifications.cardMap.get(cardId).customPortraitId  = assetId;
        } else {
            SerializableCard sc = SerializableCard.toSerializableCard(CardLibrary.getCard(cardId));
            sc.customPortraitId = assetId;
            CardModifications.cardMap.put(cardId, sc);
        }
        try {
            LoadoutMod.cardModifications.save();
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to save card portraits when importing", e);
        }
    }

    private void exportPortraitPackage(Path outputPath) throws IOException {
        ensureDirectories();
        Map<String, String> exportOverrides = new HashMap<>(permanentOverrides);
        removeMissingMappings(exportOverrides);

        Set<String> assetIds = new HashSet<>(exportOverrides.values());
        Map<String, PortraitAssetMeta> exportMeta = new HashMap<>();
        for (String assetId : assetIds) {
            if (assetId == null || assetId.isEmpty()) {
                continue;
            }
            PortraitAssetMeta meta = assetMeta.get(assetId);
            if (meta == null) {
                meta = buildFallbackMeta(assetId);
            }
            exportMeta.put(assetId, meta);
        }

        PortraitPackageData data = new PortraitPackageData();
        data.version = PORTRAIT_PACKAGE_VERSION;
        data.createdAt = Instant.now().toString();
        data.permanentOverrides = exportOverrides;
        data.assetMeta = exportMeta;

        writePortraitPackage(outputPath, data, assetIds);
    }

    private PortraitPackageData readPortraitPackageData(Path inputPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(inputPath.toFile())) {
            ZipEntry manifestEntry = zipFile.getEntry(PORTRAIT_PACKAGE_MANIFEST);
            if (manifestEntry == null) {
                return null;
            }
            try (InputStream in = zipFile.getInputStream(manifestEntry)) {
                String json = new String(readAllBytes(in), StandardCharsets.UTF_8);
                return CustomSavable.saveFileGson.fromJson(json, PortraitPackageData.class);
            }
        }
    }

    private void writePortraitPackage(Path outputPath, PortraitPackageData data, Set<String> assetIds) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(outputPath))) {
            zip.setLevel(Deflater.BEST_COMPRESSION);

            ZipEntry manifestEntry = new ZipEntry(PORTRAIT_PACKAGE_MANIFEST);
            zip.putNextEntry(manifestEntry);
            zip.write(toJsonBytes(data));
            zip.closeEntry();

            for (String assetId : assetIds) {
                if (assetId == null || assetId.isEmpty()) {
                    continue;
                }
                writeAssetEntryIfExists(zip, assetPathSmall(assetId));
                writeAssetEntryIfExists(zip, assetPathLarge(assetId));
                writeAssetEntryIfExists(zip, assetPathLegacy(assetId));
            }
        }
    }

    private void writeAssetEntryIfExists(ZipOutputStream zip, Path assetPath) throws IOException {
        if (assetPath == null || !Files.exists(assetPath)) {
            return;
        }
        ZipEntry entry = new ZipEntry(PORTRAIT_PACKAGE_ASSETS_PREFIX + assetPath.getFileName().toString());
        zip.putNextEntry(entry);
        try (InputStream in = Files.newInputStream(assetPath)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                zip.write(buffer, 0, read);
            }
        }
        zip.closeEntry();
    }

    private void extractPortraitPackageAssets(Path inputPath, Set<String> assetIds) throws IOException {
        if (assetIds == null || assetIds.isEmpty()) {
            return;
        }
        try (ZipFile zipFile = new ZipFile(inputPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                if (!name.startsWith(PORTRAIT_PACKAGE_ASSETS_PREFIX)) {
                    continue;
                }
                String relative = name.substring(PORTRAIT_PACKAGE_ASSETS_PREFIX.length());
                if (relative.isEmpty()) {
                    continue;
                }
                String fileName = Paths.get(relative).getFileName().toString();
                String assetId = extractAssetId(fileName);
                if (assetId == null || !assetIds.contains(assetId)) {
                    continue;
                }

                Path dest = assetsDir.resolve(relative).normalize();
                if (!dest.startsWith(assetsDir)) {
                    continue;
                }
                if (Files.exists(dest)) {
                    continue;
                }
                Files.createDirectories(dest.getParent());
                try (InputStream in = zipFile.getInputStream(entry)) {
                    Files.copy(in, dest);
                }
            }
        }
    }

    private void mergeAssetMeta(Map<String, PortraitAssetMeta> incomingMeta) {
        if (incomingMeta == null || incomingMeta.isEmpty()) {
            return;
        }
        for (Map.Entry<String, PortraitAssetMeta> entry : incomingMeta.entrySet()) {
            String assetId = entry.getKey();
            PortraitAssetMeta incoming = entry.getValue();
            if (assetId == null || incoming == null) {
                continue;
            }

            PortraitAssetMeta existing = assetMeta.get(assetId);
            if (existing == null) {
                assetMeta.put(assetId, incoming);
                continue;
            }

            if (existing.createdAt == null && incoming.createdAt != null) {
                existing.createdAt = incoming.createdAt;
            }
            if (existing.sourceName == null && incoming.sourceName != null) {
                existing.sourceName = incoming.sourceName;
            }
            if (!existing.hasSmall && incoming.hasSmall) {
                existing.hasSmall = true;
                existing.smallWidth = incoming.smallWidth;
                existing.smallHeight = incoming.smallHeight;
                if (existing.width == 0) {
                    existing.width = incoming.width;
                }
                if (existing.height == 0) {
                    existing.height = incoming.height;
                }
            }
            if (!existing.hasLarge && incoming.hasLarge) {
                existing.hasLarge = true;
                existing.largeWidth = incoming.largeWidth;
                existing.largeHeight = incoming.largeHeight;
            }
        }
    }

    private PortraitAssetMeta buildFallbackMeta(String assetId) {
        PortraitAssetMeta meta = new PortraitAssetMeta();
        meta.assetId = assetId;
        meta.createdAt = Instant.now().toString();
        boolean hasSmall = Files.exists(assetPathSmall(assetId)) || Files.exists(assetPathLegacy(assetId));
        boolean hasLarge = Files.exists(assetPathLarge(assetId));
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
        return meta;
    }

    private static boolean hasPortraitPackageExtension(Path path) {
        if (path == null) {
            return false;
        }
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(PORTRAIT_PACKAGE_EXTENSION);
    }

    private static Path ensurePortraitPackageExtension(Path path) {
        if (path == null) {
            return null;
        }
        if (hasPortraitPackageExtension(path)) {
            return path;
        }
        return path.resolveSibling(path.getFileName().toString() + PORTRAIT_PACKAGE_EXTENSION);
    }

    private static byte[] toJsonBytes(Object obj) {
        StringWriter writer = new StringWriter();
        CustomSavable.saveFileGson.toJson(obj, writer);
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static String[] getPortraitPackageText() {
        try {
            UIStrings strings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PortraitImportExport"));
            if (strings != null && strings.TEXT != null && strings.TEXT.length >= PORTRAIT_PACKAGE_TEXT_FALLBACK.length) {
                return strings.TEXT;
            }
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to load portrait import/export UI strings", e);
        }
        return PORTRAIT_PACKAGE_TEXT_FALLBACK;
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

    private static class PortraitPackageData {
        public int version;
        public String createdAt;
        public Map<String, String> permanentOverrides;
        public Map<String, PortraitAssetMeta> assetMeta;
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

    private static File choosePortraitPackageFile(boolean save) {
        if (SwingUtilities.isEventDispatchThread()) {
            return showPortraitPackageFileDialog(save);
        }
        final File[] selected = new File[1];
        try {
            SwingUtilities.invokeAndWait(() -> selected[0] = showPortraitPackageFileDialog(save));
        } catch (Exception e) {
            LoadoutMod.logger.error("Failed to open portrait package dialog", e);
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

    private static File showPortraitPackageFileDialog(boolean save) {
        Frame owner = getFileDialogOwner();
        String[] text = getPortraitPackageText();
        String title = save ? text[7] : text[0];

        owner.setAlwaysOnTop(true);
        owner.toFront();
        owner.requestFocus();
        FileDialog dialog = getPortraitPackageFileDialog(owner, title, save);
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

    private static FileDialog getPortraitPackageFileDialog(Frame owner, String title, boolean save) {
        FileDialog fileDialog = new FileDialog(owner, title, save ? FileDialog.SAVE : FileDialog.LOAD);
        fileDialog.setModal(true);
        fileDialog.setAlwaysOnTop(true);
        fileDialog.setFilenameFilter((dir, name) -> name != null && name.toLowerCase().endsWith(PORTRAIT_PACKAGE_EXTENSION));
        if (save) {
            fileDialog.setFile(DEFAULT_EXPORT_FILE_NAME);
        } else {
            fileDialog.setFile("*" + PORTRAIT_PACKAGE_EXTENSION);
        }

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
