package loadout.screens;

import basemod.ClickableUIElement;
import basemod.abstracts.CustomRelic;
import basemod.interfaces.PrePlayerUpdateSubscriber;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.relics.LoadoutBag;
import loadout.relics.LoadoutCauldron;
import loadout.relics.TrashBin;
import loadout.uiElements.LoadoutBagUIE;

import java.util.ArrayList;

public class SidePanel {
    public ArrayList<ClickableUIElement> relics;

    public float startX;
    public float startY;
    public static float PADDING_Y = 50.0F * Settings.yScale;
    public static float UI_ELEMENT_SIZE = 64.0f;

    public boolean shown;

    public SidePanel(float x, float y) {
        this.startX = x;
        this.startY = y;
        this.shown = true;

        this.relics = new ArrayList<>();
        this.relics.add(new LoadoutBagUIE(this,startX,startY));
        this.startY += PADDING_Y;
    }

    public void update() {
        if(!shown) return;
        for(ClickableUIElement r : relics) {
            r.update();
        }
    }

    public void render(SpriteBatch sb) {
        if(!shown) return;

        for(ClickableUIElement r : relics) {
            r.render(sb);
        }
    }



}
