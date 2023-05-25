package loadout.relics;

import basemod.abstracts.CustomSavable;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import loadout.LoadoutMod;
import loadout.cardmods.InfiniteUpgradeMod;
import loadout.patches.AbstractCardPatch;
import loadout.patches.InfUpgradePatch;
import loadout.savables.CardModifications;
import loadout.screens.GCardSelectScreen;
import loadout.util.TextureLoader;

import java.util.ArrayList;

import static loadout.LoadoutMod.*;

public class CardModifier extends AbstractCardScreenRelic implements CustomSavable<Object[][]> {

    public static final String ID = LoadoutMod.makeID("CardModifier");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("modifier_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("modifier_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("modifier_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("modifier_relic.png"));


    public CardModifier() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK, GCardSelectScreen.CardDisplayMode.UPGRADE);
    }

    @Override
    protected void openSelectScreen() {
        if (AbstractDungeon.player.masterDeck != null) {
            if (selectScreen != null)
                selectScreen.open(AbstractDungeon.player.masterDeck, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);
        } else setIsSelectionScreenUp(false);
    }

    @Override
    protected void doneSelectionLogics() {
        int count = selectScreen.selectedCards.size();
        float min = -15.0F * count;
        for (int i = 0; i < count; i++) {
            AbstractCard card = selectScreen.selectedCards.get(i);
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                upgradeCard(card);
            } else
                upgradeCard(card,Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0f, Settings.HEIGHT / 2.0F);
        }
    }


    public void upgradeCard(AbstractCard card, float x, float y) {
        card.upgrade();

        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            card.superFlash();
        } else {
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
    public Object[][] onSave() {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            ArrayList<Object[]> ret = new ArrayList<>();

            for (int i = 0; i<len; i++) {
                AbstractCard card = AbstractDungeon.player.masterDeck.group.get(i);
                if (AbstractCardPatch.isCardModified(card)) {
                    Object[] cardStat = new Object[13];
                    cardStat[0] = i;
                    cardStat[1] = card.cost;
                    cardStat[2] = card.baseDamage;
                    cardStat[3] = card.baseBlock;
                    cardStat[4] = card.baseMagicNumber;
                    cardStat[5] = card.baseHeal;
                    cardStat[6] = card.baseDraw;
                    cardStat[7] = card.baseDiscard;
                    cardStat[8] = card.color.toString();
                    cardStat[9] = card.type.toString();
                    cardStat[10] = card.rarity.toString();
                    cardStat[11] = card.misc;
                    cardStat[12] = card.timesUpgraded;
                    ret.add(cardStat);
                }

            }

            return ret.toArray(new Object[0][]);
        }


        return null;
    }

    @Override
    public void onLoad(Object[][] ret) {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            if (ret != null && ret.length <= len && ret.length > 0 && ret[0].length == 13) {
                for (int i = 0; i<ret.length; i++) {
                    AbstractCard card = AbstractDungeon.player.masterDeck.group.get((int)(double)ret[i][0]);
                    for (int j = card.timesUpgraded; j < (int)(double)ret[i][12]; j++) {card.upgrade();}
                    card.cost = (int)(double)ret[i][1];
                    card.costForTurn = card.cost;
                    card.baseDamage = (int)(double)ret[i][2];
                    card.baseBlock = (int)(double)ret[i][3];
                    card.baseMagicNumber = (int)(double)ret[i][4];
                    card.magicNumber = card.baseMagicNumber;
                    card.baseHeal = (int)(double)ret[i][5];
                    card.baseDraw = (int)(double)ret[i][6];
                    card.baseDiscard = (int)(double)ret[i][7];
                    try {
                        card.color = AbstractCard.CardColor.valueOf((String) ret[i][8]);
                    } catch (Exception e) {
                        try {
                            card.color = AbstractCard.CardColor.values()[(int)(double)ret[i][8]];
                        } catch (Exception ignore) {

                        }

                    }

                    try {
                        card.type = AbstractCard.CardType.valueOf((String) ret[i][9]);
                    } catch (Exception e) {
                        try {
                            card.type = AbstractCard.CardType.values()[(int)(double)ret[i][9]];
                        } catch (Exception ignore) {

                        }

                    }

                    try {
                        card.rarity = AbstractCard.CardRarity.valueOf((String) ret[i][10]);
                    } catch (Exception e) {
                        try {
                            card.rarity = AbstractCard.CardRarity.values()[(int)(double)ret[i][10]];
                        } catch (Exception ignore) {

                        }
                    }

                    card.misc = (int)(double)ret[i][11];
                    AbstractCardPatch.setCardModified(card,true);
                    if(CardModifierManager.hasModifier(card, InfiniteUpgradeMod.ID)) {
                        card.upgraded = false;
                        card.timesUpgraded = (int)(double)ret[i][12];
                        InfUpgradePatch.changeCardName(card);
                    }
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

    @Override
    public void onPreviewObtainCard(AbstractCard c) {
        onObtainCard(c);
    }

    @Override
    public void onObtainCard(AbstractCard c) {
        if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(c.cardID)) {
            try {
                CardModifications.modifyOnlyNumberIfExist(c);
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to modify: " + c.cardID + " when obtaining");
            }
        }
    }

}
