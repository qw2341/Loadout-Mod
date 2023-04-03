package loadout.screens;

import basemod.ReflectionHacks;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
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
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.TheBombPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import loadout.LoadoutMod;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.AllInOneBag;
import loadout.relics.PowerGiver;
import loadout.savables.Favorites;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PowerSelectScreen extends AbstractSelectScreen<PowerSelectScreen.PowerButton> implements ScrollBarListener
{
    public static AbstractPlayer dummyPlayer;
    public static AbstractMonster dummyMonster;

    static {
        try {
            Constructor<Ironclad> con = Ironclad.class.getDeclaredConstructor(String.class);
            con.setAccessible(true);
            dummyPlayer = con.newInstance("");
            dummyPlayer.name = "Player";
            dummyPlayer.isPlayer = true;
            dummyPlayer.isDying = false;
        } catch (Exception e) {
            LoadoutMod.logger.info("Failed to create dummy player");
            e.printStackTrace();
        }

        try {

            dummyMonster = new Cultist(0,0,false);
            //dummyMonster.name = "";
            dummyMonster.isPlayer = false;
            dummyMonster.isDying = false;
        } catch (Exception e) {
            LoadoutMod.logger.info("Failed to create dummy monster");
            e.printStackTrace();
        }
    }

    public static AbstractCard dummyCard = new Madness();

    public static HashSet<String> specialCases = new HashSet<>();
    static {
        specialCases.add("TheBomb");
    }

    public static class PowerButton {

        public Class<? extends AbstractPower> pClass;
        public AbstractPower instance;
        public String id;
        public PowerStrings powerStrings;
        public String name;
        public AbstractPower.PowerType type;
        public String modID;
        public String desc;
        public int amount;
        public Hitbox hb;
        public float x;
        public float y;
        public ArrayList<PowerTip> tips;
        public TextureAtlas.AtlasRegion region48;
        public TextureAtlas.AtlasRegion region128;

        public PowerButton(String id, Class<? extends AbstractPower> pClass) {
            this.pClass = pClass;
            Constructor<?>[] con = pClass.getDeclaredConstructors();
            this.tips = new ArrayList<>();





            try {
                if (specialCases.contains(id)) {
                    switch (id) {
                        case "TheBomb":
                            this.instance = new TheBombPower(dummyPlayer,0,40);
                            break;
                    }
                } else {
                    int paramCt = con[0].getParameterCount();
                    Class[] params = con[0].getParameterTypes();
                    Object[] paramz = new Object[paramCt];

                    for (int i = 0 ; i< paramCt; i++) {
                        Class param = params[i];
                        if (AbstractPlayer.class.isAssignableFrom(param)) {
                            paramz[i] = dummyPlayer;
                        } else if (AbstractMonster.class.isAssignableFrom(param)) {
                            paramz[i] = dummyMonster;
                        } else if (AbstractCreature.class.isAssignableFrom(param)) {
                            paramz[i] = dummyPlayer;
                        } else if (int.class.isAssignableFrom(param)) {
                            paramz[i] = 0;
                        } else if (AbstractCard.class.isAssignableFrom(param)) {
                            paramz[i] = dummyCard;
                        } else if (boolean.class.isAssignableFrom(param)) {
                            paramz[i] = true;
                        }
                    }
                    //LoadoutMod.logger.info("Class: " + pClass.getName() + " with parameter: " + Arrays.toString(paramz));

                    this.instance = (AbstractPower) con[0].newInstance(paramz);
                }


                this.id = id;
                this.powerStrings = ReflectionHacks.getPrivateStatic(pClass,"powerStrings");
                //this.name = instance.name;
                this.name = powerStrings.NAME;
                this.desc = this.instance.description;
                this.modID = WhatMod.findModID(pClass);
                if (this.modID == null) this.modID = "Slay the Spire";

                this.region48 = this.instance.region48;
                this.region128 = this.instance.region128;


            } catch (Exception e) {

                LoadoutMod.logger.info("Failed to create power button for: " + pClass.getName() + " with name = "+ this.name + " for mod: "+ this.modID);
                e.printStackTrace();
            }
            if(this.id == null) this.id = "Unnamed Power";
            if (this.name == null) this.name = this.id;
            if (this.modID == null) this.modID = "Slay the Spire";

            if(desc != null && desc.length() > 0) {
                //String fullD = StringUtils.join(desc," ");
//                for (String d : desc)
//                {
//                    if (d != null)
//                        this.tips.add(new PowerTip(this.name, d));
//                }

                this.tips.add(new PowerTip(this.name, desc, region48));
            }
            this.tips.add(new PowerTip("Mod",this.modID));
            this.hb = new Hitbox(200.0f * Settings.scale,75.0f * Settings.yScale);
            this.amount = 0;
            this.x = 0;
            this.y = 0;
            //this.loadRegion(StringUtils.lowerCase(this.id));
            //this.type = instance.type;
            //LoadoutMod.logger.info("Created power button for: " + pClass.getName() + " with name = "+ this.name + " for mod: "+ this.modID);
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
                float a = (amount != 0 || this.hb.hovered) ? 1.0f : 0.7f;
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

    private static final String[] TEXT = RelicSelectScreen.TEXT;


    private static final float SPACE = 85.0F * Settings.scale;
    protected static final float START_X = 300.0F * Settings.scale;
    private static final float START_Y = Settings.HEIGHT - 300.0F * Settings.scale;

    public static final float SPACE_X = 300.0F * Settings.yScale;

    //private PowerSelectSortHeader sortHeader;



    private static final Comparator<PowerButton> BY_TYPE = Comparator.comparing(p -> p.type);
    private static final Comparator<PowerButton> BY_NAME = Comparator.comparing(p -> p.name);
    private static final Comparator<PowerButton> BY_MOD = Comparator.comparing(p -> p.modID);

    private static final Comparator<PowerButton> BY_ID = Comparator.comparing(p -> p.id);

    public PowerGiver.PowerTarget currentTarget = PowerGiver.PowerTarget.PLAYER;

    private boolean firstSelection = true;


    public PowerSelectScreen(AbstractCustomScreenRelic<PowerButton> owner)
    {
        super(owner);

        //import favorites

        this.currentSortType = SortType.MOD;
        this.defaultSortType = SortType.MOD;


        for (String pID : LoadoutMod.powersToDisplay.keySet()) {
            if(pID == null) continue;
            PowerButton pb = null;
            try {
                Class<? extends AbstractPower> pClass = LoadoutMod.powersToDisplay.get(pID);
                pb = new PowerButton(pID, pClass);

            } catch (Exception | Error e) {
                LoadoutMod.logger.info("Failed to create a button for " + pID);
            }
            if(pb != null) {
                this.items.add(pb);
                this.itemsClone.add(pb);
            }

        }


        this.sortHeader = new PowerSelectSortHeader(this);

        this.itemHeight = 150.0F;
    }


    public void sortByType(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(BY_TYPE.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(BY_TYPE.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.TYPE;
        scrolledUsingBar(0.0F);
    }

    public void sortAlphabetically(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.items.sort(BY_ID);
            else this.items.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.items.sort(BY_ID.reversed());
            else this.items.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }
    public void sortByMod(boolean isAscending){
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(BY_MOD.thenComparing(BY_ID));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(BY_MOD.reversed().thenComparing(BY_ID));
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    public void sort(boolean isAscending) {
        switch (currentSortType) {
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
            case TYPE:
                sortByType(isAscending);
                break;
        }
    }

    public void refreshPowersForTarget() {
        PowerGiver o = ((PowerGiver)owner);
        for(PowerButton pb : this.itemsClone) {
            pb.amount = 0;
            if (currentTarget == PowerGiver.PowerTarget.PLAYER) {
                if(o.savedPowersPlayer.containsKey(pb.id)) {
                    pb.amount = o.savedPowersPlayer.get(pb.id);
                }
            } else if (currentTarget == PowerGiver.PowerTarget.MONSTER) {
                if(o.savedPowersMonster.containsKey(pb.id)) {
                    pb.amount = o.savedPowersMonster.get(pb.id);
                }
            }
        }
    }

    public void resetPowerAmounts() {
        for (PowerButton pb : this.items) {
            pb.amount = 0;
        }
        if (currentTarget == PowerGiver.PowerTarget.PLAYER)
            ((PowerGiver)this.owner).savedPowersPlayer.clear();
        else if (currentTarget == PowerGiver.PowerTarget.MONSTER)
            ((PowerGiver)this.owner).savedPowersMonster.clear();
    }

    @Override
    protected void callOnOpen() {
        refreshPowersForTarget();

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;
        firstSelection = true;
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


    @Override
    protected void updateItemClickLogic() {
        if (hoveredItem != null) {
            if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
                clickStartedItem = hoveredItem;
                //logger.info("Pressed Left");
            }
            if (InputHelper.justReleasedClickLeft || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredItem == clickStartedItem)
                {
                    if(isFaving) {
                        String pID = hoveredItem.id;
                        //Add to fav
                        if(Favorites.favoritePowers.contains(pID)) {
                            Favorites.favoritePowers.remove(pID);
                        } else {
                            Favorites.favoritePowers.add(pID);
                        }
                        if(filterFavorites)
                            updateFilters();

                        try {
                            LoadoutMod.favorites.save();
                        } catch (IOException e) {
                            LoadoutMod.logger.info("Failed to save favorites");
                        }
                    } else {
                        if(firstSelection) {
                            firstSelection = false;
                            PowerGiver.lastPowers.clear();
                        }
                        PowerGiver.lastPowers.add(new PowerGiver.PowerAction(currentTarget, clickStartedItem.id, selectMult));
                        clickStartedItem.amount += selectMult;
                        if(currentTarget == PowerGiver.PowerTarget.PLAYER)
                            ((PowerGiver)owner).modifyAmountPlayer(clickStartedItem.id, +selectMult);
                        else if (currentTarget == PowerGiver.PowerTarget.MONSTER)
                            ((PowerGiver)owner).modifyAmountMonster(clickStartedItem.id, +selectMult);

                        if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                            if(currentTarget == PowerGiver.PowerTarget.PLAYER) {
                                ((PowerGiver)owner).applyPowerToPlayer(clickStartedItem.id, +selectMult);
                                if(clickStartedItem.id.equals(StrengthPower.POWER_ID) && selectMult > 0) {
                                    if(selectMult > 5) AllInOneBag.XGGGSay("@I@ @need@ @MORE@ @power!@");
                                    else AllInOneBag.XGGGSay("~I~ ~need~ ~power!~");
                                }
                            }
                            else if (currentTarget == PowerGiver.PowerTarget.MONSTER) {
                                for (AbstractMonster am : AbstractDungeon.getMonsters().monsters) {
                                    ((PowerGiver)owner).applyPowerToMonster(clickStartedItem.id, +selectMult, am);
                                }
                            }
                        }

                        this.owner.flash();
                    }

                    clickStartedItem = null;

                    if (doneSelecting()) {
                        close();
                    }
                }
            }

            if (InputHelper.justClickedRight || CInputActionSet.select.isJustPressed()) {
                clickStartedItem = hoveredItem;

            }
            if (InputHelper.justReleasedClickRight || CInputActionSet.select.isJustPressed())
            {
                CInputActionSet.select.unpress();
                if (hoveredItem == clickStartedItem)
                {
                    if(firstSelection) {
                        firstSelection = false;
                        PowerGiver.lastPowers.clear();
                    }
                    PowerGiver.lastPowers.add(new PowerGiver.PowerAction(currentTarget, clickStartedItem.id, -selectMult));
                    clickStartedItem.amount -= selectMult;
                    if(currentTarget == PowerGiver.PowerTarget.PLAYER)
                        ((PowerGiver)owner).modifyAmountPlayer(clickStartedItem.id, -selectMult);
                    else if (currentTarget == PowerGiver.PowerTarget.MONSTER)
                        ((PowerGiver)owner).modifyAmountMonster(clickStartedItem.id, -selectMult);

                    if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                        if(currentTarget == PowerGiver.PowerTarget.PLAYER)
                            ((PowerGiver)owner).applyPowerToPlayer(clickStartedItem.id, -selectMult);
                        else if (currentTarget == PowerGiver.PowerTarget.MONSTER) {
                            for (AbstractMonster am : AbstractDungeon.getMonsters().monsters) {
                                ((PowerGiver)owner).applyPowerToMonster(clickStartedItem.id, -selectMult, am);
                            }
                        }
                    }

                    this.owner.flash();

                    clickStartedItem = null;
                }
            }

        } else {
            clickStartedItem = null;
        }
    }


    private boolean testTextFilter(PowerButton pb) {
       if (pb.id != null && StringUtils.containsIgnoreCase(pb.id,sortHeader.searchBox.filterText)) return true;
       if (pb.name != null && StringUtils.containsIgnoreCase(pb.name,sortHeader.searchBox.filterText)) return true;
       if (pb.desc != null && StringUtils.containsIgnoreCase(pb.desc,sortHeader.searchBox.filterText)) return true;
       return false;
    }

    protected boolean testFilters(PowerButton pb) {
        boolean favCheck = filterAll || (filterFavorites && Favorites.favoritePowers.contains(pb.id));
        boolean textCheck = sortHeader == null || sortHeader.searchBox.filterText.equals("") || testTextFilter(pb);
        return favCheck && textCheck;
    }

    @Override
    public void updateFilters() {
        super.updateFilters();

        if(!filterFavorites)
            scrolledUsingBar(0.0f);
    }


    @Override
    protected void updateList(ArrayList<PowerButton> list)
    {
        if (this.confirmButton.hb.hovered) return;

        for (PowerButton p : list)
        {
            p.update();
            p.hb.move(p.x  + 150.0f, p.y);

            if (p.hb.hovered)
            {
                hoveredItem = p;
            }
        }
    }


    @Override
    protected void renderList(SpriteBatch sb, ArrayList<PowerButton> list)
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

            if(filterAll && Favorites.favoritePowers.contains(p.id)) {

                sb.setColor(GOLD_BACKGROUND);
                sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT,curX - (float)128 / 2.0F, curY - (float)128 / 2.0F, (float)128, (float)128);
            }

            p.render(sb);



            col += 1;
        }
        calculateScrollBounds();
    }

}
