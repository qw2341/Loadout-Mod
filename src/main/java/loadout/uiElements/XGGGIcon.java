package loadout.uiElements;

import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.makeUIPath;

public class XGGGIcon extends ClickableUIElement {
    public static final Texture XGGG = TextureLoader.getTexture(makeUIPath("XGGG.png"));
    public float scale = 1.0f;

    public XGGGIcon(float x, float y) {
        super(XGGG, x, y, 64.0f, 64.0f);
    }

    @Override
    protected void onHover() {

    }

    @Override
    protected void onUnhover() {

    }

    @Override
    protected void onClick() {

    }
    public float getX() {
        return this.x;
    }
    public float getY() {
        return this.y;
    }

    @Override
    public void render(SpriteBatch sb, Color color) {
        sb.setColor(color);
        float halfWidth;
        float halfHeight;
        if (this.image != null) {
            halfWidth = (float)this.image.getWidth() / 2.0F;
            halfHeight = (float)this.image.getHeight() / 2.0F;
            sb.draw(this.image, this.x - 64.0F * Settings.scale - halfWidth + halfWidth * Settings.scale, this.y - 64.0F * Settings.scale - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, (float)this.image.getWidth(), (float)this.image.getHeight(), Settings.scale * this.scale, Settings.scale * this.scale, this.angle, 0, 0, this.image.getWidth(), this.image.getHeight(), false, false);
            if (this.tint.a > 0.0F) {
                sb.setBlendFunction(770, 1);
                sb.setColor(this.tint);
                sb.draw(this.image, this.x - halfWidth + halfWidth * Settings.scale, this.y - halfHeight + halfHeight * Settings.scale, halfWidth, halfHeight, (float)this.image.getWidth(), (float)this.image.getHeight(), Settings.scale * this.scale, Settings.scale * this.scale, this.angle, 0, 0, this.image.getWidth(), this.image.getHeight(), false, false);
                sb.setBlendFunction(770, 771);
            }
        }

        this.renderHitbox(sb);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
