package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;

import basemod.ReflectionHacks;

import java.util.Map;

public class AdditionalUpgradePatches {
//    @SpirePatch2(clz = AbstractCard.class, method = "upgradeDamage")
//    public static class UpgradeDamagePatch {
//        @SpirePrefixPatch
//        public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
//            amount[0] = amount[0] + AbstractCardPatch.getCardNormalUpgrade(__instance)[1];
//        }
//    }
//
//    @SpirePatch2(clz = AbstractCard.class, method = "upgradeBlock")
//    public static class UpgradeBlockPatch {
//        @SpirePrefixPatch
//        public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
//            amount[0] = amount[0] + AbstractCardPatch.getCardNormalUpgrade(__instance)[2];
//        }
//    }
//
//    @SpirePatch2(clz = AbstractCard.class, method = "upgradeMagicNumber")
//    public static class UpgradeMagicNumberPatch {
//        @SpirePrefixPatch
//        public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
//            amount[0] = amount[0] + AbstractCardPatch.getCardNormalUpgrade(__instance)[3];
//        }
//    }
//
//    /**
//     * Dynamic Patched
//     * @param __instance
//     */
//    public static void additionalUpgrade(AbstractCard __instance) {
//        Integer[] normUpgrades = AbstractCardPatch.getCardNormalUpgrade(__instance);
//        int costUpgrade = normUpgrades[0];
//        if(costUpgrade != 0)
//            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeBaseCost", int.class).invoke(__instance, costUpgrade);
//        int dmgUpgrade = normUpgrades[1];
//        if(dmgUpgrade != 0)
//            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeDamage", int.class).invoke(__instance, dmgUpgrade);
//        int blckUpgrade = normUpgrades[2];
//        if(blckUpgrade != 0)
//            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeBlock", int.class).invoke(__instance, blckUpgrade);
//        int magkUpgrade = normUpgrades[3];
//        if (magkUpgrade != 0)
//            ReflectionHacks.privateMethod(AbstractCard.class, "upgradeMagicNumber", int.class).invoke(__instance, magkUpgrade);
//        int miscUpgrade = normUpgrades[4];
//        __instance.misc += miscUpgrade;
//
//        //Additional magic number upgrade
////        Map<String, Integer> additionalUpgrades = AbstractCardPatch.getCardAdditionalMagicUpgrade(__instance);
//    }

}
