package loadout.screens;

import basemod.interfaces.TextReceiver;
import basemod.patches.com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor.TextInput;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.steamInput.SteamInputHelper;
import com.megacrit.cardcrawl.localization.KeywordStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.options.DropdownMenuListener;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static loadout.LoadoutMod.*;

public class CardSelectSortHeader implements HeaderButtonPlusListener, DropdownMenuListener, TextReceiver {
    private static final UIStrings cUIStrings = CardCrawlGame.languagePack.getUIString("CardLibraryScreen");
    public static final String[] cTEXT = cUIStrings.TEXT;
    private static final UIStrings clUIStrings = CardCrawlGame.languagePack.getUIString("CardLibSortHeader");
    public static final String[] clTEXT = clUIStrings.TEXT;
    private static final UIStrings rhUIStrings = CardCrawlGame.languagePack.getUIString("RunHistoryScreen");
    public static final String[] rhTEXT = rhUIStrings.TEXT;

    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] rTEXT = rUiStrings.TEXT;

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardSelectSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;

    private static final UIStrings sUiStrings = CardCrawlGame.languagePack.getUIString("SingleCardViewPopup");
    public static final String[] sTEXT = sUiStrings.TEXT;

    private static final KeywordStrings kKeyString = CardCrawlGame.languagePack.getKeywordString("Game Dictionary");
    public static final String[] kTEXT = kKeyString.UNPLAYABLE.NAMES;

    public boolean justSorted = false;

    public float startX = 650.0F * Settings.xScale;
    public static final float SPACE_X = 226.0F * Settings.xScale;
    private static final float START_Y = Settings.HEIGHT - 200.0F * Settings.yScale;
    public static final float SPACE_Y = 75.0F * Settings.yScale;

    private final HeaderButtonPlus rarityButton;
    private final HeaderButtonPlus modButton;
    private final HeaderButtonPlus nameButton;
    private final HeaderButtonPlus colorButton;

    private HeaderButtonPlus fabricateButton;
    private final HeaderButtonPlus upgradeButton;

    private final DropdownMenu colorFilterDropdown;
    private final DropdownMenu selectionModeButton;
    private final DropdownMenu typeFilterDropdown;
    private final DropdownMenu costFilterDropdown;
    private final DropdownMenu rarityFilterDropdown;
    private final DropdownMenu modNameDropdown;


    private String[] dropdownMenuHeaders;
    public HeaderButtonPlus[] buttons;
    public DropdownMenu[] dropdownMenus;
    public int selectionIndex = -1;

    private static Texture img;
    private Color selectionColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);

    public GCardSelectScreen cardSelectScreen;
    private List<AbstractCard.CardColor> cardColors;
    private ArrayList<String> cardMods;
    private HashMap<String,String> cardModNames;

    public boolean isTyping = false;

    public float waitTimer = 0.0F;
    public String filterTextPlaceholder = TEXT[3];
    public String filterText = "";
    public Hitbox filterTextHb;

    public float filterBarY = START_Y - 9.0f * 52.0f * Settings.yScale;

    public float filterBarX = 0.0f;

    public Texture filterTextBoxImg = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");
    private Color highlightBoxColor = new Color(1.0F, 0.95F, 0.5F, 0.0F);


    public CardSelectSortHeader(GCardSelectScreen cardSelectScreen, float startX) {
        if (img == null)
            img = ImageMaster.loadImage("images/ui/cardlibrary/selectBox.png");

        this.startX = startX;
        float xPosition = this.startX;
        float yPosition = START_Y - 450.0f * Settings.yScale;
        this.colorButton = new HeaderButtonPlus(rTEXT[0], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.rarityButton = new HeaderButtonPlus(rTEXT[1], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.nameButton = new HeaderButtonPlus(rTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(rTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.upgradeButton = new HeaderButtonPlus(cTEXT[7], xPosition, yPosition, this, false ,true, HeaderButtonPlus.Alignment.RIGHT);

        List<HeaderButtonPlus> bList = new LinkedList<>();
        bList.add(this.colorButton);
        bList.add(this.rarityButton);
        bList.add(this.nameButton);
        bList.add(this.modButton);
        bList.add(this.upgradeButton);

        if(FABRICATE_MOD_LOADED) {
            this.fabricateButton =  new HeaderButtonPlus(TEXT[5], filterBarX + 75.0f * Settings.scale, filterBarY - 100.0f * Settings.scale, this, true, ImageMaster.REWARD_CARD_NORMAL);
            bList.add(this.fabricateButton);
        }

        this.buttons = bList.toArray(new HeaderButtonPlus[0]);

        //ArrayList<String> a = RelicClassComparator.classList.stream().map(RelicClassComparator::getCharacterNameByColor).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> a = new ArrayList<>();
        cardColors = allCharacters.stream().map(AbstractPlayer::getCardColor).collect(Collectors.toCollection(ArrayList::new));

//        for (AbstractCard.CardColor cc : cardColors) {
//
//            a.add(toCharacterNameByColor(cc));
//        }
        a.add(0,TEXT[0]);
        //int i = 1;
        int selectedClass;
        for (AbstractPlayer ap : allCharacters) {
            a.add(ap.getLoadout().name);
            //i++;
        }
        //Colorless
        a.add(cTEXT[4]);
        //Curse
        a.add(StringUtils.capitalize(GameDictionary.CURSE.NAMES[0]));


        this.colorFilterDropdown = new DropdownMenu(this,a,FontHelper.panelNameFont, Settings.CREAM_COLOR);
        //this.colorFilterDropdown.setSelectedIndex(i);

        String[] b = new String[2];
        b[0]= rTEXT[8];
        b[1]= rTEXT[9];
        this.selectionModeButton = new DropdownMenu(this,b,FontHelper.panelNameFont, Settings.CREAM_COLOR);
        this.selectionModeButton.setSelectedIndex(LoadoutMod.enableDrag ? 0 : 1);

        ArrayList<String> c = new ArrayList<>();
        c.add(0,TEXT[0]);
        for (AbstractCard.CardType ct : AbstractCard.CardType.values())
            c.add(toLocalCardTypeStrings(ct));
        this.typeFilterDropdown = new DropdownMenu(this, c,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        ArrayList<String> d = new ArrayList<>();
        for (int i = -2; i<5; i++) {
            if (i==-2)
                d.add(StringUtils.capitalize(kTEXT[0]));
            else if (i==-1) {
                d.add("X");
            } else if (i==4) {
                d.add("4+");
            }else {
                d.add(String.valueOf(i));
            }

        }
        d.add(0,TEXT[0]);
        this.costFilterDropdown = new DropdownMenu(this,d,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        ArrayList<String> e = new ArrayList<>();
        for (AbstractCard.CardRarity cr : AbstractCard.CardRarity.values()) {
            e.add(toLocalRarity(cr));
        }
        e.add(0,TEXT[0]);
        this.rarityFilterDropdown = new DropdownMenu(this,e,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        cardMods = new ArrayList<>(findCardAddingMods());
        cardMods.sort(Comparator.naturalOrder());

        ArrayList<String> f = new ArrayList<>();
        String fName;
        for (String id : cardMods) {
            fName = cardModNames.get(id);
            if (fName.length() >= 12) fName = fName.substring(0,11) + "...";
            f.add(fName);
        }


        f.add(0,TEXT[0]);
        f.add("Slay the Spire");
        this.modNameDropdown = new DropdownMenu(this, f,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.dropdownMenus = new DropdownMenu[] {this.selectionModeButton,this.rarityFilterDropdown, this.typeFilterDropdown, this.costFilterDropdown,this.colorFilterDropdown, this.modNameDropdown};
        this.dropdownMenuHeaders = new String[] {TEXT[1],clTEXT[0],clTEXT[1],clTEXT[3],rTEXT[0],"Mod"};
        this.cardSelectScreen = cardSelectScreen;


        filterTextHb = new Hitbox(filterBarX,filterBarY - 25.0F * Settings.yScale, 250.0F * Settings.scale, 50.0F * Settings.yScale);

    }

    public float getFilterTextWidth() {
        return FontHelper.getSmartWidth(FontHelper.panelNameFont,this.filterText,250.0F,20.0F);
    }

    public void setToCurrentClass() {
        this.colorFilterDropdown.setSelectedIndex(allCharacters.indexOf(AbstractDungeon.player)+1);
    }

    private HashSet<String> findCardAddingMods() {
        HashSet<String> modIDs = new HashSet<>();
        cardModNames = new HashMap<>();
        for (AbstractCard ac : CardLibrary.getAllCards()) {
            String modID = WhatMod.findModID(ac.getClass());

            if (modID == null) continue;
            if (modIDs.contains(modID)) {
                continue;
            } else {
                modIDs.add(modID);
                cardModNames.put(modID,WhatMod.findModName(ac.getClass()));
            }
        }
        return modIDs;
    }

    public static String toLocalCardTypeStrings(AbstractCard.CardType ct) {
        String type = ct.toString();
        switch (type) {
            case "ATTACK":
                return sTEXT[0];
            case "SKILL":
                return sTEXT[1];
            case "POWER":
                return sTEXT[2];
            case "CURSE":
                return sTEXT[3];
            case "STATUS":
                return sTEXT[7];
            default:
                return type;
        }
    }

    public static String toLocalRarity(AbstractCard.CardRarity cr) {
        switch (cr) {
            case BASIC:
                return rhTEXT[11];
            case SPECIAL:
                return rhTEXT[15];
            case COMMON:
                return rhTEXT[12];
            case UNCOMMON:
                return rhTEXT[13];
            case RARE:
                return rhTEXT[14];
            case CURSE:
                return rhTEXT[16];
            default:
                return cr.name();
        }
    }

    private String toCharacterNameByColor(AbstractCard.CardColor cc) {
        for (AbstractPlayer player : LoadoutMod.allCharacters) {
            if (player.getCardColor().equals(cc)) {
                return player.getLocalizedCharacterName();
            }
        }
        return cc.toString();
    }

    public void update() {
        if (this.isTyping && Gdx.input.isKeyPressed(67) && !this.filterText.equals("") && this.waitTimer <= 0.0F) {

            this.filterText = this.filterText.substring(0, this.filterText.length() - 1);
            this.waitTimer = 0.05F;
        }

        if (this.waitTimer > 0.0F) {
            this.waitTimer -= Gdx.graphics.getDeltaTime();
        }
        //filterTextHb.resize();



        for (HeaderButtonPlus button : this.buttons) {
            button.update();
        }
        for (DropdownMenu dropdownMenu : this.dropdownMenus) {
            if (dropdownMenu.isOpen) {
                dropdownMenu.update();
                return;
            }
        }

        filterTextHb.update();

        for (DropdownMenu dropdownMenu : this.dropdownMenus)
            dropdownMenu.update();


        if (this.filterTextHb.justHovered) {
            CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
        }

        if (this.filterTextHb.hovered && InputHelper.justClickedLeft) {
            this.filterTextHb.clickStarted = true;
        }
        if(!this.filterTextHb.hovered && (InputHelper.justClickedLeft || InputHelper.justClickedRight) && this.isTyping) {
            //cancel typing
            stopTyping();
            this.cardSelectScreen.updateFilters();
        }
        if (this.filterTextHb.clicked || this.filterTextHb.hovered && CInputActionSet.select.isJustPressed()) {
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            this.filterTextHb.clicked = false;

            if(!isTyping) {
                this.isTyping = true;

                this.filterText = "";

                //Gdx.input.setInputProcessor(new TextInputHelper(this, false));
                TextInput.startTextReceiver(this);
                if (SteamInputHelper.numControllers == 1 && CardCrawlGame.clientUtils != null && CardCrawlGame.clientUtils.isSteamRunningOnSteamDeck()) {
                    CardCrawlGame.clientUtils.showFloatingGamepadTextInput(SteamUtils.FloatingGamepadTextInputMode.ModeSingleLine, 0, 0, Settings.WIDTH, (int)(Settings.HEIGHT * 0.25F));
                }
            }





        }

        if(this.isTyping) {
            if (Gdx.input.isKeyJustPressed(66)) {

                stopTyping();
                this.cardSelectScreen.updateFilters();
            }
            else if (InputHelper.pressedEscape) {
                InputHelper.pressedEscape = false;

                stopTyping();
            }
        }
    }

    public void stopTyping() {
        this.isTyping = false;
        //Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
        TextInput.stopTextReceiver(this);
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
            if ( button == this.upgradeButton) {
                button.setActive(cardSelectScreen.filterUpgraded);
            } else {button.setActive(false);}
        }
    }

    public void resetOtherButtons() {
        int btnIdx = getHoveredIndex();
        for (int i = 0;i<this.buttons.length;i++) {
            if (i!= btnIdx) {
                HeaderButtonPlus button = buttons[i];
                if ( button == this.upgradeButton) {
                    button.setActive(cardSelectScreen.filterUpgraded);
                } else {
                    button.reset();
                }
            }
        }
    }
    public void resetAllButtons() {
        for (int i = 0;i<this.buttons.length;i++) {
            HeaderButtonPlus button = buttons[i];
            if ( button == this.upgradeButton) {
                button.setActive(cardSelectScreen.filterUpgraded);
            } else {
                button.reset();
            }
        }
        for (DropdownMenu ddm : dropdownMenus) {
            if (ddm != this.selectionModeButton) {
                ddm.setSelectedIndex(0);
            } else {
                this.selectionModeButton.setSelectedIndex(LoadoutMod.enableDrag ? 0 : 1);
            }
        }

        this.filterText = "";
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.rarityButton) {
            clearActiveButtons();
            this.cardSelectScreen.sortByRarity(isAscending);
            resetOtherButtons();
        } else if (button == this.colorButton) {
            clearActiveButtons();
            this.cardSelectScreen.sortByColor(isAscending);
            resetOtherButtons();
        } else if (button == this.nameButton) {
            clearActiveButtons();
            this.cardSelectScreen.sortByName(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            this.cardSelectScreen.sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.upgradeButton) {
            clearActiveButtons();
            this.cardSelectScreen.filterUpgraded = isAscending;
            this.cardSelectScreen.updateFilters();
            resetOtherButtons();
            return;
        } else if (button == this.fabricateButton) {
            launchFabricateScreen();
            return;
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);

    }

    private void launchFabricateScreen() {

    }

    public void render(SpriteBatch sb) {
        //sb.draw(ImageMaster.COLOR_TAB_BAR, 10.0F, -50.0F, 300.0F, 500.0F, 0, 0, 1334, 102, false, false);
        updateScrollPositions();
        if (cardSelectScreen.currentMode == GCardSelectScreen.CardDisplayMode.OBTAIN) {



            filterTextHb.render(sb);
            this.highlightBoxColor.a = isTyping ? 0.7F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L)) / 5.0F : 1.0F;
            sb.setColor(this.highlightBoxColor);
            float doop = this.filterTextHb.hovered ? 1.0F + (1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 2L % 360L))) / 50.0F : 1.0F ;
            //float doop = 1.0F;
            sb.draw(this.filterTextBoxImg, this.filterBarX - 50.0F, this.filterBarY - 50.0F, 100.0F, 43.0F, 250.0F, 86.0F, Settings.scale * doop * this.filterTextHb.width / 150.0F / Settings.scale, Settings.yScale * doop, 0.0F, 0, 0, 200, 86, false, false);
            String renderFilterText = filterText.equals("") ? filterTextPlaceholder : filterText;
            Color filterTextColor = isTyping ? Color.CYAN : Settings.GOLD_COLOR;
            FontHelper.renderSmartText(sb, FontHelper.panelNameFont, renderFilterText, filterBarX, filterBarY, 250.0F, 20.0F, filterTextColor);
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, TEXT[4], filterBarX, filterBarY + 35.0F * Settings.yScale, 250.0F, 20.0F, Settings.GOLD_COLOR);
            renderButtons(sb);
            renderSelection(sb);
        }

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


        //this.selectionModeButton.render(sb,xPos,yPos);
        //yPos += spaceY;
        //FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, rTEXT[10], xPos, yPos, 200.0F, 20.0F, Settings.GOLD_COLOR);


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
        if(dropdownMenu == this.colorFilterDropdown) {
            if (i==0) {
                //if showing all
                cardSelectScreen.filterColor = null;
            } else if (i==(cardColors.size()+3)-2) {
                cardSelectScreen.filterColor = AbstractCard.CardColor.COLORLESS;
            } else if (i==(cardColors.size()+3)-1) {
                cardSelectScreen.filterColor = AbstractCard.CardColor.CURSE;
            } else {
                cardSelectScreen.filterColor = cardColors.get(i-1);
            }
            cardSelectScreen.updateFilters();
        }
        if(dropdownMenu == this.typeFilterDropdown) {
            if(i==0) {
                cardSelectScreen.filterType = null;

            } else {
                cardSelectScreen.filterType = AbstractCard.CardType.values()[i-1];
            }
            cardSelectScreen.updateFilters();
        }
        if (dropdownMenu == this.costFilterDropdown) {
            if (i==0)
                cardSelectScreen.filterCost = -99;
            else {
                cardSelectScreen.filterCost = i-3;
            }
            cardSelectScreen.updateFilters();
        }
        if (dropdownMenu == this.rarityFilterDropdown) {
            if (i==0)
                cardSelectScreen.filterRarity = null;
            else {
                cardSelectScreen.filterRarity = AbstractCard.CardRarity.values()[i-1];
            }
            cardSelectScreen.updateFilters();
        }
        if (dropdownMenu == this.modNameDropdown) {
            if (i==0) {
                //if showing all
                cardSelectScreen.filterMod = null;
            } else if (i==(cardMods.size()+2)-1) {
                cardSelectScreen.filterMod = "Slay the Spire";
            } else {
                cardSelectScreen.filterMod = cardMods.get(i-1);
            }
            cardSelectScreen.updateFilters();
        }
        if (dropdownMenu == this.selectionModeButton) {
            LoadoutMod.enableDrag = i == 0;
            try {
                LoadoutMod.config.setBool(ENABLE_DRAG_SELECT, enableDrag);
                LoadoutMod.config.save();
            } catch (NullPointerException nPE) {
                logger.debug("null pointer exception caught, caused by selection mode dropdown");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public String getCurrentText() {
        return this.filterText;
    }

    @Override
    public void setText(String s) {
        this.filterText = s;
    }

    @Override
    public boolean isDone() {
        return !isTyping;
    }

    @Override
    public boolean acceptCharacter(char c) {
        return Character.isDigit(c) || Character.isLetter(c) || (c >=32 && c<=126);

    }

    @Override
    public boolean onPushBackspace() {
        if(this.waitTimer <= 0.0F) {
            this.waitTimer = 0.09F;
            return false;
        }
        return true;
    }

    //    @Override
//    public void setTextField(String textToSet) {
//        this.filterText = textToSet;
//    }
//
//    @Override
//    public String getTextField() {
//        return this.filterText;
//    }

}
