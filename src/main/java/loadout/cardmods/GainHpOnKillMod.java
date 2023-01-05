package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.unique.FeedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;

import java.util.Arrays;

public class GainHpOnKillMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GainHpOnKillModifier");
    private static String description = "";

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + description + LocalizedStrings.PERIOD;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {

        AbstractDungeon.actionManager.addToBottom(new FeedAction(target,new DamageInfo(AbstractDungeon.player, 0,action.damageType), card.misc));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainHpOnKillMod();
    }

    public static void onLoad() {
        String txtToAdd = CardCrawlGame.languagePack.getCardStrings("Feed").DESCRIPTION.split("[" + LocalizedStrings.PERIOD + "]")[1];
        description = txtToAdd.replace("!M!", "!"+LoadoutMod.makeID("Misc")+"!");
    }
}
