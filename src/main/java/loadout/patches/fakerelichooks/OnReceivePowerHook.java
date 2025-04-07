package loadout.patches.fakerelichooks;

import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;

import javassist.CtBehavior;
import loadout.relics.AllInOneBag;

/**
 * Copied from Stslib
 */
public class OnReceivePowerHook
{
    static SpireReturn<Void> CheckPower(AbstractGameAction action, AbstractCreature target, AbstractCreature source, float[] duration, AbstractPower powerToApply)
    {
        if (source != null) {
            if (source.isPlayer) {

//                for (AbstractRelic relic : AbstractDungeon.player.relics) {
//                    if (relic instanceof OnApplyPowerRelic) {
//                        // Allows changing the stackAmount
//                        action.amount = ((OnApplyPowerRelic) relic).onApplyPowerStacks(powerToApply, target, source, action.amount);
//                        // Allows negating the power
//                        boolean apply = ((OnApplyPowerRelic) relic).onApplyPower(powerToApply, target, source);
//                        if (!apply) {
//                            AbstractDungeon.actionManager.addToTop(new TextAboveCreatureAction(target, ApplyPowerAction.TEXT[0]));
//                            duration[0] -= Gdx.graphics.getDeltaTime();
//                            CardCrawlGame.sound.play("NULLIFY_SFX");
//                            return SpireReturn.Return(null);
//                        }
//                    }
//                }
            }
        }

        if (target != null) {

            if (target.isPlayer) {
                action.amount = AllInOneBag.INSTANCE.onReceivePowerStacks(powerToApply, source, action.amount);
                boolean apply = AllInOneBag.INSTANCE.onReceivePower(powerToApply, source);
                if (!apply) {
                    AbstractDungeon.actionManager.addToTop(new TextAboveCreatureAction(target, ApplyPowerAction.TEXT[0]));
                    duration[0] -= Gdx.graphics.getDeltaTime();
                    CardCrawlGame.sound.play("NULLIFY_SFX");
                    return SpireReturn.Return(null);
                }
            }

        }
        return SpireReturn.Continue();
    }

    @SpirePatch(
            clz=ApplyPowerAction.class,
            method="update"
    )
    public static class ApplyPower
    {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"duration", "powerToApply"}
        )
        public static SpireReturn<Void> Insert(ApplyPowerAction __instance, @ByRef float[] duration, AbstractPower powerToApply)
        {
            return CheckPower(__instance, __instance.target, __instance.source, duration, powerToApply);
        }

        private static class Locator extends SpireInsertLocator
        {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "hasRelic");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
