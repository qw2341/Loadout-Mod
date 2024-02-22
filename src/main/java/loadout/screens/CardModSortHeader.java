package loadout.screens;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

import java.util.ArrayList;

public class CardModSortHeader extends AbstractSortHeader{

    private final DropdownMenu modNameDropdown;

    public CardModSortHeader(AbstractSelectScreen ss) {
        super(ss);

        ArrayList<String> f = new ArrayList<>(((CardModSelectScreen) ss).modMods);
        f.add(0,CardSelectSortHeader.TEXT[0]);
        this.modNameDropdown = new DropdownMenu(this, f, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.searchBox = new TextSearchBox(this, 0f, 250f*Settings.scale, false);

        this.buttons = new HeaderButtonPlus[] {};
        this.dropdownMenuHeaders = new String[] {"Mod"};
        this.dropdownMenus = new DropdownMenu[] {this.modNameDropdown};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if (dropdownMenu == this.modNameDropdown) {
            if (i==0) {
                //if showing all
                selectScreen.filterMod = null;
            } else {
                selectScreen.filterMod = s;
            }
            selectScreen.updateFilters();
        }
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus var1, boolean var2) {

    }
}
