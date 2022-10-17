package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import loadout.LoadoutMod;
import loadout.helper.PotionModComparator;
import loadout.helper.PotionNameComparator;
import loadout.relics.PowerGiver;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Class from Hubris Mod
 * <a href="https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java">https://github.com/kiooeht/Hubris/blob/master/src/main/java/com/evacipated/cardcrawl/mod/hubris/screens/select/RelicSelectScreen.java</a>
 */
public class PowerSelectScreen implements ScrollBarListener
{
    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString("RelicViewScreen");
    public static final String[] rTEXT = rUiStrings.TEXT;
    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    private static final UIStrings pUiStrings = CardCrawlGame.languagePack.getUIString("PotionViewScreen");
    public static final String[] pTEXT = pUiStrings.TEXT;
    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectionScreen"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final CharacterStrings redStrings = CardCrawlGame.languagePack.getCharacterString("Ironclad");
    private static final CharacterStrings greenStrings = CardCrawlGame.languagePack.getCharacterString("Silent");
    private static final CharacterStrings blueStrings = CardCrawlGame.languagePack.getCharacterString("Defect");
    private static final CharacterStrings purpleStrings = CardCrawlGame.languagePack.getCharacterString("Watcher");

    private static final CharacterStrings[] charStrings = {redStrings,greenStrings,blueStrings,purpleStrings};

    private static final float SPACE = 80.0F * Settings.scale;
    protected static final float START_X = 750.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 226.0F * Settings.yScale;

    private PotionSelectSortHeader sortHeader;

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
    private AbstractPower hoveredPower = null;
    private AbstractPower clickStartedPower = null;
    private boolean grabbedScreen = false;
    private float grabStartY = 0.0F;
    private ScrollBar scrollBar;
    private Hitbox controllerRelicHb = null;

    private ArrayList<Class<? extends AbstractPower>> powers;
    private boolean show = false;
    public static int selectMult = 1;
    private ArrayList<AbstractPower> selectedPowers = new ArrayList<>();

    private GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    private boolean doneSelecting = false;
    public boolean isDeleteMode;

    public enum SortType {TYPE,NAME,MOD};

    public SortType currentSortType = null;
    public enum SortOrder {ASCENDING,DESCENDING};
    public SortOrder currentSortOrder = SortOrder.ASCENDING;

    private static final Comparator<AbstractPower> BY_TYPE = Comparator.comparing(p -> p.type);
    private static final Comparator<AbstractPower> BY_NAME = Comparator.comparing(p -> p.name);
    private static final Comparator<AbstractPower> BY_MOD = Comparator.comparing(p -> {
        String powerModID = WhatMod.findModID(p.getClass());
        return powerModID == null? "Slay the Spire" : powerModID;
    });

    private AbstractRelic owner;

    public boolean doneSelecting()
    {
        return doneSelecting;
    }

    public ArrayList<AbstractPower> getSelectedPowers()
    {
        ArrayList<AbstractPower> ret = new ArrayList<>(selectedPowers);
        selectedPowers.clear();
        return ret;
    }

    public PowerSelectScreen(boolean isDeleteMode, AbstractRelic owner)
    {
        scrollBar = new ScrollBar(this);

        this.isDeleteMode = isDeleteMode;
        this.owner = owner;
    }

