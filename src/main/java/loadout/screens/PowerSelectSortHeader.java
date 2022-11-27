package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.unique.RemoveAllPowersAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import loadout.LoadoutMod;
import loadout.relics.PowerGiver;
import loadout.savables.Favorites;

public class PowerSelectSortHeader extends AbstractSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {

    private static final UIStrings pUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PotionSelectSortHeader"));
    public static final String[] pTEXT = pUiStrings.TEXT;

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PowerSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;

    private static final UIStrings cUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardSelectSortHeader"));
    public static final String[] cTEXT = cUiStrings.TEXT;

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

    private DropdownMenu typeButton;
    private DropdownMenu targetSelectMenu;
    public DropdownMenu[] dropdownMenus;
    public String[] dropdownMenuHeaders;

    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public PowerSelectScreen selectScreen;

    //public TextSearchBox searchBox;



    public PowerSelectSortHeader(PowerSelectScreen powerSelectScreen) {
        super(powerSelectScreen);
        this.selectScreen = powerSelectScreen;

        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        float xPosition = START_X;
        float yPosition = START_Y;
        this.searchBox = new TextSearchBox(this, 0.0F, Settings.HEIGHT - 200.0F * Settings.scale,false);

        this.nameButton = new HeaderButtonPlus(pTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(pTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2*SPACE_Y;
        this.resetAllButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.resetAllButton.isIcon = true;
        this.resetAllButton.isAscending = true;
        this.resetAllButton.texture = ImageMaster.WARNING_ICON_VFX;
        this.resetAllButton.hb.resize(200.0F * Settings.xScale, 48.0F * Settings.scale);
        this.resetAllButton.hb.moveX(xPosition - 100.0f);
        yPosition -= SPACE_Y;
        this.clearAllEffectsButton = new HeaderButtonPlus(TEXT[3], xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.clearAllEffectsButton.isIcon = true;
        this.clearAllEffectsButton.texture = ImageMaster.MAP_NODE_REST;
        this.clearAllEffectsButton.isAscending = true;
        this.clearAllEffectsButton.hb.resize(200.0F * Settings.xScale, 48.0F * Settings.scale);
        this.clearAllEffectsButton.hb.moveX(xPosition - 100.0f);



        this.buttons = new HeaderButtonPlus[] {  this.nameButton, this.modButton, this.resetAllButton, this.clearAllEffectsButton};

        this.targetSelectMenu = new DropdownMenu(this, new String[] {TEXT[4],TEXT[5]}, FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.targetSelectMenu.setSelectedIndex(powerSelectScreen.currentTarget.ordinal());

        this.typeButton = new DropdownMenu(this, new String[] {cTEXT[0],TEXT[8]}, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.typeButton.setSelectedIndex(!Favorites.favoritePowers.isEmpty() ? 1 : 0);

        this.dropdownMenus = new DropdownMenu[] {this.targetSelectMenu, this.typeButton};
        this.dropdownMenuHeaders = new String[] {TEXT[6],TEXT[7]};


        }

    @Override
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

        for (DropdownMenu dm : this.dropdownMenus) {
            dm.update();
        }

        this.searchBox.update();
    }

    public Hitbox updateControllerInput() {
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        for (DropdownMenu dm: this.dropdownMenus) {
            if(dm.getHitbox().hovered)
                return dm.getHitbox();
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

    @Override
    public void clearActiveButtons() {
        //does not clear the last 3 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].setActive(false);
        }
    }
    @Override
    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 3 buttons
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
    }
    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.nameButton) {
            clearActiveButtons();
            this.selectScreen.sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.selectScreen.sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.resetAllButton) {
            this.selectScreen.resetPowerAmounts();
        } else if (button == this.clearAllEffectsButton) {
            if(this.selectScreen.currentTarget == PowerGiver.PowerTarget.PLAYER)
                AbstractDungeon.actionManager.addToBottom(new RemoveAllPowersAction(AbstractDungeon.player,false));
            else if (this.selectScreen.currentTarget == PowerGiver.PowerTarget.MONSTER) {
                for (AbstractMonster am : AbstractDungeon.getMonsters().monsters) {
                    AbstractDungeon.actionManager.addToBottom(new RemoveAllPowersAction(am,false));
                }
            }

        } else {
            return;
        }
        this.justSorted = true;
        if (button != this.clearAllEffectsButton && button != this.resetAllButton)
            button.setActive(true);

    }

    @Override
    public void render(SpriteBatch sb) {
        //sb.draw(ImageMaster.COLOR_TAB_BAR, 10.0F, -50.0F, 300.0F, 500.0F, 0, 0, 1334, 102, false, false);
        updateScrollPositions();
        this.searchBox.render(sb);
        renderButtons(sb);
        renderSelection(sb);
    }

    protected void updateScrollPositions() {

    }

    @Override
    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }

        float spaceY = 52.0f * Settings.yScale;
        float yPos = START_Y - 9.0f * spaceY;

        float xPos = 20.0f * Settings.scale;

        for (int i = 0; i< this.dropdownMenus.length ; i++) {

            DropdownMenu ddm = this.dropdownMenus[i];

            ddm.render(sb,xPos,yPos);
            yPos += 0.5f * spaceY;
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
            yPos += spaceY;
        }
    }

    @Override
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
        if (dropdownMenu == this.targetSelectMenu) {
            this.selectScreen.currentTarget = PowerGiver.PowerTarget.values()[i];
            this.selectScreen.refreshPowersForTarget();
        }
        if(dropdownMenu == this.typeButton) {
            if(i == 1) {
                this.selectScreen.filterFavorites = true;


            } else if (i==0) {
                this.selectScreen.filterFavorites = false;
            }
            this.selectScreen.filterAll = !this.selectScreen.filterFavorites;
            this.selectScreen.updateFilters();
        }
    }
}
