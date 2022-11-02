package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class GraveMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("GraveModifier");


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return StringUtils.capitalize(BaseMod.getKeywordTitle("grave")) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        GraveField.grave.set(card,true);
    }

    @Override
    public void onRemove(AbstractCard card) {
        GraveField.grave.set(card,false);
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new GraveMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
