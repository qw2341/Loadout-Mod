package loadout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import loadout.LoadoutMod;

import java.util.ArrayList;

public abstract class SelectScreen<T> implements ScrollBarListener {



    protected static final float SPACE = 80.0F * Settings.scale;
    protected static final float START_X = 450.0F * Settings.scale;
    protected static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 226.0F * Settings.scale;

    protected float scrollY = START_Y;
    protected float targetY = this.scrollY;

    protected int row = 0;
    protected int col = 0;

    protected float scrollLowerBound = Settings.HEIGHT - 200.0F * Settings.scale;
    protected float scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;//2600.0F * Settings.scale;
    protected int scrollTitleCount = 0;

    protected boolean grabbedScreen = false;
    protected float grabStartY = 0.0F;

    public ArrayList<T> items;

    public ArrayList<T> itemsClone;

    public T hoveredItem = null;

    protected SortHeader sortHeader;

    protected ScrollBar scrollBar;
    protected Hitbox controllerRelicHb = null;
    protected boolean show = false;

    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    protected GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    private boolean doneSelecting = false;

    public enum SortOrder {ASCENDING,DESCENDING};
    public EventSelectScreen.SortOrder currentSortOrder = EventSelectScreen.SortOrder.ASCENDING;

    public AbstractRelic owner;
    protected boolean isDragSelecting = false;
    protected boolean isTryingToScroll = false;




    public boolean doneSelecting()
    {
        return doneSelecting;
    }

    public SelectScreen(AbstractRelic owner) {
        scrollBar = new ScrollBar(this);

        this.owner = owner;

    }

    protected abstract void sortOnOpen();

    public abstract void updateFilters();

    public abstract void sort(boolean isAscending);

    public boolean isOpen()
    {
        return show;
    }

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

        this.currentSortOrder = EventSelectScreen.SortOrder.ASCENDING;

        sortOnOpen();

        calculateScrollBounds();
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

    protected void calculateScrollBounds()
    {
        int size = items.size();

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

    protected abstract void updateList(ArrayList<T> list);

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        show = false;

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

    protected abstract void renderList(SpriteBatch sb, ArrayList<?> list);

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
