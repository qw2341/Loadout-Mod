package loadout.screens;

import basemod.BaseMod;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import loadout.LoadoutMod;
import loadout.helper.Action;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.AllInOneBag;
import loadout.relics.BottledMonster;
import loadout.relics.TildeKey;
import loadout.uiElements.AbstractButton;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class CharacterSkinSelectScreen extends AbstractSelectScreen<CharacterSkinSelectScreen.CharacterButton>{

    private static final Comparator<CharacterButton> BY_NAME = Comparator.comparing(c -> c.labelText);
    private static final Comparator<CharacterButton> BY_ID = Comparator.comparing(c -> c.id);

    private static final Comparator<CharacterButton> BY_MOD = Comparator.comparing(c -> c.modID);

    static HashSet<String> EXCLUSIONS = new HashSet<>();
    static {
        EXCLUSIONS.add("Hexaghost");
    }
    public CharacterSkinSelectScreen(AbstractCustomScreenRelic owner) {
        super(owner);
        this.sortHeader = new CharacterSkinSortHeader(this);

        this.defaultSortType = SortType.MOD;
    }

    private boolean testTextFilter(CharacterButton cb) {
        if (cb.id != null && StringUtils.containsIgnoreCase(cb.id,sortHeader.searchBox.filterText)) return true;
        if (cb.labelText != null && StringUtils.containsIgnoreCase(cb.labelText,sortHeader.searchBox.filterText)) return true;
        //if (cb.desc != null && StringUtils.containsIgnoreCase(cb.desc,sortHeader.searchBox.filterText)) return true;
        return false;
    }

    @Override
    protected boolean testFilters(CharacterButton item) {
        boolean textCheck = sortHeader == null || sortHeader.searchBox.filterText.equals("") || testTextFilter(item);
        return textCheck;
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
            if (shouldSortById()) this.items.sort(BY_MOD.thenComparing(BY_ID));
            else this.items.sort(BY_MOD.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.items.sort(BY_MOD.reversed().thenComparing(BY_ID));
            else this.items.sort(BY_MOD.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
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

    @Override
    protected void callOnOpen() {
        this.currentSortOrder = SortOrder.ASCENDING;
        this.currentSortType = SortType.MOD;
        updateFilters();

        if(this.itemsClone == null || this.itemsClone.isEmpty()) {
            ArrayList<MonsterSelectScreen.MonsterButton> ml = AllInOneBag.INSTANCE.bottledMonster.getMonsterButtons();

            this.itemsClone = new ArrayList<>();

            //characters
            for(AbstractPlayer ap : CardCrawlGame.characterManager.getAllCharacters()) {
                if(EXCLUSIONS.contains(ap.getClass().getSimpleName())) continue;

                this.itemsClone.add(new CharacterButton(ap));
            }


            //monsters
            for (MonsterSelectScreen.MonsterButton mb : ml) {
                //handle exclusions

                //handle exceptions
                if(EXCLUSIONS.contains(mb.mClass.getSimpleName())) {
                    continue;
                }
                //TODO: make modded monster compatible
                if(mb.modID.equals("Slay the Spire")) this.itemsClone.add(new CharacterButton(mb));
            }

        }
        this.items = new ArrayList<>(this.itemsClone);

        targetY = scrollLowerBound;
        scrollY = Settings.HEIGHT - 400.0f * Settings.scale;
    }

    @Override
    protected void updateItemClickLogic() {
        if(this.hoveredItem != null) {
            if(InputHelper.justReleasedClickLeft){
                this.close();
            }
        }
    }

    @Override
    protected void updateList(ArrayList<CharacterButton> list) {
        if (this.confirmButton.hb.hovered) return;

        for (CharacterButton cb : list)
        {

            cb.update();

            if (cb.hb.hovered)
            {
                hoveredItem = cb;
            }
            if(cb.hb.hovered && InputHelper.justClickedLeft) {
                cb.hb.clicked = true;
            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<CharacterButton> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;

        char prevFirst = '\0';
        String prevMod = "";
        scrollTitleCount = 0;

        for (Iterator<CharacterButton> it = list.iterator(); it.hasNext();) {
            CharacterButton cb = it.next();

            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {
                    char rFirst = (shouldSortById() || cb.labelText == null) ? cb.id.toUpperCase().charAt(0) : cb.labelText.toUpperCase().charAt(0);
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
                    } else if (currentSortType == SortType.MOD) {
                        String rMod = cb.modID;
                        if (!rMod.equals(prevMod)) {
                            row++;
                            scrollTitleCount++;

                            //if new type, render new texts
                            prevMod = rMod;

                            String msg = "Undefined:";
                            String desc = "";
                            if (prevMod != null) {
                                msg = prevMod + ":";
                            }
                            FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                            if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                    FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                            row++;
                            col = 0;
                        }
                    }
                }
            }

            if (col == itemsPerLine) {
                col = 0;
                row += 1;
            }
            curX = (START_X + SPACE_X * col);
            curY = (scrollY - SPACE * row);

            cb.x = curX;
            cb.y = curY;

            cb.render(sb);
            col += 1;
        }
    }

    public static class CharacterButton extends AbstractButton {

        public CharacterButton(String labelText, String id) {
            super(labelText, id);
        }

        public CharacterButton(String labelText, String id, float x, float y) {
            super(labelText, id, x, y);
        }

        public CharacterButton(MonsterSelectScreen.MonsterButton mb) {
            super(mb.name, mb.id);
            this.modID = mb.modID;
            if(this.modID == null) this.modID = "Slay the Spire";
            this.onHoverRender = (sb) -> {
                //mb.instance.render(sb);
            };

            this.onRelease = () -> {
                if(!this.pressStarted) return;
                TildeKey.morph(TildeKey.morphee, MonsterSelectScreen.MonsterButton.createMonster(mb.mClass));
                AllInOneBag.INSTANCE.closeAllScreens();
            };

        }

        public CharacterButton(AbstractPlayer ap) {
            super(ap.title, ap.getClass().getName());
            this.modID = WhatMod.findModID(ap.getClass());
            if(this.modID == null) this.modID = "Slay the Spire";

            this.onHoverRender = (sb) -> {
                //mb.instance.render(sb);
            };

            this.onRelease = () -> {
                if(!this.pressStarted) return;
                TildeKey.morph(TildeKey.morphee, ap);
                AllInOneBag.INSTANCE.closeAllScreens();
            };
        }

        public CharacterButton setOnReleaseAction(Action onReleaseAction) {
            this.onRelease = onReleaseAction;
            return this;
        }

        public CharacterButton setOnRightReleaseAction(Action onRightReleaseAction) {
            this.onRightRelease = onRightReleaseAction;
            return this;
        }
    }

}
