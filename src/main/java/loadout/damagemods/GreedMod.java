package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.evacipated.cardcrawl.mod.stslib.damagemods.DamageModifierManager;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.GainPennyEffect;

/**
 * Code modified from Chimera Cards Mod
 */
public class GreedMod extends AbstractOnKillMod {

    @Override
    public void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount) {
        AbstractDungeon.player.gainGold(amount);
        for (int i = 0; i < amount; i++)
            AbstractDungeon.effectList.add(new GainPennyEffect(info.owner, target.hb.cX, target.hb.cY, info.owner.hb.cX, info.owner.hb.cY, true));
    }


    @Override
    public AbstractDamageModifier makeCopy() {
        return new GreedMod();
    }
}
