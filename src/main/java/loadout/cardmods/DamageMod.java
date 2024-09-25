package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.evacipated.cardcrawl.mod.stslib.extraeffects.ExtraEffectModifier;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import loadout.LoadoutMod;
import loadout.util.KeywordsAdder;
import loadout.util.Wiz;
import org.apache.commons.lang3.StringUtils;

public class DamageMod extends AbstractCardModifier {
    public static String ID = LoadoutMod.makeID("DamageMod");
    public static String description = "";


    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }


    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        DamageInfo di = new DamageInfo(Wiz.adp(), card.damage);
        DamageModifierManager.bindInstigator(di, card);
        addToBot(new DamageAction(target, di));
    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        if (card.target != AbstractCard.CardTarget.ENEMY) card.target = AbstractCard.CardTarget.SELF_AND_ENEMY;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new DamageMod();
    }

    public static void onLoad() {
        description = CardCrawlGame.languagePack.getCardStrings("Strike_R").DESCRIPTION;
    }
}
