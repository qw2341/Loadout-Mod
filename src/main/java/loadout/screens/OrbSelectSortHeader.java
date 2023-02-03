package loadout.screens;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

public class OrbSelectSortHeader extends AbstractSortHeader{
    public static final float START_X = 200.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;
    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus modButton;
    public OrbSelectSortHeader(AbstractSelectScreen<OrbSelectScreen.OrbButton> ss) {
        super(ss);
        float xPosition = START_X;
        float yPosition = START_Y;
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
            ((OrbSelectScreen)this.selectScreen).sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            ((OrbSelectScreen)this.selectScreen).sortByMod(isAscending);
            resetOtherButtons();
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);
    }
}
