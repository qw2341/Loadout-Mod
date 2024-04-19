package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.relics.AllInOneBag;


public class DamageModifyHooks {
    @SpirePatches2({
            @SpirePatch2(clz = AbstractCard.class, method = "applyPowers"),
            @SpirePatch2(clz = AbstractCard.class, method = "calculateCardDamage")})
    public static class NonmultiDamage {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class, localvars = {"tmp"})
        public static void Insert(AbstractCard __instance, float tmp) {
            tmp = AllInOneBag.INSTANCE.atDamageModify(tmp, __instance);
        }
    }

    @SpirePatches2({
            @SpirePatch2(clz = AbstractCard.class, method = "applyPowers"),
            @SpirePatch2(clz = AbstractCard.class, method = "calculateCardDamage")})
    public static class MultiDamage {
        @SpireInsertPatch(locator = PlayerRelicsLocatorLast.class, localvars = {"tmp", "i"})
        public static void Insert(AbstractCard __instance, float[] tmp, int i) {
            tmp[i] = AllInOneBag.INSTANCE.atDamageModify(tmp[i], __instance);
        }
    }
}
