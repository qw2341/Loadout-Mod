package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import javassist.CtBehavior;

public class PlayerRelicsLocatorLast extends SpireInsertLocator{
    @Override
    public int[] Locate(CtBehavior ctBehavior) throws Exception {
        Matcher matcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class,"relics");
        int[] ret = LineFinder.findAllInOrder(ctBehavior,matcher);
        int[] ret1 = new int[1];
        ret1[0] = ret[ret.length - 1];
        return ret1;
    }
}
