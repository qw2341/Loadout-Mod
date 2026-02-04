package loadout.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import loadout.util.Wiz;

/**
 * Used Hubris Zylophone Code
 */

public class MultiUseAction extends AbstractGameAction {
        private AbstractCard card;
        private AbstractPlayer player;
        private AbstractMonster monster;
        private int usesLeft;

    public MultiUseAction(AbstractCard card, AbstractPlayer player, AbstractMonster monster, int energyOnUse)
        {
            this.card = card;
            this.player = player;
            this.monster = monster;
            usesLeft = energyOnUse;
        }

        @Override
        public void update()
        {
            isDone = true;

            if (usesLeft > 0 && card.canUse(player, monster)) {
                for (int i=0; i < usesLeft; i++) {
                    card.use(player, monster);
                }
//                card.use(player, monster);
//                if (usesLeft > 1) {
//                    AbstractDungeon.actionManager.addToBottom(new MultiUseAction(card, player, monster, usesLeft - 1));
//                }
            }
        }
    }

