package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.screens.CardViewPopupHeader;
import loadout.util.KeywordsAdder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoplayMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("AutoPlayModifier");

    public AutoplayMod() {
        this.priority = 1;
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return KeywordsAdder.getKeywordString(StringUtils.capitalize(BaseMod.getKeywordTitle("autoplay")), null)  + LocalizedStrings.PERIOD + " NL " + rawDescription;
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

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(BaseMod.getKeywordTitle("autoplay"), BaseMod.getKeywordDescription("autoplay")));
        return tips;
    }

}
