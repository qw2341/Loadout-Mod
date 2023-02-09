package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.RelicSelectScreen;
import loadout.util.TextureLoader;

import java.util.*;

import static loadout.LoadoutMod.*;

public class TrashBin extends AbstractCustomScreenRelic<AbstractRelic> {

    public static final String ID = LoadoutMod.makeID("TheBin");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("thebin_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic.png"));

    public TrashBin() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
    }

    @Override
    protected AbstractSelectScreen<AbstractRelic> getNewSelectScreen() {
        return new RelicSelectScreen(true, this);
    }

    @Override
    protected void doneSelectionLogics() {
        HashSet<Integer> idxToRemove = ((RelicSelectScreen)selectScreen).getRemovingRelics();
        if (!idxToRemove.isEmpty()) {
            relicsToRemove.addAll(idxToRemove);
            this.flash();
        }
    }
    @Override
    protected void openSelectScreen() {

        ArrayList<AbstractRelic> playerRelics = AbstractDungeon.player.relics;

        ArrayList<AbstractRelic> relics = new ArrayList<>();

        playerRelics.forEach((r)->relics.add(r.makeCopy()));
        try {
            if (this.selectScreen == null)
                selectScreen = getNewSelectScreen();
        } catch (NoClassDefFoundError e) {
            logger.info("Error: RelicSelectScreen Class not found while opening relic select for bin!");
        }
        if (this.selectScreen != null) {
            itemSelected = false;
            isScreenUpMap.put(TrashBin.class.getSimpleName(), true);
            ((RelicSelectScreen) selectScreen).open(relics, 1);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new TrashBin();
    }
}
