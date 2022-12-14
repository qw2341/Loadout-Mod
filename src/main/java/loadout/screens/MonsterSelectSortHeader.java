package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;

import java.util.ArrayList;

import static loadout.screens.PowerSelectSortHeader.cTEXT;
import static loadout.screens.PowerSelectSortHeader.pTEXT;

public class MonsterSelectSortHeader extends AbstractSortHeader {


    public final TextSearchBox searchBox;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus dupeButton;
    private final HeaderButtonPlus showPreviewButton;


    private final DropdownMenu typeFilterButton;
    private final String[] rhTEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    public final String[] TEXT = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("MonsterSelectSortHeader")).TEXT;

    public MonsterSelectSortHeader(AbstractSelectScreen<MonsterSelectScreen.MonsterButton> ss) {
        super(ss);

        float xPosition = 300.0F * Settings.scale;
        float yPosition = START_Y - 400.0F*Settings.yScale;
        this.searchBox = new TextSearchBox(this, 0.0F, Settings.HEIGHT - 500.0F * Settings.scale,false);

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.dupeButton = new HeaderButtonPlus(TEXT[0],xPosition,yPosition,this,false, ImageMaster.PROFILE_B);
        this.dupeButton.alignment = HeaderButtonPlus.Alignment.RIGHT;
        yPosition -= SPACE_Y;
        this.showPreviewButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.RIGHT);
        this.showPreviewButton.isAscending = MonsterSelectScreen.showPreviews;

        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton, this.dupeButton, this.showPreviewButton};

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
                    ((MonsterSelectScreen) selectScreen).filterType = null;
                    break;
                case 1: case 2: case 3:
                    ((MonsterSelectScreen) selectScreen).filterType = AbstractMonster.EnemyType.values()[i-1];
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
        } else if (button == this.dupeButton) {
            AbstractRoom ar = AbstractDungeon.getCurrRoom();
            if(ar!=null && ar.monsters!= null) {
                ArrayList<AbstractMonster> monsterTemp = new ArrayList<>();
                for (AbstractMonster am: ar.monsters.monsters) {

                    AbstractMonster m = MonsterSelectScreen.spawnMonster(am.getClass(),am.drawX - MonsterSelectScreen.MonsterButton.calculateSmartDistance(am, am) + 30.0F * (float) Math.random(), am.drawY + 20.0F * (float) Math.random());
                    m.flipHorizontal = am.flipHorizontal;
                    monsterTemp.add(m);
                }
                ar.monsters.monsters.addAll(monsterTemp);
            }
        } else if (button == this.showPreviewButton) {
            MonsterSelectScreen.showPreviews = isAscending;
            selectScreen.itemHeight = isAscending ? 570.0F : 420.0F;

            selectScreen.updateFilters();
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

    @Override
    public void clearActiveButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if(button != this.showPreviewButton) button.setActive(false);
        }
    }

    @Override
    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        for (int i = 0;i<this.buttons.length;i++) {
            if (i!= btnIdx) {
                HeaderButtonPlus button = buttons[i];

                if(button != this.showPreviewButton) button.reset();

            }
        }
    }

    @Override
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if (button != this.showPreviewButton) button.reset();
        }
        for (DropdownMenu ddm : dropdownMenus) {
            ddm.setSelectedIndex(0);
        }
    }
}
