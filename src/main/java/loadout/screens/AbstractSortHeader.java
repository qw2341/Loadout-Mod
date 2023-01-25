package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;

import java.util.ArrayList;

import static loadout.LoadoutMod.allCharacters;

public abstract class AbstractSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {
    public boolean justSorted = false;

    public float startX = 650.0F * Settings.xScale;
    public static final float START_X = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    protected float startY = Settings.HEIGHT - 200.0F * Settings.yScale;
    public static final float START_Y = Settings.HEIGHT - 200.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;

    protected String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    protected static Texture img;
    private final Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public AbstractSelectScreen selectScreen;
    public TextSearchBox searchBox;

    public ArrayList<String> playerClasses;
    public ArrayList<String> relicTiers;

    public AbstractSortHeader(AbstractSelectScreen ss) {

        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");

        this.selectScreen = ss;
        if (allCharacters != null) {
            playerClasses = new ArrayList<>();
            for (AbstractPlayer ap : allCharacters) {
                playerClasses.add(ap.getLoadout().name);
            }
            playerClasses.add(0,CardSelectSortHeader.TEXT[0]);//All
            playerClasses.add(removeLastChar(RelicSelectScreen.TEXT[4]));//Shared
        }

        relicTiers = new ArrayList<>();
        for (AbstractRelic.RelicTier rt : AbstractRelic.RelicTier.values()) {
            relicTiers.add(toLocalTier(rt.toString()));
        }
        relicTiers.add(0,CardSelectSortHeader.TEXT[0]);
    }

    public void update() {

        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            if (dropdownMenu.isOpen) {
                dropdownMenu.update();
                return;
            }
        }
        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }
        for (DropdownMenu dropdownMenu : this.dropdownMenus)
            dropdownMenu.update();

        if (this.searchBox != null) {
            this.searchBox.update();
        }
    }

    public Hitbox updateControllerInput() {
        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            Hitbox hb = dropdownMenu.getHitbox();
            if (hb.hovered) {
                return hb;
            }
        }
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        return null;
    }

    public int getHoveredIndex() {
        int retVal = 0;
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return retVal;
            }
            retVal++;
        }
        return 0;
    }

    public void clearActiveButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            button.setActive(false);
        }
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        for (int i = 0;i<this.buttons.length;i++) {
            if (i!= btnIdx) {
                HeaderButtonPlus button = buttons[i];

                button.reset();

            }
        }
    }
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            button.reset();
        }
        for (DropdownMenu ddm : dropdownMenus) {
            ddm.setSelectedIndex(0);
        }

    }

    public void render(SpriteBatch sb) {
        updateScrollPositions();
        if(this.searchBox != null) this.searchBox.render(sb);
        renderButtons(sb);
        renderSelection(sb);
    }

    protected void updateScrollPositions() {

    }

    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }

        float spaceY = 52.0f * Settings.yScale;
        float yPos = startY;

        float xPos = 0.0f;

        for (int i = 0; i< this.dropdownMenus.length ; i++) {

            DropdownMenu ddm = this.dropdownMenus[i];

            ddm.render(sb,xPos,yPos);
            yPos += 0.5f * spaceY;
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
            yPos += spaceY;
        }

    }

    protected void renderSelection(SpriteBatch sb) {
        for (int i = 0; i < this.buttons.length; i++) {
            if (i == this.selectionIndex) {
                this.selectionColor.a = 0.7F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F;
                sb.setColor(this.selectionColor);
                float doop = 1.0F + (1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L))) / 50.0F;

                sb.draw(img, (this.buttons[this.selectionIndex]).hb.cX - 80.0F - (this.buttons[this.selectionIndex]).textWidth / 2.0F * Settings.scale, (this.buttons[this.selectionIndex]).hb.cY - 43.0F, 100.0F, 43.0F, 160.0F + (this.buttons[this.selectionIndex]).textWidth, 86.0F, Settings.scale * doop, Settings.scale * doop, 0.0F, 0, 0, 200, 86, false, false);
            }
        }
    }

    protected static String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
    }

    protected static String toLocalTier(String rt) {
        switch (rt) {
            case "DEPRECATED":
                return removeLastChar(RelicSelectScreen.TEXT[0]);
            case "STARTER":
                return removeLastChar(RelicSelectScreen.rTEXT[1]);
            case "COMMON":
                return removeLastChar(RelicSelectScreen.rTEXT[3]);
            case "UNCOMMON":
                return removeLastChar(RelicSelectScreen.rTEXT[5]);
            case "RARE":
                return removeLastChar(RelicSelectScreen.rTEXT[7]);
            case "SPECIAL":
                return removeLastChar(RelicSelectScreen.rTEXT[11]);
            case "BOSS":
                return removeLastChar(RelicSelectScreen.rTEXT[9]);
            case "SHOP":
                return removeLastChar(RelicSelectScreen.rTEXT[13]);
            default:
                return toWordCase(rt);
        }
    }

    protected static String toWordCase(String str) {
        if (str.length() > 1)
            return str.toUpperCase().charAt(0) + str.toLowerCase().substring(1);
        else
            return String.valueOf(str.toUpperCase().charAt(0));
    }

}
