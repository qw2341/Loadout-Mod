package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.PotionSlot;
import loadout.LoadoutMod;

public class PowerSelectSortHeader implements HeaderButtonPlusListener {

    private static final UIStrings pUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PotionSelectSortHeader"));
    public static final String[] pTEXT = pUiStrings.TEXT;

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PowerSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;


    public boolean justSorted = false;

    public static final float START_X = 200.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;
    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus modButton;
    private HeaderButtonPlus resetAllButton;
    private HeaderButtonPlus clearAllEffectsButton;
    public HeaderButtonPlus[] buttons;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public PowerSelectScreen powerSelectScreen;



    public PowerSelectSortHeader(PowerSelectScreen powerSelectScreen) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        float xPosition = START_X;
        float yPosition = START_Y;

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2*SPACE_Y;
        this.resetAllButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.resetAllButton.isIcon = true;
        this.resetAllButton.isAscending = true;
        this.resetAllButton.texture = ImageMaster.WARNING_ICON_VFX;
        yPosition -= SPACE_Y;
        this.clearAllEffectsButton = new HeaderButtonPlus(TEXT[3], xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.clearAllEffectsButton.isIcon = true;
        this.clearAllEffectsButton.texture = ImageMaster.MAP_NODE_REST;
        this.clearAllEffectsButton.isAscending = true;
        this.buttons = new HeaderButtonPlus[] {  this.nameButton, this.modButton, this.resetAllButton, this.clearAllEffectsButton};
        this.powerSelectScreen = powerSelectScreen;

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
        if (button == this.nameButton) {
            clearActiveButtons();
            this.powerSelectScreen.sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.powerSelectScreen.sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.resetAllButton) {
            this.powerSelectScreen.resetPowerAmounts();
        } else if (button == this.clearAllEffectsButton) {
            AbstractDungeon.actionManager.addToBottom(new RemoveAllPowersAction(AbstractDungeon.player,false));
        } else {
            return;
        }
        this.justSorted = true;
        if (button != this.clearAllEffectsButton && button != this.resetAllButton)
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
