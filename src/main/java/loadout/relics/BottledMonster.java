package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
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
    private static final Texture IMG =  (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("bottle_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("bottle_relic.png"));

    private static final Texture IMG_XGGG_ALT = TextureLoader.getTexture(makeRelicPath("bottle_relic_xggg.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic.png"));

    private static final String XGGG_NAME = "瓶装星光";

    public static Class<? extends AbstractMonster> lastMonster = null;


    public BottledMonster() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);

        if (LoadoutMod.isXggg()) {
            this.img = IMG_XGGG_ALT;
            this.tips.get(0).header = XGGG_NAME;
        }
    }

    @Override
    protected AbstractSelectScreen<MonsterSelectScreen.MonsterButton> getNewSelectScreen() {
        return new MonsterSelectScreen(this);
    }

    @Override
    public void onCtrlRightClick() {
        if(lastMonster != null && AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            flash();
            MonsterSelectScreen.spawnMonster(lastMonster);
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

    @Override
    public AbstractRelic makeCopy()
    {
        return new BottledMonster();
    }
}
