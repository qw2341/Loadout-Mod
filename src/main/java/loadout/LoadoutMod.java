package loadout;

import basemod.*;
import basemod.abstracts.AbstractCardModifier;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
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
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.orbs.*;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.HandDrill;
import com.megacrit.cardcrawl.relics.deprecated.DEPRECATEDDodecahedron;
import com.megacrit.cardcrawl.relics.deprecated.DEPRECATEDYin;
import com.megacrit.cardcrawl.relics.deprecated.DerpRock;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.compendium.RelicViewScreen;
import javassist.ClassPool;
import javassist.CtClass;
import loadout.helper.ModifierLibrary;
import loadout.helper.RelicNameComparator;
import loadout.helper.dynamicvariables.HealVariable;
import loadout.helper.dynamicvariables.MiscVariable;
import loadout.savables.CardLoadouts;
import loadout.savables.CardModifications;
import loadout.savables.Favorites;
import loadout.screens.CardViewPopupHeader;
import loadout.screens.SidePanel;
import loadout.util.*;
import lor.helper.LORHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loadout.relics.*;
import org.clapper.util.classutil.*;
import pinacolada.cards.base.PCLCustomCardSlot;
import relicupgradelib.arch.Proxy;
import relicupgradelib.arch.ProxyManager;
import relicupgradelib.arch.UpgradeBranch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import static basemod.BaseMod.customRewardTypeExists;
import static basemod.BaseMod.gson;
import static loadout.screens.PowerSelectScreen.dummyPlayer;

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
        EditRelicsSubscriber, EditCardsSubscriber,
        EditStringsSubscriber, EditKeywordsSubscriber,
        PostInitializeSubscriber,
