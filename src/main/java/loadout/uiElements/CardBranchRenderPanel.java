package loadout.uiElements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.BranchingUpgradesCard;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import loadout.relics.CardModifier;
import rs.lazymankits.interfaces.cards.BranchableUpgradeCard;
import rs.lazymankits.interfaces.cards.UpgradeBranch;

import java.util.ArrayList;
import java.util.List;

public class CardBranchRenderPanel implements UIElement{
    private final Hitbox prevHb;
    private final Hitbox nextHb;

    public AbstractCard card;
    public ArrayList<AbstractCard> branchUpgrades;
    public int currentBranch;
    public float x;
    public float y;
    public boolean shown;


    public CardBranchRenderPanel() {
        branchUpgrades = new ArrayList<>();
        currentBranch = 0;
        shown = false;
        x = 0;
        y = 0;

        this.prevHb = new Hitbox(160.0F * Settings.scale, 160.0F * Settings.scale);
        this.nextHb = new Hitbox(160.0F * Settings.scale, 160.0F * Settings.scale);

    }

    public void show(AbstractCard card) {
        this.shown = true;
        this.card = card;
        if(!this.card.canUpgrade()) {
            hide();
            return;
        }


        this.prevHb.move(card.target_x - card.hb.width * 0.7f, card.target_y );
        this.nextHb.move(card.target_x + card.hb.width * 0.7f, card.target_y );

        this.currentBranch = 0;

        this.branchUpgrades.clear();

        if(card instanceof BranchingUpgradesCard) {
            BranchingUpgradesCard bCard = (BranchingUpgradesCard) card.makeStatEquivalentCopy();
            bCard.doNormalUpgrade();
            this.branchUpgrades.add((AbstractCard) bCard);
            BranchingUpgradesCard bCard1 = (BranchingUpgradesCard) card.makeStatEquivalentCopy();
            bCard1.doBranchUpgrade();
            this.branchUpgrades.add((AbstractCard) bCard1);
        } else if (Loader.isModLoaded("LazyManKits")) {
            if(card instanceof BranchableUpgradeCard) {
                BranchableUpgradeCard bCard = (BranchableUpgradeCard) card;
                List<UpgradeBranch> branches = bCard.getPossibleBranches();
                for (int i = 0; i<branches.size(); i++) {
                    BranchableUpgradeCard b = ((BranchableUpgradeCard)card.makeStatEquivalentCopy());
                    b.setChosenBranch(i);
                    b.upgradeCalledOnSL();
                    this.branchUpgrades.add((AbstractCard) b);
                }
            }
        }
        for (AbstractCard c : branchUpgrades) {
            c.current_x = this.card.current_x - 200.0f * Settings.scale;
            c.current_y = this.card.current_y;
        }
        if(branchUpgrades.size() == 0) hide();
    }

    public void hide() {
        this.shown = false;
    }

    @Override
    public void render(SpriteBatch sb) {
        if (!shown || branchUpgrades.isEmpty()) return;

        AbstractCard curr = this.branchUpgrades.get(currentBranch);
        curr.renderInLibrary(sb);
        curr.renderCardTip(sb);

        this.nextHb.render(sb);
        this.prevHb.render(sb);
        renderArrows(sb);

    }

    @Override
    public void update() {
        if(!shown || branchUpgrades.isEmpty()) {
            shown = false;
            return;
        }

        if (this.currentBranch < this.branchUpgrades.size() - 1) this.nextHb.update();
        if (this.currentBranch > 0) this.prevHb.update();


        AbstractCard curr = this.branchUpgrades.get(currentBranch);
        curr.targetDrawScale = 1.25f;
        curr.target_x = this.card.target_x;
        curr.target_y = this.card.target_y;
        curr.hb.update();
        curr.update();

        if (InputHelper.justReleasedClickLeft) {
            if (nextHb.hovered && this.currentBranch < this.branchUpgrades.size() - 1) {
                curr.current_x = this.card.current_x + 250.0f * Settings.scale;
                curr.drawScale = 0.5f;
                this.currentBranch ++;
            } else if (prevHb.hovered && this.currentBranch > 0) {
                curr.current_x = this.card.current_x - 250.0f * Settings.scale;
                curr.drawScale = 0.5f;
                this.currentBranch --;
            } else if (curr.hb.hovered) {
                this.hide();
                CardModifier.branchUpgradeCard(card, currentBranch, true);
            } else {
                this.hide();
            }
        }

    }

    private void renderArrows(SpriteBatch sb) {
        if (this.currentBranch > 0) {
            sb.draw(ImageMaster.POPUP_ARROW, this.prevHb.cX - 128.0F, this.prevHb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
            if (Settings.isControllerMode) {
                sb.draw(CInputActionSet.pageLeftViewDeck.getKeyImg(), this.prevHb.cX - 32.0F + 0.0F * Settings.scale, this.prevHb.cY - 32.0F + 100.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
            }

            if (this.prevHb.hovered) {
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.5F));
                sb.draw(ImageMaster.POPUP_ARROW, this.prevHb.cX - 128.0F, this.prevHb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
                sb.setColor(Color.WHITE);
                sb.setBlendFunction(770, 771);
            }
        }

        if (this.currentBranch < this.branchUpgrades.size() - 1) {
            sb.draw(ImageMaster.POPUP_ARROW, this.nextHb.cX - 128.0F, this.nextHb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, true, false);
            if (Settings.isControllerMode) {
                sb.draw(CInputActionSet.pageRightViewExhaust.getKeyImg(), this.nextHb.cX - 32.0F + 0.0F * Settings.scale, this.nextHb.cY - 32.0F + 100.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
            }

            if (this.nextHb.hovered) {
                sb.setBlendFunction(770, 1);
                sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.5F));
                sb.draw(ImageMaster.POPUP_ARROW, this.nextHb.cX - 128.0F, this.nextHb.cY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, true, false);
                sb.setColor(Color.WHITE);
                sb.setBlendFunction(770, 771);
            }
        }

    }
}
