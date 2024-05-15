package loadout.screens;

import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.BranchingUpgradesCard;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.CancelButton;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import loadout.LoadoutMod;
import loadout.helper.FabricateScreenController;
import loadout.relics.*;
import loadout.uiElements.CardBranchRenderPanel;
import loadout.util.ModConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import rs.lazymankits.interfaces.cards.BranchableUpgradeCard;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;


public class GCardSelectScreen
        implements ScrollBarListener
{
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] TEXT = uiStrings.TEXT; private static float drawStartX; private static float drawStartY;
    private static float padX;
    private static float padY;
    private static final int CARDS_PER_LINE = 5;
    private static final float SCROLL_BAR_THRESHOLD = 500.0F * Settings.scale;
    private float grabStartY = 0.0F; private float currentDiffY = 0.0F;
    public ArrayList<AbstractCard> selectedCards = new ArrayList<>();
    public CardGroup originalGroup;
    public CardGroup targetGroup;
    private AbstractCard hoveredCard = null;
    public AbstractCard upgradePreviewCard = null;
    private int numCards = 0; private int cardSelectAmount = 0;
    private float scrollLowerBound = -Settings.DEFAULT_SCROLL_LIMIT;
    private float scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT;
    private boolean grabbedScreen = false;
    private boolean canCancel = true;
    public boolean forUpgrade = false;
    public GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(TEXT[0]); public boolean forTransform = false; public boolean forPurge = false; public boolean confirmScreenUp = false; public boolean isJustForConfirming = false;
    public PeekButton peekButton = new PeekButton();
    private String tipMsg = "";
    private String lastTip = "";
    private float ritualAnimTimer = 0.0F;
    private static final float RITUAL_ANIM_INTERVAL = 0.1F;
    private int prevDeckSize = 0; public boolean cancelWasOn = false; public boolean anyNumber = false;
    public boolean forClarity = false;
    public String cancelText;
    private ScrollBar scrollBar;
    private AbstractCard controllerCard = null;

    private static final int ARROW_W = 64;

    private float arrowScale1 = 1.0F, arrowScale2 = 1.0F, arrowScale3 = 1.0F, arrowTimer = 0.0F;
    public boolean show = false;
    public boolean doneSelecting = false;
    
    private CancelButton cancelButton = new CancelButton();

    private static final Comparator<AbstractCard> BY_COLOR;
    private static final Comparator<AbstractCard> BY_NAME;
    private static final Comparator<AbstractCard> BY_COST;
    private static final Comparator<AbstractCard> BY_MOD;
    private static final Comparator<AbstractCard> BY_RARITY;
    private static final Comparator<AbstractCard> BY_TYPE;
    static {
        BY_COLOR = Comparator.comparing(card -> card.color);
        BY_NAME = Comparator.comparing(card -> card.name);
        BY_COST = Comparator.comparingInt(card -> card.cost);
        BY_MOD = Comparator.comparing(card -> {
            String cardModID = WhatMod.findModID(card.getClass());
            return cardModID== null? "Slay the Spire" : cardModID;
        });
        BY_RARITY = Comparator.comparing(card -> card.rarity);
        BY_TYPE = Comparator.comparing(card -> card.type);
    }

    private CardSelectSortHeader cardSelectSortHeader;
    private CombatCardSelectSortHeader combatCardSelectSortHeader;

    private SCardViewPopup cardModScreen = null;

    public AbstractCard.CardColor filterColor = null;
    public int filterCost = -99;
    public String filterMod = null;
    public AbstractCard.CardType filterType = null;
    public AbstractCard.CardRarity filterRarity = null;
    public boolean filterUpgraded = false;
    protected enum SortType {
        COLOR,NAME,MOD,COST,RARITY,TYPE
    }
    public SortType currentSortType;
    private boolean isDragSelecting;
    private boolean isTryingToScroll = false;
    public enum CardDisplayMode {
        OBTAIN, DELETE, UPGRADE
    }

    public CardDisplayMode currentMode = CardDisplayMode.OBTAIN;
    private AbstractCard clickStartedCard = null;

    private InputAction shiftKey;
    private InputAction ctrlKey;

    public AbstractCardScreenRelic caller;

    public enum ViewingPool {
        EXTERNAL,MASTER_DECK, DRAW, DISCARD, HAND, EXHAUST
    }

    public ViewingPool currentPool;

    public int selectMult = 1;

    public CardBranchRenderPanel branchRenderPanel;


    public GCardSelectScreen(CardDisplayMode currentMode,  AbstractCardScreenRelic caller) {
        this.caller = caller;

        drawStartX = Settings.WIDTH;
        drawStartX -= (float)CARDS_PER_LINE * AbstractCard.IMG_WIDTH * 0.75F;
        drawStartX -= (float)(CARDS_PER_LINE-1) * Settings.CARD_VIEW_PAD_X;
        drawStartX /= 2.0F;

        //drawStartX += AbstractCard.IMG_WIDTH * 0.75F / 2.0F;
        drawStartX += AbstractCard.IMG_WIDTH * 0.75F;

        padX = AbstractCard.IMG_WIDTH * 0.75F + Settings.CARD_VIEW_PAD_X;
        padY = AbstractCard.IMG_HEIGHT * 0.75F + Settings.CARD_VIEW_PAD_Y;

        this.scrollBar = new ScrollBar(this);
        this.scrollBar.move(+50.0F * Settings.scale, 50.0F * Settings.scale);
        this.scrollBar.changeHeight(Settings.HEIGHT/1.5f);
        this.currentMode = currentMode;

        this.shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);
        this.ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);

        switch (currentMode) {

            case OBTAIN:
                currentSortType = SortType.RARITY;
                this.currentPool = ViewingPool.EXTERNAL;
                break;
            case DELETE:
            case UPGRADE:
                this.currentPool = ViewingPool.MASTER_DECK;
                break;
        }

        this.branchRenderPanel = new CardBranchRenderPanel();
    }

    public void update() {
        updateControllerInput();
        updatePeekButton();

        if(LoadoutMod.FABRICATE_MOD_LOADED) {
            if(FabricateScreenController.isScreenUp) {
                FabricateScreenController.update();
                return;
            }
        }


        if (this.currentMode == CardDisplayMode.UPGRADE && this.cardModScreen != null && this.cardModScreen.isOpen) {
            this.cardModScreen.update();
            return;
        } else if (this.currentMode == CardDisplayMode.UPGRADE && this.branchRenderPanel.shown) {
            this.branchRenderPanel.update();
            updateScrolling();
            return;
        }

        if (PeekButton.isPeeking) {
            return;
        }
        if(this.cancelButton.hb.clicked) {
            close();
            return;
        }
        if (InputHelper.pressedEscape) {
            close();
            InputHelper.pressedEscape = false;
            return;
        }

        if (Settings.isControllerMode && this.controllerCard != null && !CardCrawlGame.isPopupOpen && this.upgradePreviewCard == null)
        {
            if (Gdx.input.getY() > Settings.HEIGHT * 0.75F) {
                this.currentDiffY += Settings.SCROLL_SPEED;
            } else if (Gdx.input.getY() < Settings.HEIGHT * 0.25F) {
                this.currentDiffY -= Settings.SCROLL_SPEED;
            }
        }

        if (this.shiftKey.isPressed() && this.ctrlKey.isPressed()) {
            selectMult = 50;
        } else if (this.shiftKey.isPressed()) {
            selectMult = 10;
        } else if (this.ctrlKey.isPressed()) {
            selectMult = 5;
        } else {
            selectMult = 1;
        }


        if (this.forClarity) {
            if (this.selectedCards.size() > 0) {
                this.confirmButton.isDisabled = false;
            } else {
                this.confirmButton.isDisabled = true;
            }
        }

        this.confirmButton.update();
        if(this.currentMode == CardDisplayMode.OBTAIN && this.cardSelectSortHeader != null)
            this.cardSelectSortHeader.update();
        else if ((this.currentMode == CardDisplayMode.DELETE || this.currentMode == CardDisplayMode.UPGRADE) && this.combatCardSelectSortHeader != null)
            if(AbstractDungeon.getCurrRoom()!=null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT)
                this.combatCardSelectSortHeader.update();

        if (this.isJustForConfirming) {
            updateCardPositionsAndHoverLogic();
            if (this.confirmButton.hb.clicked || CInputActionSet.topPanel.isJustPressed()) {
                CInputActionSet.select.unpress();
                this.confirmButton.hb.clicked = false;
                this.cancelButton.hide();
                AbstractDungeon.dynamicBanner.hide();
                this.confirmScreenUp = false;
                for (AbstractCard c : this.targetGroup.group) {
                    AbstractDungeon.topLevelEffects.add(new FastCardObtainEffect(c, c.current_x, c.current_y));
                }
                AbstractDungeon.closeCurrentScreen();
            }  return;
        }
        if (this.confirmButton.hb.clicked) {
            this.confirmButton.hb.clicked = false;
            this.doneSelecting = true;
            close();
            return;
        }
        boolean isDraggingScrollBar = false;
        if (shouldShowScrollBar()) {
            isDraggingScrollBar = this.scrollBar.update();
        }
        if (!this.confirmScreenUp) {
            updateCardPositionsAndHoverLogic();

            if ((InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed())&&hoveredCard==null) {
                isTryingToScroll = true;
            }
            if(InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
                isTryingToScroll = false;
            }

            if (this.hoveredCard != null  && !isTryingToScroll) {
                if (ModConfig.enableDrag) {
                    if (InputHelper.isMouseDown) {
                        this.hoveredCard.hb.clickStarted = true;
                        this.isDragSelecting = true;
                        if (this.hoveredCard != this.clickStartedCard) {
                            this.clickStartedCard = this.hoveredCard;
                            if (!this.selectedCards.contains(this.hoveredCard)) {
                                this.selectedCards.add(this.hoveredCard);
                                this.hoveredCard.beginGlowing();
                                this.hoveredCard.targetDrawScale = 0.75F;
                                this.hoveredCard.drawScale = 0.875F;

                                this.cardSelectAmount++;
                                CardCrawlGame.sound.play("CARD_SELECT");

                            } else {
                                //if clicked already selected card
                                this.hoveredCard.stopGlowing();
                                this.selectedCards.remove(this.hoveredCard);
                                this.cardSelectAmount--;
                            }
                        }

                    } else if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustReleased()) {
                        CInputActionSet.select.unpress();
                        isDragSelecting = false;
                        if (hoveredCard == clickStartedCard) {

                            clickStartedCard = null;

                        }
                    }

                        if ((this.hoveredCard.hb.clicked || CInputActionSet.select.isJustPressed())) {
                        this.hoveredCard.hb.clicked = false;
                        //return;
                    }
                } else {
                    if (InputHelper.justClickedLeft) {
                        this.hoveredCard.hb.clickStarted = true;
                    }

                    if ((this.hoveredCard.hb.clicked || CInputActionSet.select.isJustPressed())) {
                        this.hoveredCard.hb.clicked = false;
                        this.hoveredCard.targetDrawScale = 0.75F;
                        this.hoveredCard.drawScale = 0.875F;

                        CardCrawlGame.sound.play("CARD_SELECT");

                        switch (currentMode) {

                            case OBTAIN:
                                CardPrinter.lastCards.clear();
                                for (int i=0; i<selectMult; i++) {
                                    ((CardPrinter)caller).obtainCard(this.hoveredCard);
                                    CardPrinter.lastCards.add(this.hoveredCard.makeStatEquivalentCopy());
                                }
                                break;
                            case DELETE:
                                ((CardShredder)caller).removeCard(this.hoveredCard);
                                refreshCardsInDisplay();
                                break;
                            case UPGRADE:
                                if(this.hoveredCard instanceof BranchingUpgradesCard || (Loader.isModLoaded("LazyManKits") && this.hoveredCard instanceof BranchableUpgradeCard))
                                    branchRenderPanel.show(this.hoveredCard);
                                else CardModifier.upgradeCard(this.hoveredCard);
                                refreshCardsInDisplay();
                                break;
                        }

                        //return;
                    }
                }

                if (InputHelper.justReleasedClickRight)
                {
                    if (!(currentMode==CardDisplayMode.UPGRADE))
                        CardCrawlGame.cardPopup.open(this.hoveredCard,this.targetGroup);
                    else {
                        if(!this.cardModScreen.isOpen)
                            this.cardModScreen.open(this.hoveredCard,this.targetGroup);
                    }
                }

        } else {
                clickStartedCard = null;
                isDragSelecting = false;
            }

        } else {
            if (this.forTransform) {
                this.ritualAnimTimer -= Gdx.graphics.getDeltaTime();
                if (this.ritualAnimTimer < 0.0F) {
                    this.upgradePreviewCard = AbstractDungeon.returnTrulyRandomCardFromAvailable(this.upgradePreviewCard).makeCopy();
                    this.ritualAnimTimer = 0.1F;
                }
            }


            if (this.forUpgrade) {
                this.upgradePreviewCard.update();
            }
            if (!this.forPurge) {
                this.upgradePreviewCard.drawScale = 1.0F;
                this.hoveredCard.update();
                this.hoveredCard.drawScale = 1.0F;
            }

            if (this.confirmButton.hb.clicked || CInputActionSet.topPanel.isJustPressed()) {
                CInputActionSet.select.unpress();
                this.confirmButton.hb.clicked = false;
                this.cancelButton.hide();
                this.confirmScreenUp = false;
                this.selectedCards.add(this.hoveredCard);
                this.caller.setIsSelectionScreenUp(false);
                AbstractDungeon.closeCurrentScreen();
            }
        }


        if (!isDraggingScrollBar && !isDragSelecting) {
            updateScrolling();
        }
        hoveredCard = null;

        if (Settings.isControllerMode) {
            if (this.upgradePreviewCard != null) {
                CInputHelper.setCursor(this.upgradePreviewCard.hb);
            } else if (this.controllerCard != null) {
                CInputHelper.setCursor(this.controllerCard.hb);
            }
        }


    }

    private void updatePeekButton() {
        this.peekButton.update();
    }

    private void updateControllerInput() {
        if (!Settings.isControllerMode || this.upgradePreviewCard != null) {
            return;
        }

        boolean anyHovered = false;
        int index = 0;

        for (AbstractCard c : this.targetGroup.group) {
            if (c.hb.hovered) {
                anyHovered = true;
                break;
            }
            index++;
        }

        if (!anyHovered && this.controllerCard == null) {
            CInputHelper.setCursor(((AbstractCard)this.targetGroup.group.get(0)).hb);
            this.controllerCard = this.targetGroup.group.get(0);
        }
        else if ((CInputActionSet.up.isJustPressed() || CInputActionSet.altUp.isJustPressed()) && this.targetGroup
                .size() > 5) {

            if (index < 5) {
                index = this.targetGroup.size() + 2 - 4 - index;
                if (index > this.targetGroup.size() - 1) {
                    index -= 5;
                }
                if (index > this.targetGroup.size() - 1 || index < 0) {
                    index = 0;
                }
            } else {

                index -= 5;
            }
            CInputHelper.setCursor(((AbstractCard)this.targetGroup.group.get(index)).hb);
            this.controllerCard = this.targetGroup.group.get(index);
        } else if ((CInputActionSet.down.isJustPressed() || CInputActionSet.altDown.isJustPressed()) && this.targetGroup
                .size() > 5) {
            if (index < this.targetGroup.size() - 5) {
                index += 5;
            } else {
                index %= 5;
            }
            CInputHelper.setCursor(((AbstractCard)this.targetGroup.group.get(index)).hb);
            this.controllerCard = this.targetGroup.group.get(index);
        } else if (CInputActionSet.left.isJustPressed() || CInputActionSet.altLeft.isJustPressed()) {
            if (index % 5 > 0) {
                index--;
            } else {
                index += 4;
                if (index > this.targetGroup.size() - 1) {
                    index = this.targetGroup.size() - 1;
                }
            }
            CInputHelper.setCursor(((AbstractCard)this.targetGroup.group.get(index)).hb);
            this.controllerCard = this.targetGroup.group.get(index);
        } else if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
            if (index % 5 < 4) {
                index++;
                if (index > this.targetGroup.size() - 1) {
                    index -= this.targetGroup.size() % 5;
                }
            } else {
                index -= 4;
                if (index < 0) {
                    index = 0;
                }
            }

            if (index > this.targetGroup.group.size() - 1) {
                index = 0;
            }
            CInputHelper.setCursor(((AbstractCard)this.targetGroup.group.get(index)).hb);
            this.controllerCard = this.targetGroup.group.get(index);
        }
    }

    private void updateCardPositionsAndHoverLogic() {
        if (this.isJustForConfirming && this.targetGroup.size() <= 4) {
            switch (this.targetGroup.size()) {
                case 1:
                    (this.targetGroup.getBottomCard()).current_x = Settings.WIDTH / 2.0F;
                    (this.targetGroup.getBottomCard()).target_x = Settings.WIDTH / 2.0F;
                    break;
                case 2:
                    ((AbstractCard)this.targetGroup.group.get(0)).current_x = Settings.WIDTH / 2.0F - padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(0)).target_x = Settings.WIDTH / 2.0F - padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(1)).current_x = Settings.WIDTH / 2.0F + padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(1)).target_x = Settings.WIDTH / 2.0F + padX / 2.0F;
                    break;
                case 3:
                    ((AbstractCard)this.targetGroup.group.get(0)).current_x = drawStartX + padX;
                    ((AbstractCard)this.targetGroup.group.get(1)).current_x = drawStartX + padX * 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(2)).current_x = drawStartX + padX * 3.0F;
                    ((AbstractCard)this.targetGroup.group.get(0)).target_x = drawStartX + padX;
                    ((AbstractCard)this.targetGroup.group.get(1)).target_x = drawStartX + padX * 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(2)).target_x = drawStartX + padX * 3.0F;
                    break;
                case 4:
                    ((AbstractCard)this.targetGroup.group.get(0)).current_x = Settings.WIDTH / 2.0F - padX / 2.0F - padX;
                    ((AbstractCard)this.targetGroup.group.get(0)).target_x = Settings.WIDTH / 2.0F - padX / 2.0F - padX;
                    ((AbstractCard)this.targetGroup.group.get(1)).current_x = Settings.WIDTH / 2.0F - padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(1)).target_x = Settings.WIDTH / 2.0F - padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(2)).current_x = Settings.WIDTH / 2.0F + padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(2)).target_x = Settings.WIDTH / 2.0F + padX / 2.0F;
                    ((AbstractCard)this.targetGroup.group.get(3)).current_x = Settings.WIDTH / 2.0F + padX / 2.0F + padX;
                    ((AbstractCard)this.targetGroup.group.get(3)).target_x = Settings.WIDTH / 2.0F + padX / 2.0F + padX;
                    break;
            }

            ArrayList<AbstractCard> c2 = this.targetGroup.group;

            for (int j = 0; j < c2.size(); j++) {
                ((AbstractCard)c2.get(j)).target_y = drawStartY + this.currentDiffY;
                ((AbstractCard)c2.get(j)).fadingOut = false;
                ((AbstractCard)c2.get(j)).update();
                ((AbstractCard)c2.get(j)).updateHoverLogic();

                this.hoveredCard = null;
                for (AbstractCard c : c2) {
                    if (c.hb.hovered) {
                        this.hoveredCard = c;
                    }
                }
            }


            return;
        }

        int lineNum = 0;

        ArrayList<AbstractCard> cards = this.targetGroup.group;
        if (cards != null) {
            for (int i = 0; i < cards.size(); i++) {
                int mod = i % 5;
                if (mod == 0 && i != 0) {
                    lineNum++;
                }
                ((AbstractCard)cards.get(i)).target_x = drawStartX + mod * padX;
                ((AbstractCard)cards.get(i)).target_y = drawStartY + this.currentDiffY - lineNum * padY;
                ((AbstractCard)cards.get(i)).fadingOut = false;
                ((AbstractCard)cards.get(i)).update();
                ((AbstractCard)cards.get(i)).updateHoverLogic();

                this.hoveredCard = null;
                for (AbstractCard c : cards) {
                    if (c.hb.hovered) {
                        this.hoveredCard = c;
                    }
                }
            }
        }
    }

    public void sortByName(boolean isAscending) {
        currentSortType = SortType.NAME;
        if(isAscending) {
            this.targetGroup.group.sort(BY_NAME);
        } else {
            this.targetGroup.group.sort(BY_NAME.reversed());
        }
    }
    public void sortByCost(boolean isAscending) {
        currentSortType = SortType.COST;
        if(isAscending) {
            this.targetGroup.group.sort(BY_COST.thenComparing(BY_NAME));
        } else {
            this.targetGroup.group.sort(BY_COST.reversed().thenComparing(BY_NAME));
        }
    }
    public void sortByColor(boolean isAscending) {
        currentSortType = SortType.COLOR;
        if(isAscending) {
            this.targetGroup.group.sort(BY_COLOR.thenComparing(BY_NAME));
        } else {
            this.targetGroup.group.sort(BY_COLOR.reversed().thenComparing(BY_NAME));
        }
    }
    public void sortByMod(boolean isAscending) {
        currentSortType = SortType.MOD;
        if(isAscending) {
            this.targetGroup.group.sort(BY_MOD.thenComparing(BY_NAME));
        } else {
            this.targetGroup.group.sort(BY_MOD.reversed().thenComparing(BY_NAME));
        }
    }

    public void sortByRarity(boolean isAscending) {
        currentSortType = SortType.RARITY;
        if(isAscending) {
            this.targetGroup.group.sort(BY_COLOR.thenComparing(BY_RARITY.thenComparing(BY_NAME)));
        } else {
            this.targetGroup.group.sort(BY_COLOR.thenComparing(BY_RARITY.reversed().thenComparing(BY_NAME)));
        }
    }

    public void sort(boolean isAscending) {
        switch (currentSortType) {
            case COLOR:
                sortByColor(isAscending);
                break;
            case NAME:
                sortByName(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
            case COST:
                sortByCost(isAscending);
                break;
            case RARITY:
                sortByRarity(isAscending);
                break;
            case TYPE:
                break;
        }
    }

    private boolean testTextFilter(AbstractCard card, String text) {
        //text = StringUtils.lowerCase(text);
        return StringUtils.containsIgnoreCase(card.cardID,text) || StringUtils.containsIgnoreCase(card.name,text) || StringUtils.containsIgnoreCase(card.rawDescription,text) ;
    }

    private boolean testFilter(AbstractCard card) {
        boolean costCheck = this.filterCost == -99 || card.cost == filterCost || (filterCost == 4 && card.cost >= 4) || (filterCost == -2 && card.cost <=-2);
        boolean colorCheck = this.filterColor == null || card.color == this.filterColor;
        String modID = WhatMod.findModID(card.getClass());
        if (modID == null) modID = "Slay the Spire";
        boolean modCheck = this.filterMod == null || modID.equals(this.filterMod);
        boolean rarityCheck = this.filterRarity == null || card.rarity == this.filterRarity;
        boolean typeCheck = this.filterType == null || card.type == this.filterType;
        boolean textCheck = this.cardSelectSortHeader.filterText.equals("") || testTextFilter(card, this.cardSelectSortHeader.filterText);

        return costCheck && colorCheck && modCheck && rarityCheck && typeCheck && textCheck;
    }
    public void resetFilters() {
//        this.targetGroup.group = new ArrayList<>();
//        for (AbstractCard ac : this.originalGroup.group) {
//            this.targetGroup.addToTop(ac);
//        }
        this.targetGroup = new CardGroup(this.originalGroup, CardGroup.CardGroupType.CARD_POOL);
    }
    public void updateFilters() {
        resetFilters();
        if(filterUpgraded) {
            this.targetGroup.group.forEach(AbstractCard::upgrade);
        }

        this.targetGroup.group = this.targetGroup.group.stream().filter(this::testFilter).collect(Collectors.toCollection(ArrayList::new));
        sort(true);


        scrolledUsingBar(0.0f);
    }

    public void refreshCardsInDisplay() {
        switch (currentPool) {

            case EXTERNAL:
                break;
            case MASTER_DECK:
                this.targetGroup = AbstractDungeon.player.masterDeck;
                break;
            case DRAW:
                this.targetGroup = AbstractDungeon.player.drawPile;
                break;
            case DISCARD:
                this.targetGroup = AbstractDungeon.player.discardPile;
                break;
            case HAND:
                this.targetGroup = AbstractDungeon.player.hand;

                break;
            case EXHAUST:
                this.targetGroup = AbstractDungeon.player.exhaustPile;
                break;
        }



    }


    public void open(CardGroup group, int numCards, String tipMsg, boolean forUpgrade, boolean forTransform, boolean canCancel, boolean forPurge) {

        this.originalGroup = group;
        //this.originalGroup.sortByRarityPlusStatusCardType(true);
        if (this.targetGroup == null) {
            if(currentMode == CardDisplayMode.OBTAIN)
                this.targetGroup = new CardGroup(this.originalGroup, CardGroup.CardGroupType.CARD_POOL);
            else
                this.targetGroup = group;
        }

        callOnOpen();

        this.forUpgrade = forUpgrade;
        this.forTransform = forTransform;
        this.canCancel = canCancel;
        this.forPurge = forPurge;
        this.tipMsg = tipMsg;
        this.numCards = numCards;

        if (canCancel)
        {
            this.cancelButton.show(TEXT[1]);
        }

        if (!canCancel) {
            this.cancelButton.hide();
        }


        if (AbstractDungeon.getCurrRoom() !=null && (AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT) {
            this.peekButton.hideInstantly();
            this.peekButton.show();
        }

        calculateScrollBounds();
    }

    public void openConfirmationGrid(CardGroup group, String tipMsg) {
        this.targetGroup = group;
        callOnOpen();


        this.isJustForConfirming = true;
        this.tipMsg = tipMsg;


        this.cancelButton.hideInstantly();


        this.confirmButton.show();
        this.confirmButton.updateText(TEXT[0]);
        this.confirmButton.isDisabled = false;

        this.canCancel = false;

        if (group.size() <= 5) {
            AbstractDungeon.dynamicBanner.appear(tipMsg);
        }
    }

    private void callOnOpen() {
        Configurator.setLevel(TipHelper.class.getName(),Level.FATAL);
        Configurator.setLevel(LoadoutMod.class.getName(),Level.FATAL);

        if (Settings.isControllerMode) {
            Gdx.input.setCursorPosition(10, Settings.HEIGHT / 2);
            this.controllerCard = null;
        }
        this.doneSelecting = false;
        AbstractSelectScreen.hideLoadoutRelics();


        if(currentMode == CardDisplayMode.OBTAIN) {
            if (this.cardSelectSortHeader == null)
                this.cardSelectSortHeader = new CardSelectSortHeader(this,drawStartX - 4 * Settings.CARD_VIEW_PAD_X);

            this.cardSelectSortHeader.resetAllButtons();
            this.cardSelectSortHeader.setToCurrentClass();
            this.sortByRarity(true);
        }


        if (currentMode == CardDisplayMode.DELETE || currentMode == CardDisplayMode.UPGRADE) {
            if (this.combatCardSelectSortHeader == null) {
                //first time opening
                this.combatCardSelectSortHeader = new CombatCardSelectSortHeader(this,drawStartX - 6 * Settings.CARD_VIEW_PAD_X);
                this.combatCardSelectSortHeader.setActiveButton(0);
            } else if (AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT) {
                //when it is opened again outside of combat
                this.currentPool = ViewingPool.MASTER_DECK;
                refreshCardsInDisplay();
            }


        }

        if (currentMode == CardDisplayMode.UPGRADE) {
            if(this.cardModScreen == null) {
                this.cardModScreen = new SCardViewPopup();
            }
        }

        this.isTryingToScroll = false;

        this.anyNumber = true;
        this.forClarity = false;
        this.canCancel = true;
        this.forUpgrade = false;
        this.forTransform = false;
        this.forPurge = false;
        this.confirmScreenUp = false;
        this.isJustForConfirming = false;
        AbstractDungeon.overlayMenu.proceedButton.hide();
        this.controllerCard = null;
        this.hoveredCard = null;
        this.selectedCards.clear();
        AbstractDungeon.topPanel.unhoverHitboxes();
        this.cardSelectAmount = 0;
        this.currentDiffY = 0.0F;
        this.grabStartY = 0.0F;
        this.grabbedScreen = false;
        hideCards();
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NO_INTERACT;
        AbstractDungeon.overlayMenu.showBlackScreen(0.75F);
        this.confirmButton.isDisabled = false;
        this.confirmButton.show();
        this.peekButton.hideInstantly();
        if (this.targetGroup.group.size() <= 5) {
            drawStartY = Settings.HEIGHT * 0.5F;
        } else {
            drawStartY = Settings.HEIGHT * 0.66F;
        }
    }

    public void reopen() {
        AbstractDungeon.overlayMenu.showBlackScreen(0.75F);
        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.GRID;
        AbstractDungeon.topPanel.unhoverHitboxes();
        if (this.cancelWasOn && !this.isJustForConfirming && this.canCancel) {
            this.cancelButton.show(this.cancelText);
        }
        for (AbstractCard c : this.targetGroup.group) {
            c.targetDrawScale = 0.75F;
            c.drawScale = 0.75F;
            c.lighten(false);
        }
        this.scrollBar.reset();
    }

    public void hide() {
        if (!this.cancelButton.isHidden) {
            this.cancelWasOn = true;
            this.cancelText = this.cancelButton.buttonText;
        }
    }
    public void close() {

        if (this.currentMode == CardDisplayMode.UPGRADE && this.cardModScreen != null && this.cardModScreen.isOpen)
            this.cardModScreen.close();

        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        this.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        this.targetGroup.group.forEach(c -> c.isGlowing = false);

        show = false;

        caller.setIsSelectionScreenUp(false);
        AbstractSelectScreen.showLoadoutRelics();

        Configurator.setLevel(TipHelper.class.getName(),Level.INFO);
        Configurator.setLevel(LoadoutMod.class.getName(),Level.INFO);
    }
    private void updateScrolling() {
        if (PeekButton.isPeeking) {
            return;
        }


        if (this.isJustForConfirming && this.targetGroup.size() <= 5) {
            this.currentDiffY = -64.0F * Settings.scale;

            return;
        }
        int y = InputHelper.mY;
        boolean isDraggingScrollBar = this.scrollBar.update();

        if (!isDraggingScrollBar) {
            if (!this.grabbedScreen) {
                if (InputHelper.scrolledDown) {
                    this.currentDiffY += Settings.SCROLL_SPEED;
                } else if (InputHelper.scrolledUp) {
                    this.currentDiffY -= Settings.SCROLL_SPEED;
                }

                if (InputHelper.justClickedLeft) {
                    this.grabbedScreen = true;
                    this.grabStartY = y - this.currentDiffY;
                }

            } else if (InputHelper.isMouseDown) {
                this.currentDiffY = y - this.grabStartY;
            } else {
                this.grabbedScreen = false;
            }
        }


        if (this.prevDeckSize != this.targetGroup.size()) {
            calculateScrollBounds();
        }
        resetScrolling();
        updateBarPosition();
    }




    private void calculateScrollBounds() {
        int scrollTmp = 0;
        if (this.targetGroup.size() > 10) {
            scrollTmp = this.targetGroup.size() / 5 - 2;
            if (this.targetGroup.size() % 5 != 0) {
                scrollTmp++;
            }
            this.scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT + scrollTmp * padY;
        } else {
            this.scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT;
        }

        this.prevDeckSize = this.targetGroup.size();
    }




    private void resetScrolling() {
        if (this.currentDiffY < this.scrollLowerBound) {
            this.currentDiffY = MathHelper.scrollSnapLerpSpeed(this.currentDiffY, this.scrollLowerBound);
        } else if (this.currentDiffY > this.scrollUpperBound) {
            this.currentDiffY = MathHelper.scrollSnapLerpSpeed(this.currentDiffY, this.scrollUpperBound);
        }
    }

    private void hideCards() {
        int lineNum = 0;
        ArrayList<AbstractCard> cards = this.targetGroup.group;
        for (int i = 0; i < cards.size(); i++) {
            ((AbstractCard)cards.get(i)).setAngle(0.0F, true);
            int mod = i % 5;
            if (mod == 0 && i != 0) {
                lineNum++;
            }

            ((AbstractCard)cards.get(i)).lighten(true);
            ((AbstractCard)cards.get(i)).current_x = drawStartX + mod * padX;
            ((AbstractCard)cards.get(i)).current_y = drawStartY + this.currentDiffY - lineNum * padY - MathUtils.random(100.0F * Settings.scale, 200.0F * Settings.scale);


            ((AbstractCard)cards.get(i)).targetDrawScale = 0.75F;
            ((AbstractCard)cards.get(i)).drawScale = 0.75F;
        }
    }

    public void cancelUpgrade() {
        this.cardSelectAmount = 0;
        this.confirmScreenUp = false;
        this.confirmButton.hide();
        this.confirmButton.isDisabled = true;
        this.hoveredCard = null;
        this.upgradePreviewCard = null;

        if (Settings.isControllerMode && this.controllerCard != null) {
            this.hoveredCard = this.controllerCard;
            CInputHelper.setCursor(this.hoveredCard.hb);
        }

        if ((this.forUpgrade || this.forTransform || this.forPurge || AbstractDungeon.previousScreen == AbstractDungeon.CurrentScreen.SHOP) && this.canCancel)
        {
            this.cancelButton.show(TEXT[1]);
        }


        int lineNum = 0;
        ArrayList<AbstractCard> cards = this.targetGroup.group;
        for (int i = 0; i < cards.size(); i++) {
            int mod = i % 5;
            if (mod == 0 && i != 0) {
                lineNum++;
            }
            ((AbstractCard)cards.get(i)).current_x = drawStartX + mod * padX;
            ((AbstractCard)cards.get(i)).current_y = drawStartY + this.currentDiffY - lineNum * padY;
        }

        this.tipMsg = this.lastTip;
    }

    public void render(SpriteBatch sb) {

        if(LoadoutMod.FABRICATE_MOD_LOADED){
            if(FabricateScreenController.isScreenUp) {
                FabricateScreenController.render(sb);
                return;
            }
        }


        if (currentMode == CardDisplayMode.UPGRADE) {
            if (this.cardModScreen != null && this.cardModScreen.isOpen) {
                this.cardModScreen.render(sb);
                return;
            }
        }

        if (shouldShowScrollBar()) {
            this.scrollBar.render(sb);
        }
        if(currentMode == CardDisplayMode.OBTAIN)
            this.cardSelectSortHeader.render(sb);
        else if (currentMode == CardDisplayMode.DELETE || currentMode == CardDisplayMode.UPGRADE) {
            if(AbstractDungeon.getCurrRoom() !=null && AbstractDungeon.getCurrRoom().phase== AbstractRoom.RoomPhase.COMBAT)
                this.combatCardSelectSortHeader.render(sb);
        }



        if (this.hoveredCard != null) {
            if (AbstractDungeon.getCurrRoom() !=null && (AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT) {
                this.targetGroup.renderExceptOneCard(sb, this.hoveredCard);
            } else {
                this.targetGroup.renderExceptOneCardShowBottled(sb, this.hoveredCard);
            }
            if(!branchRenderPanel.shown) {
                this.hoveredCard.renderHoverShadow(sb);
                this.hoveredCard.renderInLibrary(sb);
                this.hoveredCard.renderCardTip(sb);
            }



            if (AbstractDungeon.getCurrRoom() !=null && (AbstractDungeon.getCurrRoom()).phase != AbstractRoom.RoomPhase.COMBAT) {
                if (this.hoveredCard.inBottleFlame) {
                    AbstractRelic tmp = RelicLibrary.getRelic("Bottled Flame");
                    float prevX = tmp.currentX;
                    float prevY = tmp.currentY;
                    tmp.currentX = this.hoveredCard.current_x + 130.0F * Settings.scale;
                    tmp.currentY = this.hoveredCard.current_y + 182.0F * Settings.scale;
                    tmp.scale = this.hoveredCard.drawScale * Settings.scale * 1.5F;
                    tmp.render(sb);
                    tmp.currentX = prevX;
                    tmp.currentY = prevY;
                    tmp = null;
                } else if (this.hoveredCard.inBottleLightning) {
                    AbstractRelic tmp = RelicLibrary.getRelic("Bottled Lightning");
                    float prevX = tmp.currentX;
                    float prevY = tmp.currentY;
                    tmp.currentX = this.hoveredCard.current_x + 130.0F * Settings.scale;
                    tmp.currentY = this.hoveredCard.current_y + 182.0F * Settings.scale;
                    tmp.scale = this.hoveredCard.drawScale * Settings.scale * 1.5F;
                    tmp.render(sb);
                    tmp.currentX = prevX;
                    tmp.currentY = prevY;
                    tmp = null;
                } else if (this.hoveredCard.inBottleTornado) {
                    AbstractRelic tmp = RelicLibrary.getRelic("Bottled Tornado");
                    float prevX = tmp.currentX;
                    float prevY = tmp.currentY;
                    tmp.currentX = this.hoveredCard.current_x + 130.0F * Settings.scale;
                    tmp.currentY = this.hoveredCard.current_y + 182.0F * Settings.scale;
                    tmp.scale = this.hoveredCard.drawScale * Settings.scale * 1.5F;
                    tmp.render(sb);
                    tmp.currentX = prevX;
                    tmp.currentY = prevY;
                    tmp = null;
                }
            }

        } else {
            if(this.targetGroup != null) {
//                //switch (currentPool) {
//
//                    case EXTERNAL:
//                        this.targetGroup.render(sb);
//                        break;
//                    case MASTER_DECK:
//                        this.targetGroup.renderMasterDeck(sb);
//                        break;
//                    case DRAW:
//                        this.targetGroup.renderShowBottled(sb);
//                        break;
//                    case DISCARD:
//                        this.targetGroup.renderDiscardPile(sb);
//                        break;
//                    case HAND:
//                        this.targetGroup.renderHand(sb,null);
//                        break;
//                    case EXHAUST:
//                    default:
//                        this.targetGroup.render(sb);
//                        break;
//
//                }

                this.targetGroup.renderInLibrary(sb);
                this.targetGroup.renderTip(sb);
            }

        }




        if (this.confirmScreenUp) {
            sb.setColor(new Color(0.0F, 0.0F, 0.0F, 0.8F));
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0.0F, 0.0F, Settings.WIDTH, Settings.HEIGHT - 64.0F * Settings.scale);

            if (this.forTransform || this.forUpgrade) {
                renderArrows(sb);

                this.hoveredCard.current_x = Settings.WIDTH * 0.36F;
                this.hoveredCard.current_y = Settings.HEIGHT / 2.0F;
                this.hoveredCard.target_x = Settings.WIDTH * 0.36F;
                this.hoveredCard.target_y = Settings.HEIGHT / 2.0F;
                this.hoveredCard.render(sb);
                this.hoveredCard.updateHoverLogic();
                this.hoveredCard.renderCardTip(sb);


                this.upgradePreviewCard.current_x = Settings.WIDTH * 0.63F;
                this.upgradePreviewCard.current_y = Settings.HEIGHT / 2.0F;
                this.upgradePreviewCard.target_x = Settings.WIDTH * 0.63F;
                this.upgradePreviewCard.target_y = Settings.HEIGHT / 2.0F;
                this.upgradePreviewCard.render(sb);
                this.upgradePreviewCard.updateHoverLogic();
                this.upgradePreviewCard.renderCardTip(sb);
            }
            else {

                this.hoveredCard.current_x = Settings.WIDTH / 2.0F;
                this.hoveredCard.current_y = Settings.HEIGHT / 2.0F;
                this.hoveredCard.render(sb);
                this.hoveredCard.updateHoverLogic();
            }
        }


        this.confirmButton.render(sb);
        this.cancelButton.render(sb);

        if (this.currentMode == CardDisplayMode.UPGRADE && this.branchRenderPanel != null && this.branchRenderPanel.shown) {
            this.branchRenderPanel.render(sb);
        }

        //this.peekButton.render(sb);

        if ((!this.isJustForConfirming || this.targetGroup.size() > 5)) {
            FontHelper.renderDeckViewTip(sb, this.tipMsg, 96.0F * Settings.scale, Settings.CREAM_COLOR);
        }


    }


    private void renderArrows(SpriteBatch sb) {
        float x = Settings.WIDTH / 2.0F - 73.0F * Settings.scale - 32.0F;
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.UPGRADE_ARROW, x, Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale1 * Settings.scale, this.arrowScale1 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        x += 64.0F * Settings.scale;
        sb.setColor(Color.WHITE);
        sb.draw(ImageMaster.UPGRADE_ARROW, x, Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale2 * Settings.scale, this.arrowScale2 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
        x += 64.0F * Settings.scale;
        sb.draw(ImageMaster.UPGRADE_ARROW, x, Settings.HEIGHT / 2.0F - 32.0F, 32.0F, 32.0F, 64.0F, 64.0F, this.arrowScale3 * Settings.scale, this.arrowScale3 * Settings.scale, 0.0F, 0, 0, 64, 64, false, false);

        this.arrowTimer += Gdx.graphics.getDeltaTime() * 2.0F;
        this.arrowScale1 = 0.8F + (MathUtils.cos(this.arrowTimer) + 1.0F) / 8.0F;
        this.arrowScale2 = 0.8F + (MathUtils.cos(this.arrowTimer - 0.8F) + 1.0F) / 8.0F;
        this.arrowScale3 = 0.8F + (MathUtils.cos(this.arrowTimer - 1.6F) + 1.0F) / 8.0F;
    }


    public void scrolledUsingBar(float newPercent) {
        this.currentDiffY = MathHelper.valueFromPercentBetween(this.scrollLowerBound, this.scrollUpperBound, newPercent);
        updateBarPosition();
    }

    private void updateBarPosition() {
        float percent = MathHelper.percentFromValueBetween(this.scrollLowerBound, this.scrollUpperBound, this.currentDiffY);
        this.scrollBar.parentScrolledToPercent(percent);
    }

    private boolean shouldShowScrollBar() {
        return (!this.confirmScreenUp && this.scrollUpperBound > SCROLL_BAR_THRESHOLD && !PeekButton.isPeeking);
    }

    public SCardViewPopup getSCardPopup() {
        return this.cardModScreen;
    }
}


