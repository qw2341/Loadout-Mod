package loadout.screens;

import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import loadout.LoadoutMod;
import loadout.relics.TildeKey;

public class StatModSortHeader extends SortHeader{

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("StatModSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    private HeaderButtonPlus killAllButton;

    private HeaderButtonPlus godModeButton;
    private HeaderButtonPlus infEnergyButton;
    private HeaderButtonPlus canGoToAnyRoomButton;

    private HeaderButtonPlus alwaysPlayerTurnButton;

    private HeaderButtonPlus drawTillLimitButton;

    private HeaderButtonPlus changeSeedButton;
    private HeaderButtonPlus applySeedButton;




    public StatModSortHeader(SelectScreen ss) {
        super(ss);
        float xPosition = this.startX - 75.0f;
        float yPosition = START_Y;

        this.killAllButton = new HeaderButtonPlus(TEXT[0], xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.killAllButton.isAscending = TildeKey.isKillAllMode;
        yPosition -= SPACE_Y;

        this.godModeButton = new HeaderButtonPlus(TEXT[1], xPosition,yPosition,  this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.godModeButton.isAscending = TildeKey.isGodMode;
        yPosition -= SPACE_Y;

        this.infEnergyButton = new HeaderButtonPlus(TEXT[2], xPosition,yPosition,  this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.infEnergyButton.isAscending = TildeKey.isInfiniteEnergy;
        yPosition -= SPACE_Y;

        this.drawTillLimitButton = new HeaderButtonPlus(TEXT[5], xPosition,yPosition,  this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.drawTillLimitButton.isAscending = TildeKey.isDrawCardsTillLimit;
        yPosition -= SPACE_Y;

        this.canGoToAnyRoomButton = new HeaderButtonPlus(TEXT[3], xPosition,yPosition,  this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.canGoToAnyRoomButton.isAscending = TildeKey.canGoToAnyRooms;
        yPosition -= SPACE_Y;

        this.alwaysPlayerTurnButton = new HeaderButtonPlus(TEXT[4], xPosition,yPosition,  this, false, true, HeaderButtonPlus.Alignment.CENTER);
        this.alwaysPlayerTurnButton.isAscending = TildeKey.isAlwaysPlayerTurn;
        yPosition -= SPACE_Y;

        this.changeSeedButton = new HeaderButtonPlus(TEXT[6], xPosition,yPosition,  this, true, ImageMaster.MAP_NODE_EVENT);
        yPosition -= SPACE_Y;

        this.applySeedButton = new HeaderButtonPlus(TEXT[7], xPosition,yPosition,  this, true, ImageMaster.SETTINGS_ICON);
        yPosition -= SPACE_Y;




        this.buttons = new HeaderButtonPlus[] { this.killAllButton, this.godModeButton, this.infEnergyButton, this.drawTillLimitButton, this.canGoToAnyRoomButton, this.alwaysPlayerTurnButton, this.changeSeedButton, this.applySeedButton};
        this.dropdownMenus = new DropdownMenu[] {};
        this.dropdownMenuHeaders = new String[] {};
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if(button == this.killAllButton) {
            TildeKey.isKillAllMode = isAscending;

        } else if (button == this.godModeButton) {
            TildeKey.isGodMode = isAscending;
        } else if (button == this.infEnergyButton) {
            TildeKey.isInfiniteEnergy = isAscending;
        } else if (button == this.canGoToAnyRoomButton) {
            TildeKey.canGoToAnyRooms = isAscending;
        } else if (button == this.alwaysPlayerTurnButton) {
            TildeKey.isAlwaysPlayerTurn = isAscending;
        } else if (button == this.drawTillLimitButton) {
            TildeKey.isDrawCardsTillLimit = isAscending;
        } else if (button == this.changeSeedButton) {
            ((StatModSelectScreen)selectScreen).seedPanel.show();
        } else if (button == this.applySeedButton) {
            AbstractDungeon.generateSeeds();
        }

    }
}
