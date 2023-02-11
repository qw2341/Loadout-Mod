package loadout.screens;

import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

public class BlightSelectSortHeader extends AbstractSortHeader{
    public BlightSelectSortHeader(AbstractSelectScreen<AbstractBlight> ss) {
        super(ss);

        this.buttons = new HeaderButtonPlus[] {};
        this.dropdownMenuHeaders = new String[] {};
        this.dropdownMenus = new DropdownMenu[] {};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus var1, boolean var2) {

    }
}
