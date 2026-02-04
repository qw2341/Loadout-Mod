package loadout.savables;

import basemod.abstracts.CustomSavable;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import loadout.patches.AbstractCardPatch;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoadoutCardFieldsSavables implements CustomSavable<LoadoutCardFieldsSavables.AdditionalFieldData> {

    @Override
    public AdditionalFieldData onSave() {
        if(AbstractDungeon.player == null || AbstractDungeon.player.masterDeck == null) return null;
        return new AdditionalFieldData(AbstractDungeon.player.masterDeck.group);
    }

    @Override
    public void onLoad(AdditionalFieldData additionalFieldData) {
        if(AbstractDungeon.player == null || AbstractDungeon.player.masterDeck == null) return;
        additionalFieldData.onLoad(AbstractDungeon.player.masterDeck.group);
    }

    @Override
    public Type savedType() {
        return new TypeToken<AdditionalFieldData>() {}.getType();
    }

    public static class AdditionalFieldData {
        //saves the fields on every card in a deck
        public ArrayList<Map<String, Integer>> additionalMagicUpgradeDiffs;
        public ArrayList<String> additionalMagicNumbers;
        public ArrayList<Integer[]> normalUpgradeDiffs;
        public ArrayList<String[]> additionalModifiers;

        public AdditionalFieldData(ArrayList<AbstractCard> deck) {
            additionalMagicUpgradeDiffs = new ArrayList<>();
            additionalMagicNumbers = new ArrayList<>();
            normalUpgradeDiffs = new ArrayList<>();
            additionalModifiers = new ArrayList<>();
            for (AbstractCard card : deck) {
                if (!AbstractCardPatch.CardModificationFields.additionalMagicNumbers.get(card).isEmpty()) {
                    additionalMagicNumbers.add(AbstractCardPatch.serializeAdditionalMagicNumbers(card));
                } else {
                    additionalMagicNumbers.add(null);
                }
                normalUpgradeDiffs.add(AbstractCardPatch.getCardNormalUpgrade(card));
                additionalModifiers.add(AbstractCardPatch.getCardAdditionalModifiers(card));
                additionalMagicUpgradeDiffs.add(AbstractCardPatch.getCardAdditionalMagicUpgrade(card));
            }
        }

        public void onLoad(ArrayList<AbstractCard> deck) {
            if (deck != null && deck.size() <= additionalMagicNumbers.size()) {
                for (int i  = 0; i < deck.size(); i++) {
                    AbstractCardPatch.deserializeAdditionalMagicNumbers(deck.get(i), additionalMagicNumbers.get(i));
                    AbstractCardPatch.setCardNormalUpgrade(deck.get(i), normalUpgradeDiffs.get(i));
                    AbstractCardPatch.setCardAdditionalModifiers(deck.get(i), additionalModifiers.get(i));
                    AbstractCardPatch.setCardAdditionalMagicUpgrade(deck.get(i), additionalMagicUpgradeDiffs.get(i));
                }
            }

        }
    }


}
