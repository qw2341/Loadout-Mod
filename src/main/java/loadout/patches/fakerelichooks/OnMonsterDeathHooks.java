package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.AwakenedOne;
import loadout.relics.AllInOneBag;

public class OnMonsterDeathHooks {
    @SpirePatches2({
            @SpirePatch2(clz = AbstractMonster.class, method = "die", paramtypez = {boolean.class}),
            @SpirePatch2(clz = AwakenedOne.class, method = "damage"),
            @SpirePatch2(clz = AwakenedOne.class, method = "damage")})
    public static class OnDeathHook{
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(AbstractMonster __instance) {
            AllInOneBag.INSTANCE.onMonsterDeath(__instance);
        }
    }
}
