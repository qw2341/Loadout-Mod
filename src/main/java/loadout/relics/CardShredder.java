package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.*;
import loadout.LoadoutMod;
import loadout.screens.GCardSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;
import static loadout.relics.LoadoutBag.landingSfx;

public class CardShredder extends CustomRelic implements ClickableRelic {

    public static final String ID = LoadoutMod.makeID("CardShredder");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("shredder_relic_alt.png")): TextureLoader.getTexture(makeRelicPath("shredder_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("shredder_relic_alt.png")): TextureLoader.getTexture(makeRelicOutlinePath("shredder_relic.png"));

    public static boolean isSelectionScreenUp = false;
    public GCardSelectScreen cardSelectScreen ;



    public CardShredder() {
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

        this.cardSelectScreen = new GCardSelectScreen(GCardSelectScreen.CardDisplayMode.DELETE, this);
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

        if (LoadoutBag.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardModifier.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || BottledMonster.isSelectionScreenUp || OrbBox.isSelectionScreenUp)
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
        if (AbstractDungeon.player.masterDeck != null) {
            isSelectionScreenUp = true;
            if (cardSelectScreen != null) {
                cardSelectScreen.open(AbstractDungeon.player.masterDeck, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);

            }

        }

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
                float min = -15.0F * count;
                for (int i = 0; i < count; i++) {
                    AbstractCard card = cardSelectScreen.selectedCards.get(i);
                    card.stopGlowing();
                    if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        //AbstractDungeon.effectList.add(new CardDisappearEffect(card, Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        removeCard(card);
                    } else {
                        //AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card, Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                        removeCard(card);
                    }


                }
                cardSelectScreen.doneSelecting = false;
            }
        }
    }

    public void removeCard(AbstractCard card) {
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            removeFromPlayerInCombat(card);
            AbstractDungeon.effectList.add(new ExhaustCardEffect(card));
        } else {
            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(card, Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
            AbstractDungeon.player.masterDeck.group.remove(card);
        }
        this.flash();
    }

    public void removeFromPlayerInCombat(AbstractCard card) {
        switch (this.cardSelectScreen.currentPool) {

            case EXTERNAL:
                break;
            case MASTER_DECK:
                AbstractDungeon.player.masterDeck.group.remove(card);
                break;
            case DRAW:
                AbstractDungeon.player.drawPile.removeCard(card);
                break;
            case DISCARD:
                AbstractDungeon.player.discardPile.removeCard(card);
                break;
            case HAND:
                AbstractDungeon.player.hand.removeCard(card);
                AbstractDungeon.player.hand.refreshHandLayout();
                break;
            case EXHAUST:
                AbstractDungeon.player.exhaustPile.removeCard(card);
                break;
        }


    }


    @Override
    public AbstractRelic makeCopy() {
        return new CardShredder();
    }
}
