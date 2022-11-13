package loadout.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.MasterDeckViewScreen;
import loadout.screens.MDeckViewSortHeader;

public class MasterDeckScreenPatch {
    public static MDeckViewSortHeader mDeckViewSortHeader = null;

    @SpirePatch(clz = MasterDeckViewScreen.class, method = "<init>")
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void PostfixPatch() {
            if(loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader == null) loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader = new MDeckViewSortHeader();
        }
    }
    @SpirePatch(clz = MasterDeckViewScreen.class, method = "update")
    public static class UpdatePatch{
        @SpirePostfixPatch
        public static void PostfixPatch() {
            if(loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader != null) loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader.update();
        }
    }
    @SpirePatch(clz = MasterDeckViewScreen.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class RenderPatch{
        @SpirePostfixPatch
        public static void PostfixPatch(MasterDeckViewScreen __instance, SpriteBatch sb) {
            if(loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader != null) loadout.patches.MasterDeckScreenPatch.mDeckViewSortHeader.render(sb);
        }
    }

}
