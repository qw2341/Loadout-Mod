package loadout.patches.othermods;

import CardAugments.cardmods.AbstractAugment;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.savables.CardModifications;

import java.util.function.Predicate;

public class CardAugmentsPatch {
    @SpirePatch(clz = AbstractAugment.class, method = "cardCheck", paramtypez = {AbstractCard.class, Predicate.class}, requiredModId = "CardAugments", optional = true)
    public static class CardCheckPatch {
        @SpirePrefixPatch
        public static SpireReturn<Boolean> Prefix(AbstractCard card, Predicate<AbstractCard> p) {
            if(CardModifications.cardMap.containsKey(card.cardID)) return SpireReturn.Return(Boolean.TRUE);
            else return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractAugment.class, method = "makeNewInstance", paramtypez = {AbstractCard.class}, requiredModId = "CardAugments", optional = true)
    public static class MakeNewInstancePatch {
        @SpirePrefixPatch
        public static SpireReturn<AbstractCard> Prefix(AbstractCard card) {
            if(CardModifications.cardMap.containsKey(card.cardID)) return SpireReturn.Return(CardModifications.getUnmoddedCopy(card));
            else return SpireReturn.Continue();
        }
    }
}
