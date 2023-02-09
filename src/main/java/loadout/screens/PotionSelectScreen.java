package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import loadout.LoadoutMod;
import loadout.helper.*;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.LoadoutCauldron;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Class from Hubris Mod
 * <a href="https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java">https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java</a>
 */
public class PotionSelectScreen extends AbstractSelectScreen<AbstractPotion>
{

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectionScreen"));
    public static final String[] TEXT = UiStrings.TEXT;

    protected static final float START_X = 750.0F * Settings.scale;
    private final Comparator<AbstractPotion> potionTierComparator = new PotionTierComparator();
    private final Comparator<AbstractPotion> potionClassComparator = new PotionClassComparator();
    private final Comparator<AbstractPotion> potionNameComparator = new PotionNameComparator();
    private final Comparator<AbstractPotion> potionModComparator = new PotionModComparator();

    public AbstractPotion.PotionRarity filterRarity = null;


    public PotionSelectScreen(AbstractCustomScreenRelic<AbstractPotion> owner)
    {
        super(owner);
        this.defaultSortType = SortType.RARITY;
        sortHeader = new PotionSelectSortHeader(this);
    }

    private boolean checkPotionClass(AbstractPotion ap) {
        boolean isShared = false;
        if(this.filterColor == AbstractCard.CardColor.COLORLESS) {
            isShared = PotionClassComparator.sharedList.contains(ap.ID) ;
        }

        return isShared || PotionClassComparator.getPotionClass(ap) == this.filterColor;
    }
    private boolean testTextFilter(AbstractPotion ap) {
        if (ap.ID != null && StringUtils.containsIgnoreCase(ap.ID,(this.sortHeader).searchBox.filterText)) return true;
        if (ap.name != null && StringUtils.containsIgnoreCase(ap.name,(this.sortHeader).searchBox.filterText)) return true;
        if (ap.description != null && StringUtils.containsIgnoreCase(ap.description,(this.sortHeader).searchBox.filterText)) return true;
        return false;
    }
    @Override
    protected boolean testFilters(AbstractPotion ap) {
        boolean colorCheck = this.filterColor == null || checkPotionClass(ap);
        String modID = WhatMod.findModID(ap.getClass());
        if (modID == null) modID = "Slay the Spire";
        boolean modCheck = this.filterMod == null || modID.equals(this.filterMod);
        boolean rarityCheck = this.filterRarity == null || ap.rarity == this.filterRarity;
        boolean textCheck = sortHeader == null || (this.sortHeader).searchBox.filterText.equals("") || testTextFilter(ap);
        return colorCheck && modCheck && rarityCheck && textCheck;
    }

    @Override
    public void sort(boolean isAscending) {
        switch (this.currentSortType) {

            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
            case RARITY:
                sortByRarity(isAscending);
                break;
            case CLASS:
                sortByClass(isAscending);
                break;
        }
    }

    public void sortByRarity(boolean isAscending) {
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(potionTierComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(potionTierComparator.reversed());
        }
        this.currentSortType = SortType.RARITY;
        scrolledUsingBar(0.0F);
    }

    public void sortByClass(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(potionClassComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(potionClassComparator.reversed());
        }
        this.currentSortType = SortType.CLASS;
        scrolledUsingBar(0.0F);
    }
    public void sortAlphabetically(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(potionNameComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(potionNameComparator.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(potionModComparator);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(potionModComparator.reversed());
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    @Override
    protected void callOnOpen() {
        this.itemsClone = LoadoutMod.potionsToDisplay;
        this.items = new ArrayList<>(itemsClone);
    }

    @Override
    protected void updateItemClickLogic() {
        if (hoveredItem != null) {
            if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
                clickStartedItem = hoveredItem;
                //logger.info("Pressed Left");
            }
            if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredItem == clickStartedItem)
                {
                    AbstractDungeon.player.obtainPotion(hoveredItem.makeCopy());
                    AbstractPotion.playPotionSound();
                    this.owner.flash();
                    clickStartedItem = null;

                    if (doneSelecting()) {
                        close();
                    }
                }
            }

            if (InputHelper.justClickedRight || CInputActionSet.select.isJustPressed()) {
                clickStartedItem = hoveredItem;

            }
            if (InputHelper.justReleasedClickRight || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredItem == clickStartedItem)
                {
                    clickStartedItem = null;
                }
            }
        } else {
            clickStartedItem = null;
        }
    }

