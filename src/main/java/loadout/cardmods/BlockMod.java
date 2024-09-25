package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.extraeffects.ExtraEffectModifier;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import loadout.LoadoutMod;

public class BlockMod extends ExtraEffectModifier {
    public static String ID = LoadoutMod.makeID("BlockMod");
    public static String description = "";

    public BlockMod() {
        super(VariableType.BLOCK, 0);
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public void doExtraEffects(AbstractCard abstractCard, AbstractPlayer abstractPlayer, AbstractCreature abstractCreature, UseCardAction useCardAction) {
        addToBot(new GainBlockAction(abstractPlayer, abstractCard.block));
    }

    @Override
    public String getExtraText(AbstractCard abstractCard) {
        return null;
    }

    @Override
    public String getEffectId(AbstractCard abstractCard) {
        return ID;
    }


    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new BlockMod();
    }

    public static void onLoad() {
        description = CardCrawlGame.languagePack.getCardStrings("Defend_R").DESCRIPTION;
    }
}
