package loadout.screens;


import basemod.BaseMod;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.AutoplayField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.FleetingField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.GraveField;
import com.evacipated.cardcrawl.mod.stslib.fields.cards.AbstractCard.SoulboundField;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.KeywordStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.compendium.CardLibSortHeader;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import loadout.LoadoutMod;
import loadout.cardmods.*;
import loadout.patches.AbstractCardPatch;
import loadout.relics.CardModifier;
import loadout.relics.CardPrinter;
import loadout.savables.CardModifications;
import loadout.savables.SerializableCard;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static loadout.LoadoutMod.*;

public class CardViewPopupHeader implements HeaderButtonPlusListener, DropdownMenuListener {


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

    private final HeaderButtonPlus costIncreaseButton;
    private final HeaderButtonPlus costDecreaseButton;

    private final HeaderButtonPlus damageIncreaseButton;
    private final HeaderButtonPlus damageDecreaseButton;

    private final HeaderButtonPlus blockIncreaseButton;
    private final HeaderButtonPlus blockDecreaseButton;

    private final HeaderButtonPlus magicNumberIncButton;
    private final HeaderButtonPlus magicNumberDecButton;

    private final HeaderButtonPlus healIncreaseButton;
    private final HeaderButtonPlus healDecreaseButton;

    private final HeaderButtonPlus drawIncreaseButton;
    private final HeaderButtonPlus drawDecreaseButton;

    private final HeaderButtonPlus discardIncreaseButton;
    private final HeaderButtonPlus discardDecreaseButton;
    private final HeaderButtonPlus miscIncreaseButton;
    private final HeaderButtonPlus miscDecreaseButton;

    private final HeaderButtonPlus rarityIncreaseButton;
    private final HeaderButtonPlus rarityDecreaseButton;

    private final HeaderButtonPlus makeXCostButton;

    private final HeaderButtonPlus makeUnplayableButton;
    //private final HeaderButtonPlus makePlayableButton;
    private final HeaderButtonPlus makeUncurseButton;
    private final HeaderButtonPlus makeCurseButton;

    private final HeaderButtonPlus makeEtherealButton;
    private final HeaderButtonPlus makeExhaustButton;
    private final HeaderButtonPlus makeInnateButton;
    private final HeaderButtonPlus makeRetainButton;
    private final HeaderButtonPlus makeAutoPlayButton;
    private final HeaderButtonPlus makeSoulBoundButton;
    private final HeaderButtonPlus makeFleetingButton;
    private final HeaderButtonPlus makeGraveButton;

    private final HeaderButtonPlus restoreDefaultButton;
    private final HeaderButtonPlus saveChangesButton;
    private final HeaderButtonPlus getCopyButton;

    private String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    public int multiplier = 1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public SCardViewPopup cardViewScreen;




    public CardViewPopupHeader(SCardViewPopup sCardViewPopup, float startX) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
        AbstractCard card = sCardViewPopup.card;
        this.startX = startX;
        float xPosition = this.startX;
        float yPosition = START_Y;

