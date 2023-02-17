package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.RelicSelectScreen;
import loadout.util.TextureLoader;

import java.util.*;

import static loadout.LoadoutMod.*;

public class TrashBin extends AbstractCustomScreenRelic<AbstractRelic> {

    public static final String ID = LoadoutMod.makeID("TheBin");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("thebin_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic.png"));
    private static final Texture IMG_XGGG_ALT = TextureLoader.getTexture(makeRelicPath("thebin_relic_xggg.png"));
    public static int numLoadoutRelics = 0;
    public static int loadoutRelicsStartIdx = -1;
    public TrashBin() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
        if (LoadoutMod.isXggg()) {
            this.img = IMG_XGGG_ALT;
        }
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

    public static ArrayList<AbstractRelic> getPlayerRelicCopy() {
        numLoadoutRelics = 0;
        ArrayList<AbstractRelic> playerRelics = AbstractDungeon.player.relics;
        ArrayList<AbstractRelic> relics = new ArrayList<>();
        int idx = 0;
        loadoutRelicsStartIdx = -1;
        for (AbstractRelic r: playerRelics) {
            if(!LoadoutRelicHelper.loadoutRelicIds.contains(r.relicId)) relics.add(r.makeCopy());
            else {
                if(loadoutRelicsStartIdx == -1) loadoutRelicsStartIdx = idx;
                numLoadoutRelics++;
            }
            idx++;
        }

        return relics;
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new TrashBin();
    }
}
