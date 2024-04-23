package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.screens.CardViewPopupHeader;
import loadout.util.KeywordsAdder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InfiniteUpgradeMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("InfUpMod");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return  KeywordsAdder.getKeywordString(CardViewPopupHeader.TEXT[22], LoadoutMod.getModID()) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(CardViewPopupHeader.TEXT[22], CardViewPopupHeader.TEXT[23]));
        return tips;
    }


    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new InfiniteUpgradeMod();
    }
}
