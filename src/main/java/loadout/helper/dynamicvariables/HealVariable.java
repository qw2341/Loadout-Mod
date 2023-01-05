package loadout.helper.dynamicvariables;

import basemod.abstracts.DynamicVariable;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;

public class HealVariable extends DynamicVariable {
    @Override
    public String key() {
        return LoadoutMod.makeID("Heal");
    }

    @Override
    public boolean isModified(AbstractCard abstractCard) {
        return false;
    }

    @Override
    public int value(AbstractCard abstractCard) {
        return abstractCard.baseHeal;
    }

    @Override
    public int baseValue(AbstractCard abstractCard) {
        return abstractCard.baseHeal;
    }

    @Override
    public boolean upgraded(AbstractCard abstractCard) {
        return abstractCard.upgraded;
    }
}
