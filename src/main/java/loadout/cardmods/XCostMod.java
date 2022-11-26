package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.red.Corruption;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import loadout.LoadoutMod;
import loadout.actions.MultiUseAction;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class XCostMod extends AbstractCardModifier {

    public static String ID = LoadoutMod.makeID("XCostModifier");

    @Override
    public boolean isInherent(AbstractCard card) {
        return super.isInherent(card);
    }

//    @Override
//    public boolean shouldApply(AbstractCard card) {
//        return card.cost != -1;
//    }

    @Override
    public void onUse(AbstractCard card, AbstractCreature target, UseCardAction action) {
        int energyOnUse = EnergyPanel.getCurrentEnergy();

        if (!card.freeToPlayOnce
                && (!player.hasPower(Corruption.ID) || card.type != AbstractCard.CardType.SKILL)) {
            player.energy.use(energyOnUse);
        }

    }

    @Override
    public void onInitialApplication(AbstractCard card) {
        card.cost = -1;
        card.costForTurn = -1;
    }

    @Override
    public void onRemove(AbstractCard card) {
        card.cost = CardLibrary.getCard(card.cardID).cost;
        card.costForTurn = card.cost;
    }

    @Override
    public String identifier(AbstractCard card) {
        return ID;
    }

    @Override
    public AbstractCardModifier makeCopy() {
        return new XCostMod();
    }
}
