package loadout.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.relics.PowerGiver;
import loadout.relics.TildeKey;

public class AbstractPlayerPatch {

    @SpirePatch(
            clz= AbstractPlayer.class,
            method="damage",
            paramtypez = {
                    DamageInfo.class
            }
    )
    public static class DamageCheckPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractPlayer __instance, DamageInfo info) {
            if(TildeKey.isGodMode) {
                int damageAmount = 0;

                __instance.currentHealth = __instance.maxHealth;
                if (info.owner == __instance) {
                    for (AbstractRelic r : __instance.relics) {
                        r.onAttack(info, damageAmount, __instance);
                    }
                }
                if (info.owner != null) {
                    for (AbstractPower p : info.owner.powers) {
                        p.onAttack(info, damageAmount, __instance);
                    }
                    for (AbstractPower p : __instance.powers) {
                        damageAmount = p.onAttacked(info, damageAmount);
                    }
                    for (AbstractRelic r : __instance.relics) {
                        damageAmount = r.onAttacked(info, damageAmount);
                    }
                } else {
                    LoadoutMod.logger.info("NO OWNER, DON'T TRIGGER POWERS");
                }
                return SpireReturn.Return();
            }

            return SpireReturn.Continue();
        }
    }

    @SpirePatch(clz = AbstractPlayer.class,
    method = "applyPreCombatLogic")
    public static class PreCombatHookPatch {
        public static void Prefix(AbstractPlayer __instance) {
            if(__instance.hasRelic(PowerGiver.ID)) ((PowerGiver)(__instance.getRelic(PowerGiver.ID))).battleStartPreDraw();

        }
    }
}
