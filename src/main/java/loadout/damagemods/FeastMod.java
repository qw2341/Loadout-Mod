package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import loadout.util.Wiz;

import java.util.function.Supplier;

public class FeastMod extends AbstractOnKillMod {

    public FeastMod(Supplier<Integer> getValue) {
        super(getValue);
    }

    @Override
    public void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount) {
        Wiz.adp().increaseMaxHp(amount, false);
    }

    @Override
    public AbstractDamageModifier makeCopy() {
        return new FeastMod(getValue);
    }
}
