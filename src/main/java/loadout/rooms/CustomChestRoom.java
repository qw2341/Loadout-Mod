package loadout.rooms;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;

public class CustomChestRoom extends TreasureRoom {
    @Override
    public void onPlayerEntry() {
        playBGM(null);
        AbstractDungeon.overlayMenu.proceedButton.setLabel(TEXT[0]);
    }
}
