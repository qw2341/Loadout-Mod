package loadout.patches;

import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import loadout.LoadoutMod;
import loadout.ui.CreatureManipulationPanel;
import loadout.uiElements.CreatureManipulationButton;

public class AbstractCreaturePatch {
    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CLASS)
    public static class PanelField {
        public static SpireField<CreatureManipulationPanel> manipulationPanel = new SpireField<>(() -> null);

        public static SpireField<Boolean> isCurrentHPLocked = new SpireField<>(() -> Boolean.FALSE);
        public static SpireField<Integer> currentHPLockAmount = new SpireField<>(() -> 0);
        public static SpireField<Boolean> isMaxHPLocked = new SpireField<>(() -> Boolean.FALSE);
        public static SpireField<Integer> maxHPLockAmount = new SpireField<>(() -> 0);
        public static SpireField<Boolean> isBlockLocked = new SpireField<>(() -> Boolean.FALSE);
        public static SpireField<Integer> blockLockAmount = new SpireField<>(() -> 0);
    }


    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CONSTRUCTOR)
    public static class CtorPatch {
        @SpirePostfixPatch
        public static void PostFix(AbstractCreature __instance) {
            PanelField.manipulationPanel.set(__instance, new CreatureManipulationPanel(__instance));
        }
    }
    @SpirePatches({@SpirePatch(clz = AbstractMonster.class, method = "render", paramtypez = {SpriteBatch.class}),
            @SpirePatch(clz = AbstractPlayer.class, method = "render", paramtypez = {SpriteBatch.class}),
            @SpirePatch(clz = CustomMonster.class, method = "render", paramtypez = {SpriteBatch.class})})
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void PostFix(AbstractCreature __instance, SpriteBatch sb) {
            PanelField.manipulationPanel.get(__instance).render(sb);
        }
    }

    @SpirePatches({@SpirePatch(clz = AbstractMonster.class, method = "update"),
            @SpirePatch(clz = AbstractPlayer.class, method = "update")})
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void PostFix(AbstractCreature __instance) {
            CreatureManipulationPanel panel = PanelField.manipulationPanel.get(__instance);
            if(__instance.hb.hovered && InputHelper.justClickedRight && !InputHelper.isMouseDown && !__instance.isDead && !__instance.isEscaping) {
                panel.isHidden = !panel.isHidden;
                //LoadoutMod.logger.info("Panel is now: {}", panel.isHidden);
            }
            if(__instance.isDead || __instance.isEscaping ||
                    AbstractDungeon.player.cardInUse != null ||
                    AbstractDungeon.player.isDraggingCard ||
                    AbstractDungeon.player.inSingleTargetMode)
                panel.isHidden = true;

            if(panel.isHidden) panel.resetAllButtons();

            panel.update();

            if(PanelField.isCurrentHPLocked.get(__instance)) {
                __instance.currentHealth = PanelField.currentHPLockAmount.get(__instance);
            }
            if(PanelField.isMaxHPLocked.get(__instance)) {
                __instance.maxHealth = PanelField.maxHPLockAmount.get(__instance);
            }
            if(PanelField.isBlockLocked.get(__instance)) {
                __instance.currentBlock = PanelField.blockLockAmount.get(__instance);
            }
        }
    }
}
