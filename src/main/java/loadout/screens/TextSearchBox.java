package loadout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;
import com.megacrit.cardcrawl.helpers.steamInput.SteamInputHelper;
import loadout.helper.TextInputHelper;
import loadout.helper.TextInputReceiver;

public class TextSearchBox implements TextInputReceiver {
    public boolean isTyping = false;

    public float waitTimer = 0.0F;
    public String filterTextPlaceholder = CardSelectSortHeader.TEXT[3];
    public String filterText = "";
    public Hitbox filterTextHb;

    public float filterBarY;

    public float filterBarX;

    public Texture filterTextBoxImg = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
    private Color highlightBoxColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    private boolean digitonly;

    public SelectScreen<?> receiver;

    public String title = CardSelectSortHeader.TEXT[4];

    public TextSearchBox(SelectScreen<?> receiver, float x, float y, boolean digitOnly) {
        this.receiver = receiver;
        this.filterBarX = x;
        this.filterBarY = y;
        this.filterTextHb = new Hitbox(filterBarX,filterBarY - 25.0F * Settings.yScale, 250.0F * Settings.scale, 50.0F * Settings.yScale);
        this.digitonly = digitOnly;
    }

    public void update() {
        if (this.isTyping && Gdx.input.isKeyPressed(67) && !this.filterText.equals("") && this.waitTimer <= 0.0F) {

            this.filterText = this.filterText.substring(0, this.filterText.length() - 1);
            this.waitTimer = 0.05F;
        }

        if (this.waitTimer > 0.0F) {
            this.waitTimer -= Gdx.graphics.getDeltaTime();
        }

        filterTextHb.update();

        if (this.filterTextHb.hovered && InputHelper.justClickedLeft) {
            this.filterTextHb.clickStarted = true;
        }
        if(!this.filterTextHb.hovered && (InputHelper.justClickedLeft || InputHelper.justClickedRight) && this.isTyping) {
            //cancel typing
            stopTyping();
            this.receiver.updateFilters();
        }
        if (this.filterTextHb.clicked || this.filterTextHb.hovered && CInputActionSet.select.isJustPressed()) {
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            this.filterTextHb.clicked = false;

            if(!isTyping) {
                this.isTyping = true;

                this.filterText = "";

                Gdx.input.setInputProcessor(new TextInputHelper(this, this.digitonly));
                if (SteamInputHelper.numControllers == 1 && CardCrawlGame.clientUtils != null && CardCrawlGame.clientUtils.isSteamRunningOnSteamDeck()) {
                    CardCrawlGame.clientUtils.showFloatingGamepadTextInput(SteamUtils.FloatingGamepadTextInputMode.ModeSingleLine, 0, 0, Settings.WIDTH, (int)(Settings.HEIGHT * 0.25F));
                }
            }





        }

        if(this.isTyping) {
            if (Gdx.input.isKeyJustPressed(66)) {

                stopTyping();
                this.receiver.updateFilters();
            }
            else if (InputHelper.pressedEscape) {
                InputHelper.pressedEscape = false;

                stopTyping();
            }
        }


    }

    public void stopTyping() {
        this.isTyping = false;
        Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
    }

    public void resetText() {
        this.filterText = "";
    }

    @Override
    public void setTextField(String textToSet) {
        this.filterText = textToSet;
    }

    @Override
    public String getTextField() {
        return this.filterText;
    }

    public void render(SpriteBatch sb) {
        filterTextHb.render(sb);
        this.highlightBoxColor.a = isTyping ? 0.7F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F : 1.0F;
        sb.setColor(this.highlightBoxColor);
        float doop = this.filterTextHb.hovered ? 1.0F + (1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L))) / 50.0F : 1.0F ;

        sb.draw(this.filterTextBoxImg, this.filterBarX - 50.0F, this.filterBarY - 50.0F, 100.0F, 43.0F, 250.0F, 86.0F, Settings.scale * doop * this.filterTextHb.width / 150.0F / Settings.scale, Settings.yScale * doop, 0.0F, 0, 0, 200, 86, false, false);
        String renderFilterText = filterText.equals("") ? filterTextPlaceholder : filterText;
        Color filterTextColor = isTyping ? Color.CYAN : Settings.GOLD_COLOR;
        FontHelper.renderSmartText(sb, FontHelper.panelNameFont, renderFilterText, filterBarX, filterBarY, 250.0F, 20.0F, filterTextColor);
        FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, this.title, filterBarX, filterBarY + 35.0F * Settings.yScale, 250.0F, 20.0F, Settings.GOLD_COLOR);

    }
}
