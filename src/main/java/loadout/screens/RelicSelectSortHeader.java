package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import loadout.LoadoutMod;
import loadout.helper.RelicModComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import static loadout.LoadoutMod.*;

public class RelicSelectSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {
    //private static final UIStrings cUIStrings = CardCrawlGame.languagePack.getUIString("CardLibSortHeader");
    //public static final String[] cTEXT = cUIStrings.TEXT;

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final UIStrings esKeyUiStrings = CardCrawlGame.languagePack.getUIString("RewardItem");
    public static final String[] esTEXT = esKeyUiStrings.TEXT;
    public static final String GREEN_KEY_TEXT = esTEXT[5];
    public static final String BLUE_KEY_TEXT = esTEXT[6];
    public static final String RED_KEY_TEXT = TEXT[4];

    public boolean justSorted = false;

    public static final float START_X = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;
    private HeaderButtonPlus rarityButton;
    private HeaderButtonPlus classButton;
    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus modButton;
    private HeaderButtonPlus obtainRedKeyButton;
    private HeaderButtonPlus obtainGreenKeyButton;
    private HeaderButtonPlus obtainBlueKeyButton;
    private DropdownMenu numRelicsButton;
    private DropdownMenu selectionModeButton;

    private DropdownMenu colorFilterDropdown;

    private DropdownMenu rarityFilterDropdown;
    private DropdownMenu modNameDropdown;

    private String[] dropdownMenuHeaders;
    public DropdownMenu[] dropdownMenus;

    public HeaderButtonPlus[] buttons;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public ArrayList<String> relicMods;
    public HashMap<String,String> relicModNames;

    public RelicSelectScreen relicSelectScreen;



