package loadout.helper;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import loadout.LoadoutMod;
import loadout.relics.AllInOneBag;
import pinacolada.cards.base.PCLCustomCardSlot;
import pinacolada.ui.editor.card.PCLCustomCardEditScreen;

import java.util.List;
import java.util.Objects;

public class FabricateScreenController {

    public static PCLCustomCardEditScreen currentScreen;

    public static boolean isScreenUp = false;

    public static void openAddCardScreen(AbstractCard.CardColor cardColor) {
        isScreenUp = true;
        PCLCustomCardSlot slot = new PCLCustomCardSlot(cardColor);
        currentScreen = new PCLCustomCardEditScreen(slot);
        currentScreen.setOnSave(() -> {
                    AbstractCard newCard = slot.make();
                    PCLCustomCardSlot.getCards(cardColor).add(slot);
                    LoadoutMod.cardsToDisplay.add(newCard);
                    refreshCardPrinterCards();
                    ReflectionHacks.privateMethod(PCLCustomCardSlot.class, "commitBuilder").invoke(slot);
                    isScreenUp = false;
                });
    }

    public static void openEditCardScreen(AbstractCard card, CardGroup cg) {
        isScreenUp = true;
        PCLCustomCardSlot slot = PCLCustomCardSlot.get(card.cardID);

        if(slot == null) {
            isScreenUp = false;
            return;
        }

        currentScreen = new PCLCustomCardEditScreen(slot);
        currentScreen.setOnSave(() -> {
                    AbstractCard newCard = slot.make();
                    replaceCardInList(LoadoutMod.cardsToDisplay,card,newCard);
                    replaceCardInList(cg.group,card,newCard);

                    replaceSCardPopupCard(newCard);
                    ReflectionHacks.privateMethod(PCLCustomCardSlot.class, "commitBuilder").invoke(slot);
                    isScreenUp = false;
                });
    }

    public static void update() {
        if(currentScreen != null) {
            currentScreen.update();
        }
    }

    public static void render(SpriteBatch sb) {
        if(currentScreen != null) {
            currentScreen.render(sb);
        }
    }

    private static void refreshCardPrinterCards() {
        if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.player.hasRelic(AllInOneBag.ID))
            ((AllInOneBag)AbstractDungeon.player.getRelic(AllInOneBag.ID)).cardPrinter.selectScreen.updateFilters();
    }

    private static void replaceSCardPopupCard(AbstractCard card) {
        if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.player.hasRelic(AllInOneBag.ID))
            ((AllInOneBag)AbstractDungeon.player.getRelic(AllInOneBag.ID)).cardModifier.selectScreen.getSCardPopup().replaceCurrentCard(card);
    }

    private static boolean replaceCardInList(List<AbstractCard> cards, AbstractCard original, AbstractCard replacer) {
        int ci = cards.indexOf(original);
        if(ci < 0) {
            ci = 0;
            for(AbstractCard ac : cards) {
                if(ac.cardID.equals(original.cardID)) {
                    break;
                }
                ci++;
            }

            if(ci >= cards.size()) return false;

            cards.remove(ci);
        } else {
            cards.remove(original);
        }
        cards.add(ci,replacer);
        return true;
    }
}
