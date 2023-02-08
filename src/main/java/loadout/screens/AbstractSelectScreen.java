package loadout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import loadout.LoadoutMod;
import loadout.relics.AllInOneBag;

import java.util.ArrayList;
import java.util.stream.Collectors;

public abstract class AbstractSelectScreen<T> implements ScrollBarListener {



    protected static final float SPACE = 80.0F * Settings.scale;
    protected static final float START_X = 450.0F * Settings.scale;
    protected static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

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
    public AbstractRelic owner;
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

    public AbstractSelectScreen(AbstractRelic owner) {
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
        if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.player.hasRelic(AllInOneBag.ID)) ((AllInOneBag)AbstractDungeon.player.getRelic(AllInOneBag.ID)).hideAllRelics();
    }
    public static void showLoadoutRelics() {
        if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.player.hasRelic(AllInOneBag.ID)) ((AllInOneBag)AbstractDungeon.player.getRelic(AllInOneBag.ID)).showRelics();
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
            scrollTmp = size / this.itemsPerLine;
            scrollTmp += this.itemsPerLine;
            if (size % this.itemsPerLine != 0) {
                ++scrollTmp;
            }
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * itemHeight * Settings.scale;
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
}
