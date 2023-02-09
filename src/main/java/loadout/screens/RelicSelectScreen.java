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
import loadout.relics.AbstractCardScreenRelic;
import loadout.relics.AbstractCustomScreenRelic;
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

    protected static final float START_X = 750.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public boolean isDeleteMode;

    private final Comparator<AbstractRelic> relicTierComparator = RelicTierComparator.INSTANCE;
    private final Comparator<AbstractRelic> relicClassComparator = new RelicClassComparator();
    private final Comparator<AbstractRelic> relicNameComparator = RelicNameComparator.INSTANCE;
    private final Comparator<AbstractRelic> relicModComparator = RelicModComparator.INSTANCE;


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

    public RelicSelectScreen(boolean isDeleteMode, AbstractCustomScreenRelic<AbstractRelic> owner)
    {
        super(owner);
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


    public void sortByClass(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(relicClassComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(relicClassComparator.reversed());
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

        AbstractSelectScreen.hideLoadoutRelics();
    }

    public void hide() {
        confirmButton.isDisabled = true;
        confirmButton.hide();
        show = false;
        owner.setIsSelectionScreenUp(false);
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

}
