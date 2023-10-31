package loadout.patches;

import basemod.abstracts.CustomMonster;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import loadout.LoadoutMod;
import loadout.ui.CreatureManipulationPanel;
import loadout.uiElements.CreatureManipulationButton;

public class AbstractCreaturePatch {
    @SpirePatch(clz = AbstractCreature.class, method = SpirePatch.CLASS)
    public static class PanelField {
        public static SpireField<CreatureManipulationPanel> manipulationPanel = new SpireField<>(() -> null);
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
            if(__instance.hb.hovered && InputHelper.justReleasedClickRight && !__instance.isDeadOrEscaped()) {
                panel.isHidden = !panel.isHidden;
                //LoadoutMod.logger.info("Panel is now: {}", panel.isHidden);
            }
            if(__instance.isDeadOrEscaped()) panel.isHidden = true;

            panel.update();
        }
    }
}