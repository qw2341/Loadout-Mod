package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.unique.FeedAction;
import com.megacrit.cardcrawl.actions.unique.RitualDaggerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.actions.GDOKAction;

public class GainDamageOnKill extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GDOKMod");
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

        AbstractDungeon.actionManager.addToBottom(new GDOKAction(target, card.misc, card.uuid));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainDamageOnKill();
    }

    public static void onLoad() {
        String desc= CardCrawlGame.languagePack.getCardStrings("RitualDagger").DESCRIPTION;
        String txtToAdd;
        if(!LocalizedStrings.PERIOD.equals("") && desc.contains(LocalizedStrings.PERIOD)) {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        } else {
            txtToAdd = desc.split("NL")[1];
        }
        description = txtToAdd.replace("!M!", "!"+LoadoutMod.makeID("Misc")+"!");
    }
}
