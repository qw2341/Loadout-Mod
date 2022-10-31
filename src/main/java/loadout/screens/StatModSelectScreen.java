package loadout.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.relics.TildeKey;

import java.util.ArrayList;

public class StatModSelectScreen extends SelectScreen{
    protected enum ModType {
        HEALTH, MAX_HEALTH, MONEY
    }

    public class StatModButton {
        public ModType modType;

        public Hitbox hb;

        public float x;
        public float y;

        public int amount;

        public boolean isLocked;

        public StatModButton(ModType mt) {
            this.modType = mt;
            this.hb = new Hitbox(200,100);
            this.x = 0;
            this.y = 0;
            this.amount = 0;
            this.isLocked = false;
        }

        public void update() {

            this.hb.update();
            if (this.hb.justHovered) {
                CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
            }

            if (this.hb.hovered && InputHelper.justClickedLeft) {
                this.hb.clickStarted = true;
            }
            if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
                this.hb.clicked = false;



            }

            switch (this.modType) {

                case HEALTH:
                    this.amount = AbstractDungeon.player.currentHealth;
                    break;
                case MAX_HEALTH:
                    this.amount = AbstractDungeon.player.maxHealth;
                    break;
                case MONEY:
                    this.amount = AbstractDungeon.player.gold;
                    break;
            }
        }

        public void render(SpriteBatch sb) {

        }
    }

    public StatModSelectScreen(AbstractRelic owner) {
        super(owner);
        if (sortHeader == null) this.sortHeader = new StatModSortHeader(this);
        this.items = new ArrayList<StatModButton>();

    }


    @Override
    protected void sortOnOpen() {

    }

    @Override
    public void open() {
        super.open();

    }
    @Override
    public void close() {
        super.close();
        MonsterGroup mg = AbstractDungeon.getMonsters();
        if(TildeKey.isKillAllMode && mg != null && !mg.areMonstersDead()) {
            this.owner.flash();
            for (AbstractMonster am: mg.monsters) {
                AbstractDungeon.actionManager.addToTop(new InstantKillAction(am));
            }
        }
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

    @Override
    public void update() {
        super.update();
    }
}
