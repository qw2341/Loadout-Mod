package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import basemod.helpers.CardModifierManager;
import javassist.CtBehavior;
import loadout.cardmods.InevitableMod;

@SpirePatch(clz = CardGroup.class, method = "removeCard", paramtypez = {AbstractCard.class})
public class CardRemovalHookPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void Insert(CardGroup __instance, AbstractCard c) {
        if(CardModifierManager.hasModifier(c, InevitableMod.ID)) {
            AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        }
    }
    private static class Locator
            extends SpireInsertLocator
    {
        public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
            Matcher.MethodCallMatcher methodCallMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "onRemoveFromMasterDeck");
            return LineFinder.findInOrder(ctMethodToPatch, (Matcher)methodCallMatcher);
        }
    }
}
