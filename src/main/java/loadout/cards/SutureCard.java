package loadout.cards;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomCard;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.stances.AbstractStance;
import loadout.LoadoutMod;
import loadout.savables.SerializableCard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Modified from REMEMod: https://github.com/REME-easy/REMEMod/blob/master/src/main/java/REMEMod/Cards/Colorless/Skill/SutureCard.java
 *
 */
public class SutureCard extends CustomCard implements CustomSavable<ArrayList<SerializableCard>> {

    public static final String ID = LoadoutMod.makeID("SutureCard");
    private static CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String NAME = cardStrings.NAME;

    public ArrayList<AbstractCard> sutureCards;
    private AbstractCard topCard;

    public SutureCard(ArrayList<AbstractCard> sutureCards) {
        this();
        suture(sutureCards);
    }

    public SutureCard() {
        super(ID, NAME, LoadoutMod.makeUIPath("missing_texture.png"), 0, " ",
                CardType.SKILL, CardColor.COLORLESS, CardRarity.SPECIAL, CardTarget.NONE);
        this.sutureCards = new ArrayList<>();
        this.topCard = new Madness();
    }

    public void suture(ArrayList<AbstractCard> sutureCards) {
        this.sutureCards = sutureCards;
        if(sutureCards.size() > 0){
            this.topCard = this.sutureCards.get(0);
            this.initializeSutureCard();
            this.getTopCardStat();
            this.cardsToPreview = this.topCard.makeCopy();
            this.rawDescription = this.getDescription();

            this.originalName = generateOriginalName();
            this.name = generateName();
        }
        this.initializeDescription();
    }

    private String generateOriginalName() {
        StringBuilder ret = new StringBuilder();

//        if(Settings.lineBreakViaCharacter) {
//            //CN stuff
//        } else {
//            //Eng stuff with space
//        }
        for(int i = 0; i < this.sutureCards.size(); ++i) {
            ret.append(this.sutureCards.get(i).originalName);
            if (i + 1 < this.sutureCards.size()) {
                ret.append("+");
            }
        }

        return ret.toString();
    }

