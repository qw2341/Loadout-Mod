package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.extraeffects.ExtraEffectModifier;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import loadout.LoadoutMod;

public class DamageAOEMod extends ExtraEffectModifier{
    public static String ID = LoadoutMod.makeID("DamageAOEMod");
    public static String description = "";

    public DamageAOEMod() {
        super(ExtraEffectModifier.VariableType.DAMAGE, 0);
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public void doExtraEffects(AbstractCard abstractCard, AbstractPlayer abstractPlayer, AbstractCreature abstractCreature, UseCardAction useCardAction) {
        addToBot(new DamageAllEnemiesAction(abstractPlayer, abstractCard.baseDamage, abstractCard.damageTypeForTurn, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
    }

    @Override
    public String getExtraText(AbstractCard abstractCard) {
        return null;
    }

    @Override
    public String getEffectId(AbstractCard abstractCard) {
        return ID;
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        if (card.target != AbstractCard.CardTarget.ALL && card.target != AbstractCard.CardTarget.ALL_ENEMY) {
            if(card.target == AbstractCard.CardTarget.SELF || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY)
                card.target = AbstractCard.CardTarget.ALL;
            else if(card.target == AbstractCard.CardTarget.ENEMY)
                card.target = AbstractCard.CardTarget.ALL_ENEMY;
        }
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
        description = CardCrawlGame.languagePack.getCardStrings("Cleave").DESCRIPTION;
    }
}
