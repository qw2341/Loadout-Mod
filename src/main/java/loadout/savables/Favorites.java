package loadout.savables;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

import static basemod.abstracts.CustomSavable.saveFileGson;

public class Favorites {

    public static final String POWER_SAVE_KEY = "FavoritePowers";
    private File file;
    private String filePath;

    Type favType;

    public static HashMap<String, HashSet<String>> favorites = new HashMap<>();
    public static HashSet<String> favoritePowers = new HashSet<>();


    public Favorites() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","Favorites","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.favType = new TypeToken<HashMap<String, HashSet<String>>>() { }.getType();
        this.load();
    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));

        HashMap<String, HashSet<String>> fMap = saveFileGson.fromJson(reader, favType);
        if (fMap != null) {
            favorites.clear();
            favorites.putAll(fMap);
        }

        favoritePowers.clear();
        if(favorites.containsKey(POWER_SAVE_KEY))
            favoritePowers.addAll(favorites.get(POWER_SAVE_KEY));


        reader.close();
    }

    public void save() throws IOException {
        FileWriter fileWriter = new FileWriter(this.filePath);

        favorites.put(POWER_SAVE_KEY, favoritePowers);

        saveFileGson.toJson(favorites, favType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }


}
