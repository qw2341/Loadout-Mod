package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.actions.unique.FeedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.damagemods.FeastMod;

import java.util.Arrays;
import java.util.regex.Pattern;

public class GainHpOnKillMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GainHpOnKillModifier");
    private static String description = "";

    private static AbstractDamageModifier feedMod = new FeastMod();

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + description + LocalizedStrings.PERIOD;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        DamageModifierManager.addModifier(card, feedMod);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, feedMod);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainHpOnKillMod();
    }

    public static void onLoad() {
        String desc= CardCrawlGame.languagePack.getCardStrings("Feed").DESCRIPTION;
        String txtToAdd;
        if(!LocalizedStrings.PERIOD.equals("") && desc.contains(LocalizedStrings.PERIOD)) {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        } else {
            txtToAdd = desc.split("NL")[1];
        }
        description = txtToAdd.replace("!M!", "!"+LoadoutMod.makeID("Misc")+"!");
    }
}
