package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import javassist.CtBehavior;

public class PlayerRelicsLocator  extends SpireInsertLocator{
    @Override
    public int[] Locate(CtBehavior ctBehavior) throws Exception {
        Matcher matcher = new Matcher.FieldAccessMatcher(AbstractPlayer.class,"relics");
        return LineFinder.findInOrder(ctBehavior,matcher);
    }
}
