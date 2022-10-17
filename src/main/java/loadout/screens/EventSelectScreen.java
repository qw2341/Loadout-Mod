package loadout.screens;

import basemod.BaseMod;
import basemod.CustomEventRoom;
import basemod.ReflectionHacks;
import basemod.eventUtil.AddEventParams;
import basemod.eventUtil.EventUtils;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.events.beyond.*;
import com.megacrit.cardcrawl.events.city.*;
import com.megacrit.cardcrawl.events.exordium.*;
import com.megacrit.cardcrawl.events.shrines.*;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import loadout.LoadoutMod;
import loadout.helper.RelicClassComparator;
import loadout.helper.RelicModComparator;
import loadout.helper.RelicNameComparator;
import loadout.helper.RelicTierComparator;
import loadout.relics.EventfulCompass;
import loadout.relics.LoadoutBag;
import loadout.relics.TrashBin;
//import net.sourceforge.pinyin4j.PinyinHelper;


import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class EventSelectScreen implements ScrollBarListener
{
    public class EventButton {
        public String id;
        public String name;
        public Hitbox hb;
        public float x;
        public float y;

        public Class<? extends AbstractEvent> eventClass;
        //public Texture img;
        public String modID;

        public ArrayList<PowerTip> tips;


        public EventButton(String id, float x, float y, String modID, Class<? extends AbstractEvent> eClass) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.hb = new Hitbox(200.0f * Settings.scale,75.0f * Settings.yScale);
            this.eventClass = eClass;
            this.tips = new ArrayList<>();

            this.modID = modID;
            if (this.modID == null) this.modID = "Slay the Spire";
            //this.name = EventHelper.getEventName(id);
            try {
                //this.name = (String) eClass.getField("NAME").get(null);
                this.name = (String) ReflectionHacks.getPrivateStatic(eClass,"NAME");
                if (this.name == null || this.name.length() == 0) this.name = "Unnamed Event";
                String[] desc = (String[]) eClass.getField("DESCRIPTIONS").get(null);
                if(desc != null && desc.length > 0) {
//                    for (String d : desc)
//                        this.tips.add(new PowerTip(this.name, d));
                    this.tips.add(new PowerTip(this.name, desc[0]));
                }
                this.tips.add(new PowerTip("Mod",this.modID));

            } catch (IllegalAccessException | NoSuchFieldException e) {
                LoadoutMod.logger.error("Failed to get name for event: " + id);
            }



        }

        public void update() {
            //this.hb.cX = this.x;
            //this.hb.cY = this.y;

            this.hb.update();
        }

        public void render(SpriteBatch sb) {


            if (this.hb != null) {
                this.hb.render(sb);
                if (this.hb.hovered) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                    sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x+40.0F,y-64.0F, 64.0F, 64.0F, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.GOLD_COLOR);
                    sb.setBlendFunction(770, 771);

                    TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
                } else {
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.CREAM_COLOR);
                }
                //if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                        //FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
            }

        }
    }
    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString("RelicViewScreen");
    public static final String[] rTEXT = rUiStrings.TEXT;
    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectionScreen"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final CharacterStrings redStrings = CardCrawlGame.languagePack.getCharacterString("Ironclad");
    private static final CharacterStrings greenStrings = CardCrawlGame.languagePack.getCharacterString("Silent");
    private static final CharacterStrings blueStrings = CardCrawlGame.languagePack.getCharacterString("Defect");
    private static final CharacterStrings purpleStrings = CardCrawlGame.languagePack.getCharacterString("Watcher");

    private static final CharacterStrings[] charStrings = {redStrings,greenStrings,blueStrings,purpleStrings};

    private static final float SPACE = 80.0F * Settings.scale;
    protected static final float START_X = 450.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 226.0F * Settings.scale;

    private EventSelectSortHeader sortHeader;

    protected float scrollY = START_Y;
    private float targetY = this.scrollY;
    private float scrollLowerBound = Settings.HEIGHT - 200.0F * Settings.scale;
    private float scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;//2600.0F * Settings.scale;
    private int scrollTitleCount = 0;
    private int row = 0;
    private int col = 0;
    private static final Color RED_OUTLINE_COLOR = new Color(-10132568);
    private static final Color GREEN_OUTLINE_COLOR = new Color(2147418280);
    private static final Color BLUE_OUTLINE_COLOR = new Color(-2016482392);
    private static final Color PURPLE_OUTLINE_COLOR = Color.PURPLE;
    private static final Color BLACK_OUTLINE_COLOR = new Color(168);

    private static Color GOLD_OUTLINE_COLOR = new Color(-2686721);
    private EventButton hoveredEvent = null;
    private boolean grabbedScreen = false;
    private float grabStartY = 0.0F;
    private ScrollBar scrollBar;
    private Hitbox controllerRelicHb = null;
    private boolean show = false;

    private GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    private boolean doneSelecting = false;

    public enum SortType {NAME,MOD};

    public SortType currentSortType = null;
    public enum SortOrder {ASCENDING,DESCENDING};
    public SortOrder currentSortOrder = SortOrder.ASCENDING;

    public ArrayList<EventButton> events;
    private ArrayList<EventButton> eventsClone;

    public HashSet<String> eventAddingMods;
    public HashMap<String,String> eventModNames;

    private AbstractRelic owner;
    private boolean isDragSelecting = false;
    private boolean isTryingToScroll = false;


    private static final Comparator<EventButton> BY_NAME;
    private static final Comparator<EventButton> BY_ID;

    private static final Comparator<EventButton> BY_MOD;

    static {

        BY_NAME = Comparator.comparing(event -> event.name == null ? event.id : event.name);
        BY_ID = Comparator.comparing(event -> event.id);
        BY_MOD = Comparator.comparing(event -> {
            String cardModID = event.modID;
            return cardModID== null? "Slay the Spire" : cardModID;
        });
    }
    protected String filterMod = null;
    public boolean doneSelecting()
    {
        return doneSelecting;
    }




    public EventSelectScreen(AbstractRelic owner)
    {
        scrollBar = new ScrollBar(this);

        this.owner = owner;

        this.events = new ArrayList<>();
        this.eventAddingMods = new HashSet<>();
        this.eventModNames = new HashMap<>();

        //addBaseGameEvents();

        for (AddEventParams aep : LoadoutMod.eventsToDisplay) {
            String eID = aep.eventID;
            if (eID == null) continue;
            //LoadoutMod.logger.info("Adding event: " + eID);
            //eID = BaseMod.underScoreEventIDs.get(eID);
            //if (!BaseMod.underScoreEventIDs.containsKey(eID)) continue;

            Class<? extends AbstractEvent> eClass = aep.eventClass;

                String modID = null;
            if(eClass == null) {
                continue;
            } else {
                modID = WhatMod.findModID(eClass);
            }
            if (modID == null) modID = "Slay the Spire";

            if (!eventAddingMods.contains(modID)) {
                eventAddingMods.add(modID);
                if(!modID.equals("Slay the Spire"))
                    eventModNames.put(modID,WhatMod.findModName(eClass));
                else
                    eventModNames.put(modID,"Slay the Spire");
            }


            events.add(new EventSelectScreen.EventButton(eID,0,0,modID, eClass));
            //LoadoutMod.logger.info("Added event: " + eID + " from " + modID);
        }
        this.eventsClone = new ArrayList<>(this.events);

        if (sortHeader == null) sortHeader = new EventSelectSortHeader(this, START_X);
    }

