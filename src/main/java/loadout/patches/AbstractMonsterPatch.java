package loadout.patches;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import loadout.relics.TildeKey;

public class AbstractMonsterPatch {
    @SpirePatch2(clz = AbstractMonster.class, method = "calculateDamage")
    public static class DamageCalculationMod {
        @SpireInsertPatch(locator = Locator.class, localvars = {"tmp"})
        public static void Insert(@ByRef float[] tmp) {
            tmp[0] *= TildeKey.enemyAttackMult/100.0f;
        }


        private static class Locator
                extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(MathUtils.class, "floor");

                return LineFinder.findInOrder(ctMethodToPatch, (Matcher)methodCallMatcher);
            }
        }
    }

    @SpirePatch2(clz = DamageInfo.class, method = "applyPowers", paramtypez = {AbstractCreature.class, AbstractCreature.class})
    public static class DamageInfoMod {
        @SpireInsertPatch(locator = Locator.class, localvars = {"tmp"})
        public static void Insert(AbstractCreature owner, AbstractCreature target, @ByRef float[] tmp) {
            if(!owner.isPlayer) tmp[0] *= TildeKey.enemyAttackMult/100.0f;
        }


        private static class Locator
                extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(MathUtils.class, "floor");

                return LineFinder.findInOrder(ctMethodToPatch, (Matcher)methodCallMatcher);
            }
        }
    }


}
