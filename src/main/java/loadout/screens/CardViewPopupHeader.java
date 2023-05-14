package loadout.screens;


import basemod.BaseMod;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.compendium.CardLibSortHeader;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import loadout.LoadoutMod;
import loadout.cardmods.*;
import loadout.helper.RelicClassComparator;
import loadout.patches.AbstractCardPatch;
import loadout.savables.CardModifications;
import loadout.savables.SerializableCard;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static loadout.LoadoutMod.*;

public class CardViewPopupHeader implements HeaderButtonPlusListener, DropdownMenuListener, CardEffectButton.CardStuffProvider {


    public static final String[] clTEXT = CardLibSortHeader.TEXT;


    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardViewPopupHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    public static final String TEXT_BLOCK = GameDictionary.BLOCK.NAMES[0];
    public static final String TEXT_DISCARD = CardCrawlGame.languagePack.getUIString("DiscardAction").TEXT[0];

    public boolean justSorted = false;

    public float startX = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 150.0F * Settings.yScale;
    public static final float SPACE_Y = 45.0F * Settings.yScale;

    private final HeaderButtonPlus makeXCostButton;

    private final HeaderButtonPlus makeUnplayableButton;

    private final HeaderButtonPlus makeEtherealButton;
    private final HeaderButtonPlus makeExhaustButton;
    private final HeaderButtonPlus makeInnateButton;
    private final HeaderButtonPlus makeRetainButton;
    private final HeaderButtonPlus makeAutoPlayButton;
    private final HeaderButtonPlus makeSoulBoundButton;
    private final HeaderButtonPlus makeFleetingButton;
    private final HeaderButtonPlus makeGraveButton;

    private final HeaderButtonPlus makeGainGoldOnKillButton;
    private final HeaderButtonPlus makeGainHPOnKillButton;

    private final HeaderButtonPlus makeGainGoldOnPlayButton;
    private final HeaderButtonPlus makeHealOnPlayButton;

    private final HeaderButtonPlus randomUpgradeOnKillButton;
    private final HeaderButtonPlus makeGainDamageOnKillButton;
    private final HeaderButtonPlus makeGainMagicOnKillButton;
    private final HeaderButtonPlus makeLifestealButton;

    private final HeaderButtonPlus makeInevitableButton;

    private final HeaderButtonPlus makeInfUpgradeButton;

    private final CardEffectButton costButton;
    private final CardEffectButton damageButton;

    private final CardEffectButton blockButton;


    private final CardEffectButton magicNumberButton;


    private final CardEffectButton healButton;


    private final CardEffectButton drawButton;


    private final CardEffectButton discardButton;

    private final CardEffectButton miscButton;

    private final DropdownMenu rarityButton;
    private final DropdownMenu classButton;

    private final DropdownMenu typeButton;


    private final HeaderButtonPlus restoreDefaultButton;
    private final HeaderButtonPlus saveChangesButton;
    private final HeaderButtonPlus getCopyButton;

    private String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public CardEffectButton[] cardEffectButtons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    public int multiplier = 1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public SCardViewPopup cardViewScreen;




    public CardViewPopupHeader(SCardViewPopup sCardViewPopup, float startX) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        this.cardViewScreen = sCardViewPopup;
        this.startX = startX;
        float xPosition = this.startX;
        float yPosition = START_Y - 260.0f * Settings.scale;

        this.costButton = new CardEffectButton(null, xPosition, yPosition, clTEXT[3], new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().cost;
            }

