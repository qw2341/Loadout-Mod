package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.powers.watcher.EndTurnDeathPower;
import loadout.LoadoutMod;

public class DieNextTurnMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("DieNTMod");
    public static String description = "";

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + description + LocalizedStrings.PERIOD;
    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        AbstractCreature p = AbstractDungeon.player;
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(p, p, new EndTurnDeathPower(p)));
    }


    @Override
    public AbstractCardModifier makeCopy() {
        return new DieNextTurnMod();
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    public static void onLoad() {
        String desc = CardCrawlGame.languagePack.getCardStrings("Blasphemy").DESCRIPTION;
        String txtToAdd;
        if(!LocalizedStrings.PERIOD.equals("") && desc.contains(LocalizedStrings.PERIOD)) {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        } else {
            txtToAdd = desc.split("NL")[1];
        }
        description = txtToAdd;
    }
}
