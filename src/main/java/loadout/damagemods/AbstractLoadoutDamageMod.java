package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;

public abstract class AbstractLoadoutDamageMod extends AbstractDamageModifier {

    protected boolean isValidKill(DamageInfo info, int lastDamageTaken, AbstractCreature targetHit) {
        return DamageModifierManager.getInstigator(info) instanceof com.megacrit.cardcrawl.cards.AbstractCard &&
                targetHit.currentHealth > 0 && targetHit.currentHealth - lastDamageTaken <= 0 && !targetHit.halfDead && !targetHit.hasPower("Minion");
    }

    protected boolean isValid(DamageInfo info, AbstractCreature targetHit) {
        return DamageModifierManager.getInstigator(info) instanceof com.megacrit.cardcrawl.cards.AbstractCard &&
                targetHit.currentHealth > 0 && !targetHit.halfDead;
    }

    protected int getMiscAmount(DamageInfo info) {
        Object instigator = DamageModifierManager.getInstigator(info);
        if(instigator instanceof com.megacrit.cardcrawl.cards.AbstractCard) {
            return ((AbstractCard) instigator).misc;
        }
        return 0;
    }

    protected AbstractCard getCard(DamageInfo info) {
        Object instigator = DamageModifierManager.getInstigator(info);
        return instigator instanceof com.megacrit.cardcrawl.cards.AbstractCard ? (AbstractCard) instigator : new Madness();
    }

}
