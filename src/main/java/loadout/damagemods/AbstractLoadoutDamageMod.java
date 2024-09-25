package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.RegrowPower;
import loadout.patches.AbstractCardPatch;
import loadout.util.Wiz;

public abstract class AbstractLoadoutDamageMod extends AbstractDamageModifier {

    protected boolean isValidKill(DamageInfo info, int lastDamageTaken, AbstractCreature targetHit) {
        return DamageModifierManager.getInstigator(info) instanceof com.megacrit.cardcrawl.cards.AbstractCard &&
                targetHit.currentHealth > 0 && targetHit.currentHealth - lastDamageTaken <= 0 && !targetHit.halfDead && !targetHit.hasPower("Minion") && isOnlyLifeLinkLeft(targetHit);
    }

    protected boolean isOnlyLifeLinkLeft(AbstractCreature targetHit) {
        if(!targetHit.hasPower(RegrowPower.POWER_ID)) return true;

        final boolean[] ret = {true};
        Wiz.getEnemies().forEach((m)-> {
            if(m != targetHit && !m.halfDead) {
                if (m.hasPower(RegrowPower.POWER_ID)) ret[0] = false;
            }
        });
        return ret[0];
    }

    protected boolean isValid(DamageInfo info, AbstractCreature targetHit) {
        return DamageModifierManager.getInstigator(info) instanceof com.megacrit.cardcrawl.cards.AbstractCard &&
                targetHit.currentHealth > 0 && !targetHit.halfDead;
    }

    protected int getAmount(DamageInfo info, String numberModID) {
        Object instigator = DamageModifierManager.getInstigator(info);
        if(instigator instanceof com.megacrit.cardcrawl.cards.AbstractCard) {
            return AbstractCardPatch.getMagicNumber((AbstractCard) instigator, numberModID);
        }
        return 0;
    }

    protected AbstractCard getCard(DamageInfo info) {
        Object instigator = DamageModifierManager.getInstigator(info);
        return instigator instanceof com.megacrit.cardcrawl.cards.AbstractCard ? (AbstractCard) instigator : new Madness();
    }

}
