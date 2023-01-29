package loadout.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.FlyingOrbEffect;

public class LifestealAction extends AbstractGameAction {


    public LifestealAction(AbstractCreature source, AbstractCreature target) {
        this.target = target;
        this.source = source;
        this.actionType = ActionType.DAMAGE;
        this.damageType = DamageInfo.DamageType.NORMAL;
        this.duration = Settings.ACTION_DUR_FAST;
    }

    @Override
    public void update() {
        tickDuration();

        if (this.isDone) {

            int healAmount = 0;
                    if (target != null && target.lastDamageTaken > 0) {
                        healAmount += target.lastDamageTaken;
                        for (int j = 0; j < target.lastDamageTaken / 2 && j < 10; j++) {
                            addToBot((AbstractGameAction)new VFXAction((AbstractGameEffect)new FlyingOrbEffect(target.hb.cX, target.hb.cY)));
                        }
                    }



            if (healAmount > 0) {
                if (!Settings.FAST_MODE) {
                    addToBot((AbstractGameAction)new WaitAction(0.3F));
                }
                addToBot((AbstractGameAction)new HealAction(this.source, this.source, healAmount));
            }

            if ((AbstractDungeon.getCurrRoom()).monsters.areMonstersBasicallyDead()) {
                AbstractDungeon.actionManager.clearPostCombatActions();
            }
            addToTop((AbstractGameAction)new WaitAction(0.1F));
        }
    }
}
