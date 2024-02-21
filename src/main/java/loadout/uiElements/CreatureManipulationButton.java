package loadout.uiElements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import loadout.helper.Action;

public class CreatureManipulationButton implements UIElement {

    public Hitbox hb;
    public boolean isHidden;
    private String labelText;
    public Action onPress;
    public Action onRelease;
    public Action onHold;
    public static final float ROW_HEIGHT;
    public static final float ROW_WIDTH;
    public static final float ROW_RENDER_HEIGHT;
    private static final Color ROW_BG_COLOR;
    private static final Color ROW_HOVER_COLOR;
    private static final Color ROW_SELECT_COLOR;
    private static final Color TEXT_DEFAULT_COLOR;
    private static final Color TEXT_FOCUSED_COLOR;
    private static final Color TEXT_HOVERED_COLOR;
    private static final float ROW_TEXT_Y_OFFSET;
    private static final float ROW_TEXT_LEADING_OFFSET;
    static {
        ROW_HEIGHT = 50.0F * Settings.scale;
        ROW_WIDTH = 256.0F * Settings.scale;
        ROW_RENDER_HEIGHT = 64.0F * Settings.scale;
        ROW_TEXT_Y_OFFSET = 12.0F * Settings.scale;
        ROW_TEXT_LEADING_OFFSET = 40.0F * Settings.scale;
        ROW_BG_COLOR = new Color(588124159);
        ROW_HOVER_COLOR = new Color(-193);
        ROW_SELECT_COLOR = new Color(-1924910337);
        TEXT_DEFAULT_COLOR = Settings.CREAM_COLOR;
        TEXT_FOCUSED_COLOR = Settings.GREEN_TEXT_COLOR;
        TEXT_HOVERED_COLOR = Settings.GOLD_COLOR;
    }

    public CreatureManipulationButton(String labelText, Action onRelease) {
        this(labelText, () -> {}, onRelease, () -> {});
    }

    public CreatureManipulationButton(String labelText, Action onPress, Action onRelease, Action onHold) {
        this.labelText = labelText;
        this.onRelease = onRelease;
        this.onHold = onHold;
        this.onPress = onPress;
        this.hb = new Hitbox(ROW_WIDTH, ROW_RENDER_HEIGHT);
        isHidden = false;
    }

    @Override
    public void render(SpriteBatch sb) {
        if (!this.isHidden) {
            sb.setBlendFunction(770, 1);
            sb.setColor(this.getRowBgColor());
            sb.draw(ImageMaster.INPUT_SETTINGS_ROW, this.hb.x, this.hb.y, ROW_WIDTH, ROW_RENDER_HEIGHT);
            sb.setBlendFunction(770, 771);
            sb.setColor(Color.WHITE);
            Color textColor = this.getTextColor();
            float textY = this.hb.cY + ROW_TEXT_Y_OFFSET;
            float textX = this.hb.x + ROW_TEXT_LEADING_OFFSET;
            FontHelper.renderFont(sb, FontHelper.topPanelInfoFont, this.labelText, textX, textY, textColor);

            this.hb.render(sb);
        }
    }

    @Override
    public void update() {
        this.hb.update();
        if(!this.isHidden && this.hb.hovered) {
            if(InputHelper.isMouseDown) {
                this.onHold.execute();
            } else if (InputHelper.justReleasedClickLeft) {
                this.onRelease.execute();
            }
        }
    }

    protected Color getRowBgColor() {
        Color bgColor = ROW_BG_COLOR;

        if (this.hb.hovered && InputHelper.isMouseDown) {
            bgColor = ROW_SELECT_COLOR;
        } else if (this.hb.hovered) {
            bgColor = ROW_HOVER_COLOR;
        }

        return bgColor;
    }

    protected Color getTextColor() {
        Color color = TEXT_DEFAULT_COLOR;

        if (this.hb.hovered && InputHelper.isMouseDown) {
            color = TEXT_FOCUSED_COLOR;
        } else if (this.hb.hovered) {
            color = TEXT_HOVERED_COLOR;
        }

        return color;
    }

    public void move(float x, float y) {
        this.hb.move(x, y);
    }


}
