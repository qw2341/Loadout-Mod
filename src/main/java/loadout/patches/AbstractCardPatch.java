package loadout.patches;

import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import loadout.actions.MultiUseAction;
import loadout.cardmods.XCostMod;
import loadout.savables.CardModifications;

public class AbstractCardPatch {
    @SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
    public static class CopyPatch {
        @SpireInsertPatch(rloc = 10, localvars = {"card"})
        public static void Insert(AbstractCard __instance, AbstractCard card) {
            card.rarity = __instance.rarity;
            card.type = __instance.type;
            card.color = __instance.color;
        }
    }

    /**
     * From Hubris Zylophone patch
     */
    @SpirePatch(
            clz= AbstractPlayer.class,
            method="useCard"
    )
    public static class MultiUse
    {
        public static ExprEditor Instrument()
        {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getClassName().equals(AbstractCard.class.getName()) && m.getMethodName().equals("use")) {

                        m.replace(
                                "if (" + MultiUse.class.getName() + ".isXCost($0)) {" +
                                        MultiUse.class.getName() + ".use($0, $$, energyOnUse);" +
                                        "} else {" +
                                        "$_ = $proceed($$);" +
                                        "}"
                        );
                    }
                }
            };
        }

        public static boolean isXCost(AbstractCard c)
        {
            return CardModifierManager.hasModifier(c, XCostMod.ID);
        }

        public static void use(AbstractCard c, AbstractPlayer player, AbstractMonster monster, int energyOnUse)
        {
            energyOnUse = EnergyPanel.getCurrentEnergy();
            if (player.hasRelic(ChemicalX.ID)) {
                player.getRelic(ChemicalX.ID).flash();
                energyOnUse += 2;
            }
            AbstractDungeon.actionManager.addToBottom(new MultiUseAction(c, player, monster, energyOnUse));
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "<class>")
    public static class CardModificationFields {
        public static SpireField<Boolean> isCardModifiedByModifier = new SpireField<>(() -> Boolean.valueOf(false));
    }

    public static boolean isCardModified(AbstractCard ac) {
        return CardModificationFields.isCardModifiedByModifier.get(ac);
    }

    public static void setCardModified(AbstractCard ac, boolean isModified) {
        CardModificationFields.isCardModifiedByModifier.set(ac,isModified);
    }

    @SpirePatch(clz = AbstractCard.class, method = "<ctor>", paramtypez={
            String.class,
            String.class,
            String.class,
            int.class,
            String.class,
            AbstractCard.CardType.class,
            AbstractCard.CardColor.class,
            AbstractCard.CardRarity.class,
            AbstractCard.CardTarget.class,
            DamageInfo.DamageType.class
    })
    public static class CardModApplicatorPatch {
        @SpirePostfixPatch
        public static void PostFix(AbstractCard __instance, String id, String name, String imgUrl, int cost, String rawDescription, AbstractCard.CardType type, AbstractCard.CardColor color, AbstractCard.CardRarity rarity, AbstractCard.CardTarget target, DamageInfo.DamageType dType) {
            if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(__instance.cardID)) {
                CardModifications.modifyCard(__instance,CardModifications.cardMap.get(__instance.cardID));
            }
        }
    }
}
