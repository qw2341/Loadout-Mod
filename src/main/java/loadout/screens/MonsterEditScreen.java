package loadout.screens;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.utility.LoseBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import loadout.patches.AbstractCreaturePatch;
import loadout.relics.AbstractCustomScreenRelic;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import static loadout.relics.TildeKey.target;

public class MonsterEditScreen extends AbstractSelectScreen<StatModSelectScreen.StatModButton>{
    public MonsterEditScreen(AbstractCustomScreenRelic<StatModSelectScreen.StatModButton> owner) {
        super(owner);
        if (sortHeader == null) sortHeader = new MonsterEditScreenSortHeader(this);
        itemHeight = StatModSelectScreen.StatModButton.HITBOX_HEIGHT;

        this.items.add(new StatModSelectScreen.StatModButton(TopPanel.LABEL[3], false, ImageMaster.TP_HP, StatModSelectScreen.HP_NUM_OFFSET_X, Color.SALMON, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return target.currentHealth;
            }

            @Override
            public void setAmount(int amountToSet) {
                target.currentHealth = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                AbstractCreaturePatch.PanelField.isCurrentHPLocked.set(target, boolToChange);
                if(AbstractCreaturePatch.PanelField.isCurrentHPLocked.get(target)) {
                    AbstractCreaturePatch.PanelField.currentHPLockAmount.set(target, amount);
                }
            }

        }, ()-> AbstractCreaturePatch.PanelField.isCurrentHPLocked.get(target)));
        this.items.add(new StatModSelectScreen.StatModButton(StatModSelectScreen.TEXT[0], false, ImageMaster.TP_HP, StatModSelectScreen.HP_NUM_OFFSET_X, Color.SALMON, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return target.maxHealth;
            }

            @Override
            public void setAmount(int amountToSet) {
                target.maxHealth = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                AbstractCreaturePatch.PanelField.isMaxHPLocked.set(target, boolToChange);
                if(AbstractCreaturePatch.PanelField.isMaxHPLocked.get(target)) {
                    AbstractCreaturePatch.PanelField.maxHPLockAmount.set(target, amount);
                }
            }
        }, () -> AbstractCreaturePatch.PanelField.isMaxHPLocked.get(target)));
        this.items.add(new StatModSelectScreen.StatModButton(StringUtils.capitalize(GameDictionary.BLOCK.NAMES[0]), false, ImageMaster.BLOCK_ICON, StatModSelectScreen.HP_NUM_OFFSET_X,  new Color(0.9F, 0.9F, 0.9F, 1.0F), new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return target.currentBlock;
            }

            @Override
            public void setAmount(int amountToSet) {
                int diff = amountToSet - target.currentBlock;
                if(diff > 0) {
                    target.addBlock(diff);
                } else if(diff < 0) {
                    target.loseBlock(diff);
                }

                target.currentBlock = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                AbstractCreaturePatch.PanelField.isBlockLocked.set(target, boolToChange);
                if(AbstractCreaturePatch.PanelField.isBlockLocked.get(target)) {
                    AbstractCreaturePatch.PanelField.blockLockAmount.set(target, amount);
                }
            }
        }, () -> AbstractCreaturePatch.PanelField.isBlockLocked.get(target)));
    }

    @Override
    protected boolean testFilters(StatModSelectScreen.StatModButton item) {
        return true;
    }

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void callOnOpen() {
        this.items.forEach(StatModSelectScreen.StatModButton::refreshBool);
    }

    @Override
    protected void updateItemClickLogic() {

    }

    @Override
    public void updateFilters() {

    }

    @Override
    protected void updateList(ArrayList<StatModSelectScreen.StatModButton> list) {
        for (StatModSelectScreen.StatModButton smb : list)
        {
            smb.update();
            if (smb.hb.hovered)
            {
                hoveredItem = smb;
            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<StatModSelectScreen.StatModButton> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;

        for(StatModSelectScreen.StatModButton smb: this.items) {
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
}
