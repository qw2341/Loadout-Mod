package loadout.patches.cardmods;

import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.PersistFields;
import com.evacipated.cardcrawl.mod.stslib.patches.PersistPatch;
import com.evacipated.cardcrawl.mod.stslib.powers.interfaces.OnPersistPower;
import com.evacipated.cardcrawl.mod.stslib.relics.OnPersistRelic;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import javassist.CtBehavior;
import loadout.cardmods.StickyMod;
import loadout.util.Wiz;

import java.util.Iterator;

public class StickModPatches {
    /**
     * Credit to Stslib Persist Patch
     */
    @SpirePatch2(
            clz = UseCardAction.class,
            method = "update"
    )
    public static class OnPlay {
        public OnPlay() {
        }

        @SpireInsertPatch(
                locator = OnPlay.Locator1.class
        )
        public static void Insert1(AbstractCard ___targetCard) {
            if (CardModifierManager.hasModifier(___targetCard, StickyMod.ID)) {
                ___targetCard.returnToHand = true;
            }

        }

        @SpireInsertPatch(
                locator = OnPlay.Locator2.class
        )
        public static void Insert2(AbstractCard ___targetCard) {
            if (CardModifierManager.hasModifier(___targetCard, StickyMod.ID)) {
                ___targetCard.returnToHand = false;
            }

        }

        private static class Locator2 extends SpireInsertLocator {
            private Locator2() {
            }

            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "exhaustOnUseOnce");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }

        private static class Locator1 extends SpireInsertLocator {
            private Locator1() {
            }

            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "freeToPlayOnce");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch2(clz = CardGroup.class, method = "moveToDiscardPile")
    public static class MoveToDiscard {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(CardGroup __instance, AbstractCard c) {
            if(CardModifierManager.hasModifier(c , StickyMod.ID)) {
                if(__instance == Wiz.adp().hand) {
                    c.flash();
                    return SpireReturn.Return();
                }
            }

            return SpireReturn.Continue();
        }
    }
}
