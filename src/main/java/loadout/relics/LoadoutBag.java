package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.RelicSelectScreen;
import loadout.util.TextureLoader;

import java.util.*;


import static loadout.LoadoutMod.*;

/**
 * Code from Hubris Mod's Backtick Relic, slightly modified
 */
public class LoadoutBag extends AbstractCustomScreenRelic<AbstractRelic> {

    // ID, images, text.
    public static final boolean isIsaacMode = enableIsaacIcons || Loader.isModLoadedOrSideloaded("IsaacMod")||Loader.isModLoadedOrSideloaded("IsaacModExtend");
    public static final String ID = LoadoutMod.makeID("LoadoutBag");
    public static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("loadout_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("loadout_relic.png"));
    public static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("loadout_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("loadout_relic.png"));

    public static final ArrayList<AbstractRelic> lastRelics = new ArrayList<>();
    public LoadoutBag() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
    }


    @Override
    protected AbstractSelectScreen<AbstractRelic> getNewSelectScreen() {
        return new RelicSelectScreen(false, this);
    }

    @Override
    protected void doneSelectionLogics() {
        ArrayList<AbstractRelic> relics = selectScreen.getSelectedItems();
        lastRelics.clear();
        for (AbstractRelic r : relics) {
            for(int i = 0; i< relicObtainMultiplier; i++) {
                relicsToAdd.add(r.makeCopy());
                lastRelics.add(r.makeCopy());
                r.playLandingSFX();
                this.flash();
            }
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new LoadoutBag();
    }

    @Override
    public void onCtrlRightClick() {
        if(!lastRelics.isEmpty()) {
            lastRelics.forEach(r -> relicsToAdd.add(r.makeCopy()));
        }
    }
}
