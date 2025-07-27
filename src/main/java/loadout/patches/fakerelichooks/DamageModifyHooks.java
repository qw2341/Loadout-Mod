package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
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
        @SpireInsertPatch(locator = PlayerStanceLocator.class, localvars = {"tmp"})
        public static void Insert(AbstractCard __instance, @ByRef float[] tmp) {
            tmp[0] = AllInOneBag.INSTANCE.atDamageModify(tmp[0], __instance);
        }
    }

    @SpirePatches2({
            @SpirePatch2(clz = AbstractCard.class, method = "applyPowers"),
            @SpirePatch2(clz = AbstractCard.class, method = "calculateCardDamage")})
    public static class MultiDamage {
        @SpireInsertPatch(locator = PlayerStanceLocatorLast.class, localvars = {"tmp", "i"})
        public static void Insert(AbstractCard __instance, float[] tmp, int i) {
            tmp[i] = AllInOneBag.INSTANCE.atDamageModify(tmp[i], __instance);
        }
    }
}
