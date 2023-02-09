package loadout.uiElements;

import basemod.ClickableUIElement;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import loadout.LoadoutMod;
import loadout.relics.LoadoutBag;
import loadout.screens.RelicSelectScreen;
import loadout.screens.SidePanel;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.makeRelicOutlinePath;

public class LoadoutBagUIE extends ClickableUIElement {

    private static final Texture IMG = LoadoutBag.IMG;
    //public static final String ID = LoadoutMod.makeID("LoadoutBagUIE");
    private boolean relicSelected = true;
    private RelicSelectScreen relicSelectScreen;

    public static boolean isSelectionScreenUp = false;
    //public SidePanel owner;
    private LoadoutBag child;

    public LoadoutBagUIE(SidePanel owner, float x, float y) {
        super(IMG,x,y, SidePanel.UI_ELEMENT_SIZE, SidePanel.UI_ELEMENT_SIZE);
        child = new LoadoutBag();
    }

    @Override
    protected void onHover() {

    }

    @Override
    protected void onUnhover() {

    }

    @Override
    protected void onClick() {
        child.onRightClick();
    }

    @Override
    public void update() {
        super.update();
        child.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        if(hitbox.hovered) child.renderTip(sb);
        if(child.isSelectionScreenUp()) child.selectScreen.render(sb);
    }
}