        this.costIncreaseButton = new HeaderButtonPlus(clTEXT[3], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.costIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.costDecreaseButton = new HeaderButtonPlus(clTEXT[3], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

        this.damageIncreaseButton = new HeaderButtonPlus(TEXT[0], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.damageIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.damageDecreaseButton = new HeaderButtonPlus(TEXT[0], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

        this.blockIncreaseButton = new HeaderButtonPlus(StringUtils.capitalize(TEXT_BLOCK), xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.blockIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.blockDecreaseButton = new HeaderButtonPlus(StringUtils.capitalize(TEXT_BLOCK), xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;


        this.magicNumberIncButton = new HeaderButtonPlus(TEXT[1], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.magicNumberIncButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.magicNumberDecButton = new HeaderButtonPlus(TEXT[1], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

        this.healIncreaseButton = new HeaderButtonPlus(TEXT[2], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.healIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.healDecreaseButton = new HeaderButtonPlus(TEXT[2], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

        this.drawIncreaseButton = new HeaderButtonPlus(TEXT[3], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.drawIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.drawDecreaseButton = new HeaderButtonPlus(TEXT[3], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;


        this.discardIncreaseButton = new HeaderButtonPlus(TEXT_DISCARD, xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.discardIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.discardDecreaseButton = new HeaderButtonPlus(TEXT_DISCARD, xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;
        this.miscIncreaseButton = new HeaderButtonPlus("Misc", xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.miscIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.miscDecreaseButton = new HeaderButtonPlus("Misc", xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

//        this.makeXCostButton = new HeaderButtonPlus("X Cost", xPosition, yPosition, this, false, true, HeaderButtonPlus.Alignment.CENTER);
//        this.makeXCostButton.isActive = this.cardViewScreen.card.cost == -1;
//        yPosition -= SPACE_Y;
//        this.makeUnplayableButton = new HeaderButtonPlus("Make the card unplayable", xPosition, yPosition, this,false, true, HeaderButtonPlus.Alignment.CENTER);
//        this.makeUnplayableButton.isActive = this.cardViewScreen.card.cost == -2;
//        yPosition -= SPACE_Y;
        this.restoreDefaultButton = new HeaderButtonPlus(TEXT[4], xPosition, yPosition, this, true, ImageMaster.MAP_NODE_REST);
        yPosition -= SPACE_Y;
        this.saveChangesButton = new HeaderButtonPlus(TEXT[9], xPosition, yPosition, this, true, ImageMaster.SETTINGS_ICON);
        yPosition -= SPACE_Y;
        this.getCopyButton = new HeaderButtonPlus(TEXT[10], xPosition, yPosition, this, true, ImageMaster.PROFILE_B);


        yPosition = START_Y;
        xPosition = Settings.WIDTH - xPosition;

        this.rarityIncreaseButton = new HeaderButtonPlus(clTEXT[0], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        this.rarityIncreaseButton.isAscending = false;
        yPosition -= SPACE_Y;
        this.rarityDecreaseButton = new HeaderButtonPlus(clTEXT[0], xPosition,yPosition,this,true,ImageMaster.FILTER_ARROW);
        yPosition -= SPACE_Y;

        //this.makeUnplayableButton = new HeaderButtonPlus(TEXT[5], xPosition, yPosition, this, true, ImageMaster.loadImage("images/blights/muzzle.png"));
        this.makeUnplayableButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]), xPosition, yPosition, this, false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        //this.makePlayableButton = new HeaderButtonPlus(TEXT[6], xPosition, yPosition, this, true, ImageMaster.loadImage("images/blights/ancient.png"));
        //yPosition -= SPACE_Y;
        this.makeUncurseButton = new HeaderButtonPlus(TEXT[7], xPosition, yPosition, this, true, ImageMaster.loadImage("images/relics/omamori.png"));
        yPosition -= SPACE_Y;
        this.makeCurseButton = new HeaderButtonPlus(TEXT[8], xPosition, yPosition, this, true, ImageMaster.loadImage("images/blights/accursed.png"));
        yPosition -= SPACE_Y;
        this.makeExhaustButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);

        yPosition -= SPACE_Y;
        this.makeEtherealButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.ETHEREAL.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);

        yPosition -= SPACE_Y;
        this.makeInnateButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.INNATE.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);

        yPosition -= SPACE_Y;
        this.makeRetainButton = new HeaderButtonPlus(StringUtils.capitalize(GameDictionary.RETAIN.NAMES[0]),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);

        yPosition -= SPACE_Y;
        this.makeXCostButton = new HeaderButtonPlus("X " + clTEXT[3],xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        this.makeAutoPlayButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("autoplay"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        this.makeSoulBoundButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("soulbound"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        this.makeFleetingButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("fleeting"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;
        this.makeGraveButton = new HeaderButtonPlus(BaseMod.getKeywordTitle("grave"),xPosition,yPosition,this,false,true, HeaderButtonPlus.Alignment.CENTER);
        yPosition -= SPACE_Y;



        this.buttons = new HeaderButtonPlus[] { this.costIncreaseButton, this.costDecreaseButton, this.damageIncreaseButton, this.damageDecreaseButton, this.blockIncreaseButton, this.blockDecreaseButton,this.magicNumberIncButton, this.magicNumberDecButton , this.healIncreaseButton, this.healDecreaseButton, this.drawIncreaseButton, this.drawDecreaseButton, this.discardIncreaseButton, this.discardDecreaseButton, this.miscIncreaseButton, this.miscDecreaseButton, this.restoreDefaultButton,
        this.saveChangesButton, this.getCopyButton, this.rarityIncreaseButton, this.rarityDecreaseButton,this.makeUnplayableButton, this.makeUncurseButton, this.makeCurseButton, this.makeExhaustButton, this.makeEtherealButton, this.makeInnateButton, this.makeRetainButton, this.makeXCostButton, this.makeAutoPlayButton, this.makeSoulBoundButton, this.makeFleetingButton, this.makeGraveButton};



        this.dropdownMenus = new DropdownMenu[] {};
        this.dropdownMenuHeaders = new String[] {};
        this.cardViewScreen = sCardViewPopup;

    }

    public void update() {
        for (HeaderButtonPlus button : this.buttons) {
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
            }
        }
    }
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if(!button.isToggle)
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
        }


        for (DropdownMenu ddm : dropdownMenus) {
            ddm.setSelectedIndex(0);
        }

    }

    private void setCardModded(boolean isModified) {
        AbstractCardPatch.setCardModified(cardViewScreen.card,isModified);
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.costIncreaseButton) {
            clearActiveButtons();
            if(cardViewScreen.card.cost >= 0) {
                cardViewScreen.card.cost += multiplier;
                cardViewScreen.card.costForTurn = cardViewScreen.card.cost;
                setCardModded(true);
            }

            resetAllButtons();
        } else if (button == this.costDecreaseButton) {
            clearActiveButtons();

            if(cardViewScreen.card.cost>0) {
                int tmpCost = cardViewScreen.card.cost;
                tmpCost -= multiplier;
                if(tmpCost < 0) tmpCost = 0;
                cardViewScreen.card.cost = tmpCost;

                cardViewScreen.card.costForTurn = cardViewScreen.card.cost;
                setCardModded(true);
            }

            resetOtherButtons();
        } else if (button == this.magicNumberIncButton) {
            clearActiveButtons();
            cardViewScreen.card.baseMagicNumber += multiplier;
            cardViewScreen.card.magicNumber += multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.magicNumberDecButton) {
            clearActiveButtons();
            cardViewScreen.card.baseMagicNumber-= multiplier;
            cardViewScreen.card.magicNumber -= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.damageIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDamage+= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.damageDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDamage-= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.blockIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseBlock+= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.blockDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseBlock-= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.healIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseHeal+= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.healDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseHeal-= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.drawIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDraw+= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.drawDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDraw-= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.discardIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDiscard+= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.discardDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.baseDiscard-= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.miscIncreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.misc += multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.miscDecreaseButton) {
            clearActiveButtons();
            cardViewScreen.card.misc -= multiplier;
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.restoreDefaultButton) {
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
            LoadoutMod.createCardList();
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
        } else if (button == this.rarityIncreaseButton) {
            clearActiveButtons();
            int rarInt = cardViewScreen.card.rarity.ordinal();
            AbstractCard.CardRarity[] rarities = AbstractCard.CardRarity.values();
            if( ++rarInt < rarities.length) {
                cardViewScreen.card.rarity = rarities[rarInt];
                setCardModded(true);
            }

            resetOtherButtons();
        } else if (button == this.rarityDecreaseButton) {
            clearActiveButtons();
            int rarInt = cardViewScreen.card.rarity.ordinal();
            AbstractCard.CardRarity[] rarities = AbstractCard.CardRarity.values();
            if( --rarInt >= 0) {
                cardViewScreen.card.rarity = rarities[rarInt];
                setCardModded(true);
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
        } else if (button == this.makeUncurseButton) {
            clearActiveButtons();
            if (cardViewScreen.card.type == AbstractCard.CardType.CURSE) {
                cardViewScreen.card.type = AbstractCard.CardType.SKILL;
            }
            if (cardViewScreen.card.color == AbstractCard.CardColor.CURSE) {
                cardViewScreen.card.color = AbstractCard.CardColor.COLORLESS;
            }
            if (cardViewScreen.card.rarity == AbstractCard.CardRarity.CURSE) {
                cardViewScreen.card.rarity = AbstractCard.CardRarity.SPECIAL;
            }
            setCardModded(true);
            resetOtherButtons();
        } else if (button == this.makeCurseButton) {
            clearActiveButtons();
            cardViewScreen.card.type = AbstractCard.CardType.CURSE;
            cardViewScreen.card.color = AbstractCard.CardColor.CURSE;
            cardViewScreen.card.rarity = AbstractCard.CardRarity.CURSE;
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
            if (!button.isAscending)
                CardModifierManager.removeModifiersById(cardViewScreen.card, EtherealMod.ID, true);
            else
                CardModifierManager.addModifier(cardViewScreen.card, new EtherealMod());
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
        }else {
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

        float spaceY = 52.0f * Settings.yScale;
        float yPos = START_Y - 7.0f * spaceY;

        float xPos = 0.0f;

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

    }
}
