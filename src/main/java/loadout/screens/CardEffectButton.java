package loadout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
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

public class CardEffectButton implements HeaderButtonPlusListener, TextInputReceiver {




    public interface OnClickListener {
        void onClick();
    }


    public interface CardStuffProvider {
        int getMultiplier();
        AbstractCard getCard();
    }
    protected Texture image;
    public Hitbox hb;
    public float x;
    public float y;
    public String text;
    protected float textWidth;
    public String displayValue;
    protected float hb_w;
    protected float hb_h;
    private boolean clickable;
    protected HeaderButtonPlus upButton;
    protected HeaderButtonPlus downButton;

    public StatModSelectScreen.StatModActions statModActions;
    public CardStuffProvider multiplierGetter;
    private final float imageOffset = 32.0f;

    private boolean isTyping;
    private InputProcessor prevInputProcessor;
    private float waitTimer = 0.0F;
    private final float segment = 50.0F * Settings.scale;

    public CardEffectButton(Texture image, float x, float y, String text, StatModSelectScreen.StatModActions statModActions, CardStuffProvider multiplierGetter) {
        this.image = image;

        this.x = x;
        this.y = y;
        this.text = text;
        this.statModActions = statModActions;
        this.multiplierGetter = multiplierGetter;
        this.displayValue = String.valueOf(statModActions.getAmount());
        this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, displayValue, Float.MAX_VALUE, 0.0F);
        this.hb_w = 2f * segment;
        this.hb_h = 50.0f * Settings.yScale;
        this.hb = new Hitbox(x + 3f * segment,y - hb_h / 2f,hb_w,hb_h);
        this.upButton = new HeaderButtonPlus("",x + segment,y,this,true, ImageMaster.FILTER_ARROW);
        this.upButton.isAscending = false;
        this.downButton = new HeaderButtonPlus("",x + 6f * segment,y,this,true, ImageMaster.FILTER_ARROW);
        //this.imageOffset = this.image == null ? 0f : this.image.getWidth() / 2f;
        this.isTyping = false;
    }

    public void update() {
        if (this.isTyping && Gdx.input.isKeyPressed(67) && !displayValue.equals("") && this.waitTimer <= 0.0F) {
            displayValue = displayValue.substring(0, displayValue.length() - 1);
            this.waitTimer = 0.09F;
        }

        if (this.waitTimer > 0.0F) {
            this.waitTimer -= Gdx.graphics.getDeltaTime();
        }

        //this.hb.move(this.x + this.textWidth/2f + 2f * segment, this.y);
        this.hb.update();
        this.upButton.update();
        this.downButton.update();

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            this.hb.clickStarted = true;
        }
        if(!this.hb.hovered && (InputHelper.justClickedLeft || InputHelper.justClickedRight) && this.isTyping) {
            //cancel typing
            stopTyping();
            setAmountPlayer();
        }
        if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            this.hb.clicked = false;

            if(!isTyping) {
                this.isTyping = true;

                prevInputProcessor = Gdx.input.getInputProcessor();

                Gdx.input.setInputProcessor(new TextInputHelper(this, true));
                if (SteamInputHelper.numControllers == 1 && CardCrawlGame.clientUtils != null && CardCrawlGame.clientUtils.isSteamRunningOnSteamDeck()) {
                    CardCrawlGame.clientUtils.showFloatingGamepadTextInput(SteamUtils.FloatingGamepadTextInputMode.ModeSingleLine, 0, 0, Settings.WIDTH, (int)(Settings.HEIGHT * 0.25F));
                }
            }

        }

        if(this.isTyping) {
            if (Gdx.input.isKeyJustPressed(66)) {
                stopTyping();
                setAmountPlayer();
            }
            else if (InputHelper.pressedEscape) {
                InputHelper.pressedEscape = false;
                stopTyping();
            }
        }

        if(!this.isTyping) {
            this.displayValue = String.valueOf(this.statModActions.getAmount());
        }
    }

    public void render(SpriteBatch sb) {
        this.hb.render(sb);
        if(image != null)
            sb.draw(image, this.x - imageOffset + imageOffset * Settings.scale - 100.0f * Settings.scale, this.y - imageOffset * Settings.scale, imageOffset, imageOffset, imageOffset*2f, imageOffset*2f, Settings.scale, Settings.scale, 0.0F, 0, 0, image.getWidth(), image.getHeight(), false, false);
        FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, this.text, this.x, this.y, Color.WHITE);
        this.upButton.render(sb);
        Color valueColor = isTyping ? Color.CYAN : Color.GREEN;
        FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, this.displayValue, this.x + 4f * segment, this.y, valueColor);
        this.downButton.render(sb);

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if(button == this.upButton) {
            this.statModActions.setAmount(this.statModActions.getAmount() + multiplierGetter.getMultiplier());
        } else if(button == this.downButton) {
            this.statModActions.setAmount(this.statModActions.getAmount() - multiplierGetter.getMultiplier());
        }
        this.displayValue = String.valueOf(statModActions.getAmount());
    }

    @Override
    public void setTextField(String textToSet) {
        this.displayValue = textToSet;
    }

    @Override
    public String getTextField() {
        return this.displayValue;
    }

    public void stopTyping() {
        this.isTyping = false;
        Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
    }

    private void setAmountPlayer() {

        int amtToSet;
        if (this.displayValue.equals("")) {
            amtToSet = 0;
        } else {
            try {
                amtToSet = Integer.parseInt(this.displayValue);
            } catch (Exception e) {
                amtToSet = Integer.MAX_VALUE;
            }
        }

        this.statModActions.setAmount(amtToSet);
        refreshText();
    }

    private void refreshText() {
        this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, displayValue, Float.MAX_VALUE, 0.0F);
//        this.hb_w = Math.max(this.textWidth,segment);
//        this.hb.resize(this.hb_w, this.hb_h);

    }

    public void reset() {
        this.displayValue = String.valueOf(statModActions.getAmount());
        refreshText();
    }
}