    private String generateName() {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < this.sutureCards.size(); ++i) {
            ret.append(this.sutureCards.get(i).name);
            if (i + 1 < this.sutureCards.size()) {
                ret.append(" + ");
            }
        }
        return ret.toString();
    }

    private void getTopCardStat() {
        this.portrait = this.topCard.portrait;
        this.rarity = this.topCard.rarity;
        this.type = this.topCard.type;
        this.color = this.topCard.color;

        AbstractCard card;
        for(Iterator var1 = this.sutureCards.iterator(); var1.hasNext(); this.baseBlock += Integer.max(0, card.baseBlock)) {
            card = (AbstractCard)var1.next();
            this.cost += Integer.max(0, card.cost);
            this.baseDamage += Integer.max(0, card.baseDamage);
        }

        this.damage = this.baseDamage;
        this.block = this.baseBlock;
        this.costForTurn = this.cost;
    }

    private String getDescription() {
        StringBuilder des = new StringBuilder();
        int max = this.sutureCards.size();

        for(int i = 0; i < max; ++i) {
            des.append(this.sutureCards.get(i).rawDescription);
            if (i + 1 < max) {
                des.append(" NL ");
            }
        }

        return des.toString();
    }

    @Override
    public void initializeDescription() {
        this.description.clear();
        for(AbstractCard c:this.sutureCards) {
            c.initializeDescription();
            this.description.addAll(c.description);
        }
    }

    public void renderCardPreview(SpriteBatch sb) {
        if (AbstractDungeon.player != null && !AbstractDungeon.player.isDraggingCard && this.sutureCards.size() > 0) {
            int max = this.sutureCards.size();
            float tmpScale;
            if (max <= 2) {
                tmpScale = this.drawScale * 0.8F;
            } else {
                tmpScale = this.drawScale * Float.max(0.33F, 1.0F / (float)((max + 1) / 2));
            }

            for(int i = 0; i < max; ++i) {
                float w = (float)(i / 6) * IMG_WIDTH * tmpScale;
                if (i % 2 == 0) {
                    (this.sutureCards.get(i)).current_x = this.current_x - (IMG_WIDTH / 2.0F + IMG_WIDTH / 2.0F * tmpScale + 16.0F + w) * this.drawScale;
                } else {
                    (this.sutureCards.get(i)).current_x = this.current_x + (IMG_WIDTH / 2.0F + IMG_WIDTH / 2.0F * tmpScale + 16.0F + w) * this.drawScale;
                }

                (this.sutureCards.get(i)).current_y = this.current_y + (IMG_HEIGHT / 2.0F - IMG_HEIGHT / 2.0F * tmpScale - IMG_HEIGHT * tmpScale * (float)(i / 2 % 3)) * this.drawScale;
                (this.sutureCards.get(i)).drawScale = tmpScale;
                (this.sutureCards.get(i)).applyPowers();
//                (this.sutureCards.get(i)).initializeDescription();
                (this.sutureCards.get(i)).render(sb);
            }
        }

    }

    private void initializeSutureCard() {
        for(AbstractCard c:this.sutureCards) {
//            if (c.type == CardType.POWER)   this.exhaust = true;
            if (c.exhaust)                  this.exhaust = true;
            if (c.isEthereal)               this.isEthereal = true;
            if (c.isInnate)                 this.isInnate = true;
            if (c.returnToHand)             this.returnToHand = true;
            if (c.retain)                   this.retain = true;
            if (c.selfRetain)               this.selfRetain = true;
            if (c.shuffleBackIntoDrawPile)  this.shuffleBackIntoDrawPile = true;
        }
        for(AbstractCard c:this.sutureCards) {
            if(c.target == CardTarget.SELF_AND_ENEMY){
                this.target = CardTarget.SELF_AND_ENEMY;
                break;
            }
            if (c.target == CardTarget.ENEMY) {
                this.target = CardTarget.ENEMY;
                break;
            }
        }

        ReflectionHacks.setPrivate(this,AbstractCard.class,"renderColor", new Color(0.8F,0.9F,0.8F,1.0F));
    }

    public void use(AbstractPlayer p, AbstractMonster m) {
        if(p == null)
            p = AbstractDungeon.player;
        if(m == null)
            m = AbstractDungeon.getRandomMonster();
        if(sutureCards.size() > 0 && m != null && !m.isDeadOrEscaped() && !m.isDead)
            for(AbstractCard c:sutureCards) {
                c.use(p, m);
            }
    }

    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        boolean canuse = super.canUse(p, m);
        Iterator var4 = this.sutureCards.iterator();
        AbstractCard c;
        do {
            if (!var4.hasNext()) {
                return canuse;
            }
            c = (AbstractCard)var4.next();
        } while(!c.hasEnoughEnergy() || c.canUse(p, m));
        return false;
    }

    public void moveToDiscardPile() {
        super.moveToDiscardPile();
        if(sutureCards.size() > 0) {
            for (AbstractCard c : this.sutureCards) {
                c.moveToDiscardPile();
            }
            this.initializeDescription();
        }
    }

    public void triggerWhenDrawn() {
        super.triggerWhenDrawn();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerWhenDrawn();
            }
            this.initializeDescription();
        }
    }

    public void triggerOnEndOfPlayerTurn() {
        super.triggerOnEndOfPlayerTurn();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerOnEndOfPlayerTurn();
            }
            this.initializeDescription();
        }
    }

    public void triggerWhenCopied() {
        super.triggerWhenCopied();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerWhenCopied();
            }
            this.initializeDescription();
        }
    }

    public void triggerOnEndOfTurnForPlayingCard() {
        super.triggerOnEndOfTurnForPlayingCard();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                if(c.type != CardType.CURSE && c.type != CardType.STATUS){
                    c.triggerOnEndOfTurnForPlayingCard();
                }else{
                    c.dontTriggerOnUseCard = true;
                    c.use(AbstractDungeon.player,AbstractDungeon.getRandomMonster());
                    c.dontTriggerOnUseCard = false;
                }
            }
            this.initializeDescription();
        }
    }

    public void triggerOnOtherCardPlayed(AbstractCard c) {
        super.triggerOnOtherCardPlayed(c);
        if(sutureCards.size() > 0) {
            for(AbstractCard card:sutureCards) {
                card.triggerOnOtherCardPlayed(c);
            }
            this.initializeDescription();
        }
    }

    public void triggerOnGainEnergy(int e, boolean dueToCard) {
        super.triggerOnGainEnergy(e, dueToCard);
        if(sutureCards.size() > 0) {
            for(AbstractCard card:sutureCards) {
                card.triggerOnGainEnergy(e, dueToCard);
            }
            this.initializeDescription();
        }
    }

    public void triggerOnCardPlayed(AbstractCard cardPlayed) {
        super.triggerOnCardPlayed(cardPlayed);
        if(sutureCards.size() > 0) {
            for(AbstractCard card:sutureCards) {
                card.triggerOnCardPlayed(cardPlayed);
            }
            this.initializeDescription();
        }
    }

    public void triggerOnScry() {
        super.triggerOnScry();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerOnScry();
            }
            this.initializeDescription();
        }
    }

    public void triggerExhaustedCardsOnStanceChange(AbstractStance newStance) {
        super.triggerExhaustedCardsOnStanceChange(newStance);

        if(sutureCards.size() > 0) {
            for(AbstractCard c:sutureCards) {
                c.triggerExhaustedCardsOnStanceChange(newStance);
            }
            this.initializeDescription();
        }
    }

    public void triggerAtStartOfTurn() {
        super.triggerAtStartOfTurn();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerAtStartOfTurn();
            }
            this.initializeDescription();
        }
    }

    public void onPlayCard(AbstractCard c, AbstractMonster m) {
        super.onPlayCard(c, m);

        if(sutureCards.size() > 0) {
            for(AbstractCard card:sutureCards) {
                card.onPlayCard(c, m);
            }
            this.initializeDescription();
        }
    }

    public void atTurnStart() {
        super.atTurnStart();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.atTurnStart();
            }
            this.initializeDescription();
        }
    }

    public void atTurnStartPreDraw() {
        super.atTurnStartPreDraw();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.atTurnStartPreDraw();
            }
            this.initializeDescription();
        }
    }

    public void onChoseThisOption() {
        super.onChoseThisOption();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.onChoseThisOption();
            }
            this.initializeDescription();
        }
    }

    public void triggerOnExhaust() {
        super.triggerOnExhaust();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerOnExhaust();
            }
            this.initializeDescription();
        }
    }

    public void calculateCardDamage(AbstractMonster mo) {
        super.calculateCardDamage(mo);

        if(sutureCards.size() > 0) {
            for(AbstractCard c:sutureCards) {
                c.calculateCardDamage(mo);
            }
            this.initializeDescription();
        }
    }

    public void onRetained() {
        super.onRetained();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.onRetained();
            }
            this.initializeDescription();
        }
    }

    public void triggerOnManualDiscard() {
        super.triggerOnManualDiscard();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.triggerOnManualDiscard();
            }
            this.initializeDescription();
        }
    }

    public void tookDamage() {
        super.tookDamage();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.tookDamage();
            }
            this.initializeDescription();
        }
    }

    public void didDiscard() {
        super.didDiscard();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.didDiscard();
            }
            this.initializeDescription();
        }
    }

    public void switchedStance() {
        super.switchedStance();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.switchedStance();
            }
            this.initializeDescription();
        }
    }

    public void applyPowers() {
        super.applyPowers();
        if(sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.applyPowers();
            }
            this.initializeDescription();
        }
    }

    public void triggerOnGlowCheck() {
        super.triggerOnGlowCheck();
        if(sutureCards.size() > 0)
            for(AbstractCard c:this.sutureCards) {

                c.triggerOnGlowCheck();
                if (c.glowColor != AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy()) {
                    this.glowColor = c.glowColor;
                    break;
                }
            }

    }

    public AbstractCard makeCopy() {
        return new SutureCard(this.sutureCards.stream().map(AbstractCard::makeStatEquivalentCopy).collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public void upgrade() {
        if (sutureCards.size() > 0) {
            for(AbstractCard c:this.sutureCards) {
                c.upgrade();
            }

            this.upgradeName();
            this.initializeDescription();
        }

    }

    @Override
    protected void upgradeName() {
        this.name = generateName();
        this.initializeTitle();
    }

    @Override
    public ArrayList<SerializableCard> onSave() {
        return this.sutureCards.stream().map(SerializableCard::toSerializableCard).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void onLoad(ArrayList<SerializableCard> sav) {
        suture(sav.stream().map(SerializableCard::toAbstractCard).collect(Collectors.toCollection(ArrayList::new)));
    }
}
