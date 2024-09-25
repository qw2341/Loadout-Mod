package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.actions.unique.FeedAction;
import com.megacrit.cardcrawl.actions.unique.RitualDaggerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.actions.GDOKAction;
import loadout.damagemods.RitualDaggerMod;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

public class GainDamageOnKill extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("GDOKMod");
    private static String description = ModifierLibrary.TEXT[1];

    private AbstractDamageModifier daggerMod = null;

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
        daggerMod = new RitualDaggerMod(()-> AbstractCardPatch.getMagicNumber(card, ID));
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
