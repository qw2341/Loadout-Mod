package loadout.relics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.vfx.FloatyEffect;
import com.megacrit.cardcrawl.vfx.GlowRelicParticle;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.patches.RelicPopUpPatch;
import loadout.screens.GCardSelectScreen;
import loadout.uiElements.UIElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static loadout.LoadoutMod.logger;
import static loadout.LoadoutMod.makeSoundPath;

public abstract class LoadoutRelic implements UIElement {
    public static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);

    protected boolean itemSelected = true;

    protected final InputAction ctrlKey;
    protected final InputAction shiftKey;

    private AbstractRelic.LandingSound landingSFX;
    public Hitbox hb;

    public boolean isDone;
    public boolean isAnimating;
    public boolean isObtained;

    public float scale;
    protected boolean pulse;
    private float animationTimer;
    private float glowTimer;
    public float flashTimer;
    private static final float FLASH_ANIM_TIME = 2.0F;
    private static final float DEFAULT_ANIM_SCALE = 4.0F;
    private FloatyEffect f_effect;
    private float rotation;
    public float currentX;
    public float currentY;
    public float targetX;
    public float targetY;
    public Texture img;
    public Texture largeImg;
    public Texture outlineImg;
    public ArrayList<PowerTip> tips = new ArrayList();
    public boolean grayscale = false;
    public String description;
    public String flavorText = "missing";

    public final String name;
    public final String relicId;
    private final RelicStrings relicStrings;
    public final String[] DESCRIPTIONS;
    private static float offsetX;

    private Color flashColor = new Color(1.0F, 1.0F, 1.0F, 0.0F);
    private Color goldOutlineColor = new Color(1.0F, 0.9F, 0.4F, 0.0F);

    public LoadoutRelic(String id, Texture texture, Texture outline, AbstractRelic.LandingSound sfx) {

        this.scale = Settings.scale;
        this.pulse = false;
        this.glowTimer = 0.0F;
        this.flashTimer = 0.0F;
        this.f_effect = new FloatyEffect(10.0F, 0.2F);
        this.isDone = false;
        this.isAnimating = false;
        this.isObtained = false;
        this.landingSFX = AbstractRelic.LandingSound.CLINK;
        this.hb = new Hitbox(AbstractRelic.PAD_X, AbstractRelic.PAD_X);
        this.rotation = 0.0F;

        this.relicId = id;
        this.relicStrings = CardCrawlGame.languagePack.getRelicStrings(this.relicId);

        this.DESCRIPTIONS = this.relicStrings.DESCRIPTIONS;
        this.img = texture;
        this.outlineImg = outline;

        this.name = this.relicStrings.NAME;
        this.description = this.getUpdatedDescription();
        this.flavorText = this.relicStrings.FLAVOR;

        this.landingSFX = sfx;

        this.tips.add(new PowerTip(this.name, this.description));

        this.ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);
        this.shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);

        if(LoadoutMod.isIsaac()) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(id+"Alt");
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }


    public boolean isOtherRelicScreenOpen() {
        for(Map.Entry<String,Boolean> e : AbstractCustomScreenRelic.isScreenUpMap.entrySet()) {
            //logger.info(e.getKey() + " : " + e.getValue());
            if(!e.getKey().equals(this.getClass().getSimpleName())) {
                if(e.getValue()) return true;
            }
        }
        for(Map.Entry<String,Boolean> e : AbstractCardScreenRelic.isScreenUpMap.entrySet()) {
            //logger.info(e.getKey() + " : " + e.getValue());
            if(!e.getKey().equals(this.getClass().getSimpleName())) {
                if(e.getValue()) return true;
            }
        }

        return false;
    }
    public static boolean isOtherRelicScreenOpen(Class<?> callerClazz) {
        for(Map.Entry<String,Boolean> e : AbstractCustomScreenRelic.isScreenUpMap.entrySet()) {
            if(!e.getKey().equals(callerClazz.getSimpleName())) {
                if(e.getValue()) return true;
            }
        }
        for(Map.Entry<String,Boolean> e : AbstractCardScreenRelic.isScreenUpMap.entrySet()) {
            if(!e.getKey().equals(callerClazz.getSimpleName())) {
                if(e.getValue()) return true;
            }
        }

        return false;
    }

    public void onCtrlRightClick() {}
    public void onShiftRightClick() {}

    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (isOtherRelicScreenOpen()) {
            AllInOneBag.INSTANCE.closeAllScreens();
        }

        if(this.ctrlKey.isPressed()) {
            onCtrlRightClick();
            return;
        }

        if(this.shiftKey.isPressed()) {
            onShiftRightClick();
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openSelectScreen();
    }

    protected abstract void openSelectScreen();

    public abstract void setIsSelectionScreenUp(boolean bool);
    public abstract boolean isSelectionScreenUp();

    protected abstract void doneSelectionLogics();


    private void updateFlash() {
        if (this.flashTimer != 0.0F) {
            this.flashTimer -= Gdx.graphics.getDeltaTime();
            if (this.flashTimer < 0.0F) {
                if (this.pulse) {
                    this.flashTimer = 1.0F;
                } else {
                    this.flashTimer = 0.0F;
                }
            }
        }
    }

    public void update()
    {
        this.updateFlash();
        if (!this.isDone) {
            if (this.isAnimating) {
                this.glowTimer -= Gdx.graphics.getDeltaTime();
                if (this.glowTimer < 0.0F) {
                    this.glowTimer = 0.5F;
                    AbstractDungeon.effectList.add(new GlowRelicParticle(this.img, this.currentX + this.f_effect.x, this.currentY + this.f_effect.y, this.rotation));
                }

                this.f_effect.update();
                if (this.hb.hovered) {
                    this.scale = Settings.scale * 1.5F;
                } else {
                    this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale * 1.1F);
                }
            } else if (this.hb.hovered) {
                this.scale = Settings.scale * 1.25F;
            } else {
                this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale);
            }

            if (this.isObtained) {
                if (this.rotation != 0.0F) {
                    this.rotation = MathUtils.lerp(this.rotation, 0.0F, Gdx.graphics.getDeltaTime() * 6.0F * 2.0F);
                }

                if (this.currentX != this.targetX) {
                    this.currentX = MathUtils.lerp(this.currentX, this.targetX, Gdx.graphics.getDeltaTime() * 6.0F);
                    if (Math.abs(this.currentX - this.targetX) < 0.5F) {
                        this.currentX = this.targetX;
                    }
                }

                if (this.currentY != this.targetY) {
                    this.currentY = MathUtils.lerp(this.currentY, this.targetY, Gdx.graphics.getDeltaTime() * 6.0F);
                    if (Math.abs(this.currentY - this.targetY) < 0.5F) {
                        this.currentY = this.targetY;
                    }
                }

                if (this.currentY == this.targetY && this.currentX == this.targetX) {
                    this.isDone = true;
                    if (AbstractDungeon.topPanel != null) {
                        AbstractDungeon.topPanel.adjustRelicHbs();
                    }

                    this.hb.move(this.currentX, this.currentY);
                }

                this.scale = Settings.scale;
            }

            if (this.hb != null) {
                this.hb.update();
            }

        } else {
            this.hb.update();
            this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale);
