package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.MonsterSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;

public class BottledMonster extends AbstractCustomScreenRelic<MonsterSelectScreen.MonsterButton> {
    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("BottledMonster");
    private static final Texture IMG =  (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("bottle_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("bottle_relic.png"));

    private static final Texture IMG_XGGG_ALT = TextureLoader.getTexture(makeRelicPath("bottle_relic_xggg.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic.png"));

    private static final String XGGG_NAME = "瓶装星光";


    public BottledMonster() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);

        if (LoadoutMod.isXggg()) {
            this.img = IMG_XGGG_ALT;
            this.tips.get(0).header = XGGG_NAME;
        }
    }

    @Override
    protected AbstractSelectScreen<MonsterSelectScreen.MonsterButton> getNewSelectScreen() {
        return new MonsterSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }
    @Override
    public AbstractRelic makeCopy()
    {
        return new BottledMonster();
    }
}
