package loadout.screens;

import basemod.abstracts.AbstractCardModifier;
import basemod.helpers.CardModifierManager;
import com.megacrit.cardcrawl.cards.AbstractCard;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import org.jetbrains.annotations.Nullable;

/**
 * Designed for adding modifiers to a card
 */
public class ModifierButtonPlus extends HeaderButtonPlus{
    public interface CustomModifierLogics {
        void customRemovalLogic();
        void customAddingLogic();

        boolean getStatus();
    }
    private ModifierButtonPlusListener delegate;
    public AbstractCardModifier modifier;
    public CustomModifierLogics customLogics;

    /**
     * For custom modifiers
     * @param text
     * @param cx
     * @param cy
     * @param delegate
     * @param modifier
     */
    public ModifierButtonPlus(String text, float cx, float cy, ModifierButtonPlusListener delegate, AbstractCardModifier modifier) {
        super(text, cx, cy);
        this.delegate  = delegate;
        this.isSorting = false;
        this.isToggle = true;
        this.alignment = Alignment.CENTER;
        this.modifier = modifier;
    }

    /**
     * For built-in modifiers
     * @param text
     * @param cx
     * @param cy
     */
    public ModifierButtonPlus(String text, float cx, float cy, ModifierButtonPlusListener delegate, CustomModifierLogics logics) {
        super(text, cx, cy);
        this.delegate  = delegate;
        this.isSorting = false;
        this.isToggle = true;
        this.alignment = Alignment.CENTER;
        this.customLogics = logics;
    }

    @Override
    protected void updateClickLogic() {
        if (this.isToggle) {
            this.isAscending = !this.isAscending;
        }

        if (!this.isAscending) {
            if (customLogics != null)
                customLogics.customRemovalLogic();
            else
                CardModifierManager.removeModifiersById(delegate.getCard(), modifier.identifier(null), true);
        } else {
            if (customLogics != null)
                customLogics.customAddingLogic();
            else
                CardModifierManager.addModifier(delegate.getCard(), modifier);
        }

        AbstractCardPatch.setCardModified(delegate.getCard(), true);
        this.delegate.didChangeOrder(this, this.isAscending);
    }

    public void updateStatus() {
        if(customLogics != null) {
            this.isAscending = customLogics.getStatus();
        } else {
            this.isAscending = CardModifierManager.hasModifier(delegate.getCard(), modifier.identifier(null));
        }
    }

    public ModifierButtonPlus makeCopy(ModifierButtonPlusListener newDelegate) {
        if (customLogics != null) {
            return new ModifierButtonPlus(this.text, this.x, this.y, newDelegate, customLogics);
        } else {
            return new ModifierButtonPlus(this.text, this.x, this.y, newDelegate, modifier);
        }
    }
}
