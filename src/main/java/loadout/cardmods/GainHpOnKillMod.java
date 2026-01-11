package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;

import loadout.LoadoutMod;
import loadout.damagemods.FeastMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class GainHpOnKillMod extends AbstractLoadoutMagicCardModifier {

    public static String ID = LoadoutMod.makeID("GainHpOnKillModifier");
    private static String description = ModifierLibrary.TEXT[2];

    private static final AbstractDamageModifier feedMod = new FeastMod(ID);

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
        DamageModifierManager.addModifier(card, feedMod);
        AbstractCardPatch.addMagicNumber(card, ID, 0);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, feedMod);
        AbstractCardPatch.removeMagicNumber(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainHpOnKillMod();
    }

}
