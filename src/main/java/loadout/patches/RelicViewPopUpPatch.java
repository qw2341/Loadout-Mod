package loadout.patches;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.SingleRelicViewPopup;
import loadout.screens.RelicViewHeader;

import java.util.ArrayList;

public class RelicViewPopUpPatch {
    public static RelicViewHeader header = new RelicViewHeader();
@SpirePatch(clz = SingleRelicViewPopup.class, method = "update")
    public static class UpdatePatch {
        public static void Prefix(SingleRelicViewPopup __instance) {
            if(__instance.isOpen) {
                header.update();
            }
        }
    }

    @SpirePatch(clz = SingleRelicViewPopup.class, method = "render")
    public static class RenderPatch {
        public static void Postfix(SingleRelicViewPopup __instance, SpriteBatch sb) {
            if(__instance.isOpen) {
                header.render(sb);
            }
        }
    }

    @SpirePatch(clz = SingleRelicViewPopup.class, method = "open", paramtypez = {AbstractRelic.class, ArrayList.class})
    @SpirePatch(clz = SingleRelicViewPopup.class, method = "open", paramtypez = {AbstractRelic.class})// 2 methods named open
    public static class OpenPatch {
        public static void Postfix(SingleRelicViewPopup __instance, AbstractRelic relic) {
            header.onOpen(relic);
        }
    }
}
