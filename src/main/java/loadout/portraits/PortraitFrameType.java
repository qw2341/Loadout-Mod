package loadout.portraits;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum PortraitFrameType {
    // If the game uses different aspect ratios per frame type, adjust target sizes here.
    ATTACK("Attack", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT),
    SKILL("Skill", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT),
    POWER("Power", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT);

    private static final String MASK_DIR = "loadoutResources/masks/";
    private static final Map<String, BufferedImage> MASK_CACHE = new ConcurrentHashMap<>();

    private final String displayName;
    private final double aspect;
    private final String maskBaseName;

    PortraitFrameType(String displayName, int targetW, int targetH) {
        this.displayName = displayName;
        this.aspect = (double) targetW / (double) targetH;
        this.maskBaseName = displayName + "Mask";
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getAspect() {
        return aspect;
    }

    public String getSmallMaskPath() {
        return MASK_DIR + maskBaseName + ".png";
    }

    public String getLargeMaskPath() {
        return MASK_DIR + maskBaseName + "_p.png";
    }

    public BufferedImage getSmallMask() {
        return loadMask(getSmallMaskPath());
    }

    public BufferedImage getLargeMask() {
        return loadMask(getLargeMaskPath());
    }

    private static BufferedImage loadMask(String path) {
        if (path == null) {
            return null;
        }
        return MASK_CACHE.computeIfAbsent(path, ImageUtil::readResourceImageQuiet);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
