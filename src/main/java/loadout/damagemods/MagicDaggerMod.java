package loadout.damagemods;

import java.util.Iterator;
import java.util.UUID;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GetAllInBattleInstances;

public class MagicDaggerMod extends AbstractOnKillMod{
    public MagicDaggerMod(String cardmodID) {
        super(cardmodID);
    }

    @Override
    public void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount) {
        Iterator<AbstractCard> var1 = AbstractDungeon.player.masterDeck.group.iterator();
        AbstractCard c;
        UUID uuid = getCard(info).uuid;
        while(var1.hasNext()) {
            c = (AbstractCard)var1.next();
            if (c.uuid.equals(uuid)) {
                c.baseMagicNumber += amount;
                c.applyPowers();
                c.isMagicNumberModified = false;
            }
        }

        for(var1 = GetAllInBattleInstances.get(uuid).iterator(); var1.hasNext(); ) {
            c = (AbstractCard)var1.next();
            c.baseMagicNumber += amount;
            c.applyPowers();
        }
    }

    @Override
    public AbstractDamageModifier makeCopy() {
        return new MagicDaggerMod(cardmodID);
    }
}