//    private void addBaseGameEvents() {
//        String modID = "Slay the Spire";
//        String[] eIDs = {"Accursed Blacksmith","Bonfire Elementals","Fountain of Cleansing","Designer","Duplicator","Lab","Match and Keep!","Golden Shrine","Purifier","Transmorgrifier","Wheel of Change","Upgrade Shrine","FaceTrader","NoteForYourself","WeMeetAgain","The Woman in Blue","Big Fish","The Cleric","Dead Adventurer","Golden Wing","Golden Idol","World of Goop","Forgotten Altar","Scrap Ooze","Liars Game","Living Wall","Mushrooms","N'loth","Shining Light","Vampires","Ghosts","Addict","Back to Basics","Beggar","Cursed Tome","Drug Dealer","Knowing Skull","Masked Bandits","Nest","The Library","The Mausoleum","The Joust","Colosseum","Mysterious Sphere","SecretPortal","Tomb of Lord Red Mask","Falling","Winding Halls","The Moai Head","SensoryStone","MindBloom"};
//        for (String eID : eIDs)
//            events.add(new EventSelectScreen.EventButton(eID,0,0,modID));
//    }


    private void sortOnOpen() {
            this.sortHeader.justSorted = true;
            sortByMod(true);
            this.sortHeader.resetAllButtons();
            this.sortHeader.clearActiveButtons();
    }

    private boolean shouldSortById() {
        return Settings.language == Settings.GameLanguage.ZHS || Settings.language == Settings.GameLanguage.ZHT;
    }

    public void sortAlphabetically(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.events.sort(BY_ID);
            else this.events.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.events.sort(BY_ID.reversed());
            else this.events.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.events.sort(BY_MOD.thenComparing(BY_ID));
            else this.events.sort(BY_MOD.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.events.sort(BY_MOD.reversed().thenComparing(BY_ID));
            else this.events.sort(BY_MOD.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    private boolean testFilters(EventButton eb) {
        String modID = eb.modID;
        if (modID == null) modID = "Slay the Spire";
        boolean modCheck = this.filterMod == null || modID.equals(this.filterMod);

        return  modCheck;
    }

    public void updateFilters() {

        this.events = this.eventsClone.stream().filter(this::testFilters).collect(Collectors.toCollection(ArrayList::new));

        sort(currentSortOrder == SortOrder.ASCENDING);

        scrolledUsingBar(0.0f);
    }

    public void sort(boolean isAscending) {
        switch (this.currentSortType) {
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
        }
    }



    public void open()
    {
        if(AbstractDungeon.isScreenUp) {
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.overlayMenu.proceedButton.hide();
            //AbstractDungeon.closeCurrentScreen();
            LoadoutMod.isScreenUp = false;
            AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NO_INTERACT;
        }

        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.showBlackScreen(0.5f);



        show = true;
        doneSelecting = false;

        confirmButton.isDisabled = false;
        confirmButton.show();
        controllerRelicHb = null;



        this.currentSortOrder = SortOrder.ASCENDING;
        this.currentSortType = SortType.NAME;
        updateFilters();

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;

        sortOnOpen();
        sortHeader.resetAllButtons();

        calculateScrollBounds();

    }

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        show = false;
        EventfulCompass.isSelectionScreenUp = false;
    }

    public boolean isOpen()
    {
        return show;
    }

    public void update()
    {
        if (!isOpen()) {
            return;
        }

        if (InputHelper.pressedEscape) {
            close();
            InputHelper.pressedEscape = false;
            return;
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            close();
            return;
        }

        updateControllerInput();
        if (Settings.isControllerMode && controllerRelicHb != null) {
            if (Gdx.input.getY() > Settings.HEIGHT * 0.7F) {
                targetY += Settings.SCROLL_SPEED;
                if (targetY > scrollUpperBound) {
                    targetY = scrollUpperBound;
                }
            } else if (Gdx.input.getY() < Settings.HEIGHT * 0.3F) {
                targetY -= Settings.SCROLL_SPEED;
                if (targetY < scrollLowerBound) {
                    targetY = scrollLowerBound;
                }
            }
        }
        confirmButton.update();
        this.sortHeader.update();

        if (confirmButton.hb.clicked) {
            CInputActionSet.select.unpress();
            confirmButton.hb.clicked = false;
            doneSelecting = true;
        }

        if (hoveredEvent != null && InputHelper.justClickedLeft) {
            executeEvent(hoveredEvent);
        }



        hoveredEvent = null;

        boolean isScrollingScrollBar = scrollBar.update();
        if (!isScrollingScrollBar && !isDragSelecting) {
            updateScrolling();
        }
        InputHelper.justClickedLeft = false;
        InputHelper.justClickedRight = false;

        updateList(events);

        if (Settings.isControllerMode && controllerRelicHb != null) {
            Gdx.input.setCursorPosition((int)controllerRelicHb.cX, (int)(Settings.HEIGHT - controllerRelicHb.cY));
        }
        if(doneSelecting) close();
    }

    private void updateControllerInput()
    {
        // TODO
    }

    private void updateScrolling()
    {
        int y = InputHelper.mY;
        if (!grabbedScreen)
        {
            if (InputHelper.scrolledDown) {
                targetY += Settings.SCROLL_SPEED;
            } else if (InputHelper.scrolledUp) {
                targetY -= Settings.SCROLL_SPEED;
            }
            if (InputHelper.justClickedLeft)
            {
                grabbedScreen = true;
                grabStartY = (y - targetY);
            }
        }
        else if (InputHelper.isMouseDown)
        {
            targetY = (y - grabStartY);
        }
        else
        {
            grabbedScreen = false;
        }
        scrollY = MathHelper.scrollSnapLerpSpeed(scrollY, targetY);
        resetScrolling();
        updateBarPosition();
    }

    private void calculateScrollBounds()
    {
        int size = events.size();

        int scrollTmp = 0;
        if (size > 5) {
            scrollTmp = size / 5;
            scrollTmp += 5;
            if (size % 5 != 0) {
                ++scrollTmp;
            }
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * 420.0f * Settings.scale;
        } else {
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;
        }
    }

    private void resetScrolling()
    {
        if (targetY < scrollLowerBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollLowerBound);
        } else if (targetY > scrollUpperBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollUpperBound);
        }
    }

    private void updateList(ArrayList<EventButton> list)
    {
        for (EventButton e : list)
        {
            e.hb.move(e.x + 150.0f, e.y);
            e.update();
            if (e.hb.hovered)
            {
                hoveredEvent = e;
            }
            if (e.hb.clicked) {
                e.hb.clicked = false;
                if(hoveredEvent == e) {
                    //executeEvent(e);
                    break;
                }
            }
        }
    }

    public void executeEvent(EventButton eb) {

        if (AbstractDungeon.currMapNode == null) return;


        doneSelecting = true;
        close();
        Class isaacModEventPatch;
        Field hiddenRoomTimesField = null;
        int oghRoomNum = 0;

        if (Loader.isModLoadedOrSideloaded("IsaacMod")) {
            try {
                isaacModEventPatch = Class.forName("patches.event.AddEventPatch");
                hiddenRoomTimesField = isaacModEventPatch.getDeclaredField("hidenRoomTimes");
                hiddenRoomTimesField.setAccessible(true);
                oghRoomNum = (int) hiddenRoomTimesField.get(null);
                hiddenRoomTimesField.setInt(null,999);

            } catch (ClassNotFoundException e) {
                LoadoutMod.logger.info("Failed to get IsaacMod");
            } catch (NoSuchFieldException e) {
                LoadoutMod.logger.info("Failed to get IsaacMod Field");
            } catch (Exception e) {
                LoadoutMod.logger.info("Error handling Isaac Mod in EventSelectScreen");
            }
        }

        RoomEventDialog.optionList.clear();

        AbstractDungeon.eventList.add(0, eb.id);

        MapRoomNode cur = AbstractDungeon.currMapNode;
        MapRoomNode mapRoomNode2 = new MapRoomNode(cur.x, cur.y);
        mapRoomNode2.room = (AbstractRoom)new CustomEventRoom();

        ArrayList<MapEdge> curEdges = cur.getEdges();
        for (MapEdge edge : curEdges) {
            mapRoomNode2.addEdge(edge);
        }

        AbstractDungeon.player.releaseCard();
        AbstractDungeon.overlayMenu.hideCombatPanels();
        AbstractDungeon.previousScreen = null;
        AbstractDungeon.dynamicBanner.hide();
        AbstractDungeon.dungeonMapScreen.closeInstantly();
        AbstractDungeon.closeCurrentScreen();
        AbstractDungeon.topPanel.unhoverHitboxes();
        AbstractDungeon.fadeIn();
        AbstractDungeon.effectList.clear();
        AbstractDungeon.topLevelEffects.clear();
        AbstractDungeon.topLevelEffectsQueue.clear();
        AbstractDungeon.effectsQueue.clear();
        AbstractDungeon.dungeonMapScreen.dismissable = true;
        AbstractDungeon.nextRoom = mapRoomNode2;
        AbstractDungeon.setCurrMapNode(mapRoomNode2);
        AbstractDungeon.getCurrRoom().onPlayerEntry();
        AbstractDungeon.scene.nextRoom(mapRoomNode2.room);
        AbstractDungeon.rs = (mapRoomNode2.room.event instanceof com.megacrit.cardcrawl.events.AbstractImageEvent) ? AbstractDungeon.RenderScene.EVENT : AbstractDungeon.RenderScene.NORMAL;

        if (Loader.isModLoadedOrSideloaded("IsaacMod")) {
            try {
                hiddenRoomTimesField.setInt(null,oghRoomNum);
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to set hidden room count back to original");
            }

        }
    }

    public void render(SpriteBatch sb)
    {
        if (!isOpen()) {
            return;
        }

        row = -1;
        col = 0;


        renderList(sb, events, LoadoutMod.ignoreUnlock);

        scrollBar.render(sb);
        confirmButton.render(sb);

        sortHeader.render(sb);
    }

    private void renderList(SpriteBatch sb, ArrayList<EventButton> list, boolean ignoreLocks)
    {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;

        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;


        for (Iterator<EventButton> it = list.iterator(); it.hasNext(); ) {
            EventButton eventButton = it.next();

            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                 if (currentSortType == SortType.NAME) {

                    char rFirst = (shouldSortById() || eventButton.name == null) ? eventButton.id.toUpperCase().charAt(0) : eventButton.name.toUpperCase().charAt(0);
                    if (rFirst != prevFirst) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevFirst = rFirst;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevFirst != '\0') {
                            msg = String.valueOf(prevFirst).toUpperCase() + ":";
                            desc = "";
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.MOD) {
                    String rMod = eventButton.modID;
                    if (!rMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = rMod;

                        String msg = "Undefined:";
                        String desc = "";
                        if (prevMod != null) {
                            msg = prevMod + ":";
//                            if (eventButton.eventClass != null) {
//                                desc = RelicModComparator.getModDesc(eventButton.eventClass);
//                            } else desc = "";

                        }
                        //remove other lines
//                        if (desc.contains("NL")) {
//                            desc = desc.split(" NL ")[0];
//                        } else if (desc.equals("StsOrigPlaceholder")) {
//                            desc = TEXT[6];
//                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                }
            }
            if (col == 5) {
                col = 0;
                row += 1;
            }
            curX = (START_X + SPACE_X * col);
            curY = (scrollY - SPACE * row);

            eventButton.x = curX;
            eventButton.y = curY;

            eventButton.render(sb);

            col += 1;
        }
        calculateScrollBounds();
    }

    @Override
    public void scrolledUsingBar(float newPercent)
    {
        float newPosition = MathHelper.valueFromPercentBetween(scrollLowerBound, scrollUpperBound, newPercent);
        scrollY = newPosition;
        targetY = newPosition;
        updateBarPosition();
    }

    private void updateBarPosition()
    {
        float percent = MathHelper.percentFromValueBetween(scrollLowerBound, scrollUpperBound, scrollY);
        scrollBar.parentScrolledToPercent(percent);
    }
}

