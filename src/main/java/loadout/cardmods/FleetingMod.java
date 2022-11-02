package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class FleetingMod  extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("FleetingModifier");

    public FleetingMod() {
        this.priority = -1;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + StringUtils.capitalize(BaseMod.getKeywordTitle("fleeting")) + LocalizedStrings.PERIOD;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        FleetingField.fleeting.set(card,true);
    }

    @Override
    public void onRemove(AbstractCard card) {
        FleetingField.fleeting.set(card,false);
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new FleetingMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
