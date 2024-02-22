package loadout.uiElements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import loadout.helper.Action;
import loadout.helper.RenderAction;

import java.util.ArrayList;

public abstract class AbstractButton implements UIElement  {
    public Hitbox hb;
    public String labelText;
    public static float WIDTH = 200.0f * Settings.scale;
    public static float HEIGHT = 50.0f * Settings.scale;

    public String id;

    public String modID = "Slay the Spire";

    public float x = 0f;
    public float y = 0f;

    public boolean isHidden;

    public Action onPress;
    public Action onRelease;
    public Action onHold;
    public Action onRightClick;
    public Action onRightRelease;

    public ArrayList<PowerTip> tips;

    public Texture texture = null;
    public TextureRegion textureRegion = null;

    public RenderAction onHoverRender = RenderAction.EMPTY_ACTION;

    public boolean pressStarted = false;


    public AbstractButton(Hitbox hb, String labelText, String id, float x, float y, boolean isHidden, Action onPress, Action onRelease, Action onHold, Action onRightClick, Action onRightRelease) {
        this.hb = hb;
        this.labelText = labelText;
        this.id = id;
        this.x = x;
        this.y = y;
        this.isHidden = isHidden;
        this.onPress = onPress;
        this.onRelease = onRelease;
        this.onHold = onHold;
        this.onRightClick = onRightClick;
        this.onRightRelease = onRightRelease;
        this.tips = new ArrayList<>();
    }

    public AbstractButton(String labelText, String id) {
        this.hb = new Hitbox(WIDTH, HEIGHT);
        this.labelText = labelText;
        this.id = id;
        this.isHidden = false;
        this.onPress = Action.EMPTY_ACTION;
        this.onRelease = Action.EMPTY_ACTION;
        this.onHold = Action.EMPTY_ACTION;
        this.onRightClick = Action.EMPTY_ACTION;
        this.onRightRelease = Action.EMPTY_ACTION;
        this.tips = new ArrayList<>();
    }

    public AbstractButton(String labelText, String id, float x, float y) {
        this(labelText, id);
        this.x = x;
        this.y = y;
        this.hb.move(x + WIDTH / 2.0f,y);
    }

    public void renderIcon(SpriteBatch sb) {
        float a = (this.hb.hovered) ? 1.0f : 0.7f;
        if(texture != null) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, a));
            sb.draw(this.texture, x - (float)this.texture.getWidth() / 2.0F, y - (float)this.texture.getHeight() / 2.0F, HEIGHT, HEIGHT, 0 ,0, texture.getWidth(), texture.getHeight(), false, false);
        } else if(textureRegion != null) {
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, a));
            sb.draw(textureRegion, x - (float)this.textureRegion.getRegionWidth() / 2.0F, y - (float)this.textureRegion.getRegionHeight() / 2.0F, (float)this.textureRegion.getRegionX() / 2.0F, (float)this.textureRegion.getRegionY() / 2.0F, HEIGHT, HEIGHT, Settings.scale, Settings.scale, 0.0F);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if(hb != null) {
            this.hb.render(sb);
            if (this.hb.hovered) {
                this.onHoverRender.render(sb);
            }

            renderIcon(sb);

            if (this.hb.hovered) {
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x + 40.0F, y - 64.0F, 64.0F, 64.0F, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
                FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, this.labelText, x + 150.0f / 2, y + 20.0f, 200.0f, 25.0f, Settings.GOLD_COLOR);
                sb.setBlendFunction(770, 771);

                if (!this.tips.isEmpty()) TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
            } else {
                FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, this.labelText, x + 150.0f / 2, y + 20.0f, 200.0f, 25.0f, Settings.CREAM_COLOR);
            }
        }
    }

    @Override
    public void update() {
        this.hb.move(x + WIDTH ,y);
        this.hb.update();
        if(!this.isHidden && this.hb.hovered) {
            if (InputHelper.justClickedLeft) {
                this.pressStarted = true;
                this.onPress.execute();
            } else if(InputHelper.isMouseDown) {
                this.onHold.execute();
            } else if (InputHelper.justReleasedClickLeft) {

                this.onRelease.execute();
                this.pressStarted = false;
            } else if (InputHelper.justClickedRight) {
                this.pressStarted = true;
                this.onRightClick.execute();
            } else if (InputHelper.justReleasedClickRight) {

                this.onRightRelease.execute();
                this.pressStarted = false;
            }
        }
        if (!this.hb.hovered) this.pressStarted = false;
    }
}
