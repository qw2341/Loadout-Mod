package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.ExhaustAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class ExhaustCardMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("ExhaustCMod");
    public static String description = ModifierLibrary.TEXT[9];


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }


    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        addToBot(new ExhaustAction(AbstractCardPatch.getMagicNumber(card, ID), false, false, false));
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new ExhaustCardMod();
    }
}
