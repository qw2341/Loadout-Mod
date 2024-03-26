package loadout.util;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.megacrit.cardcrawl.localization.Keyword;
import loadout.LoadoutMod;

import java.nio.charset.StandardCharsets;

import static basemod.BaseMod.gson;

public class KeywordsAdder {

    public static void addKeywords() {
        Keywords keywords = gson.fromJson(Gdx.files.internal(LoadoutMod.getModID() + "Resources/localization/"+LoadoutMod.languageSupport()+"/Keyword.json").readString(String.valueOf(StandardCharsets.UTF_8)), Keywords.class);
        if(keywords != null && keywords.keywords != null)
            for (Keyword k : keywords.keywords)
                BaseMod.addKeyword(k.NAMES,k.DESCRIPTION);
    }

    static class Keywords {

        Keyword[] keywords;

        Keywords() {
        }

    }
}
