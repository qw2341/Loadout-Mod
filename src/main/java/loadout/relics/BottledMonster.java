package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.MonsterSelectScreen;
import loadout.util.TextureLoader;

import java.util.ArrayList;

import static loadout.LoadoutMod.*;
import static loadout.screens.MonsterSelectScreen.MonsterButton.calculateSmartDistance;

public class BottledMonster extends AbstractCustomScreenRelic<MonsterSelectScreen.MonsterButton> {
    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("BottledMonster");
    public static Texture IMG = null;
    private static Texture OUTLINE = null;

    private static final String XGGG_NAME = LoadoutMod.isCHN() ? "瓶装星光" : "Isaac?!";

    public static Class<? extends AbstractMonster> lastMonster = null;


    public BottledMonster() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.FLAT);

        if (LoadoutMod.isXggg()) {
            this.tips.get(0).header = XGGG_NAME;
        }
    }

    @Override
    protected AbstractSelectScreen<MonsterSelectScreen.MonsterButton> getNewSelectScreen() {
        return new MonsterSelectScreen(this);
    }

    public ArrayList<MonsterSelectScreen.MonsterButton> getMonsterButtons() {
        if(this.selectScreen == null) this.selectScreen = getNewSelectScreen();
        return this.selectScreen.getList();
    }

    @Override
    public void onCtrlRightClick() {
        if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            flash();
            if(lastMonster != null)
                MonsterSelectScreen.spawnMonster(lastMonster);
            else
                dupeMonsters();
        }

    }

    @Override
    protected void doneSelectionLogics() {

    }

    public static void dupeMonsters() {
        AbstractRoom ar = AbstractDungeon.getCurrRoom();
        if(ar!=null && ar.monsters!= null) {
            ArrayList<AbstractMonster> monsterTemp = new ArrayList<>();
            for (AbstractMonster am: ar.monsters.monsters) {
                AbstractMonster m = MonsterSelectScreen.spawnMonster(am.getClass(),am.drawX - calculateSmartDistance(am, am) + 30.0F * (float) Math.random(), am.drawY + 20.0F * (float) Math.random());
                m.flipHorizontal = am.flipHorizontal;
                monsterTemp.add(m);
            }
            ar.monsters.monsters.addAll(monsterTemp);
        }
    }

    public static void copyMonster(AbstractMonster original, AbstractCreature copy) {
        copy.flipHorizontal = original.flipHorizontal;
        copy.powers.clear();
        if(original.powers != null && !original.powers.isEmpty()) original.powers.forEach(op -> PowerGiver.applyPowerToMonster(op.ID,op.amount,copy));
        //copy.name = original.name;
        copy.maxHealth = original.maxHealth;
        copy.currentHealth = original.currentHealth;
        copy.currentBlock = original.currentBlock;
    }
}
