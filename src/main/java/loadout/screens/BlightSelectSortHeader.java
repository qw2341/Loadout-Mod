package loadout.screens;

import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

public class BlightSelectSortHeader extends AbstractSortHeader{

    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus modButton;
    public BlightSelectSortHeader(AbstractSelectScreen<AbstractBlight> ss) {
        super(ss);
        float xPosition = 200.0F * Settings.scale;
        float yPosition = Settings.HEIGHT - 300.0F * Settings.scale;
        this.nameButton = new HeaderButtonPlus(PotionSelectSortHeader.TEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(PotionSelectSortHeader.TEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2*SPACE_Y;
        this.buttons = new HeaderButtonPlus[] {this.nameButton, this.modButton};
        this.dropdownMenuHeaders = new String[] {};
        this.dropdownMenus = new DropdownMenu[] {};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.nameButton) {
            clearActiveButtons();
            ((BlightSelectScreen)this.selectScreen).sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            ((BlightSelectScreen)this.selectScreen).sortByMod(isAscending);
            resetOtherButtons();
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);
    }
}
