package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class PlayableMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("PlayableCardModifier");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return StringUtils.remove(rawDescription, StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]) + LocalizedStrings.PERIOD);
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.cost = 0;
        card.costForTurn = 0;

    }

    @Override
    public boolean canPlayCard(AbstractCard card) {
        return true;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new PlayableMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
}
