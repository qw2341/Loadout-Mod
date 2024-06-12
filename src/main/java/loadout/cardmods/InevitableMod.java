package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import basemod.helpers.TooltipInfo;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.screens.CardViewPopupHeader;
import loadout.util.KeywordsAdder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InevitableMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("InevitableMod");

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return KeywordsAdder.getKeywordString(CardViewPopupHeader.TEXT[20], LoadoutMod.getModID()) + LocalizedStrings.PERIOD + " NL " + rawDescription;
    }
    @Override
    public void onInitialApplication(AbstractCard card) {
        if(!CardModifierManager.hasModifier(card, SoulboundMod.ID)) SoulboundField.soulbound.set(card,true);
    }

    @Override
    public void onRemove(AbstractCard card) {
        if(!CardModifierManager.hasModifier(card, SoulboundMod.ID)) SoulboundField.soulbound.set(card,false);
    }

    @Override
    public void onExhausted(AbstractCard card) {
        AbstractDungeon.actionManager.addToBottom(new MakeTempCardInHandAction(card.makeStatEquivalentCopy()));
    }

    @Override
    public List<TooltipInfo> additionalTooltips(AbstractCard card) {
        if(!LoadoutMod.isCHN()) return null;
        ArrayList<TooltipInfo> tips = new ArrayList<>();
        tips.add(new TooltipInfo(CardViewPopupHeader.TEXT[20], CardViewPopupHeader.TEXT[21]));
        return tips;
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
