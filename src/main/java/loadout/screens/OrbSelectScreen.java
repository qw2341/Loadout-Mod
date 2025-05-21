package loadout.screens;

import basemod.ReflectionHacks;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.OrbBox;
import loadout.savables.Favorites;
import loadout.util.ModConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class OrbSelectScreen extends AbstractSelectScreen<OrbSelectScreen.OrbButton>{

    public static class OrbButton extends AbstractSelectScreen.AbstractSelectButton<AbstractOrb> {
        public OrbButton(AbstractOrb orb) {
            super(orb.ID);
            this.instance = orb;
            if(AbstractSelectButton.PLACEHOLDER_ID.equals(id)) this.id = orb.getClass().getName();
            this.name = this.instance.name;
            if(name == null) this.name = orb.getClass().getSimpleName();
            this.modID = WhatMod.findModID(orb.getClass());
            if (this.modID == null) this.modID = STS_MODID;

            hasAmount = false;

            this.tips.add(new PowerTip(this.instance.name, this.instance.description));
            ReflectionHacks.setPrivate(this.instance, AbstractOrb.class, "scale", Settings.scale);
            ReflectionHacks.setPrivate(this.instance, AbstractOrb.class, "channelAnimTimer", 0.0f);
        }

        @Override
        public void update() {
            super.update();
            try {
                this.instance.update();
                this.instance.tX = this.x;
                this.instance.cX = this.x;
                this.instance.tY = this.y;
                this.instance.cY = this.y;
                if(this.hb.hovered) this.instance.updateAnimation();
            } catch (Exception ignored) {

            }
        }

        @Override
        public void renderIcon(SpriteBatch sb, float a) {
            try{
                this.instance.render(sb);
            } catch (Exception ignored) {

            }
        }

    }

    private static final Comparator<OrbButton> BY_NAME = Comparator.comparing(o -> o.name);
    private static final Comparator<OrbButton> BY_MOD = Comparator.comparing(o -> o.modID);

    private static final Comparator<OrbButton> BY_ID = Comparator.comparing(o -> o.id);
    public OrbSelectScreen(AbstractCustomScreenRelic<OrbButton> owner) {
        super(owner);
        this.defaultSortType = SortType.MOD;
        this.itemHeight = 75.0f * Settings.yScale;

        this.sortHeader = new OrbSelectSortHeader(this);
    }

    @Override
    protected boolean testFilters(OrbButton item) {
        return true;
    }

    @Override
    public void sort(boolean isAscending) {
        switch (currentSortType){
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
        }
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

    @Override
    protected void callOnOpen() {
        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;

        if(this.itemsClone == null || this.itemsClone.isEmpty()) {
            //first time
            this.itemsClone = new ArrayList<>();
            for (Class<?extends AbstractOrb> orbC : LoadoutMod.orbMap.values()) {
                try {
                    itemsClone.add(new OrbButton(orbC.getDeclaredConstructor(new Class[] {}).newInstance()));
                } catch (InvocationTargetException|InstantiationException|IllegalAccessException|NoSuchMethodException e) {
                    LoadoutMod.logger.info("Error creating button for " + orbC.getName());
                    e.printStackTrace();
                    continue;
                } catch (NoClassDefFoundError noClassError) {
                    LoadoutMod.logger.info("ERROR THROWN! NO CLASS DEF FOUND FOR " + orbC.getName());
                    continue;
                }
            }


            this.items = new ArrayList<>(itemsClone);
        }

    }
    private boolean isCombat() {
        return AbstractDungeon.getCurrRoom() !=null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }
    @Override
    protected void updateItemClickLogic() {
        if(hoveredItem != null) {
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
                        //TODO Add to fav

                        if(filterFavorites)
                            updateFilters();

                        try {
                            LoadoutMod.favorites.save();
                        } catch (IOException e) {
                            LoadoutMod.logger.info("Failed to save favorites");
                        }
                    } else {
                        clickStartedItem.amount += selectMult;
                        //TODO modify amount

                        if(isCombat()) {
                            ((OrbBox)owner).channelOrb(clickStartedItem.instance.makeCopy(), +selectMult);
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
                    clickStartedItem.amount -= selectMult;
                    //TODO modify amount

                    if(isCombat()) {

                    }

                    this.owner.flash();

                    clickStartedItem = null;
                }
            }

        } else {
            clickStartedItem = null;
        }

    }

    @Override
    protected void updateList(ArrayList<OrbButton> list) {
        if (this.confirmButton.hb.hovered) return;

        for (OrbButton o : list)
        {
            o.update();
            o.hb.move(o.x  + 150.0f*Settings.scale, o.y);

            if (o.hb.hovered)
            {
                hoveredItem = o;
            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<OrbButton> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;
        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;



        for (Iterator<OrbButton> it = list.iterator(); it.hasNext(); ) {
            OrbButton o = it.next();
            if(ModConfig.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {

                    char pFirst = (shouldSortById() || o.name== null || o.name.length() == 0) ?   o.id.toUpperCase().charAt(0) : o.name.toUpperCase().charAt(0);

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
                        if (ModConfig.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.MOD) {
                    String pMod = o.modID;
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
                            desc = RelicSelectScreen.TEXT[6];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (ModConfig.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

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

            o.x = curX;
            o.y = curY;

            if(filterAll && Favorites.favoritePowers.contains(o.id)) {

                sb.setColor(GOLD_BACKGROUND);
                sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT,curX - (float)128 / 2.0F, curY - (float)128 / 2.0F, (float)128, (float)128);
            }

            o.render(sb);

            col += 1;
        }
        calculateScrollBounds();
    }
}
