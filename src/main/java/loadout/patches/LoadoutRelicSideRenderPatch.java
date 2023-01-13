package loadout.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import loadout.helper.LoadoutRelicHelper;

public class LoadoutRelicSideRenderPatch {
//    @SpirePatch(clz = AbstractPlayer.class, method = "renderRelics", paramtypez = {SpriteBatch.class})
//    public static class PlayerRelicRenderPatch {
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
//    }
}
