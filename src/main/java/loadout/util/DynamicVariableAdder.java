package loadout.util;

import basemod.BaseMod;
import basemod.abstracts.DynamicVariable;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.helper.ModifierLibrary;
import loadout.helper.dynamicvariables.HealVariable;
import loadout.helper.dynamicvariables.MiscVariable;
import loadout.patches.AbstractCardPatch;

public class DynamicVariableAdder {
    public static void addAllVariables() {
        BaseMod.addDynamicVariable(new HealVariable());
        BaseMod.addDynamicVariable(new MiscVariable());

        for (String key: ModifierLibrary.modifiers.keySet()) {
            BaseMod.addDynamicVariable(new DynamicVariable() {
                @Override
                public String key() {
                    return key;
                }

                @Override
                public boolean isModified(AbstractCard abstractCard) {
                    return false;
                }

                @Override
                public int value(AbstractCard abstractCard) {
                    return AbstractCardPatch.getMagicNumber(abstractCard, key);
                }

                @Override
                public int baseValue(AbstractCard abstractCard) {
                    return AbstractCardPatch.getMagicNumber(abstractCard, key);
                }

                @Override
                public boolean upgraded(AbstractCard abstractCard) {
                    return abstractCard.upgraded;
                }
            });
        }
    }
}
