package loadout.screens;

import basemod.interfaces.TextReceiver;
import basemod.patches.com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor.TextInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.steamInput.SteamInputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import loadout.LoadoutMod;
import loadout.helper.EnhancedTextInputReceiver;
import loadout.helper.TextInputReceiver;
import org.apache.logging.log4j.Logger;



public class TextPopup implements TextReceiver
{
    private static final Logger logger = LoadoutMod.logger;


    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("RenamePanel");
    public static final String[] TEXT = uiStrings.TEXT;


    //private int slot = 0;
    //private boolean newSave = false;
    public static String textField = "";

    public boolean shown = false;
    public Hitbox yesHb = new Hitbox(160.0F * Settings.scale, 70.0F * Settings.scale); public Hitbox noHb = new Hitbox(160.0F * Settings.scale, 70.0F * Settings.scale); private static final int CONFIRM_W = 360;
    private static final int CONFIRM_H = 414;
    private static final int YES_W = 173;
    private static final int NO_W = 161;
    private static final int BUTTON_H = 74;
    private Color faderColor = new Color(0.0F, 0.0F, 0.0F, 0.0F);
    private Color uiColor = new Color(1.0F, 0.965F, 0.886F, 0.0F);
    private float waitTimer = 0.0F;

    private TextInputReceiver receiver;
    public String title;
    private boolean digitonly;
    public boolean isClearing;

    public TextPopup(TextInputReceiver receiver, String title, boolean digitonly, boolean isClearing) {
        this.yesHb.move(854.0F * Settings.scale, Settings.OPTION_Y - 120.0F * Settings.scale);
        this.noHb.move(1066.0F * Settings.scale, Settings.OPTION_Y - 120.0F * Settings.scale);
        this.receiver = receiver;
        this.title = title;
        this.digitonly = digitonly;
        this.isClearing = isClearing;
    }

    public void update() {
        if (Gdx.input.isKeyPressed(67) && !textField.equals("") && this.waitTimer <= 0.0F) {
            textField = textField.substring(0, textField.length() - 1);
            this.waitTimer = 0.09F;
        }

        if (this.waitTimer > 0.0F) {
            this.waitTimer -= Gdx.graphics.getDeltaTime();
        }

        if (this.shown) {
            this.faderColor.a = MathHelper.fadeLerpSnap(this.faderColor.a, 0.75F);
            this.uiColor.a = MathHelper.fadeLerpSnap(this.uiColor.a, 1.0F);
            updateButtons();


            if (Gdx.input.isKeyJustPressed(66)) {
                confirm();
            } else if (InputHelper.pressedEscape) {
                InputHelper.pressedEscape = false;
                cancel();
            }
        } else {
            this.faderColor.a = MathHelper.fadeLerpSnap(this.faderColor.a, 0.0F);
            this.uiColor.a = MathHelper.fadeLerpSnap(this.uiColor.a, 0.0F);
        }
    }


    private void updateButtons() {
        this.yesHb.update();
        if (this.yesHb.justHovered) {
            CardCrawlGame.sound.play("UI_HOVER");
        }

        if (this.yesHb.hovered && InputHelper.justClickedLeft) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            this.yesHb.clickStarted = true;
        } else if (this.yesHb.clicked) {
            confirm();
            this.yesHb.clicked = false;
        }


        this.noHb.update();
        if (this.noHb.justHovered) {
            CardCrawlGame.sound.play("UI_HOVER");
        }

