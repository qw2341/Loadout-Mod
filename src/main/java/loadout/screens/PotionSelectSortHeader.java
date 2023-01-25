package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;

public class PotionSelectSortHeader extends AbstractSortHeader {

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PotionSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final PotionStrings pStrings = CardCrawlGame.languagePack.getPotionString("Potion Slot");
    public static final String POTION_SLOT_NAME = pStrings.NAME;


    public static final float START_X = 600.0F * Settings.xScale;

    private final HeaderButtonPlus rarityButton;
    private final HeaderButtonPlus classButton;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus slotAddButton;
    private final HeaderButtonPlus slotSubButton;

    public PotionSelectSortHeader(PotionSelectScreen potionSelectScreen) {
        super(potionSelectScreen);
        float xPosition = START_X;
        float yPosition = START_Y;
        this.classButton = new HeaderButtonPlus(TEXT[0], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.rarityButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.nameButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(TEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2*SPACE_Y;
        this.slotAddButton = new HeaderButtonPlus(POTION_SLOT_NAME, xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.slotAddButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.slotSubButton = new HeaderButtonPlus(POTION_SLOT_NAME, xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.slotSubButton.isAscending = true;
        this.buttons = new HeaderButtonPlus[] { this.classButton, this.rarityButton, this.nameButton, this.modButton, this.slotAddButton, this.slotSubButton };

        this.dropdownMenuHeaders= new String[] {};
        this.dropdownMenus = new DropdownMenu[] {};

    }


    @Override
    public void clearActiveButtons() {
        //does not clear the last 2 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].setActive(false);
        }
    }

    @Override
    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 2 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            if(i!=btnIdx) {
                buttons[i].reset();
            }
        }
    }
    @Override
    public void resetAllButtons() {
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].reset();
        }
        for (DropdownMenu ddm : dropdownMenus) {
            ddm.setSelectedIndex(0);
        }
    }
    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.rarityButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.RARITY;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.classButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.CLASS;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.nameButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.NAME;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.MOD;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.slotAddButton) {
            AbstractDungeon.player.potionSlots ++;
            AbstractDungeon.player.potions.add(new PotionSlot(AbstractDungeon.player.potionSlots - 1));
        } else if (button == this.slotSubButton) {
            if(AbstractDungeon.player.potionSlots > 0) {
                AbstractDungeon.player.potionSlots --;
//            if(!(AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1) instanceof PotionSlot))
                AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1);
            }

        } else {
            return;
        }
        this.justSorted = true;
        if (button != this.slotSubButton && button != this.slotAddButton)
            button.setActive(true);

    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }
}
