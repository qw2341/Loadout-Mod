package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import loadout.LoadoutMod;
import loadout.util.ModConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import static loadout.LoadoutMod.*;

public class RelicSelectSortHeader extends AbstractSortHeader implements HeaderButtonPlusListener, DropdownMenuListener {
    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final UIStrings esKeyUiStrings = CardCrawlGame.languagePack.getUIString("RewardItem");
    public static final String[] esTEXT = esKeyUiStrings.TEXT;
    public static final String GREEN_KEY_TEXT = esTEXT[5];
    public static final String BLUE_KEY_TEXT = esTEXT[6];
    public static final String RED_KEY_TEXT = TEXT[4];

    public static final float START_X = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;
    private final HeaderButtonPlus rarityButton;
    private final HeaderButtonPlus classButton;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus obtainRedKeyButton;
    private final HeaderButtonPlus obtainGreenKeyButton;
    private final HeaderButtonPlus obtainBlueKeyButton;
    private final DropdownMenu numRelicsButton;
    private final DropdownMenu selectionModeButton;

    private final DropdownMenu colorFilterDropdown;

    private final DropdownMenu rarityFilterDropdown;
    private final DropdownMenu modNameDropdown;

    private final Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public ArrayList<String> relicMods;
    public HashMap<String,String> relicModNames;

    public RelicSelectScreen relicSelectScreen;



    public RelicSelectSortHeader(RelicSelectScreen relicSelectScreen) {
        super(relicSelectScreen);

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

        this.searchBox = new TextSearchBox(this, 0.0F, yPosition, false);

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
        this.numRelicsButton.setSelectedIndex(ModConfig.relicObtainMultiplier -1);
        String[] b = new String[2];
        b[0]=TEXT[8];
        b[1]=TEXT[9];
        this.selectionModeButton = new DropdownMenu(this,b,FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.selectionModeButton.setSelectedIndex(ModConfig.enableDrag ? 0 : 1);

        this.colorFilterDropdown = new DropdownMenu(this,playerClasses,FontHelper.panelNameFont, Settings.CREAM_COLOR);


        this.rarityFilterDropdown = new DropdownMenu(this,relicTiers,FontHelper.panelNameFont, Settings.CREAM_COLOR);

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



    //Use to remove the colon




@Override
    public void clearActiveButtons() {
        //does not clear the last 3 buttons
        for (int i = 0;i<this.buttons.length-3;i++) {
            buttons[i].setActive(false);
        }
    }

    @Override
    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 3 buttons
        for (int i = 0;i<this.buttons.length-3;i++) {
            if(i!=btnIdx) {
                buttons[i].reset();
            }
        }
    }
    @Override
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
                this.selectionModeButton.setSelectedIndex(ModConfig.enableDrag ? 0 : 1);
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


    protected void updateScrollPositions() {

    }

    @Override
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

        }
    }


    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.numRelicsButton) {
            ModConfig.relicObtainMultiplier = i+1;
            RelicSelectScreen.selectMult = i+1;
            try {
                ModConfig.config.setInt(ModConfig.RELIC_OBTAIN_AMOUNT, i+1);
                ModConfig.config.save();
            } catch (NullPointerException nPE) {
                logger.debug("null pointer exception caught, caused by num relics dropdown");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (dropdownMenu == this.selectionModeButton) {
            ModConfig.enableDrag = i == 0;
            try {
                ModConfig.config.setBool(ModConfig.ENABLE_DRAG_SELECT, ModConfig.enableDrag);
                ModConfig.config.save();
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
