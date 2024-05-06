package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.unique.FeedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.FlyingOrbEffect;
import loadout.LoadoutMod;
import loadout.actions.LifestealAction;
import loadout.damagemods.LifestealDamageMod;

public class LifestealMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("Lifesteal");
    private static String description = "";

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + description + LocalizedStrings.PERIOD;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

//    @Override
//    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
//        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new LifestealAction(AbstractDungeon.player, target));
//    }


    @Override
    public void onInitialApplication(AbstractCard card) {
        DamageModifierManager.addModifier(card, new LifestealDamageMod());
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new LifestealMod();
    }

    public static void onLoad() {
        String desc= CardCrawlGame.languagePack.getCardStrings("Reaper").DESCRIPTION;
        String txtToAdd;
        if(!LocalizedStrings.PERIOD.equals("") && desc.contains(LocalizedStrings.PERIOD)) {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        } else {
            if(Settings.language == Settings.GameLanguage.ZHT) txtToAdd = desc.split("NL|[，]")[1];
            else txtToAdd = desc.split("NL")[1];
        }
        description = txtToAdd;
    }
}
