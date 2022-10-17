package loadout.savables;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;
import loadout.relics.CardModifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class SerializableCard implements Serializable {
    public String id;
    public int cost;
    public int baseDamage;
    public int baseBlock;
    public int baseMagicNumber;
    public int baseHeal;
    public int baseDraw;
    public int baseDiscard;
    public int color;
    public int type;
    public int rarity;
    public boolean modified;
    public String[] modifiers;

    public static AbstractCard toAbstractCard(SerializableCard sc) {
        AbstractCard card = CardLibrary.getCard(sc.id).makeCopy();
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
        AbstractCardPatch.setCardModified(card,sc.modified);

        for(String modifierId : sc.modifiers) {
            CardModifierManager.addModifier(card, Objects.requireNonNull(ModifierLibrary.getModifier(modifierId)));
        }

        return card;
    }

    public static SerializableCard toSerializableCard(AbstractCard card) {
        SerializableCard sc = new SerializableCard();
        sc.id = card.cardID;
        sc.cost = card.cost;
        sc.baseDamage = card.baseDamage;
        sc.baseBlock = card.baseBlock;
        sc.baseMagicNumber = card.baseMagicNumber;
        sc.baseHeal = card.baseHeal;
        sc.baseDraw = card.baseDraw;
        sc.baseDiscard = card.baseDiscard;
        sc.color = card.color.ordinal();
        sc.type = card.type.ordinal();
        sc.rarity = card.rarity.ordinal();
        sc.modified = AbstractCardPatch.isCardModified(card);
        ArrayList<AbstractCardModifier> cardMods = CardModifierManager.modifiers(card);
        sc.modifiers = new String[cardMods.size()];
        int i = 0;
        for (AbstractCardModifier acm : cardMods) {
            sc.modifiers[i] = acm.identifier(card);
            i++;
        }

        return sc;
    }
}
