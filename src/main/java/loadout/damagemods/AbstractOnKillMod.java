package loadout.damagemods;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

import loadout.patches.AbstractCardPatch;

/**
 * Some inspirations from chimera cards mod
 */
public abstract class AbstractOnKillMod extends AbstractLoadoutDamageMod {

    protected String cardmodID;

    public AbstractOnKillMod(String cardmodID) {
        this.priority = Short.MAX_VALUE;
        this.cardmodID = cardmodID;
    }

    @Override
    public boolean isInherent() {
        return true;
    }


    @Override
    public void onLastDamageTakenUpdate(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target) {
        if(isValidKill(info,lastDamageTaken, target)) {
            onKill(info, lastDamageTaken, overkillAmount, target, AbstractCardPatch.getMagicNumber(getCard(info), cardmodID));
        }
    }

    public abstract void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount);

}
