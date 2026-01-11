package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;

import loadout.LoadoutMod;
import loadout.damagemods.GreedMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class GainGoldOnKillMod extends AbstractLoadoutMagicCardModifier {

    public static final String ID = LoadoutMod.makeID("GainGoldOnKillModifier");
    private static String description = ModifierLibrary.TEXT[0];

    private static final AbstractDamageModifier greedMod = new GreedMod(ID);;

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
        AbstractCardPatch.addMagicNumber(card, ID, 0);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, greedMod);
        AbstractCardPatch.removeMagicNumber(card, ID);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainGoldOnKillMod();
    }

}
