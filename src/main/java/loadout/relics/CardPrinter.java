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

public class CardPrinter extends AbstractCardScreenRelic {

    public static final String ID = LoadoutMod.makeID("CardPrinter");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("printer_relic_alt.png")): TextureLoader.getTexture(makeRelicPath("printer_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("printer_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("printer_relic.png"));
    private static final Texture IMG_XGGG_ALT = TextureLoader.getTexture(makeRelicPath("chest_relic_xggg.png"));


    public CardPrinter() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK, GCardSelectScreen.CardDisplayMode.OBTAIN);
        if (LoadoutMod.isXggg()) {
            this.img = IMG_XGGG_ALT;
        }
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
            for (int i = 0; i < count; i++) {
                AbstractCard card = selectScreen.selectedCards.get(i);
                if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
                    AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
            }
            this.flash();
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
