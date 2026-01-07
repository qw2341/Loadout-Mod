package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Header that uses CardEffectButtons to tweak upgrade diffs.
 */
public class CardUpgradePreviewHeader implements HeaderButtonPlusListener, CardEffectButton.CardStuffProvider {
    private static final float START_Y = Settings.HEIGHT - 180.0f * Settings.scale;
    private static final float SPACE_Y = 48.0f * Settings.scale;

    private final CardUpgradePreviewScreen screen;

    private final List<CardEffectButton> normalButtons = new ArrayList<>();
    private final List<CardEffectButton> additionalButtons = new ArrayList<>();
    private Integer[] cachedNormalDiffs = new Integer[]{0, 0, 0, 0, 0};
    private Map<String, Integer> cachedAdditionalMagic = new HashMap<>();

    public CardUpgradePreviewHeader(CardUpgradePreviewScreen screen) {
        this.screen = screen;
    }

    public void syncWithCard(AbstractCard card) {
        if (normalButtons.isEmpty()) {
            buildNormalButtons();
        }
        rebuildAdditionalButtons(card);
        cachedNormalDiffs = screen.getNormalUpgradeDiffs();
        cachedAdditionalMagic = screen.getAdditionalMagicUpgradeDiffs();
    }

    private void buildNormalButtons() {
        normalButtons.clear();
        float x = Settings.WIDTH / 2.0f - 260.0f * Settings.scale;
        float y = START_Y;
        addNormalRow("Cost", 0, x, y);
        y -= SPACE_Y;
        addNormalRow("Damage", 1, x, y);
        y -= SPACE_Y;
        addNormalRow("Block", 2, x, y);
        y -= SPACE_Y;
        addNormalRow("Magic", 3, x, y);
        y -= SPACE_Y;
        addNormalRow("Misc", 4, x, y);
    }

    private void rebuildAdditionalButtons(AbstractCard card) {
        additionalButtons.clear();
        float x = Settings.WIDTH / 2.0f - 260.0f * Settings.scale;
        float y = START_Y - SPACE_Y * 5;

        for (String key : getAdditionalMagicKeys(card)) {
            addAdditionalMagicRow(key, x, y);
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
                int current = getAmount();
                int delta = amountToSet - current;
                screen.adjustNormalUpgrade(index, delta);
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
                return screen.getAdditionalMagicUpgradeDiffs().getOrDefault(key, 0);
            }

            @Override
            public void setAmount(int amountToSet) {
                int current = getAmount();
                int delta = amountToSet - current;
                screen.adjustAdditionalMagicUpgrade(key, delta);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                // not used for numeric diff
            }
        }, this);
        additionalButtons.add(ceb);
    }

    public void update() {
        cachedNormalDiffs = screen.getNormalUpgradeDiffs();
        cachedAdditionalMagic = screen.getAdditionalMagicUpgradeDiffs();
        for (CardEffectButton ceb : normalButtons) {
            ceb.update();
        }
        for (CardEffectButton ceb : additionalButtons) {
            ceb.update();
        }
    }

    public void render(SpriteBatch sb) {
        float titleX = Settings.WIDTH / 2.0f - 260.0f * Settings.scale;
        float titleY = START_Y + 48.0f * Settings.scale;
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "Upgrade Diffs", titleX, titleY, Color.GOLD);

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

    private List<String> getAdditionalMagicKeys(AbstractCard card) {
        ArrayList<String> keys = new ArrayList<>();
        if (card == null) {
            return keys;
        }
        String serialized = AbstractCardPatch.serializeAdditionalMagicNumbers(card);
        if (serialized == null || serialized.isEmpty()) {
            return keys;
        }
        String[] pairs = serialized.split(AbstractCardPatch.MAGIC_NUMBER_DELIMITER);
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("\\|");
            if (kv.length > 0) {
                keys.add(kv[0]);
            }
        }
        return keys;
    }
}
