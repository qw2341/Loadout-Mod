package loadout.patches;

import basemod.ReflectionHacks;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.localization.CardStrings;
import javassist.CtBehavior;
import loadout.LoadoutMod;
import loadout.cardmods.InfiniteUpgradeMod;

import java.lang.reflect.InvocationTargetException;

import static loadout.LoadoutMod.logger;

@SpirePatch(clz = AbstractCard.class, method = "upgradeName")
public class InfUpgradePatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void Insert(AbstractCard __instance) {
        if(CardModifierManager.hasModifier(__instance, InfiniteUpgradeMod.ID)) {
            try {
                logger.info("upgrading " + __instance.cardID + " with upgradeTimes: " + __instance.timesUpgraded);
                __instance.name = ((CardStrings) ReflectionHacks.getPrivateStatic(__instance.getClass(), "cardStrings")).NAME + "+" + __instance.timesUpgraded;
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Failed to infinitely upgrade " + __instance.cardID + " !");
            }

        }
    }

    @SpirePostfixPatch
    public static void Postfix(AbstractCard __instance) {
        if(CardModifierManager.hasModifier(__instance, InfiniteUpgradeMod.ID)) {
            __instance.upgraded = false;
            logger.info("Setting " + __instance.cardID + " upgraded to " + __instance.upgraded);

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
        if(CardModifierManager.hasModifier(card,InfiniteUpgradeMod.ID)) {
            try {
                card.name = ((CardStrings) ReflectionHacks.getPrivateStatic(card.getClass(), "cardStrings")).NAME + "+" + card.timesUpgraded;
                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
                card.upgraded = false;
            } catch (Exception e) {
                e.printStackTrace();
                LoadoutMod.logger.info("Failed to upgrade the name for " + card.cardID);
            }

        }
    }
}
