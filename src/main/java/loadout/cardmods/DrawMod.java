package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class DrawMod extends AbstractLoadoutMagicCardModifier {
    public static String ID = LoadoutMod.makeID("DrawMod");
    public static String description = ModifierLibrary.TEXT[7];


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        addToBot(new DrawCardAction(AbstractCardPatch.getMagicNumber(card, ID)));
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new DrawMod();
    }
}
