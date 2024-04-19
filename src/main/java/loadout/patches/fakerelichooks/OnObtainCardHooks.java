package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.events.shrines.NoteForYourself;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import loadout.relics.AllInOneBag;

public class OnObtainCardHooks {
    @SpirePatches2({@SpirePatch2(clz = ShowCardAndObtainEffect.class, method = "update"),
            @SpirePatch2(clz = FastCardObtainEffect.class, method = "update")})
    public static class UpdateHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(AbstractCard ___card) {
            AllInOneBag.INSTANCE.onObtainCard(___card);
        }
    }
    @SpirePatch2(clz = NoteForYourself.class, method = "buttonEffect")
    public static class NoteHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class)
        public static void Insert(AbstractCard ___obtainCard) {
            AllInOneBag.INSTANCE.onObtainCard(___obtainCard);
        }
    }
}
