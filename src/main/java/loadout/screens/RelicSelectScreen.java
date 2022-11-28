package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.*;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import loadout.LoadoutMod;
import loadout.helper.RelicClassComparator;
import loadout.helper.RelicModComparator;
import loadout.helper.RelicNameComparator;
import loadout.helper.RelicTierComparator;
import loadout.relics.LoadoutBag;
import loadout.relics.TrashBin;
import org.apache.commons.lang3.StringUtils;
//import net.sourceforge.pinyin4j.PinyinHelper;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Class from Hubris Mod
 * <a href="https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java">https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java</a>
 */
public class RelicSelectScreen extends AbstractSelectScreen<AbstractRelic> implements ScrollBarListener
{
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

    //private static final float SPACE = 80.0F * Settings.scale;
    protected static final float START_X = 750.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    //public static final float SPACE_X = 226.0F * Settings.yScale;

    //private RelicSelectSortHeader sortHeader;

    //protected float scrollY = START_Y;
    //private float targetY = this.scrollY;
//    private float scrollLowerBound = Settings.HEIGHT - 200.0F * Settings.scale;
//    private float scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;//2600.0F * Settings.scale;
//    private int scrollTitleCount = 0;
    //private int row = 0;
    //private int col = 0;
//    private static final Color RED_OUTLINE_COLOR = new Color(-10132568);
//    private static final Color GREEN_OUTLINE_COLOR = new Color(2147418280);
//    private static final Color BLUE_OUTLINE_COLOR = new Color(-2016482392);
//    private static final Color PURPLE_OUTLINE_COLOR = Color.PURPLE;
//    private static final Color BLACK_OUTLINE_COLOR = new Color(168);

    //private static Color GOLD_OUTLINE_COLOR = new Color(-2686721);
    //private AbstractRelic hoveredItem = null;
    //private AbstractRelic clickStartedItem = null;
//    private boolean grabbedScreen = false;
//    private float grabStartY = 0.0F;
    //private ScrollBar scrollBar;
    //private Hitbox controllerRelicHb = null;

    //private ArrayList<AbstractRelic> items;
    //private ArrayList<AbstractRelic> itemsClone;
    //private ArrayList<AbstractRelic> relicsClass;
    //private ArrayList<AbstractRelic> relicsClassReverse;
    //private boolean show = false;
    //public static int selectMult = 1;
    //private ArrayList<AbstractRelic> selectedItems = new ArrayList<>();

    //private GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    //private boolean doneSelecting = false;
    public boolean isDeleteMode;

    //public enum SortType {CLASS,RARITY,NAME,MOD};


    private final Comparator<AbstractRelic> relicTierComparator = RelicTierComparator.INSTANCE;
    private final Comparator<AbstractRelic> relicClassComparator = new RelicClassComparator();
    private final Comparator<AbstractRelic> relicNameComparator = RelicNameComparator.INSTANCE;
    private final Comparator<AbstractRelic> relicModComparator = RelicModComparator.INSTANCE;

    //private boolean isDragSelecting = false;
    //private boolean isTryingToScroll = false;

    protected AbstractCard.CardColor filterColor = null;
    protected AbstractRelic.RelicTier filterRarity = null;
    protected String filterMod = null;

    public static HashSet<String> screenRelics = new HashSet<>();

    static {
        screenRelics.add(BottledFlame.ID);
        screenRelics.add(BottledLightning.ID);
        screenRelics.add(BottledTornado.ID);
        screenRelics.add(TinyHouse.ID);
        screenRelics.add(EmptyCage.ID);
        screenRelics.add(Orrery.ID);
        screenRelics.add(Cauldron.ID);
        screenRelics.add(DollysMirror.ID);
    }

    public boolean doneSelecting()
    {
        return doneSelecting;
    }

