package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import loadout.relics.AllInOneBag;

@SpirePatches2({@SpirePatch2(clz = CardCrawlGame.class, method = "startOver"),
@SpirePatch2(clz = CardCrawlGame.class, method = "startOverButShowCredits")})
public class SaveAndQuitPatch {
    public static void Prefix() {
        AllInOneBag.INSTANCE.hideAllRelics();
    }
}
