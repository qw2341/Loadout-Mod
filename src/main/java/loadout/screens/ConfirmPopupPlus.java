package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.SaveHelper;
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveAndContinue;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.mainMenu.SaveSlot;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen.CurScreen;
import com.megacrit.cardcrawl.screens.mainMenu.SaveSlotScreen.CurrentPopup;
import com.megacrit.cardcrawl.screens.stats.StatsScreen;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfirmPopupPlus {

    protected static final String[] TEXT;
    public String title;
    public String desc;
    public Hitbox yesHb;
    public Hitbox noHb;
    public boolean shown = false;
    protected Color screenColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
    protected Color uiColor = new Color(1.0F, 1.0F, 1.0F, 0.0F);
    protected Color headerColor;
    protected Color descriptionColor;
    protected float targetAlpha;
    protected float targetAlpha2;
    protected static final int CONFIRM_W = 360;
    protected static final int CONFIRM_H = 414;
    protected static final int YES_W = 173;
    protected static final int NO_W = 161;
    protected static final int BUTTON_H = 74;
    protected static final float SCREEN_DARKNESS = 0.75F;

    protected Runnable yesAction;
    protected Runnable noAction;


    private void initializeButtons() {
        if (Settings.isMobile) {
            this.yesHb = new Hitbox(240.0F * Settings.scale, 110.0F * Settings.scale);
            this.noHb = new Hitbox(240.0F * Settings.scale, 110.0F * Settings.scale);
            this.yesHb.move(810.0F * Settings.xScale, Settings.OPTION_Y - 162.0F * Settings.scale);
            this.noHb.move(1112.0F * Settings.xScale, Settings.OPTION_Y - 162.0F * Settings.scale);
        } else {
            this.yesHb = new Hitbox(160.0F * Settings.scale, 70.0F * Settings.scale);
            this.noHb = new Hitbox(160.0F * Settings.scale, 70.0F * Settings.scale);
            this.yesHb.move(860.0F * Settings.xScale, Settings.OPTION_Y - 118.0F * Settings.scale);
            this.noHb.move(1062.0F * Settings.xScale, Settings.OPTION_Y - 118.0F * Settings.scale);
        }

    }


    public ConfirmPopupPlus(String title, String desc, Runnable yesAction, Runnable noAction) {
        this.headerColor = Settings.GOLD_COLOR.cpy();
        this.descriptionColor = Settings.CREAM_COLOR.cpy();
        this.targetAlpha = 0.0F;
        this.targetAlpha2 = 0.0F;

        this.title = title;
        this.desc = desc;
        this.yesAction = yesAction;
        this.noAction = noAction;
        this.initializeButtons();
    }

    public void show() {
        if (!this.shown) {
            this.shown = true;
        }

    }

    public void hide() {
        if (this.shown) {
            this.shown = false;
        }

    }

    protected void updateTransparency() {
        if (this.shown) {
            this.screenColor.a = MathHelper.fadeLerpSnap(this.screenColor.a, 0.75F);
            this.uiColor.a = MathHelper.fadeLerpSnap(this.uiColor.a, 1.0F);
        } else {
            this.screenColor.a = MathHelper.fadeLerpSnap(this.screenColor.a, 0.0F);
            this.uiColor.a = MathHelper.fadeLerpSnap(this.uiColor.a, 0.0F);
        }

    }

    public void update() {
        this.updateTransparency();
        if (this.shown) {
            this.updateYes();
            this.updateNo();
        }

    }

    protected void updateYes() {
        this.yesHb.update();
        if (this.yesHb.justHovered) {
            CardCrawlGame.sound.play("UI_HOVER");
        } else if (InputHelper.justClickedLeft && this.yesHb.hovered) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            this.yesHb.clickStarted = true;
        } else if (this.yesHb.clicked) {
            this.yesHb.clicked = false;
            this.yesButtonEffect();
        }

        if (CInputActionSet.proceed.isJustPressed()) {
            CInputActionSet.proceed.unpress();
            this.yesHb.clicked = true;
        }

    }

    protected void updateNo() {
        this.noHb.update();
        if (this.noHb.justHovered) {
            CardCrawlGame.sound.play("UI_HOVER");
        } else if (this.noHb.hovered && InputHelper.justClickedLeft) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            this.noHb.clickStarted = true;
        } else if (this.noHb.clicked) {
            this.noHb.clicked = false;
            this.noButtonEffect();
            this.hide();
        }

        if (CInputActionSet.cancel.isJustPressed() || InputActionSet.cancel.isJustPressed()) {
            CInputActionSet.cancel.unpress();
            this.noButtonEffect();
        }

    }

    protected void noButtonEffect() {
        this.noAction.run();
        this.hide();
    }

    protected void yesButtonEffect() {
        this.yesAction.run();
        this.hide();
    }

    public void render(SpriteBatch sb) {
        this.renderPopupBg(sb);
        this.renderText(sb);
        if (this.shown) {
            this.renderButtons(sb);
        }

        this.renderControllerUi(sb);
    }

    protected void renderPopupBg(SpriteBatch sb) {
        sb.setColor(this.screenColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, (float)Settings.WIDTH, (float)Settings.HEIGHT);
        sb.setColor(this.uiColor);
        if (!Settings.isMobile) {
            sb.draw(ImageMaster.OPTION_CONFIRM, (float)Settings.WIDTH / 2.0F - 180.0F, Settings.OPTION_Y - 207.0F, 180.0F, 207.0F, 360.0F, 414.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 360, 414, false, false);
        } else {
            sb.draw(ImageMaster.OPTION_CONFIRM, (float)Settings.WIDTH / 2.0F - 180.0F, Settings.OPTION_Y - 207.0F, 180.0F, 207.0F, 360.0F, 414.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 360, 414, false, false);
        }

    }

    private void renderButtons(SpriteBatch sb) {
        if (Settings.isMobile) {
            sb.draw(ImageMaster.OPTION_YES, (float)Settings.WIDTH / 2.0F - 86.5F - 150.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 170.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 173, 74, false, false);
        } else {
            sb.draw(ImageMaster.OPTION_YES, (float)Settings.WIDTH / 2.0F - 86.5F - 100.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 173, 74, false, false);
        }

        if (this.yesHb.hovered) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, this.uiColor.a * 0.25F));
            sb.setBlendFunction(770, 1);
            if (Settings.isMobile) {
                sb.draw(ImageMaster.OPTION_YES, (float)Settings.WIDTH / 2.0F - 86.5F - 150.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 170.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 173, 74, false, false);
            } else {
                sb.draw(ImageMaster.OPTION_YES, (float)Settings.WIDTH / 2.0F - 86.5F - 100.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 173, 74, false, false);
            }

            sb.setBlendFunction(770, 771);
            sb.setColor(this.uiColor);
            if (Settings.isMobile) {
                FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[2], (float)Settings.WIDTH / 2.0F - 165.0F * Settings.scale, Settings.OPTION_Y - 162.0F * Settings.scale, this.uiColor, 1.0F);
            } else {
                FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[2], (float)Settings.WIDTH / 2.0F - 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, this.uiColor, 1.0F);
            }
        } else if (Settings.isMobile) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[2], (float)Settings.WIDTH / 2.0F - 165.0F * Settings.scale, Settings.OPTION_Y - 162.0F * Settings.scale, this.headerColor, 1.0F);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[2], (float)Settings.WIDTH / 2.0F - 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, this.headerColor, 1.0F);
        }

        if (Settings.isMobile) {
            sb.draw(ImageMaster.OPTION_NO, (float)Settings.WIDTH / 2.0F - 80.5F + 160.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 170.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 161, 74, false, false);
        } else {
            sb.draw(ImageMaster.OPTION_NO, (float)Settings.WIDTH / 2.0F - 80.5F + 106.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 161, 74, false, false);
        }

        if (this.noHb.hovered) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, this.uiColor.a * 0.25F));
            sb.setBlendFunction(770, 1);
            if (Settings.isMobile) {
                sb.draw(ImageMaster.OPTION_NO, (float)Settings.WIDTH / 2.0F - 80.5F + 160.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 170.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale * 1.5F, Settings.scale * 1.5F, 0.0F, 0, 0, 161, 74, false, false);
            } else {
                sb.draw(ImageMaster.OPTION_NO, (float)Settings.WIDTH / 2.0F - 80.5F + 106.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 161, 74, false, false);
            }

            sb.setBlendFunction(770, 771);
            sb.setColor(this.uiColor);
            if (Settings.isMobile) {
                FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[3], (float)Settings.WIDTH / 2.0F + 165.0F * Settings.scale, Settings.OPTION_Y - 162.0F * Settings.scale, this.uiColor, 1.0F);
            } else {
                FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[3], (float)Settings.WIDTH / 2.0F + 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, this.uiColor, 1.0F);
            }
        } else if (Settings.isMobile) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, TEXT[3], (float)Settings.WIDTH / 2.0F + 165.0F * Settings.scale, Settings.OPTION_Y - 162.0F * Settings.scale, this.headerColor, 1.0F);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[3], (float)Settings.WIDTH / 2.0F + 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, this.headerColor, 1.0F);
        }

        this.yesHb.render(sb);
        this.noHb.render(sb);
    }

    private void renderText(SpriteBatch sb) {
        this.headerColor.a = this.uiColor.a;
        if (Settings.isMobile) {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.title, (float)Settings.WIDTH / 2.0F, Settings.OPTION_Y + 200.0F * Settings.scale, this.headerColor, 1.2F);
        } else {
            FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, this.title, (float)Settings.WIDTH / 2.0F, Settings.OPTION_Y + 126.0F * Settings.scale, this.headerColor);
        }

        this.descriptionColor.a = this.uiColor.a;
        if (Settings.isMobile) {
            FontHelper.renderWrappedText(sb, FontHelper.panelNameFont, this.desc, (float)Settings.WIDTH / 2.0F, Settings.OPTION_Y + 30.0F * Settings.scale, 380.0F * Settings.scale, this.descriptionColor, 1.0F);
        } else {
            FontHelper.renderWrappedText(sb, FontHelper.tipBodyFont, this.desc, (float)Settings.WIDTH / 2.0F, Settings.OPTION_Y + 20.0F * Settings.scale, 240.0F * Settings.scale, this.descriptionColor, 1.0F);
        }

    }

    private void renderControllerUi(SpriteBatch sb) {
        if (Settings.isControllerMode) {
            sb.draw(CInputActionSet.proceed.getKeyImg(), 770.0F * Settings.xScale - 32.0F, Settings.OPTION_Y - 32.0F - 140.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
            sb.draw(CInputActionSet.cancel.getKeyImg(), 1150.0F * Settings.xScale - 32.0F, Settings.OPTION_Y - 32.0F - 140.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        }

    }

    static {
        TEXT = CardCrawlGame.languagePack.getUIString("ConfirmPopup").TEXT;
    }

}

