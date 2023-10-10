package loadout.patches;

import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.red.SearingBlow;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import loadout.LoadoutMod;
import loadout.actions.MultiUseAction;
import loadout.cardmods.XCostMod;
import loadout.savables.CardModifications;
import loadout.savables.SerializableCard;

public class AbstractCardPatch {
    @SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
    public static class CopyPatch {
        @SpireInsertPatch(rloc = 10, localvars = {"card"})
        public static void Insert(AbstractCard __instance, AbstractCard card) {
            card.rarity = __instance.rarity;
            card.type = __instance.type;
            card.color = __instance.color;
            card.magicNumber = __instance.baseMagicNumber;

            card.originalName = __instance.originalName;
            card.name = __instance.name;
            card.rawDescription = __instance.rawDescription;
        }

        /**
         * Patch to prevent double modifying
         * @param __instance
         * @param card
         */
        @SpireInsertPatch(rloc = 2, localvars = {"card"})
        public static void Insert2(AbstractCard __instance, AbstractCard card) {
            if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(card.cardID))CardModifierManager.removeAllModifiers(card, false);
            //CardModifierManager.copyModifiers(__instance, card, false, true, false);
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
            //energyOnUse = EnergyPanel.getCurrentEnergy();
            c.energyOnUse = energyOnUse;
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
            CardModifications.modifyIfExist(__instance);
        }
    }
    @SpirePatch(clz = CardLibrary.class, method = "getCopy", paramtypez = {String.class, int.class, int.class})
    public static class CardLibraryGetCopyMethodPatch {
        @SpirePostfixPatch
        public static AbstractCard PostFix(AbstractCard __result, String key, int upgradeTime, int misc) {
            if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(__result.cardID)) {
                AbstractCard ret = SerializableCard.toAbstractCard(CardModifications.cardMap.get(__result.cardID)) ;
                if(ret.timesUpgraded < upgradeTime) {
                    int upgradesNeeded = upgradeTime - ret.timesUpgraded;
                    for (int i = 0; i < upgradesNeeded; i++) {ret.upgrade();}
                }
                if(ret.misc != misc) {
                    ret.misc = misc;
                }

                return ret;
            }
            return __result;
        }

    }

    @SpirePatch(clz = SearingBlow.class, method = "upgrade")
    public static class upgradeNamePatch {

        public static ExprEditor Instrument()
        {
            return new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    if (f.getClassName().equals(SearingBlow.class.getName())
                            && f.getFieldName().equals("name"))
                        f.replace("{ $1 = $0.originalName + \"+\" + $0.timesUpgraded; $_ = $proceed($$); }");
                }
            };
        }
    }
}
