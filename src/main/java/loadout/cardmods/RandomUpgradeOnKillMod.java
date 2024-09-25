package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.actions.unique.GreedAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.actions.watcher.LessonLearnedAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.damagemods.LessonLearnedMod;
import loadout.patches.AbstractCardPatch;

public class RandomUpgradeOnKillMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("RandUpOKMod");
    private static String description = "";

    private static AbstractDamageModifier lessonLearnedMod;

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
//
//        AbstractDungeon.actionManager.addToBottom(new LessonLearnedAction(target,new DamageInfo(AbstractDungeon.player, 0,action.damageType)));
//    }


    @Override
    public void onInitialApplication(AbstractCard card) {
        lessonLearnedMod = new LessonLearnedMod(() -> AbstractCardPatch.getMagicNumber(card, ID));
        DamageModifierManager.addModifier(card, lessonLearnedMod);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, lessonLearnedMod);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new RandomUpgradeOnKillMod();
    }

    public static void onLoad() {
        String desc= CardCrawlGame.languagePack.getCardStrings("LessonLearned").DESCRIPTION;
        String txtToAdd;
        if(!LocalizedStrings.PERIOD.equals("") && desc.contains(LocalizedStrings.PERIOD)) {
            txtToAdd = desc.split("[" + LocalizedStrings.PERIOD + "]")[1];
        } else {
            txtToAdd = desc.split("NL")[1];
        }
        description = txtToAdd;
    }
}
