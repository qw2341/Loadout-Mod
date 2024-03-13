package loadout.patches;

import com.evacipated.cardcrawl.modthespire.finders.InOrderFinder;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CtBehavior;
import loadout.LoadoutMod;
import loadout.relics.TildeKey;

@SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = {SaveFile.class})
public class RoomTransitionPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void Insert(AbstractDungeon __instance, SaveFile saveFile) {
        LoadoutMod.logger.info("Transitioning, flipping player");
        TildeKey.flipPlayer();
    }

    private static class Locator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher.FieldAccessMatcher m = new Matcher.FieldAccessMatcher(AbstractPlayer.class, "flipHorizontal");
            int[] ret =  LineFinder.findAllInOrder(ctBehavior, m);
            ret[0] ++;
            return ret;
        }
    }
}
