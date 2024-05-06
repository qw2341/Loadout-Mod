package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.actions.GMOKAction;
import loadout.damagemods.MagicDaggerMod;
import loadout.screens.CardViewPopupHeader;

public class GainMagicOnKillMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GMOKMod");
    private static String description = "";

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
        DamageModifierManager.addModifier(card, new MagicDaggerMod());
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new GainMagicOnKillMod();
    }

    public static void onLoad() {
        description = CardViewPopupHeader.TEXT[18];
    }
}
