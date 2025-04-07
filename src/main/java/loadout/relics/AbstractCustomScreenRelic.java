package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.screens.AbstractSelectScreen;
import java.util.HashMap;


public abstract class AbstractCustomScreenRelic<T> extends LoadoutRelic {


    public AbstractSelectScreen<T> selectScreen;

    public static HashMap<String, Boolean> isScreenUpMap = new HashMap<>();


    public AbstractCustomScreenRelic(String id, Texture texture, Texture outline, AbstractRelic.LandingSound sfx) {
        super(id, texture, outline, sfx);

        isScreenUpMap.put(this.getClass().getSimpleName(), Boolean.FALSE);
    }

    protected void openSelectScreen() {
        this.itemSelected = false;
        setIsSelectionScreenUp(true);

        if (selectScreen == null) this.selectScreen = getNewSelectScreen();
        if(selectScreen != null) this.selectScreen.open();
    }

    protected abstract AbstractSelectScreen<T> getNewSelectScreen();

    public void updateSelectScreen() {
        if(selectScreen != null) {
            if(isSelectionScreenUp()) selectScreen.update();

            if (!itemSelected) {
                if (selectScreen.doneSelecting()) {
                    itemSelected = true;
                    setIsSelectionScreenUp(false);
                    doneSelectionLogics();
                }
            }
        }
    }


    @Override
    public void renderInTopPanel(SpriteBatch sb) {
        render(sb);
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

    public static void setIsSelectionScreenUp(Class<? extends AbstractCustomScreenRelic<?>> caller, boolean bool) {
        isScreenUpMap.put(caller.getSimpleName(), bool);
    }
}
