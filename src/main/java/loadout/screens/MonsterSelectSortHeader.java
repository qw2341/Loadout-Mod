package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import loadout.LoadoutMod;

import java.util.ArrayList;
import java.util.Arrays;

import static loadout.screens.PowerSelectSortHeader.cTEXT;
import static loadout.screens.PowerSelectSortHeader.pTEXT;

public class MonsterSelectSortHeader extends SortHeader{


    public final TextSearchBox searchBox;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final DropdownMenu typeFilterButton;
    private final String[] rhTEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;

    public MonsterSelectSortHeader(SelectScreen<MonsterSelectScreen.MonsterButton> ss) {
        super(ss);

        float xPosition = 300.0F * Settings.scale;
        float yPosition = START_Y - 400.0F*Settings.yScale;
        this.searchBox = new TextSearchBox(this, 0.0F, Settings.HEIGHT - 500.0F * Settings.scale,false);

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);


        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton};

//        ArrayList<String> a = new ArrayList<>();
//        a.add(cTEXT[0]);
//        Arrays.stream(AbstractMonster.EnemyType.values()).map(et -> a.add(et));
//        a.addAll();
        this.typeFilterButton = new DropdownMenu(this, new String[] {cTEXT[0],rhTEXT[1],rhTEXT[2],rhTEXT[4]}, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.dropdownMenuHeaders = new String[] {rhTEXT[1]};
        this.dropdownMenus = new DropdownMenu[] {this.typeFilterButton};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.typeFilterButton) {
            switch (i) {
                case 0:
                    ((MonsterSelectScreen)selectScreen).filterType = null;
                    break;
                case 1: case 2: case 3:
                    ((MonsterSelectScreen)selectScreen).filterType = AbstractMonster.EnemyType.values()[i-1];
                    break;
            }
            selectScreen.updateFilters();
        }
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.nameButton) {
            clearActiveButtons();
            ((MonsterSelectScreen)this.selectScreen).sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            ((MonsterSelectScreen)this.selectScreen).sortByMod(isAscending);
            resetOtherButtons();
        }
    }

    @Override
    public void update() {
        super.update();
        this.searchBox.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        this.searchBox.render(sb);
        super.render(sb);

    }
}