    public RelicSelectSortHeader(RelicSelectScreen relicSelectScreen) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        float xPosition = START_X;
        float yPosition = START_Y;
        this.classButton = new HeaderButtonPlus(TEXT[0], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.rarityButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.nameButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(TEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= 2*SPACE_Y;
        this.obtainRedKeyButton = new HeaderButtonPlus(RED_KEY_TEXT, xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.RIGHT);
        this.obtainRedKeyButton.isAscending = Settings.hasRubyKey;
        this.obtainRedKeyButton.setActive(Settings.hasRubyKey);
        yPosition -= SPACE_Y;
        this.obtainGreenKeyButton = new HeaderButtonPlus(GREEN_KEY_TEXT, xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.RIGHT);
        this.obtainGreenKeyButton.isAscending = Settings.hasEmeraldKey;
        this.obtainGreenKeyButton.setActive(Settings.hasEmeraldKey);
        yPosition -= SPACE_Y;
        this.obtainBlueKeyButton = new HeaderButtonPlus(BLUE_KEY_TEXT, xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.RIGHT);
        this.obtainBlueKeyButton.isAscending = Settings.hasSapphireKey;
        this.obtainBlueKeyButton.setActive(Settings.hasSapphireKey);
        String[] a=new String[10];
        for (int i=0;i<10;++i){
            a[i]= String.valueOf(i+1) + "x";
        }
        this.numRelicsButton = new DropdownMenu(this,a, FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.numRelicsButton.setSelectedIndex(LoadoutMod.relicObtainMultiplier -1);
        String[] b = new String[2];
        b[0]=TEXT[8];
        b[1]=TEXT[9];
        this.selectionModeButton = new DropdownMenu(this,b,FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.selectionModeButton.setSelectedIndex(LoadoutMod.enableDrag ? 0 : 1);

        ArrayList<String> aa = new ArrayList<>();

        for (AbstractPlayer ap : allCharacters) {
            aa.add(ap.getLoadout().name);
        }

        aa.add(0,CardSelectSortHeader.TEXT[0]);//All
        aa.add(removeLastChar(RelicSelectScreen.TEXT[4]));//Shared
        this.colorFilterDropdown = new DropdownMenu(this,aa,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        ArrayList<String> e = new ArrayList<>();
        for (AbstractRelic.RelicTier rt : AbstractRelic.RelicTier.values()) {
            e.add(toLocalTier(rt));
        }
        e.add(0,CardSelectSortHeader.TEXT[0]);
        this.rarityFilterDropdown = new DropdownMenu(this,e,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        relicMods = new ArrayList<>(findRelicAddingMods());
        relicMods.sort(Comparator.naturalOrder());
        ArrayList<String> f = new ArrayList<>();
        String fName;
        for (String id : relicMods) {
            fName = relicModNames.get(id);
            if (fName.length() >= 12) fName = fName.substring(0,11) + "...";
            f.add(fName);
        }


        f.add(0,CardSelectSortHeader.TEXT[0]);
        f.add("Slay the Spire");
        this.modNameDropdown = new DropdownMenu(this, f,FontHelper.panelNameFont, Settings.CREAM_COLOR);



        this.buttons = new HeaderButtonPlus[] { this.classButton, this.rarityButton, this.nameButton, this.modButton, this.obtainRedKeyButton, this.obtainGreenKeyButton, this.obtainBlueKeyButton };

        this.dropdownMenuHeaders = new String[] {TEXT[7],TEXT[10],CardSelectSortHeader.clTEXT[0],TEXT[0],"Mod"};
        this.dropdownMenus = new DropdownMenu[] {this.numRelicsButton, this.selectionModeButton, this.rarityFilterDropdown, this.colorFilterDropdown, this.modNameDropdown};

        this.relicSelectScreen = relicSelectScreen;

    }

    private HashSet<String> findRelicAddingMods() {
        HashSet<String> modIDs = new HashSet<>();
        relicModNames = new HashMap<String, String>();
        for (AbstractRelic ar : relicsToDisplay) {
            String modID = WhatMod.findModID(ar.getClass());

            if (modID == null) continue;
            if (modIDs.contains(modID)) {
                continue;
            } else {
                modIDs.add(modID);
                relicModNames.put(modID,WhatMod.findModName(ar.getClass()));
            }
        }
        return modIDs;
    }

    private String toLocalTier(AbstractRelic.RelicTier rt) {
        switch (rt) {
            case DEPRECATED:
                return removeLastChar(RelicSelectScreen.TEXT[0]);
            case STARTER:
                return removeLastChar(RelicSelectScreen.rTEXT[1]);
            case COMMON:
                return removeLastChar(RelicSelectScreen.rTEXT[3]);
            case UNCOMMON:
                return removeLastChar(RelicSelectScreen.rTEXT[5]);
            case RARE:
                return removeLastChar(RelicSelectScreen.rTEXT[7]);
            case SPECIAL:
                return removeLastChar(RelicSelectScreen.rTEXT[11]);
            case BOSS:
                return removeLastChar(RelicSelectScreen.rTEXT[9]);
            case SHOP:
                return removeLastChar(RelicSelectScreen.rTEXT[13]);
            default:
                return toWordCase(rt.toString());
        }
    }

    //Use to remove the colon
    private String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
    }

    private String toWordCase(String str) {
        if (str.length() > 1)
            return str.toUpperCase().charAt(0) + str.toLowerCase().substring(1);
        else
            return String.valueOf(str.toUpperCase().charAt(0));
    }


    public void update() {
        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }

        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            if (dropdownMenu.isOpen) {
                dropdownMenu.update();
                return;
            }
        }

        for (DropdownMenu dm : this.dropdownMenus) {
            dm.update();
        }

    }

    public Hitbox updateControllerInput() {
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        for (DropdownMenu dm: this.dropdownMenus) {
            if(dm.getHitbox().hovered)
                return dm.getHitbox();
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
        //does not clear the last 3 buttons
        for (int i = 0;i<this.buttons.length-3;i++) {
            buttons[i].setActive(false);
        }
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-3;i++) {
            if(i!=btnIdx) {
                buttons[i].reset();
            }
        }
    }
    public void resetAllButtons() {
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-3;i++) {
            buttons[i].reset();
        }

        //also reset the select mode button in case of change
        for (DropdownMenu ddm : dropdownMenus) {
            if (ddm != this.selectionModeButton) {
                ddm.setSelectedIndex(0);
            } else {
                this.selectionModeButton.setSelectedIndex(LoadoutMod.enableDrag ? 0 : 1);
            }
        }
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.rarityButton) {
            clearActiveButtons();
            this.relicSelectScreen.sortByRarity(isAscending);
            resetOtherButtons();
        } else if (button == this.classButton) {
            clearActiveButtons();
            this.relicSelectScreen.sortByClass(isAscending);
            resetOtherButtons();
        } else if (button == this.nameButton) {
            clearActiveButtons();
            this.relicSelectScreen.sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.relicSelectScreen.sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.obtainRedKeyButton) {
            if(isAscending)
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.RED));
            else
                Settings.hasRubyKey = false;
        } else if (button == this.obtainGreenKeyButton) {
            if(isAscending)
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.GREEN));
            else
                Settings.hasEmeraldKey = false;
        } else if (button == this.obtainBlueKeyButton) {
            if(isAscending)
                AbstractDungeon.topLevelEffects.add(new ObtainKeyEffect(ObtainKeyEffect.KeyColor.BLUE));
            else
                Settings.hasSapphireKey = false;
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);

    }

    public void render(SpriteBatch sb) {
        //sb.draw(ImageMaster.COLOR_TAB_BAR, 10.0F, -50.0F, 300.0F, 500.0F, 0, 0, 1334, 102, false, false);
        updateScrollPositions();
        renderButtons(sb);
        renderSelection(sb);
    }

    protected void updateScrollPositions() {

    }

    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }
        if(!relicSelectScreen.isDeleteMode) {
            float spaceY = 52.0f * Settings.yScale;
            float yPos = START_Y - 4.0f * spaceY;

            float xPos = 20.0f * Settings.scale;

            for (int i = 0; i< this.dropdownMenus.length ; i++) {

                DropdownMenu ddm = this.dropdownMenus[i];

                ddm.render(sb,xPos,yPos);
                yPos += 0.5f * spaceY;
                FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
                yPos += spaceY;
            }
//
//            this.numRelicsButton.render(sb,xPos,yPos);
//            yPos += spaceY;
//            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, TEXT[7], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
//            yPos += 2.0f * spaceY;
//            this.selectionModeButton.render(sb,xPos,yPos);
//            yPos += spaceY;
//            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, TEXT[10], xPos, yPos, 200.0F, 20.0F, Settings.GOLD_COLOR);

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

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.numRelicsButton) {
            LoadoutMod.relicObtainMultiplier = i+1;
            RelicSelectScreen.selectMult = i+1;
            try {
                //Field f = LoadoutMod.class.getDeclaredField("config");
                //f.setAccessible(true);
                //SpireConfig config = (SpireConfig) f.get(null);
                LoadoutMod.config.setInt(RELIC_OBTAIN_AMOUNT, i+1);
                LoadoutMod.config.save();
            } catch (NullPointerException nPE) {
                logger.debug("null pointer exception caught, caused by num relics dropdown");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (dropdownMenu == this.selectionModeButton) {
            LoadoutMod.enableDrag = i == 0;
            try {
                //Field f = LoadoutMod.class.getDeclaredField("config");
                //f.setAccessible(true);
                //SpireConfig config = (SpireConfig) f.get(null);
                LoadoutMod.config.setBool(ENABLE_DRAG_SELECT, enableDrag);
                LoadoutMod.config.save();
            } catch (NullPointerException nPE) {
                logger.debug("null pointer exception caught, caused by selection mode dropdown");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if(dropdownMenu == this.colorFilterDropdown) {
            if (i==0) {
                //if showing all
                relicSelectScreen.filterColor = null;
            } else if (i == allCharacters.size() + 1) {
                relicSelectScreen.filterColor = AbstractCard.CardColor.COLORLESS;
            } else {
                relicSelectScreen.filterColor = allCharacters.get(i-1).getCardColor();
            }
            relicSelectScreen.updateFilters();
        }

        if (dropdownMenu == this.rarityFilterDropdown) {
            if (i==0)
                relicSelectScreen.filterRarity = null;
            else {
                relicSelectScreen.filterRarity = AbstractRelic.RelicTier.values()[i-1];
            }
            relicSelectScreen.updateFilters();
        }
        if (dropdownMenu == this.modNameDropdown) {
            if (i==0) {
                //if showing all
                relicSelectScreen.filterMod = null;
            } else if (i==(relicMods.size()+2)-1) {
                relicSelectScreen.filterMod = "Slay the Spire";
            } else {
                relicSelectScreen.filterMod = relicMods.get(i-1);
            }
            relicSelectScreen.updateFilters();
        }
    }
}
