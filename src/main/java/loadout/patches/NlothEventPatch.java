package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.events.shrines.Nloth;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Circlet;
import javassist.CtBehavior;
import loadout.helper.LoadoutRelicHelper;

import java.util.ArrayList;
import java.util.Collections;

@SpirePatch(clz = Nloth.class, method = SpirePatch.CONSTRUCTOR)
public class NlothEventPatch {

    @SpireInsertPatch(locator = Locator.class, localvars = {"relics"})
    public static void Insert(Nloth __instance, ArrayList<AbstractRelic> relics) {
        relics.removeIf(r-> LoadoutRelicHelper.loadoutRelicIds.contains(r.relicId));
        if(relics.size() == 0) {
            relics.add(new Circlet());
        }
        if(relics.size() == 1) {
            relics.add(relics.get(0));
        }
    }

    private static class Locator
            extends SpireInsertLocator
    {
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(Collections.class, "shuffle");
            return LineFinder.findInOrder(ctMethodToPatch, (Matcher)methodCallMatcher);
        }
    }
}
