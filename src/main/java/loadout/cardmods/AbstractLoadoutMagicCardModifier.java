package loadout.cardmods;

import basemod.abstracts.AbstractCardModifier;
import com.megacrit.cardcrawl.cards.AbstractCard;

public abstract class AbstractLoadoutMagicCardModifier extends AbstractCardModifier {

    @Override
    public void onInitialApplication(com.megacrit.cardcrawl.cards.AbstractCard card) {
        loadout.patches.AbstractCardPatch.addMagicNumber(card, identifier(card), 0);
    }

    @Override
    public void onRemove(com.megacrit.cardcrawl.cards.AbstractCard card) {
        loadout.patches.AbstractCardPatch.removeMagicNumber(card, identifier(card));
    }

    /**
     * Modifies the description of a card with a custom magic number by replacing the original variable name with the custom magic number
     * @param rawDescription
     * @param modifierID could be loadout:modifierID
     * @param originalVariableName could be D or M or B, it should be surrounded by '!' in the rawDescription, like "!D!"
     * @return
     */
    public static String modifyDescriptionWithCustomMagic(String rawDescription, String modifierID, String originalVariableName) {
        // Use regex to find and replace the original variable name with the custom magic modifier ID
        return rawDescription.replaceAll("!" + originalVariableName + "!", "!" + modifierID + "!");
    }
}
