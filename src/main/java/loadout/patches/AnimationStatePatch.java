package loadout.patches;


import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.AbstractCreature;
import javassist.CtBehavior;

public class AnimationStatePatch {
    public static Animation getAnimation(String animationName, AnimationState as) {
        Array<Animation> anim = as.getData().getSkeletonData().getAnimations();
        for (Animation an : anim) {
            if (an.getName().equalsIgnoreCase(animationName) || an.getName().contains(animationName) || an.getName().contains(animationName))
                return an;
        }
        return anim.get(0);
    }

    @SpirePatch(clz = AnimationState.class, method = "setAnimation", paramtypez = {int.class, String.class, boolean.class})
    public static class AntiCrashPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<AnimationState.TrackEntry> Insert(AnimationState __instance, int trackIndex, String animationName, boolean loop) {
            return SpireReturn.Return(__instance.setAnimation(trackIndex, getAnimation(animationName,__instance),loop));
        }

        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.NewExprMatcher newExprMatcher = new Matcher.NewExprMatcher(IllegalArgumentException.class);
                return LineFinder.findInOrder(ctBehavior, newExprMatcher);
            }
        }
    }

    @SpirePatch(clz = AnimationState.class, method = "addAnimation", paramtypez = {int.class, String.class, boolean.class, float.class})
    public static class AntiAddCrashPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static SpireReturn<AnimationState.TrackEntry> Insert(AnimationState __instance, int trackIndex, String animationName, boolean loop, float delay) {
            return SpireReturn.Return(__instance.addAnimation(trackIndex, getAnimation(animationName,__instance), loop, delay));
        }

        private static class Locator extends SpireInsertLocator {

            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.NewExprMatcher newExprMatcher = new Matcher.NewExprMatcher(IllegalArgumentException.class);
                return LineFinder.findInOrder(ctBehavior, newExprMatcher);
            }
        }
    }
}
