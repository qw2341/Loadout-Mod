package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;

public class MonsterSelectScreen extends SelectScreen<MonsterSelectScreen.MonsterButton>{

    public class MonsterButton {

        public String id;


        public MonsterButton() {
        }
    }

    public MonsterSelectScreen(AbstractRelic owner) {
        super(owner);


    }

    @Override
    protected void sortOnOpen() {

    }

    @Override
    public void updateFilters() {

    }

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void updateList(ArrayList<MonsterButton> list) {

    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<?> list) {

    }


}
