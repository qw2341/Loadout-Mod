package loadout.cardmods;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
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

public class DamageMod extends ExtraEffectModifier {
    public static String ID = LoadoutMod.makeID("DamageMod");
    public static String description = "";

    public DamageMod() {
        super(VariableType.DAMAGE, 0);
    }

    @Override
    public String modifyDescription(String rawDescription, AbstractCard card) {
        return rawDescription + " NL " + description;
    }

    @Override
    public void doExtraEffects(AbstractCard abstractCard, AbstractPlayer abstractPlayer, AbstractCreature abstractCreature, UseCardAction useCardAction) {
        addToBot(new DamageAction(abstractCreature, new DamageInfo(abstractPlayer, abstractCard.damage)));
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
