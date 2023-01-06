package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.unique.DiscoveryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import javassist.CtBehavior;
import loadout.savables.CardModifications;

import java.util.ArrayList;
import java.util.Collections;

public class DiscoveryModPatch {
    @SpirePatch(clz = DiscoveryAction.class, method = "update")
    public static class UpdatePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = {"generatedCards"})
        public static void Insert(DiscoveryAction __instance, ArrayList<AbstractCard> generatedCards) {
            for(AbstractCard card : generatedCards) CardModifications.modifyOnlyNumberIfExist(card);
        }

        private static class Locator
                extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher.FieldAccessMatcher fieldAccessMatcher = new Matcher.FieldAccessMatcher(Settings.class, "ACTION_DUR_FAST");
                return LineFinder.findInOrder(ctMethodToPatch, (Matcher)fieldAccessMatcher);
            }
        }
    }

//    @SpirePatch(clz = DiscoveryAction.class, method = "update")
//    public static class UpdatePatch2 {
//        @SpireInsertPatch(locator = Locator.class, localvars = {"disCard", "disCard2"})
//        public static void Insert(DiscoveryAction __instance, AbstractCard disCard, AbstractCard disCard2) {
//            CardModifications.modifyIfExist(disCard);
//            CardModifications.modifyIfExist(disCard2);
//        }
//
//        private static class Locator
//                extends SpireInsertLocator
//        {
//            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
//                Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(AbstractCard.class,"setCostForTurn");
//                return LineFinder.findInOrder(ctMethodToPatch, (Matcher)methodCallMatcher);
//            }
//        }
//    }
}
