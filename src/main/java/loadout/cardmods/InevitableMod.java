package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.screens.CardViewPopupHeader;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InevitableMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("InevitableMod");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return CardViewPopupHeader.TEXT[20] + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }

    @Override
    public void onExhausted(AbstractCard card) {
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction(card.makeStatEquivalentCopy()));
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new InevitableMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }


}
