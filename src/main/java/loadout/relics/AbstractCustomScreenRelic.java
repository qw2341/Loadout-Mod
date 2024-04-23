package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.vfx.FloatyEffect;
import com.megacrit.cardcrawl.vfx.GlowRelicParticle;
import loadout.LoadoutMod;
import loadout.helper.LoadoutRelicHelper;
import loadout.patches.RelicPopUpPatch;
import loadout.screens.AbstractSelectScreen;
import loadout.uiElements.UIElement;
import loadout.util.TextureLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static loadout.LoadoutMod.*;


public abstract class AbstractCustomScreenRelic<T> extends LoadoutRelic {


    public AbstractSelectScreen<T> selectScreen;

    public static HashMap<String, Boolean> isScreenUpMap = new HashMap<>();


    public AbstractCustomScreenRelic(String id, Texture texture, Texture outline, AbstractRelic.LandingSound sfx) {
        super(id, texture, outline, sfx);

        isScreenUpMap.put(this.getClass().getSimpleName(), Boolean.FALSE);
    }

    protected void openSelectScreen() {
        this.itemSelected = false;
        setIsSelectionScreenUp(true);

        if (selectScreen == null) this.selectScreen = getNewSelectScreen();
        if(selectScreen != null) this.selectScreen.open();
    }

    protected abstract AbstractSelectScreen<T> getNewSelectScreen();

    public void updateSelectScreen() {
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
    public void renderInTopPanel(SpriteBatch sb) {
        render(sb);
        if (isSelectionScreenUp()) {
            selectScreen.render(sb);
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
