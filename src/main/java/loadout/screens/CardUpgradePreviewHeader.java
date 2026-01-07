package loadout.screens;

import basemod.patches.whatmod.CardView;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Header that uses CardEffectButtons to tweak upgrade diffs.
 */
public class CardUpgradePreviewHeader implements HeaderButtonPlusListener, CardEffectButton.CardStuffProvider {

//    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardUpgradePreviewHeader"));
//    public static final String[] TEXT = uiStrings.TEXT;
    private static final float START_Y = Settings.HEIGHT - 180.0f * Settings.scale;
    private static final float SPACE_Y = 48.0f * Settings.scale;

    private static final float STAT_BUTTON_X = Settings.WIDTH / 2.0f - 180.0f * Settings.scale;

    private final CardUpgradePreviewScreen screen;

    private final List<CardEffectButton> normalButtons = new ArrayList<>();
    private final List<CardEffectButton> additionalButtons = new ArrayList<>();

    public CardUpgradePreviewHeader(CardUpgradePreviewScreen screen) {
        this.screen = screen;
    }

    public void syncWithCard(AbstractCard card) {
        if (normalButtons.isEmpty()) {
            buildNormalButtons();
        }
        rebuildAdditionalButtons(card);
    }

    private void buildNormalButtons() {
        normalButtons.clear();
        float x = STAT_BUTTON_X;
        float y = START_Y;
        addNormalRow(CardViewPopupHeader.clTEXT[3], 0, x, y);
        y -= SPACE_Y;
        addNormalRow(CardViewPopupHeader.TEXT[0], 1, x, y);
        y -= SPACE_Y;
        addNormalRow(StringUtils.capitalize(CardViewPopupHeader.TEXT_BLOCK), 2, x, y);
        y -= SPACE_Y;
        addNormalRow(CardViewPopupHeader.TEXT[1], 3, x, y);
        y -= SPACE_Y;
        addNormalRow("Misc", 4, x, y);
    }

    private void rebuildAdditionalButtons(AbstractCard card) {
        additionalButtons.clear();
        float y = START_Y - SPACE_Y * 5;

        for (String key : getAdditionalMagicKeys(card)) {
            addAdditionalMagicRow(key, STAT_BUTTON_X, y);
            y -= SPACE_Y;
        }
    }

    private void addNormalRow(String label, int index, float x, float y) {
        CardEffectButton ceb = new CardEffectButton(null, x, y, label, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                Integer[] diffs = screen.getNormalUpgradeDiffs();
                if (diffs == null || diffs.length <= index || diffs[index] == null) return 0;
                return diffs[index];
            }

            @Override
            public void setAmount(int amountToSet) {
                screen.setNormalUpgrade(index, amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                // not used for numeric diff
            }
        }, this);
        normalButtons.add(ceb);
    }

    private void addAdditionalMagicRow(String key, float x, float y) {
        CardEffectButton ceb = new CardEffectButton(null, x, y, key, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return screen.getAdditionalMagicUpgradeDiffs(key);
            }

            @Override
            public void setAmount(int amountToSet) {
                screen.setAdditionalMagicUpgrade(key, amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                // not used for numeric diff
            }
        }, this);
        additionalButtons.add(ceb);
    }

    public void update() {
        for (CardEffectButton ceb : normalButtons) {
            ceb.update();
        }
        for (CardEffectButton ceb : additionalButtons) {
            ceb.update();
        }
    }

    public void render(SpriteBatch sb) {
        float titleY = START_Y + 60.0f * Settings.scale;
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, CardViewPopupHeader.TEXT[38], STAT_BUTTON_X, titleY, Color.GOLD);

        for (CardEffectButton ceb : normalButtons) {
            ceb.render(sb);
        }
        for (CardEffectButton ceb : additionalButtons) {
            ceb.render(sb);
        }
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        // CardEffectButtons handle their own arrows; nothing extra needed.
    }


    public void resetOtherButtons() {
        // no-op
    }


    public void clearActiveButtons() {
        // no-op
    }

    /**
     * We can get the multiplier from LoadoutMod directly.
     */
    @Override
    public int getMultiplier() {
        return LoadoutMod.universalMultiplier;
    }

    @Override
    public AbstractCard getCard() {
        return screen.getSourceCard() == null ? new Madness() : screen.getSourceCard();
    }

    private Set<String> getAdditionalMagicKeys(AbstractCard card) {
        return AbstractCardPatch.CardModificationFields.additionalMagicNumbers.get(card).keySet();
    }
}
