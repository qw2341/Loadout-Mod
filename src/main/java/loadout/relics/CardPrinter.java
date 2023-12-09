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

import java.util.ArrayList;


public class CardPrinter extends AbstractCardScreenRelic {

    public static final String ID = LoadoutMod.makeID("CardPrinter");
    private static Texture IMG = null;
    private static Texture OUTLINE = null;

    public static final ArrayList<AbstractCard> lastCards = new ArrayList<>();

    public CardPrinter() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK, GCardSelectScreen.CardDisplayMode.OBTAIN);
    }

    @Override
    protected void openSelectScreen() {
        CardGroup cards = new CardGroup(CardGroup.CardGroupType.CARD_POOL);
        cards.group = LoadoutMod.cardsToDisplay;
        if (selectScreen != null)
            selectScreen.open(cards, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);
    }

    @Override
    protected void doneSelectionLogics() {
        int count = selectScreen.selectedCards.size();
        if (count > 0) {
            float min = -15.0F * count;
            lastCards.clear();
            for (int i = 0; i < count; i++) {
                AbstractCard card = selectScreen.selectedCards.get(i);
                card = card.makeStatEquivalentCopy();
                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
                    AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(card.makeSameInstanceOf(), Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card, Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                lastCards.add(card.makeStatEquivalentCopy());
            }
            this.flash();
        }
    }

    public void obtainCard(AbstractCard card) {
        card = card.makeStatEquivalentCopy();
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
            AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(card.makeSameInstanceOf(), Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card, Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
        this.flash();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new CardPrinter();
    }

    @Override
    public void onCtrlRightClick() {
        if(!lastCards.isEmpty()) {
            lastCards.forEach(this::obtainCard);
        }
    }
}
