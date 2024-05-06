package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.FlyingOrbEffect;

public class LifestealDamageMod extends AbstractLoadoutDamageMod {

    @Override
    public void onLastDamageTakenUpdate(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target) {
        if(isValid(info,target)) {
            for (int j = 0; j < lastDamageTaken / 2 && j < 10; j++) {
                addToBot((AbstractGameAction)new VFXAction((AbstractGameEffect)new FlyingOrbEffect(target.hb.cX, target.hb.cY)));
            }
            addToBot(new HealAction(info.owner, info.owner, lastDamageTaken));
        }
    }

    @Override
    public AbstractDamageModifier makeCopy() {
        return new LifestealDamageMod();
    }
}
