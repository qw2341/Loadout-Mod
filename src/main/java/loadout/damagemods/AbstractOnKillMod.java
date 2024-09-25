package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.AbstractCreature;

import java.util.function.Supplier;

/**
 * Some inspirations from chimera cards mod
 */
public abstract class AbstractOnKillMod extends AbstractLoadoutDamageMod {

    protected Supplier<Integer> getValue;
    public AbstractOnKillMod(Supplier<Integer> getValue) {
        this.priority = Short.MAX_VALUE;
        this.getValue = getValue;
    }

    @Override
    public boolean isInherent() {
        return true;
    }


    @Override
    public void onLastDamageTakenUpdate(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target) {
        if(isValidKill(info,lastDamageTaken, target)) {
            onKill(info, lastDamageTaken, overkillAmount, target, getValue.get());
        }
    }

    public abstract void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount);

}
