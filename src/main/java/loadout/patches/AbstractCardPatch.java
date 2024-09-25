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
import loadout.damagemods.MagicDaggerMod;
import loadout.savables.CardModifications;
import loadout.savables.SerializableCard;

import java.util.HashMap;
import java.util.Map;

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

            CardModificationFields.isCardModifiedByModifier.set(card, CardModificationFields.isCardModifiedByModifier.get(__instance));

            CardModificationFields.additionalMagicNumbers.set(card, new HashMap<String, Integer>(CardModificationFields.additionalMagicNumbers.get(__instance)));
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
        public static SpireField<Map<String, Integer>> additionalMagicNumbers = new SpireField<>(HashMap::new);
    }

    public static int getMagicNumber(AbstractCard ac, String cardModID) {
        return CardModificationFields.additionalMagicNumbers.get(ac).getOrDefault(cardModID, 0);
    }
    public static void setMagicNumber(AbstractCard ac, String cardModID, int numberToSet) {
        CardModificationFields.additionalMagicNumbers.get(ac).replace(cardModID, numberToSet);
    }

    public static void addMagicNumber(AbstractCard ac, String cardModID, int numberToSet) {
        CardModificationFields.additionalMagicNumbers.get(ac).putIfAbsent(cardModID, numberToSet);
    }

    public static void removeMagicNumber(AbstractCard ac, String cardModID) {
        CardModificationFields.additionalMagicNumbers.get(ac).remove(cardModID);
    }

    public static final String MAGIC_NUMBER_DELIMITER = ";";
    public static String serializeAdditionalMagicNumbers(AbstractCard ac) {
        Map<String, Integer> numberMap = CardModificationFields.additionalMagicNumbers.get(ac);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : numberMap.entrySet()) {
            sb.append(entry.getKey()).append("|").append(entry.getValue()).append(MAGIC_NUMBER_DELIMITER);
        }
        return sb.toString();
    }

     public static void deserializeAdditionalMagicNumbers(AbstractCard ac, String data) {
        Map<String, Integer> numberMap = CardModificationFields.additionalMagicNumbers.get(ac);
        numberMap.clear();
        String[] pairs = data.split(MAGIC_NUMBER_DELIMITER);
        for (String pair : pairs) {
            if (!pair.isEmpty()) {
                String[] keyValue = pair.split("\\|");
                numberMap.put(keyValue[0], Integer.parseInt(keyValue[1]));
            }
        }
    }

    public static boolean isCardModified(AbstractCard ac) {
        return CardModificationFields.isCardModifiedByModifier.get(ac);
    }

    public static void setCardModified(AbstractCard ac, boolean isModified) {
        CardModificationFields.isCardModifiedByModifier.set(ac,isModified);
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
