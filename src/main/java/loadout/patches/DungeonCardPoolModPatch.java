package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;

import java.util.Collections;

//@SpirePatch(clz = AbstractDungeon.class, method = "initializeCardPools")
public class DungeonCardPoolModPatch {
//    @SpireInsertPatch( locator = Locator.class)
//    public static void Insert() {
//
//    }
//
//    public static class Locator extends SpireInsertLocator {
//
//        @Override
//        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
//            Matcher.NewExprMatcher newExprMatcher = new Matcher.NewExprMatcher(CardGroup.class);
//            return LineFinder.findInOrder(ctMethodToPatch, (Matcher)newExprMatcher);
//        }
//    }
}
