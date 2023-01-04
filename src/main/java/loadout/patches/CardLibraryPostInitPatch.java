package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.savables.CardModifications;

@SpirePatch(clz = CardLibrary.class, method = "initialize")
public class CardLibraryPostInitPatch {
    @SpirePostfixPatch
    public static void Postfix() {
        CardModifications.modifyCards();
    }

}
