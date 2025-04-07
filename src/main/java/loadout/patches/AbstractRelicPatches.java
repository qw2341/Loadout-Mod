package loadout.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import loadout.relics.TildeKey;

public class AbstractRelicPatches {
    @SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CLASS)
    public static class RelicCounterFields {
        public static SpireField<Boolean> isCounterLocked = new SpireField<Boolean>(() -> Boolean.FALSE);
        public static SpireField<Integer> counterLockAmount = new SpireField<Integer>(() -> -1);
    }

    @SpirePatch2(clz = AbstractRelic.class, method = "update")
    public static class RelicUpdatePatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractRelic __instance) {
            if(__instance.isObtained) {
                if(RelicCounterFields.isCounterLocked.get(__instance)){
                    __instance.counter = RelicCounterFields.counterLockAmount.get(__instance);
                }

                if(TildeKey.enableRelicCounterEdit) {
                    __instance.hb.update();
                    if(__instance.hb.clicked) {
                        __instance.hb.clicked = false;
                        RelicCounterFields.isCounterLocked.set(__instance, !RelicCounterFields.isCounterLocked.get(__instance));
                        if(RelicCounterFields.isCounterLocked.get(__instance)) {
                            RelicCounterFields.counterLockAmount.set(__instance, __instance.counter);
                        }
                    }
                }
            }
        }
    }

    @SpirePatch2(clz = AbstractRelic.class, method = "renderCounter")
    public static class RelicCounterRenderPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractRelic __instance, SpriteBatch sb, boolean inTopPanel, float ___rotation) {
            if(RelicCounterFields.isCounterLocked.get(__instance)) {
                sb.draw(ImageMaster.RELIC_LOCK, __instance.currentX - 36.0F, __instance.currentY - 36.0F, 64.0F, 64.0F, 128.0F, 128.0F, 0.3f, 0.3f, ___rotation, 0, 0, 128, 128, false, false);
            }
        }

//        public static ExprEditor Instrument() {
//            return new ExprEditor() {
//                @Override
//                public void edit(FieldAccess f) throws CannotCompileException {
//                    if(f.getClassName().equals(Color.class.getName())) {
//                        f.replace("$_ = ((boolean)"+ RelicCounterFields.class.getName() + ".isCounterLocked.get(this)) ? "+
//                                Color.class.getName()+".GOLD : " + Color.class.getName()+".WHITE; $proceed();");
//                    }
//                }
//            };
//        }
    }
}
