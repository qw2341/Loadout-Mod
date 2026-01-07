package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import loadout.patches.AbstractCardPatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple screen that previews an upgrade: original card on the left and the upgraded copy on the right.
 * The header exposes controls to tweak upgrade diffs.
 */
public class CardUpgradePreviewScreen {
    private final Color overlayColor = new Color(0f, 0f, 0f, 0.6f);
    public boolean show = false;

    private SCardViewPopup parent;
    private AbstractCard sourceCard;
    private AbstractCard basePreview;
    private AbstractCard upgradedPreview;
    private CardUpgradePreviewHeader header;

    public void open(SCardViewPopup parent, AbstractCard card) {
        this.parent = parent;
        this.sourceCard = card;
        if (this.header == null) {
            this.header = new CardUpgradePreviewHeader(this);
        }
        this.header.syncWithCard(card);
        refreshPreview();
        this.show = true;
    }

    public void close() {
        this.show = false;
    }

    public void update() {
        if (!show) return;
        if (InputHelper.pressedEscape) {
            close();
            return;
        }
        if (header != null) {
            header.update();
        }
        if (basePreview != null) {
            basePreview.update();
        }
        if (upgradedPreview != null) {
            upgradedPreview.update();
        }
    }

    public void render(SpriteBatch sb) {
        if (!show) return;
        sb.setColor(overlayColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(Color.WHITE);

        if (basePreview != null) {
            basePreview.render(sb);
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "Original", basePreview.current_x - 80.0f * Settings.scale, basePreview.current_y + 260.0f * Settings.scale, Color.GOLD);
        }
        if (upgradedPreview != null) {
            upgradedPreview.render(sb);
            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "Upgraded", upgradedPreview.current_x - 80.0f * Settings.scale, upgradedPreview.current_y + 260.0f * Settings.scale, Color.GOLD);
        }

        if (header != null) {
            header.render(sb);
        }
    }

    public void adjustNormalUpgrade(int index, int delta) {
        if (sourceCard == null) return;
        Integer[] diffs = AbstractCardPatch.getCardNormalUpgrade(sourceCard);
        if (diffs == null || diffs.length != 5) {
            diffs = new Integer[]{0, 0, 0, 0, 0};
        } else {
            diffs = Arrays.copyOf(diffs, diffs.length);
        }
        diffs[index] = diffs[index] + delta;
        AbstractCardPatch.setCardNormalUpgrade(sourceCard, diffs);
        refreshPreview();
    }

    public void adjustAdditionalMagicUpgrade(String id, int delta) {
        if (sourceCard == null) return;
        Map<String, Integer> upgrades = AbstractCardPatch.getCardAdditionalMagicUpgrade(sourceCard);
        if (upgrades == null) {
            upgrades = new HashMap<>();
        } else {
            upgrades = new HashMap<>(upgrades);
        }
        upgrades.put(id, upgrades.getOrDefault(id, 0) + delta);
        AbstractCardPatch.setCardAdditionalMagicUpgrade(sourceCard, upgrades);
        refreshPreview();
    }

    public Integer[] getNormalUpgradeDiffs() {
        Integer[] diffs = AbstractCardPatch.getCardNormalUpgrade(sourceCard);
        if (diffs == null || diffs.length != 5) {
            return new Integer[]{0, 0, 0, 0, 0};
        }
        return diffs;
    }

    public Map<String, Integer> getAdditionalMagicUpgradeDiffs() {
        Map<String, Integer> upgrades = AbstractCardPatch.getCardAdditionalMagicUpgrade(sourceCard);
        if (upgrades == null) {
            return new HashMap<>();
        }
        return upgrades;
    }

    public AbstractCard getSourceCard() {
        return sourceCard;
    }

    protected void refreshPreview() {
        if (sourceCard == null) return;
        basePreview = sourceCard.makeStatEquivalentCopy();
        upgradedPreview = sourceCard.makeStatEquivalentCopy();

        copyUpgradeData(sourceCard, basePreview);
        copyUpgradeData(sourceCard, upgradedPreview);

        positionCard(basePreview, Settings.WIDTH / 2.0f - 320.0f * Settings.scale);
        positionCard(upgradedPreview, Settings.WIDTH / 2.0f + 320.0f * Settings.scale);

        upgradedPreview.timesUpgraded = sourceCard.timesUpgraded;
        upgradedPreview.upgrade();
    }

    private void positionCard(AbstractCard c, float centerX) {
        c.current_x = centerX;
        c.target_x = centerX;
        c.current_y = Settings.HEIGHT / 2.0f;
        c.target_y = Settings.HEIGHT / 2.0f;
        c.drawScale = 2.0f;
        c.targetDrawScale = 2.0f;
    }

    private void copyUpgradeData(AbstractCard from, AbstractCard to) {
        Integer[] diffs = AbstractCardPatch.getCardNormalUpgrade(from);
        if (diffs == null || diffs.length != 5) {
            diffs = new Integer[]{0, 0, 0, 0, 0};
        }
        AbstractCardPatch.setCardNormalUpgrade(to, Arrays.copyOf(diffs, diffs.length));

        Map<String, Integer> magicUpgrades = AbstractCardPatch.getCardAdditionalMagicUpgrade(from);
        if (magicUpgrades != null) {
            AbstractCardPatch.setCardAdditionalMagicUpgrade(to, new HashMap<>(magicUpgrades));
        }

        String serializedMagicNumbers = AbstractCardPatch.serializeAdditionalMagicNumbers(from);
        AbstractCardPatch.deserializeAdditionalMagicNumbers(to, serializedMagicNumbers);

        String[] additionalModifiers = AbstractCardPatch.getCardAdditionalModifiers(from);
        if (additionalModifiers != null) {
            AbstractCardPatch.setCardAdditionalModifiers(to, Arrays.copyOf(additionalModifiers, additionalModifiers.length));
        }
    }
}
