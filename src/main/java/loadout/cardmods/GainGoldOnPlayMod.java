package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.GainGoldAction;
import com.megacrit.cardcrawl.actions.unique.GreedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.RainingGoldEffect;
import loadout.LoadoutMod;

public class GainGoldOnPlayMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GainGoldOnPlayModifier");
    private static String description = "";

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
        AbstractDungeon.effectList.add(new RainingGoldEffect(card.magicNumber * 2, true));
        AbstractDungeon.actionManager.addToBottom(new GainGoldAction(card.magicNumber));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnPlayMod();
    }

    public static void onLoad() {
        description = CardCrawlGame.languagePack.getCardStrings("FameAndFortune").DESCRIPTION;
    }
}
