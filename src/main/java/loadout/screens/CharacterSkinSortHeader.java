package loadout.screens;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;
import loadout.relics.TildeKey;

import static loadout.screens.PowerSelectSortHeader.pTEXT;

public class CharacterSkinSortHeader extends AbstractSortHeader{

    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus resetMorphButton;

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CharacterSkinSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    public CharacterSkinSortHeader(AbstractSelectScreen ss) {
        super(ss);
        float xPosition = 300.0F * Settings.scale;
        float yPosition = START_Y - 400.0F*Settings.yScale;
        this.searchBox = new TextSearchBox(this, 0.0F, Settings.HEIGHT - 500.0F * Settings.scale,false);

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2 * SPACE_Y;
        resetMorphButton = new HeaderButtonPlus(TEXT[0], xPosition, yPosition, this,true, ImageMaster.MAP_NODE_REST);

        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton, this.resetMorphButton};

        this.dropdownMenuHeaders = new String[] {};
        this.dropdownMenus = new DropdownMenu[] {};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if(button == this.resetMorphButton) {
            TildeKey.resetPlayerMorph();
            return;
        } else if(button == this.nameButton) {
            clearActiveButtons();
            ((CharacterSkinSelectScreen)this.selectScreen).sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if(button == this.modButton) {
            clearActiveButtons();
            ((CharacterSkinSelectScreen)this.selectScreen).sortByMod(isAscending);
            resetOtherButtons();
        }
        this.justSorted = true;
        button.setActive(true);
    }


}
