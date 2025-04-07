package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

import loadout.LoadoutMod;

public class ScreensPatch {
    @SpirePatch(clz = GridCardSelectScreen.class, method = "callOnOpen")
    public static class GridScreenOpenBool {
        @SpirePrefixPatch
        public static void Prefix() {
            LoadoutMod.isScreenUp = true;
        }
    }
//    @SpirePatch(clz = CombatRewardScreen.class, method = "open", paramtypez = {String.class})
//    public static class RewardScreenOpenBool {
//        @SpirePrefixPatch
//        public static void Prefix() {
//            LoadoutMod.isScreenUp = true;
//        }
//    }
//    @SpirePatch(clz = CombatRewardScreen.class, method = "setLabel")
//    public static class RewardScreenOpenLabelBool {
//        @SpirePrefixPatch
//        public static void Prefix() {
//            LoadoutMod.isScreenUp = true;
//        }
//    }
    @SpirePatch(clz = AbstractDungeon.class, method = "closeCurrentScreen")
    public static class GridScreenCloseBool {
        @SpirePrefixPatch
        public static void Prefix() {
            LoadoutMod.isScreenUp = false;
        }
    }
}
