package loadout.relics;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.PowerSelectScreen;
import loadout.util.TextureLoader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;

import static loadout.LoadoutMod.*;

/**
 * TODO: Unfinished
 */
public class PowerGiver extends AbstractCustomScreenRelic<PowerSelectScreen.PowerButton> implements CustomSavable<HashMap<String,Integer>[]> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("PowerGiver");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("powergiver_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("powergiver_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("powergiver_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("powergiver_relic.png"));

    public HashMap<String, Integer> savedPowersPlayer;

    public HashMap<String, Integer> savedPowersMonster;

    private final AbstractCard placeholderCard = new Madness();

    public enum PowerTarget {
        PLAYER, MONSTER
    }

    public static HashSet<String> buggedPowers;
    static {
        buggedPowers = new HashSet<>();
        buggedPowers.add(WeakPower.POWER_ID);
        buggedPowers.add(FrailPower.POWER_ID);
        buggedPowers.add(VulnerablePower.POWER_ID);
    }

    public PowerGiver() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
        if (savedPowersPlayer == null) {
            savedPowersPlayer = new HashMap<>();
        }
        if (savedPowersMonster == null) {
            savedPowersMonster = new HashMap<>();
        }
    }



    @Override
    protected AbstractSelectScreen<PowerSelectScreen.PowerButton> getNewSelectScreen() {
        return new PowerSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new PowerGiver();
    }

    public void modifyAmountPlayer(String pID, int modAmt) {
        if (savedPowersPlayer.containsKey(pID)) {
            int result = savedPowersPlayer.get(pID) + modAmt;
            if (result != 0)
                savedPowersPlayer.put(pID, result);
            else
                savedPowersPlayer.remove(pID);
        } else {
            savedPowersPlayer.put(pID, modAmt);
        }
    }
    public void modifyAmountMonster(String pID, int modAmt) {
        if (savedPowersMonster.containsKey(pID)) {
            int result = savedPowersMonster.get(pID) + modAmt;
            if (result != 0)
                savedPowersMonster.put(pID, result);
            else
                savedPowersMonster.remove(pID);
        } else {
            savedPowersMonster.put(pID, modAmt);
        }
    }


    @Override
    public HashMap<String, Integer>[] onSave() {
        HashMap<String, Integer>[] ret = new HashMap[2];
        ret[0] = savedPowersPlayer;
        ret[1] = savedPowersMonster;

        return ret;
    }

    @Override
    public void onLoad(HashMap<String, Integer>[] savedPowers) {
        if(savedPowers == null || savedPowers.length != 2) {
            this.savedPowersPlayer = new HashMap<>();
            this.savedPowersMonster = new HashMap<>();
            return;
        }
        this.savedPowersPlayer = savedPowers[0];
        this.savedPowersMonster = savedPowers[1];
    }

    public AbstractPower getPower(String pID, int amount, AbstractCreature creature, AbstractCard card) {
        Class<? extends AbstractPower> powerClassToApply = powersToDisplay.get(pID);
        AbstractPower powerToApply = new StrengthPower(PowerSelectScreen.dummyPlayer,0);

        try {
            if (PowerSelectScreen.specialCases.contains(pID)) {
                switch (pID) {
                    case "TheBomb":
                        return new TheBombPower(creature,amount,40);
                }
            } else {
                Constructor<?>[] con = powerClassToApply.getDeclaredConstructors();
                int paramCt = con[0].getParameterCount();
                Class[] params = con[0].getParameterTypes();
                Object[] paramz = new Object[paramCt];

                for (int i = 0 ; i< paramCt; i++) {
                    Class param = params[i];
                    if (AbstractCreature.class.isAssignableFrom(param)) {
                        paramz[i] = creature;
                    } else if (int.class.isAssignableFrom(param)) {
                        paramz[i] = amount;
                    } else if (AbstractCard.class.isAssignableFrom(param)) {
                        paramz[i] = card;
                    } else if (boolean.class.isAssignableFrom(param)) {
                        paramz[i] = true;
                    }
                }
                //LoadoutMod.logger.info("Class: " + pClass.getName() + " with parameter: " + Arrays.toString(paramz));

                powerToApply = (AbstractPower) con[0].newInstance(paramz);
            }

            if(powerToApply.ID != null && buggedPowers.contains(powerToApply.ID) ) {
                ReflectionHacks.setPrivate(powerToApply,powerClassToApply,"justApplied",false);
            }

            return powerToApply;

        } catch (Exception e) {
            logger.info("Failed to get player power: " + pID);
            e.printStackTrace();
        }
        return powerToApply;
    }

    public void applyPowerToPlayer(String id, int amount) {
        AbstractDungeon.actionManager.addToTop(new ApplyPowerAction((AbstractCreature)AbstractDungeon.player, (AbstractCreature)AbstractDungeon.player, getPower(id, amount, AbstractDungeon.player, placeholderCard), amount));

    }

    public void applyPowerToMonster(String id, int amount, AbstractCreature monster) {
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(monster,monster, getPower(id, amount, monster, placeholderCard), amount));
    }


    public void battleStartPreDraw() {
        savedPowersPlayer.keySet().forEach(id -> {
            int amount = savedPowersPlayer.get(id);
            applyPowerToPlayer(id, amount);
        });

        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            savedPowersMonster.keySet().forEach(id -> {
                int amount = savedPowersMonster.get(id);
                applyPowerToMonster(id,amount,monster);
            });
        }
    }

    @Override
    public void onSpawnMonster(AbstractMonster monster) {
        savedPowersMonster.keySet().forEach(id -> {
            int amount = savedPowersMonster.get(id);
            applyPowerToMonster(id,amount,monster);
        });
    }


}
