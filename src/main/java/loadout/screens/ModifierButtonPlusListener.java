package loadout.screens;

import com.megacrit.cardcrawl.cards.AbstractCard;

public interface ModifierButtonPlusListener {
    AbstractCard  getCard();
    void didChangeOrder(HeaderButtonPlus caller, boolean isAscending);
}