    private void sortOnOpen() {
        if(!isDeleteMode) {
            this.sortHeader.justSorted = true;
            sortAlphabetically(true);
            this.sortHeader.resetAllButtons();
            this.sortHeader.clearActiveButtons();
        }
    }
    public void sortByType(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.powers.sort(BY_TYPE.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.powers.sort(BY_TYPE.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.TYPE;
        scrolledUsingBar(0.0F);
    }

    public void sortAlphabetically(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.powers.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.powers.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.powers.sort(BY_MOD);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.powers.sort(BY_MOD.reversed());
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    public void open(ArrayList<Class<? extends AbstractPower>> powers)
    {
        if(AbstractDungeon.isScreenUp) {
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.overlayMenu.proceedButton.hide();
            AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NO_INTERACT;
        }

        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.showBlackScreen(0.5f);

        show = true;
        doneSelecting = false;

        confirmButton.isDisabled = false;
        confirmButton.show();
        controllerRelicHb = null;
        this.powers = powers;

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;
        sortOnOpen();
        calculateScrollBounds();
        selectedPowers.clear();
    }

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();
        show = false;
        PowerGiver.isSelectionScreenUp = false;
        if (isDeleteMode) {
            this.powers.clear();
        }
    }

    public boolean isOpen()
    {
        return show;
    }

    private boolean isCombat() {
        return AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    public void addPowerToPlayer(AbstractPower p, int stackAmount) {
        if (isCombat()) {
            try {
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player,AbstractDungeon.player, (AbstractPower) p.getClass().getDeclaredConstructors()[0].newInstance(), stackAmount));
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (hoveredPower != null) {
            if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
                clickStartedPower = hoveredPower;
                //logger.info("Pressed Left");
            }
            if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredPower == clickStartedPower)
                {



                    this.owner.flash();
                    clickStartedPower = null;

                    if (doneSelecting()) {
                        close();
                    }
                }
            }

            if (InputHelper.justClickedRight || CInputActionSet.select.isJustPressed()) {
                clickStartedPower = hoveredPower;

            }
            if (InputHelper.justReleasedClickRight || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredPower == clickStartedPower)
                {
                    clickStartedPower = null;
                }
            }
        } else {
            clickStartedPower = null;
        }
        boolean isScrollingScrollBar = scrollBar.update();
        if (!isScrollingScrollBar) {
            updateScrolling();
        }
        InputHelper.justClickedLeft = false;
        InputHelper.justClickedRight = false;

        hoveredPower = null;
        updateList(powers);
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
        int size = powers.size();

        int scrollTmp = 0;
        if (size > 10) {
            scrollTmp = size / 5-2;
            scrollTmp += 5;
            if (size % 5 != 0) {
                ++scrollTmp;
            }
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * 75.0f * Settings.scale;
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

    private void updateList(ArrayList<AbstractPower> list)
    {
        for (AbstractPower p : list)
        {
//            p.update();
//            p.hb.move(p.posX, p.posY);
//            if(!isDeleteMode)
//                p.update();
//            if (p.hb.hovered)
//            {
//                hoveredPower = p;
//            }
        }
    }

    public void render(SpriteBatch sb)
    {
        if (!isOpen()) {
            return;
        }

        row = -1;
        col = 0;
        renderList(sb, powers);

        scrollBar.render(sb);
        confirmButton.render(sb);
        if (!isDeleteMode)
            sortHeader.render(sb);
    }

    private void renderList(SpriteBatch sb, ArrayList<AbstractPower> list)
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

        if(isDeleteMode) {
            FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, TEXT[7], START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * (this.row-1), 99999.0F, 0.0F, Settings.GOLD_COLOR);
        }

        for (Iterator<AbstractPower> it = list.iterator(); it.hasNext(); ) {
            AbstractPower p = it.next();
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {
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
                    String pMod = WhatMod.findModName(p.getClass());
                    if (pMod == null) pMod = "Slay the Spire";
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

//            p.posX = curX;
//            p.posY = curY;
//                if(selectedPowers.contains(p)) {
//                    sb.setColor(new Color(1.0F, 0.8F, 0.2F, 0.5F + (
//                            MathUtils.cosDeg((float)(System.currentTimeMillis() / 4L % 360L)) + 1.25F) / 5.0F));
//                    sb.draw(ImageMaster.FILTER_GLOW_BG, curX-60.0F*Settings.scale, curY-64.0F*Settings.yScale, 64.0F, 64.0F, 80.0f*1.5f, 80.0f*1.5f, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
//                    p.renderOutline(sb, Color.GOLD);
//                    p.labRender(sb);
//                } else {
//                    outlineColor = PotionClassComparator.getRelicColor(p);
//                    p.renderOutline(sb,outlineColor);
//                    p.labRender(sb);
//                }


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
