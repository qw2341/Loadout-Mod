package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.extraeffects.ExtraEffectModifier;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;
import loadout.util.Wiz;

public class DiscardMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("DiscardMod");
    public static String description = ModifierLibrary.TEXT[8];

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        addToBot(new DiscardAction(Wiz.adp(), Wiz.adp(), AbstractCardPatch.getMagicNumber(card, ID), false));
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new DiscardMod();
    }
}
