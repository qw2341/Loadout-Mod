package loadout.util;

import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.relics.LoadoutBag;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkinManager {

    public static final String SKIN_SELECTION = "currentSkinSelection";
    public static final String ICON_FILE_AFFIX = ".png";
    public static String currentSkin;

    private final UIStrings uiStrings;
    public final Map<String, String> skinNames;

    public SkinManager() {
        uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("Skins"));
        skinNames = uiStrings.TEXT_DICT;
    }

    private void updateRelics(String skinName) {
        if (!skinNames.containsKey(skinName)) {
            System.out.println("Skin not found: " + skinName);
            return;
        }

        for (Class relicClass : LoadoutRelicHelper.LOADOUT_RELIC_CLASSES) {
            try {
                setSkinForRelicClass(relicClass,skinName);
            } catch (Exception e) {
                LoadoutMod.logger.warn("Unable to load the {} skin, reverting to default", skinName);
                e.printStackTrace();
                //load default
                try {
                    setSkinForRelicClass(relicClass,"default");
                } catch (Exception ex) {
                    LoadoutMod.logger.error("UNABLE TO LOAD DEFAULT SKINS! Somethings not right");
                    ex.printStackTrace();
                }
            }
        }
    }

    private void setSkinForRelicClass(Class relicClass, String skinName) {
        ReflectionHacks.setPrivateStatic(relicClass,"IMG",TextureLoader.getTexture(makeRelicSkinPath(skinName,relicClass.getSimpleName())));
        ReflectionHacks.setPrivateStatic(relicClass,"OUTLINE",TextureLoader.getTexture(makeRelicOutlinePath(skinName,relicClass.getSimpleName())));
    }

    public static String makeRelicSkinPath(String skinName, String relicID) {
        return LoadoutMod.getModID() + "Resources/images/relics/" + skinName + "/" + relicID + ICON_FILE_AFFIX;
    }

    public static String makeRelicOutlinePath(String skinName, String relicID) {
        return LoadoutMod.getModID() + "Resources/images/relics/" + skinName + "/outline/" + relicID + ICON_FILE_AFFIX;
    }

    public void switchSkin(String skinName) {
        updateRelics(skinName);
        currentSkin = skinName;
    }

}
