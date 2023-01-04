package loadout.helper.dynamicvariables;

import basemod.abstracts.DynamicVariable;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;

public class MiscVariable extends DynamicVariable {
    @Override
    public String key() {
        return LoadoutMod.makeID("Misc");
    }

    @Override
    public boolean isModified(AbstractCard abstractCard) {
        return false;
    }

    @Override
    public int value(AbstractCard abstractCard) {
        return abstractCard.misc;
    }

    @Override
    public int baseValue(AbstractCard abstractCard) {
        return abstractCard.misc;
    }

    @Override
    public boolean upgraded(AbstractCard abstractCard) {
        return abstractCard.upgraded;
    }
}
