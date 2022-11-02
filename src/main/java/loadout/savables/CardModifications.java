package loadout.savables;

import basemod.abstracts.AbstractCardModifier;
import basemod.abstracts.CustomSavable;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.utils.Json;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

public class CardModifications implements CustomSavable<HashMap<String,SerializableCard>> {

    public static final String KEY = "Modified Cards";
    private File file;
    private String filePath;
    Type cardMapType;

    public static HashMap<String,SerializableCard> cardMap = new HashMap<>();

    public CardModifications() throws IOException {
        this.filePath = SpireConfig.makeFilePath("loadoutMod","CardModifications","json");
        this.file = new File(this.filePath);
        this.file.createNewFile();
        this.cardMapType = new TypeToken<HashMap<String,SerializableCard>>() { }.getType();
        this.load();
    }

    @Override
    public HashMap<String,SerializableCard> onSave() {

        return cardMap;
    }

    @Override
    public void onLoad(HashMap<String,SerializableCard> s) {

        if (s == null) return;
        else cardMap = s;

        modifyCards();

    }

    public void load() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(this.filePath));
        HashMap<String, SerializableCard> cMap = saveFileGson.fromJson(reader, cardMapType);
        if (cMap != null) {
            cardMap.clear();
            cardMap.putAll(cMap);
        }

        reader.close();
    }

    public void save() throws IOException {
        FileWriter fileWriter = new FileWriter(this.filePath);
        saveFileGson.toJson(cardMap, cardMapType, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

    public static void modifyCards() {
        for (String cardId:cardMap.keySet()) {
            if(cardId != null && CardLibrary.cards.containsKey(cardId))
                CardLibrary.cards.put(cardId,SerializableCard.toAbstractCard(cardMap.get(cardId)));
        }
    }

    public static void modifyCard(AbstractCard card, SerializableCard sc) {
        card.cost = sc.cost;
        card.costForTurn = card.cost;
        card.baseDamage = sc.baseDamage;
        card.baseBlock = sc.baseBlock;
        card.baseMagicNumber = sc.baseMagicNumber;
        card.baseHeal = sc.baseHeal;
        card.baseDraw = sc.baseDraw;
        card.baseDiscard = sc.baseDiscard;
        card.color = AbstractCard.CardColor.values()[sc.color];
        card.type = AbstractCard.CardType.values()[sc.type];
        card.rarity = AbstractCard.CardRarity.values()[sc.rarity];
//        AutoplayField.autoplay.set(card,sc.autoplay);
//        SoulboundField.soulbound.set(card,sc.soulbound);
//        FleetingField.fleeting.set(card,sc.fleeting);
//        GraveField.grave.set(card,sc.grave);
        //AbstractCardPatch.setCardModified(card,sc.modified);

        for(String modifierId : sc.modifiers) {
            AbstractCardModifier acm = ModifierLibrary.getModifier(modifierId);
            if (acm != null)
                CardModifierManager.addModifier(card, acm);
        }
    }
}
