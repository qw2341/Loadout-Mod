package loadout.cardmods;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
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
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.damagemods.GreedMod;
import loadout.util.Wiz;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public class GainGoldOnKillMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GainGoldOnKillModifier");
    private static String description = "";

    private static AbstractDamageModifier greedMod = new GreedMod();

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        DamageModifierManager.addModifier(card, greedMod);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, greedMod);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnKillMod();
    }

    public static void onLoad() {
        String txtToAdd;
        String desc = CardCrawlGame.languagePack.getCardStrings("HandOfGreed").DESCRIPTION;
        if(desc.contains("NL")) {
            txtToAdd = desc.split("NL")[1];
        } else {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        }

        description = txtToAdd.replace("!M!", "!"+LoadoutMod.makeID("Misc")+"!");
    }
}
