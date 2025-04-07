package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.*;
import loadout.LoadoutMod;
import loadout.screens.GCardSelectScreen;

public class CardShredder extends AbstractCardScreenRelic {

    public static final String ID = LoadoutMod.makeID("CardShredder");
    private static Texture IMG = null;
    private static Texture OUTLINE = null;

    public CardShredder() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.CLINK, GCardSelectScreen.CardDisplayMode.DELETE);
    }

    @Override
    protected void openSelectScreen() {
        if (AbstractDungeon.player.masterDeck == null) {
            setIsSelectionScreenUp(false);
        } else {
            selectScreen.open(AbstractDungeon.player.masterDeck, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);
        }
    }

    @Override
    protected void doneSelectionLogics() {
        int count = selectScreen.selectedCards.size();
        float min = -15.0F * count;
        for (int i = 0; i < count; i++) {
            AbstractCard card = selectScreen.selectedCards.get(i);
            card.stopGlowing();
            removeCard(card);
        }
    }

    public void removeCard(AbstractCard card) {
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            removeFromPlayerInCombat(card);
            AbstractDungeon.effectList.add(new ExhaustCardEffect(card));
        } else {
            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card, Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
            AbstractDungeon.player.masterDeck.group.remove(card);
        }
        this.flash();
    }

    public void removeFromPlayerInCombat(AbstractCard card) {
        switch (this.selectScreen.currentPool) {

            case EXTERNAL:
                break;
            case MASTER_DECK:
                AbstractDungeon.player.masterDeck.group.remove(card);
                break;
            case DRAW:
                AbstractDungeon.player.drawPile.removeCard(card);
                break;
            case DISCARD:
                AbstractDungeon.player.discardPile.removeCard(card);
                break;
            case HAND:
                AbstractDungeon.player.hand.removeCard(card);
                AbstractDungeon.player.hand.refreshHandLayout();
                break;
            case EXHAUST:
                AbstractDungeon.player.exhaustPile.removeCard(card);
                break;
        }


    }

}
