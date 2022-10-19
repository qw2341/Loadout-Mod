package loadout.screens;

import basemod.ReflectionHacks;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.cards.colorless.Madness;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Mnemonic;
import loadout.LoadoutMod;
import loadout.helper.PotionModComparator;
import loadout.helper.PotionNameComparator;
import loadout.relics.PowerGiver;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PowerSelectScreen implements ScrollBarListener
{
    public static AbstractCreature dummyCreature;

    static {
        try {
            dummyCreature = Ironclad.class.getDeclaredConstructor(String.class).newInstance("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AbstractCard dummyCard = new Madness();

    public class PowerButton {

        public Class<? extends AbstractPower> pClass;
        public AbstractPower instance;
        public String id;
        public PowerStrings powerStrings;
        public String name;
        public AbstractPower.PowerType type;
        public String modID;
        public String[] desc;
        public int amount;
        public Hitbox hb;
        public float x;
        public float y;
        public ArrayList<PowerTip> tips;
        public TextureAtlas.AtlasRegion region48;
        public TextureAtlas.AtlasRegion region128;

        public PowerButton(Class<? extends AbstractPower> pClass) {
            this.pClass = pClass;
            Constructor<?>[] con = pClass.getDeclaredConstructors();
            this.tips = new ArrayList<>();
            try {
                int paramCt = con[0].getParameterCount();
                Class[] params = con[0].getParameterTypes();
                Object[] paramz = new Object[paramCt];

                for (int i = 0 ; i< paramCt; i++) {
                    Class param = params[i];
                    if (AbstractCreature.class.isAssignableFrom(param)) {
                        paramz[i] = dummyCreature;
                    } else if (int.class.isAssignableFrom(param)) {
                        paramz[i] = 0;
                    } else if (AbstractCard.class.isAssignableFrom(param)) {
                        paramz[i] = dummyCard;
                    } else if (boolean.class.isAssignableFrom(param)) {
                        paramz[i] = true;
                    }
                }
                LoadoutMod.logger.info("Class: " + pClass.getName() + " with parameter: " + Arrays.toString(paramz));

                this.instance = (AbstractPower) con[0].newInstance(paramz);

                this.id = (String) pClass.getField("POWER_ID").get(null);
                this.powerStrings = ReflectionHacks.getPrivateStatic(pClass,"powerStrings");
                //this.name = instance.name;
                this.name = powerStrings.NAME;
                this.desc = powerStrings.DESCRIPTIONS;
                this.modID = WhatMod.findModID(pClass);
                if (this.modID == null) this.modID = "Slay the Spire";

                this.region48 = this.instance.region48;
                this.region128 = this.instance.region128;

                //TextureAtlas ta = (TextureAtlas) pClass.getField("atlas").get(null);
//                CtClass cc = ClassPool.getDefault().get(pClass.getName());
                //cc.getDeclaredMethod("<init>");
//                ClassFile cf = cc.getClassFile();
//                MethodInfo mi = cf.getMethod("<init>");
//                CodeIterator ci = mi.getCodeAttribute().iterator();
//
//                while (ci.hasNext()) {
//                    int idx = ci.next();
//                    int op = ci.byteAt(idx);
//                    LoadoutMod.logger.info(Mnemonic.OPCODE[op]);
//                }

            } catch (Exception e) {

                LoadoutMod.logger.info("Failed to create power button for: " + pClass.getName() + " with name = "+ this.name + " for mod: "+ this.modID);
                e.printStackTrace();
            }
            if (this.name == null) this.name = "Unnamed Power";
            if(desc != null && desc.length > 0) {
                String fullD = StringUtils.join(desc," ");
//                for (String d : desc)
//                {
//                    if (d != null)
//                        this.tips.add(new PowerTip(this.name, d));
//                }

                this.tips.add(new PowerTip(this.name, fullD, region48));
            }
            this.tips.add(new PowerTip("Mod",this.modID));
            this.hb = new Hitbox(200.0f * Settings.scale,75.0f * Settings.yScale);
            this.amount = 0;
            this.x = 0;
            this.y = 0;
            //this.loadRegion(StringUtils.lowerCase(this.id));
            //this.type = instance.type;
            LoadoutMod.logger.info("Created power button for: " + pClass.getName() + " with name = "+ this.name + " for mod: "+ this.modID);
        }

        public void update() {
            this.hb.update();
        }
        protected void loadRegion(String fileName) {
            this.region48 = AbstractPower.atlas.findRegion("48/" + fileName);
            this.region128 = AbstractPower.atlas.findRegion("128/" + fileName);
        }

        public void render(SpriteBatch sb) {
            if (this.hb != null) {
                this.hb.render(sb);
                float a = (amount == 0 || this.hb.hovered) ? 0.7f : 1.0f;
                if(this.region128 != null) {

                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, a));
                    sb.draw(this.region128, x - (float)this.region128.packedWidth / 2.0F, y - (float)this.region128.packedHeight / 2.0F, (float)this.region128.packedWidth / 2.0F, (float)this.region128.packedHeight / 2.0F, (float)this.region128.packedWidth, (float)this.region128.packedHeight, Settings.scale, Settings.scale, 0.0F);

                } else {
                    if (this.region48 != null) {
                        sb.setColor(new Color(1.0F, 1.0F, 1.0F, a));
                        sb.draw(this.region48, x - (float)this.region48.packedWidth / 2.0F, y - (float)this.region48.packedHeight / 2.0F, (float)this.region48.packedWidth / 2.0F, (float)this.region48.packedHeight / 2.0F, 128.0f, 128.0f, Settings.scale, Settings.scale, 0.0F);

                    }
                }

                if (this.hb.hovered) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                    sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x+40.0F,y-64.0F, 64.0F, 64.0F, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.GOLD_COLOR);
                    sb.setBlendFunction(770, 771);

                    TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
                } else {
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.CREAM_COLOR);
                }
                if (this.amount > 0) {
                    FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.amount), x+30.0f, y-30.0f, 3.0f, Settings.GREEN_TEXT_COLOR);
                } else if (this.amount < 0) {
                    FontHelper.renderFontRightTopAligned(sb, FontHelper.powerAmountFont, Integer.toString(this.amount), x+30.0f, y-30.0f, 3.0f, Settings.RED_TEXT_COLOR);
                }
            }
        }

    }

    private static final UIStrings gUiStrings = CardCrawlGame.languagePack.getUIString("GridCardSelectScreen");
    public static final String[] gTEXT = gUiStrings.TEXT;
    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectionScreen"));
    public static final String[] TEXT = UiStrings.TEXT;


    private static final float SPACE = 85.0F * Settings.scale;
    protected static final float START_X = 300.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 300.0F * Settings.yScale;

    private PowerSelectSortHeader sortHeader;

    protected float scrollY = START_Y;
    private float targetY = this.scrollY;
    private float scrollLowerBound = Settings.HEIGHT - 200.0F * Settings.scale;
    private float scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;//2600.0F * Settings.scale;
    private int scrollTitleCount = 0;
    private int row = 0;
    private int col = 0;
    private static final Color RED_OUTLINE_COLOR = new Color(-10132568);
    private static final Color GREEN_OUTLINE_COLOR = new Color(2147418280);
    private static final Color BLUE_OUTLINE_COLOR = new Color(-2016482392);
    private static final Color PURPLE_OUTLINE_COLOR = Color.PURPLE;
    private static final Color BLACK_OUTLINE_COLOR = new Color(168);

    private static Color GOLD_OUTLINE_COLOR = new Color(-2686721);
    private PowerButton hoveredPower = null;
    private PowerButton clickStartedPower = null;
    private boolean grabbedScreen = false;
    private float grabStartY = 0.0F;
    private ScrollBar scrollBar;
    private Hitbox controllerRelicHb = null;

    private ArrayList<PowerButton> powers;
    private boolean show = false;
    public static int selectMult = 1;
    private ArrayList<PowerButton> selectedPowers = new ArrayList<>();

    private GridSelectConfirmButton confirmButton = new GridSelectConfirmButton(gTEXT[0]);
    private boolean doneSelecting = false;
    public boolean isDeleteMode;

    public enum SortType {TYPE,NAME,MOD};

    public SortType currentSortType = null;
    public enum SortOrder {ASCENDING,DESCENDING};
    public SortOrder currentSortOrder = SortOrder.ASCENDING;

    private static final Comparator<PowerButton> BY_TYPE = Comparator.comparing(p -> p.type);
    private static final Comparator<PowerButton> BY_NAME = Comparator.comparing(p -> p.name);
    private static final Comparator<PowerButton> BY_MOD = Comparator.comparing(p -> {
        String powerModID = WhatMod.findModID(p.getClass());
        return powerModID == null? "Slay the Spire" : powerModID;
    });

    private static final Comparator<PowerButton> BY_ID = Comparator.comparing(p -> p.id);

    private AbstractRelic owner;

    private InputAction shiftKey;
    private InputAction ctrlKey;



    public boolean doneSelecting()
    {
        return doneSelecting;
    }

    public ArrayList<PowerButton> getSelectedPowers()
    {
        ArrayList<PowerButton> ret = new ArrayList<>(selectedPowers);
        selectedPowers.clear();
        return ret;
    }

    public PowerSelectScreen(AbstractRelic owner)
    {
        scrollBar = new ScrollBar(this);
        this.sortHeader = new PowerSelectSortHeader(this);
        this.owner = owner;
        this.shiftKey = new InputAction(Input.Keys.SHIFT_LEFT);
        this.ctrlKey = new InputAction(Input.Keys.CONTROL_LEFT);

        this.powers = new ArrayList<>();

        for (Class<? extends AbstractPower> pClass : LoadoutMod.powersToDisplay) {
            this.powers.add(new PowerButton(pClass));
        }


    }

    private boolean shouldSortById() {
        return Settings.language == Settings.GameLanguage.ZHS || Settings.language == Settings.GameLanguage.ZHT;
    }

    private void sortOnOpen() {
        if(!isDeleteMode) {
            this.sortHeader.justSorted = true;
            sortByMod(true);
            this.sortHeader.resetAllButtons();
            this.sortHeader.clearActiveButtons();
        }
    }
    public void sortByType(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.powers.sort(BY_TYPE.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.powers.sort(BY_TYPE.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.TYPE;
        scrolledUsingBar(0.0F);
    }

    public void sortAlphabetically(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.powers.sort(BY_ID);
            else this.powers.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.powers.sort(BY_ID.reversed());
            else this.powers.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.powers.sort(BY_MOD);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.powers.sort(BY_MOD.reversed());
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    public void open(HashMap<String,Integer> savedPowers)
    {
        if(AbstractDungeon.isScreenUp) {
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.overlayMenu.proceedButton.hide();
            AbstractDungeon.screen = AbstractDungeon.CurrentScreen.NO_INTERACT;
        }

        AbstractDungeon.isScreenUp = true;
        AbstractDungeon.overlayMenu.showBlackScreen(0.5f);

        show = true;
        doneSelecting = false;

        confirmButton.isDisabled = false;
        confirmButton.show();
        controllerRelicHb = null;

        for (PowerButton pb : this.powers) {
            if (savedPowers.containsKey(pb.id)) {
                pb.amount = savedPowers.get(pb.id);
            }
        }

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;
        sortOnOpen();
        calculateScrollBounds();
        selectedPowers.clear();
    }

    public void close()
    {
        AbstractDungeon.screen = AbstractDungeon.CurrentScreen.FTUE;
        confirmButton.isDisabled = true;
        confirmButton.hide();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();
        show = false;
        PowerGiver.isSelectionScreenUp = false;
        if (isDeleteMode) {
            this.powers.clear();
        }
    }

    public void resetPowerAmounts() {
        for (PowerButton pb : this.powers) {
            pb.amount = 0;
        }

        ((PowerGiver)this.owner).savedPowers.clear();
    }

    public boolean isOpen()
    {
        return show;
    }

    private boolean isCombat() {
        return AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    public void addPowerToPlayer(AbstractPower p, int stackAmount) {
        if (isCombat()) {
            try {
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player,AbstractDungeon.player, (AbstractPower) p.getClass().getDeclaredConstructors()[0].newInstance(), stackAmount));
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void update()
    {
        if (!isOpen()) {
            return;
        }
        if (InputHelper.pressedEscape) {
            close();
            InputHelper.pressedEscape = false;
            return;
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            close();
            return;
        }

        updateControllerInput();
        if (Settings.isControllerMode && controllerRelicHb != null) {
            if (Gdx.input.getY() > Settings.HEIGHT * 0.7F) {
                targetY += Settings.SCROLL_SPEED;
                if (targetY > scrollUpperBound) {
                    targetY = scrollUpperBound;
                }
            } else if (Gdx.input.getY() < Settings.HEIGHT * 0.3F) {
                targetY -= Settings.SCROLL_SPEED;
                if (targetY < scrollLowerBound) {
                    targetY = scrollLowerBound;
                }
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

        confirmButton.update();
        this.sortHeader.update();

        if (confirmButton.hb.clicked) {
            CInputActionSet.select.unpress();
            confirmButton.hb.clicked = false;
            doneSelecting = true;
        }
        if (hoveredPower != null) {
            if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
                clickStartedPower = hoveredPower;
                //logger.info("Pressed Left");
            }
            if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredPower == clickStartedPower)
                {

                    clickStartedPower.amount += selectMult;
                    ((PowerGiver)owner).modifyAmount(clickStartedPower.id, +selectMult);
                    if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        ((PowerGiver)owner).applyPowerToPlayer(clickStartedPower.id, +selectMult);
                    }

                    this.owner.flash();
                    clickStartedPower = null;

                    if (doneSelecting()) {
                        close();
                    }
                }
            }

            if (InputHelper.justClickedRight || CInputActionSet.select.isJustPressed()) {
                clickStartedPower = hoveredPower;

            }
            if (InputHelper.justReleasedClickRight || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredPower == clickStartedPower)
                {
                    clickStartedPower.amount -= selectMult;
                    ((PowerGiver)owner).modifyAmount(clickStartedPower.id, -selectMult);

                    if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        ((PowerGiver)owner).applyPowerToPlayer(clickStartedPower.id, -selectMult);
                    }

                    this.owner.flash();

                    clickStartedPower = null;
                }
            }
        } else {
            clickStartedPower = null;
        }
        boolean isScrollingScrollBar = scrollBar.update();
        if (!isScrollingScrollBar) {
            updateScrolling();
        }
        InputHelper.justClickedLeft = false;
        InputHelper.justClickedRight = false;

        hoveredPower = null;
        updateList(powers);
        if (Settings.isControllerMode && controllerRelicHb != null) {
            Gdx.input.setCursorPosition((int)controllerRelicHb.cX, (int)(Settings.HEIGHT - controllerRelicHb.cY));
        }
        if(doneSelecting) close();
    }

    private void updateControllerInput()
    {
        // TODO
    }

    private void updateScrolling()
    {
        int y = InputHelper.mY;
        if (!grabbedScreen)
        {
            if (InputHelper.scrolledDown) {
                targetY += Settings.SCROLL_SPEED;
            } else if (InputHelper.scrolledUp) {
                targetY -= Settings.SCROLL_SPEED;
            }
            if (InputHelper.justClickedLeft)
            {
                grabbedScreen = true;
                grabStartY = (y - targetY);
            }
        }
        else if (InputHelper.isMouseDown)
        {
            targetY = (y - grabStartY);
        }
        else
        {
            grabbedScreen = false;
        }
        scrollY = MathHelper.scrollSnapLerpSpeed(scrollY, targetY);
        resetScrolling();
        updateBarPosition();
    }

    private void calculateScrollBounds()
    {
        int size = powers.size();

        int scrollTmp = 0;
        if (size > 5) {
            scrollTmp = size / 5;
            scrollTmp += 5;
            if (size % 5 != 0) {
                ++scrollTmp;
            }
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT + (scrollTmp + scrollTitleCount) * 75.0f * Settings.scale;
        } else {
            scrollUpperBound = scrollLowerBound + Settings.DEFAULT_SCROLL_LIMIT;
        }
    }

    private void resetScrolling()
    {
        if (targetY < scrollLowerBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollLowerBound);
        } else if (targetY > scrollUpperBound) {
            targetY = MathHelper.scrollSnapLerpSpeed(targetY, scrollUpperBound);
        }
    }

    private void updateList(ArrayList<PowerButton> list)
    {
        for (PowerButton p : list)
        {
            p.update();
            p.hb.move(p.x  + 150.0f, p.y);

            if (p.hb.hovered)
            {
                hoveredPower = p;
            }
        }
    }

    public void render(SpriteBatch sb)
    {
        if (!isOpen()) {
            return;
        }

        row = -1;
        col = 0;
        renderList(sb, powers);

        scrollBar.render(sb);
        confirmButton.render(sb);
        if (!isDeleteMode)
            sortHeader.render(sb);
    }

    private void renderList(SpriteBatch sb, ArrayList<PowerButton> list)
    {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;
        AbstractPotion.PotionRarity prevRarity = null;
        int prevType = -1;
        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;
        boolean isRelicLocked = false;
        Color outlineColor;

        if(isDeleteMode) {
            FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, TEXT[7], START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * (this.row-1), 99999.0F, 0.0F, Settings.GOLD_COLOR);
        }

        for (Iterator<PowerButton> it = list.iterator(); it.hasNext(); ) {
            PowerButton p = it.next();
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {

                    char pFirst = (shouldSortById() || p.name== null || p.name.length() == 0) ?   p.id.toUpperCase().charAt(0) : p.name.toUpperCase().charAt(0);

                    if (pFirst != prevFirst) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevFirst = pFirst;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevFirst != '\0') {
                            msg = String.valueOf(prevFirst).toUpperCase() + ":";
                            desc = "";
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.MOD) {
                    String pMod = p.modID;
                    if (pMod == null) pMod = "Slay the Spire";
                    if (!pMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = pMod;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevMod != null) {
                            msg = prevMod + ":";
                            desc = "";
                        }
                        //remove other lines
                        if (desc.contains("NL")) {
                            desc = desc.split(" NL ")[0];
                        } else if (desc.equals("StsOrigPlaceholder")) {
                            desc = TEXT[6];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                }
            }
            if (col == 5) {
                col = 0;
                row += 1;
            }
            curX = (START_X + SPACE_X * col);
            curY = (scrollY - SPACE * row);

            p.x = curX;
            p.y = curY;

            p.render(sb);


            col += 1;
        }
        calculateScrollBounds();
    }

    @Override
    public void scrolledUsingBar(float newPercent)
    {
        float newPosition = MathHelper.valueFromPercentBetween(scrollLowerBound, scrollUpperBound, newPercent);
        scrollY = newPosition;
        targetY = newPosition;
        updateBarPosition();
    }

    private void updateBarPosition()
    {
        float percent = MathHelper.percentFromValueBetween(scrollLowerBound, scrollUpperBound, scrollY);
        scrollBar.parentScrolledToPercent(percent);
    }
}
