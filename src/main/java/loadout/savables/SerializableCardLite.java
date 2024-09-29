package loadout.savables;

import basemod.ReflectionHacks;
import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import loadout.LoadoutMod;
import loadout.cards.SutureCard;
import loadout.helper.ModifierLibrary;
import loadout.patches.AbstractCardPatch;
import loadout.patches.InfUpgradePatch;
import loadout.relics.CardModifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static loadout.LoadoutMod.logger;

public class SerializableCardLite {

    public static Object[] toObjectArray(AbstractCard ac) {
        Object[] cardArray = new Object[18];
        if(ac instanceof SutureCard){
            //TODO: Add Suture Card Compatibility
            ac = new Madness();
        }
        cardArray[0] = ac.cardID;
            cardArray[1] = AbstractCardPatch.isCardModified(ac);
            //get an unmodded copy and compare whats changed
            AbstractCard unmoddedCopy = CardModifications.getUnmoddedCopy(ac);

            if(unmoddedCopy.upgraded != ac.upgraded) cardArray[2] = ac.upgraded;
            if(unmoddedCopy.timesUpgraded != ac.timesUpgraded) cardArray[3] = ac.timesUpgraded;

            for (int i = 0; i < ac.timesUpgraded; i++) {
                unmoddedCopy.upgrade();
            }

            if(unmoddedCopy.cost != ac.cost) cardArray[4] = ac.cost;
            if(unmoddedCopy.baseDamage != ac.baseDamage ) cardArray[5] = ac.baseDamage;
            if(unmoddedCopy.baseBlock != ac.baseBlock ) cardArray[6] = ac.baseBlock;
            if(unmoddedCopy.baseMagicNumber != ac.baseMagicNumber ) cardArray[7] = ac.baseMagicNumber;
        Map<String, Integer> additionalMNs = AbstractCardPatch.CardModificationFields.additionalMagicNumbers.get(ac);

            if(!additionalMNs.isEmpty()) cardArray[8] = AbstractCardPatch.serializeAdditionalMagicNumbers(ac);
            if(unmoddedCopy.baseDraw != ac.baseDraw ) cardArray[9] = ac.baseDraw;
            if(unmoddedCopy.baseDiscard != ac.baseDiscard ) cardArray[10] = ac.baseDiscard;
            if(unmoddedCopy.misc != ac.misc ) cardArray[11] = ac.misc;
            if(unmoddedCopy.color != ac.color ) cardArray[12] = ac.color.toString();
            if(unmoddedCopy.type != ac.type ) cardArray[13] = ac.type.toString();
            if(unmoddedCopy.rarity != ac.rarity ) cardArray[14] = ac.rarity.toString();

        ArrayList<AbstractCardModifier> cardMods = CardModifierManager.modifiers(ac);
        if(!cardMods.isEmpty()) {
            cardArray[15] = new ArrayList<String>();
            for (AbstractCardModifier acm : cardMods) {
                ((ArrayList<String>)cardArray[15]).add(acm.identifier(ac));
            }
        }
        AbstractCard cardCopy = ac.makeStatEquivalentCopy();
        CardModifierManager.removeAllModifiers(cardCopy, false);
        if(!unmoddedCopy.originalName.equals(cardCopy.originalName)) cardArray[16] = cardCopy.originalName;
        if(!unmoddedCopy.rawDescription.equals(cardCopy.rawDescription)) cardArray[17] = cardCopy.rawDescription;

        return cardArray;
    }

    public static AbstractCard toAbstractCard(Object[] sc){
        if(sc==null || sc[0]== null || !CardLibrary.isACard((String) sc[0])) {
            return new Madness();
        }

        //AbstractCard card = CardLibrary.getCard((String) sc[0]).makeCopy();
        AbstractCard card = CardModifications.getUnmoddedCopy((String) sc[0]);
        if (sc[3] != null && (int)(double)sc[3] > 0)  {
            card.timesUpgraded = (int)(double)sc[3] - 1;
            card.upgrade();
        }

        if(sc[4] != null) card.cost = (int)(double) sc[4];
        card.costForTurn = card.cost;
        if(sc[5] != null) card.baseDamage = (int)(double) sc[5];
        if(sc[6] != null) card.baseBlock = (int)(double) sc[6];
        if(sc[7] != null) card.baseMagicNumber = (int)(double) sc[7];
        card.magicNumber = card.baseMagicNumber;
        try {
            if(sc[8] != null) {
                //check
                String data = (String) sc[8];
                //logger.info("Now loading magic number array: {}", data);
                if (!data.isEmpty()) {
                    if (data.contains(AbstractCardPatch.MAGIC_NUMBER_DELIMITER))
                        AbstractCardPatch.deserializeAdditionalMagicNumbers(card, data);
                    else
                        card.baseHeal = Integer.parseInt(data);
                }

            }
        } catch (Exception e) {
            logger.warn("Failed to get magic number array while importing from clipboard! received: {}", sc[8]);
            e.printStackTrace();
        }
        if(sc[9] != null) card.baseDraw = (int)(double) sc[9];
        if(sc[10] != null) card.baseDiscard = (int)(double) sc[10];
        if(sc[11] != null) card.misc = (int)(double) sc[11];

        if(sc[12] != null) card.color = AbstractCard.CardColor.valueOf((String) sc[12]);
        if(sc[13] != null) card.type = AbstractCard.CardType.valueOf((String) sc[13]);
        if(sc[14] != null) card.rarity = AbstractCard.CardRarity.valueOf((String) sc[14]);
        if(sc[1] != null) AbstractCardPatch.setCardModified(card, (Boolean) sc[1]);

        if(sc[15] != null) for(String modifierId : (ArrayList<String>)sc[15]) {
            CardModifierManager.addModifier(card, Objects.requireNonNull(ModifierLibrary.getModifier(modifierId)));
        }

        if(sc.length >= 18) {
            if(sc[16] != null) {
                card.originalName = (String) sc[16];
                card.name = CardModifier.getUpgradedName(card);
                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
            }
            if(sc[17] != null) {
                card.rawDescription = (String) sc[17];
                card.initializeDescription();
            }
        }

        InfUpgradePatch.changeCardName(card);

        return card;
    }
}
