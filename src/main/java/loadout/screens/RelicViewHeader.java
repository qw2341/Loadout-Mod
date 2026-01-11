package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.Abacus;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import loadout.LoadoutMod;
import loadout.patches.AbstractRelicPatches;

import java.util.ArrayList;
import java.util.Map;

public class RelicViewHeader implements HeaderButtonPlusListener, DropdownMenuListener {

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicViewHeader"));

    public static final String[] TEXT = uiStrings.TEXT;
    public static AbstractRelic currentRelic = new Abacus();
    private final ArrayList<CardEffectButton> statButtons = new ArrayList<>();
    private CardEffectButton counterButton;

    private static final float START_X = 250.0f * Settings.scale;
    private static final float START_Y = Settings.HEIGHT / 2.0F + 300.0f * Settings.scale;
    private static final float SPACE_Y = 100.0f * Settings.scale;

    private static final float ICON_OFFSET_X = 50.0f * Settings.scale;


    public RelicViewHeader() {

    }

    public void onOpen(AbstractRelic relicToOpen) {
        currentRelic = relicToOpen;
        buildButtons();
    }

    private void buildButtons() {
        this.statButtons.clear();
        float yPosition = START_Y;
        if (this.counterButton == null) {
            this.counterButton = new CardEffectButton(ImageMaster.MAP_NODE_REST, START_X, yPosition,TEXT[0], new StatModSelectScreen.StatModActions() {
                @Override
                public int getAmount() {
                    return currentRelic.counter;
                }

                @Override
                public void setAmount(int amountToSet) {
                    currentRelic.counter = amountToSet;
                }

                @Override
                public void onBoolChange(boolean boolToChange, int amount) {
                    AbstractRelicPatches.RelicCounterFields.isCounterLocked.set(currentRelic, boolToChange);
                    AbstractRelicPatches.RelicCounterFields.counterLockAmount.set(currentRelic, amount);
                }
            }, null,() -> AbstractRelicPatches.RelicCounterFields.isCounterLocked.get(currentRelic));
        }
        this.counterButton.refreshBool();
        this.statButtons.add(this.counterButton);
        yPosition -= SPACE_Y;
    for (Map.Entry<String, Integer> entry : AbstractRelicPatches.RelicCounterFields.relicMagicNumberMap.get(currentRelic).entrySet()) {
        CardEffectButton b = new CardEffectButton(ImageMaster.SETTINGS_ICON, START_X, yPosition, entry.getKey(), new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return entry.getValue();
            }

            @Override
            public void setAmount(int amountToSet) {
                entry.setValue(amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, null);
        this.statButtons.add(b);
        yPosition -= SPACE_Y;
    }
    }

    public void update() {
        if(this.counterButton == null) {
            this.buildButtons();
        }
        for (CardEffectButton button : this.statButtons) {
            button.update();
        }
    }

    public void render(SpriteBatch sb) {
        for (CardEffectButton button : this.statButtons) {
            button.render(sb);
        }
    }
    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

    }

    @Override
    public void didChangeOrder(HeaderButtonPlus var1, boolean var2) {

    }
}
