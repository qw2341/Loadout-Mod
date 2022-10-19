package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.compendium.CardLibSortHeader;
import com.megacrit.cardcrawl.screens.mainMenu.SortHeaderButton;
import com.megacrit.cardcrawl.screens.mainMenu.SortHeaderButtonListener;
import jdk.javadoc.internal.doclets.formats.html.markup.Head;

public class HeaderButtonPlus extends SortHeaderButton {
    public Hitbox hb;
    public boolean isAscending;
    public boolean isActive;
    public float x;
    public float y;
    private String text;
    public HeaderButtonPlusListener delegate;
    private final float ARROW_SIZE;
    public float textWidth;
    public boolean isSorting;
    public boolean isToggle;

    public boolean isIcon;


    public Texture texture;
    public enum Alignment {
        LEFT,CENTER,RIGHT
    }
    public Alignment alignment;

    public HeaderButtonPlus(String text, float cx, float cy) {
        super(text, cx, cy);
        this.isAscending = false;
        this.isActive = false;
        this.isSorting = true;
        this.isToggle = false;
        this.ARROW_SIZE = 32.0F;
        this.texture = ImageMaster.FILTER_ARROW;
        this.isIcon = false;

        this.text = text;
        this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text, Float.MAX_VALUE, 0.0F);
        this.hb = new Hitbox(100.0F * Settings.xScale, 48.0F * Settings.scale);
        this.hb.move(cx, cy);
        this.x = cx;
        this.y = cy;
    }

    public HeaderButtonPlus(String text, float cx, float cy, HeaderButtonPlusListener delegate) {
        this(text, cx, cy);
        this.delegate = delegate;
    }
    public HeaderButtonPlus(String text, float cx, float cy, HeaderButtonPlusListener delegate, boolean isSorting, boolean isToggle, Alignment alignment) {
        this(text, cx, cy);
        this.delegate = delegate;
        this.isSorting = isSorting;
        this.isToggle = isToggle;
        this.alignment = alignment;
//        switch (alignment) {
//            case RIGHT:
//                this.hb.move(cx - this.textWidth / 2.0F, cy);
//                break;
//            case CENTER:
//                this.hb.move(cx, cy);
//                break;
//            case LEFT:
//                this.hb.move(cx + this.textWidth / 2.0F, cy);
//                break;
//        }
    }

    public HeaderButtonPlus(String text, float cx, float cy, HeaderButtonPlusListener delegate, boolean isIcon, Texture icon) {
        this(text,cx,cy,delegate,false,false,Alignment.CENTER);
        this.isIcon = isIcon;
        this.texture = icon;
        if(isIcon) {
            //this.hb.resize(40,40);
            this.isAscending = true;
        }
    }

    public void update() {
        this.hb.update();
        if (this.hb.justHovered) {
            CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
        }

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            this.hb.clickStarted = true;
        }

        if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
            this.hb.clicked = false;

            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            if (this.isSorting) {
                this.isAscending = !this.isAscending;
                if (this.delegate instanceof RelicSelectSortHeader) ((RelicSelectSortHeader)this.delegate).clearActiveButtons();
                else if (this.delegate instanceof PotionSelectSortHeader) ((PotionSelectSortHeader)this.delegate).clearActiveButtons();
                else if (this.delegate instanceof CardSelectSortHeader) ((CardSelectSortHeader)this.delegate).clearActiveButtons();
            } else if (this.isToggle) {
                this.isAscending = !this.isAscending;
            }

            this.delegate.didChangeOrder(this, this.isAscending);
        }
        if(isToggle) {
            this.isActive = isAscending;
        }

    }

    public void updateScrollPosition(float newY) {
        this.hb.move(this.hb.cX, newY);
    }

    public void render(SpriteBatch sb) {
        Color color = !this.hb.hovered && !this.isActive ? Settings.CREAM_COLOR : Settings.GOLD_COLOR;
        float arrowCenterOffset = 16.0F;

        if (this.alignment == Alignment.CENTER) {
            FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, this.text, this.x, this.y, color);
            sb.setColor(color);
            if (isSorting) {
                sb.draw(ImageMaster.FILTER_ARROW, this.x - arrowCenterOffset + this.textWidth / 2.0F + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, !this.isAscending);
            } else if (isToggle) {
                sb.draw(ImageMaster.OPTION_TOGGLE, this.x - arrowCenterOffset + this.textWidth / 2.0F + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
                if(isActive)
                    sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x - arrowCenterOffset + this.textWidth / 2.0F + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
            } else {
                sb.draw(this.texture, this.x - arrowCenterOffset + this.textWidth / 2.0F + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, this.texture.getWidth(), this.texture.getHeight(), false, !this.isAscending);
            }
        } else if (this.alignment == Alignment.RIGHT) {
            FontHelper.renderFontRightAligned(sb, FontHelper.topPanelInfoFont, this.text, this.x, this.y, color);
            sb.setColor(color);
            if (isSorting) {
                sb.draw(ImageMaster.FILTER_ARROW, this.x - arrowCenterOffset + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, !this.isAscending);
            } else if (isToggle) {
                sb.draw(ImageMaster.OPTION_TOGGLE, this.x - arrowCenterOffset + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
                if(isActive)
                    sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x - arrowCenterOffset + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
            } else {
                sb.draw(this.texture, this.x - arrowCenterOffset + arrowCenterOffset * Settings.xScale, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, this.texture.getWidth(), this.texture.getHeight(), false, !this.isAscending);
            }
        } else if (this.alignment == Alignment.LEFT) {
            FontHelper.renderFontLeft(sb, FontHelper.topPanelInfoFont, this.text, this.x, this.y, color);
            sb.setColor(color);
            if (isSorting) {
                sb.draw(ImageMaster.FILTER_ARROW, this.x + arrowCenterOffset * Settings.xScale - arrowCenterOffset, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, !this.isAscending);
            } else if (isToggle) {
                sb.draw(ImageMaster.OPTION_TOGGLE, this.x + arrowCenterOffset * Settings.xScale - arrowCenterOffset, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
                if(isActive)
                    sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x + arrowCenterOffset * Settings.xScale - arrowCenterOffset, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 32, 32, false, false);
            } else {
                sb.draw(this.texture, this.x + arrowCenterOffset * Settings.xScale - arrowCenterOffset, this.y - arrowCenterOffset, 16.0F, 16.0F, 32.0F, 32.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, this.texture.getWidth(), this.texture.getHeight(), false, !this.isAscending);
            }
        }
        this.hb.render(sb);
    }

    public void reset() {
        if (!isIcon) this.isAscending = false;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
