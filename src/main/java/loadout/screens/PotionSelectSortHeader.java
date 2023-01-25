package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import static loadout.LoadoutMod.*;

public class PotionSelectSortHeader extends AbstractSortHeader {

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("PotionSelectSortHeader"));
    public static final String[] TEXT = UiStrings.TEXT;
    private static final PotionStrings pStrings = CardCrawlGame.languagePack.getPotionString("Potion Slot");
    public static final String POTION_SLOT_NAME = pStrings.NAME;


    public static final float START_X = 600.0F * Settings.xScale;
    public static final float START_Y = Settings.HEIGHT - 400.0f * Settings.yScale;

    private final HeaderButtonPlus rarityButton;
    private final HeaderButtonPlus classButton;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus slotAddButton;
    private final HeaderButtonPlus slotSubButton;
    private final DropdownMenu colorFilterDropdown;
    private final DropdownMenu rarityFilterDropdown;
    private final DropdownMenu modNameDropdown;
    private ArrayList<String> potionMods;
    private HashMap<String, String> potionModNames;

    public PotionSelectSortHeader(PotionSelectScreen potionSelectScreen) {
        super(potionSelectScreen);
        float xPosition = START_X;
        float yPosition = START_Y;

        this.classButton = new HeaderButtonPlus(TEXT[0], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.rarityButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.nameButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(TEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.searchBox = new TextSearchBox(this, 0.0F, yPosition,false);
        yPosition -= SPACE_Y;
        this.slotAddButton = new HeaderButtonPlus(POTION_SLOT_NAME, xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.slotAddButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.slotSubButton = new HeaderButtonPlus(POTION_SLOT_NAME, xPosition, yPosition, this,false,false, HeaderButtonPlus.Alignment.RIGHT);
        this.slotSubButton.isAscending = true;

        this.colorFilterDropdown = new DropdownMenu(this,playerClasses, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        ArrayList<String> e = new ArrayList<>();
        for (AbstractPotion.PotionRarity apr : AbstractPotion.PotionRarity.values()) {
            e.add(toLocalTier(apr.toString()));
        }
        e.add(0,CardSelectSortHeader.TEXT[0]);
        this.rarityFilterDropdown = new DropdownMenu(this,e,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        potionMods = new ArrayList<>(findPotionAddingMods());
        potionMods.sort(Comparator.naturalOrder());
        ArrayList<String> f = new ArrayList<>();
        String fName;
        for (String id : potionMods) {
            fName = potionModNames.get(id);
            if (fName.length() >= 12) fName = fName.substring(0,11) + "...";
            f.add(fName);
        }


        f.add(0,CardSelectSortHeader.TEXT[0]);
        f.add("Slay the Spire");
        this.modNameDropdown = new DropdownMenu(this, f,FontHelper.panelNameFont, Settings.CREAM_COLOR);


        this.buttons = new HeaderButtonPlus[] { this.classButton, this.rarityButton, this.nameButton, this.modButton, this.slotAddButton, this.slotSubButton };

        this.dropdownMenuHeaders= new String[] {CardSelectSortHeader.clTEXT[0],RelicSelectSortHeader.TEXT[0],"Mod"};
        this.dropdownMenus = new DropdownMenu[] {this.rarityFilterDropdown, this.colorFilterDropdown, this.modNameDropdown};
        this.startY = START_Y;
    }

    private HashSet<String> findPotionAddingMods() {
        HashSet<String> modIDs = new HashSet<>();
        potionModNames = new HashMap<String, String>();
        for (AbstractPotion ap : potionsToDisplay) {
            String modID = WhatMod.findModID(ap.getClass());

            if (modID == null) continue;
            if (modIDs.contains(modID)) {
                continue;
            } else {
                modIDs.add(modID);
                potionModNames.put(modID,WhatMod.findModName(ap.getClass()));
            }
        }
        return modIDs;
    }


    @Override
    public void clearActiveButtons() {
        //does not clear the last 2 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].setActive(false);
        }
    }

    @Override
    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        //not resetting the last 2 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            if(i!=btnIdx) {
                buttons[i].reset();
            }
        }
    }
    @Override
    public void resetAllButtons() {
        //not resetting the last 2 buttons
        for (int i = 0;i<this.buttons.length-2;i++) {
            buttons[i].reset();
        }
        for (DropdownMenu ddm : dropdownMenus) {
            ddm.setSelectedIndex(0);
        }
    }
    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.rarityButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.RARITY;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.classButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.CLASS;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.nameButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.NAME;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.selectScreen.currentSortType = AbstractSelectScreen.SortType.MOD;
            this.selectScreen.sort(isAscending);
            resetOtherButtons();
        } else if (button == this.slotAddButton) {
            AbstractDungeon.player.potionSlots ++;
            AbstractDungeon.player.potions.add(new PotionSlot(AbstractDungeon.player.potionSlots - 1));
        } else if (button == this.slotSubButton) {
            if(AbstractDungeon.player.potionSlots > 0) {
                AbstractDungeon.player.potionSlots --;
//            if(!(AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1) instanceof PotionSlot))
                AbstractDungeon.player.potions.remove(AbstractDungeon.player.potions.size()-1);
            }

        } else {
            return;
        }
        this.justSorted = true;
        if (button != this.slotSubButton && button != this.slotAddButton)
            button.setActive(true);

    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.colorFilterDropdown) {
            if (i==0) {
                //if showing all
                selectScreen.filterColor = null;
            } else if (i == allCharacters.size() + 1) {
                selectScreen.filterColor = AbstractCard.CardColor.COLORLESS;
            } else {
                selectScreen.filterColor = allCharacters.get(i-1).getCardColor();
            }
            selectScreen.updateFilters();
        }

        if (dropdownMenu == this.rarityFilterDropdown) {
            if (i==0)
                ((PotionSelectScreen)selectScreen).filterRarity = null;
            else {
                ((PotionSelectScreen)selectScreen).filterRarity = AbstractPotion.PotionRarity.values()[i-1];
            }
            selectScreen.updateFilters();
        }
        if (dropdownMenu == this.modNameDropdown) {
            if (i==0) {
                //if showing all
                selectScreen.filterMod = null;
            } else if (i==(potionMods.size()+2)-1) {
                selectScreen.filterMod = "Slay the Spire";
            } else {
                selectScreen.filterMod = potionMods.get(i-1);
            }
            selectScreen.updateFilters();
        }
    }
}
