package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.PotionSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;

/**
 * Code from Hubris Mod's Backtick Relic, slightly modified
 */
public class LoadoutCauldron extends AbstractCustomScreenRelic<AbstractPotion> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("LoadoutCauldron");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("cauldron_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("cauldron_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("cauldron_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("cauldron_relic.png"));

    public static AbstractPotion lastPotion = null;

    public LoadoutCauldron() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
    }

    @Override
    protected AbstractSelectScreen<AbstractPotion> getNewSelectScreen() {
        return new PotionSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new LoadoutCauldron();
    }

    @Override
    public void onCtrlRightClick() {
        if (lastPotion != null) {
            flash();
            AbstractDungeon.player.obtainPotion(lastPotion.makeCopy());
        }
    }
}
