package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.screens.GCardSelectScreen;
import java.util.HashMap;

public abstract class AbstractCardScreenRelic extends LoadoutRelic {

    public GCardSelectScreen selectScreen;

    public static HashMap<String, Boolean> isScreenUpMap = new HashMap<>();

    public GCardSelectScreen.CardDisplayMode displayMode;


    public AbstractCardScreenRelic(String id, Texture texture, Texture outline, AbstractRelic.LandingSound sfx, GCardSelectScreen.CardDisplayMode displayMode) {
        super(id,texture,outline,sfx);

        this.displayMode = displayMode;
        isScreenUpMap.put(this.getClass().getSimpleName(), Boolean.FALSE);
        this.selectScreen = new GCardSelectScreen(displayMode, this);
    }

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (isOtherRelicScreenOpen()) {
            AllInOneBag.INSTANCE.closeAllScreens();
        }


        if(this.ctrlKey.isPressed()) {
            onCtrlRightClick();
            return;
        }
        if(this.shiftKey.isPressed()) {
            onShiftRightClick();
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        this.itemSelected = false;
        setIsSelectionScreenUp(true);
        openSelectScreen();
    }

    @Override
    public void updateSelectScreen() {
        if(isSelectionScreenUp()) {
            selectScreen.update();
        }

        if (selectScreen != null) {
            if (selectScreen.doneSelecting) {
                itemSelected = true;
                setIsSelectionScreenUp(false);
                doneSelectionLogics();
                selectScreen.doneSelecting = false;
            }
        }
    }

    public void renderInTopPanel(SpriteBatch sb)
    {

        this.render(sb);

        if (isSelectionScreenUp()) {
            selectScreen.render(sb);
        }
    }


    public boolean isSelectionScreenUp() {
        return isScreenUpMap.get(this.getClass().getSimpleName());
    }

    public static boolean isSelectionScreenUp(Class<? extends AbstractCustomScreenRelic<?>> caller) {
        return isScreenUpMap.get(caller.getSimpleName());
    }

    public void setIsSelectionScreenUp(boolean bool) {
        //logger.info("Setting isScreenUp for " + this.getClass().getSimpleName() + " to " + bool);
        isScreenUpMap.put(this.getClass().getSimpleName(), bool);
    }
}
