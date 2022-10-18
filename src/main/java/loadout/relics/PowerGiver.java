package loadout.relics;

import basemod.BaseMod;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.PotionSelectScreen;
import loadout.screens.PowerSelectScreen;
import loadout.util.TextureLoader;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;

/**
 * TODO: Unfinished
 */
public class PowerGiver extends CustomRelic implements ClickableRelic, CustomSavable<HashMap<String,Integer>> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("PowerGiver");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("powergiver_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("powergiver_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("powergiver_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("powergiver_relic.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);
    private boolean powerSelected = true;
    public PowerSelectScreen powerSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;

    public HashMap<String, Integer> savedPowers;



    public PowerGiver() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);

        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (savedPowers == null) {
            savedPowers = new HashMap<>();
        }
    }

    @Override
    public String getUpdatedDescription()
    {
        return DESCRIPTIONS[0];
    }

    @Override
    public void relicTip() {

    }
    @Override
    public void onUnequip() {
        if(isSelectionScreenUp) {
            if(powerSelectScreen!=null) {
                isSelectionScreenUp = false;
                powerSelectScreen.close();
            }
        }
    }
    @Override
    public void onRightClick() {
        if (!isObtained||AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(powerSelectScreen!=null) {
                isSelectionScreenUp = false;
                powerSelectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openPowerSelect();
    }



    private void openPowerSelect()
    {
        powerSelected = false;
        isSelectionScreenUp = true;
        try {
            if (this.powerSelectScreen == null) powerSelectScreen = new PowerSelectScreen(this);
        } catch (NoClassDefFoundError e) {
        logger.info("Error: PotionSelectScreen Class not found while opening potion select for cauldron!");
    }
        if (this.powerSelectScreen != null) powerSelectScreen.open(savedPowers);
    }

    @Override
    public void update()
    {
        super.update();

        if (!powerSelected && powerSelectScreen != null) {
            if (powerSelectScreen.doneSelecting()) {
                powerSelected = true;
                isSelectionScreenUp = false;
            } else {
                powerSelectScreen.update();
                if (!hb.hovered) {
                    fakeHover = true;
                }
                hb.hovered = true;
            }
        }
    }

    @Override
    public void renderTip(SpriteBatch sb)
    {
        if (!powerSelected && fakeHover) {
            powerSelectScreen.render(sb);
        }
        if (fakeHover) {
            fakeHover = false;
            hb.hovered = false;
        } else {
            super.renderTip(sb);
        }
    }

    @Override
    public void renderInTopPanel(SpriteBatch sb)
    {
        super.renderInTopPanel(sb);

        if (!powerSelected && !fakeHover) {
            powerSelectScreen.render(sb);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new PowerGiver();
    }

    @Override
    public void playLandingSFX() {
        if (isIsaacMode) {
            if (CardCrawlGame.MUTE_IF_BG && Settings.isBackgrounded) {
                return;
            } else if (landingSfx != null) {
                landingSfx.play(Settings.SOUND_VOLUME * Settings.MASTER_VOLUME);
            } else {
                logger.info("Missing landing sound!");
            }
        } else {
            CardCrawlGame.sound.play("RELIC_DROP_HEAVY");
        }
    }

    public void modifyAmount(String pID, int modAmt) {
        if (savedPowers.containsKey(pID)) {
            int result = savedPowers.get(pID) + modAmt;
            if (result != 0)
                savedPowers.put(pID, result);
            else
                savedPowers.remove(pID);
        } else {
            savedPowers.put(pID, modAmt);
        }
    }


    @Override
    public HashMap<String, Integer> onSave() {
        return savedPowers;
    }

    @Override
    public void onLoad(HashMap<String, Integer> savedPowers) {
        this.savedPowers = savedPowers;
    }

    @Override
    public void atBattleStart() {
        savedPowers.keySet().forEach(id -> {
            Class<? extends AbstractPower> powerClassToApply = BaseMod.getPowerClass(id);
            AbstractPower powerToApply;
            int amount = savedPowers.get(id);
            try {
                Constructor<?>[] con = powerClassToApply.getDeclaredConstructors();
                int paramCt = con[0].getParameterCount();
                Class[] params = con[0].getParameterTypes();
                Object[] paramz = new Object[paramCt];

                for (int i = 0 ; i< paramCt; i++) {
                    Class param = params[i];
                    if (AbstractCreature.class.isAssignableFrom(param)) {
                        paramz[i] = AbstractDungeon.player;
                    } else if (int.class.isAssignableFrom(param)) {
                        paramz[i] = amount;
                    } else if (AbstractCard.class.isAssignableFrom(param)) {
                        paramz[i] = new Madness();
                    } else if (boolean.class.isAssignableFrom(param)) {
                        paramz[i] = true;
                    }
                }
                //LoadoutMod.logger.info("Class: " + pClass.getName() + " with parameter: " + Arrays.toString(paramz));

                powerToApply = (AbstractPower) con[0].newInstance(paramz);

                AbstractDungeon.actionManager.addToTop(new ApplyPowerAction((AbstractCreature)AbstractDungeon.player, (AbstractCreature)AbstractDungeon.player, (AbstractPower) powerToApply, amount));

            } catch (Exception e) {
                logger.info("Failed to get player power: " + id);
                e.printStackTrace();
            }

        });
    }
}
