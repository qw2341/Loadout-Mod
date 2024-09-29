package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.GainGoldAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class HealOnPlayMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("HealOnPlayModifier");
    private static String description = ModifierLibrary.TEXT[4];

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

        AbstractDungeon.actionManager.addToBottom(new HealAction(AbstractDungeon.player,AbstractDungeon.player, AbstractCardPatch.getMagicNumber(card, ID)));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new HealOnPlayMod();
    }

}
