package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.OrbSelectScreen;
import loadout.util.TextureLoader;


public class OrbBox extends AbstractCustomScreenRelic<OrbSelectScreen.OrbButton> {

    public static final String ID = LoadoutMod.makeID("OrbBox");

    public static Texture IMG = null;
    private static Texture OUTLINE = null;
    private static final String XGGG_NAME = "";


    public OrbBox() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.MAGICAL);
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
