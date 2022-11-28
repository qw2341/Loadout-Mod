package loadout.savables;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.patches.AbstractCardPatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializableDeck implements Serializable {

    public HashMap<String,Integer> simpleDeck;
    public ArrayList<SerializableCard> moddedDeck;

    public SerializableDeck() {
        simpleDeck = new HashMap<>();
        moddedDeck = new ArrayList<>();
    }

    public SerializableDeck(ArrayList<AbstractCard> deck) {
        this();
        for(AbstractCard ac : deck) {
            if(AbstractCardPatch.isCardModified(ac)){
                moddedDeck.add(SerializableCard.toSerializableCard(ac));
            } else {
                //unmodded
                simpleDeck.merge(ac.cardID,1,Integer::sum);
            }
        }
    }

    public static SerializableDeck toSerializableDeck(ArrayList<AbstractCard> deck) {
        return new SerializableDeck(deck);
    }

    public static ArrayList<AbstractCard> toAbstractCardDeck(SerializableDeck sDeck) {
        ArrayList<AbstractCard> deck = new ArrayList<>();
        for (String id: sDeck.simpleDeck.keySet()) {
            int numCards = sDeck.simpleDeck.get(id);
            if(CardLibrary.isACard(id)) {
                for (int i = 0; i< numCards; i++) {
                    deck.add(CardLibrary.getCard(id).makeCopy());
                }
            }
        }
        for(SerializableCard sc : sDeck.moddedDeck) {
            deck.add(SerializableCard.toAbstractCard(sc));
        }
        return deck;
    }
}
