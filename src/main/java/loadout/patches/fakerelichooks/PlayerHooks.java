package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import loadout.relics.AllInOneBag;

public class PlayerHooks {
    @SpirePatch2(clz = AbstractPlayer.class, method = "applyStartOfCombatLogic")
    public static class AtBattleStart {
        @SpirePrefixPatch
        public static void Prefix() {
            AllInOneBag.INSTANCE.atBattleStart();
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "applyStartOfTurnRelics")
    public static class AtTurnStart {
        @SpirePrefixPatch
        public static void Prefix() {
            AllInOneBag.INSTANCE.atTurnStart();
        }
    }

    @SpirePatch2(clz = CardGroup.class, method = "refreshHandLayout")
    public static class OnRefreshHand {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert() {
            AllInOneBag.INSTANCE.onRefreshHand();
        }
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "damage", paramtypez = {DamageInfo.class})
    public static class OnDamageChangeAmount {
        @SpireInsertPatch(locator = ODCALocator.class, localvars = "damageAmount")
        public static void Insert(DamageInfo info, int damageAmount) {
            AllInOneBag.INSTANCE.onAttackedToChangeDamage(info,damageAmount);
        }
    }
    private static class ODCALocator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher matcher = new Matcher.MethodCallMatcher(AbstractRelic.class, "onAttackedToChangeDamage");
            int[] ret = LineFinder.findInOrder(ctBehavior, matcher);
            ret[0]--;
            return ret;
        }
    }

    @SpirePatch(
            clz=AbstractPlayer.class,
            method="damage",
            paramtypez={DamageInfo.class}
    )
    public static class OnPlayerDeath {
        @SpireInsertPatch(
                locator=IsDeadLocator.class
        )
        public static SpireReturn<Void> Insert(AbstractPlayer __instance, DamageInfo info){
            if(!AllInOneBag.INSTANCE.onPlayerDeath(__instance, info)) return SpireReturn.Return(null);
            return SpireReturn.Continue();
        }
    }

    private static class IsDeadLocator extends SpireInsertLocator
    {
        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception
        {
            Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "isDead");

            return LineFinder.findInOrder(ctBehavior, finalMatcher);
        }
    }


}
