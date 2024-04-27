package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.ending.SpireShield;
import com.megacrit.cardcrawl.monsters.ending.SpireSpear;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import javassist.CtBehavior;
import loadout.relics.TildeKey;

public class MorphPatches {

    public static boolean playerFilp = false;

    @SpirePatch2(clz = AbstractPlayer.class, method = "render")
    public static class RenderFlipPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractPlayer __instance) {
            playerFilp = __instance.flipHorizontal;
//            if(TildeKey.isNotFightingSurrounded()) {
                if(TildeKey.shouldFlip())__instance.flipHorizontal = !__instance.flipHorizontal;
//            }

        }

        @SpirePostfixPatch
        public static void Postfix(AbstractPlayer __instance) {
            __instance.flipHorizontal = playerFilp;
        }
    }

//TODO: Fix black morphs when morphing to another player

//    @SpirePatch2(clz = ConfirmPopup.class, method = "yesButtonEffect")
//    public static class MidRunExitPatch {
//        @SpireInsertPatch(locator = MidRunLocator.class)
//        public static void Insert() {
//            TildeKey.restorePlayerMorph();
//        }
//    }
//
//    private static class MidRunLocator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctBehavior) throws Exception {
//            Matcher m = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "stance");
//            return LineFinder.findInOrder(ctBehavior, m);
//        }
//    }

//    @SpirePatches2({@SpirePatch2(clz = AbstractPlayer.class, method = "playCard"),@SpirePatch2(clz = PotionPopUp.class, method = "updateTargetMode")})
//    public static class SurroundPatch {
//        @SpireInsertPatch(locator = PlayerSurroundLocator.class)
//        public static void Insert(AbstractPlayer __instance) {
//            if(TildeKey.shouldFlip()) __instance.flipHorizontal = !__instance.flipHorizontal;
//        }
//    }
//    private static class PlayerSurroundLocator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctBehavior) throws Exception {
//            Matcher m = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "flipHorizontal");
//            int[] ret = LineFinder.findInOrder(ctBehavior, m);
//            ret[0]++;
//            return ret;
//        }
//    }
//
//    @SpirePatches2({@SpirePatch2(clz = SpireShield.class, method = "die"),@SpirePatch2(clz = SpireSpear.class, method = "die")})
//    public static class SpireSSFilpsOnDeathPatch{
//        @SpireInsertPatch(locator = SSDeathFilpLocator.class)
//        public static void Insert() {
//            if(TildeKey.shouldFlip()) AbstractDungeon.player.flipHorizontal = !AbstractDungeon.player.flipHorizontal;
//        }
//    }
//
//    private static class SSDeathFilpLocator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctBehavior) throws Exception {
//            Matcher m = new Matcher.MethodCallMatcher(GameActionManager.class, "addToBottom");
//            return LineFinder.findInOrder(ctBehavior, m);
//        }
//    }

    //NOPE because refreshHandLayout in AbstractPlayer is hard coded smh

//    @SpirePatch2(clz = AbstractMonster.class, method = "applyBackAttack")
//    public static class AbstractMonsterApplyBackAttackPatch {
//        @SpirePostfixPatch
//        public static boolean Postfix(boolean __result, AbstractMonster __instance) {
//            if(!TildeKey.shouldFlip()) return __result;
//            return AbstractDungeon.player.hasPower("Surrounded") && (AbstractDungeon.player.flipHorizontal && AbstractDungeon.player.drawX > __instance.drawX || !AbstractDungeon.player.flipHorizontal && AbstractDungeon.player.drawX < __instance.drawX);
//        }
//    }
}