//            this.updateRelicPopupClick();
        }

        this.updateSelectScreen();

    }

    public abstract void updateSelectScreen();

    public void renderOutline(SpriteBatch sb, boolean inTopPanel) {
        float tmpX = this.currentX - 64.0F;
        if (inTopPanel) {
            tmpX += offsetX;
        }

        if (this.hb.hovered) {
            sb.setBlendFunction(770, 1);
            this.goldOutlineColor.a = 0.6F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F;
            sb.setColor(this.goldOutlineColor);
            sb.draw(this.outlineImg, tmpX, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, this.rotation, 0, 0, 128, 128, false, false);
            sb.setBlendFunction(770, 771);
        } else {
            sb.setColor(AbstractRelic.PASSIVE_OUTLINE_COLOR);
            sb.draw(this.outlineImg, tmpX, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, this.rotation, 0, 0, 128, 128, false, false);
        }
    }

    public void renderFlash(SpriteBatch sb, boolean inTopPanel) {
        float tmp = Interpolation.exp10In.apply(0.0F, 4.0F, this.flashTimer / 2.0F);
        sb.setBlendFunction(770, 1);
        this.flashColor.a = this.flashTimer * 0.2F;
        sb.setColor(this.flashColor);
        float tmpX = this.currentX - 64.0F;
        if (inTopPanel) {
            tmpX += offsetX;
        }

        sb.draw(this.img, tmpX, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale + tmp, this.scale + tmp, this.rotation, 0, 0, 128, 128, false, false);
        sb.draw(this.img, tmpX, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale + tmp * 0.66F, this.scale + tmp * 0.66F, this.rotation, 0, 0, 128, 128, false, false);
        sb.draw(this.img, tmpX, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale + tmp / 3.0F, this.scale + tmp / 3.0F, this.rotation, 0, 0, 128, 128, false, false);
        sb.setBlendFunction(770, 771);
    }

    public abstract void renderInTopPanel(SpriteBatch sb);

    public void render(SpriteBatch sb)
    {
        this.renderOutline(sb, false);
        if (this.grayscale) {
            ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
        }

        sb.setColor(Color.WHITE);
        sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0f, 0, 0, 128, 128, false, false);
        if (this.grayscale) {
            ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
        }

        this.renderFlash(sb, true);
        this.hb.render(sb);

    }

    public void renderTip(SpriteBatch sb) {
        TipHelper.queuePowerTips((float)InputHelper.mX + 60.0F * Settings.scale, (float)InputHelper.mY - 30.0F * Settings.scale, this.tips);
    }

    public void playLandingSFX() {
        if (LoadoutMod.isIsaac()) {
            if (CardCrawlGame.MUTE_IF_BG && Settings.isBackgrounded) {
                return;
            } else if (landingSfx != null) {
                landingSfx.play(Settings.SOUND_VOLUME * Settings.MASTER_VOLUME);
            } else {
                logger.info("Missing landing sound!");
            }
        } else {
            switch (this.landingSFX) {
                case CLINK:
                    CardCrawlGame.sound.play("RELIC_DROP_CLINK");
                    break;
                case FLAT:
                    CardCrawlGame.sound.play("RELIC_DROP_FLAT");
                    break;
                case SOLID:
                    CardCrawlGame.sound.play("RELIC_DROP_ROCKY");
                    break;
                case HEAVY:
                    CardCrawlGame.sound.play("RELIC_DROP_HEAVY");
                    break;
                case MAGICAL:
                    CardCrawlGame.sound.play("RELIC_DROP_MAGICAL");
                    break;
                default:
                    CardCrawlGame.sound.play("RELIC_DROP_CLINK");
            }
        }
    }

    public void flash() {
        this.flashTimer = 2.0F;
    }

    protected void addToBot(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToBottom(action);
    }

    protected void addToTop(AbstractGameAction action) {
        AbstractDungeon.actionManager.addToTop(action);
    }



}
