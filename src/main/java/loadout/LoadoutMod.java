package loadout;

import basemod.*;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.beyond.*;
import com.megacrit.cardcrawl.events.city.*;
import com.megacrit.cardcrawl.events.exordium.*;
import com.megacrit.cardcrawl.events.shrines.*;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.deprecated.DEPRECATEDDodecahedron;
import com.megacrit.cardcrawl.relics.deprecated.DEPRECATEDYin;
import com.megacrit.cardcrawl.relics.deprecated.DEPRECATED_DarkCore;
import com.megacrit.cardcrawl.relics.deprecated.DerpRock;
import com.megacrit.cardcrawl.screens.compendium.RelicViewScreen;
import javassist.CtClass;
import javassist.NotFoundException;
import loadout.helper.ModifierLibrary;
import loadout.helper.RelicNameComparator;
import loadout.savables.CardModifications;
import loadout.savables.Favorites;
import loadout.savables.SerializableCard;
import loadout.screens.EventSelectScreen;
import loadout.util.PowerFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loadout.relics.*;
import loadout.util.IDCheckDontTouchPls;
import loadout.util.TextureLoader;
import org.clapper.util.classutil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static basemod.BaseMod.gson;

//TODO: DON'T MASS RENAME/REFACTOR
//TODO: DON'T MASS RENAME/REFACTOR
//TODO: DON'T MASS RENAME/REFACTOR
//TODO: DON'T MASS RENAME/REFACTOR
// Please don't just mass replace "theDefault" with "yourMod" everywhere.
// It'll be a bigger pain for you. You only need to replace it in 4 places.
// I comment those places below, under the place where you set your ID.

//TODO: FIRST THINGS FIRST: RENAME YOUR PACKAGE AND ID NAMES FIRST-THING!!!
// Right click the package (Open the project pane on the left. Folder with black dot on it. The name's at the very top) -> Refactor -> Rename, and name it whatever you wanna call your mod.
// Scroll down in this file. Change the ID from "theDefault:" to "yourModName:" or whatever your heart desires (don't use spaces). Dw, you'll see it.
// In the JSON strings (resources>localization>eng>[all them files] make sure they all go "yourModName:" rather than "theDefault", and change to "yourmodname" rather than "thedefault".
// You can ctrl+R to replace in 1 file, or ctrl+shift+r to mass replace in specific files/directories, and press alt+c to make the replace case sensitive (Be careful.).
// Start with the DefaultCommon cards - they are the most commented cards since I don't feel it's necessary to put identical comments on every card.
// After you sorta get the hang of how to make cards, check out the card template which will make your life easier

/*
 * With that out of the way:
 * Welcome to this super over-commented Slay the Spire modding base.
 * Use it to make your own mod of any type. - If you want to add any standard in-game content (character,
 * cards, relics), this is a good starting point.
 * It features 1 character with a minimal set of things: 1 card of each type, 1 debuff, couple of relics, etc.
 * If you're new to modding, you basically *need* the BaseMod wiki for whatever you wish to add
 * https://github.com/daviscook477/BaseMod/wiki - work your way through with this base.
 * Feel free to use this in any way you like, of course. MIT licence applies. Happy modding!
 *
 * And pls. Read the comments.
 */

@SpireInitializer
public class LoadoutMod implements
        EditRelicsSubscriber,
        EditStringsSubscriber,
        PostInitializeSubscriber,
