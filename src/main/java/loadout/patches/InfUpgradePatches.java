package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;

public class InfUpgradePatches {
    /**
     * References: Autumn, M. 2023. Chimera Cards. OnUpgradePatches, pp. 13-64.
     */
    @SpirePatches2({@SpirePatch2(clz = AbstractCard.class, method = "upgradeDamage"),
            @SpirePatch2(clz = AbstractCard.class, method = "upgradeBlock"),
            @SpirePatch2(clz = AbstractCard.class, method = "upgradeMagicNumber")})
    public static class InfUpgradeNumbersPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractCard __instance, @ByRef int[] amount) {
            if(loadout.patches.InfUpgradePatch.isInfUpgrade(__instance)) {
                amount[0] = amount[0] + Math.max(0, __instance.timesUpgraded - 1);
            }
        }
    }

    @SpirePatch2(clz = AbstractCard.class, method = "canUpgrade")
    public static class CanUpgradePatch {
        @SpirePostfixPatch
        public static boolean Postfix(boolean __result, AbstractCard __instance) {
            return InfUpgradePatch.isInfUpgrade(__instance) || __result;
        }
    }
}


