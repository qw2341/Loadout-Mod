package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.OverlayMenu;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;

import static loadout.LoadoutMod.relicsToAdd;

public class OverlayMenuUpdatePatch {
    @SpirePatch(clz = OverlayMenu.class, method = "update")
    public static class UpdateFix {
        @SpirePrefixPatch
        public static void Prefix(OverlayMenu __instance) {
            LoadoutMod.modifyPlayerRelics();
        }
    }
}