        if (this.noHb.hovered && InputHelper.justClickedLeft) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            this.noHb.clickStarted = true;
        } else if (this.noHb.clicked) {
            cancel();
            this.noHb.clicked = false;
        }


        if (CInputActionSet.proceed.isJustPressed()) {
            CInputActionSet.proceed.unpress();
            confirm();
        } else if (CInputActionSet.cancel.isJustPressed() || InputActionSet.cancel.isJustPressed()) {
            CInputActionSet.cancel.unpress();
            cancel();
        }
    }

    public void confirm() {
        //textField = textField.trim();
        if (textField.equals("")) {
            return;
        }

        //CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop = SaveSlotScreen.CurrentPopup.NONE;
        this.shown = false;
        TextInput.stopTextReceiver(this);
        //Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
        //AbstractDungeon.screen = AbstractDungeon.previousScreen;
        receiver.setTextField(textField);
        if(receiver instanceof EnhancedTextInputReceiver) ((EnhancedTextInputReceiver)receiver).onConfirming();

    }


    public void cancel() {
        //CardCrawlGame.mainMenuScreen.saveSlotScreen.curPop = SaveSlotScreen.CurrentPopup.NONE;
        this.shown = false;
        TextInput.stopTextReceiver(this);
        //Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
        //AbstractDungeon.screen = AbstractDungeon.previousScreen;
        if(receiver instanceof EnhancedTextInputReceiver) ((EnhancedTextInputReceiver)receiver).onCanceling();
    }

    public void render(SpriteBatch sb) {
        sb.setColor(this.faderColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT);
        renderPopupBg(sb);
        renderTextbox(sb);
        renderHeader(sb);
        renderButtons(sb);
    }

    private void renderHeader(SpriteBatch sb) {
        Color c = Settings.GOLD_COLOR.cpy();
        c.a = this.uiColor.a;
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, title, Settings.WIDTH / 2.0F, Settings.OPTION_Y + 150.0F * Settings.scale, c);
    }








    private void renderPopupBg(SpriteBatch sb) {
        sb.setColor(this.uiColor);
        sb.draw(ImageMaster.OPTION_CONFIRM, Settings.WIDTH / 2.0F - 180.0F, Settings.OPTION_Y - 207.0F, 180.0F, 207.0F, 360.0F, 414.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 360, 414, false, false);
    }


















    private void renderTextbox(SpriteBatch sb) {
        sb.draw(ImageMaster.RENAME_BOX, Settings.WIDTH / 2.0F - 160.0F, Settings.OPTION_Y + 20.0F * Settings.scale - 160.0F, 160.0F, 160.0F, 320.0F, 320.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 320, 320, false, false);


















        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, textField, Settings.WIDTH / 2.0F - 120.0F * Settings.scale, Settings.OPTION_Y + 24.0F * Settings.scale, 100000.0F, 0.0F, this.uiColor, 0.82F);










        float tmpAlpha = (MathUtils.cosDeg((float)(System.currentTimeMillis() / 3L % 360L)) + 1.25F) / 3.0F * this.uiColor.a;
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "_", Settings.WIDTH / 2.0F - 122.0F * Settings.scale +



                FontHelper.getSmartWidth(FontHelper.cardTitleFont, textField, 1000000.0F, 0.0F, 0.82F), Settings.OPTION_Y + 24.0F * Settings.scale, 100000.0F, 0.0F, new Color(1.0F, 1.0F, 1.0F, tmpAlpha));
    }









    private void renderButtons(SpriteBatch sb) {
        sb.setColor(this.uiColor);
        Color c = Settings.GOLD_COLOR.cpy();
        c.a = this.uiColor.a;


        if (this.yesHb.clickStarted) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, this.uiColor.a * 0.9F));
            sb.draw(ImageMaster.OPTION_YES, Settings.WIDTH / 2.0F - 86.5F - 100.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 173, 74, false, false);
















            sb.setColor(new Color(this.uiColor));
        } else {
            sb.draw(ImageMaster.OPTION_YES, Settings.WIDTH / 2.0F - 86.5F - 100.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 173, 74, false, false);
        }

















        if (!this.yesHb.clickStarted && this.yesHb.hovered) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, this.uiColor.a * 0.25F));
            sb.setBlendFunction(770, 1);
            sb.draw(ImageMaster.OPTION_YES, Settings.WIDTH / 2.0F - 86.5F - 100.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 86.5F, 37.0F, 173.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 173, 74, false, false);
















            sb.setBlendFunction(770, 771);
            sb.setColor(this.uiColor);
        }

        if (this.yesHb.clickStarted || textField.trim().equals("")) {
            c = Color.LIGHT_GRAY.cpy();
        } else if (this.yesHb.hovered) {
            c = Settings.CREAM_COLOR.cpy();
        } else {
            c = Settings.GOLD_COLOR.cpy();
        }
        c.a = this.uiColor.a;

        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[2], Settings.WIDTH / 2.0F - 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, c, 0.82F);









        sb.draw(ImageMaster.OPTION_NO, Settings.WIDTH / 2.0F - 80.5F + 106.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 161, 74, false, false);

















        if (!this.noHb.clickStarted && this.noHb.hovered) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, this.uiColor.a * 0.25F));
            sb.setBlendFunction(770, 1);
            sb.draw(ImageMaster.OPTION_NO, Settings.WIDTH / 2.0F - 80.5F + 106.0F * Settings.scale, Settings.OPTION_Y - 37.0F - 120.0F * Settings.scale, 80.5F, 37.0F, 161.0F, 74.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 161, 74, false, false);
















            sb.setBlendFunction(770, 771);
            sb.setColor(this.uiColor);
        }

        if (this.noHb.clickStarted) {
            c = Color.LIGHT_GRAY.cpy();
        } else if (this.noHb.hovered) {
            c = Settings.CREAM_COLOR.cpy();
        } else {
            c = Settings.GOLD_COLOR.cpy();
        }

        c.a = this.uiColor.a;
        FontHelper.renderFontCentered(sb, FontHelper.cardTitleFont, TEXT[3], Settings.WIDTH / 2.0F + 110.0F * Settings.scale, Settings.OPTION_Y - 118.0F * Settings.scale, c, 0.82F);








        if (Settings.isControllerMode) {
            sb.draw(CInputActionSet.proceed
                    .getKeyImg(), 770.0F * Settings.xScale - 32.0F, Settings.OPTION_Y - 32.0F - 140.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);















            sb.draw(CInputActionSet.cancel
                    .getKeyImg(), 1150.0F * Settings.xScale - 32.0F, Settings.OPTION_Y - 32.0F - 140.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        }
















        if (this.shown) {
            this.yesHb.render(sb);
            this.noHb.render(sb);
        }
    }

    public void open() {
        //AbstractDungeon.previousScreen = AbstractDungeon.screen;
        //AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NONE;
        TextInput.startTextReceiver(this);

        //Gdx.input.setInputProcessor((InputProcessor)new TextInputHelper(receiver,false));

        if (SteamInputHelper.numControllers == 1 && CardCrawlGame.clientUtils != null && CardCrawlGame.clientUtils.isSteamRunningOnSteamDeck()) {
            CardCrawlGame.clientUtils.showFloatingGamepadTextInput(SteamUtils.FloatingGamepadTextInputMode.ModeSingleLine, 0, 0, Settings.WIDTH, (int)(Settings.HEIGHT * 0.25F));
        }


        this.shown = true;
        if(isClearing) textField = "";
    }

    @Override
    public String getCurrentText() {
        return textField;
    }

    @Override
    public void setText(String s) {
        textField = s;
    }

    @Override
    public boolean isDone() {
        return !this.shown;
    }

    @Override
    public boolean acceptCharacter(char c) {
        return !this.digitonly || Character.isDigit(c);
    }

    @Override
    public boolean onPushBackspace() {
        if(this.waitTimer <= 0.0F) {
            this.waitTimer = 0.09F;
            return false;
        }
        return true;
    }
}



