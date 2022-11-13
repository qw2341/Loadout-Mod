package loadout.savables;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import static basemod.abstracts.CustomSavable.saveFileGson;

public class CardLoadouts {

    private File file;
    private String filePath;

    Type favType;

    public static HashMap<String, ArrayList<SerializableCard>> loadouts = new HashMap<>();



    public CardLoadouts() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","CardLoadouts","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.favType = new TypeToken<HashMap<String, ArrayList<SerializableCard>>>() { }.getType();
        this.load();
    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));

        HashMap<String, ArrayList<SerializableCard>> cMap = saveFileGson.fromJson(reader, favType);
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
        FileWriter fileWriter = new FileWriter(this.filePath);

        //loadouts.put(CARD_LOADOUT_SAVE_KEY, cardLoadout);

        saveFileGson.toJson(loadouts, favType, fileWriter);
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
}
