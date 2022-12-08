package loadout.savables;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.patches.AbstractCardPatch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializableDeck implements Serializable {

    public HashMap<String,Integer> simpleDeck;
    public ArrayList<Object[]> moddedDeck;

    private static final String textSeparator = "-";

    public SerializableDeck() {
        simpleDeck = new HashMap<>();
        moddedDeck = new ArrayList<>();
    }

    public SerializableDeck(ArrayList<AbstractCard> deck) {
        this();
        for(AbstractCard ac : deck) {
            if(AbstractCardPatch.isCardModified(ac)){
                moddedDeck.add(SerializableCardLite.toObjectArray(ac));
            } else {
                //unmodded
                int timesUpgraded = ac.upgraded ? Math.max(ac.timesUpgraded, 1) : 0;
                //edit id
                String id = timesUpgraded + textSeparator + ac.cardID;

                simpleDeck.merge( id,1,Integer::sum);
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
            //edit id back to normal form
            String[] splitId = id.split(textSeparator,2);
            id = splitId[1];
            int timesUpgraded = Integer.parseInt(splitId[0]);
            if(CardLibrary.isACard(id)) {
                for (int i = 0; i< numCards; i++) {
                    AbstractCard ac = CardLibrary.getCard(id).makeCopy();
                    for(int j = 0; j< timesUpgraded; j++) {
                        ac.upgrade();
                    }
                    deck.add(ac);
                }
            } else {
                for (int i = 0; i< numCards; i++) deck.add(new Madness());
            }
        }
        for(Object[] sc : sDeck.moddedDeck) {
            try{
                deck.add(SerializableCardLite.toAbstractCard(sc));
            } catch (Exception e) {
                e.printStackTrace();
                deck.add(new Madness());
            }

        }
        return deck;
    }
}
