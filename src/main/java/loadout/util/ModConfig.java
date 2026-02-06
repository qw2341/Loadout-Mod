package loadout.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.compendium.RelicViewScreen;

import basemod.BaseMod;
import basemod.ModLabeledButton;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import loadout.LoadoutMod;
import static loadout.LoadoutMod.skinManager;
import loadout.portraits.CardPortraitManager;
import loadout.relics.AllInOneBag;
import loadout.savables.CardModifications;
import loadout.uiElements.ModLabeledDropdown;

public class ModConfig {

    public static final String SETTINGS_STRINGS = "loadoutResources/localization/eng/UI-Strings.json";

    //Mod Badge - A small icon that appears in the mod settings menu next to your mod.
    public static final String BADGE_IMAGE = "loadoutResources/images/Badge.png";
    public static final String ENABLE_LEGACY_LAYOUT = "enableLegacyLayout";
    public static final String IGNORE_UNLOCK_PROGRESS = "ignoreUnlockProgress";
    public static final String ENABLE_STARTER_POOL = "enableStarterPool";
    public static final String ENABLE_COMMON_POOL = "enableCommonPool";
    public static final String ENABLE_UNCOMMON_POOL = "enableUncommonPool";
    public static final String ENABLE_RARE_POOL = "enableRarePool";
    public static final String ENABLE_BOSS_POOL = "enableBossPool";
    public static final String ENABLE_SHOP_POOL = "enableShopPool";
    public static final String ENABLE_EVENT_POOL = "enableEventPool";
    public static final String ENABLE_DEPRECATED_POOL = "enableDeprecatedPool";
    public static final String ENABLE_CATEGORY = "enableCategory";
    public static final String ENABLE_DESC = "enableDescriptions";
    public static final String ENABLE_DRAG_SELECT = "enableDragSelection";
    public static final String ENABLE_CREATURE_MANIPULATION = "enableCreatureManipulation";
    public static final String RELIC_OBTAIN_AMOUNT = "amountOfRelicToObtain";
    public static final String REMOVE_RELIC_FROM_POOLS = "removeRelicFromPools";
    // Mod-settings settings. This is if you want an on/off savable button
    public static SpireConfig config = null;
    public static Properties theDefaultDefaultSettings = new Properties();
    public static boolean enableLegacyLayout = true; // The boolean we'll be setting on/off (true/false)
    public static boolean ignoreUnlock = false;
    public static boolean enableStarterPool = true;
    public static boolean enableCommonPool = true;
    public static boolean enableUncommonPool = true;
    public static boolean enableRarePool = true;
    public static boolean enableBossPool = true;
    public static boolean enableShopPool = true;
    public static boolean enableEventPool = true;
    public static boolean enableDeprecatedPool = false;
    public static boolean enableCategory = true;
    public static boolean enableDesc = true;
    public static boolean enableDrag = true;
    public static boolean enableCreatureManipulation = true;
    public static int relicObtainMultiplier = 1;
    public static boolean enableRemoveFromPool = false;

    public static final String ENABLE_ID_DISPLAY = "enableIDDisplay";
    public static boolean enableIDDisplay = false;