    public ArrayList<AbstractRelic> getSelectedRelics()
    {
        ArrayList<AbstractRelic> ret = new ArrayList<>(selectedItems);
        selectedItems.clear();
        if (isDeleteMode)
            items.forEach(r->r.isObtained=true);
        return ret;
    }

    public HashSet<Integer> getRemovingRelics() {
        int numRelics = this.items.size();
        HashSet<Integer> ret = new HashSet<>();
        for (int i=0; i<numRelics;i++) {
            AbstractRelic r = this.items.get(i);
            if(selectedItems.contains(r)) ret.add(i);
        }
        return ret;
    }

    public RelicSelectScreen(boolean isDeleteMode, AbstractRelic owner)
    {
        super(owner);
        //scrollBar = new ScrollBar(this);
        sortHeader = new RelicSelectSortHeader(this);
        this.isDeleteMode = isDeleteMode;

        this.itemHeight = 75.0F;
    }

    @Override
    protected void sortOnOpen() {
        if(!isDeleteMode) {
            this.sortHeader.justSorted = true;
            sortByRarity(true);
            this.sortHeader.resetAllButtons();
            this.sortHeader.clearActiveButtons();
        }
    }

    public void sortByRarity(boolean isAscending) {

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(relicTierComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(relicTierComparator.reversed());
        }
        this.currentSortType = SortType.RARITY;
        scrolledUsingBar(0.0F);
    }

//    private void initClassList() {
//        this.relicsClass = new ArrayList<>();
//        this.relicsClass.addAll(RelicLibrary.redList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClass.addAll(RelicLibrary.greenList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClass.addAll(RelicLibrary.blueList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClass.addAll(RelicLibrary.whiteList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        for (HashMap<String, AbstractRelic> rPool : RelicClassComparator.customRelicPools) {
//            this.relicsClass.addAll(rPool.values().stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        }
//
//        try {
//            Field f = RelicLibrary.class.getDeclaredField("sharedRelics");
//            f.setAccessible(true);
//            HashMap<String, AbstractRelic> sharedRelics = (HashMap<String, AbstractRelic>) f.get(null);
//            this.relicsClass.addAll(sharedRelics.values().stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        } catch (Exception e) {
//            LoadoutMod.logger.error("Failed to retrieve shared relics from the pool");
//        }
//
//        this.relicsClassReverse = new ArrayList<>();
//        try {
//            Field f = RelicLibrary.class.getDeclaredField("sharedRelics");
//            f.setAccessible(true);
//            HashMap<String, AbstractRelic> sharedRelics = (HashMap<String, AbstractRelic>) f.get(null);
//            this.relicsClassReverse.addAll(sharedRelics.values().stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        } catch (Exception e) {
//            LoadoutMod.logger.error("Failed to retrieve shared relics from the pool");
//        }
//
//        for (int i = RelicClassComparator.customRelicPools.size() - 1; i >= 0 ; i --) {
//            this.relicsClassReverse.addAll(RelicClassComparator.customRelicPools.get(i).values().stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        }
//        this.relicsClassReverse.addAll(RelicLibrary.whiteList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClassReverse.addAll(RelicLibrary.blueList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClassReverse.addAll(RelicLibrary.greenList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//        this.relicsClassReverse.addAll(RelicLibrary.redList.stream().sorted(relicNameComparator).collect(Collectors.toCollection(ArrayList::new)));
//    }

