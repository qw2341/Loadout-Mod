package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class UnexhaustMod  extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("UnexhaustMod");

    public UnexhaustMod() {
        super();
        this.priority = 5;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {

        String regEx = "NL" + "\\s*" + StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]) + (Settings.lineBreakViaCharacter ? " " : "") + LocalizedStrings.PERIOD;
        return StringUtils.replacePattern(rawDescription, regEx, "");
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.exhaust = false;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new UnexhaustMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }
//    @Override
//    public boolean shouldApply(AbstractCard card) {
//        return card.exhaust;
//    }
}
