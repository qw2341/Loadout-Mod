package loadout.patches;

import java.util.Map;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import loadout.helper.ModifierLibrary;

public class AdditionalUpgradePatches {
   @SpirePatch2(clz = AbstractCard.class, method = "upgradeDamage")
   public static class UpgradeDamagePatch {
       @SpirePrefixPatch
       public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
            Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
            if (normUpgrades == null) return;
            // System.out.println("Upgrading! The array is: " + java.util.Arrays.toString(normUpgrades));
            amount[0] += normUpgrades[1];
       }
   }

   @SpirePatch2(clz = AbstractCard.class, method = "upgradeBlock")
   public static class UpgradeBlockPatch {
       @SpirePrefixPatch
       public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
            Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
            if (normUpgrades == null) return;
            amount[0] += normUpgrades[2];
       }
   }

   @SpirePatch2(clz = AbstractCard.class, method = "upgradeMagicNumber")
   public static class UpgradeMagicNumberPatch {
       @SpirePrefixPatch
       public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
            Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
            if (normUpgrades == null) return;
            amount[0] += normUpgrades[3];
       }
   }

   @SpirePatch2(clz = AbstractCard.class, method = "upgradeBaseCost")
   public static class UpgradeBaseCostPatch {
       @SpireInsertPatch(rloc = 2)
       public static void Insert(AbstractCard __instance, @ByRef int[] newBaseCost) {
            Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
            if (normUpgrades == null) return;
            __instance.cost += normUpgrades[0];
       }
   }

    public static void upgradeBaseCost(AbstractCard card,int costUpgrade) {
        int newBaseCost = card.cost + costUpgrade;
        int diff = card.costForTurn - card.cost;
        card.cost = newBaseCost;
        if (card.costForTurn > 0) {
            card.costForTurn = card.cost + diff;
        }

        if (card.costForTurn < 0) {
            card.costForTurn = 0;
        }

        card.upgradedCost = true;
    }
   /**
    * Dynamic Patched
    * @param __instance, the card to be upgraded
    * @param doCost, whether to upgrade cost
    * @param doDamage whether to upgrade damage
    * @param doBlock whether to upgrade block
    * @param doMagic whether to upgrade magic number
    */
    public static void additionalUpgrade(AbstractCard __instance, boolean doCost, boolean doDamage, boolean doBlock, boolean doMagic) {
        Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
        if (normUpgrades == null) return;
        int costUpgrade = normUpgrades[0];
        if(doCost && costUpgrade != 0)
            upgradeBaseCost(__instance, costUpgrade);
        int dmgUpgrade = normUpgrades[1];
        if(doDamage && dmgUpgrade != 0)
            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeDamage", int.class).invoke(__instance, dmgUpgrade);
        int blckUpgrade = normUpgrades[2];
        if(doBlock && blckUpgrade != 0)
            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeBlock", int.class).invoke(__instance, blckUpgrade);
        int magkUpgrade = normUpgrades[3];
        if (doMagic && magkUpgrade != 0)
            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeMagicNumber", int.class).invoke(__instance, magkUpgrade);
       int miscUpgrade = normUpgrades[4];
       __instance.misc += miscUpgrade;

    //Additional magic number upgrade
        Map<String, Integer> additionalMagicUpgrades = AbstractCardPatch.getCardAdditionalMagicUpgrade(__instance);
        if (additionalMagicUpgrades != null) {
            for (Map.Entry<String, Integer> entry : additionalMagicUpgrades.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                AbstractCardPatch.upgradeMagicNumber(__instance, key, value);
            }
        }

        // Apply additional modifiers on the first upgrade only
        String[] additionalModifiers = AbstractCardPatch.getCardAdditionalModifiers(__instance);
        if (additionalModifiers != null && additionalModifiers.length > 0 && __instance.timesUpgraded == 0) {
            for (String modifierId : additionalModifiers) {
                //get the first character to determine add or remove
                char action = modifierId.charAt(0);
                modifierId = modifierId.substring(1);

                AbstractCardModifier modifier = ModifierLibrary.getModifier(modifierId);
                
                if (action == '+') {
                    if (modifier != null) {
                        CardModifierManager.addModifier(__instance, modifier);
                    }
                } else if (action == '-') {
                    CardModifierManager.removeModifiersById(__instance, modifierId, true);
                }
                
            }
        }
   }

}
