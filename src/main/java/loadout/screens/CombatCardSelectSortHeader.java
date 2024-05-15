package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.KeywordStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import loadout.LoadoutMod;
import loadout.util.ModConfig;

import static loadout.LoadoutMod.*;

public class CombatCardSelectSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {
    private static final UIStrings cUIStrings = CardCrawlGame.languagePack.getUIString("CardLibraryScreen");
    public static final String[] cTEXT = cUIStrings.TEXT;
    private static final UIStrings clUIStrings = CardCrawlGame.languagePack.getUIString("CardLibSortHeader");
    public static final String[] clTEXT = clUIStrings.TEXT;
    private static final UIStrings isUIStrings = CardCrawlGame.languagePack.getUIString("InputSettingsScreen");
    public static final String[] isTEXT = isUIStrings.TEXT;

    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] rTEXT = rUiStrings.TEXT;

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardSelectSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    private static final UIStrings sUiStrings = CardCrawlGame.languagePack.getUIString("SingleCardViewPopup");
    public static final String[] sTEXT = sUiStrings.TEXT;

    private static final KeywordStrings kKeyString = CardCrawlGame.languagePack.getKeywordString("Game Dictionary");
    public static final String[] kTEXT = kKeyString.UNPLAYABLE.NAMES;

    public boolean justSorted = false;

    public float startX = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 200.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;

    private HeaderButtonPlus masterDeckButton;
    private HeaderButtonPlus drawPileButton;
    private HeaderButtonPlus handPileButton;
    private HeaderButtonPlus discardPileButton;
    private HeaderButtonPlus exhaustPileButton;
    private DropdownMenu selectionModeButton;


    private String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public GCardSelectScreen cardSelectScreen;




    public CombatCardSelectSortHeader(GCardSelectScreen cardSelectScreen, float startX) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");

        this.startX = startX;
        float xPosition = this.startX;
        float yPosition = START_Y - 450.0f * Settings.yScale;

        this.masterDeckButton = new HeaderButtonPlus(isTEXT[10], xPosition,yPosition,this,true,ImageMaster.DECK_ICON);
        yPosition -= SPACE_Y;
        this.drawPileButton = new HeaderButtonPlus(isTEXT[11], xPosition,yPosition,this,true,ImageMaster.DECK_BTN_BASE);
        yPosition -= SPACE_Y;
        this.handPileButton = new HeaderButtonPlus(TEXT[2],xPosition,yPosition,this,true,ImageMaster.CHAR_SELECT_IRONCLAD);
        yPosition -= SPACE_Y;
        this.discardPileButton = new HeaderButtonPlus(isTEXT[12], xPosition,yPosition,this,true,ImageMaster.DISCARD_BTN_BASE);
        yPosition -= SPACE_Y;
        this.exhaustPileButton = new HeaderButtonPlus(isTEXT[13], xPosition,yPosition,this,true,ImageMaster.WARNING_ICON_VFX);


        this.buttons = new HeaderButtonPlus[] { this.masterDeckButton, this.drawPileButton, this.handPileButton, this.discardPileButton, this.exhaustPileButton};


        String[] b = new String[2];
        b[0]= rTEXT[8];
        b[1]= rTEXT[9];
        this.selectionModeButton = new DropdownMenu(this,b,FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.selectionModeButton.setSelectedIndex(ModConfig.enableDrag ? 0 : 1);

        this.dropdownMenus = new DropdownMenu[] {this.selectionModeButton};
        this.dropdownMenuHeaders = new String[] {TEXT[1]};
        this.cardSelectScreen = cardSelectScreen;

    }

    public void update() {
        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }
        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            if (dropdownMenu.isOpen) {
                dropdownMenu.update();
                return;
            }
        }

        for (DropdownMenu dropdownMenu : this.dropdownMenus)
            dropdownMenu.update();
    }

    public Hitbox updateControllerInput() {
        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            Hitbox hb = dropdownMenu.getHitbox();
            if (hb.hovered) {
                return hb;
            }
        }
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        return null;
    }

    public void setActiveButton(int idx) {
        if(idx < this.buttons.length)
            this.buttons[idx].setActive(true);
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
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            button.setActive(false);
        }
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        for (int i = 0;i<this.buttons.length;i++) {
            if (i!= btnIdx) {
                HeaderButtonPlus button = buttons[i];

                button.reset();

            }
        }
    }
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];

            button.reset();

        }
        for (DropdownMenu ddm : dropdownMenus) {
            if (ddm != this.selectionModeButton) {
                ddm.setSelectedIndex(0);
            } else {
                this.selectionModeButton.setSelectedIndex(ModConfig.enableDrag ? 0 : 1);
            }
        }

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.masterDeckButton) {
            clearActiveButtons();
            this.cardSelectScreen.currentPool = GCardSelectScreen.ViewingPool.MASTER_DECK;
            this.cardSelectScreen.targetGroup = AbstractDungeon.player.masterDeck;
            resetOtherButtons();
        } else if (button == this.drawPileButton) {
            clearActiveButtons();
            this.cardSelectScreen.currentPool = GCardSelectScreen.ViewingPool.DRAW;
            this.cardSelectScreen.targetGroup = AbstractDungeon.player.drawPile;
            resetOtherButtons();
        } else if (button == this.handPileButton) {
            clearActiveButtons();
            this.cardSelectScreen.currentPool = GCardSelectScreen.ViewingPool.HAND;
            this.cardSelectScreen.targetGroup = AbstractDungeon.player.hand;
            this.cardSelectScreen.targetGroup.refreshHandLayout();
            resetOtherButtons();
        } else if (button == this.discardPileButton) {
            clearActiveButtons();
            this.cardSelectScreen.currentPool = GCardSelectScreen.ViewingPool.DISCARD;
            this.cardSelectScreen.targetGroup = AbstractDungeon.player.discardPile;
            resetOtherButtons();
        } else if (button == this.exhaustPileButton) {
            clearActiveButtons();
            this.cardSelectScreen.currentPool = GCardSelectScreen.ViewingPool.EXHAUST;
            this.cardSelectScreen.targetGroup = AbstractDungeon.player.exhaustPile;
            resetOtherButtons();
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);

    }

    public void render(SpriteBatch sb) {
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

        float spaceY = 52.0f * Settings.yScale;
        float yPos = START_Y - 7.0f * spaceY;

        float xPos = 0.0f;

        for (int i = 0; i< this.dropdownMenus.length ; i++) {

            DropdownMenu ddm = this.dropdownMenus[i];

            ddm.render(sb,xPos,yPos);
            yPos += 0.5f * spaceY;
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
            yPos += spaceY;
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

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if (dropdownMenu == this.selectionModeButton) {
            ModConfig.enableDrag = i == 0;
            try {
                ModConfig.config.setBool(ModConfig.ENABLE_DRAG_SELECT, ModConfig.enableDrag);
                ModConfig.config.save();
            } catch (NullPointerException nPE) {
                logger.debug("null pointer exception caught, caused by selection mode dropdown");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
