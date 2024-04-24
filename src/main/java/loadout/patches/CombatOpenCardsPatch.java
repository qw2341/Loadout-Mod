package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import loadout.savables.CardModifications;

import java.util.ArrayList;


public class CombatOpenCardsPatch {
//    @SpirePatch(clz = CardRewardScreen.class, method = "customCombatOpen", paramtypez = {ArrayList.class, String.class, boolean.class})
//    public static class CombatOpenPatch {
//        @SpirePrefixPatch
//        public static void Prefix(CardRewardScreen __instance, ArrayList<AbstractCard> choices, String text, boolean skippable) {
//            for (AbstractCard card: choices) CardModifications.modifyOnlyNumberIfExist(card);
//        }
//    }
//
//    @SpirePatch(clz = CardRewardScreen.class, method = "chooseOneOpen", paramtypez = {ArrayList.class})
//    public static class ChooseOnePatch {
//        @SpirePrefixPatch
//        public static void Prefix(CardRewardScreen __instance, ArrayList<AbstractCard> choices) {
//            for(AbstractCard card: choices) CardModifications.modifyOnlyNumberIfExist(card);
//        }
//
//    }

}
