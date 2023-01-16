package loadout.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GetAllInBattleInstances;

import java.util.Iterator;
import java.util.UUID;

public class GMOKAction extends AbstractGameAction {

    private int increaseAmount;
    private UUID uuid;

    public GMOKAction(AbstractCreature target, int increaseAmount, UUID uuid) {
        this.actionType = ActionType.DAMAGE;
        setValues(target, AbstractDungeon.player);
        this.duration = 0.1F;
        this.increaseAmount = increaseAmount;
        this.uuid = uuid;
    }

    @Override
    public void update() {
        if (this.duration == 0.1F && this.target != null) {
            if ((this.target.isDying || this.target.currentHealth <= 0) && !this.target.halfDead && !this.target.hasPower("Minion")) {
                Iterator<AbstractCard> var1 = AbstractDungeon.player.masterDeck.group.iterator();
                AbstractCard c;
                while(var1.hasNext()) {
                    c = (AbstractCard)var1.next();
                    if (c.uuid.equals(this.uuid)) {
                        int diff = c.magicNumber - c.baseMagicNumber;

                        c.baseMagicNumber += this.increaseAmount;
                        c.magicNumber = c.baseMagicNumber + diff;
                        c.isMagicNumberModified = false;
                        c.applyPowers();
                    }
                }

                for(var1 = GetAllInBattleInstances.get(this.uuid).iterator(); var1.hasNext(); ) {
                    c = (AbstractCard)var1.next();
                    int diff = c.magicNumber - c.baseMagicNumber;
                    c.baseMagicNumber += this.increaseAmount;
                    c.magicNumber = c.baseMagicNumber + diff;
                    c.applyPowers();
                }
            }
            if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                AbstractDungeon.actionManager.clearPostCombatActions();
            }
        }
        this.tickDuration();
    }
}
