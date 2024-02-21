package loadout.screens;

import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;

import static loadout.screens.PowerSelectSortHeader.pTEXT;

public class CharacterSkinSortHeader extends AbstractSortHeader{

    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;


    public CharacterSkinSortHeader(AbstractSelectScreen ss) {
        super(ss);
        float xPosition = 300.0F * Settings.scale;
        float yPosition = START_Y - 400.0F*Settings.yScale;
        this.searchBox = new TextSearchBox(this, 0.0F, Settings.HEIGHT - 500.0F * Settings.scale,false);

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;

        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton};

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
