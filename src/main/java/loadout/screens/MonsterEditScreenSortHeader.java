package loadout.screens;

import com.megacrit.cardcrawl.screens.options.DropdownMenu;

public class MonsterEditScreenSortHeader extends AbstractSortHeader{
    public MonsterEditScreenSortHeader(AbstractSelectScreen ss) {
        super(ss);
        this.dropdownMenus = new DropdownMenu[] {};
        this.dropdownMenuHeaders = new String[] {};
        this.buttons = new HeaderButtonPlus[] {};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus var1, boolean var2) {

    }
}
