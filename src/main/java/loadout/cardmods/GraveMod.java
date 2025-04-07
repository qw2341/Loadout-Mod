package loadout.cardmods;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import loadout.LoadoutMod;
import loadout.util.KeywordsAdder;

public class GraveMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("GraveModifier");


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return KeywordsAdder.getKeywordString(StringUtils.capitalize(BaseMod.getKeywordTitle("grave")), null)  + LocalizedStrings.PERIOD + " NL " + rawDescription;
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

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(BaseMod.getKeywordTitle("grave"), BaseMod.getKeywordDescription("grave")));
        return tips;
    }
}