            @Override
            public void setAmount(int amountToSet) {
                if(amountToSet >= 0) {
                    AbstractCard card = getCard();
                    card.cost = amountToSet;
                    card.costForTurn = amountToSet;
                    setCardModded(true);
                }
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.damageButton = new CardEffectButton(null, xPosition, yPosition, TEXT[0], new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseDamage;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().baseDamage = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.blockButton = new CardEffectButton(null, xPosition, yPosition, StringUtils.capitalize(TEXT_BLOCK), new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseBlock;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().baseBlock = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.magicNumberButton = new CardEffectButton(null, xPosition, yPosition, TEXT[1], new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseMagicNumber;
            }

            @Override
            public void setAmount(int amountToSet) {
                AbstractCard card = getCard();
                int diff = card.magicNumber - card.baseMagicNumber;
                card.baseMagicNumber = amountToSet;
                card.magicNumber = amountToSet + diff;

                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;


        this.healButton = new CardEffectButton(null, xPosition, yPosition, TEXT[2], new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseHeal;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().baseHeal = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.drawButton = new CardEffectButton(null, xPosition, yPosition, TEXT[3], new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseDraw;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().baseDraw = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.discardButton = new CardEffectButton(null, xPosition, yPosition, TEXT_DISCARD, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().baseDiscard;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().baseDiscard = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.miscButton = new CardEffectButton(null, xPosition, yPosition, "Misc", new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return getCard().misc;
            }

            @Override
            public void setAmount(int amountToSet) {
                getCard().misc = amountToSet;
                setCardModded(true);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }, this);
        yPosition -= SPACE_Y;

        this.restoreDefaultButton = new HeaderButtonPlus(TEXT[4], xPosition, yPosition, this, true, ImageMaster.MAP_NODE_REST);
        yPosition -= SPACE_Y;
        this.saveChangesButton = new HeaderButtonPlus(TEXT[9], xPosition, yPosition, this, true, ImageMaster.SETTINGS_ICON);
        yPosition -= SPACE_Y;
        this.getCopyButton = new HeaderButtonPlus(TEXT[10], xPosition, yPosition, this, true, ImageMaster.PROFILE_B);


        yPosition = START_Y;
        xPosition = Settings.WIDTH - 2 * xPosition;

        //this.makeUnplayableButton = new HeaderButtonPlus(TEXT[5], xPosition, yPosition, this, true, ImageMaster.loadImage("images/blights/muzzle.png"));
        this.makeUnplayableButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]), xPosition, yPosition, this, false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeExhaustButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition -= SPACE_X;
        yPosition -= SPACE_Y;

        this.makeEtherealButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.ETHEREAL.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeInnateButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.INNATE.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition -= SPACE_X;
        yPosition -= SPACE_Y;

        this.makeXCostButton = new HeaderButtonPlus("X " + clTEXT[3],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeRetainButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.RETAIN.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition -= SPACE_X;
        yPosition -= SPACE_Y;

        this.makeGraveButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("grave"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeAutoPlayButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("autoplay"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.makeSoulBoundButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("soulbound"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeFleetingButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("fleeting"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.makeGainGoldOnKillButton = new HeaderButtonPlus(TEXT[11],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeGainHPOnKillButton = new HeaderButtonPlus(TEXT[12],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.makeGainGoldOnPlayButton = new HeaderButtonPlus(TEXT[13],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeHealOnPlayButton = new HeaderButtonPlus(TEXT[14],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.randomUpgradeOnKillButton = new HeaderButtonPlus(TEXT[15],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeGainDamageOnKillButton = new HeaderButtonPlus(TEXT[16],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.makeGainMagicOnKillButton = new HeaderButtonPlus(TEXT[17],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeLifestealButton = new HeaderButtonPlus(TEXT[19],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.makeInevitableButton = new HeaderButtonPlus(TEXT[20],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        xPosition += SPACE_X;
        this.makeInfUpgradeButton = new HeaderButtonPlus(TEXT[22],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        xPosition -= SPACE_X;

        this.buttons = new HeaderButtonPlus[] { this.restoreDefaultButton,
        this.saveChangesButton, this.getCopyButton, this.makeUnplayableButton, this.makeExhaustButton, this.makeEtherealButton, this.makeInnateButton, this.makeRetainButton, this.makeXCostButton, this.makeAutoPlayButton, this.makeSoulBoundButton, this.makeFleetingButton, this.makeGraveButton, this.makeGainGoldOnKillButton, this.makeGainHPOnKillButton, this.makeGainGoldOnPlayButton,
        this.makeHealOnPlayButton, this.randomUpgradeOnKillButton, this.makeGainDamageOnKillButton, this.makeGainMagicOnKillButton, this.makeLifestealButton, this.makeInevitableButton, this.makeInfUpgradeButton};

        this.cardEffectButtons = new CardEffectButton[] {this.costButton, this.damageButton, this.blockButton, this.magicNumberButton, this.healButton, this.drawButton, this.discardButton, this.miscButton};

        ArrayList<String> a = new ArrayList<>();
        for (AbstractCard.CardColor cc : AbstractCard.CardColor.values()) {
            a.add(RelicClassComparator.getCharacterNameByColor(cc));
        }
        //Colorless
//        a.add(4,CardSelectSortHeader.cTEXT[4]);
//        //Curse
//        a.add(5,StringUtils.capitalize(GameDictionary.CURSE.NAMES[0]));
        this.classButton = new DropdownMenu(this,a,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        ArrayList<String> c = new ArrayList<>();
        for (AbstractCard.CardType ct : AbstractCard.CardType.values())
            c.add(CardSelectSortHeader.toLocalCardTypeStrings(ct));
        this.typeButton = new DropdownMenu(this, c,FontHelper.panelNameFont, Settings.CREAM_COLOR);


        ArrayList<String> e = new ArrayList<>();
        for (AbstractCard.CardRarity cr : AbstractCard.CardRarity.values()) {
            e.add(CardSelectSortHeader.toLocalRarity(cr));
        }
        this.rarityButton = new DropdownMenu(this,e,FontHelper.panelNameFont, Settings.CREAM_COLOR);


        this.dropdownMenus = new DropdownMenu[] {this.typeButton, this.rarityButton,this.classButton};
        this.dropdownMenuHeaders = new String[] {CardSelectSortHeader.clTEXT[1],CardSelectSortHeader.clTEXT[0],CardSelectSortHeader.rTEXT[0]};


    }

    public void update() {
        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }
        for (CardEffectButton button : this.cardEffectButtons) {
            button.update();
        }

        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            if (dropdownMenu.isOpen) {
                dropdownMenu.update();
                return;
            }
        }

        for (DropdownMenu dropdownMenu : this.dropdownMenus)
            dropdownMenu.update();
    }

    private int getCurCardColorIndex() {
        return getCard().color.ordinal();
    }
    private AbstractCard.CardColor indexToCardColor(int index) {
        return AbstractCard.CardColor.values()[index];
    }

    private int getCurCardRarityIndex() {
        return getCard().rarity.ordinal();
    }

    private AbstractCard.CardRarity indexToCardRarity(int index) {
        return AbstractCard.CardRarity.values()[index];
    }

    private int getCurCardTypeIndex() {
        return getCard().type.ordinal();
    }

    private AbstractCard.CardType indexToCardType(int index) {
        return AbstractCard.CardType.values()[index];
    }

    public Hitbox updateControllerInput() {
        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            Hitbox hb = dropdownMenu.getHitbox();
            if (hb.hovered) {
                return hb;
            }
        }
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        for (CardEffectButton button : this.cardEffectButtons) {
            if (button.hb.hovered) {
                return button.hb;
            }
        }

        return null;
    }

    public void setActiveButton(int idx) {
        if(idx < this.buttons.length)
            this.buttons[idx].setActive(true);
    }

    public int getHoveredIndex() {
        int retVal = 0;
        for (HeaderButtonPlus button : this.buttons) {
            if (button.hb.hovered) {
                return retVal;
            }
            retVal++;
        }
        return 0;
    }

    public void clearActiveButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if(!button.isToggle)
                button.setActive(false);
        }
    }

    private void resetDropdownMenus() {
        this.classButton.setSelectedIndex(getCurCardColorIndex());
        this.rarityButton.setSelectedIndex(getCurCardRarityIndex());
        this.typeButton.setSelectedIndex(getCurCardTypeIndex());
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        for (int i = 0;i<this.buttons.length;i++) {
            if (i!= btnIdx) {
                HeaderButtonPlus button = buttons[i];
                if(!button.isToggle)
                    button.reset();
                else if (button == this.makeExhaustButton)
                    button.isAscending = cardViewScreen.card.exhaust;
                else if (button == this.makeEtherealButton)
                    button.isAscending = cardViewScreen.card.isEthereal;
                else if (button == this.makeInnateButton)
                    button.isAscending = cardViewScreen.card.isInnate;
                else if (button == this.makeRetainButton)
                    button.isAscending = cardViewScreen.card.selfRetain;
                else if (button == this.makeUnplayableButton)
                    button.isAscending = cardViewScreen.card.cost == -2;
                else if (button == this.makeXCostButton)
                    button.isAscending = cardViewScreen.card.cost == -1;
                else if (button == this.makeAutoPlayButton)
                    button.isAscending = AutoplayField.autoplay.get(cardViewScreen.card);
                else if (button == this.makeSoulBoundButton)
                    button.isAscending = SoulboundField.soulbound.get(cardViewScreen.card);
                else if (button == this.makeFleetingButton)
                    button.isAscending = FleetingField.fleeting.get(cardViewScreen.card);
                else if (button == this.makeGraveButton)
                    button.isAscending = GraveField.grave.get(cardViewScreen.card);
                else if(button == this.makeGainGoldOnKillButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainGoldOnKillMod.ID);
                else if(button == this.makeGainHPOnKillButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainHpOnKillMod.ID);
                else if(button == this.makeGainGoldOnPlayButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainGoldOnPlayMod.ID);
                else if(button == this.makeHealOnPlayButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, HealOnPlayMod.ID);
                else if(button == this.randomUpgradeOnKillButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, RandomUpgradeOnKillMod.ID);
                else if (button == this.makeGainDamageOnKillButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainDamageOnKill.ID);
                else if(button == this.makeGainMagicOnKillButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainMagicOnKillMod.ID);
                else if(button == this.makeLifestealButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, LifestealMod.ID);
                else if(button == this.makeInevitableButton)
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, InevitableMod.ID);
                else if (button == this.makeInfUpgradeButton) {
                    button.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, InfiniteUpgradeMod.ID);
                }
            }
        }

        resetDropdownMenus();
    }
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if(!button.isToggle)
                button.reset();
        }
        for (int i = 0;i<this.cardEffectButtons.length;i++) {
            CardEffectButton button = cardEffectButtons[i];
            button.reset();
        }
        AbstractCard card = cardViewScreen.card;
        if(card != null) {
            this.makeExhaustButton.isAscending = card.exhaust;
            this.makeEtherealButton.isAscending = card.isEthereal;
            this.makeInnateButton.isAscending = card.isInnate;
            this.makeRetainButton.isAscending = card.selfRetain;
            this.makeUnplayableButton.isAscending = card.cost == -2;
            this.makeXCostButton.isAscending = card.cost == -1;
            this.makeAutoPlayButton.isAscending = AutoplayField.autoplay.get(card);
            this.makeSoulBoundButton.isAscending = SoulboundField.soulbound.get(card);
            this.makeFleetingButton.isAscending = FleetingField.fleeting.get(card);
            this.makeGraveButton.isAscending = GraveField.grave.get(card);
            this.makeGainGoldOnKillButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainGoldOnKillMod.ID);
            this.makeGainHPOnKillButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainHpOnKillMod.ID);
            this.makeGainGoldOnPlayButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainGoldOnPlayMod.ID);
            this.makeHealOnPlayButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, HealOnPlayMod.ID);
            this.randomUpgradeOnKillButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, RandomUpgradeOnKillMod.ID);
            this.makeGainDamageOnKillButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainDamageOnKill.ID);
            this.makeGainMagicOnKillButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, GainMagicOnKillMod.ID);
            this.makeLifestealButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, LifestealMod.ID);
            this.makeInevitableButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, InevitableMod.ID);
            this.makeInfUpgradeButton.isAscending = CardModifierManager.hasModifier(cardViewScreen.card, InfiniteUpgradeMod.ID);
        }


//        for (DropdownMenu ddm : dropdownMenus) {
//            ddm.setSelectedIndex(0);
//        }
        resetDropdownMenus();
    }

    private void setCardModded(boolean isModified) {
        AbstractCardPatch.setCardModified(cardViewScreen.card,isModified);
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.restoreDefaultButton) {
            clearActiveButtons();
            String cardId = cardViewScreen.card.cardID;
            int idx = cardViewScreen.group.group.indexOf(cardViewScreen.card);

            if (CardModifications.cardMap.containsKey(cardId)) {
                CardModifications.cardMap.remove(cardId);
                try {
                    cardModifications.save();
                } catch (IOException e) {
                    logger.error("Error saving card mods");
                }
            }
            AbstractCard freshCopy = CardLibrary.getCard(cardId).makeCopy();
            CardLibrary.cards.put(cardId,freshCopy);
            cardViewScreen.card = freshCopy;
            cardViewScreen.group.group.remove(idx);
            cardViewScreen.group.group.add(idx,freshCopy);

            setCardModded(false);
            resetOtherButtons();
        } else if (button == this.saveChangesButton) {
            clearActiveButtons();
            String cardId = cardViewScreen.card.cardID;
            CardModifications.cardMap.put(cardId, SerializableCard.toSerializableCard(cardViewScreen.card));
            //logger.info(CardModifications.cardMap.toString());
            CardLibrary.cards.put(cardId,cardViewScreen.card);
            //LoadoutMod.createCardList();
            cardsToDisplay.replaceAll(card -> (card.cardID.equals(cardId)) ? cardViewScreen.card : card);
            try {
                cardModifications.save();
            } catch (IOException e) {
                logger.error("Error saving card mods");
            }
            //CardModifications.modifyCards();

            resetOtherButtons();
        } else if (button == this.getCopyButton) {
            clearActiveButtons();

            for (int i = 0; i < multiplier; i++) {
                AbstractCard cardCopy = cardViewScreen.card.makeStatEquivalentCopy();
                AbstractCardPatch.CardModificationFields.isCardModifiedByModifier.set(cardCopy,AbstractCardPatch.CardModificationFields.isCardModifiedByModifier.get(cardViewScreen.card));
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(cardCopy, Settings.WIDTH / 2.0F - AbstractCard.IMG_WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
            }

            resetOtherButtons();
        } else if (button == this.makeUnplayableButton) {
            clearActiveButtons();
            if (!button.isAscending) {
                CardModifierManager.removeModifiersById(cardViewScreen.card, UnplayableMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new PlayableMod());
            }
            else {
                CardModifierManager.removeModifiersById(cardViewScreen.card, PlayableMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new UnplayableMod());
            }
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeExhaustButton) {
            clearActiveButtons();
            if (!button.isAscending) {
                CardModifierManager.removeModifiersById(cardViewScreen.card, ExhaustMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new UnexhaustMod());
            }
            else {
                CardModifierManager.removeModifiersById(cardViewScreen.card, UnexhaustMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new ExhaustMod());
            }
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeEtherealButton) {
            clearActiveButtons();
            if (!button.isAscending) {
                CardModifierManager.removeModifiersById(cardViewScreen.card, EtherealMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new UnetherealMod());
            }
            else {
                CardModifierManager.removeModifiersById(cardViewScreen.card, UnetherealMod.ID, true);
                CardModifierManager.addModifier(cardViewScreen.card, new EtherealMod());
            }

            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeInnateButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, InnateMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new InnateMod());
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeRetainButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, RetainMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new RetainMod());
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeXCostButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, XCostMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new XCostMod());
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeAutoPlayButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, AutoplayMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new AutoplayMod());
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeSoulBoundButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, SoulboundMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new SoulboundMod());
            setCardModded(true);
            resetOtherButtons();
        }else if (button == this.makeFleetingButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, FleetingMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new FleetingMod());
            setCardModded(true);
            resetOtherButtons();
        }else if (button == this.makeGraveButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GraveMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GraveMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeGainGoldOnKillButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GainGoldOnKillMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GainGoldOnKillMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeGainHPOnKillButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GainHpOnKillMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GainHpOnKillMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeGainGoldOnPlayButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GainGoldOnPlayMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GainGoldOnPlayMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeHealOnPlayButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, HealOnPlayMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new HealOnPlayMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.randomUpgradeOnKillButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, RandomUpgradeOnKillMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new RandomUpgradeOnKillMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeGainDamageOnKillButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GainDamageOnKill.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GainDamageOnKill());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeGainMagicOnKillButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, GainMagicOnKillMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new GainMagicOnKillMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeLifestealButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, LifestealMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new LifestealMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeInevitableButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, InevitableMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new InevitableMod());
            setCardModded(true);
            resetOtherButtons();
        } else if(button == this.makeInfUpgradeButton) {
            clearActiveButtons();
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, InfiniteUpgradeMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new InfiniteUpgradeMod());
            setCardModded(true);
            resetOtherButtons();
        } else {
            return;
        }
        if (cardViewScreen != null && cardViewScreen.card != null) {
            cardViewScreen.card.initializeDescription();
        }

    }

    public void render(SpriteBatch sb) {
        updateScrollPositions();

        renderButtons(sb);
        renderSelection(sb);


    }

    protected void updateScrollPositions() {

    }

    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }

        for (CardEffectButton b : this.cardEffectButtons) {
            b.render(sb);
        }

        float spaceY = 52.0f * Settings.yScale;
        float yPos = START_Y - 3.0f * spaceY;

        float xPos = 0f;

        for (int i = 0; i< this.dropdownMenus.length ; i++) {

            DropdownMenu ddm = this.dropdownMenus[i];

            ddm.render(sb,xPos,yPos);
            yPos += 0.5f * spaceY;
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
            yPos += spaceY;
        }

    }

    protected void renderSelection(SpriteBatch sb) {
        for (int i = 0; i < this.buttons.length; i++) {
            if (i == this.selectionIndex) {
                this.selectionColor.a = 0.7F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F;
                sb.setColor(this.selectionColor);
                float doop = 1.0F + (1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L))) / 50.0F;

                sb.draw(img, (this.buttons[this.selectionIndex]).hb.cX - 80.0F - (this.buttons[this.selectionIndex]).textWidth / 2.0F * Settings.scale, (this.buttons[this.selectionIndex]).hb.cY - 43.0F, 100.0F, 43.0F, 160.0F + (this.buttons[this.selectionIndex]).textWidth, 86.0F, Settings.scale * doop, Settings.scale * doop, 0.0F, 0, 0, 200, 86, false, false);
            }
        }
    }

    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {
        if(dropdownMenu == this.rarityButton) {
            AbstractCard.CardRarity rarity = indexToCardRarity(i);
            if(getCard().rarity != rarity) {
                getCard().rarity = rarity;
                setCardModded(true);
            }

        }
        if(dropdownMenu == this.classButton) {
//            logger.info("Current colors: " + Arrays.toString(AbstractCard.CardColor.values()));
//            logger.info("Selected Index: " + i);
//            logger.info("Curr Color: " + getCard().color.toString());
            AbstractCard.CardColor cc = indexToCardColor(i);
            if(getCard().color != cc) {
                getCard().color = cc;
                setCardModded(true);
            }

        }
        if(dropdownMenu == this.typeButton) {
            AbstractCard.CardType ct = indexToCardType(i);
            if(getCard().type != ct) {
                getCard().type = ct;
                setCardModded(true);
            }

        }
    }

    @Override
    public int getMultiplier() {
        return this.multiplier;
    }
    @Override
    public AbstractCard getCard() {
        return cardViewScreen.card == null ? new Madness() : cardViewScreen.card;
    }
}
