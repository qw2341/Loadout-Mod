package loadout.util;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.mod.stslib.Keyword;
import loadout.LoadoutMod;

import java.nio.charset.StandardCharsets;

import static basemod.BaseMod.gson;

public class KeywordsAdder {

    public static void addKeywords() {
        Keywords keywords = gson.fromJson(Gdx.files.internal(LoadoutMod.getModID() + "Resources/localization/"+LoadoutMod.languageSupport()+"/Keyword.json").readString(String.valueOf(StandardCharsets.UTF_8)), Keywords.class);
        if(keywords != null && keywords.keywords != null) {
            for (Keyword k : keywords.keywords)
                BaseMod.addKeyword(LoadoutMod.getModID(), k.PROPER_NAME,k.NAMES,k.DESCRIPTION);
            LoadoutMod.logger.info("Finished adding {} Keywords!", keywords.keywords.length);
        } else {
            LoadoutMod.logger.info("Failed to load Keywords!");
        }

    }

    public static String getKeywordString(String keyword, String modid) {
        return LoadoutMod.isCHN() ? " *" + keyword + " " : (modid == null ? "" : modid + ":") + keyword.replace(" ", "_");
    }

    static class Keywords {

        Keyword[] keywords;

        Keywords() {
        }

    }
}
