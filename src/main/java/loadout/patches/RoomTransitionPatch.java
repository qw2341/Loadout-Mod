package loadout.patches;

import com.evacipated.cardcrawl.modthespire.finders.InOrderFinder;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.scenes.AbstractScene;
import javassist.CtBehavior;
import loadout.LoadoutMod;
import loadout.relics.TildeKey;

import java.util.Optional;

//@SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = {SaveFile.class})
public class RoomTransitionPatch {
//    @SpireInsertPatch(locator = Locator.class)
//    public static void Insert(AbstractDungeon __instance, SaveFile saveFile) {
//        LoadoutMod.logger.info("Transitioning, ...");
//        Optional.ofNullable(AbstractDungeon.currMapNode)
//                .map(MapRoomNode::getRoom)
//                .ifPresent(room -> {
//                    LoadoutMod.logger.info("...flipping player in {}", room.getClass().getSimpleName());
//                    if((room instanceof MonsterRoom || room instanceof EventRoom || room instanceof ShopRoom) && AbstractDungeon.player != null) {
//                        TildeKey.flipPlayer();
//                    }
//                });
//    }
//
//    private static class Locator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctBehavior) throws Exception {
//            Matcher m = new Matcher.MethodCallMatcher(AbstractScene.class, "nextRoom");
//            return LineFinder.findInOrder(ctBehavior, m);
//        }
//    }

//    @SpireInsertPatch(locator = SSLocator.class)
//    public static void SwordAndShieldInsert(AbstractDungeon __instance, SaveFile saveFile) {
//        LoadoutMod.logger.info("Transitioning to Sword and Shield, flipping player");
//        TildeKey.flipPlayer();
//    }
//
//    private static class SSLocator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctBehavior) throws Exception {
//            Matcher.MethodCallMatcher m = new Matcher.MethodCallMatcher(AbstractPlayer.class, "movePosition");
//            return LineFinder.findInOrder(ctBehavior, m);
//        }
//    }
}
