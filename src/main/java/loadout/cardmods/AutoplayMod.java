package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class AutoplayMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("AutoPlayModifier");

    public AutoplayMod() {
        this.priority = 1;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return StringUtils.capitalize(BaseMod.getKeywordTitle("autoplay")) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        AutoplayField.autoplay.set(card,Boolean.valueOf(true));
    }

    @Override
    public void onRemove(AbstractCard card) {
        AutoplayField.autoplay.set(card,Boolean.valueOf(false));
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new AutoplayMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

}