PostDungeonInitializeSubscriber,
StartGameSubscriber{
    // Make sure to implement the subscribers *you* are using (read basemod wiki). Editing cards? EditCardsSubscriber.
    // Making relics? EditRelicsSubscriber. etc., etc., for a full list and how to make your own, visit the basemod wiki.
    public static final Logger logger = LogManager.getLogger(LoadoutMod.class.getName());
    private static String modID;

    // Mod-settings settings. This is if you want an on/off savable button
    public static SpireConfig config = null;
    public static Properties theDefaultDefaultSettings = new Properties();
    public static final String ENABLE_AS_STARTING_RELIC = "enableAsStartingRelic";
    public static boolean enableStarting = true; // The boolean we'll be setting on/off (true/false)

    public static final String ENABLE_STARTING_LOADOUT_BAG = "enableLoadoutBagStarting";
    public static boolean enableBagStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_BIN = "enableTrashBinStarting";
    public static boolean enableBinStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_CAULDRON = "enableLoadoutCauldronStarting";
    public static boolean enableCauldronStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_PRINTER = "enableCardPrinterStarting";
    public static boolean enablePrinterStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_SHREDDER = "enableCardShredderStarting";
    public static boolean enableShredderStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_MODIFIER = "enableCardModifierStarting";
    public static boolean enableModifierStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_COMPASS = "enableEventfulCompassStarting";
    public static boolean enableCompassStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_POWER = "enablePowerGiverStarting";
    public static boolean enablePowerStarting = true;

    public static final String ENABLE_STARTING_LOADOUT_TILDE = "enableTildeKeyStarting";
    public static boolean enableTildeStarting = true;

    public static final String IGNORE_UNLOCK_PROGRESS = "ignoreUnlockProgress";
    public static boolean ignoreUnlock = false;
    public static final String ENABLE_STARTER_POOL = "enableStarterPool";
    public static boolean enableStarterPool = true;
    public static final String ENABLE_COMMON_POOL = "enableCommonPool";
    public static boolean enableCommonPool = true;
    public static final String ENABLE_UNCOMMON_POOL = "enableUncommonPool";
    public static boolean enableUncommonPool = true;
    public static final String ENABLE_RARE_POOL = "enableRarePool";
    public static boolean enableRarePool = true;
    public static final String ENABLE_BOSS_POOL = "enableBossPool";
    public static boolean enableBossPool = true;
    public static final String ENABLE_SHOP_POOL = "enableShopPool";
    public static boolean enableShopPool = true;
    public static final String ENABLE_EVENT_POOL = "enableEventPool";
    public static boolean enableEventPool = true;
    public static final String ENABLE_DEPRECATED_POOL = "enableDeprecatedPool";
    public static boolean enableDeprecatedPool = false;
    public static final String ENABLE_CATEGORY = "enableCategory";
    public static boolean enableCategory = true;
    public static final String ENABLE_DESC = "enableDescriptions";
    public static boolean enableDesc = true;
    public static final String ENABLE_DRAG_SELECT = "enableDragSelection";
    public static boolean enableDrag = true;

    public static final String RELIC_OBTAIN_AMOUNT = "amountOfRelicToObtain";
    public static int relicObtainMultiplier = 1;

    public static final String REMOVE_RELIC_FROM_POOLS = "removeRelicFromPools";
    public static boolean enableRemoveFromPool = false;

    //show isaac icons regardless of isaac mod installation?
    public static final String USE_ISAAC_ICONS = "useIsaacIcons";
    public static boolean enableIsaacIcons = false;

    public static HashMap<AbstractCard.CardColor, HashMap<String, AbstractRelic>> customRelics;

    public static final HashMap<AbstractCard.CardColor,HashMap<String,AbstractRelic>> allRelics = new HashMap<>();;

    public static ArrayList<AbstractPlayer> allCharacters;

    public static ArrayList<AbstractRelic> relicsToDisplay = new ArrayList<>();
    public static HashSet<Integer> relicsToRemove = new HashSet<>();
    public static LinkedList<AbstractRelic> relicsToAdd = new LinkedList<>();
    public static ArrayList<AbstractPotion> potionsToDisplay = new ArrayList<>();
    public static ArrayList<AbstractCard> cardsToDisplay = new ArrayList<>();

    public static ArrayList<AddEventParams> eventsToDisplay = new ArrayList<>();
    public static HashMap<String,Class<? extends AbstractPower>> powersToDisplay = new HashMap<>();

    public static boolean isScreenUp = false;

    public static CardModifications cardModifications = null;
    public static Favorites favorites = null;



    //This is for the in-game mod settings panel.
    private static final String MODNAME = "Loadout Mod";
    private static final String AUTHOR = "JasonW"; // And pretty soon - You!
    private static final String DESCRIPTION = "A mod to give you any relic you want.";
    
    // =============== INPUT TEXTURE LOCATION =================

    public static final String SETTINGS_STRINGS = "loadoutResources/localization/eng/UI-Strings.json";

    //Mod Badge - A small icon that appears in the mod settings menu next to your mod.
    public static final String BADGE_IMAGE = "loadoutResources/images/Badge.png";

    public static String makeSoundPath(String soundPath) {
        return getModID() + "Resources/sounds/" + soundPath;
    }

    // =============== MAKE IMAGE PATHS =================
    
    public static String makeRelicPath(String resourcePath) {
        return getModID() + "Resources/images/relics/" + resourcePath;
    }
    
    public static String makeRelicOutlinePath(String resourcePath) {
        return getModID() + "Resources/images/relics/outline/" + resourcePath;
    }

    public static Map getRelicStrings(String jsonPath) {
        return (Map)gson.fromJson(Gdx.files.internal(jsonPath).readString(String.valueOf(StandardCharsets.UTF_8)),(new TypeToken<Map<String, RelicStrings>>() {
        }).getType());
    }
    
    // =============== /MAKE IMAGE PATHS/ =================
    
    // =============== /INPUT TEXTURE LOCATION/ =================
    
    
    // =============== SUBSCRIBE, CREATE THE COLOR_GRAY, INITIALIZE =================
    
    public LoadoutMod() {
        logger.info("Subscribe to BaseMod hooks");
        
        BaseMod.subscribe(this);
      
        setModID("loadout");

        logger.info("Done subscribing");
        
        logger.info("Adding mod settings");
        // This loads the mod settings.
        // The actual mod Button is added below in receivePostInitialize()
        theDefaultDefaultSettings.setProperty(ENABLE_AS_STARTING_RELIC, "TRUE"); // This is the default setting. It's actually set...
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_BAG, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_BIN, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_CAULDRON, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_PRINTER, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_SHREDDER, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_MODIFIER, "TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_COMPASS,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_POWER,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTING_LOADOUT_TILDE,"TRUE");
        theDefaultDefaultSettings.setProperty(IGNORE_UNLOCK_PROGRESS, "FALSE");
        theDefaultDefaultSettings.setProperty(ENABLE_STARTER_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_COMMON_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_UNCOMMON_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_RARE_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_BOSS_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_SHOP_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_EVENT_POOL,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_DEPRECATED_POOL,"FALSE");
        theDefaultDefaultSettings.setProperty(ENABLE_CATEGORY,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_DESC,"TRUE");
        theDefaultDefaultSettings.setProperty(ENABLE_DRAG_SELECT,"TRUE");
        theDefaultDefaultSettings.setProperty(RELIC_OBTAIN_AMOUNT,"1");
        theDefaultDefaultSettings.setProperty(REMOVE_RELIC_FROM_POOLS,"FALSE");
        theDefaultDefaultSettings.setProperty(USE_ISAAC_ICONS,"FALSE");


        try {
            config = new SpireConfig("loadoutMod", "theLoadoutConfig", theDefaultDefaultSettings); // ...right here
            // the "fileName" parameter is the name of the file MTS will create where it will save our setting.
            config.load(); // Load the setting and set the boolean to equal it
            enableStarting = config.getBool(ENABLE_AS_STARTING_RELIC);
            enableBagStarting = config.getBool(ENABLE_STARTING_LOADOUT_BAG);
            enableBinStarting = config.getBool(ENABLE_STARTING_LOADOUT_BIN);
            enableCauldronStarting = config.getBool(ENABLE_STARTING_LOADOUT_CAULDRON);
            enablePrinterStarting = config.getBool(ENABLE_STARTING_LOADOUT_PRINTER);
            enableShredderStarting = config.getBool(ENABLE_STARTING_LOADOUT_SHREDDER);
            enableModifierStarting = config.getBool(ENABLE_STARTING_LOADOUT_MODIFIER);
            enableCompassStarting = config.getBool(ENABLE_STARTING_LOADOUT_COMPASS);
            enableCompassStarting = config.getBool(ENABLE_STARTING_LOADOUT_POWER);
            enableTildeStarting = config.getBool(ENABLE_STARTING_LOADOUT_TILDE);
            ignoreUnlock = config.getBool(IGNORE_UNLOCK_PROGRESS);
            enableStarterPool = config.getBool(ENABLE_STARTER_POOL);
            enableCommonPool = config.getBool(ENABLE_COMMON_POOL);
            enableUncommonPool = config.getBool(ENABLE_UNCOMMON_POOL);
            enableRarePool = config.getBool(ENABLE_RARE_POOL);
            enableBossPool = config.getBool(ENABLE_BOSS_POOL);
            enableShopPool = config.getBool(ENABLE_SHOP_POOL);
            enableEventPool = config.getBool(ENABLE_EVENT_POOL);
            enableDeprecatedPool = config.getBool(ENABLE_DEPRECATED_POOL);
            enableCategory = config.getBool(ENABLE_CATEGORY);
            enableDesc = config.getBool(ENABLE_DESC);
            enableDrag = config.getBool(ENABLE_DRAG_SELECT);
            relicObtainMultiplier = config.getInt(RELIC_OBTAIN_AMOUNT);
            enableRemoveFromPool = config.getBool(REMOVE_RELIC_FROM_POOLS);
            enableIsaacIcons = config.getBool(USE_ISAAC_ICONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Done adding mod settings");

        logger.info("loading card modifications");

        try {
            ModifierLibrary.initialize();
            cardModifications = new CardModifications();
        } catch (IOException e) {
            logger.error("Error loading card modifications");
        }

        logger.info("Done loading card modifications");

        logger.info("loading favorites");
        try {
            favorites = new Favorites();
        } catch (IOException e) {
            logger.error("Error loading favorites");
        }
        logger.info("Done loading favorites");
    }
    
    // ====== NO EDIT AREA ======
    // DON'T TOUCH THIS STUFF. IT IS HERE FOR STANDARDIZATION BETWEEN MODS AND TO ENSURE GOOD CODE PRACTICES.
    // IF YOU MODIFY THIS I WILL HUNT YOU DOWN AND DOWNVOTE YOUR MOD ON WORKSHOP
    
    public static void setModID(String ID) { // DON'T EDIT
        Gson coolG = new Gson(); // EY DON'T EDIT THIS
        //   String IDjson = Gdx.files.internal("IDCheckStringsDONT-EDIT-AT-ALL.json").readString(String.valueOf(StandardCharsets.UTF_8)); // i hate u Gdx.files
        InputStream in = LoadoutMod.class.getResourceAsStream("/IDCheckStringsDONT-EDIT-AT-ALL.json"); // DON'T EDIT THIS ETHER
        IDCheckDontTouchPls EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheckDontTouchPls.class); // OR THIS, DON'T EDIT IT
        logger.info("You are attempting to set your mod ID as: " + ID); // NO WHY
        if (ID.equals(EXCEPTION_STRINGS.DEFAULTID)) { // DO *NOT* CHANGE THIS ESPECIALLY, TO EDIT YOUR MOD ID, SCROLL UP JUST A LITTLE, IT'S JUST ABOVE
            throw new RuntimeException(EXCEPTION_STRINGS.EXCEPTION); // THIS ALSO DON'T EDIT
        } else if (ID.equals(EXCEPTION_STRINGS.DEVID)) { // NO
            modID = EXCEPTION_STRINGS.DEFAULTID; // DON'T
        } else { // NO EDIT AREA
            modID = ID; // DON'T WRITE OR CHANGE THINGS HERE NOT EVEN A LITTLE
        } // NO
        logger.info("Success! ID is " + modID); // WHY WOULD U WANT IT NOT TO LOG?? DON'T EDIT THIS.
    } // NO
    
    public static String getModID() { // NO
        return modID; // DOUBLE NO
    } // NU-UH
    
    private static void pathCheck() { // ALSO NO
        Gson coolG = new Gson(); // NOPE DON'T EDIT THIS
        //   String IDjson = Gdx.files.internal("IDCheckStringsDONT-EDIT-AT-ALL.json").readString(String.valueOf(StandardCharsets.UTF_8)); // i still hate u btw Gdx.files
        InputStream in = LoadoutMod.class.getResourceAsStream("/IDCheckStringsDONT-EDIT-AT-ALL.json"); // DON'T EDIT THISSSSS
        IDCheckDontTouchPls EXCEPTION_STRINGS = coolG.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), IDCheckDontTouchPls.class); // NAH, NO EDIT
        String packageName = LoadoutMod.class.getPackage().getName(); // STILL NO EDIT ZONE
        FileHandle resourcePathExists = Gdx.files.internal(getModID() + "Resources"); // PLEASE DON'T EDIT THINGS HERE, THANKS
        if (!modID.equals(EXCEPTION_STRINGS.DEVID)) { // LEAVE THIS EDIT-LESS
            if (!packageName.equals(getModID())) { // NOT HERE ETHER
                throw new RuntimeException(EXCEPTION_STRINGS.PACKAGE_EXCEPTION + getModID()); // THIS IS A NO-NO
            } // WHY WOULD U EDIT THIS
            if (!resourcePathExists.exists()) { // DON'T CHANGE THIS
                throw new RuntimeException(EXCEPTION_STRINGS.RESOURCE_FOLDER_EXCEPTION + getModID() + "Resources"); // NOT THIS
            }// NO
        }// NO
    }// NO
    
    // ====== YOU CAN EDIT AGAIN ======
    
    
    public static void initialize() {
        logger.info("========================= Initializing Loadout Mod.  =========================");
        LoadoutMod loadoutmod = new LoadoutMod();
        logger.info("========================= /Loadout Mod Initialized./ =========================");
    }
    
    // ============== /SUBSCRIBE, CREATE THE COLOR_GRAY, INITIALIZE/ =================

    
    
    // =============== POST-INITIALIZE =================
    
    @Override
    public void receivePostInitialize() {
        logger.info("Loading badge image and mod options");
        
        // Load the Mod Badge
        Texture badgeTexture = TextureLoader.getTexture(BADGE_IMAGE);
        
        // Create the Mod Menu
        ModPanel settingsPanel = new ModPanel();
        float startingXPos = 350.0f;
        float settingXPos = startingXPos;
        float xSpacing = 250.0f;
        float settingYPos = 750.0f;
        float lineSpacing = 50.0f;
        UIStrings UIStrings = CardCrawlGame.languagePack.getUIString(makeID("OptionsMenu"));
        String[] SettingText = UIStrings.TEXT;
        // Create the on/off button:
        ModLabeledToggleButton enableAsStartingButton = new ModLabeledToggleButton(SettingText[0],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
            
            enableStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
            try {
                // And based on that boolean, set the settings and save them
                config.setBool(ENABLE_AS_STARTING_RELIC, enableStarting);
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        settingsPanel.addUIElement(enableAsStartingButton); // Add the button to the settings panel. Button is a go.

        settingYPos -= lineSpacing;

        settingXPos += 100.0f;

        ModLabeledToggleButton enableBagAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(LoadoutBag.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableBagStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableBagStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_BAG, enableBagStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableBagAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enableBinAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(TrashBin.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableBinStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableBinStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_BIN, enableBinStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableBinAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enableCauldronAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(LoadoutCauldron.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableCauldronStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableCauldronStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_CAULDRON, enableCauldronStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableCauldronAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enablePrinterAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(CardPrinter.ID).name,
        settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enablePrinterStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enablePrinterStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_PRINTER, enablePrinterStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enablePrinterAsStartingButton);

        settingXPos = startingXPos + 100.0f;
        settingYPos -= lineSpacing;

        ModLabeledToggleButton enableShredderAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(CardShredder.ID).name,
        settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableShredderStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableShredderStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_SHREDDER, enableShredderStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableShredderAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enableModifierAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(CardModifier.ID).name,
        settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableModifierStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableModifierStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_MODIFIER, enableModifierStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableModifierAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enableCompassAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(EventfulCompass.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableCompassStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableCompassStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_COMPASS, enableCompassStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableCompassAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enablePowerAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(PowerGiver.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enablePowerStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enablePowerStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_POWER, enablePowerStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enablePowerAsStartingButton);
        settingXPos += xSpacing;

        ModLabeledToggleButton enableTildeAsStartingButton = new ModLabeledToggleButton(RelicLibrary.getRelic(TildeKey.ID).name,
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableTildeStarting, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:

                    enableTildeStarting = button.enabled; // The boolean true/false will be whether the button is enabled or not
                    try {
                        // And based on that boolean, set the settings and save them
                        config.setBool(ENABLE_STARTING_LOADOUT_TILDE, enableTildeStarting);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        settingsPanel.addUIElement(enableTildeAsStartingButton);
        settingXPos += xSpacing;

        settingXPos = startingXPos;
        settingYPos -= lineSpacing;

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

        //String[] relicPoolStrs = {ENABLE_STARTER_POOL,ENABLE_COMMON_POOL,ENABLE_UNCOMMON_POOL,ENABLE_RARE_POOL,ENABLE_BOSS_POOL,ENABLE_SHOP_POOL,ENABLE_EVENT_POOL};
        //boolean[] relicPoolBools = {enableStarterPool,enableCommonPool,enableUncommonPool,enableRarePool,enableBossPool,enableShopPool,enableEventPool};
        //ModLabeledToggleButton[] relicPoolBtns = new ModLabeledToggleButton[relicPoolBools.length];

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
        //settingsPanel.addUIElement(enableDeprecatedPoolButton);

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

        ModLabel RelicAmountLabel = new ModLabel(SettingText[10], settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, settingsPanel, l -> {
        });
        settingsPanel.addUIElement(RelicAmountLabel);

        settingYPos -= 0.5f * lineSpacing;

        ModMinMaxSlider RelicAmountSlider = new ModMinMaxSlider("",
                settingXPos, settingYPos, 1.0F, 10.0F,
                relicObtainMultiplier,
                "x%.0f",
                settingsPanel,
                slider -> {
            float fVal = slider.getValue();
            int iVal = Math.round(fVal);
            relicObtainMultiplier = iVal;
            try {
                config.setInt(RELIC_OBTAIN_AMOUNT, iVal);
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        settingsPanel.addUIElement(RelicAmountSlider);

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

        ModLabeledToggleButton enableIsaacIconsButton = new ModLabeledToggleButton(SettingText[14],
                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
                enableIsaacIcons, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
                    enableIsaacIcons = button.enabled;
                    try {
                        config.setBool(USE_ISAAC_ICONS, enableIsaacIcons);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        settingsPanel.addUIElement(enableIsaacIconsButton);

        settingYPos -= lineSpacing;
        settingXPos += 800.0f;

        ModLabeledButton removeModificationsButton = new ModLabeledButton(SettingText[15],settingXPos, settingYPos, Settings.CREAM_COLOR, Settings.RED_TEXT_COLOR, FontHelper.charDescFont, settingsPanel,
                (button) -> {
                    CardModifications.cardMap.clear();

                    try {
                        cardModifications.save();
                    } catch (IOException e) {
                        logger.error("Error occurred while saving card modification after clearing");
                    }
                });
        settingsPanel.addUIElement(removeModificationsButton);

        BaseMod.registerModBadge(badgeTexture, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);

        logger.info("Done loading badge Image and mod options");

        logger.info("Initializing relic maps");

        initAllRelics();

        logger.info("Done initializing all relic maps");

        logger.info("Initializing base game events");
        createEventList();
        logger.info("Done initializing base game events");
        logger.info("Initializing powers");
        createPowerList();
        logger.info("Done initializing powers");
    }
    
    // =============== / POST-INITIALIZE/ =================

    
    // ================ ADD RELICS ===================
    
    @Override
    public void receiveEditRelics() {
        logger.info("Adding relics");

        // Take a look at https://github.com/daviscook477/BaseMod/wiki/AutoAdd
        // as well as
        // https://github.com/kiooeht/Bard/blob/e023c4089cc347c60331c78c6415f489d19b6eb9/src/main/java/com/evacipated/cardcrawl/mod/bard/BardMod.java#L319
        // for reference as to how to turn this into an "Auto-Add" rather than having to list every relic individually.
        // Of note is that the bard mod uses it's own custom relic class (not dissimilar to our AbstractDefaultCard class for cards) that adds the 'color' field,
        // in order to automatically differentiate which pool to add the relic too.

        
        // This adds a relic to the Shared pool. Every character can find this relic.
        BaseMod.addRelic(new LoadoutBag(), RelicType.SHARED);
        BaseMod.addRelic(new TrashBin(), RelicType.SHARED);
        BaseMod.addRelic(new LoadoutCauldron(), RelicType.SHARED);
        BaseMod.addRelic(new CardPrinter(), RelicType.SHARED);
        BaseMod.addRelic(new CardShredder(), RelicType.SHARED);
        BaseMod.addRelic(new CardModifier(), RelicType.SHARED);
        BaseMod.addRelic(new EventfulCompass(), RelicType.SHARED);
        BaseMod.addRelic(new PowerGiver(), RelicType.SHARED);
        BaseMod.addRelic(new TildeKey(), RelicType.SHARED);
        // Mark relics as seen - makes it visible in the compendium immediately
        // If you don't have this it won't be visible in the compendium until you see them in game
        // (the others are all starters so they're marked as seen in the character file)
        logger.info("Done adding relics!");
    }
    
    // ================ /ADD RELICS/ ===================
    
    
    // ================ LOAD THE TEXT ===================
    
    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());
        loadLocStrings("eng");
        if (!languageSupport().equals("eng"))
            loadLocStrings(languageSupport());

        // RelicStrings
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                getModID() + "Resources/localization/"+languageSupport()+"/LoadoutMod-Relic-Strings.json");
        
        logger.info("Done edittting strings");
    }
    
    // ================ /LOAD THE TEXT/ ===================

    /**
     * Add Starting Relics
     * Currently NOT using it
     */
//    public void receivePostCreateStartingRelics(AbstractPlayer.PlayerClass pclass, ArrayList<String> relicslist) {
//        if(enableStarting) {
//            relicslist.add("loadout:LoadoutBag");
//            relicslist.add("loadout:TheBin");
//        }
//    }



    // this adds "ModName:" before the ID of any card/relic/power etc.
    // in order to avoid conflicts if any other mod uses the same ID.
    public static String makeID(String idText) {
        return getModID() + ":" + idText;
    }

    public static String languageSupport() {
        switch (Settings.language) {
            case ZHS:
                return "zhs";
            case ZHT:
                return "zht";
            case KOR:
                return "kor";
            case JPN:
                return "jpn";
            case FRA:
                return "fra";
//            case RUS:
//                return "rus";
        }
        return "eng";
    }

    private void loadLocStrings(String language) {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/localization/" + language + "/UI-Strings.json");
    }

    /**
     * Using this as a way to avoid starting relic conflicts
     */
    @Override
    public void receivePostDungeonInitialize() {
        //patch to fix crash from a fresh new game
        if (TipTracker.relicCounter < 20) {
            if (!(Boolean)TipTracker.tips.get("RELIC_TIP")) {
                TipTracker.neverShowAgain("RELIC_TIP");
            }
        }

        if(enableStarting) {
            if(enableBagStarting&&RelicLibrary.isARelic(LoadoutBag.ID)&&!AbstractDungeon.player.hasRelic(LoadoutBag.ID)) RelicLibrary.getRelic(LoadoutBag.ID).makeCopy().instantObtain();
            if(enableBinStarting&&RelicLibrary.isARelic(TrashBin.ID)&&!AbstractDungeon.player.hasRelic(TrashBin.ID)) RelicLibrary.getRelic(TrashBin.ID).makeCopy().instantObtain();
            if(enableCauldronStarting&&RelicLibrary.isARelic("loadout:LoadoutCauldron")&&!AbstractDungeon.player.hasRelic(LoadoutCauldron.ID)) RelicLibrary.getRelic("loadout:LoadoutCauldron").makeCopy().instantObtain();
            if(enablePrinterStarting&&RelicLibrary.isARelic("loadout:CardPrinter")&&!AbstractDungeon.player.hasRelic(CardPrinter.ID)) RelicLibrary.getRelic("loadout:CardPrinter").makeCopy().instantObtain();
            if(enableShredderStarting&&RelicLibrary.isARelic("loadout:CardShredder")&&!AbstractDungeon.player.hasRelic(CardShredder.ID)) RelicLibrary.getRelic("loadout:CardShredder").makeCopy().instantObtain();
            if(enableModifierStarting&&RelicLibrary.isARelic("loadout:CardModifier")&&!AbstractDungeon.player.hasRelic(CardModifier.ID)) RelicLibrary.getRelic("loadout:CardModifier").makeCopy().instantObtain();
            if(enableCompassStarting&&RelicLibrary.isARelic(EventfulCompass.ID)&&!AbstractDungeon.player.hasRelic(EventfulCompass.ID)) RelicLibrary.getRelic(EventfulCompass.ID).makeCopy().instantObtain();
            if(enablePowerStarting&&RelicLibrary.isARelic(PowerGiver.ID)&&!AbstractDungeon.player.hasRelic(PowerGiver.ID)) RelicLibrary.getRelic(PowerGiver.ID).makeCopy().instantObtain();
            if(enableTildeStarting&&RelicLibrary.isARelic(TildeKey.ID)&&!AbstractDungeon.player.hasRelic(TildeKey.ID)) RelicLibrary.getRelic(TildeKey.ID).makeCopy().instantObtain();
        }

    }

    private void init() {
        customRelics = BaseMod.getAllCustomRelics();
        allCharacters = CardCrawlGame.characterManager.getAllCharacters();
        createRelicList();
        createPotionList();
        createCardList();
        TildeKey.resetToDefault();
    }

    private void createEventList() {

        registerEvent("Match and Keep!", GremlinMatchGame.class, EventUtils.EventType.SHRINE);
        registerEvent("Golden Shrine",  GoldShrine.class, EventUtils.EventType.SHRINE);
        registerEvent("Transmorgrifier",  Transmogrifier.class, EventUtils.EventType.SHRINE);
        registerEvent("Purifier",  PurificationShrine.class, EventUtils.EventType.SHRINE);
        registerEvent("Upgrade Shrine",  UpgradeShrine.class, EventUtils.EventType.SHRINE);
        registerEvent("Wheel of Change", GremlinWheelGame.class, EventUtils.EventType.SHRINE);
        registerEvent("Accursed Blacksmith", AccursedBlacksmith.class, EventUtils.EventType.ONE_TIME);
        registerEvent("Bonfire Elementals", Bonfire.class, EventUtils.EventType.ONE_TIME);
        registerEvent("Designer", Designer.class, EventUtils.EventType.ONE_TIME, new String[] { "TheCity", "TheBeyond" });
        registerEvent("Duplicator", Duplicator.class, EventUtils.EventType.ONE_TIME, new String[] { "TheCity", "TheBeyond" });
        registerEvent("FaceTrader", FaceTrader.class, EventUtils.EventType.ONE_TIME, new String[] { "Exordium", "TheCity" });
        registerEvent("Fountain of Cleansing", FountainOfCurseRemoval.class, EventUtils.EventType.ONE_TIME);
        registerEvent("Knowing Skull", KnowingSkull.class, EventUtils.EventType.ONE_TIME, new String[] { "TheCity" });
        registerEvent("Lab", Lab.class, EventUtils.EventType.ONE_TIME);
        registerEvent("N'loth", Nloth.class, EventUtils.EventType.ONE_TIME, new String[] { "TheCity" });
        registerEvent("NoteForYourself", NoteForYourself.class, EventUtils.EventType.ONE_TIME);
        registerEvent("SecretPortal",  SecretPortal.class, EventUtils.EventType.ONE_TIME, new String[] { "TheBeyond" });
        registerEvent("The Joust",  TheJoust.class, EventUtils.EventType.ONE_TIME, new String[] { "TheCity" });
        registerEvent("WeMeetAgain", WeMeetAgain.class, EventUtils.EventType.ONE_TIME);
        registerEvent("The Woman in Blue", WomanInBlue.class, EventUtils.EventType.ONE_TIME);
        registerEvent("Big Fish", BigFish.class, "Exordium");
        registerEvent("The Cleric",  Cleric.class, "Exordium");
        registerEvent("Dead Adventurer",  DeadAdventurer.class, "Exordium");
        registerEvent("Golden Idol", GoldenIdolEvent.class, "Exordium");
        registerEvent("Golden Wing",  GoldenWing.class, "Exordium");
        registerEvent("World of Goop", GoopPuddle.class, "Exordium");
        registerEvent("Liars Game", Sssserpent.class, "Exordium");
        registerEvent("Living Wall", LivingWall.class, "Exordium");
        registerEvent("Mushrooms", Mushrooms.class, "Exordium");
        registerEvent("Scrap Ooze", ScrapOoze.class, "Exordium");
        registerEvent("Shining Light", ShiningLight.class, "Exordium");
        registerEvent("Addict",  Addict.class, "TheCity");
        registerEvent("Back to Basics",  BackToBasics.class, "TheCity");
        registerEvent("Beggar",  Beggar.class, "TheCity");
        registerEvent("Colosseum", Colosseum.class, "TheCity");
        registerEvent("Cursed Tome", CursedTome.class, "TheCity");
        registerEvent("Drug Dealer", DrugDealer.class, "TheCity");
        registerEvent("Forgotten Altar", ForgottenAltar.class, "TheCity");
        registerEvent("Ghosts", Ghosts.class, "TheCity");
        registerEvent("Masked Bandits", MaskedBandits.class, "TheCity");
        registerEvent("Nest", Nest.class, "TheCity");
        registerEvent("The Library", TheLibrary.class, "TheCity");
        registerEvent("The Mausoleum", TheMausoleum.class, "TheCity");
        registerEvent("Vampires", Vampires.class, "TheCity");
        registerEvent("Falling",  Falling.class, "TheBeyond");
        registerEvent("MindBloom",  MindBloom.class, "TheBeyond");
        registerEvent("The Moai Head",  MoaiHead.class, "TheBeyond");
        registerEvent("Mysterious Sphere",  MysteriousSphere.class, "TheBeyond");
        registerEvent("SensoryStone", SensoryStone.class, "TheBeyond");
        registerEvent("Tomb of Lord Red Mask", TombRedMask.class, "TheBeyond");
        registerEvent("Winding Halls", WindingHalls.class, "TheBeyond");

        if(Loader.isModLoadedOrSideloaded("IsaacMod")) {
            try {
                //registerEvent("HidenRoomEvent", (Class<? extends AbstractImageEvent>) Class.forName("events.HidenRoomEvent"), new String[] { "Exordium", "TheCity" , "TheBeyond"});
            } catch (Exception e) {
                logger.info("Failed to register Hidden Room event");
            }
        }

    }
    private void registerEvent(String eID, Class<? extends AbstractEvent> eClass, EventUtils.EventType eType) {
        eventsToDisplay.add(new AddEventParams.Builder(eID,eClass).eventType(eType).create());
    }

    private void registerEvent(String eID, Class<? extends AbstractEvent> eClass, EventUtils.EventType eType, String... acts) {
        eventsToDisplay.add(new AddEventParams.Builder(eID,eClass).eventType(eType).dungeonIDs(acts).create());
    }

    private void registerEvent(String eID, Class<? extends AbstractEvent> eClass, String... acts) {
        eventsToDisplay.add(new AddEventParams.Builder(eID,eClass).dungeonIDs(acts).create());
    }

    private void createPowerList() {
        powersToDisplay.clear();
        for (String pid : BaseMod.getPowerKeys()) {
            try {
                powersToDisplay.put(pid,BaseMod.getPowerClass(pid));
            } catch (Exception e) {
                logger.error("Failed to instantiate power");
            }
        }

        autoAddPowers();


    }

    private void autoAddPowers() {
        ClassFinder finder = new ClassFinder();
        AndClassFilter andClassFilter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new PowerFilter()});
        ClassLoader clazzLoader = Loader.getClassPool().getClassLoader();
        String noID = "Unnamed Power ";
        int count = 0;


        for (ModInfo mi : Loader.MODINFOS) {
            try {
                URL url = mi.jarURL;
                finder.add(new java.io.File(url.toURI()));
                Collection<ClassInfo> foundClasses = new ArrayList<>();
                finder.findClasses(foundClasses, (ClassFilter) andClassFilter);
                for (ClassInfo classInfo : foundClasses) {
                    try {
                        CtClass cls = Loader.getClassPool().get(classInfo.getClassName());
//                if (cls.hasAnnotation(CardIgnore.class))
//                    continue;
                        boolean isPower = false;
                        CtClass superCls = cls;
                        while (superCls != null) {
                            superCls = superCls.getSuperclass();
                            if (superCls == null)
                                break;
                            if (superCls.getName().equals(AbstractPower.class.getName())) {
                                isPower = true;
                                break;
                            }
                        }
                        if (!isPower)
                            continue;

                        //System.out.println(classInfo.getClassName());
                        Class<?extends AbstractPower> powerC = (Class<? extends AbstractPower>) clazzLoader.loadClass(cls.getName());
                        //PowerStrings pStr = ReflectionHacks.getPrivateStatic(powerC,"powerStrings");

                        try{
                            Class.forName(powerC.getName(),false,clazzLoader);
                        } catch (ClassNotFoundException|NoClassDefFoundError cnfe) {
                            logger.info(powerC.getName() + "does not exist");
                            continue;
                        }

                        String pID = null;

                        try {
                            pID = (String) powerC.getDeclaredField("POWER_ID").get(null);
                        } catch (NoSuchFieldException|ExceptionInInitializerError ignored) {

                        } catch (NoClassDefFoundError ncdfe) {
                            continue;
                        }

                        if (pID == null)  {

                            try {
                                AbstractPower p = powerC.newInstance();

                                pID = p.ID;
                            } catch (InstantiationException|IllegalAccessException|ExceptionInInitializerError ignored) {

                            }

                            if (pID == null) {
                                //pID = noID + count++;
                                continue;
                            }
                        }


                        powersToDisplay.put(pID, (Class<? extends AbstractPower>) powerC);
                    } catch (IllegalAccessException e) {
                        logger.info("Failed to initialize custom power for " + classInfo.getClassName());
                    }

                }
            } catch (Exception e) {
                logger.info("Failed to initialize custom power for "+ mi.ID);
                e.printStackTrace();
            }

        }
    }


    private void initAllRelics() {
        HashMap<String, AbstractRelic> ur = null;

        if (Loader.isModLoaded("RelicUpgradeLib")) {
            try {
                Class proxyManager = Class.forName("relicupgradelib.arch.ProxyManager");
                ur = (HashMap<String, AbstractRelic>) proxyManager.getDeclaredField("upgradeRelics").get(null);
            } catch (Exception e) {
                logger.info("Failed to load relicUpgradeLib!");
            }
        }
        allRelics.put(AbstractCard.CardColor.RED,getOtherModRelics((HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "redRelics"),ur));
        allRelics.put(AbstractCard.CardColor.GREEN,getOtherModRelics((HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "greenRelics"),ur));
        allRelics.put(AbstractCard.CardColor.BLUE,getOtherModRelics((HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "blueRelics"),ur));
        allRelics.put(AbstractCard.CardColor.PURPLE,getOtherModRelics((HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "purpleRelics"),ur));

        allRelics.putAll(BaseMod.getAllCustomRelics());

        allRelics.put(AbstractCard.CardColor.COLORLESS,getOtherModRelics((HashMap)ReflectionHacks.getPrivateStatic(RelicLibrary.class, "sharedRelics"),ur));


    }

    private HashMap<String, AbstractRelic> getOtherModRelics(HashMap<String,AbstractRelic> relicMap, HashMap<String,AbstractRelic> modMap) {
        HashMap<String,AbstractRelic> retMap = new HashMap<>();
        if (relicMap != null) {
            retMap.putAll(relicMap);
            if (modMap != null) {
                Iterator<String> rIt = modMap.keySet().iterator();
                while (rIt.hasNext()) {
                    String rId = rIt.next();
                    if (retMap.containsKey(RelicNameComparator.editModRelicId(rId))) {
                        retMap.put(rId,modMap.get(rId));
                    }
                }
            }
        }

        return retMap;
    }

    @Override
    public void receiveStartGame() {
        init();
    }
    public static void createCardList() {
        cardsToDisplay.clear();

        for (AbstractCard c : CardLibrary.cards.values()) {
            if(ignoreUnlock)
                c.isSeen = true;

            cardsToDisplay.add(c);
        }

    }
    private void createPotionList() {
        potionsToDisplay.clear();
        ArrayList<String> potions = new ArrayList<>();
        potions.addAll(PotionHelper.getPotions(AbstractPlayer.PlayerClass.IRONCLAD, true));
        potions.forEach(p->potionsToDisplay.add(PotionHelper.getPotion(p)));
    }
    private void createRelicList() {
        relicsToDisplay.clear();
        ArrayList<AbstractRelic> relics = new ArrayList<>();
        if (LoadoutMod.enableStarterPool)
            relics.addAll(RelicLibrary.starterList);
        if (LoadoutMod.enableCommonPool)
            relics.addAll(RelicLibrary.commonList);
        if (LoadoutMod.enableUncommonPool)
            relics.addAll(RelicLibrary.uncommonList);
        if (LoadoutMod.enableRarePool)
            relics.addAll(RelicLibrary.rareList);
        if (LoadoutMod.enableBossPool)
            relics.addAll(RelicLibrary.bossList);
        if (LoadoutMod.enableShopPool)
            relics.addAll(RelicLibrary.shopList);
        if (LoadoutMod.enableEventPool)
            relics.addAll(RelicLibrary.specialList);
        if(enableDeprecatedPool) {
            try {relics.add(new DEPRECATED_DarkCore());
                relics.add(new DerpRock());
                relics.add(new DEPRECATEDDodecahedron());
                relics.add(new DEPRECATEDYin());
            } catch (Exception e) {
                logger.warn("Error occurred while adding deprecated relics");
            }

        }

        if (Loader.isModLoaded("RelicUpgradeLib")) {
            try {
                Class proxyManager = Class.forName("relicupgradelib.arch.ProxyManager");
                HashMap<String, AbstractRelic> ur = (HashMap<String, AbstractRelic>) proxyManager.getDeclaredField("upgradeRelics").get(null);
                relics.addAll(ur.values());
            } catch (Exception e) {
                logger.info("Failed to load relicUpgradeLib!");
            }
        }
        //add a default option
        relics.add(RelicLibrary.getRelic("Circlet"));
        //relics.forEach(r->relicsToDisplay.add(r.makeCopy()));
        relicsToDisplay.addAll(relics);


    }

    public static void modifyPlayerRelics() {
        AbstractDungeon.CurrentScreen prevScreen = AbstractDungeon.screen;

        if(relicsToRemove.size()>0) {
            //AbstractDungeon.player.relics.removeAll(relicsToRemove);
            ArrayList<AbstractRelic> playerRelics = AbstractDungeon.player.relics;
            relicsToRemove.forEach(i->playerRelics.get(i).onUnequip());
            AbstractDungeon.player.relics = IntStream.range(0, playerRelics.size())
                    .filter(i -> !relicsToRemove.contains(i))
                    .mapToObj(playerRelics::get)
                    .collect(Collectors.toCollection(ArrayList::new));
            AbstractDungeon.player.reorganizeRelics();

//            int actualRelicPage = AbstractDungeon.player.relics.size()/AbstractRelic.MAX_RELICS_PER_PAGE;
//            if (AbstractRelic.relicPage > actualRelicPage && actualRelicPage >= 0)
//                AbstractRelic.relicPage = actualRelicPage;
            AbstractRelic.relicPage = 0;
            AbstractDungeon.topPanel.adjustRelicHbs();

            relicsToRemove.clear();
        }
        if(relicsToAdd.size()>0){
//            while (relicsToAdd.size()>0 && !isScreenUp) {
//                AbstractRelic r = relicsToAdd.pop();
//                AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, r);
//            }
            for (AbstractRelic r : relicsToAdd) {
                if(enableRemoveFromPool) removeRelicFromPools(r.relicId);
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, r);
            }
            //AbstractDungeon.actionManager.addToBottom(new );
            relicsToAdd.clear();
        }
    }
    public static boolean isSelectionScreenUp() {
        return LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp;
    }

    public static void removeRelicFromPools(String relicId) {
        if(!AbstractDungeon.commonRelicPool.remove(relicId))
            if(!AbstractDungeon.uncommonRelicPool.remove(relicId))
                if(!AbstractDungeon.rareRelicPool.remove(relicId))
                    if(!AbstractDungeon.shopRelicPool.remove(relicId))
                        if(!AbstractDungeon.bossRelicPool.remove(relicId))
                            return;
    }


}
