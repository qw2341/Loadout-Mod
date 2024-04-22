package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import loadout.relics.TildeKey;

public class AbstractPotionPatch {
    @SpirePatch2(clz = AbstractPotion.class, method = "getPotency", paramtypez = {})
    public static class PotencyPatch {
        @SpirePostfixPatch
        public static int Postfix(int __result, AbstractPotion __instance) {
            return Math.round(__result * (TildeKey.potionPotencyMult / 100f));
        }
    }
}
