package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import loadout.LoadoutMod;
import loadout.util.Wiz;

public class BlockMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("BlockMod");
    public static String description = "";


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }


    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        addToBot(new GainBlockAction(Wiz.adp(), card.block));
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
