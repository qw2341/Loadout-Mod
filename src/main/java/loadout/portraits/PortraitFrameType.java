package loadout.portraits;

public enum PortraitFrameType {
    // If the game uses different aspect ratios per frame type, adjust target sizes here.
    ATTACK("Attack", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT),
    SKILL("Skill", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT),
    POWER("Power", CardPortraitManager.TARGET_WIDTH, CardPortraitManager.TARGET_HEIGHT);

    private final String displayName;
    private final double aspect;

    PortraitFrameType(String displayName, int targetW, int targetH) {
        this.displayName = displayName;
        this.aspect = (double) targetW / (double) targetH;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getAspect() {
        return aspect;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
