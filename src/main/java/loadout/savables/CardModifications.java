package loadout.savables;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.abstracts.CustomSavable;
import basemod.helpers.CardModifierManager;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.LoadoutMod;
import loadout.helper.ModifierLibrary;
import loadout.relics.CardModifier;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class CardModifications
        //implements CustomSavable<HashMap<String,SerializableCard>>
{

    public static final String KEY = "Modified Cards";
    private File file;
    private String filePath;
    Type cardMapType;

    public static boolean isGettingUnmoddedCopy = false;

    public static HashMap<String,SerializableCard> cardMap = new HashMap<>();

    public CardModifications() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","CardModifications","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.cardMapType = new TypeToken<HashMap<String,SerializableCard>>() { }.getType();
        this.load();
    }

    //@Override
    public HashMap<String,SerializableCard> onSave() {

        return cardMap;
    }

    //@Override
    public void onLoad(HashMap<String,SerializableCard> s) {

        if (s == null) return;
        else cardMap = s;

        modifyCards();

    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));
        HashMap<String, SerializableCard> cMap = CustomSavable.saveFileGson.fromJson(reader, cardMapType);
        if (cMap != null) {
            cardMap.clear();
            cardMap.putAll(cMap);
        }

        reader.close();
    }

    public void save() throws IOException {
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(this.filePath),StandardCharsets.UTF_8);;
        CustomSavable.saveFileGson.toJson(cardMap, cardMapType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void modifyCards() {
        LoadoutMod.logger.info("Loading Custom Card Modifications into CardLib");
        for (String cardId:cardMap.keySet()) {
//            try{
//                AbstractCard moddedCard = SerializableCard.toAbstractCard(cardMap.get(cardId));
//                if(cardId != null && CardLibrary.cards.containsKey(cardId))
//                    CardLibrary.cards.put(cardId,moddedCard);
//            } catch (Exception e) {
//                LoadoutMod.logger.info("Exception occurred while modding card with id = " + cardId);
//                e.printStackTrace();
//            }
            if(CardLibrary.isACard(cardId)) modifyOnlyNumberIfExist(CardLibrary.getCard(cardId));

        }
        LoadoutMod.logger.info("Done Loading Custom Card Modifications");
    }

    public static void modifyCard(AbstractCard card, SerializableCard sc) throws Exception {

        if(!isGettingUnmoddedCopy) {

            for (int i = card.timesUpgraded; i < sc.timesUpgraded; i++) {card.upgrade();}
            card.cost = sc.cost;
            card.costForTurn = card.cost;
            card.baseDamage = sc.baseDamage;
            card.baseBlock = sc.baseBlock;
            card.baseMagicNumber = sc.baseMagicNumber;
            card.magicNumber = sc.baseMagicNumber;
            card.baseHeal = sc.baseHeal;
            card.baseDraw = sc.baseDraw;
            card.baseDiscard = sc.baseDiscard;
            card.color = AbstractCard.CardColor.values()[sc.color];
            card.type = AbstractCard.CardType.values()[sc.type];
            card.rarity = AbstractCard.CardRarity.values()[sc.rarity];
            card.misc = sc.misc;

            if (sc.originalName != null) {
                card.originalName = sc.originalName;
                card.name = CardModifier.getUpgradedName(card);
                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
            }
            if (sc.rawDescription != null) {
                card.rawDescription = sc.rawDescription;
                card.initializeDescription();
            }

            for(String modifierId : sc.modifiers) {
                AbstractCardModifier acm = ModifierLibrary.getModifier(modifierId);
                if (acm != null)
                    CardModifierManager.addModifier(card, acm);
            }
//            LoadoutMod.logger.info("Resulting cardID: "+card.cardID+" cost: " + card.cost + " damage: "
//                    +card.baseDamage+" block: " + card.baseBlock +" is card modded: "
//                    + AbstractCardPatch.isCardModified(card));
        }

    }

    public static void modifyCardNumberOnly(AbstractCard card, SerializableCard sc) throws Exception {

        if(!isGettingUnmoddedCopy) {

            for (int i = card.timesUpgraded; i < sc.timesUpgraded; i++) {card.upgrade();}
            card.cost = sc.cost;
            card.costForTurn = card.cost;
            card.baseDamage = sc.baseDamage;
            card.baseBlock = sc.baseBlock;
            card.baseMagicNumber = sc.baseMagicNumber;
            card.magicNumber = sc.baseMagicNumber;
            card.baseHeal = sc.baseHeal;
            card.baseDraw = sc.baseDraw;
            card.baseDiscard = sc.baseDiscard;

            card.misc = sc.misc;

//            LoadoutMod.logger.info("Resulting cardID: "+card.cardID+" cost: " + card.cost + " damage: "
//                    +card.baseDamage+" block: " + card.baseBlock +" is card modded: "
//                    + AbstractCardPatch.isCardModified(card));
        }

    }

    public static AbstractCard getUnmoddedCopy(AbstractCard card) {
        isGettingUnmoddedCopy = true;
        AbstractCard unmoddedCopy = card.makeCopy();
        isGettingUnmoddedCopy = false;
        return unmoddedCopy;
    }

    public static AbstractCard getUnmoddedCopy(String cardId) {
        if(!CardLibrary.isACard(cardId)) return new Madness();
        isGettingUnmoddedCopy = true;
        AbstractCard unmoddedCopy = CardLibrary.getCard(cardId).makeCopy();
        isGettingUnmoddedCopy = false;
        return unmoddedCopy;
    }

    public static void restoreACardInLibrary(AbstractCard card) {
        CardLibrary.cards.put(card.cardID, getUnmoddedCopy(card));
    }

    public static void restoreACardInLibrary(String cardID) {
        AbstractCard unmod = getUnmoddedCopy(CardLibrary.getCard(cardID));
        //unmod.initializeDescription();
        CardLibrary.cards.put(cardID, unmod);
    }

    public static void restoreAllCardsInLibrary() {
        for (String id:cardMap.keySet()) {
            LoadoutMod.logger.info("Resetting card: " + id);
            CardModifications.restoreACardInLibrary(id);
        }
    }

    public static void modifyIfExist(AbstractCard card) {
        if (isGettingUnmoddedCopy) return;
        if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(card.cardID)) {
            try {
                CardModifications.modifyCard(card,CardModifications.cardMap.get(card.cardID));
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to modify: " + card.cardID + " during its constructor call");
            }
        }
    }

    public static void modifyOnlyNumberIfExist(AbstractCard card) {
        if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(card.cardID)) {
            try {
                CardModifications.modifyCardNumberOnly(card,CardModifications.cardMap.get(card.cardID));
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to modify: " + card.cardID + " during its constructor call");
            }
        }
    }
}
