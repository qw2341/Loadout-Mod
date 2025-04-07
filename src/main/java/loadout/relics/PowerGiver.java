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
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.PowerSelectScreen;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static loadout.LoadoutMod.*;

/**
 * TODO: Unfinished
 */
public class PowerGiver extends AbstractCustomScreenRelic<PowerSelectScreen.PowerButton> implements CustomSavable<HashMap<String,Integer>[]> {

    public static class PowerAction {
        public PowerTarget target;
        public String id;
        public int amount;
        public PowerAction(PowerTarget target, String id, int amount) {
            this.target = target;
            this.id = id;
            this.amount = amount;
        }

        public void execute(PowerGiver powerGiver) {
            switch (target) {
                case PLAYER:
                    powerGiver.modifyAmountPlayer(id, amount);
                    if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        powerGiver.applyPowerToPlayer(id, amount);
                    }
                    break;
                case MONSTER:
                    powerGiver.modifyAmountMonster(id, amount);
                    MonsterGroup monsterGroup = AbstractDungeon.getMonsters();
                    if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && monsterGroup!=null) {
                        for (AbstractMonster monster: monsterGroup.monsters)
                            powerGiver.applyPowerToMonster(id, amount, monster);
                    }
                    break;
            }
        }
    }

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("PowerGiver");
    private static Texture IMG = null;
    private static Texture OUTLINE = null;

    public HashMap<String, Integer> savedPowersPlayer;

    public HashMap<String, Integer> savedPowersMonster;

    private static final AbstractCard placeholderCard = new Madness();

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

    public static final ArrayList<PowerAction> lastPowers = new ArrayList<>();
    public PowerGiver() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.FLAT);
        if (savedPowersPlayer == null) {
            savedPowersPlayer = new HashMap<>();
        }
        if (savedPowersMonster == null) {
            savedPowersMonster = new HashMap<>();
        }
    }

    public void openSingle(AbstractCreature abstractCreature) {
        this.itemSelected = false;
        setIsSelectionScreenUp(true);
        if(this.selectScreen == null) this.selectScreen = getNewSelectScreen();
        ((PowerSelectScreen) this.selectScreen).openSingle(abstractCreature);
    }

    @Override
    protected AbstractSelectScreen<PowerSelectScreen.PowerButton> getNewSelectScreen() {
        return new PowerSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

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

    public static AbstractPower getPower(String pID, int amount, AbstractCreature creature, AbstractCard card) {
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

    public static void applyPowerToMonster(String id, int amount, AbstractCreature monster) {
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

    public void onSpawnMonster(AbstractMonster monster) {
        savedPowersMonster.keySet().forEach(id -> {
            int amount = savedPowersMonster.get(id);
            applyPowerToMonster(id,amount,monster);
        });
    }

    @Override
    public void onCtrlRightClick() {
        if(!lastPowers.isEmpty()) {
            flash();
            lastPowers.forEach(p -> p.execute(this));
        }
    }

    public void resetPowers() {
        savedPowersPlayer.clear();
        savedPowersMonster.clear();
    }
}
