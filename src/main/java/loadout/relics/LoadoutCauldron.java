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
    private static Texture IMG = null;
    private static Texture OUTLINE = null;

    public static AbstractPotion lastPotion = null;

    public LoadoutCauldron() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.FLAT);
    }

    @Override
    protected AbstractSelectScreen<AbstractPotion> getNewSelectScreen() {
        return new PotionSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public void onCtrlRightClick() {
        if (lastPotion != null) {
            flash();
            AbstractDungeon.player.obtainPotion(lastPotion.makeCopy());
        }
    }
}
