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
    private static final String EXHAUST_STRING = StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]) + (Settings.lineBreakViaCharacter ? " " : "") + LocalizedStrings.PERIOD;

    public UnexhaustMod() {
        super();
        this.priority = 5;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {

        String regEx = "(NL\\s)*" + EXHAUST_STRING + "(?!.*" + EXHAUST_STRING + ")";
        return StringUtils.replacePattern(rawDescription, regEx, "");
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.exhaust = false;
    }

    @Override
    public void onRemove(AbstractCard card) {
        card.exhaust = true;
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
