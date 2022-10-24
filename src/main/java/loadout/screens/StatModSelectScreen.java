package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;

public class StatModSelectScreen extends SelectScreen{

    public class StatModButton {
        public StatModButton() {

        }
    }

    public StatModSelectScreen(AbstractRelic owner) {
        super(owner);
        if (sortHeader == null) this.sortHeader = new StatModSortHeader(this);


    }


    @Override
    protected void sortOnOpen() {

    }

    public void open() {
        super.open();

    }

    @Override
    public void updateFilters() {

    }

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void updateList(ArrayList<?> list) {

    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<?> list) {

    }
}
