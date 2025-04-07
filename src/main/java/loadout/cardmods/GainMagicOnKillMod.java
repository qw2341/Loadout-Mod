package loadout.cardmods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.abstracts.AbstractCardModifier;
import loadout.LoadoutMod;
import loadout.damagemods.MagicDaggerMod;
import loadout.helper.ModifierLibrary;
import loadout.screens.CardViewPopupHeader;

public class GainMagicOnKillMod extends AbstractCardModifier {

    public static final String ID = LoadoutMod.makeID("GMOKMod");
    private static String description = ModifierLibrary.TEXT[6];

    private static final AbstractDamageModifier daggerMod = new MagicDaggerMod(ID);

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
        return new GainMagicOnKillMod();
    }

    public static void onLoad() {
        description = CardViewPopupHeader.TEXT[18];
    }
}
