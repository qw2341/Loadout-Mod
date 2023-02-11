package loadout.screens;

import basemod.ReflectionHacks;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.blights.GrotesqueTrophy;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import loadout.LoadoutMod;
import loadout.helper.RelicClassComparator;
import loadout.helper.RelicModComparator;
import loadout.helper.RelicNameComparator;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.BlightChest;
import loadout.relics.OrbBox;
import loadout.savables.Favorites;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class BlightSelectScreen extends AbstractSelectScreen<AbstractBlight>{

    public static HashSet<String> screenBlights = new HashSet<>();
    static {
        screenBlights.add(GrotesqueTrophy.ID);
    }

    private static final Comparator<AbstractBlight> BY_NAME = Comparator.comparing(b -> b.name);
    private static final Comparator<AbstractBlight> BY_MOD = Comparator.comparing(b -> {
        String modID = WhatMod.findModID(b.getClass());
        return (modID ==null ) ? "Slay the Spire" : modID;
    });

    private static final Comparator<AbstractBlight> BY_ID = Comparator.comparing(b -> b.blightID);

    public BlightSelectScreen(AbstractCustomScreenRelic<AbstractBlight> owner) {
        super(owner);
        this.sortHeader = new BlightSelectSortHeader(this);
        this.itemsPerLine = 10;
        this.defaultSortType = SortType.MOD;
        this.itemHeight = 75.0f;
        for (String b: BlightHelper.blights) {
            AbstractBlight ab = BlightHelper.getBlight(b);
            if(LoadoutMod.ignoreUnlock) {
                ab.isSeen = true;
            }
            this.itemsClone.add(ab);
        }
        this.items.addAll(this.itemsClone);
    }


    @Override
    protected boolean testFilters(AbstractBlight item) {
        return true;
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
    public void sort(boolean isAscending) {
        switch (this.currentSortType) {
            case NAME:
                this.sortAlphabetically(isAscending);
                break;
            case MOD:
                this.sortByMod(isAscending);
                break;
        }
    }



    @Override
    protected void callOnOpen() {

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
                        String pID = hoveredItem.blightID;
                        //TODO Add to fav

                        if(filterFavorites)
                            updateFilters();

                        try {
                            LoadoutMod.favorites.save();
                        } catch (IOException e) {
                            LoadoutMod.logger.info("Failed to save favorites");
                        }
                    } else {
                        if(!clickStartedItem.isSeen) {
                            clickStartedItem.isSeen = true;
                        }
                        String bID = clickStartedItem.blightID;
                        if(screenBlights.contains(bID)) close();
                        BlightChest.obtainBlight(BlightHelper.getBlight(bID), selectMult);
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
                    BlightChest.removeBlight(clickStartedItem, selectMult);
                    this.owner.flash();

                    clickStartedItem = null;
                }
            }

        } else {
            clickStartedItem = null;
        }
    }

    @Override
    protected void updateList(ArrayList<AbstractBlight> list) {
        for (AbstractBlight b : list)
        {
            b.hb.move(b.currentX, b.currentY);
            b.update();
            if (b.hb.hovered)
            {
                hoveredItem = b;
            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<AbstractBlight> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;
        GOLD_OUTLINE_COLOR.a = 0.3f;
        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;

        for(AbstractBlight b : list) {
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {

                    char rFirst = (shouldSortById()) ? b.blightID.toUpperCase().charAt(0) : b.name.toUpperCase().charAt(0);
                    if (rFirst != prevFirst) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevFirst = rFirst;

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
                    String bMod = WhatMod.findModID(b.getClass());
                    if(bMod==null) bMod = "Slay the Spire";
                    if (!bMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = bMod;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevMod != null) {
                            msg = prevMod + ":";
                            desc = RelicModComparator.getModDesc(b);
                        }
                        //remove other lines
                        if (desc.contains("NL")) {
                            desc = desc.split(" NL ")[0];
                        } else if (desc.equals("StsOrigPlaceholder")) {
                            desc = "";
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                }
            }
            if (col == this.itemsPerLine) {
                col = 0;
                row += 1;
            }
            curX = (START_X + SPACE * col);
            curY = (scrollY - SPACE * row);

            b.currentX = curX;
            b.currentY = curY;



            if(selectedItems.contains(b)) {
                sb.setColor(new Color(1.0F, 0.8F, 0.2F, 0.5F + (
                        MathUtils.cosDeg((float)(System.currentTimeMillis() / 4L % 360L)) + 1.25F) / 5.0F));
                sb.draw(ImageMaster.FILTER_GLOW_BG, curX-64.0F, curY-64.0F, 64.0F, 64.0F, 128.0f, 128.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 128, 128, false, false);
                b.render(sb,false,Color.GOLD);

            } else {
                b.render(sb, false, Color.BLACK);
            }
            col += 1;
        }
        calculateScrollBounds();
    }


}
