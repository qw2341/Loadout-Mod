package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.OrbSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.makeRelicOutlinePath;
import static loadout.LoadoutMod.makeRelicPath;

public class OrbBox extends AbstractCustomScreenRelic<OrbSelectScreen.OrbButton> {

    public static final String ID = LoadoutMod.makeID("OrbBox");
    private static final String RESDIR = "orb_box_relic" + (isIsaacMode ? "_alt" : "") + ".png";
    public static final Texture IMG = TextureLoader.getTexture(makeRelicPath(RESDIR));
    private static final Texture OUTLINE = TextureLoader.getTexture(makeRelicOutlinePath(RESDIR));
    private static final String XGGG_NAME = "";
    private static final Texture IMG_XGGG_ALT = TextureLoader.getTexture(makeRelicPath("orb_box_relic_xggg.png"));


    public OrbBox() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.MAGICAL);
        if (LoadoutMod.isXggg()) {
            this.img = IMG_XGGG_ALT;
        }
    }

    @Override
    protected AbstractSelectScreen<OrbSelectScreen.OrbButton> getNewSelectScreen() {
        return new OrbSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    public void channelOrb(AbstractOrb orb, int amount) {
        for (int i = 0; i < amount; i++) addToBot(new ChannelAction(orb));
    }

    public void removeOrb(AbstractOrb orb, int amount){

    }
}
