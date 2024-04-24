package loadout.patches;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import javassist.ClassPool;
import javassist.CtBehavior;
import loadout.LoadoutMod;
import loadout.util.CardClassPatcher;
import org.clapper.util.classutil.ClassFinder;

import java.net.URL;

@SpirePatch(
        clz= CardCrawlGame.class,
        method=SpirePatch.CONSTRUCTOR
)
public class AbstractCardDynamicPatch {

    public static void Raw(CtBehavior ctBehavior) {

        ClassPool pool = ctBehavior.getDeclaringClass().getClassPool();

        try{
            CardClassPatcher cardClassPatcher = new CardClassPatcher(Loader.STS_JAR, "StSCardPatchingThread", pool);
            cardClassPatcher.start();
        } catch  (Exception ignored) {
            LoadoutMod.logger.info("Something wrong occurred while dynamic patching basegame cards!");
        }


        for (ModInfo mi : Loader.MODINFOS) {
            try{
                URL url = mi.jarURL;
                CardClassPatcher cardClassPatcher = new CardClassPatcher(url, mi.ID + "CardPatchingThread", pool);
                cardClassPatcher.start();
            } catch (Exception ignored) {
                LoadoutMod.logger.info("Something wrong occurred while dynamic patching {} cards!", mi.ID);
            }
        }

    }
}
