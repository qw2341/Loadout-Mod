package loadout.screens;

import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

public class RelicDeleteSortHeader extends AbstractSortHeader{
    public RelicDeleteSortHeader(AbstractSelectScreen<AbstractRelic> ss) {
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
