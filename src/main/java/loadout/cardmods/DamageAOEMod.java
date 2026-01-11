package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;

import com.evacipated.cardcrawl.mod.stslib.damagemods.BindingHelper;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import loadout.util.Wiz;

public class DamageAOEMod extends AbstractLoadoutMagicCardModifier {
    public static String ID = LoadoutMod.makeID("DamageAOEMod");
    public static String description = "";


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }


    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        addToBot(BindingHelper.makeAction(card, new DamageAllEnemiesAction(Wiz.adp(), AbstractCardPatch.getMagicNumber(card, ID), card.damageTypeForTurn, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL)));
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        if(card.target == AbstractCard.CardTarget.SELF)
                card.target = AbstractCard.CardTarget.ALL;
//        card.multiDamage = DamageInfo.createDamageMatrix(card.baseDamage);
        AbstractCardPatch.addMagicNumber(card, ID, 0);
    }

    @Override
    public void onCalculateCardDamage(AbstractCard card, AbstractMonster mo) {
        card.multiDamage = DamageInfo.createDamageMatrix(card.baseDamage);
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new DamageAOEMod();
    }

    public static void onLoad() {
        description = modifyDescriptionWithCustomMagic(CardCrawlGame.languagePack.getCardStrings("Cleave").DESCRIPTION, ID, "D");
    }
}
