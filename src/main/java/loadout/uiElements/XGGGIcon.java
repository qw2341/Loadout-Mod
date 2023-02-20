package loadout.uiElements;

import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.vfx.ShopSpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechBubble;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;
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

    public void say(String msg) {
        AbstractDungeon.effectList.add(new ShopSpeechBubble(this.x - 100.0F * Settings.xScale, this.y - 330.0F * Settings.yScale, 3.0F, msg, true));
        AbstractDungeon.effectList.add(new SpeechTextEffect(x + 75.0F * Settings.scale, y - 200.0F * Settings.scale, 3.0F, msg, DialogWord.AppearEffect.BUMP_IN));
    }
}
