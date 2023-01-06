package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;
import loadout.savables.CardModifications;

public class AddCardToHandPatch {
    @SpirePatch(clz = MakeTempCardInHandAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class, boolean.class})
    public static class MakeTempCardActionConPatch1 {
        @SpirePrefixPatch
        public static void Prefix(MakeTempCardInHandAction __instance, AbstractCard card, boolean isOtherCardInCenter) {
            CardModifications.modifyOnlyNumberIfExist(card);
        }
    }

    @SpirePatch(clz = MakeTempCardInHandAction.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.class, int.class})
    public static class MakeTempCardActionConPatch2 {
        @SpirePrefixPatch
        public static void Prefix(MakeTempCardInHandAction __instance, AbstractCard card, int amount) {
            CardModifications.modifyOnlyNumberIfExist(card);
        }
    }

}
