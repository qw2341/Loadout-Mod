package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

public class SoulboundMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("SoulboundModifier");


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return StringUtils.capitalize(BaseMod.getKeywordTitle("soulbound")) + LocalizedStrings.PERIOD + " NL " + rawDescription;
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
}
