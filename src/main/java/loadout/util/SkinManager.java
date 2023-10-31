package loadout.util;

import java.util.HashMap;

public class SkinManager {
    public enum Skin {
        DEFAULT, LEGACY, ISAAC, XGGG
    }

    public static final String SKIN_SELECTION = "currentSkinSelection";
    public static final String ICON_FILE_AFFIX = ".png";
    public static final String RELIC_MID = "_relic";

    public static final HashMap<Skin, String> SKIN_AFFIX = new HashMap<>();
    static {
        SKIN_AFFIX.put(Skin.DEFAULT, "");
        SKIN_AFFIX.put(Skin.LEGACY, "_old");
        SKIN_AFFIX.put(Skin.ISAAC, "_alt");
        SKIN_AFFIX.put(Skin.XGGG, "");
    }

    public static Skin currentSkin;

}
