package loadout.relics;

import basemod.CustomEventRoom;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.helpers.EventHelper;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.RestRoom;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.EventSelectScreen;
import loadout.util.TextureLoader;

import java.util.ArrayList;

import static loadout.LoadoutMod.*;

public class EventfulCompass extends AbstractCustomScreenRelic<EventSelectScreen.EventButton> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("EventfulCompass");
    private static Texture IMG = null;
    private static Texture OUTLINE = null;
    public static EventSelectScreen.EventButton lastEvent = null;

    public EventfulCompass() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK);
    }

    @Override
    protected AbstractSelectScreen<EventSelectScreen.EventButton> getNewSelectScreen() {
        return new EventSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new EventfulCompass();
    }

    public static void goToRoom(AbstractRoom destination) {
        if (AbstractDungeon.currMapNode == null) return;
        RoomEventDialog.optionList.clear();
        MapRoomNode cur = AbstractDungeon.currMapNode;
        MapRoomNode mapRoomNode2 = new MapRoomNode(cur.x, cur.y);
        mapRoomNode2.room = destination;

        ArrayList<MapEdge> curEdges = cur.getEdges();
        for (MapEdge edge : curEdges) {
            mapRoomNode2.addEdge(edge);
        }

        AbstractDungeon.player.releaseCard();
        AbstractDungeon.overlayMenu.hideCombatPanels();
        AbstractDungeon.previousScreen = null;
        AbstractDungeon.dynamicBanner.hide();
        AbstractDungeon.dungeonMapScreen.closeInstantly();
        AbstractDungeon.closeCurrentScreen();
        AbstractDungeon.topPanel.unhoverHitboxes();
        AbstractDungeon.fadeIn();
        AbstractDungeon.effectList.clear();
        AbstractDungeon.topLevelEffects.clear();
        AbstractDungeon.topLevelEffectsQueue.clear();
        AbstractDungeon.effectsQueue.clear();
        AbstractDungeon.dungeonMapScreen.dismissable = true;
        AbstractDungeon.nextRoom = mapRoomNode2;
        AbstractDungeon.setCurrMapNode(mapRoomNode2);
        try {
            AbstractDungeon.getCurrRoom().onPlayerEntry();
        } catch (Exception e) {
            logger.info("Error Occurred while entering");
        }

        AbstractDungeon.scene.nextRoom(mapRoomNode2.room);
        if(mapRoomNode2.room instanceof EventRoom)
            AbstractDungeon.rs = (mapRoomNode2.room.event instanceof com.megacrit.cardcrawl.events.AbstractImageEvent) ? AbstractDungeon.RenderScene.EVENT : AbstractDungeon.RenderScene.NORMAL;
        else if(mapRoomNode2.room instanceof RestRoom) {
            AbstractDungeon.rs = AbstractDungeon.RenderScene.CAMPFIRE;
        } else {
            AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
        }
    }

    public static void goToRoom(EventSelectScreen.EventButton eb) {
        if (AbstractDungeon.currMapNode == null) return;
        RoomEventDialog.optionList.clear();
        MapRoomNode cur = AbstractDungeon.currMapNode;
        MapRoomNode mapRoomNode2 = new MapRoomNode(cur.x, cur.y);
        CustomEventRoom cer = new CustomEventRoom();
        mapRoomNode2.room = cer;

        ArrayList<MapEdge> curEdges = cur.getEdges();
        for (MapEdge edge : curEdges) {
            mapRoomNode2.addEdge(edge);
        }

        AbstractDungeon.player.releaseCard();
        AbstractDungeon.overlayMenu.hideCombatPanels();
        AbstractDungeon.previousScreen = null;
        AbstractDungeon.dynamicBanner.hide();
        AbstractDungeon.dungeonMapScreen.closeInstantly();
        AbstractDungeon.closeCurrentScreen();
        AbstractDungeon.topPanel.unhoverHitboxes();
        AbstractDungeon.fadeIn();
        AbstractDungeon.effectList.clear();
        AbstractDungeon.topLevelEffects.clear();
        AbstractDungeon.topLevelEffectsQueue.clear();
        AbstractDungeon.effectsQueue.clear();
        AbstractDungeon.dungeonMapScreen.dismissable = true;
        AbstractDungeon.nextRoom = mapRoomNode2;
        AbstractDungeon.setCurrMapNode(mapRoomNode2);
        try {
            AbstractDungeon.overlayMenu.proceedButton.hide();
            cer.event = eb.getEvent();
            cer.event.onEnterRoom();
        } catch (Exception e) {
            logger.info("Error Occurred while entering");
        }

        AbstractDungeon.scene.nextRoom(mapRoomNode2.room);
        if(mapRoomNode2.room instanceof EventRoom)
            AbstractDungeon.rs = (mapRoomNode2.room.event instanceof com.megacrit.cardcrawl.events.AbstractImageEvent) ? AbstractDungeon.RenderScene.EVENT : AbstractDungeon.RenderScene.NORMAL;
        else if(mapRoomNode2.room instanceof RestRoom) {
            AbstractDungeon.rs = AbstractDungeon.RenderScene.CAMPFIRE;
        } else {
            AbstractDungeon.rs = AbstractDungeon.RenderScene.NORMAL;
        }
    }

    @Override
    public void onCtrlRightClick() {
        if(lastEvent != null) {
            flash();
            if(selectScreen == null) {
                selectScreen = getNewSelectScreen();
            }
            ((EventSelectScreen)selectScreen).executeEvent(lastEvent);
        }
    }
}
