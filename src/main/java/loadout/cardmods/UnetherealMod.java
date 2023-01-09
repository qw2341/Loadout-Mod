package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class UnetherealMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("UnetherealMod");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {

        String regEx = StringUtils.capitalize(GameDictionary.ETHEREAL.NAMES[0]) + (Settings.lineBreakViaCharacter ? " " : "") + LocalizedStrings.PERIOD;
        return StringUtils.replacePattern(rawDescription, regEx, "");
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.isEthereal = false;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
    @Override
    public AbstractCardModifier makeCopy() {
        return new UnetherealMod();
    }
}
