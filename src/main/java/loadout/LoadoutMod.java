package loadout;

import basemod.*;
import basemod.abstracts.AbstractCardModifier;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
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
import com.megacrit.cardcrawl.helpers.input.InputAction;
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
import javassist.ClassPool;
import javassist.CtClass;
import loadout.cards.SutureCard;
import loadout.helper.ModifierLibrary;
import loadout.helper.RelicNameComparator;
import loadout.portraits.CardPortraitManager;
import loadout.savables.CardLoadouts;
import loadout.savables.CardModifications;
import loadout.savables.Favorites;
import loadout.savables.RelicStateSavables;
import loadout.screens.SidePanel;
import loadout.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loadout.relics.*;
import org.clapper.util.classutil.*;
import pinacolada.cards.base.PCLCustomCardSlot;
import relicupgradelib.arch.ProxyManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
StartGameSubscriber, PrePlayerUpdateSubscriber, RenderSubscriber, PostCampfireSubscriber, PreUpdateSubscriber, RelicGetSubscriber, PostDeathSubscriber, PostUpdateSubscriber, PostRenderSubscriber{
    public static final Logger logger = LogManager.getLogger(LoadoutMod.class.getName());
    private static String modID;

    //show isaac icons regardless of isaac mod installation?
    public static final String USE_ISAAC_ICONS = "useIsaacIcons";
    public static boolean FABRICATE_MOD_LOADED = false;
    public static boolean VUPSHION_MOD_LOADED = false;

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

    public static int universalMultiplier = 1;
    private static InputAction shiftKey;
    private static InputAction ctrlKey;

    public static int numThreadsTotal = 0;
    public static int numThreadsFinished = 0;

    public static long startTime;

    private static boolean isGameLoaded = false;

    //This is for the in-game mod settings panel.
    public static final String MODNAME = "Loadout Mod";
    public static final String AUTHOR = "JasonW";
    public static final String DESCRIPTION = "A mod to give you any relic you want.";
    
    // =============== INPUT TEXTURE LOCATION =================



    public static String makeSoundPath(String soundPath) {
        return getModID() + "Resources/sounds/" + soundPath;
    }

    // =============== MAKE IMAGE PATHS =================

    public static String makeImagePath(String resourcePath) {
        return getModID() + "Resources/images/" + resourcePath;
    }

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
        ModConfig.initModSettings();
        logger.info("Done adding mod settings");

        //mod checks
        FABRICATE_MOD_LOADED = Loader.isModLoaded("pinacolada-fabricate");
        VUPSHION_MOD_LOADED = Loader.isModLoaded("VUPShionMod");


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
        
        ModConfig.initModConfigMenu();
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

        //AllInOneBag Save Load
        BaseMod.addSaveField(AllInOneBag.ID, AllInOneBag.INSTANCE);
        BaseMod.addSaveField(RelicStateSavables.ID, new RelicStateSavables());

        logger.info("Replacing custom card portraits");
        CardPortraitManager.INSTANCE.load();
        logger.info("Done replacing custom card portraits");

        shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);
        ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);

        isGameLoaded = true;
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

        AllInOneBag.INSTANCE = new AllInOneBag();
        AllInOneBag.INSTANCE.hideAllRelics();

        // Mark relics as seen - makes it visible in the compendium immediately
        // If you don't have this it won't be visible in the compendium until you see them in game
        // (the others are all starters so they're marked as seen in the character file)

//        if(Loader.isModLoaded("RelicUpgradeLib")) {
//            Proxy p = new Proxy(new AllInOneBag());
//            p.addBranch(new UpgradeBranch(new AllInOneBagUp(), true, true, true));
//            ProxyManager.register(p);
//        }
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

    @Override
    public void receivePostDungeonInitialize() {

        AllInOneBag.INSTANCE.onStartingNewRun();

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
        AllInOneBag.INSTANCE.hideAllRelics();
        AllInOneBag.isSelectionScreenUp = false;
        AllInOneBag.INSTANCE.showButton();
    }
    public static void createCardList() {
        cardsToDisplay.clear();
        CardLibrary.cards.remove(SutureCard.ID);
        for (AbstractCard c : CardLibrary.cards.values()) {
            if(ModConfig.ignoreUnlock)
                c.isSeen = true;
            cardsToDisplay.add(c);
        }
        if(ModConfig.enableDeprecatedPool){
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
        if (ModConfig.enableStarterPool)
            relics.addAll(RelicLibrary.starterList);
        if (ModConfig.enableCommonPool)
            relics.addAll(RelicLibrary.commonList);
        if (ModConfig.enableUncommonPool)
            relics.addAll(RelicLibrary.uncommonList);
        if (ModConfig.enableRarePool)
            relics.addAll(RelicLibrary.rareList);
        if (ModConfig.enableBossPool)
            relics.addAll(RelicLibrary.bossList);
        if (ModConfig.enableShopPool)
            relics.addAll(RelicLibrary.shopList);
        if (ModConfig.enableEventPool)
            relics.addAll(RelicLibrary.specialList);
        if(ModConfig.enableDeprecatedPool) {
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
                relics.add(new AllInOneBagUp());
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
                if(ModConfig.enableRemoveFromPool) removeRelicFromPools(r.relicId);
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
    public void receiveRender(SpriteBatch sb) {

        if(sidePanel != null) sidePanel.render(sb);
        if(isGameLoaded){
            sb.setColor(Color.WHITE);
            AllInOneBag.INSTANCE.renderInTopPanel(sb);
        }
    }


    @Override
    public boolean receivePostCampfire() {
        return !TildeKey.infiniteCampfireActions;
    }

    @Override
    public void receiveEditCards() {
        logger.info("loading card modifications");

        try {
            ModifierLibrary.initialize();
            addCardModIDs();
            cardModifications = new CardModifications();
        } catch (IOException e) {
            logger.error("Error loading card modifications");
        }
        DynamicVariableAdder.addAllVariables();//must after mod lib init

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
        if(isXggg() && isCHN()) {
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
        return Settings.language == Settings.GameLanguage.ZHS || Settings.language == Settings.GameLanguage.ZHT || Settings.language == Settings.GameLanguage.JPN;
    }

    @Override
    public void receivePostDeath() {
        TildeKey.resetPlayerMorph();
        TildeKey.resetToDefault();
        CardPortraitManager.INSTANCE.garbageCollectAssets();
    }

    @Override
    public void receivePostUpdate() {
        if(isGameLoaded) {
            if (shiftKey.isPressed() && ctrlKey.isPressed()) {
            universalMultiplier = 50;
            } else if (shiftKey.isPressed()) {
                universalMultiplier = 10;
            } else if (ctrlKey.isPressed()) {
                universalMultiplier = 5;
            } else {
                universalMultiplier = 1;
            }
            AllInOneBag.INSTANCE.update();
        }
    }

    @Override
    public void receivePostRender(SpriteBatch sb) {

    }
}
