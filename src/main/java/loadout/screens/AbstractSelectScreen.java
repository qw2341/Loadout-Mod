package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import loadout.LoadoutMod;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.AllInOneBag;

import java.util.ArrayList;
import java.util.stream.Collectors;

public abstract class AbstractSelectScreen<T> implements ScrollBarListener {

    public static abstract class AbstractSelectButton<V> {

        static final float BUTTON_W = 200.0f * Settings.scale;
        static final float BUTTON_H = 75.0f * Settings.yScale;
        static final String PLACEHOLDER_ID  = "No ID Placeholder";
        static final String STS_MODID = "Slay the Spire";
        public Class<? extends V> pClass;
        public V instance;
        public String id;
        public String name;
        public String modID;
        public String desc;
        public int amount;
        public Hitbox hb;
        public float x;
        public float y;
        public ArrayList<PowerTip> tips;
        public boolean hasAmount = true;

        public AbstractSelectButton(String id, Class<? extends V> pClass) {
            this();
            this.pClass = pClass;
            this.id = id;
            this.modID = WhatMod.findModID(pClass);
            if (this.modID == null) this.modID = STS_MODID;

            if(this.id == null) this.id = PLACEHOLDER_ID;

            this.tips.add(new PowerTip("Mod",this.modID));
        }
        public AbstractSelectButton(String id) {
            this();
            this.id = id;
            if(this.id == null) this.id = PLACEHOLDER_ID;
        }
        public AbstractSelectButton() {
            this.tips = new ArrayList<>();
            this.hb = new Hitbox(BUTTON_W,BUTTON_H);
            this.amount = 0;
            this.x = 0.0f;
            this.y = 0.0f;
        }

        public void update() {
            this.hb.update();
        }

        public void renderIcon(SpriteBatch sb, float a) {

        }
        public void render(SpriteBatch sb) {
            if (this.hb != null) {
                this.hb.render(sb);
                float a = (amount != 0 || this.hb.hovered) ? 1.0f : 0.7f;

                renderIcon(sb, a);

                if (this.hb.hovered) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                    sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x - 150.0f + 128.0f*Settings.scale+ 32.5f*Settings.scale ,y - 50.0f, 150.0F, 50.f, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 220, 220, false, false);
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f*Settings.scale / 2,y + 20.0f*Settings.scale,200.0f*Settings.scale,25.0f*Settings.scale,Settings.GOLD_COLOR);
                    sb.setBlendFunction(770, 771);

