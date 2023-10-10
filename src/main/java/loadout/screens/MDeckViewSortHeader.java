package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;
import loadout.helper.EnhancedTextInputReceiver;
import loadout.savables.CardLoadouts;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MDeckViewSortHeader extends AbstractSortHeader implements EnhancedTextInputReceiver {

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("MasterDeckViewSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    public ArrayList<String> loadouts;
    private HeaderButtonPlus saveButton;
    public TextPopup namingPopup;
    private HeaderButtonPlus loadButton;
    private DropdownMenu loadoutsButton;

    private HeaderButtonPlus deleteButton;

    private HeaderButtonPlus copyToClipButton;
    private HeaderButtonPlus loadFromClipButton;

    private String currentLoadoutName = "";

    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public MDeckViewSortHeader() {
        super(null);

        float xPos = 10.0F * Settings.scale;
        float yPos = START_Y - 100.0F * Settings.yScale;
        float spaceY = 50.0f * Settings.yScale;

        this.loadouts = new ArrayList<>();
        loadLoadouts();

        //this.loadoutsButton = new DropdownMenu(this, this.loadouts, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.saveButton = new HeaderButtonPlus(TEXT[0], xPos, yPos, this, true, ImageMaster.SETTINGS_ICON);
        this.saveButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPos -= spaceY;
        this.loadButton = new HeaderButtonPlus(TEXT[3], xPos, yPos, this, true, ImageMaster.DECK_BTN_BASE);
        this.loadButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPos -= spaceY;

        this.deleteButton = new HeaderButtonPlus(TEXT[1], xPos, yPos, this, true, ImageMaster.PROFILE_DELETE);
        this.deleteButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPos -= spaceY;
        this.copyToClipButton = new HeaderButtonPlus(TEXT[6], xPos, yPos, this, true, ImageMaster.PROFILE_RENAME);
        this.copyToClipButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPos -= spaceY;
        this.loadFromClipButton = new HeaderButtonPlus(TEXT[7], xPos, yPos, this, true, ImageMaster.PROFILE_A);
        this.loadFromClipButton.alignment = HeaderButtonPlus.Alignment.LEFT;

        this.buttons = new HeaderButtonPlus[] { this.saveButton, this.loadButton ,this.deleteButton, this.copyToClipButton, this.loadFromClipButton};
        this.dropdownMenuHeaders = new String[] { TEXT[4] };
        this.dropdownMenus = new DropdownMenu[] { this.loadoutsButton };

        this.namingPopup = new TextPopup(this,TEXT[5], false, true);
    }

    private void loadLoadouts() {
        this.loadouts.clear();
        this.loadouts.add(TEXT[2]);
        this.loadouts.addAll(CardLoadouts.loadouts.keySet());
        this.loadoutsButton = new DropdownMenu(this, this.loadouts, FontHelper.panelNameFont, Settings.CREAM_COLOR);
        if(this.dropdownMenus != null) dropdownMenus[0] = this.loadoutsButton;

        LoadoutMod.logger.info("Loading card loadouts: " + this.loadouts.toString());
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.loadoutsButton) {
            this.currentLoadoutName = this.loadouts.get(i);
        }
    }

    private String getCurrentLoadoutName() {
        return this.loadouts.get(loadoutsButton.getSelectedIndex());
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {

        if(button == this.saveButton) {
            this.namingPopup.open();
        } else if (button == this.loadButton) {
            if(this.loadoutsButton.getSelectedIndex() == 0) {
                //if starter
                AbstractDungeon.player.masterDeck.group = AbstractDungeon.player.getStartingDeck().stream().map(cID -> CardLibrary.getCard(cID).makeCopy()).collect(Collectors.toCollection(ArrayList::new));
            } else {
                AbstractDungeon.player.masterDeck.group = CardLoadouts.getLoadout(getCurrentLoadoutName());
            }

        } else if (button == this.deleteButton) {
            if(this.loadoutsButton.getSelectedIndex() > 0) {
                CardLoadouts.removeLoadout(getCurrentLoadoutName());
                try {
                    LoadoutMod.cardLoadouts.save();
                } catch (IOException e) {
                    LoadoutMod.logger.info("Error saving card loadouts");
                }
                int tgtIdx = this.loadoutsButton.getSelectedIndex()-1;
                loadLoadouts();
                this.loadoutsButton.setSelectedIndex(tgtIdx);
            }
        } else if (button == this.copyToClipButton) {
            setClipboardString(CardLoadouts.exportEncodedLoadout(AbstractDungeon.player.masterDeck.group));
        }else if (button == this.loadFromClipButton) {
            try {

                AbstractDungeon.player.masterDeck.group = CardLoadouts.importEncodedLoadout(getClipboardString());
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to import from clipboard!");
            }

        }
    }

    @Override
    public void onConfirming() {
        CardLoadouts.addLoadout(currentLoadoutName, AbstractDungeon.player.masterDeck.group);
        loadLoadouts();
        this.loadoutsButton.setSelectedIndex(this.loadouts.indexOf(currentLoadoutName));
        try {
            LoadoutMod.cardLoadouts.save();
        } catch (IOException e) {
            LoadoutMod.logger.info("Error saving card loadouts");
        }
    }

    @Override
    public void onCanceling() {

    }

    @Override
    public void setTextField(String textToSet) {

        currentLoadoutName = textToSet;
        //LoadoutMod.logger.info("Entered: " + currentLoadoutName);
    }

    @Override
    public String getTextField() {
        return currentLoadoutName;
    }

    @Override
    public void update() {
        if(!this.namingPopup.shown) {

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
        } else {
            this.namingPopup.update();
        }

    }

    @Override
    public void render(SpriteBatch sb) {
        if(!this.namingPopup.shown) {
            super.render(sb);
        } else {
            this.namingPopup.render(sb);
        }

    }

    public static void setClipboardString(String content) {
        StringSelection selection = new StringSelection(content);
        clipboard.setContents(selection,selection);
    }

    public static String getClipboardString() {
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            LoadoutMod.logger.info("Failed to import deck preset from Clipboard");
            e.printStackTrace();
        }
        return "";
    }
}
