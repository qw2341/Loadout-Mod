package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import loadout.relics.TildeKey;

public class MapRoomNodePatch {

    @SpirePatch(clz = MapRoomNode.class, method = "isConnectedTo", paramtypez = {MapRoomNode.class})
    public static class ConnectionPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(MapRoomNode __Instance, MapRoomNode node) {
            if(TildeKey.canGoToAnyRooms) return SpireReturn.Return(true);
            else return SpireReturn.Continue();
        }
    }
}
