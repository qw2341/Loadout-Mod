package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.screens.options.ConfirmPopup;
import loadout.LoadoutMod;
import loadout.relics.TildeKey;

@SpirePatch(clz = ConfirmPopup.class, method = "abandonRunFromMainMenu", paramtypez = {AbstractPlayer.class})
public class AbandonRunPatch {
    @SpirePostfixPatch
    public static void PostFix(ConfirmPopup __instance, AbstractPlayer player) {
        TildeKey.resetPlayerMorph();
    }
}
