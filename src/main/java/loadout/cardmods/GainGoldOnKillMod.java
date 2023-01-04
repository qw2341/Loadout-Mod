package loadout.cardmods;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.actions.unique.GreedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.HandOfGreed;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import loadout.LoadoutMod;

public class GainGoldOnKillMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GainGoldOnKillModifier");
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

        AbstractDungeon.actionManager.addToBottom(new GreedAction(target,new DamageInfo(AbstractDungeon.player, 0,action.damageType), card.magicNumber));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnKillMod();
    }

    public static void onLoad() {
        description = CardCrawlGame.languagePack.getCardStrings("HandOfGreed").DESCRIPTION.split("NL")[1];
    }
}
