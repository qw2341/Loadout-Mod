package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MDeckViewSortHeader extends SortHeader implements EnhancedTextInputReceiver {

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("MasterDeckViewSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    public ArrayList<String> loadouts;
    private HeaderButtonPlus saveButton;
    private TextPopup namingPopup;
    private HeaderButtonPlus loadButton;
    private DropdownMenu loadoutsButton;

    private HeaderButtonPlus deleteButton;

    private String currentLoadoutName = "";

    public MDeckViewSortHeader() {
        super(null);

        float xPos = startX;
        float yPos = START_Y;

        this.loadouts = new ArrayList<>();
        loadLoadouts();

        this.loadoutsButton = new DropdownMenu(this, this.loadouts, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.saveButton = new HeaderButtonPlus(TEXT[0], xPos, yPos, this, true, ImageMaster.SETTINGS_ICON);
        yPos -= SPACE_Y;
        this.loadButton = new HeaderButtonPlus(TEXT[3], xPos, yPos, this, true, ImageMaster.DECK_BTN_BASE);
        yPos -= SPACE_Y;

        this.deleteButton = new HeaderButtonPlus(TEXT[1], xPos, yPos, this, true, ImageMaster.PROFILE_DELETE);



        this.buttons = new HeaderButtonPlus[] { this.saveButton, this.loadButton ,this.deleteButton};
        this.dropdownMenuHeaders = new String[] { TEXT[4] };
        this.dropdownMenus = new DropdownMenu[] { this.loadoutsButton };

        this.namingPopup = new TextPopup(this,TEXT[5]);
    }

    private void loadLoadouts() {
        this.loadouts.clear();
        this.loadouts.add(TEXT[2]);
        this.loadouts.addAll(CardLoadouts.loadouts.keySet());
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

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
            if(this.loadoutsButton.getSelectedIndex() != 0) {
                CardLoadouts.removeLoadout(getCurrentLoadoutName());
                try {
                    LoadoutMod.cardLoadouts.save();
                } catch (IOException e) {
                    LoadoutMod.logger.info("Error saving card loadouts");
                }
                loadLoadouts();
                this.loadoutsButton.setSelectedIndex(this.loadoutsButton.getSelectedIndex()-1);
            }
        }
    }

    @Override
    public void onConfirming() {
        CardLoadouts.addLoadout(currentLoadoutName, AbstractDungeon.player.masterDeck.group);
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
    }

    @Override
    public String getTextField() {
        return currentLoadoutName;
    }

    @Override
    public void update() {
        if(!this.namingPopup.shown) {
            super.update();
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
}
