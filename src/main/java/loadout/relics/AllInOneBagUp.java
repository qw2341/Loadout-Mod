package loadout.relics;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ShaderHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.TrueVictoryRoom;
import loadout.LoadoutMod;

import static loadout.LoadoutMod.isXggg;

public class AllInOneBagUp extends CustomRelic implements ClickableRelic {

    public static final String ID = LoadoutMod.makeID("AllInOneBagUp");
    public static final Texture IMG = LoadoutBag.IMG;
    private static final Texture OUTLINE = LoadoutBag.OUTLINE;
    private Color color = new Color();

    public AllInOneBagUp() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void onRightClick() {
        if(!this.usedUp) {
            CardCrawlGame.music.fadeOutBGM();
            MapRoomNode node = new MapRoomNode(3, 4);
            node.room = new TrueVictoryRoom();
            AbstractDungeon.nextRoom = node;
            AbstractDungeon.closeCurrentScreen();
            AbstractDungeon.nextRoomTransitionStart();
            this.usedUp();
        }

    }

    @Override
    public void renderInTopPanel(SpriteBatch sb) {
        if (!Settings.hideRelics) {
            this.renderOutline(sb, true);
            if (this.grayscale) {
                ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
            }
            updateColor();
            sb.setColor(this.color);
            sb.draw(this.img, this.currentX - 64.0F + (float) getOffsetX(), this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0F, 0, 0, 128, 128, false, false);
            if (this.grayscale) {
                ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
            }

            this.renderCounter(sb, true);
            this.renderFlash(sb, true);
            this.hb.render(sb);
        }
    }

    private float getOffsetX() {
        return ReflectionHacks.getPrivateStatic(AbstractRelic.class, "offsetX");
    }

    private void updateColor() {

        float minBrightnessFactor = 0.5F;
        float maxBrightnessFactor = 0.8F;
        float greenRange = 0.3F;

        float randomBrightness = MathUtils.random(minBrightnessFactor, maxBrightnessFactor);

        this.color.r = 0.7F + (MathUtils.random() * 0.3F);
        this.color.g = ((MathUtils.cosDeg((float)((System.currentTimeMillis() + 1000L) / 10L % 360L)) + 1.25F) / (2.3F / greenRange)) * randomBrightness;
        this.color.b = (0.5F + (MathUtils.cosDeg((float)(System.currentTimeMillis() / 10L % 360L)) + 1.25F) / 2.3F) * randomBrightness;
        this.color.a = 1.0F;
    }

    @Override
    public void render(SpriteBatch sb, boolean renderAmount, Color outlineColor) {
        if (this.isSeen) {
            this.renderOutline(outlineColor, sb, false);
        } else {
            this.renderOutline(Color.LIGHT_GRAY, sb, false);
        }

        if (this.isSeen) {
            updateColor();
            sb.setColor(this.color);
        } else if (this.hb.hovered) {
            sb.setColor(Settings.HALF_TRANSPARENT_BLACK_COLOR);
        } else {
            sb.setColor(Color.BLACK);
        }

        if (AbstractDungeon.screen != null && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.NEOW_UNLOCK) {
            if (this.largeImg == null) {
                sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, Settings.scale * 2.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 15.0F, Settings.scale * 2.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 15.0F, 0.0F, 0, 0, 128, 128, false, false);
            } else {
                sb.draw(this.largeImg, this.currentX - 128.0F, this.currentY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale * 1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 30.0F, Settings.scale * 1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 30.0F, 0.0F, 0, 0, 256, 256, false, false);
            }
        } else {
            sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0F, 0, 0, 128, 128, false, false);
        }

        if (this.hb.hovered && !CardCrawlGame.relicPopup.isOpen) {
            if (!this.isSeen) {
                if ((float) InputHelper.mX < 1400.0F * Settings.scale) {
                    TipHelper.renderGenericTip((float)InputHelper.mX + 60.0F * Settings.scale, (float)InputHelper.mY - 50.0F * Settings.scale, LABEL[1], MSG[1]);
                } else {
                    TipHelper.renderGenericTip((float)InputHelper.mX - 350.0F * Settings.scale, (float)InputHelper.mY - 50.0F * Settings.scale, LABEL[1], MSG[1]);
                }

                return;
            }

            this.renderTip(sb);
        }

        this.hb.render(sb);
    }

    @Override
    public AbstractRelic makeCopy() {
        return new AllInOneBagUp();
    }
}
