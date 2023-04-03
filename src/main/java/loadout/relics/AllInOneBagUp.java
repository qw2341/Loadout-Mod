package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.TrueVictoryRoom;
import loadout.LoadoutMod;

public class AllInOneBagUp extends CustomRelic implements ClickableRelic {

    public static final String ID = LoadoutMod.makeID("AllInOneBagUp");
    public static final Texture IMG = LoadoutBag.IMG;
    private static final Texture OUTLINE = LoadoutBag.OUTLINE;

    public AllInOneBagUp() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void onRightClick() {
        if(!this.usedUp) {
            CardCrawlGame.music.fadeOutBGM();
            MapRoomNode node = new MapRoomNode(3, 4);
            node.room = new TrueVictoryRoom();
            AbstractDungeon.nextRoom = node;
            AbstractDungeon.closeCurrentScreen();
            AbstractDungeon.nextRoomTransitionStart();
            this.usedUp();
        }

    }

    @Override
    public AbstractRelic makeCopy() {
        return new AllInOneBagUp();
    }
}
