package loadout.patches;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.SearingBlow;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;

import basemod.helpers.CardModifierManager;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import loadout.actions.MultiUseAction;
import loadout.cardmods.InfiniteUpgradeMod;
import loadout.cardmods.XCostMod;

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
            Integer[] originalNormalUpgrades = CardModificationFields.additionalNormalUpgrades.get(__instance);
            CardModificationFields.additionalNormalUpgrades.set(card, Arrays.copyOf(originalNormalUpgrades,originalNormalUpgrades.length));
            CardModificationFields.additionalMagicUpgrades.set(card, new HashMap<String, Integer>(CardModificationFields.additionalMagicUpgrades.get(__instance)));
            String[] originalUpgradeModifiers = CardModificationFields.additionalModifiers.get(__instance);
            CardModificationFields.additionalModifiers.set(card, Arrays.copyOf(originalUpgradeModifiers,originalUpgradeModifiers.length));
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
            c.calculateCardDamage(monster);
            AbstractDungeon.actionManager.addToBottom(new MultiUseAction(c, player, monster, energyOnUse));
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "<class>")
    public static class CardModificationFields {
        public static SpireField<Boolean> isCardModifiedByModifier = new SpireField<>(() -> Boolean.valueOf(false));
        public static SpireField<Map<String, Integer>> additionalMagicNumbers = new SpireField<>(HashMap::new);
        public static SpireField<Integer[]> additionalNormalUpgrades = new SpireField<>(() -> new Integer[] {0,0,0,0,0});
        public static SpireField<Map<String, Integer>> additionalMagicUpgrades = new SpireField<>(HashMap::new);
        /**
         * An array of modifier IDs added on upgrade
         * format: '+' or '-' followed by modifier ID
         * e.g. "+ExtraDamageMod", "-WeakenMod"
         * '+' indicates to add the modifier on upgrade
         * '-' indicates to remove the modifier on upgrade
         */
        public static SpireField<String[]> additionalModifiers = new SpireField<>(() -> new String[0]);
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

    public static void upgradeMagicNumber(AbstractCard ac, String cardModID, int amount) {
        if(ac.timesUpgraded == 1 || InfUpgradePatch.isInfUpgrade(ac) || ac.cardID.equals(SearingBlow.ID)) {
            Map<String, Integer> numberMap = CardModificationFields.additionalMagicNumbers.get(ac);
            //Inf upgrade compatibility
            if (InfUpgradePatch.isInfUpgrade(ac) || ac.cardID.equals(SearingBlow.ID)) amount +=  Math.max(0, ac.timesUpgraded - 1) ;
            numberMap.put(cardModID, numberMap.getOrDefault(cardModID, 0) + amount);
        }
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

    public static Integer[] getCardNormalUpgrade(AbstractCard ac) {
        return CardModificationFields.additionalNormalUpgrades.get(ac);
    }

    /**
     *
     * @param ac
     * @param values int cost, int damage, int block, int magic, int misc
     */
    public static void setCardNormalUpgrade(AbstractCard ac, Integer... values) {
        if(values.length != 5) return;
        CardModificationFields.additionalNormalUpgrades.set(ac, values);
    }

    public static void setCardNormalUpgrade(AbstractCard ac, int index, int value) {
        if(index  < 0 || index >= 5) return;
        CardModificationFields.additionalNormalUpgrades.get(ac)[index] = value;
    }

    public static Map<String, Integer> getCardAdditionalMagicUpgrade(AbstractCard ac) {
        return CardModificationFields.additionalMagicUpgrades.get(ac);
    }

    public static void setCardAdditionalMagicUpgrade(AbstractCard ac, Map<String, Integer> upgrades) {
        CardModificationFields.additionalMagicUpgrades.set(ac, upgrades);
    }

    //Sets the value for  a specific magic number
    public static void setCardAdditionalMagicUpgrade(AbstractCard ac, String magicID, int upgrade) {
        CardModificationFields.additionalMagicUpgrades.get(ac).put(magicID,upgrade);
    }

    public static void adjustCardAdditionalMagicUpgrade(AbstractCard ac, String magicID, int delta) {
        CardModificationFields.additionalMagicUpgrades.get(ac).merge(magicID, delta, (k, v) -> v + delta);
    }

    public static String[] getCardAdditionalModifiers(AbstractCard ac) {
        return CardModificationFields.additionalModifiers.get(ac);
    }

    public static void setCardAdditionalModifiers(AbstractCard ac, String[] modifiers) {
        CardModificationFields.additionalModifiers.set(ac, modifiers);
    }

    public static void addCardAdditionalModifier(AbstractCard ac, String modifierID, boolean isAddition) {
        String[] current = CardModificationFields.additionalModifiers.get(ac);
        String[] updated = Arrays.copyOf(current, current.length + 1);
        String modifier = (isAddition ? "+" : "-") + modifierID;
        updated[current.length] = modifier;
        CardModificationFields.additionalModifiers.set(ac, updated);
    }

    public static void mergeCardAdditionalModifiers(AbstractCard ac, String[] newModifiers) {
        String[] current = CardModificationFields.additionalModifiers.get(ac);
        //merge the two arrays, note that '+' and '-' of the same modifiers will cancel out
        Map<String, Boolean> modifierMap = new HashMap<>();
        for (String mod : current) {
            if (mod.length() < 2) continue;
            String id = mod.substring(1);
            boolean isAddition = mod.charAt(0) == '+';
            modifierMap.put(id, isAddition);
        }
        for (String mod : newModifiers) {
            if (mod.length() < 2) continue;
            String id = mod.substring(1);
            boolean isAddition = mod.charAt(0) == '+';
            //cancel out if opposite exists
            modifierMap.merge(id, isAddition, (oldValue, newValue) -> oldValue != newValue ? null : newValue);
        }
        String[] updated = modifierMap.entrySet().stream()
                .map(entry -> (entry.getValue() ? "+" : "-") + entry.getKey())
                .toArray(String[]::new);
        CardModificationFields.additionalModifiers.set(ac, updated);
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
