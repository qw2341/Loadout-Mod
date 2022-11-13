package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;

public class MonsterSelectScreen extends SelectScreen<MonsterSelectScreen.MonsterButton>{

    public class MonsterButton {

        public String id;
        public String name;

        public String modID;

        public int amount;
        public Hitbox hb;
        public float x;
        public float y;
        public AbstractMonster.EnemyType type;

        public MonsterStrings monsterStrings;

        public MonsterButton() {
        }
    }

    public MonsterSelectScreen(AbstractRelic owner) {
        super(owner);

        MonsterHelper.getEncounter()
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
    protected void renderList(SpriteBatch sb, ArrayList<MonsterButton> list) {

    }


}
