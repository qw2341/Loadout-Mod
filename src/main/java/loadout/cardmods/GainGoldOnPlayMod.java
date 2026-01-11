package loadout.cardmods;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainGoldAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;

import basemod.abstracts.AbstractCardModifier;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class GainGoldOnPlayMod extends AbstractLoadoutMagicCardModifier {

    public static String ID = LoadoutMod.makeID("GainGoldOnPlayModifier");
    private static String description = ModifierLibrary.TEXT[3];

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        int amount = AbstractCardPatch.getMagicNumber(card, ID);
        AbstractDungeon.effectList.add(new RainingGoldEffect(amount * 2, true));
        AbstractGameAction aga = new GainGoldAction(amount);
        aga.actionType = AbstractGameAction.ActionType.DAMAGE;
        AbstractDungeon.actionManager.addToBottom(aga);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnPlayMod();
    }

}