    public void sortByClass(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (true) {
                this.items.sort(relicClassComparator);
            } else {
                //this.relics = this.relicsClass;
            }

        } else {

            this.currentSortOrder = SortOrder.DESCENDING;
            if (true) {
                this.items.sort(relicClassComparator.reversed());
            } else {
                //this.relics = this.relicsClassReverse;

            }

        }
        this.currentSortType = SortType.CLASS;
        scrolledUsingBar(0.0F);
    }
    public void sortAlphabetically(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(relicNameComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(relicNameComparator.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){

        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(relicModComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(relicModComparator.reversed());
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    private boolean testTextFilter(AbstractRelic ar) {
        if (ar.relicId != null && StringUtils.containsIgnoreCase(ar.relicId,sortHeader.searchBox.filterText)) return true;
        if (ar.name != null && StringUtils.containsIgnoreCase(ar.name,sortHeader.searchBox.filterText)) return true;
        if (ar.description != null && StringUtils.containsIgnoreCase(ar.description,sortHeader.searchBox.filterText)) return true;
        return false;
    }

    @Override
    protected boolean testFilters(AbstractRelic ar) {
        boolean colorCheck = this.filterColor == null || RelicClassComparator.getRelicCardColor(ar.relicId) == this.filterColor;
        String modID = WhatMod.findModID(ar.getClass());
        if (modID == null) modID = "Slay the Spire";
        boolean modCheck = this.filterMod == null || modID.equals(this.filterMod);
        boolean rarityCheck = this.filterRarity == null || ar.tier == this.filterRarity;
        boolean textCheck = sortHeader == null || sortHeader.searchBox.filterText.equals("") || testTextFilter(ar);

        return colorCheck && modCheck && rarityCheck && textCheck ;
    }

    public void updateFilters() {
        this.items = new ArrayList<>(this.itemsClone);

        this.items = this.items.stream().filter(this::testFilters).collect(Collectors.toCollection(ArrayList::new));

        sort(currentSortOrder == SortOrder.ASCENDING);

        scrolledUsingBar(0.0f);
    }

    public void sort(boolean isAscending) {
        switch (this.currentSortType) {

            case CLASS:
                sortByClass(isAscending);
                break;
            case RARITY:
                sortByRarity(isAscending);
                break;
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
        }
    }



    public void open(ArrayList<AbstractRelic> relics, int selectionMult)
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

        if (isDeleteMode)
            this.items = relics;
        else {
            this.itemsClone = relics;

            this.currentSortOrder = SortOrder.ASCENDING;
            this.currentSortType = SortType.RARITY;
            updateFilters();
            //initClassList();
        }

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;

        if(!isDeleteMode) {
            sortOnOpen();
            sortHeader.resetAllButtons();
        }


        calculateScrollBounds();

        selectedItems.clear();
        selectMult = selectionMult;

    }

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        show = false;
        LoadoutBag.isSelectionScreenUp = false;
        TrashBin.isSelectionScreenUp = false;


    }

    public void hide() {
        confirmButton.isDisabled = true;
        confirmButton.hide();
        show = false;
        LoadoutBag.isSelectionScreenUp = false;
    }

    public boolean isOpen()
    {
        return show;
    }

    @Override
    protected void callOnOpen() {

    }

    @Override
    protected void updateItemClickLogic() {

    }

    @Override
    public void update()
    {
        if (!isOpen()) {
            return;
        }
        if (LoadoutMod.isScreenUp) {
            hide();
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
        if (!isDeleteMode) this.sortHeader.update();

        if (confirmButton.hb.clicked) {
            CInputActionSet.select.unpress();
            confirmButton.hb.clicked = false;
            doneSelecting = true;
        }

        if (isDeleteMode && this.items.size() != AbstractDungeon.player.relics.size()) {
            ArrayList<AbstractRelic> relics1 = new ArrayList<>();
            for (AbstractRelic r : AbstractDungeon.player.relics)
                relics1.add(r.makeCopy());
            this.items = relics1;
        }

        if ((InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed())&& hoveredItem ==null) {
            isTryingToScroll = true;
        }
        if(InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
            isTryingToScroll = false;
        }

        if (hoveredItem != null && !isTryingToScroll) {
            if (LoadoutMod.enableDrag) {
                if (InputHelper.isMouseDown || CInputActionSet.select.isPressed()) {
                    isDragSelecting = true;
                    if (hoveredItem == clickStartedItem) {

                    } else {
                        clickStartedItem = hoveredItem;
                        if(selectedItems.contains(hoveredItem)) {
                            if(isDeleteMode)
                                selectedItems.removeIf(r->(r== hoveredItem));
                            else
                                selectedItems.removeIf(r->(r.equals(hoveredItem)));
                        } else {
                            selectedItems.add(hoveredItem);
                        }
                    }
                } else if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
                    CInputActionSet.select.unpress();
                    isDragSelecting = false;
                    if (hoveredItem == clickStartedItem) {

                        clickStartedItem = null;

                        if (doneSelecting()) {
                            close();
                        }
                    }
                }
            } else {
                //click to obtain mode
                if (InputHelper.isMouseDown || CInputActionSet.select.isPressed()) {
                    isDragSelecting = true;
                    if (hoveredItem == clickStartedItem) {
                    } else {
                        clickStartedItem = hoveredItem;
                        if (!isDeleteMode) {
                            if (screenRelics.contains(hoveredItem.relicId)) close();
                            for (int i = 0; i < LoadoutMod.relicObtainMultiplier; i++) LoadoutMod.relicsToAdd.add(clickStartedItem.makeCopy());

                        }

                    }
                } else if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
                    CInputActionSet.select.unpress();
                    isDragSelecting = false;
                    if (hoveredItem == clickStartedItem) {
                        if (isDeleteMode) {
                            LoadoutMod.relicsToRemove.add(this.items.indexOf(clickStartedItem));
                        }
                        clickStartedItem = null;
                    }

                }

            }

            if (InputHelper.justReleasedClickRight)
            {
                    CardCrawlGame.relicPopup.open(hoveredItem, items);
            }
        } else {
            clickStartedItem = null;
            isDragSelecting = false;
        }
        boolean isScrollingScrollBar = scrollBar.update();
        if (!isScrollingScrollBar && !isDragSelecting) {
            updateScrolling();
        }
        InputHelper.justClickedLeft = false;
        InputHelper.justClickedRight = false;

        hoveredItem = null;
        updateList(items);
        if (Settings.isControllerMode && controllerRelicHb != null) {
            Gdx.input.setCursorPosition((int)controllerRelicHb.cX, (int)(Settings.HEIGHT - controllerRelicHb.cY));
        }
        if(doneSelecting) close();
    }

    private void updateControllerInput()
    {
        // TODO
    }

//    private void updateScrolling()
//    {
//        int y = InputHelper.mY;
//        if (!grabbedScreen)
//        {
//            if (InputHelper.scrolledDown) {
//                targetY += Settings.SCROLL_SPEED;
//            } else if (InputHelper.scrolledUp) {
//                targetY -= Settings.SCROLL_SPEED;
//            }
//            if (InputHelper.justClickedLeft)
//            {
//                grabbedScreen = true;
//                grabStartY = (y - targetY);
//            }
//        }
//        else if (InputHelper.isMouseDown)
//        {
//            targetY = (y - grabStartY);
//        }
//        else
//        {
//            grabbedScreen = false;
//        }
//        scrollY = MathHelper.scrollSnapLerpSpeed(scrollY, targetY);
//        resetScrolling();
//        updateBarPosition();
//    }

//    private void calculateScrollBounds()
//    {
//        int size = items.size();
//
//        int scrollTmp = 0;
//        if (size > 10) {
//            scrollTmp = size / 5;
//            scrollTmp += 5;
//            if (size % 5 != 0) {
//                ++scrollTmp;
//            }
//            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * 75.0f * Settings.scale;
//        } else {
//            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;
//        }
//    }

//    private void resetScrolling()
//    {
//        if (targetY < scrollLowerBound) {
//            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollLowerBound);
//        } else if (targetY > scrollUpperBound) {
//            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollUpperBound);
//        }
//    }

    @Override
    protected void updateList(ArrayList<AbstractRelic> list)
    {
        for (AbstractRelic r : list)
        {
            r.hb.move(r.currentX, r.currentY);
            r.update();
            if (r.hb.hovered)
            {
                hoveredItem = r;
            }
        }
    }

    @Override
    public void render(SpriteBatch sb)
    {
        if (!isOpen()) {
            return;
        }
//        if (LoadoutMod.isScreenUp) {
//            return;
//        }

        row = -1;
        col = 0;


        renderList(sb, items);

        scrollBar.render(sb);
        confirmButton.render(sb);
        if (!isDeleteMode)
            sortHeader.render(sb);
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<AbstractRelic> list)
    {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;
        AbstractRelic.RelicTier prevTier = null;
        AbstractCard.CardColor prevType = null;
        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;
        boolean isRelicLocked = false;
        Color outlineColor;

        if(isDeleteMode) {
            FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, TEXT[7], START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * (this.row-1), 99999.0F, 0.0F, Settings.GOLD_COLOR);
        }

        for (Iterator<AbstractRelic> it = list.iterator(); it.hasNext(); ) {
            AbstractRelic r = it.next();
            if (isDeleteMode) {
                //r = r.makeCopy();
                r.isObtained = false;
            }
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.RARITY) {
                    if (r.tier != prevTier) {
                        row++;
                        scrollTitleCount++;
                        //if new tier, render new texts
                        prevTier = r.tier;

                        String msg;
                        String desc;

                        if (prevTier != null) {
                            switch (prevTier) {
                                case STARTER:
                                    msg = rTEXT[1];
                                    desc = rTEXT[2];
                                    break;
                                case COMMON:
                                    msg = rTEXT[3];
                                    desc = rTEXT[4];
                                    break;
                                case UNCOMMON:
                                    msg = rTEXT[5];
                                    desc = rTEXT[6];
                                    break;
                                case RARE:
                                    msg = rTEXT[7];
                                    desc = rTEXT[8];
                                    break;
                                case BOSS:
                                    msg = rTEXT[9];
                                    desc = rTEXT[10];
                                    break;
                                case SPECIAL:
                                    msg = rTEXT[11];
                                    desc = rTEXT[12];
                                    break;
                                case SHOP:
                                    msg = rTEXT[13];
                                    desc = rTEXT[14];
                                    break;
                                case DEPRECATED:
                                    msg = TEXT[0];
                                    desc = TEXT[1];
                                    break;
                                default:
                                    msg = TEXT[2];
                                    desc = TEXT[3];
                            }
                        } else {
                            msg = TEXT[2];
                            desc = TEXT[3];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.CLASS) {
                    AbstractCard.CardColor rType = RelicClassComparator.getRelicCardColor(r.relicId);
                    if (rType != prevType) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevType = rType;

                        String msg = "NoClass:";
                        String desc = "No Class";

                        if (prevType.ordinal() <= 3) {
                            switch (prevType) {
                                case RED:
                                    msg = charStrings[0].NAMES[0];
                                    desc = charStrings[0].TEXT[0];
                                    break;
                                case GREEN:
                                    msg = charStrings[1].NAMES[0];
                                    desc = charStrings[1].TEXT[0];
                                    break;
                                case BLUE:
                                    msg = charStrings[2].NAMES[0];
                                    desc = charStrings[2].TEXT[0];
                                    break;
                                case PURPLE:
                                    msg = charStrings[3].NAMES[0];
                                    desc = charStrings[3].TEXT[0];
                                    break;
                                default:
                                    msg = TEXT[2];
                                    desc = TEXT[3];
                            }


                        } else if (LoadoutMod.allRelics.containsKey(prevType) && prevType != AbstractCard.CardColor.COLORLESS) {
                            //if modded characters
                            AbstractPlayer modPlayer = RelicClassComparator.getCharacterByColor(prevType);
                            if (modPlayer != null) {
                                CharSelectInfo modCharInfo = modPlayer.getLoadout();
                                msg = modCharInfo.name;
                                desc = modCharInfo.flavorText;
                            } else {
                                msg = TEXT[2];
                                desc = TEXT[3];
                            }

                        } else if (prevType == AbstractCard.CardColor.COLORLESS) {
                            msg = TEXT[4];
                            desc = TEXT[5];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F * Settings.scale, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.NAME) {
                    //check if the name is alphanumeric
                    //char rFirst = (LoadoutMod.languageSupport().equals("eng")&&r.name.substring(0,1).matches("[A-Za-z\\d]+")) ? r.name.toUpperCase().charAt(0) : RelicNameComparator.editModRelicId(r.relicId).toUpperCase().charAt(0);
                    //char rFirst = RelicNameComparator.editModRelicId(r.relicId).toUpperCase().charAt(0);
                    char rFirst = RelicNameComparator.getComparableRelicName(r).toUpperCase().charAt(0);
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
                    String rMod = RelicModComparator.getModName(r);
                    if (!rMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = rMod;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevMod != null) {
                            msg = prevMod + ":";
                            desc = RelicModComparator.getModDesc(r);
                        }
                        //remove other lines
                        if (desc.contains("NL")) {
                            desc = desc.split(" NL ")[0];
                        } else if (desc.equals("StsOrigPlaceholder")) {
                            desc = TEXT[6];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                }
            }
            if (col == 10) {
                col = 0;
                row += 1;
            }
            curX = (START_X + SPACE * col);
            curY = (scrollY - SPACE * row);

            r.currentX = curX;
            r.currentY = curY;

            if(LoadoutMod.ignoreUnlock) {
                r.isSeen = true;
            } else {
                r.isSeen = UnlockTracker.isRelicSeen(r.relicId);
            }
            if (!isDeleteMode) isRelicLocked = UnlockTracker.isRelicLocked(r.relicId)&&!LoadoutMod.ignoreUnlock;

            if(isDeleteMode) {

                if(selectedItems.contains(r)) {
                    sb.setColor(new Color(1.0F, 0.8F, 0.2F, 0.2F + (
                            MathUtils.cosDeg((float)(System.currentTimeMillis() / 4L % 360L)) + 1.25F) / 5.0F));
                    sb.draw(ImageMaster.FILTER_GLOW_BG, curX-64.0F, curY-64.0F, 64.0F, 64.0F, 128.0f, 128.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                    r.render(sb,false,Color.GOLD);
                } else {
                    outlineColor = RelicClassComparator.getRelicColor(r);
                    r.render(sb, false, outlineColor);
                }
            } else {
                if(selectedItems.contains(r)) {
                    sb.setColor(new Color(1.0F, 0.8F, 0.2F, 0.5F + (
                            MathUtils.cosDeg((float)(System.currentTimeMillis() / 4L % 360L)) + 1.25F) / 5.0F));
                    sb.draw(ImageMaster.FILTER_GLOW_BG, curX-64.0F, curY-64.0F, 64.0F, 64.0F, 128.0f, 128.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                    if (isRelicLocked) {
                        r.renderLock(sb, Color.GOLD);
                    } else {
                        r.render(sb,false,Color.GOLD);
                    }
                } else {
                    outlineColor = RelicClassComparator.getRelicColor(r);
                    if (isRelicLocked) {
                        r.renderLock(sb, outlineColor);
                    } else {
                        r.render(sb, false, outlineColor);
                    }
                }
            }

            col += 1;
        }
        calculateScrollBounds();
    }

//    @Override
//    public void scrolledUsingBar(float newPercent)
//    {
//        float newPosition = MathHelper.valueFromPercentBetween(scrollLowerBound, scrollUpperBound, newPercent);
//        scrollY = newPosition;
//        targetY = newPosition;
//        updateBarPosition();
//    }
//
//    private void updateBarPosition()
//    {
//        float percent = MathHelper.percentFromValueBetween(scrollLowerBound, scrollUpperBound, scrollY);
//        scrollBar.parentScrolledToPercent(percent);
//    }
}
