package loadout.relics;

import basemod.DevConsole;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.blights.AbstractBlight;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.BlightHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.BlightSelectScreen;
import loadout.util.TextureLoader;


public class BlightChest extends AbstractCustomScreenRelic<AbstractBlight>{
    public static final String ID = LoadoutMod.makeID("BlightChest");
    public static Texture IMG = null;
    private static Texture OUTLINE = null;
    public BlightChest() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.HEAVY);
    }

    @Override
    protected AbstractSelectScreen<AbstractBlight> getNewSelectScreen() {
        return new BlightSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {
        for(AbstractBlight b : selectScreen.getSelectedItems()) {
            obtainBlight(b.blightID);
        }
    }


    public static void obtainBlight(String blightName){
        if(!AbstractDungeon.isPlayerInDungeon()) return;
        AbstractBlight blight = AbstractDungeon.player.getBlight(blightName);
        if (blight != null) {
            blight.incrementUp();
            blight.stack();
        } else if (BlightHelper.getBlight(blightName) != null) {
            AbstractDungeon.getCurrRoom().spawnBlightAndObtain((float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, BlightHelper.getBlight(blightName));
        } else {

        }
    }

    public static void obtainBlight(AbstractBlight b) {
        if(!AbstractDungeon.isPlayerInDungeon()) return;
        if(AbstractDungeon.player.hasBlight(b.blightID)) {
            AbstractBlight pB = AbstractDungeon.player.getBlight(b.blightID);
            pB.incrementUp();
            pB.stack();
        } else {
            AbstractDungeon.getCurrRoom().spawnBlightAndObtain((float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, b);
        }
    }

    public static void obtainBlight(AbstractBlight b, int amount) {
        if(!AbstractDungeon.isPlayerInDungeon()) return;
        if(!AbstractDungeon.player.hasBlight(b.blightID)) {
            AbstractDungeon.getCurrRoom().spawnBlightAndObtain((float) Settings.WIDTH / 2.0F, (float)Settings.HEIGHT / 2.0F, b);
            amount--;
        }
        AbstractBlight pB = AbstractDungeon.player.getBlight(b.blightID);
        for (int i = 0; i< amount ; i++) {
            pB.incrementUp();
            pB.stack();
        }

        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            AbstractDungeon.getMonsters().showIntent();
        }
    }

    public static void removeBlight(AbstractBlight b) {
        if(!AbstractDungeon.isPlayerInDungeon()) return;
        if(AbstractDungeon.player.hasBlight(b.blightID)) {
            AbstractDungeon.player.blights.removeIf(pB -> pB.blightID.equals(b.blightID));
            organizePlayerBlights();
        }
        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            AbstractDungeon.getMonsters().showIntent();
        }
    }

    public static void removeBlight(AbstractBlight b, int amount) {
        if(!AbstractDungeon.isPlayerInDungeon()) return;
        if(AbstractDungeon.player.hasBlight(b.blightID)) {
           AbstractBlight pB = AbstractDungeon.player.getBlight(b.blightID);
           pB.counter -= amount;
           if(pB.counter < 1) {
               removeBlight(b);
           } else {
               if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                   AbstractDungeon.getMonsters().showIntent();
               }
           }
        }

    }

    public static void organizePlayerBlights() {
        for(int i = 0; i < AbstractDungeon.player.blights.size(); ++i) {
            AbstractBlight tmp = (AbstractBlight)AbstractDungeon.player.blights.get(i);
            tmp.currentX = tmp.targetX = 64.0F * Settings.scale + (float)i * AbstractRelic.PAD_X;
            tmp.hb.move(tmp.currentX, tmp.currentY);
        }
    }
}
