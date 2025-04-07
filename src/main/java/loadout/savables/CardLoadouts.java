package loadout.savables;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.saveAndContinue.SaveFileObfuscator;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import static basemod.abstracts.CustomSavable.saveFileGson;

public class CardLoadouts {

    public static final String OBFUSCATION_KEY = "loadout";
    public static final String CLIPBOARD_STRING_PREFIX = "### LOADOUT MOD DECK PRESET ***";
    public static final String CLIPBOARD_STRING_SUFFIX = "***###";
    private File file;
    private String filePath;

    Type saveMapType;

    public static HashMap<String, ArrayList<SerializableCard>> loadouts = new HashMap<>();
    static Type saveDeckType = new TypeToken<SerializableDeck>() {}.getType();


    public CardLoadouts() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","CardLoadouts","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.saveMapType = new TypeToken<HashMap<String, ArrayList<SerializableCard>>>() { }.getType();
        this.load();
    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));

        HashMap<String, ArrayList<SerializableCard>> cMap = saveFileGson.fromJson(reader, saveMapType);
        if (cMap != null) {
            loadouts.clear();
            loadouts.putAll(cMap);
        }

        //cardLoadout.clear();
        //if(loadouts.containsKey(CARD_LOADOUT_SAVE_KEY))
            //cardLoadout.addAll(loadouts.get(CARD_LOADOUT_SAVE_KEY));


        reader.close();
    }

    public void save() throws IOException {
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(this.filePath),StandardCharsets.UTF_8);
        //FileWriter fileWriter = new FileWriter(this.filePath);


        //loadouts.put(CARD_LOADOUT_SAVE_KEY, cardLoadout);

        saveFileGson.toJson(loadouts, saveMapType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void addLoadout(String key, ArrayList<AbstractCard> cards) {
        ArrayList<SerializableCard> scards = new ArrayList<>();
        for (AbstractCard card: cards) {
            scards.add(SerializableCard.toSerializableCard(card));
        }
        loadouts.put(key,scards);
    }

    public static ArrayList<AbstractCard> getLoadout(String key) {
        return loadouts.get(key).stream().map(SerializableCard::toAbstractCard).collect(Collectors.toCollection(ArrayList::new));
    }

    public static void removeLoadout(String key) {
        loadouts.remove(key);
    }

    public static String exportEncodedLoadout(ArrayList<AbstractCard> cards) {
        //ArrayList<SerializableCard> scards = cards.stream().map(SerializableCard::toSerializableCard).collect(Collectors.toCollection(ArrayList::new));
        SerializableDeck sDeck = new SerializableDeck(cards);
        return CLIPBOARD_STRING_PREFIX + SaveFileObfuscator.encode(saveFileGson.toJson(sDeck),OBFUSCATION_KEY) + CLIPBOARD_STRING_SUFFIX;
    }

    public static ArrayList<AbstractCard> importEncodedLoadout(String cardString) {
        if(cardString == null || cardString.length() < CLIPBOARD_STRING_PREFIX.length() + CLIPBOARD_STRING_SUFFIX.length() || !StringUtils.startsWith(cardString,CLIPBOARD_STRING_PREFIX) || !StringUtils.endsWith(cardString,CLIPBOARD_STRING_SUFFIX)) throw new IllegalArgumentException();
        String decodedCards = SaveFileObfuscator.decode(cardString.substring(CLIPBOARD_STRING_PREFIX.length(), cardString.length() - CLIPBOARD_STRING_SUFFIX.length()),OBFUSCATION_KEY);
        try {
            //LoadoutMod.logger.info(decodedCards);
            SerializableDeck sDeck = saveFileGson.fromJson(decodedCards, saveDeckType);
            return SerializableDeck.toAbstractCardDeck(sDeck);
        } catch (Exception e) {
            e.printStackTrace();
            LoadoutMod.logger.info("Failed to import card presets!");
            throw new IllegalArgumentException();
        }
    }
}
