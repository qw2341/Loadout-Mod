package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;
import loadout.damagemods.RitualDaggerMod;
import loadout.helper.ModifierLibrary;

public class GainDamageOnKill extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GDOKMod");
    private static String description = ModifierLibrary.TEXT[1];

    private static final AbstractDamageModifier daggerMod = new RitualDaggerMod(ID);

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
        DamageModifierManager.addModifier(card, daggerMod);
    }

    @Override
    public void onRemove(AbstractCard card) {
        DamageModifierManager.removeModifier(card, daggerMod);
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainDamageOnKill();
    }

}
