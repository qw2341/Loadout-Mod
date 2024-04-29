package loadout.uiElements;

import basemod.IUIElement;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.patches.com.megacrit.cardcrawl.helpers.TipHelper.HeaderlessTip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModLabeledDropdown implements IUIElement, DropdownMenuListener {

    private static final float TEXT_X_OFFSET = 0.0F;
    private static final float TEXT_Y_OFFSET = 8.0F;
    public ModLabel text;
    public String tooltip;
    public DropdownMenu dropdownMenu;
    private float xPos;
    private float yPos;
    public ModPanel parent;

    private final BiConsumer<Integer, String> onChangeSeletionTo;

    private final Consumer<DropdownMenu> dropdownMenuUpdate;

    public ModLabeledDropdown(String labelText, String tooltipText, float xPos, float yPos, Color color, BitmapFont font, ModPanel p, ArrayList<String> options, Consumer<ModLabel> labelUpdate, Consumer<DropdownMenu> dropdownMenuUpdate, BiConsumer<Integer, String> onChangeSeletionTo) {
        this.dropdownMenu = new DropdownMenu(this, options, font, color);
        this.xPos = xPos;
        this.yPos = yPos;
        this.tooltip = tooltipText;
        this.text = new ModLabel(labelText, xPos + TEXT_X_OFFSET, yPos + TEXT_Y_OFFSET, color, font, p, labelUpdate);
        this.parent = p;
        this.onChangeSeletionTo = onChangeSeletionTo;
        this.dropdownMenuUpdate = dropdownMenuUpdate;
    }

    @Override
    public void render(SpriteBatch sb) {
        this.dropdownMenu.render(sb, xPos,yPos);
        this.text.render(sb);
        if (this.tooltip != null && this.dropdownMenu.getHitbox().hovered) {
            HeaderlessTip.renderHeaderlessTip((float) InputHelper.mX + 60.0F * Settings.scale, (float)InputHelper.mY - 50.0F * Settings.scale, this.tooltip);
        }
    }

    @Override
    public void update() {
        this.dropdownMenu.update();
        this.dropdownMenuUpdate.accept(this.dropdownMenu);
        this.text.update();
    }

    @Override
    public int renderLayer() {
        return 1;
    }

    @Override
    public int updateOrder() {
        return 1;
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        this.onChangeSeletionTo.accept(i, s);
    }

    public void set(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.text.set(xPos + 40.0F, yPos + 8.0F);
    }

    public void setX(float xPos) {
        this.xPos = xPos;
        this.text.setX(xPos + 40.0F);
    }

    public void setY(float yPos) {
        this.yPos = yPos;
        this.text.setY(yPos + 8.0F);
    }

    public float getX() {
        return xPos;
    }

    public float getY() {
        return yPos;
    }
}
