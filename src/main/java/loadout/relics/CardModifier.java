package loadout.relics;

import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.CardModifierPatches;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.actions.common.UpgradeSpecificCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.CardDisappearEffect;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;
import loadout.screens.GCardSelectScreen;
import loadout.util.TextureLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Objects;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;
import static loadout.relics.LoadoutBag.landingSfx;

public class CardModifier extends CustomRelic implements ClickableRelic, CustomSavable<Integer[][]>{

    public static final String ID = LoadoutMod.makeID("CardModifier");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("modifier_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("modifier_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("modifier_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("modifier_relic.png"));

    public static boolean isSelectionScreenUp = false;
    public GCardSelectScreen cardSelectScreen ;



    public CardModifier() {
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

        this.cardSelectScreen = new GCardSelectScreen(GCardSelectScreen.CardDisplayMode.UPGRADE, this);
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

        if (LoadoutBag.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || BottledMonster.isSelectionScreenUp)
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
            if (cardSelectScreen != null)
                cardSelectScreen.open(AbstractDungeon.player.masterDeck, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);

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
                    if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        upgradeCard(card);
                    } else
                        upgradeCard(card,Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0f, Settings.HEIGHT / 2.0F);
                }

                cardSelectScreen.doneSelecting = false;
            }
        }
    }

    public void upgradeCard(AbstractCard card, float x, float y) {
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            card.upgrade();
            card.superFlash();
        } else {
            card.upgrade();
            AbstractDungeon.topLevelEffects.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy(), x, y));
            AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(x, y));
            CardCrawlGame.sound.play("CARD_UPGRADE");
        }

        this.flash();
    }

    public void upgradeCard(AbstractCard card) {
        this.upgradeCard(card,Settings.WIDTH / 2.0F,Settings.HEIGHT / 2.0F);
    }


    @Override
    public AbstractRelic makeCopy() {
        return new CardModifier();
    }


    @Override
    public Integer[][] onSave() {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            ArrayList<Integer[]> ret = new ArrayList<>();

            for (int i = 0; i<len; i++) {
                AbstractCard card = AbstractDungeon.player.masterDeck.group.get(i);
                if (AbstractCardPatch.isCardModified(card)) {
                    Integer[] cardStat = new Integer[11];
                    cardStat[0] = i;
                    cardStat[1] = card.cost;
                    cardStat[2] = card.baseDamage;
                    cardStat[3] = card.baseBlock;
                    cardStat[4] = card.baseMagicNumber;
                    cardStat[5] = card.baseHeal;
                    cardStat[6] = card.baseDraw;
                    cardStat[7] = card.baseDiscard;
                    cardStat[8] = card.color.ordinal();
                    cardStat[9] = card.type.ordinal();
                    cardStat[10] = card.rarity.ordinal();
                    ret.add(cardStat);
                }

            }

            return ret.toArray(new Integer[0][]);
        }


        return null;
    }

    @Override
    public void onLoad(Integer[][] ret) {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            if (ret != null && ret.length <= len && ret.length > 0 && ret[0].length == 11) {
                for (int i = 0; i<ret.length; i++) {
                    AbstractCard card = AbstractDungeon.player.masterDeck.group.get(ret[i][0]);
                    card.cost = ret[i][1];
                    card.costForTurn = card.cost;
                    card.baseDamage = ret[i][2];
                    card.baseBlock = ret[i][3];
                    card.baseMagicNumber = ret[i][4];
                    card.baseHeal = ret[i][5];
                    card.baseDraw = ret[i][6];
                    card.baseDiscard = ret[i][7];
                    card.color = AbstractCard.CardColor.values()[ret[i][8]];
                    card.type = AbstractCard.CardType.values()[ret[i][9]];
                    card.rarity = AbstractCard.CardRarity.values()[ret[i][10]];
                    AbstractCardPatch.setCardModified(card,true);
                }
            }
        }

    }

    public static AbstractCard getUnmodifiedCopyCard(String id) {
        try {
            return CardLibrary.getCard(id).getClass().getDeclaredConstructor(new Class[0]).newInstance();
        } catch (Exception e) {
            logger.info("Error obtaining a new copy for card: " + id);
        }
        return null;
    }
}
