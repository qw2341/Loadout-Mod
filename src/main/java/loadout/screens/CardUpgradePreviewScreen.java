package loadout.screens;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;

import loadout.patches.AbstractCardPatch;

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

    protected GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(AbstractSelectScreen.gTEXT[0]);

    private static final int ARROW_W = 64;
    private float arrowScale1;
    private float arrowScale2;
    private float arrowScale3;
    private float arrowTimer;

    public CardUpgradePreviewScreen() {
        resetArrows();
    }

    public void open(SCardViewPopup parent, AbstractCard card) {
        this.parent = parent;
        this.sourceCard = card;
        if (this.header == null) {
            this.header = new CardUpgradePreviewHeader(this);
        }
        this.header.syncWithCard(card);
        refreshPreview();
        this.show = true;

        confirmButton.hideInstantly();
        confirmButton.isDisabled = false;
        confirmButton.show();
    }

    public void close() {
        this.show = false;
        InputHelper.justReleasedClickLeft = false;
        this.confirmButton.hide();
        this.confirmButton.isDisabled = true;
    }

    public void update() {
        if (!show) return;
        if (InputHelper.pressedEscape) {
            close();
            InputHelper.pressedEscape = false;
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

        confirmButton.update();
        if (confirmButton.hb.clicked) {
            close();
            confirmButton.hb.clicked = false;
        }
    }

    public void render(SpriteBatch sb) {
        if (!show) return;
        sb.setColor(overlayColor);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);
        sb.setColor(Color.WHITE);

        if (basePreview != null) {
            basePreview.render(sb);
//            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "Original", basePreview.current_x - 80.0f * Settings.scale, basePreview.current_y + 260.0f * Settings.scale, Color.GOLD);
        }
        if (upgradedPreview != null) {
            upgradedPreview.render(sb);
//            FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, "Upgraded", upgradedPreview.current_x - 80.0f * Settings.scale, upgradedPreview.current_y + 260.0f * Settings.scale, Color.GOLD);
        }
        renderArrows(sb);

        if (header != null) {
            header.render(sb);
        }

        confirmButton.render(sb);
    }

    private void resetArrows() {
        this.arrowScale1 = 1.0f;
        this.arrowScale2 = 1.0f;
        this.arrowScale3 = 1.0f;
        this.arrowTimer = 0.0F;
    }

    private void renderArrows(SpriteBatch sb) {
        float x = (float)Settings.WIDTH / 2.0F - 73.0F * Settings.scale - 32.0F;
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.UPGRADE_ARROW, x, (float)Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale1 * Settings.scale, this.arrowScale1 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        x += 64.0F * Settings.scale;
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.UPGRADE_ARROW, x, (float)Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale2 * Settings.scale, this.arrowScale2 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        x += 64.0F * Settings.scale;
        sb.draw(ImageMaster.UPGRADE_ARROW, x, (float)Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale3 * Settings.scale, this.arrowScale3 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        this.arrowTimer += Gdx.graphics.getDeltaTime() * 2.0F;
        this.arrowScale1 = 0.8F + (MathUtils.cos(this.arrowTimer) + 1.0F) / 8.0F;
        this.arrowScale2 = 0.8F + (MathUtils.cos(this.arrowTimer - 0.8F) + 1.0F) / 8.0F;
        this.arrowScale3 = 0.8F + (MathUtils.cos(this.arrowTimer - 1.6F) + 1.0F) / 8.0F;
    }

    public void setNormalUpgrade(int index, int newValue) {
        if (sourceCard == null) return;
        AbstractCardPatch.setCardNormalUpgrade(sourceCard, index, newValue);
        refreshPreview();
    }

    public void setAdditionalMagicUpgrade(String id, int newValue) {
        if (sourceCard == null) return;
        AbstractCardPatch.setCardAdditionalMagicUpgrade(sourceCard, id, newValue);
        refreshPreview();
    }

    public Integer[] getNormalUpgradeDiffs() {
        return AbstractCardPatch.getCardNormalUpgrade(sourceCard);
    }

    public int getAdditionalMagicUpgradeDiffs(String key) {
        return AbstractCardPatch.getCardAdditionalMagicUpgrade(sourceCard).getOrDefault(key, 0);
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

        positionCard(basePreview, Settings.WIDTH / 2.0f - 480.0f * Settings.scale);
        positionCard(upgradedPreview, Settings.WIDTH / 2.0f + 480.0f * Settings.scale);

        upgradedPreview.upgrade();
        upgradedPreview.displayUpgrades();
    }

    private void positionCard(AbstractCard c, float centerX) {
        c.current_x = centerX;
        c.target_x = centerX;
        c.current_y = Settings.HEIGHT / 2.0f;
        c.target_y = Settings.HEIGHT / 2.0f;
        c.drawScale = 1.5f;
        c.targetDrawScale = 1.5f;
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

        String[] additionalModifiers = AbstractCardPatch.getCardAdditionalModifiers(from);
        if (additionalModifiers != null) {
            AbstractCardPatch.setCardAdditionalModifiers(to, Arrays.copyOf(additionalModifiers, additionalModifiers.length));
        }
    }
}
