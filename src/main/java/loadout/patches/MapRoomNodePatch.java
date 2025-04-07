package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

public class MapRoomNodePatch {

    @SpirePatch2(clz = MapRoomNode.class, method = "update")
    public static class NodeFakeDebugPatch {
        public static void Raw(CtBehavior ctMethodToPatch) throws CannotCompileException {
            ctMethodToPatch.instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    if(f.getFieldName().equals("isDebug")) {
                        f.replace("$_ = ($proceed() || loadout.relics.TildeKey.canGoToAnyRooms);");
                    }
                }

                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(AbstractRoom.RoomPhase.class.getName()) &&
                    m.getMethodName().equals("equals")) {
                        m.replace("$_ = ($proceed($$) || loadout.relics.TildeKey.canGoToAnyRooms);");
                    }
                }
            });
        }
    }

    @SpirePatch2(clz = DungeonMap.class, method = "update")
    public static class MapPatch {
        public static void Raw(CtBehavior ctMethodToPatch) throws CannotCompileException {
            ctMethodToPatch.instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {

                    if(f.getClassName().equals(AbstractRoom.class.getName())
                            && f.getFieldName().equals("phase")) {
                        f.replace("$_ = loadout.relics.TildeKey.canGoToAnyRooms ? com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMPLETE : $proceed();");
                    } else if (f.getFieldName().equals("isDebug")) {
                        f.replace("$_ = ($proceed() || loadout.relics.TildeKey.canGoToAnyRooms);");
                    }
                }

            });
        }
    }

}
