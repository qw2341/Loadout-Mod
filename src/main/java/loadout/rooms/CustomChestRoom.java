package loadout.rooms;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.TreasureRoom;

public class CustomChestRoom extends TreasureRoom {
    @Override
    public void onPlayerEntry() {
        playBGM(null);
        AbstractDungeon.overlayMenu.proceedButton.setLabel(TEXT[0]);
    }
}
