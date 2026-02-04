package loadout.patches;

import static loadout.LoadoutMod.logger;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.localization.CardStrings;

import basemod.ReflectionHacks;
import basemod.helpers.CardModifierManager;
import javassist.CtBehavior;
import loadout.LoadoutMod;
import loadout.cardmods.InfiniteUpgradeMod;

@SpirePatch(clz = AbstractCard.class, method = "upgradeName")
public class InfUpgradePatch {
    public static boolean isInfUpgrade(AbstractCard card) {
        return CardModifierManager.hasModifier(card, InfiniteUpgradeMod.ID);
    }
    @SpireInsertPatch(locator = Locator.class)
    public static void Insert(AbstractCard __instance) {
        if(isInfUpgrade(__instance)) {
            try {
                //logger.info("upgrading " + __instance.cardID + " with upgradeTimes: " + __instance.timesUpgraded);
                __instance.name = __instance.originalName + "+" + __instance.timesUpgraded;
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Failed to infinitely upgrade " + __instance.cardID + " !");
            }

        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractCard __instance) {
        if(isInfUpgrade(__instance)) {
            __instance.upgraded = false;
            //logger.info("Setting " + __instance.cardID + " upgraded to " + __instance.upgraded);

        }

    }

    private static class Locator extends SpireInsertLocator {

        @Override
        public int[] Locate(CtBehavior ctBehavior) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class,"initializeTitle");
            return LineFinder.findAllInOrder(ctBehavior, finalMatcher);
        }
    }

    public static void changeCardName(AbstractCard card) {
        if(isInfUpgrade(card)) {
            try {
//                card.name = ((CardStrings) ReflectionHacks.getPrivateStatic(card.getClass(), "cardStrings")).NAME + "+" + card.timesUpgraded;
                card.name = card.originalName + "+" + card.timesUpgraded;
                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
                card.upgraded = false;
            } catch (Exception e) {
                e.printStackTrace();
                LoadoutMod.logger.info("Failed to upgrade the name for " + card.cardID);
            }

        }
    }
}