PostDungeonInitializeSubscriber,
StartGameSubscriber, PrePlayerUpdateSubscriber, RenderSubscriber, PostCampfireSubscriber, PreUpdateSubscriber, RelicGetSubscriber, PostDeathSubscriber{
    public static final Logger logger = LogManager.getLogger(LoadoutMod.class.getName());
    private static String modID;

    // Mod-settings settings. This is if you want an on/off savable button
    public static SpireConfig config = null;
    public static Properties theDefaultDefaultSettings = new Properties();
    public static final String ENABLE_LEGACY_LAYOUT = "enableLegacyLayout";
    public static boolean enableLegacyLayout = true; // The boolean we'll be setting on/off (true/false)

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

    public static final String ENABLE_CREATURE_MANIPULATION = "enableCreatureManipulation";
    public static boolean enableCreatureManipulation = true;

    public static final String RELIC_OBTAIN_AMOUNT = "amountOfRelicToObtain";
    public static int relicObtainMultiplier = 1;

    public static final String REMOVE_RELIC_FROM_POOLS = "removeRelicFromPools";
    public static boolean enableRemoveFromPool = false;

    //show isaac icons regardless of isaac mod installation?
    public static final String USE_ISAAC_ICONS = "useIsaacIcons";
    public static boolean FABRICATE_MOD_LOADED = false;

    public static HashMap<AbstractCard.CardColor, HashMap<String, AbstractRelic>> customRelics;

    public static final HashMap<AbstractCard.CardColor,HashMap<String,AbstractRelic>> allRelics = new HashMap<>();;

    public static ArrayList<AbstractPlayer> allCharacters;

    public static ArrayList<AbstractRelic> relicsToDisplay = new ArrayList<>();
    public static HashSet<Integer> relicsToRemove = new HashSet<>();
    public static LinkedList<AbstractRelic> relicsToAdd = new LinkedList<>();
    public static ArrayList<AbstractPotion> potionsToDisplay = new ArrayList<>();
    public static ArrayList<AbstractCard> cardsToDisplay = new ArrayList<>();

    public static ArrayList<AddEventParams> eventsToDisplay = new ArrayList<>();
    public static ConcurrentHashMap<String,Class<? extends AbstractPower>> powersToDisplay = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Class<? extends AbstractOrb>> orbMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Class<? extends AbstractMonster>> monsterMap = new ConcurrentHashMap<>();
    public static HashMap<String, Class<? extends AbstractMonster>> baseGameMonsterMap = new HashMap<>();

    public static ConcurrentHashMap<String, Class<? extends AbstractCardModifier>> cardModMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> cardModIDMap = new ConcurrentHashMap<>();
    public static boolean isScreenUp = false;

    public static CardModifications cardModifications = null;
    public static Favorites favorites = null;
    public static CardLoadouts cardLoadouts = null;

    public static SidePanel sidePanel = null;

    public static SkinManager skinManager = null;

    public static int numThreadsTotal = 0;
    public static int numThreadsFinished = 0;

    public static long startTime;

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
    


    public static String makeUIPath(String resourcePath) {
        return getModID() + "Resources/images/ui/" + resourcePath;
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
        theDefaultDefaultSettings.setProperty(ENABLE_LEGACY_LAYOUT, "FALSE"); // This is the default setting. It's actually set...

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
        theDefaultDefaultSettings.setProperty(SkinManager.SKIN_SELECTION, "default");
        theDefaultDefaultSettings.setProperty(ENABLE_CREATURE_MANIPULATION, "TRUE");

        try {
            config = new SpireConfig("loadoutMod", "theLoadoutConfig", theDefaultDefaultSettings); // ...right here
            // the "fileName" parameter is the name of the file MTS will create where it will save our setting.
            config.load(); // Load the setting and set the boolean to equal it
            enableLegacyLayout = config.getBool(ENABLE_LEGACY_LAYOUT);

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

            SkinManager.currentSkin = config.getString(SkinManager.SKIN_SELECTION);
            enableCreatureManipulation = config.getBool(ENABLE_CREATURE_MANIPULATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Done adding mod settings");

        //mod checks
        FABRICATE_MOD_LOADED = Loader.isModLoaded("pinacolada-fabricate");



        autoAddCardMods();

        logger.info("loading favorites");
        try {
            favorites = new Favorites();
        } catch (IOException e) {
            logger.error("Error loading favorites");
        }
        logger.info("Done loading favorites");

        logger.info("loading card loadouts");
        try {
            cardLoadouts = new CardLoadouts();
        } catch (IOException e) {
            logger.error("Error loading card loadouts");
        }
        logger.info("Done loading card loadouts");
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
                enableLegacyLayout, // Boolean it uses
                settingsPanel, // The mod panel in which this button will be in
                (label) -> {}, // thing??????? idk
                (button) -> { // The actual button:
            
            enableLegacyLayout = button.enabled; // The boolean true/false will be whether the button is enabled or not
            try {
                // And based on that boolean, set the settings and save them
                config.setBool(ENABLE_LEGACY_LAYOUT, enableLegacyLayout);
                config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        settingsPanel.addUIElement(enableAsStartingButton); // Add the button to the settings panel. Button is a go.


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

        float tempXPos = settingXPos;
        ArrayList<ModLabeledToggleButton> skinRadioButtons = new ArrayList<>();

        for(String skinID : skinManager.skinNames.keySet()) {
            String skinName = skinManager.skinNames.get(skinID);
            ModLabeledToggleButton mb = new ModLabeledToggleButton( skinName,
                    tempXPos, settingYPos,Settings.CREAM_COLOR, FontHelper.charDescFont,
                    skinID.equalsIgnoreCase(SkinManager.currentSkin), settingsPanel,
                    (label) -> {}, (button) -> {

                    for(ModLabeledToggleButton otherB : skinRadioButtons) {
                        otherB.toggle.enabled = false;
                    }
                    try {
                        skinManager.switchSkin(skinID);
                        config.setString(SkinManager.SKIN_SELECTION, SkinManager.currentSkin);
                        config.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    button.enabled = true;


            });
            skinRadioButtons.add(mb);
            settingsPanel.addUIElement(mb);
            tempXPos += FontHelper.getSmartWidth(FontHelper.charDescFont, skinName,99999.0f,20.0f) + 50.0f * Settings.scale;
        }


//        ModLabeledToggleButton enableIsaacIconsButton = new ModLabeledToggleButton(SettingText[14],
//                settingXPos, settingYPos, Settings.CREAM_COLOR, FontHelper.charDescFont, // Position (trial and error it), color, font
//                enableIsaacIcons, // Boolean it uses
//                settingsPanel, // The mod panel in which this button will be in
//                (label) -> {}, // thing??????? idk
//                (button) -> { // The actual button:
//                    enableIsaacIcons = button.enabled;
//                    try {
//                        config.setBool(USE_ISAAC_ICONS, enableIsaacIcons);
//                        config.save();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//        settingsPanel.addUIElement(enableIsaacIconsButton);

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


        settingYPos -= lineSpacing;
        settingXPos += 800.0f;

        ModLabeledButton removeModificationsButton = new ModLabeledButton(SettingText[15],settingXPos, settingYPos, Settings.CREAM_COLOR, Settings.RED_TEXT_COLOR, FontHelper.charDescFont, settingsPanel,
                (button) -> {
                    //restore the changes in CardLib
                    CardModifications.restoreAllCardsInLibrary();

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
        logger.info("Initializing stuffs");
        createStuffLists();
        logger.info("Done initializing stuffs");



        //CardModifications.modifyCards();

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

        //Skins
        logger.info("loading skins");
        try {
            skinManager = new SkinManager();
            if(isXggg()) {
                skinManager.switchSkin("xggg");
            } else if (isIsaac()) {
                skinManager.switchSkin("isaac");
            } else
                skinManager.switchSkin(SkinManager.currentSkin);
        } catch (Exception e) {
            logger.error("Error starting skin manager");
            e.printStackTrace();
        }
        
        // This adds a relic to the Shared pool. Every character can find this relic.
        if(enableLegacyLayout){
            BaseMod.addRelic(new LoadoutBag(), RelicType.SHARED);
            BaseMod.addRelic(new TrashBin(), RelicType.SHARED);
            BaseMod.addRelic(new LoadoutCauldron(), RelicType.SHARED);
            BaseMod.addRelic(new CardPrinter(), RelicType.SHARED);
            BaseMod.addRelic(new CardShredder(), RelicType.SHARED);
            BaseMod.addRelic(new CardModifier(), RelicType.SHARED);
            BaseMod.addRelic(new EventfulCompass(), RelicType.SHARED);
            BaseMod.addRelic(new PowerGiver(), RelicType.SHARED);
            BaseMod.addRelic(new TildeKey(), RelicType.SHARED);
            BaseMod.addRelic(new BottledMonster(), RelicType.SHARED);
            BaseMod.addRelic(new OrbBox(), RelicType.SHARED);
            BaseMod.addRelic(new BlightChest(), RelicType.SHARED);
        }

        BaseMod.addRelic(new AllInOneBag(), RelicType.SHARED);
        // Mark relics as seen - makes it visible in the compendium immediately
        // If you don't have this it won't be visible in the compendium until you see them in game
        // (the others are all starters so they're marked as seen in the character file)

        if(Loader.isModLoaded("RelicUpgradeLib")) {
            Proxy p = new Proxy(new AllInOneBag());
            p.addBranch(new UpgradeBranch(new AllInOneBagUp(), true, true, true));
            ProxyManager.register(p);
        }
        logger.info("Done adding relics!");
    }
    
    // ================ /ADD RELICS/ ===================
    
    
    // ================ LOAD THE TEXT ===================
    
    @Override
    public void receiveEditStrings() {
        logger.info("Beginning to edit strings for mod with ID: " + getModID());

        if (!languageSupport().equals("eng"))
            loadLocStrings(languageSupport());
        else
            loadLocStrings("eng");
        
        logger.info("Done editing strings");
    }
    
    // ================ /LOAD THE TEXT/ ===================


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
            case SPA:
                return "spa";
        }
        return "eng";
    }

    private void loadLocStrings(String language) {
        BaseMod.loadCustomStringsFile(UIStrings.class, getModID() + "Resources/localization/" + language + "/UI-Strings.json");
        // RelicStrings
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                getModID() + "Resources/localization/"+languageSupport()+"/LoadoutMod-Relic-Strings.json");
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



        if(enableLegacyLayout) {
            if(!AbstractDungeon.player.hasRelic(LoadoutBag.ID)) new LoadoutBag().instantObtain();
            if(!AbstractDungeon.player.hasRelic(TrashBin.ID)) new TrashBin().instantObtain();
            if(!AbstractDungeon.player.hasRelic(LoadoutCauldron.ID)) new LoadoutCauldron().instantObtain();
            if(!AbstractDungeon.player.hasRelic(CardPrinter.ID)) new CardPrinter().instantObtain();
            if(!AbstractDungeon.player.hasRelic(CardShredder.ID)) new CardShredder().instantObtain();
            if(!AbstractDungeon.player.hasRelic(CardModifier.ID)) new CardModifier().instantObtain();
            if(!AbstractDungeon.player.hasRelic(EventfulCompass.ID)) new EventfulCompass().instantObtain();
            if(!AbstractDungeon.player.hasRelic(PowerGiver.ID)) new PowerGiver().instantObtain();
            if(!AbstractDungeon.player.hasRelic(TildeKey.ID)) new TildeKey().instantObtain();
            if(!AbstractDungeon.player.hasRelic(BottledMonster.ID)) new BottledMonster().instantObtain();
            if(!AbstractDungeon.player.hasRelic(OrbBox.ID)) new OrbBox().instantObtain();
            if(!AbstractDungeon.player.hasRelic(BlightChest.ID)) new BlightChest().instantObtain();
        }
        if(!enableLegacyLayout && !AbstractDungeon.player.hasRelic(AllInOneBag.ID)) new AllInOneBag().instantObtain();

        TildeKey.resetToDefault();


    }

    private void init() {
        customRelics = BaseMod.getAllCustomRelics();
        allCharacters = CardCrawlGame.characterManager.getAllCharacters();
        createRelicList();
        createPotionList();
        createCardList();
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
                registerEvent("HidenRoomEvent", (Class<? extends AbstractImageEvent>) Class.forName("events.HidenRoomEvent"), new String[] { "Exordium", "TheCity" , "TheBeyond"});
            } catch (Exception e) {
                logger.info("Failed to register Hidden Room event");
            }
        }

        if(Loader.isModLoaded("betterNote")) {
            try {
                registerEvent("betterNote:BetterNote", (Class<? extends AbstractEvent>) Class.forName("betterNote.events.BetterNoteEvent"));
            } catch (Exception e) {
                logger.info("Failed to register better note event");
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

    private void createStuffLists() {
        powersToDisplay.clear();
        for (String pid : BaseMod.getPowerKeys()) {
            try {
                powersToDisplay.put(pid,BaseMod.getPowerClass(pid));
            } catch (Exception e) {
                logger.error("Failed to instantiate power");
            }
        }
        monsterMap.clear();
        baseGameMonsterMap.clear();

        Settings.seed = 0L;
        AbstractDungeon.generateSeeds();
        AbstractDungeon.ascensionLevel = 20;
        AbstractDungeon.player = dummyPlayer;

        addBaseGameMonsters();

        orbMap.clear();
        orbMap.put(Dark.class.getName(),Dark.class);
        orbMap.put(Frost.class.getName(),Frost.class);
        orbMap.put(Lightning.class.getName(), Lightning.class);
        orbMap.put(Plasma.class.getName(), Plasma.class);
        orbMap.put(EmptyOrbSlot.class.getName(), EmptyOrbSlot.class);



        autoAddStuffs();
    }

    private void addBaseGameMonsters() {
        ClassFinder finder = new ClassFinder();
        ClassPool clazzPool = Loader.getClassPool();
        AndClassFilter andMonsterClassFilter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new SuperClassFilter(clazzPool, AbstractMonster.class)});
        ClassLoader clazzLoader = clazzPool.getClassLoader();
        try {
//            MonsterAdder monsterAdder = new MonsterAdder(Loader.STS_JAR, "StSMonsterThread");
//            monsterAdder.start();
            finder.add(new java.io.File(Loader.STS_JAR));
            Collection<ClassInfo> foundClasses = new ArrayList<>();
            finder.findClasses(foundClasses, andMonsterClassFilter);
            for (ClassInfo classInfo : foundClasses) {
                try {
                    CtClass cls = clazzPool.get(classInfo.getClassName());
                    //logger.info("Class: " + classInfo.getClassName() + (isMonster ? " is Monster" : " is neither"));
                    Class<?extends AbstractMonster> monsterC = (Class<? extends AbstractMonster>) clazzLoader.loadClass(cls.getName());
                    baseGameMonsterMap.put(monsterC.getName(), monsterC);
                } catch (Exception e) {
                    logger.info("Failed to initialize for " + classInfo.getClassName());
                }

            }
        } catch (Exception e) {
            logger.info("Failed to initialize base game monsters");
            e.printStackTrace();
        }
    }

    private static void addBaseGameDeprecatedCards() {
        ClassFinder finder = new ClassFinder();
        ClassPool clazzPool = Loader.getClassPool();
        AndClassFilter andCardClassFilter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new DeprecatedFilter(),new SuperClassFilter(clazzPool, AbstractCard.class)});
        ClassLoader clazzLoader = clazzPool.getClassLoader();
        try {
            finder.add(new java.io.File(Loader.STS_JAR));
            Collection<ClassInfo> foundClasses = new ArrayList<>();
            finder.findClasses(foundClasses, andCardClassFilter);
            for (ClassInfo classInfo : foundClasses) {
                try {
                    CtClass cls = clazzPool.get(classInfo.getClassName());
                    //logger.info("Class: " + classInfo.getClassName() + (isMonster ? " is Monster" : " is neither"));
                    Class<?extends AbstractCard> cardC = (Class<? extends AbstractCard>) clazzLoader.loadClass(cls.getName());
                    //logger.info("Trying to create monster button for: " + monsterC.getName());
                    try{
                        cardsToDisplay.add(cardC.getDeclaredConstructor(new Class[]{}).newInstance(null));
                    } catch (Exception e) {
                        logger.info("Failed to create card for: " + cardC.getName());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    logger.info("Failed to initialize for " + classInfo.getClassName());
                }

            }
        } catch (Exception e) {
            logger.info("Failed to initialize base game deprecated cards");
            e.printStackTrace();
        }
    }


    private void autoAddStuffs() {
        logger.info("Adding stuffs...");
        startTime = System.currentTimeMillis();

        numThreadsTotal = Loader.MODINFOS.length * 4;

        for (ModInfo mi : Loader.MODINFOS) {
            try {
                URL url = mi.jarURL;
                try {
                    PowerAdder powerAdder = new PowerAdder(url, mi.ID + "PowerThread");
                    powerAdder.start();
                } catch (Exception e) {
                    logger.info("Failed to import power from " + mi.ID);
                }

                try {
                    MonsterAdder monsterAdder = new MonsterAdder(url, mi.ID + "MonsterThread");
                    monsterAdder.start();
                } catch (Exception e) {
                    logger.info("Failed to import monsters from " + mi.ID);
                }

                try {
                    OrbAdder orbAdder = new OrbAdder(url, mi.ID + "OrbThread");
                    orbAdder.start();
                } catch (Exception e) {
                    logger.info("Failed to import orbs from " + mi.ID);
                }



            } catch (Exception e) {
                logger.info("Failed to initialize custom stuff for "+ mi.ID);
                e.printStackTrace();
            }

        }

    }

    private void autoAddCardMods() {
        cardModMap.clear();
        for (ModInfo mi : Loader.MODINFOS) {
            try {
                CardModAdder cardModAdder = new CardModAdder(mi.jarURL, mi.ID + "CardModThread");
                cardModAdder.start();
            } catch (Exception e) {
                logger.info("Failed to import card Mods from " + mi.ID);
            }
        }

    }

    private void addCardModIDs() {
        for(Map.Entry<String, Class<? extends AbstractCardModifier>> entry : cardModMap.entrySet()) {
            Class<? extends AbstractCardModifier> acmC = entry.getValue();
            String ID;
            try {
                ID = (String) acmC.getDeclaredField("ID").get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                ID = acmC.getSimpleName();
            } catch (NoClassDefFoundError error) {
                logger.error("FAILED to get card mod with class name: " + acmC.getName());
                ID = acmC.getSimpleName();
            }

            cardModIDMap.put(ID, entry.getKey());
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
        TildeKey.morphAndFlip();
    }
    public static void createCardList() {
        cardsToDisplay.clear();

        for (AbstractCard c : CardLibrary.cards.values()) {
            if(ignoreUnlock)
                c.isSeen = true;
            cardsToDisplay.add(c);
        }
        if(enableDeprecatedPool){
            addBaseGameDeprecatedCards();
        }
        if(FABRICATE_MOD_LOADED){
            logger.info("Fabricate detected, adding custom cards");
//
            try{
                ArrayList<PCLCustomCardSlot> customCards = pinacolada.cards.base.PCLCustomCardSlot.getCards();
                for (PCLCustomCardSlot slot : customCards) {
                    cardsToDisplay.add(slot.make());
                }
                    //cardsToDisplay.add(slot.getBuilder(0).create(0));
            } catch (RuntimeException e) {
                logger.info("Failed to add Fabricate custom cards");
            }

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
        if(LoadoutMod.enableDeprecatedPool) {
            try {
                //relics.add(new DEPRECATED_DarkCore());
                relics.add(new DerpRock());
                relics.add(new DEPRECATEDDodecahedron());
                relics.add(new DEPRECATEDYin());
            } catch (Exception e) {
                //e.printStackTrace();
                logger.info("Error occurred while adding deprecated relics");
            }

            try {
                relics.add(new com.megacrit.cardcrawl.relics.Test1());
                relics.add(new com.megacrit.cardcrawl.relics.Test3());
                relics.add(new com.megacrit.cardcrawl.relics.Test4());
                relics.add(new com.megacrit.cardcrawl.relics.Test5());
                relics.add(new com.megacrit.cardcrawl.relics.Test6());
            }catch (Exception e) {
                logger.info("Error occurred while adding test relics");
            }

        }

        if (Loader.isModLoaded("RelicUpgradeLib")) {
            try {
                HashMap<String, AbstractRelic> ur = ProxyManager.upgradeRelics;
                for (AbstractRelic r : ur.values()) {
                    if(!r.relicId.equals(AllInOneBagUp.ID))
                        relics.add(r);
                }
            } catch (Exception e) {
                logger.info("Failed to load relicUpgradeLib!");
            }
        }
        //add a default option
        relics.add(RelicLibrary.getRelic("Circlet"));
        relicsToDisplay.addAll(relics);


    }

    public static void modifyPlayerRelics() {
        if(relicsToRemove.size()>0) {

            ArrayList<AbstractRelic> playerRelics = AbstractDungeon.player.relics;
            relicsToRemove.forEach(i->playerRelics.get(i).onUnequip());
            AbstractDungeon.player.relics = IntStream.range(0, playerRelics.size())
                    .filter(i -> !relicsToRemove.contains(i))
                    .mapToObj(playerRelics::get)
                    .collect(Collectors.toCollection(ArrayList::new));
            AbstractDungeon.player.reorganizeRelics();

            AbstractRelic.relicPage = 0;
            AbstractDungeon.topPanel.adjustRelicHbs();

            relicsToRemove.clear();
        }
        if(relicsToAdd.size()>0){
            for (AbstractRelic r : relicsToAdd) {
                if(enableRemoveFromPool) removeRelicFromPools(r.relicId);
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, r);
            }
            relicsToAdd.clear();
        }
    }

    public static void removeRelicFromPools(String relicId) {
        if(!AbstractDungeon.commonRelicPool.remove(relicId))
            if(!AbstractDungeon.uncommonRelicPool.remove(relicId))
                if(!AbstractDungeon.rareRelicPool.remove(relicId))
                    if(!AbstractDungeon.shopRelicPool.remove(relicId))
                        if(!AbstractDungeon.bossRelicPool.remove(relicId))
                            return;
    }


    @Override
    public void receivePrePlayerUpdate() {
        if(sidePanel != null) sidePanel.update();
    }

    @Override
    public void receiveRender(SpriteBatch spriteBatch) {
        if(sidePanel != null) sidePanel.render(spriteBatch);
    }


    @Override
    public boolean receivePostCampfire() {
        return !TildeKey.infiniteCampfireActions;
    }

    @Override
    public void receiveEditCards() {
        logger.info("loading card modifications");

        BaseMod.addDynamicVariable(new HealVariable());
        BaseMod.addDynamicVariable(new MiscVariable());

        try {
            ModifierLibrary.initialize();
            addCardModIDs();
            cardModifications = new CardModifications();
        } catch (IOException e) {
            logger.error("Error loading card modifications");
        }

        logger.info("Done loading card modifications");
    }

    @Override
    public void receivePreUpdate() {
        if(sidePanel != null && !AbstractDungeon.isPlayerInDungeon() && sidePanel.shown) sidePanel.shown = false;
    }

    public static boolean isXggg() {
        return SkinManager.currentSkin.equals("xggg") || (CardCrawlGame.playerName != null && CardCrawlGame.playerName.equals("BrkStarshine") && SkinManager.currentSkin.equals("default"));
    }

    public static boolean isIsaac() {
        return SkinManager.currentSkin.equals("isaac") || (Loader.isModLoadedOrSideloaded("IsaacMod") || Loader.isModLoadedOrSideloaded("IsaacModExtend")) && SkinManager.currentSkin.equals("default");
    }

    @Override
    public void receiveRelicGet(AbstractRelic r) {
        if(isXggg()) {
            if(r.relicId.equals(HandDrill.ID)) {
                AllInOneBag.XGGGSay("7点钟还能用电钻的吗?");
            }
        }

    }

    @Override
    public void receiveEditKeywords() {
        KeywordsAdder.addKeywords();
    }

    public static boolean isCHN() {
        return Settings.language == Settings.GameLanguage.ZHS;
    }

    @Override
    public void receivePostDeath() {
        TildeKey.resetPlayerMorph();
    }
}
