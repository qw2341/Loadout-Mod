package loadout.patches.blightfixes;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import javassist.CtBehavior;
import javassist.bytecode.*;
import loadout.LoadoutMod;

import java.util.Arrays;

@SpirePatches({@SpirePatch(clz = AbstractDungeon.class, method = "generateMap"),
@SpirePatch(clz = MonsterRoomElite.class, method = "dropReward"),
        @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, String.class, int.class, float.class, float.class, float.class, float.class, String.class, float.class, float.class, boolean.class}),
@SpirePatch(clz = AbstractMonster.class, method = "setHp", paramtypez = {int.class, int.class}),
@SpirePatch(clz = AbstractMonster.class, method = "calculateDamage", paramtypez = {int.class}),
@SpirePatch(clz = AbstractCreature.class, method = "increaseMaxHp", paramtypez = {int.class, boolean.class}),
        @SpirePatch(clz = AbstractCreature.class, method = "heal", paramtypez = {int.class, boolean.class})})
public class BlightFixes {
    @SpireRawPatch
    public static void Raw(CtBehavior ctMethodToPatch) throws BadBytecode {
        CodeAttribute codeAttr = ctMethodToPatch.getMethodInfo().getCodeAttribute();
        CodeIterator iterator = codeAttr.iterator();
        ConstPool constPool = ctMethodToPatch.getDeclaringClass().getClassFile2().getConstPool();
        boolean success = false;
        while (iterator.hasNext()) {
            int pos = iterator.next();
            int opcode = iterator.byteAt(pos);

            if (opcode == Opcode.GETSTATIC) {
                int fieldIndex = iterator.u16bitAt(pos + 1);
                String fieldName = constPool.getFieldrefName(fieldIndex);
                if ("isEndless".equals(fieldName)) {

                    byte[] nops = new byte[6];  // Assuming 6 bytes total for GETSTATIC and IFEQ together.
                    Arrays.fill(nops, (byte) Opcode.NOP);
                    iterator.write(nops, pos);

                    success = true;
                    break;
                }
            }
        }

        if(success) {
            LoadoutMod.logger.info("{} successful patched!", ctMethodToPatch.getName());
            ctMethodToPatch.getMethodInfo().rebuildStackMap(ctMethodToPatch.getDeclaringClass().getClassPool());
        } else {
            LoadoutMod.logger.warn("{} patch failed!",  ctMethodToPatch.getName());
        }
    }
}
