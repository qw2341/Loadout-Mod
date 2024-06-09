package loadout.savables;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.cards.SutureCard;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;
import loadout.patches.InfUpgradePatch;
import loadout.relics.CardModifier;

import java.io.Serializable;
import java.util.ArrayList;

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

    public String colorString = null;
    public int type;
    public String typeString = null;
    public int rarity;
    public String rarityString = null;
    public int misc = 0;

    public boolean upgraded = false;
    public int timesUpgraded = 0;
    public boolean modified;
    public String[] modifiers;
    public String originalName;
    public String rawDescription;

    public static AbstractCard toAbstractCard(SerializableCard sc) {
        if(!CardLibrary.isACard(sc.id)) {
            return new Madness();
        }

        AbstractCard card = CardModifications.getUnmoddedCopy(sc.id);
        if (sc.timesUpgraded > 0)  {
            card.timesUpgraded = sc.timesUpgraded - 1;
            card.upgrade();
        }

        card.cost = sc.cost;
        card.costForTurn = card.cost;
        card.baseDamage = sc.baseDamage;
        card.baseBlock = sc.baseBlock;
        card.baseMagicNumber = sc.baseMagicNumber;
        card.magicNumber = card.baseMagicNumber;
        card.baseHeal = sc.baseHeal;
        card.baseDraw = sc.baseDraw;
        card.baseDiscard = sc.baseDiscard;
        card.misc = sc.misc;

        card.color = sc.colorString != null ? AbstractCard.CardColor.valueOf(sc.colorString) : AbstractCard.CardColor.values()[sc.color];
        card.type = sc.typeString != null ? AbstractCard.CardType.valueOf(sc.typeString) : AbstractCard.CardType.values()[sc.type];
        card.rarity = sc.rarityString != null ? AbstractCard.CardRarity.valueOf(sc.rarityString) : AbstractCard.CardRarity.values()[sc.rarity];
        AbstractCardPatch.setCardModified(card,sc.modified);

        for(String modifierId : sc.modifiers) {
            AbstractCardModifier cardModifier = ModifierLibrary.getModifier(modifierId);
            if(cardModifier == null) continue;
            CardModifierManager.addModifier(card, cardModifier);
        }

        if(sc.originalName != null) {
            card.originalName = sc.originalName;
            card.name = CardModifier.getUpgradedName(card);
            ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
        }
        if(sc.rawDescription != null) {
            card.rawDescription = sc.rawDescription;
            card.initializeDescription();
        }


        InfUpgradePatch.changeCardName(card);

        return card;
    }

    public static SerializableCard toSerializableCard(AbstractCard card) {
        SerializableCard sc = new SerializableCard();

        if(card instanceof SutureCard) {
            //TODO: Add Suture Card Compatibility
            card = new Madness();
        }

        sc.id = card.cardID;
        sc.cost = card.cost;
        sc.baseDamage = card.baseDamage;
        sc.baseBlock = card.baseBlock;
        sc.baseMagicNumber = card.baseMagicNumber;
        sc.baseHeal = card.baseHeal;
        sc.baseDraw = card.baseDraw;
        sc.baseDiscard = card.baseDiscard;
        sc.color = card.color.ordinal();
        sc.colorString = String.valueOf(card.color);
        sc.type = card.type.ordinal();
        sc.typeString = String.valueOf(card.type);
        sc.rarity = card.rarity.ordinal();
        sc.rarityString = String.valueOf(card.rarity);
        sc.misc = card.misc;
        sc.upgraded = card.upgraded;
        sc.timesUpgraded = card.timesUpgraded;
        sc.modified = AbstractCardPatch.isCardModified(card);

        ArrayList<AbstractCardModifier> cardMods = CardModifierManager.modifiers(card);
        sc.modifiers = new String[cardMods.size()];
        AbstractCard original = CardModifier.getUnmodifiedCopyCard(card.cardID);

        sc.originalName = original != null && card.originalName.equals(original.originalName) ? null : card.originalName;
        sc.rawDescription = original != null && card.rawDescription.equals(original.rawDescription) ? null : card.rawDescription;

        int i = 0;
        for (AbstractCardModifier acm : cardMods) {
            sc.modifiers[i] = acm.identifier(card);
            i++;
        }

        return sc;
    }

    @Override
    public String toString() {
        return "[CardID: " + this.id + "; Cost: " + this.cost + "; Damage: " + this.baseDamage + "; Block: " + this.baseBlock;
    }
}
