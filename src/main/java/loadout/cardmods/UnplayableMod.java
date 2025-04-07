package loadout.cardmods;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import loadout.LoadoutMod;
import loadout.util.KeywordsAdder;

public class UnplayableMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("UnplayableCardModifier");


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return KeywordsAdder.getKeywordString(StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]),null) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.cost = -2;
        card.costForTurn = -2;
    }

    @Override
    public void onRemove(AbstractCard card) {
        card.cost = 0;
        card.costForTurn = 0;
    }

    @Override
    public boolean canPlayCard(AbstractCard card) {
        return false;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new UnplayableMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(GameDictionary.UNPLAYABLE.NAMES[0], GameDictionary.UNPLAYABLE.DESCRIPTION));
        return tips;
    }
}
