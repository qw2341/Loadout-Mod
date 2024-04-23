package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.util.KeywordsAdder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FleetingMod  extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("FleetingModifier");

    public FleetingMod() {
        this.priority = -1;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + KeywordsAdder.getKeywordString(StringUtils.capitalize(BaseMod.getKeywordTitle("fleeting")), null) + LocalizedStrings.PERIOD;
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

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(BaseMod.getKeywordTitle("fleeting"), BaseMod.getKeywordDescription("fleeting")));
        return tips;
    }
}
