package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;
import loadout.damagemods.LessonLearnedMod;
import loadout.helper.ModifierLibrary;

public class RandomUpgradeOnKillMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("RandUpOKMod");
    private static String description = ModifierLibrary.TEXT[5];

    private static final AbstractDamageModifier lessonLearnedMod = new LessonLearnedMod(ID);

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
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

}