    public static void initModSettings() {

        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_LEGACY_LAYOUT, "FALSE"); // This is the default setting. It's actually set...

        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.IGNORE_UNLOCK_PROGRESS, "FALSE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_STARTER_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_COMMON_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_UNCOMMON_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_RARE_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_BOSS_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_SHOP_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_EVENT_POOL,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_DEPRECATED_POOL,"FALSE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_CATEGORY,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_DESC,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_DRAG_SELECT,"TRUE");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.RELIC_OBTAIN_AMOUNT,"1");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.REMOVE_RELIC_FROM_POOLS,"FALSE");
        ModConfig.theDefaultDefaultSettings.setProperty(SkinManager.SKIN_SELECTION, "default");
        ModConfig.theDefaultDefaultSettings.setProperty(ModConfig.ENABLE_CREATURE_MANIPULATION, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_ID_DISPLAY,"FALSE");

        try {
            ModConfig.config = new SpireConfig("loadoutMod", "theLoadoutConfig", ModConfig.theDefaultDefaultSettings); // ...right here
            // the "fileName" parameter is the name of the file MTS will create where it will save our setting.
            ModConfig.config.load(); // Load the setting and set the boolean to equal it
            ModConfig.enableLegacyLayout = ModConfig.config.getBool(ModConfig.ENABLE_LEGACY_LAYOUT);

            ModConfig.ignoreUnlock = ModConfig.config.getBool(ModConfig.IGNORE_UNLOCK_PROGRESS);
            ModConfig.enableStarterPool = ModConfig.config.getBool(ModConfig.ENABLE_STARTER_POOL);
            ModConfig.enableCommonPool = ModConfig.config.getBool(ModConfig.ENABLE_COMMON_POOL);
            ModConfig.enableUncommonPool = ModConfig.config.getBool(ModConfig.ENABLE_UNCOMMON_POOL);
            ModConfig.enableRarePool = ModConfig.config.getBool(ModConfig.ENABLE_RARE_POOL);
            ModConfig.enableBossPool = ModConfig.config.getBool(ModConfig.ENABLE_BOSS_POOL);
            ModConfig.enableShopPool = ModConfig.config.getBool(ModConfig.ENABLE_SHOP_POOL);
            ModConfig.enableEventPool = ModConfig.config.getBool(ModConfig.ENABLE_EVENT_POOL);
            ModConfig.enableDeprecatedPool = ModConfig.config.getBool(ModConfig.ENABLE_DEPRECATED_POOL);
            ModConfig.enableCategory = ModConfig.config.getBool(ModConfig.ENABLE_CATEGORY);
            ModConfig.enableDesc = ModConfig.config.getBool(ModConfig.ENABLE_DESC);
            ModConfig.enableDrag = ModConfig.config.getBool(ModConfig.ENABLE_DRAG_SELECT);
            ModConfig.relicObtainMultiplier = ModConfig.config.getInt(ModConfig.RELIC_OBTAIN_AMOUNT);
            ModConfig.enableRemoveFromPool = ModConfig.config.getBool(ModConfig.REMOVE_RELIC_FROM_POOLS);

            SkinManager.currentSkin = ModConfig.config.getString(SkinManager.SKIN_SELECTION);
            ModConfig.enableCreatureManipulation = ModConfig.config.getBool(ModConfig.ENABLE_CREATURE_MANIPULATION);
            enableIDDisplay = config.getBool(ENABLE_ID_DISPLAY);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void initModConfigMenu() {
        // Load the Mod Badge
        Texture badgeTexture = TextureLoader.getTexture(BADGE_IMAGE);

        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();
        float startingXPos = 350.0f;
        float settingXPos = startingXPos;
        float xSpacing = 250.0f;
        float settingYPos = 750.0f;
        float lineSpacing = 50.0f;
        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("OptionsMenu"));
        String[] SettingText = UIStrings.TEXT;
        // Create the on/off button:

        ModLabeledToggleButton ignoreUnlocksButton = new ModLabeledToggleButton(SettingText[1],
                350.0f, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                ignoreUnlock, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    ignoreUnlock = button.enabled;
                    try {
                        config.setBool(IGNORE_UNLOCK_PROGRESS, ignoreUnlock);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(ignoreUnlocksButton);

        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableStarterPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[1]),
                SettingText[2], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableStarterPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableStarterPool = button.enabled;
                    try {
                        config.setBool(ENABLE_STARTER_POOL, enableStarterPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableStarterPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableCommonPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[3]),
                SettingText[3], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableCommonPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableCommonPool = button.enabled;
                    try {
                        config.setBool(ENABLE_COMMON_POOL, enableCommonPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableCommonPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableUncommonPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[5]),
                SettingText[4], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableUncommonPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableUncommonPool = button.enabled;
                    try {
                        config.setBool(ENABLE_UNCOMMON_POOL, enableUncommonPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableUncommonPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableRarePoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[7]),
                SettingText[5], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableRarePool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableRarePool = button.enabled;
                    try {
                        config.setBool(ENABLE_RARE_POOL, enableRarePool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableRarePoolButton);

        settingXPos = startingXPos;
        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableBossPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[9]),
                SettingText[6], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableBossPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableBossPool = button.enabled;
                    try {
                        config.setBool(ENABLE_BOSS_POOL, enableBossPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableBossPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableShopPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[13]),
                SettingText[7], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableShopPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableShopPool = button.enabled;
                    try {
                        config.setBool(ENABLE_SHOP_POOL, enableShopPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableShopPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableEventPoolButton = new ModLabeledToggleButton(StringUtils.chop(RelicViewScreen.TEXT[11]),
                SettingText[8], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableEventPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableEventPool = button.enabled;
                    try {
                        config.setBool(ENABLE_EVENT_POOL, enableEventPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableEventPoolButton);

        settingXPos += xSpacing;

        ModLabeledToggleButton enableDeprecatedPoolButton = new ModLabeledToggleButton(SettingText[9],
                SettingText[9], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableDeprecatedPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableDeprecatedPool = button.enabled;
                    try {
                        config.setBool(ENABLE_DEPRECATED_POOL, enableDeprecatedPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        //TODO
        settingsPanel.addUIElement(enableDeprecatedPoolButton);

        settingXPos = startingXPos;
        settingYPos -= lineSpacing;


        ModLabeledToggleButton enableDescButton = new ModLabeledToggleButton(SettingText[12],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableDesc, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableDesc = button.enabled;
                    try {
                        config.setBool(ENABLE_DESC, enableDesc);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableDescButton);

        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableCategoryButton = new ModLabeledToggleButton(SettingText[11],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableCategory, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableCategory = button.enabled;
                    if (!button.enabled && enableDescButton.toggle.enabled) {
                        //enableDesc = button.enabled;
                        enableDescButton.toggle.toggle();
                    }

                    try {
                        config.setBool(ENABLE_CATEGORY, enableCategory);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableCategoryButton);

        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableRemoveFromPoolButton = new ModLabeledToggleButton(SettingText[13],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableRemoveFromPool, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableRemoveFromPool = button.enabled;
                    try {
                        config.setBool(REMOVE_RELIC_FROM_POOLS, enableRemoveFromPool);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableRemoveFromPoolButton);

        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableCreatureManipButton = new ModLabeledToggleButton(SettingText[18],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableCreatureManipulation, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableCreatureManipulation = button.enabled;
                    try {
                        config.setBool(ENABLE_CREATURE_MANIPULATION, enableCreatureManipulation);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableCreatureManipButton);

        settingYPos -= lineSpacing;
//        ModLabeledToggleButton enableIDDisplayButton = new ModLabeledToggleButton(SettingText[20],SettingText[21],
//                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
//                enableIDDisplay, // Boolean it uses
//                settingsPanel, // The mod panel in which this button will be in
//                (label) -> {}, // thing??????? idk
//                (button) -> { // The actual button:
//                    enableIDDisplay = button.enabled;
//                    try {
//                        config.setBool(ENABLE_ID_DISPLAY, enableIDDisplay);
//                        config.save();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//        settingsPanel.addUIElement(enableIDDisplayButton);
//
//        settingYPos -= lineSpacing;
        ModLabeledDropdown skinSeletion = new ModLabeledDropdown(SettingText[19],null,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, skinManager.skinNameList,
                (label) -> {}, (dropdownMenu) -> {
            if(dropdownMenu.getHitbox().justHovered) {
                AllInOneBag.INSTANCE.showRelics();
            }
            if (!dropdownMenu.getHitbox().hovered && AllInOneBag.isSelectionScreenUp) {
//                skinManager.switchSkin(SkinManager.currentSkin);
                AllInOneBag.INSTANCE.hideAllRelics();
            }
            //RIP Dropdown row got private access, cant show individual skin when hovered :(

        },
                (i, skinName) -> {
                    try {
                        skinManager.switchSkin(i);
                        config.setString(SkinManager.SKIN_SELECTION, SkinManager.currentSkin);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        skinSeletion.dropdownMenu.setSelectedIndex(skinManager.getSkinIndex(SkinManager.currentSkin));
        settingsPanel.addUIElement(skinSeletion);

        settingYPos -= lineSpacing * 2;




        settingYPos -= lineSpacing;
        settingXPos += 800.0f;

        ModLabeledButton removeModificationsButton = new ModLabeledButton(SettingText[15],settingXPos, settingYPos, Settings.CREAM_COLOR, Settings.RED_TEXT_COLOR, FontHelper.charDescFont, settingsPanel,
                (button) -> {
                    //restore the changes in CardLib
                    CardModifications.restoreAllCardsInLibrary();

                    CardModifications.cardMap.clear();

                    try {
                        LoadoutMod.cardModifications.save();
                    } catch (IOException e) {
                        LoadoutMod.logger.error("Error occurred while saving card modification after clearing");
                    }
                });
        settingsPanel.addUIElement(removeModificationsButton);

        settingYPos -= 2 * lineSpacing;

        ModLabeledButton exportCustomPortraitButton = new ModLabeledButton(SettingText[20],settingXPos, settingYPos, Settings.CREAM_COLOR, Settings.GOLD_COLOR, FontHelper.charDescFont, settingsPanel,
                (button) -> {
                    CardPortraitManager.INSTANCE.exportPortraitPackage();
                });
        settingsPanel.addUIElement(exportCustomPortraitButton);

        settingYPos -= 2 * lineSpacing;

        ModLabeledButton importCustomPortraitButton = new ModLabeledButton(SettingText[21],settingXPos, settingYPos, Settings.CREAM_COLOR, Settings.GOLD_COLOR, FontHelper.charDescFont, settingsPanel,
                (button) -> {
                    CardPortraitManager.INSTANCE.importPortraitPackage();
                });
        settingsPanel.addUIElement(importCustomPortraitButton);

        BaseMod.registerModBadge(badgeTexture, LoadoutMod.MODNAME, LoadoutMod.AUTHOR, LoadoutMod.DESCRIPTION, settingsPanel);

    }
}
