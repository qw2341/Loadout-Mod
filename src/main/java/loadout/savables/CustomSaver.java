package loadout.savables;

import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class CustomSaver <K,V> {
    private File file;
    private String filePath;
    Type type;

    Class<K> clazz1;
    Class<V> clazz2;

    public CustomSaver(String fileName) throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod", fileName,"json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.type = new TypeToken<HashMap<Integer,String[]>>() { }.getType();
    }

    public void save(Object src) throws IOException {
        Writer fileWriter = new OutputStreamWriter(new FileOutputStream(this.filePath), StandardCharsets.UTF_8);;
        CustomSavable.saveFileGson.toJson( src, type, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public HashMap<Integer,String[]> load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));
        HashMap<Integer,String[]> ret = (HashMap<Integer,String[]>) CustomSavable.saveFileGson.fromJson(reader, type);

        reader.close();
        return ret;
    }
}