//    public void update()
//    {
//        if (!isOpen()) {
//            return;
//        }
//        if (InputHelper.pressedEscape) {
//            close();
//            InputHelper.pressedEscape = false;
//            return;
//        }
//        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
//            close();
//            return;
//        }
//
//        updateControllerInput();
//        if (Settings.isControllerMode && controllerRelicHb != null) {
//            if (Gdx.input.getY() > Settings.HEIGHT * 0.7F) {
//                targetY += Settings.SCROLL_SPEED;
//                if (targetY > scrollUpperBound) {
//                    targetY = scrollUpperBound;
//                }
//            } else if (Gdx.input.getY() < Settings.HEIGHT * 0.3F) {
//                targetY -= Settings.SCROLL_SPEED;
//                if (targetY < scrollLowerBound) {
//                    targetY = scrollLowerBound;
//                }
//            }
//        }
//        confirmButton.update();
//        this.sortHeader.update();
//
//        if (confirmButton.hb.clicked) {
//            CInputActionSet.select.unpress();
//            confirmButton.hb.clicked = false;
//            doneSelecting = true;
//        }
//        if (hoveredItem != null) {
//            if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
//                clickStartedItem = hoveredItem;
//                //logger.info("Pressed Left");
//            }
//            if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustPressed())
//            {
//                CInputActionSet.select.unpress();
//                if (hoveredItem == clickStartedItem)
//                {
//                    AbstractDungeon.player.obtainPotion(hoveredItem.makeCopy());
//                    AbstractPotion.playPotionSound();
//                    this.owner.flash();
//                    clickStartedItem = null;
//
//                    if (doneSelecting()) {
//                        close();
//                    }
//                }
//            }
//
//            if (InputHelper.justClickedRight || CInputActionSet.select.isJustPressed()) {
//                clickStartedItem = hoveredItem;
//
//            }
//            if (InputHelper.justReleasedClickRight || CInputActionSet.select.isJustPressed())
//            {
//                CInputActionSet.select.unpress();
//                if (hoveredItem == clickStartedItem)
//                {
//                    clickStartedItem = null;
//                }
//            }
//        } else {
//            clickStartedItem = null;
//        }
//        boolean isScrollingScrollBar = scrollBar.update();
//        if (!isScrollingScrollBar) {
//            updateScrolling();
//        }
//        InputHelper.justClickedLeft = false;
//        InputHelper.justClickedRight = false;
//
//        hoveredItem = null;
//        updateList(items);
//        if (Settings.isControllerMode && controllerRelicHb != null) {
//            Gdx.input.setCursorPosition((int)controllerRelicHb.cX, (int)(Settings.HEIGHT - controllerRelicHb.cY));
//        }
//        if(doneSelecting) close();
//    }



    protected void updateList(ArrayList<AbstractPotion> list)
    {
        for (AbstractPotion p : list)
        {
            p.hb.update();
            p.hb.move(p.posX, p.posY);

            p.update();
            if (p.hb.hovered)
            {
                hoveredItem = p;
            }
        }
    }


    protected void renderList(SpriteBatch sb, ArrayList<AbstractPotion> list)
    {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;
        AbstractPotion.PotionRarity prevRarity = null;
        int prevType = -1;
        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;
        boolean isRelicLocked = false;
        Color outlineColor;


        for (Iterator<AbstractPotion> it = list.iterator(); it.hasNext(); ) {
            AbstractPotion p = it.next();
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.RARITY) {
                    if (p.rarity != prevRarity) {
                        row++;
                        scrollTitleCount++;
                        //if new tier, render new texts
                        prevRarity = p.rarity;

                        String msg;
                        String desc;

                        if (prevRarity != null) {
                            switch (prevRarity) {
                                case PLACEHOLDER:
                                    msg = TEXT[0];
                                    desc = TEXT[3];
                                    break;
                                case COMMON:
                                    msg = pTEXT[1];
                                    desc = pTEXT[2];
                                    break;
                                case UNCOMMON:
                                    msg = pTEXT[3];
                                    desc = pTEXT[4];
                                    break;
                                case RARE:
                                    msg = pTEXT[5];
                                    desc = pTEXT[6];
                                    break;
                                default:
                                    msg = prevRarity.toString();
                                    if(msg.length()>1)
                                        msg = msg.toUpperCase().charAt(0) + msg.substring(1).toLowerCase() + ":";
                                    desc = "";
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
                    int rType = PotionClassComparator.potionClassToInt(p);
                    if (rType != prevType) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevType = rType;

                        String msg = "NoClass:";
                        String desc = "";

                        ArrayList<AbstractPlayer.PlayerClass> classList = PotionClassComparator.classList;

                        if (prevType >= 0 && prevType <= 3) {
                            switch (prevType) {
                                case 0:
                                    msg = charStrings[0].NAMES[0];
                                    desc = charStrings[0].TEXT[0];
                                    break;
                                case 1:
                                    msg = charStrings[1].NAMES[0];
                                    desc = charStrings[1].TEXT[0];
                                    break;
                                case 2:
                                    msg = charStrings[2].NAMES[0];
                                    desc = charStrings[2].TEXT[0];
                                    break;
                                case 3:
                                    msg = charStrings[3].NAMES[0];
                                    desc = charStrings[3].TEXT[0];
                                    break;
                                default:
                                    msg = TEXT[2];
                                    desc = TEXT[3];
                            }


                        } else if (prevType > 3 && prevType < classList.size()) {
                            //if modded characters
                            AbstractPlayer.PlayerClass playerClass = classList.get(prevType);
                            AbstractPlayer modPlayer = PotionClassComparator.getCharacterByClass(playerClass);
                            CharSelectInfo modCharInfo = modPlayer.getLoadout();
                            msg = modCharInfo.name;
                            desc = modCharInfo.flavorText;
                        } else if (prevType == classList.size()) {
                            msg = TEXT[4];
                            //desc = TEXT[5];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F * Settings.scale, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.NAME) {
                    //check if the name is alphanumeric
                    char pFirst = (LoadoutMod.languageSupport().equals("eng")&&p.name.substring(0,1).matches("[A-Za-z\\d]+")) ? p.name.toUpperCase().charAt(0) : PotionNameComparator.editModPotionId(p.ID).toUpperCase().charAt(0);
                    //char pFirst = PotionNameComparator.editModPotionId(p.ID).toUpperCase().charAt(0);
                    if (pFirst != prevFirst) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevFirst = pFirst;

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
                    String pMod = PotionModComparator.getModName(p);
                    if (!pMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = pMod;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevMod != null) {
                            msg = prevMod + ":";
                            desc = PotionModComparator.getModDesc(p);
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

            p.posX = curX;
            p.posY = curY;
                if(selectedItems.contains(p)) {
                    sb.setColor(new Color(1.0F, 0.8F, 0.2F, 0.5F + (
                            MathUtils.cosDeg((float)(System.currentTimeMillis() / 4L % 360L)) + 1.25F) / 5.0F));
                    sb.draw(ImageMaster.FILTER_GLOW_BG, curX-60.0F*Settings.scale, curY-64.0F*Settings.yScale, 64.0F, 64.0F, 80.0f*1.5f, 80.0f*1.5f, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                    p.renderOutline(sb, Color.GOLD);
                    p.labRender(sb);
                } else {
                    outlineColor = PotionClassComparator.getPotionColor(p);
                    p.renderOutline(sb,outlineColor);
                    p.labRender(sb);
                }


            col += 1;
        }
        calculateScrollBounds();
    }

}
