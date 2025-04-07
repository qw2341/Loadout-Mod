package loadout.patches.blightfixes;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;

import javassist.CannotCompileException;
import javassist.expr.ExprEditor;

@SpirePatches({@SpirePatch(clz = AbstractDungeon.class, method = "generateMap"),
@SpirePatch(clz = MonsterRoomElite.class, method = "dropReward"),
        @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {String.class, String.class, int.class, float.class, float.class, float.class, float.class, String.class, float.class, float.class, boolean.class}),
@SpirePatch(clz = AbstractMonster.class, method = "setHp", paramtypez = {int.class, int.class}),
@SpirePatch(clz = AbstractMonster.class, method = "calculateDamage", paramtypez = {int.class}),
@SpirePatch(clz = AbstractCreature.class, method = "increaseMaxHp", paramtypez = {int.class, boolean.class}),
        @SpirePatch(clz = AbstractCreature.class, method = "heal", paramtypez = {int.class, boolean.class}),
@SpirePatch(clz = DamageInfo.class, method = "applyPowers", paramtypez = {AbstractCreature.class, AbstractCreature.class})})
public class BlightFixes {
    public static ExprEditor Instrument()
    {
        return new ExprEditor() {
            public void edit(javassist.expr.FieldAccess m) throws CannotCompileException {
                if (m.getClassName().equals(Settings.class.getName()) && m.getFieldName().equals("isEndless")) {
                    m.replace("{$_ = com.megacrit.cardcrawl.dungeons.AbstractDungeon.player != null;}");
                }
            }
        };
    }
}
