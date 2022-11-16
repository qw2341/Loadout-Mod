package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import loadout.LoadoutMod;
import loadout.screens.GCardSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;
import static loadout.relics.LoadoutBag.landingSfx;

public class CardPrinter extends CustomRelic implements ClickableRelic {

    public static final String ID = LoadoutMod.makeID("CardPrinter");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("printer_relic_alt.png")): TextureLoader.getTexture(makeRelicPath("printer_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("printer_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("printer_relic.png"));

    public static boolean isSelectionScreenUp = false;
    public loadout.screens.GCardSelectScreen cardSelectScreen ;



    public CardPrinter() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK);
        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.cardSelectScreen = new loadout.screens.GCardSelectScreen(GCardSelectScreen.CardDisplayMode.OBTAIN, this);
    }

    @Override
    public void playLandingSFX() {
        if (isIsaacMode) {
            if (CardCrawlGame.MUTE_IF_BG && Settings.isBackgrounded) {
                return;
            } else if (landingSfx != null) {
                landingSfx.play(Settings.SOUND_VOLUME * Settings.MASTER_VOLUME);
            } else {
                logger.info("Missing landing sound!");
            }
        } else {
            CardCrawlGame.sound.play("RELIC_DROP_CLINK");
        }
    }

    @Override
    public String[] CLICKABLE_DESCRIPTIONS() {
        return ClickableRelic.super.CLICKABLE_DESCRIPTIONS();
    }

    @Override
    public void relicTip() {

    }

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }

        if (LoadoutBag.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || BottledMonster.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(cardSelectScreen!=null) {
                isSelectionScreenUp = false;
                cardSelectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openCardSelect();
    }
    private void openCardSelect() {
        CardGroup cards = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        cards.group = LoadoutMod.cardsToDisplay;
        isSelectionScreenUp = true;
        if (cardSelectScreen != null)
            cardSelectScreen.open(cards, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);
    }

    @Override
    public void onUnequip() {
        if(isSelectionScreenUp) {

            if(cardSelectScreen!=null) {
                isSelectionScreenUp = false;
                cardSelectScreen.close();
            }
        }
    }
    @Override
    public boolean hovered() {
        return ClickableRelic.super.hovered();
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void renderInTopPanel(SpriteBatch sb) {
        super.renderInTopPanel(sb);
        if(isSelectionScreenUp)
            cardSelectScreen.render(sb);
    }

    @Override
    public void update() {
        super.update();
        if(isSelectionScreenUp)
            cardSelectScreen.update();
        if (cardSelectScreen != null) {
            if (cardSelectScreen.doneSelecting) {
                int count = cardSelectScreen.selectedCards.size();
                if (count > 0) {
                    float min = -15.0F * count;
                    for (int i = 0; i < count; i++) {
                        AbstractCard card = cardSelectScreen.selectedCards.get(i);
                        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
                            AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                    }
                    this.flash();
                }

                cardSelectScreen.doneSelecting = false;
            }
        }
    }

    public void obtainCard(AbstractCard card) {
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
            AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        this.flash();
    }



    @Override
    public AbstractRelic makeCopy() {
        return new CardPrinter();
    }
}
