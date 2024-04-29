package loadout.util;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.relics.AllInOneBag;
import loadout.relics.LoadoutRelic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SkinManager {

    public static final String SKIN_SELECTION = "currentSkinSelection";
    public static final String ICON_FILE_AFFIX = ".png";
    public static String currentSkin;

    private final UIStrings uiStrings;
    public final Map<String, String> skinNames;
    public final ArrayList<String> skinNameList;
    public final ArrayList<String> skinIDList;

    public SkinManager() {
        uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("Skins"));
        //fail safe
        if(uiStrings == null || uiStrings.TEXT_DICT == null) {
            skinNames = new HashMap<>();
            skinNames.put("default","Default");
        } else
            skinNames = uiStrings.TEXT_DICT;

        skinNameList = new ArrayList<>(skinNames.values());
        skinIDList = new ArrayList<>(skinNames.keySet());
    }

    private void updateRelics(String skinID) {
        if (!skinNames.containsKey(skinID)) {
            System.out.println("Skin not found: " + skinID);
            return;
        }

        for (Class relicClass : LoadoutRelicHelper.LOADOUT_RELIC_CLASSES) {
            try {
                setSkinForRelicClass(relicClass,skinID);
            } catch (Exception e) {
                LoadoutMod.logger.warn("Unable to load the {} skin, reverting to default", skinID);
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

    private void setSkinForRelics(String skinID) {
        if(AllInOneBag.INSTANCE == null) {
            updateRelics(skinID);
        } else {
            for(LoadoutRelic lr : AllInOneBag.INSTANCE.loadoutRelics) {
                setSkinForRelic(lr, skinID);
            }
        }

        AllInOneBag.SIDE_PANEL_TAB = TextureLoader.getTexture(SkinManager.makeRelicSkinPath(skinID,"SidePanelTab"));
        AllInOneBag.SIDE_PANEL_ARROW = TextureLoader.getTexture(SkinManager.makeRelicSkinPath(skinID,"SidePanelArrow"));
    }

    private void setSkinForRelic(LoadoutRelic loadoutRelic, String skinID) {
        loadoutRelic.img = TextureLoader.getTexture(makeRelicSkinPath(skinID, loadoutRelic.getClass().getSimpleName()));
        loadoutRelic.outlineImg = TextureLoader.getTexture(makeRelicOutlinePath(skinID, loadoutRelic.getClass().getSimpleName()));
    }

    private void setSkinForRelicClass(Class relicClass, String skinID) {
        ReflectionHacks.setPrivateStatic(relicClass,"IMG",TextureLoader.getTexture(makeRelicSkinPath(skinID,relicClass.getSimpleName())));
        ReflectionHacks.setPrivateStatic(relicClass,"OUTLINE",TextureLoader.getTexture(makeRelicOutlinePath(skinID,relicClass.getSimpleName())));
    }

    public static String makeRelicSkinPath(String skinID, String relicID) {
        return LoadoutMod.getModID() + "Resources/images/relics/" + skinID + "/" + relicID + ICON_FILE_AFFIX;
    }

    public static String makeRelicOutlinePath(String skinID, String relicID) {
        return LoadoutMod.getModID() + "Resources/images/relics/" + skinID + "/outline/" + relicID + ICON_FILE_AFFIX;
    }

    public void switchSkin(String skinID) {
        LoadoutMod.logger.info("Loadout Mod skin switching from " + currentSkin + " to " + skinID);
//        if(currentSkin.equals(skinID)) return;
        setSkinForRelics(skinID);
        currentSkin = skinID;
    }

    public void switchSkin(int skinIndex) {
        switchSkin(skinIDList.get(skinIndex));
    }

    public int getSkinIndex(String skinIDToGet) {
       return skinIDList.indexOf(skinIDToGet);
    }
}
