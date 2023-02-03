package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.defect.ChannelAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.OrbSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.makeRelicOutlinePath;
import static loadout.LoadoutMod.makeRelicPath;

public class OrbBox extends AbstractCustomScreenRelics{

    public static final String ID = LoadoutMod.makeID("OrbBox");
    public static final Texture IMG = TextureLoader.getTexture(makeRelicPath("loadout_relic.png"));
    private static final Texture OUTLINE = TextureLoader.getTexture(makeRelicOutlinePath("loadout_relic.png"));


    public OrbBox() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public boolean isOtherRelicScreenOpen() {
        return LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || BottledMonster.isSelectionScreenUp;
    }

    @Override
    protected AbstractSelectScreen<OrbSelectScreen.OrbButton> getNewSelectScreen() {
        return new OrbSelectScreen(this);
    }

    public void channelOrb(AbstractOrb orb, int amount) {
        for (int i = 0; i < amount; i++) addToBot(new ChannelAction(orb));
    }

    public void removeOrb(AbstractOrb orb, int amount){

    }
}
