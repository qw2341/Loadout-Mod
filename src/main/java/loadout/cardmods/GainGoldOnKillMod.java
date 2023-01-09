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
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

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

        AbstractDungeon.actionManager.addToBottom(new GreedAction(target,new DamageInfo(AbstractDungeon.player, 0,action.damageType), card.misc));
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnKillMod();
    }

    public static void onLoad() {
        String txtToAdd;
        String desc = CardCrawlGame.languagePack.getCardStrings("HandOfGreed").DESCRIPTION;
        txtToAdd = desc.split("NL")[1];
        description = txtToAdd.replace("!M!", "!"+LoadoutMod.makeID("Misc")+"!");
    }
}
