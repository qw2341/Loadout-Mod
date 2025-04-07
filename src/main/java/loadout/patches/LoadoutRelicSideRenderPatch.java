package loadout.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import javassist.CtBehavior;
import loadout.relics.AllInOneBag;

public class LoadoutRelicSideRenderPatch {
    @SpirePatch(clz = AbstractPlayer.class, method = "renderRelics", paramtypez = {SpriteBatch.class})
    public static class PlayerRelicRenderPatch {
//        public static ExprEditor Instrument() {
//            return new ExprEditor() {
//                @Override
//                public void edit(FieldAccess f) throws CannotCompileException {
//                    if(f.getClassName().equals(AbstractRelic.class.getName()) && f.getFieldName().equals("relicPage")) {
//                        f.replace("if(" + LoadoutRelicHelper.class.getName() +
//                                ".loadoutRelicIds.contains((("+ AbstractRelic.class.getName() +")this.relics.get(i)).relicId)) {(("+ AbstractRelic.class.getName() +")this.relics.get(i)).renderInTopPanel(sb); } else { $_ = $proceed($$);}");
//                    }
//                }
//            };
//        }
        @SpireInsertPatch(locator = Locator.class, localvars = {"i"})
        public static void Insert(AbstractPlayer __instance, SpriteBatch sb, int i) {
            if(__instance.relics.get(i).relicId.equals(AllInOneBag.ID)) {
                __instance.relics.get(i).renderInTopPanel(sb);
            }
        }
        private static class Locator
                extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher.FieldAccessMatcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(AbstractRelic.class, "MAX_RELICS_PER_PAGE");
                return LineFinder.findInOrder(ctMethodToPatch, (Matcher)fieldAccessMatcher);
            }
        }
    }
}