                    TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
                } else {
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f*Settings.scale / 2,y + 20.0f*Settings.scale,200.0f*Settings.scale,25.0f*Settings.scale,Settings.CREAM_COLOR);
                }
                if(hasAmount) {
                    if (this.amount > 0) {
                        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.amount), x+30.0f*Settings.scale, y-30.0f*Settings.scale, 3.0f, Settings.GREEN_TEXT_COLOR);
                    } else if (this.amount < 0) {
                        FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.amount), x+30.0f*Settings.scale, y-30.0f*Settings.scale, 3.0f, Settings.RED_TEXT_COLOR);
                    }
                }

            }
        }

    }

    protected static final float SPACE = 80.0F * Settings.scale;
    public static final float START_X = 450.0F * Settings.scale;
    public static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 226.0F * Settings.scale;
    public AbstractCard.CardColor filterColor;

    public String filterMod;

    protected float scrollY = START_Y;
    protected float targetY = this.scrollY;

    protected int row = 0;
    protected int col = 0;

    protected int itemsPerLine = 5;

    protected float scrollLowerBound = Settings.HEIGHT - 200.0F * Settings.scale;
    protected float scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;//2600.0F * Settings.scale;
    protected int scrollTitleCount = 0;

    protected boolean grabbedScreen = false;
    protected float grabStartY = 0.0F;

    public ArrayList<T> items;

    public ArrayList<T> itemsClone;
    public ArrayList<T> selectedItems;

    public T hoveredItem = null;
    protected T clickStartedItem = null;

    protected AbstractSortHeader sortHeader;

    protected ScrollBar scrollBar;
    protected Hitbox controllerRelicHb = null;
    protected boolean show = false;

    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString("RelicViewScreen");
    public static final String[] rTEXT = rUiStrings.TEXT;
    private static final UIStrings pUiStrings = CardCrawlGame.languagePack.getUIString("PotionViewScreen");
    public static final String[] pTEXT = pUiStrings.TEXT;
    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    protected GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    protected boolean doneSelecting = false;

    public enum SortOrder {ASCENDING,DESCENDING};
    public SortOrder currentSortOrder = SortOrder.ASCENDING;
    public enum SortType {TYPE,NAME,MOD,COST,RARITY,CLASS};
    public SortType currentSortType = null;
    public AbstractCustomScreenRelic<T> owner;
    protected boolean isDragSelecting = false;
    protected boolean isTryingToScroll = false;
    public static Color GOLD_OUTLINE_COLOR = new Color(-2686721);
    public static final Color GOLD_BACKGROUND = new Color(-2686721);
    static {
        GOLD_BACKGROUND.a = 0.5f;
    }

    public static int selectMult = 1;
    private final InputAction shiftKey;
    private final InputAction ctrlKey;
    private final InputAction altKey;
    protected SortType defaultSortType;
    protected float itemHeight = 420.0F;

    protected static final Color RED_OUTLINE_COLOR = new Color(-10132568);
    protected static final Color GREEN_OUTLINE_COLOR = new Color(2147418280);
    protected static final Color BLUE_OUTLINE_COLOR = new Color(-2016482392);
    protected static final Color PURPLE_OUTLINE_COLOR = Color.PURPLE;
    protected static final Color BLACK_OUTLINE_COLOR = new Color(168);

    protected static final CharacterStrings redStrings = CardCrawlGame.languagePack.getCharacterString("Ironclad");
    protected static final CharacterStrings greenStrings = CardCrawlGame.languagePack.getCharacterString("Silent");
    protected static final CharacterStrings blueStrings = CardCrawlGame.languagePack.getCharacterString("Defect");
    protected static final CharacterStrings purpleStrings = CardCrawlGame.languagePack.getCharacterString("Watcher");

    protected static final CharacterStrings[] charStrings = {redStrings,greenStrings,blueStrings,purpleStrings};

    protected boolean isFaving = false;
    public boolean filterFavorites = false;
    public boolean filterAll = true;



    public boolean doneSelecting()
    {
        return doneSelecting;
    }

    public AbstractSelectScreen(AbstractCustomScreenRelic<T> owner) {
        scrollBar = new ScrollBar(this);

        this.owner = owner;

        this.items = new ArrayList<>();
        this.itemsClone = new ArrayList<>(this.items);
        this.selectedItems =  new ArrayList<>();

        this.shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);
        this.ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);
        this.altKey = new InputAction(Input.Keys.ALT_LEFT);
    }

    protected void sortOnOpen() {
        if(this.sortHeader.searchBox != null) this.sortHeader.searchBox.resetText();
        this.currentSortType = this.defaultSortType;
        updateFilters();

        this.sortHeader.justSorted = true;

        this.sortHeader.resetAllButtons();
        this.sortHeader.clearActiveButtons();
    }

    protected abstract boolean testFilters(T item);

    public void updateFilters() {
        resetFilters();
        this.items = this.items.stream().filter(this::testFilters).collect(Collectors.toCollection(ArrayList::new));
        sort(true);
        calculateScrollBounds();
    }

    public abstract void sort(boolean isAscending);

    public boolean isOpen()
    {
        return show;
    }

    protected abstract void callOnOpen();

    public void open() {
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
        hideLoadoutRelics();
        callOnOpen();

        sortOnOpen();

        calculateScrollBounds();

        this.selectedItems.clear();
    }
    public static void hideLoadoutRelics() {
        if(AbstractDungeon.isPlayerInDungeon()) AllInOneBag.INSTANCE.hideAllRelics();
    }
    public static void showLoadoutRelics() {
        if(AbstractDungeon.isPlayerInDungeon()) AllInOneBag.INSTANCE.showRelics();
    }

    protected void renderCurrentSelectMult(int mult, SpriteBatch sb){
        if (mult != 1) {
            FontHelper.renderSmartText(sb, FontHelper.topPanelAmountFont, "x" + mult,
                    InputHelper.mX + 5.0F * Settings.scale, InputHelper.mY - 55.0F * Settings.scale, 999.0F, 1.0F, Color.GOLD, Settings.scale);
        }
    }

    protected void updateHotkeyControls() {
        if (this.shiftKey.isPressed() && this.ctrlKey.isPressed()) {
            selectMult = 50;
        } else if (this.shiftKey.isPressed()) {
            selectMult = 10;
        } else if (this.ctrlKey.isPressed()) {
            selectMult = 5;
        } else {
            selectMult = 1;
        }
        isFaving = this.altKey.isPressed();
    }
    protected abstract void updateItemClickLogic();

    protected void preInputUpdateLogic() {}

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
        preInputUpdateLogic();
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
        if ((InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed())&& hoveredItem ==null) {
            isTryingToScroll = true;
        }
        if(InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
            isTryingToScroll = false;
        }

        updateHotkeyControls();

        confirmButton.update();
        this.sortHeader.update();

        if (confirmButton.hb.clicked) {
            CInputActionSet.select.unpress();
            confirmButton.hb.clicked = false;
            doneSelecting = true;
        }

        updateItemClickLogic();

        hoveredItem = null;

        boolean isScrollingScrollBar = scrollBar.update();
        if (!isScrollingScrollBar && !isDragSelecting) {
            updateScrolling();
        }
        updateList(items);
        InputHelper.justClickedLeft = false;
        InputHelper.justClickedRight = false;



        if (Settings.isControllerMode && controllerRelicHb != null) {
            Gdx.input.setCursorPosition((int)controllerRelicHb.cX, (int)(Settings.HEIGHT - controllerRelicHb.cY));
        }
        if(doneSelecting) close();
    }

    private void updateControllerInput()
    {
        // TODO
    }


    protected void updateScrolling()
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

    protected void calculateScrollBounds()
    {
        int size = items.size();

        int scrollTmp = 0;
        if (size > this.itemsPerLine) {
            scrollTmp = (int) Math.ceil( (double) size / this.itemsPerLine);
//            scrollTmp += this.itemsPerLine;
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * itemHeight;
        } else {
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;
        }
    }

    public void resetFilters() {
        this.items.clear();
        this.items.addAll(this.itemsClone);
    }

    private void resetScrolling()
    {
        if (targetY < scrollLowerBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollLowerBound);
        } else if (targetY > scrollUpperBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollUpperBound);
        }
    }

    protected abstract void updateList(ArrayList<T> list);

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        show = false;
        if(owner != null)owner.setIsSelectionScreenUp(false);
        showLoadoutRelics();
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

        renderCurrentSelectMult(selectMult, sb);

    }

    protected abstract void renderList(SpriteBatch sb, ArrayList<T> list);

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

    protected boolean shouldSortById() {
        return Settings.language == Settings.GameLanguage.ZHS || Settings.language == Settings.GameLanguage.ZHT;
    }

    public ArrayList<T> getSelectedItems() {
        ArrayList<T> ret = new ArrayList<>(selectedItems);
        selectedItems.clear();
        return ret;
    }

    public ArrayList<T> getList() {
        if(this.itemsClone == null || this.itemsClone.isEmpty()) {
            this.callOnOpen();
        }
        return this.itemsClone;
    }
}
