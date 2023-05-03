package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.screens.CardViewPopupHeader;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InfiniteUpgradeMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("InfUpMod");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return CardViewPopupHeader.TEXT[22] + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
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
