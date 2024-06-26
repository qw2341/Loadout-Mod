package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.RelicSelectScreen;
import loadout.util.ModConfig;

import java.util.*;


import static loadout.LoadoutMod.*;

/**
 * Code from Hubris Mod's Backtick Relic, slightly modified
 */
public class LoadoutBag extends AbstractCustomScreenRelic<AbstractRelic> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("LoadoutBag");
    public static Texture IMG = null;
    public static Texture OUTLINE = null;

    public static final ArrayList<AbstractRelic> lastRelics = new ArrayList<>();
    public LoadoutBag() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.FLAT);
    }


    @Override
    protected AbstractSelectScreen<AbstractRelic> getNewSelectScreen() {
        return new RelicSelectScreen(false, this);
    }

    @Override
    protected void doneSelectionLogics() {
        ArrayList<AbstractRelic> relics = selectScreen.getSelectedItems();
        if(!relics.isEmpty()) {
            lastRelics.clear();
            for (AbstractRelic r : relics) {
                for(int i = 0; i< ModConfig.relicObtainMultiplier; i++) {
                    relicsToAdd.add(r.makeCopy());
                    lastRelics.add(r.makeCopy());
                    r.playLandingSFX();
                    this.flash();
                }
            }
        }

    }

    @Override
    public void onCtrlRightClick() {
        if(!lastRelics.isEmpty()) {
            flash();
            lastRelics.forEach(r -> relicsToAdd.add(r.makeCopy()));
        }
    }
}
