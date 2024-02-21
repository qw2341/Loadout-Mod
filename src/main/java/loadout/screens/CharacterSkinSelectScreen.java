package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
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

    static HashSet<String> EXCLUSIONS = new HashSet<>();
    static {
        EXCLUSIONS.add("Hexaghost");
    }
    public CharacterSkinSelectScreen(AbstractCustomScreenRelic owner) {
        super(owner);
        this.sortHeader = new CharacterSkinSortHeader(this);

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

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void callOnOpen() {
        if(this.itemsClone == null || this.itemsClone.isEmpty()) {
            ArrayList<MonsterSelectScreen.MonsterButton> ml = AllInOneBag.getInstance().bottledMonster.getMonsterButtons();

            this.itemsClone = new ArrayList<>();

            //characters

            //monsters
            for (MonsterSelectScreen.MonsterButton mb : ml) {
                //handle exclusions

                //handle exceptions
                if(EXCLUSIONS.contains(mb.mClass.getSimpleName())) {
                    continue;
                }

                this.itemsClone.add(new CharacterButton(mb));
            }

        }
        this.items = new ArrayList<>(this.itemsClone);
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



        for (Iterator<CharacterButton> it = list.iterator(); it.hasNext();) {
            CharacterButton cb = it.next();

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
            this.onHoverRender = (sb) -> {

            };

            this.onRelease = () -> {
                if(!this.pressStarted) return;
                TildeKey.morph(TildeKey.morphee, MonsterSelectScreen.MonsterButton.createMonster(mb.mClass));
                AllInOneBag.getInstance().closeAllScreens();
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
