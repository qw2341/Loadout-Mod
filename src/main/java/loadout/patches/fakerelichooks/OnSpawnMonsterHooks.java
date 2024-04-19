package loadout.patches.fakerelichooks;


import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.actions.common.ReviveMonsterAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.actions.unique.SummonGremlinAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import loadout.relics.AllInOneBag;


public class OnSpawnMonsterHooks {
    @SpirePatch2(clz = ReviveMonsterAction.class, method = "update")
    public static class ReviveHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(ReviveMonsterAction __instance) {
            AllInOneBag.INSTANCE.onSpawnMonster((AbstractMonster) __instance.target);
        }
    }

    @SpirePatch2(clz = SpawnMonsterAction.class, method = "update")
    public static class SpawnHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(SpawnMonsterAction __instance) {
            AllInOneBag.INSTANCE.onSpawnMonster((AbstractMonster) __instance.target);
        }
    }

    @SpirePatch2(clz = SummonGremlinAction.class, method = SpirePatch.CONSTRUCTOR)
    public static class GremlinHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(AbstractMonster ___m) {
            AllInOneBag.INSTANCE.onSpawnMonster(___m);
        }
    }

    @SpirePatch2(clz = Darkling.class, method = "takeTurn")
    public static class DarklingHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(Darkling __instance) {
            AllInOneBag.INSTANCE.onSpawnMonster(__instance);
        }
    }
}
