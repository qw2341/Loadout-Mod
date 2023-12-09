package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.ShaderHelper;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.localization.RelicStrings;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.patches.RelicPopUpPatch;
import loadout.screens.AbstractSelectScreen;
import loadout.util.TextureLoader;

import java.util.HashMap;
import java.util.Map;

import static loadout.LoadoutMod.*;


public abstract class AbstractCustomScreenRelic<T> extends CustomRelic implements ClickableRelic {

    public static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);

    protected boolean itemSelected = true;
    public AbstractSelectScreen<T> selectScreen;

    public static HashMap<String, Boolean> isScreenUpMap = new HashMap<>();

    protected final InputAction ctrlKey;
    protected final InputAction shiftKey;

    public AbstractCustomScreenRelic(String id, Texture texture, Texture outline, RelicTier tier, LandingSound sfx) {
        super(id, texture, outline, tier, sfx);
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
        isScreenUpMap.put(this.getClass().getSimpleName(), Boolean.FALSE);
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void relicTip() {

    }
    @Override
    public void onUnequip() {
        if(isSelectionScreenUp()) {
            if(selectScreen !=null) {
                setIsSelectionScreenUp(false);
                selectScreen.close();
            }
        }
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

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (isOtherRelicScreenOpen()) {
            if(RelicPopUpPatch.IsInsideAnotherRelicField.isInsideAnother.get(this)) {
                if(AbstractDungeon.player.hasRelic(AllInOneBag.ID)) ((AllInOneBag)AbstractDungeon.player.getRelic(AllInOneBag.ID)).closeAllScreens();
            } else {
                LoadoutRelicHelper.closeAllScreens();
            }
        }

        if(isSelectionScreenUp()) {
            if(selectScreen !=null) {
                setIsSelectionScreenUp(false);
                selectScreen.close();
            }
            return;
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

    protected void openSelectScreen() {
        this.itemSelected = false;
        setIsSelectionScreenUp(true);

        if (selectScreen == null) this.selectScreen = getNewSelectScreen();
        if(selectScreen != null) this.selectScreen.open();
    }

    protected abstract AbstractSelectScreen<T> getNewSelectScreen();

    protected abstract void doneSelectionLogics();
    @Override
    public void update()
    {
        super.update();

        if(selectScreen != null) {
            if(isSelectionScreenUp()) selectScreen.update();

            if (!itemSelected) {
                if (selectScreen.doneSelecting()) {
                    itemSelected = true;
                    setIsSelectionScreenUp(false);
                    doneSelectionLogics();
                }
            }
        }

    }

    @Override
    public void renderInTopPanel(SpriteBatch sb)
    {
        if(RelicPopUpPatch.IsInsideAnotherRelicField.isInsideAnother.get(this)) {
            if (!Settings.hideRelics) {
                this.renderOutline(sb, false);
                if (this.grayscale) {
                    ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
                }

                sb.setColor(Color.WHITE);
                sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0f, 0, 0, 128, 128, false, false);
                if (this.grayscale) {
                    ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
                }

                this.renderCounter(sb, true);
                this.renderFlash(sb, true);
                this.hb.render(sb);
            }
        } else super.renderInTopPanel(sb);

        if (isSelectionScreenUp()) {
            selectScreen.render(sb);
        }
    }

    @Override
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
            super.playLandingSFX();
        }
    }

    public boolean isSelectionScreenUp() {
        return isScreenUpMap.get(this.getClass().getSimpleName());
    }

    public static boolean isSelectionScreenUp(Class<? extends AbstractCustomScreenRelic<?>> caller) {
        return isScreenUpMap.get(caller.getSimpleName());
    }

    public void setIsSelectionScreenUp(boolean bool) {
        //logger.info("Setting isScreenUp for " + this.getClass().getSimpleName() + " to " + bool);
        isScreenUpMap.put(this.getClass().getSimpleName(), bool);
    }

    public static void setIsSelectionScreenUp(Class<? extends AbstractCustomScreenRelic<?>> caller, boolean bool) {
        isScreenUpMap.put(caller.getSimpleName(), bool);
    }
}
