package loadout.patches;

import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;


public class RelicPopUpPatch {
//    @SpirePatch(clz = AbstractRelic.class, method = "updateRelicPopupClick")
//    public static class PopUpPatch {
//        @SpirePrefixPatch
//        public static SpireReturn<Void> Prefix(AbstractRelic __instance) {
//            if(__instance.hb.hovered && InputHelper.justClickedLeft && IsInsideAnotherRelicField.isInsideAnother.get(__instance)) {
//                if(__instance instanceof ClickableRelic) {
//                    ((ClickableRelic) __instance).onRightClick();
//                }
//                return SpireReturn.Return();
//            } else return SpireReturn.Continue();
//        }
//    }
//    @SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CLASS)
//    public static class IsInsideAnotherRelicField {
//        public static SpireField<Boolean> isInsideAnother = new SpireField<Boolean>(() -> Boolean.FALSE);
//    }
//    @SpirePatch(clz = AbstractRelic.class, method = "renderTip")
//    public static class TipPatch {
//        @SpirePrefixPatch
//        public static SpireReturn<Void> Prefix(AbstractRelic __instance) {
//            if(IsInsideAnotherRelicField.isInsideAnother.get(__instance)) {
//                TipHelper.queuePowerTips((float)InputHelper.mX + 60.0F * Settings.scale, (float)InputHelper.mY - 30.0F * Settings.scale, __instance.tips);
//                return SpireReturn.Return();
//            } else return SpireReturn.Continue();
//        }
//    }
}
