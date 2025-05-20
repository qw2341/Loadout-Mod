package loadout.screens;

import basemod.ReflectionHacks;
import basemod.eventUtil.AddEventParams;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import loadout.LoadoutMod;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.EventfulCompass;
import loadout.util.ModConfig;
import org.apache.commons.lang3.StringUtils;
//import net.sourceforge.pinyin4j.PinyinHelper;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class EventSelectScreen extends AbstractSelectScreen<EventSelectScreen.EventButton>
{
    public static class EventButton {
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
                try {
                    this.name = (String) ReflectionHacks.getPrivateStatic(eClass,"NAME");
                } catch (Exception ignored) {

                }

                if (this.name == null || this.name.length() == 0) this.name = eClass.getSimpleName();
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
            this.hb.update();
        }

        public void render(SpriteBatch sb) {
            if (this.hb != null) {
                this.hb.render(sb);
                if (this.hb.hovered) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                    sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x+75.0F,y-50.0F, 150.0F, 50.0F, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 220, 220, false, false);
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f*Settings.scale / 2,y + 20.0f*Settings.scale,200.0f*Settings.scale,25.0f*Settings.scale,Settings.GOLD_COLOR);
                    sb.setBlendFunction(770, 771);

                    TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
                } else {
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f*Settings.scale / 2,y + 20.0f*Settings.scale,200.0f*Settings.scale,25.0f*Settings.scale,Settings.CREAM_COLOR);
                }
            }

        }

        public AbstractEvent getEvent() {
            AbstractEvent ret;
            try {
                ret = this.eventClass.getConstructor().newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                e.printStackTrace();
                try {
                    ret = this.eventClass.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    ex.printStackTrace();
                    ret = EventHelper.getEvent(this.id);
                }
            }
            return ret;
        }
    }
    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectionScreen"));
    public static final String[] TEXT = UiStrings.TEXT;


    public HashSet<String> eventAddingMods;
    public HashMap<String,String> eventModNames;



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




    public EventSelectScreen(AbstractCustomScreenRelic<EventButton> owner)
    {
        super(owner);
        scrollBar = new ScrollBar(this);

        this.owner = owner;

        this.items = new ArrayList<>();
        this.eventAddingMods = new HashSet<>();
        this.eventModNames = new HashMap<>();

        for (AddEventParams aep : LoadoutMod.eventsToDisplay) {
            String eID = aep.eventID;
            if (eID == null) continue;


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


            items.add(new EventButton(eID, 0, 0, modID, eClass));
            //LoadoutMod.logger.info("Added event: " + eID + " from " + modID);
        }
        this.itemsClone = new ArrayList<>(this.items);

        if (sortHeader == null) sortHeader = new EventSelectSortHeader(this, START_X);
        this.defaultSortType = SortType.MOD;
    }

//    private void addBaseGameEvents() {
//        String modID = "Slay the Spire";
//        String[] eIDs = {"Accursed Blacksmith","Bonfire Elementals","Fountain of Cleansing","Designer","Duplicator","Lab","Match and Keep!","Golden Shrine","Purifier","Transmorgrifier","Wheel of Change","Upgrade Shrine","FaceTrader","NoteForYourself","WeMeetAgain","The Woman in Blue","Big Fish","The Cleric","Dead Adventurer","Golden Wing","Golden Idol","World of Goop","Forgotten Altar","Scrap Ooze","Liars Game","Living Wall","Mushrooms","N'loth","Shining Light","Vampires","Ghosts","Addict","Back to Basics","Beggar","Cursed Tome","Drug Dealer","Knowing Skull","Masked Bandits","Nest","The Library","The Mausoleum","The Joust","Colosseum","Mysterious Sphere","SecretPortal","Tomb of Lord Red Mask","Falling","Winding Halls","The Moai Head","SensoryStone","MindBloom"};
//        for (String eID : eIDs)
//            events.add(new EventSelectScreen.EventButton(eID,0,0,modID));
//    }


    public void sortAlphabetically(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.items.sort(BY_ID);
            else this.items.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.items.sort(BY_ID.reversed());
            else this.items.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.items.sort(BY_MOD.thenComparing(BY_ID));
            else this.items.sort(BY_MOD.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.items.sort(BY_MOD.reversed().thenComparing(BY_ID));
            else this.items.sort(BY_MOD.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    private boolean testTextFilter(EventButton eventButton) {
        if (eventButton.id != null && StringUtils.containsIgnoreCase(eventButton.id,sortHeader.searchBox.filterText)) return true;
        if (eventButton.name != null && StringUtils.containsIgnoreCase(eventButton.name,sortHeader.searchBox.filterText)) return true;
        //if (eventButton. != null && StringUtils.containsIgnoreCase(eventButton.description,sortHeader.searchBox.filterText)) return true;
        return false;
    }

    protected boolean testFilters(EventButton eb) {
        String modID = eb.modID;
        if (modID == null) modID = "Slay the Spire";
        boolean modCheck = this.filterMod == null || modID.equals(this.filterMod);
        boolean textCheck = sortHeader == null || sortHeader.searchBox.filterText.equals("") || testTextFilter(eb);

        return  modCheck && textCheck;
    }

    public void updateFilters() {

        this.items = this.itemsClone.stream().filter(this::testFilters).collect(Collectors.toCollection(ArrayList::new));

        sort(currentSortOrder == SortOrder.ASCENDING);

        scrolledUsingBar(0.0f);
    }

    public void sort(boolean isAscending) {
        SortType st = this.currentSortType == null ? this.defaultSortType : this.currentSortType;
        switch (st) {
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
        }
    }


    @Override
    protected void updateItemClickLogic() {
        if (hoveredItem != null && InputHelper.justClickedLeft) {
            EventfulCompass.lastEvent = hoveredItem;
            executeEvent(hoveredItem);
        }
    }

    @Override
    protected void callOnOpen() {
        this.currentSortOrder = SortOrder.ASCENDING;
        this.currentSortType = SortType.MOD;
        updateFilters();

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;
    }


    protected void updateList(ArrayList<EventButton> list)
    {
        for (EventButton e : list)
        {
            e.hb.move(e.x + 150.0f*Settings.scale, e.y);
            e.update();
            if (e.hb.hovered)
            {
                hoveredItem = e;
            }
            if (e.hb.clicked) {
                e.hb.clicked = false;
                if(hoveredItem == e) {
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

        //AbstractDungeon.eventList.add(0, eb.id);

        EventfulCompass.goToRoom(eb);

        if (Loader.isModLoadedOrSideloaded("IsaacMod")) {
            try {
                hiddenRoomTimesField.setInt(null,oghRoomNum);
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to set hidden room count back to original");
            }

        }

        this.owner.flash();
    }

    public void render(SpriteBatch sb)
    {
        if (!isOpen()) {
            return;
        }

        row = -1;
        col = 0;


        renderList(sb, items);

        scrollBar.render(sb);
        confirmButton.render(sb);

        sortHeader.render(sb);
    }



    protected void renderList(SpriteBatch sb, ArrayList<EventButton> list)
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

            if(ModConfig.enableCategory&&this.currentSortType!=null) {
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
                        if (ModConfig.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

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
                        if (ModConfig.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

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

}

