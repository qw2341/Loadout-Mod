package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.screens.mainMenu.SortHeaderButton;
import com.megacrit.cardcrawl.screens.mainMenu.SortHeaderButtonListener;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import loadout.LoadoutMod;

import java.lang.reflect.Field;

import static loadout.LoadoutMod.RELIC_OBTAIN_AMOUNT;

public class PotionSelectSortHeader implements HeaderButtonPlusListener {

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PotionSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final PotionStrings pStrings = CardCrawlGame.languagePack.getPotionString("Potion Slot");
    public static final String POTION_SLOT_NAME = pStrings.NAME;
    public boolean justSorted = false;

    public static final float START_X = 600.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;
    private HeaderButtonPlus rarityButton;
    private HeaderButtonPlus classButton;
    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus modButton;
    private HeaderButtonPlus slotAddButton;
    private HeaderButtonPlus slotSubButton;
    public HeaderButtonPlus[] buttons;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public PotionSelectScreen potionSelectScreen;



    public PotionSelectSortHeader(PotionSelectScreen potionSelectScreen) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
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
        this.potionSelectScreen = potionSelectScreen;

    }


    public void update() {
        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }
    }

    public Hitbox updateControllerInput() {
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }
        return null;
    }

    public int getHoveredIndex() {
        int retVal = 0;
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return retVal;
            }
            retVal++;
        }
        return 0;
    }

    public void clearActiveButtons() {
        //does not clear the last 3 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].setActive(false);
        }
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            if(i!=btnIdx) {
                buttons[i].reset();
            }
        }
    }
    public void resetAllButtons() {
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].reset();
        }
    }
    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.rarityButton) {
            clearActiveButtons();
            this.potionSelectScreen.sortByRarity(isAscending);
            resetOtherButtons();
        } else if (button == this.classButton) {
            clearActiveButtons();
            this.potionSelectScreen.sortByClass(isAscending);
            resetOtherButtons();
        } else if (button == this.nameButton) {
            clearActiveButtons();
            this.potionSelectScreen.sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.potionSelectScreen.sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.slotAddButton) {
            AbstractDungeon.player.potionSlots ++;
            AbstractDungeon.player.potions.add(new PotionSlot(AbstractDungeon.player.potionSlots - 1));
        } else if (button == this.slotSubButton) {
            AbstractDungeon.player.potionSlots --;
//            if(!(AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1) instanceof PotionSlot))
                AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1);
        } else {
            return;
        }
        this.justSorted = true;
        if (button != this.slotSubButton && button != this.slotAddButton)
            button.setActive(true);

    }

    public void render(SpriteBatch sb) {
        //sb.draw(ImageMaster.COLOR_TAB_BAR, 10.0F, -50.0F, 300.0F, 500.0F, 0, 0, 1334, 102, false, false);
        updateScrollPositions();
        renderButtons(sb);
        renderSelection(sb);
    }

    protected void updateScrollPositions() {

    }

    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }
    }

    protected void renderSelection(SpriteBatch sb) {
        for (int i = 0; i < this.buttons.length; i++) {
            if (i == this.selectionIndex) {
                this.selectionColor.a = 0.7F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F;
                sb.setColor(this.selectionColor);
                float doop = 1.0F + (1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L))) / 50.0F;

                sb.draw(img, (this.buttons[this.selectionIndex]).hb.cX - 80.0F - (this.buttons[this.selectionIndex]).textWidth / 2.0F * Settings.scale, (this.buttons[this.selectionIndex]).hb.cY - 43.0F, 100.0F, 43.0F, 160.0F + (this.buttons[this.selectionIndex]).textWidth, 86.0F, Settings.scale * doop, Settings.scale * doop, 0.0F, 0, 0, 200, 86, false, false);
            }
        }
    }
}
