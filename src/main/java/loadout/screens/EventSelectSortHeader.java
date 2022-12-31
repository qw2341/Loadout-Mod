package loadout.screens;


import basemod.BaseMod;
import basemod.eventUtil.EventUtils;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.KeywordStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import loadout.LoadoutMod;

import java.util.*;

import static loadout.LoadoutMod.*;

public class EventSelectSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {

    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] rTEXT = rUiStrings.TEXT;

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardSelectSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;


    public boolean justSorted = false;

    public float startX = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 200.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;

    private HeaderButtonPlus modButton;
    private HeaderButtonPlus nameButton;

    private DropdownMenu modNameDropdown;


    private String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public EventSelectScreen eventSelectScreen;
    private ArrayList<String> eventMods;
    private HashMap<String,String> eventModNames;

    private TextSearchBox textSearchBox;



    public EventSelectSortHeader(EventSelectScreen eventSelectScreen, float startX) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        this.startX = startX;
        float xPosition = this.startX - 75.0f;
        float yPosition = START_Y - 450.0f * Settings.yScale;

        this.eventSelectScreen = eventSelectScreen;

        this.nameButton = new HeaderButtonPlus(rTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(rTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;

        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton };


        eventMods = new ArrayList<>(findEventAddingMods());
        eventMods.remove("Slay the Spire");
        eventMods.sort(Comparator.naturalOrder());

        ArrayList<String> f = new ArrayList<>();
        String fName;
        for (String id : eventMods) {
            fName = eventModNames.get(id);
            if (fName.length() >= 12) fName = fName.substring(0,11) + "...";
            f.add(fName);
        }



        f.add(0,TEXT[0]);
        f.add("Slay the Spire");
        this.modNameDropdown = new DropdownMenu(this, f,FontHelper.panelNameFont, Settings.CREAM_COLOR);




        this.dropdownMenus = new DropdownMenu[] {this.modNameDropdown};
        this.dropdownMenuHeaders = new String[] {"Mod"};

        //this.textSearchBox = new TextSearchBox(this, xPosition, START_Y - 250.0f * Settings.yScale);
    }

    private HashSet<String> findEventAddingMods() {

        eventModNames = this.eventSelectScreen.eventModNames;

        return this.eventSelectScreen.eventAddingMods;
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
            ddm.setSelectedIndex(0);
        }

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.nameButton) {
            clearActiveButtons();
            this.eventSelectScreen.sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.eventSelectScreen.sortByMod(isAscending);
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
        float yPos = START_Y;

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

        if (dropdownMenu == this.modNameDropdown) {
            if (i == 0) {
                //if showing all
                eventSelectScreen.filterMod = null;
            } else if (i == (eventMods.size() + 2) - 1) {
                eventSelectScreen.filterMod = "Slay the Spire";
            } else {
                eventSelectScreen.filterMod = eventMods.get(i - 1);
            }
            eventSelectScreen.updateFilters();
        }
    }
}

