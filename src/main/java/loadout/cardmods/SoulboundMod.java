package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.util.KeywordsAdder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SoulboundMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("SoulboundModifier");


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return KeywordsAdder.getKeywordString(StringUtils.capitalize(BaseMod.getKeywordTitle("soulbound")),null) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        SoulboundField.soulbound.set(card,true);
    }

    @Override
    public void onRemove(AbstractCard card) {
        SoulboundField.soulbound.set(card,false);
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new SoulboundMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(BaseMod.getKeywordTitle("soulbound"), BaseMod.getKeywordDescription("soulbound")));
        return tips;
    }
}
