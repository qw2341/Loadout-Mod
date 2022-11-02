package loadout.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import loadout.relics.TildeKey;

import java.util.ArrayList;


public class StatModSelectScreen extends SelectScreen<StatModSelectScreen.StatModButton>{
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

            this.x = 0;
            this.y = 0;

            this.hb = new Hitbox(x,y,200,100);
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
            switch (this.modType) {
                case HEALTH:
                    sb.setColor(Color.WHITE);
                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                            Integer.toString(AbstractDungeon.player.currentHealth), x + HP_NUM_OFFSET_X, y, Color.SALMON);
                    break;
                case MAX_HEALTH:
                    sb.setColor(Color.WHITE);
                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                            Integer.toString(AbstractDungeon.player.maxHealth), x + HP_NUM_OFFSET_X, y, Color.SCARLET);
                    break;
                case MONEY:
                    sb.setColor(Color.WHITE);
                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_GOLD, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_GOLD, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                        Integer.toString(AbstractDungeon.player.displayGold), x + GOLD_NUM_OFFSET_X, y, Settings.GOLD_COLOR);
                    break;

            }


        }
    }

    public SeedPanel seedPanel = new SeedPanel();

    public static float GOLD_NUM_OFFSET_X = 65.0F * Settings.scale;

    public static float HP_NUM_OFFSET_X = 60.0F * Settings.scale;



    public StatModSelectScreen(AbstractRelic owner) {
        super(owner);
        if (sortHeader == null) this.sortHeader = new StatModSortHeader(this);
        this.items = new ArrayList<StatModButton>();

        this.items.add(new StatModButton(ModType.HEALTH));
        this.items.add(new StatModButton(ModType.MAX_HEALTH));
        this.items.add(new StatModButton(ModType.MONEY));
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

        if(this.seedPanel.shown) this.seedPanel.close();

        MonsterGroup mg = AbstractDungeon.getMonsters();
        if(TildeKey.isKillAllMode && mg != null && !mg.areMonstersDead()) {
            this.owner.flash();
            TildeKey.killAllMonsters();
        }
        if(TildeKey.isNegatingDebuffs && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            AbstractDungeon.actionManager.addToTop(new RemoveDebuffsAction(AbstractDungeon.player));
        }
    }

    @Override
    public void updateFilters() {

    }

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void updateList(ArrayList<StatModButton> list) {
        for (StatModButton smb : list)
        {
            smb.hb.move(smb.x, smb.y);
            smb.update();
            if (smb.hb.hovered)
            {
                hoveredItem = smb;
            }
            if (smb.hb.clicked) {
                smb.hb.clicked = false;

            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<?> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;

        for(StatModButton smb: this.items) {
            if (col == 1) {
                col = 0;
                row += 1;
            }
            curX = (START_X+ 400.0F * Settings.scale + SPACE_X * col);
            curY = (scrollY - SPACE * row);

            smb.x = curX;
            smb.y = curY;

            smb.render(sb);

            col += 1;
        }
        calculateScrollBounds();
    }

    @Override
    public void update() {
        if(!seedPanel.shown) super.update();
        if(seedPanel.shown) seedPanel.update();

    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        if(this.seedPanel.shown) this.seedPanel.render(sb);
    }
}
