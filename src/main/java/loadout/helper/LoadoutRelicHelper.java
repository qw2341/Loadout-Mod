package loadout.helper;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import loadout.LoadoutMod;
import loadout.relics.*;

import java.util.ArrayList;
import java.util.HashSet;

public class LoadoutRelicHelper {
    public static HashSet<String> loadoutRelicIds;
    public static HashSet<String> customScreenRelicIds;
    public static HashSet<String> cardScreenRelicIds;
    static {
        loadoutRelicIds = new HashSet<>();
        loadoutRelicIds.add(LoadoutBag.ID);
        loadoutRelicIds.add(TrashBin.ID);
        loadoutRelicIds.add(LoadoutCauldron.ID);
        loadoutRelicIds.add(CardPrinter.ID);
        loadoutRelicIds.add(CardShredder.ID);
        loadoutRelicIds.add(CardModifier.ID);
        loadoutRelicIds.add(EventfulCompass.ID);
        loadoutRelicIds.add(PowerGiver.ID);
        loadoutRelicIds.add(TildeKey.ID);
        loadoutRelicIds.add(BottledMonster.ID);
        loadoutRelicIds.add(OrbBox.ID);
        loadoutRelicIds.add(AllInOneBag.ID);

        customScreenRelicIds = new HashSet<>();
        customScreenRelicIds.add(LoadoutBag.ID);
        customScreenRelicIds.add(TrashBin.ID);
        customScreenRelicIds.add(LoadoutCauldron.ID);
        customScreenRelicIds.add(EventfulCompass.ID);
        customScreenRelicIds.add(PowerGiver.ID);
        customScreenRelicIds.add(TildeKey.ID);
        customScreenRelicIds.add(BottledMonster.ID);
        customScreenRelicIds.add(OrbBox.ID);

        cardScreenRelicIds = new HashSet<>();
        cardScreenRelicIds.add(CardPrinter.ID);
        cardScreenRelicIds.add(CardShredder.ID);
        cardScreenRelicIds.add(CardModifier.ID);
    }

    public static ArrayList<AbstractCustomScreenRelic<?>> getPlayerCustomScreenRelics() {
        ArrayList<AbstractCustomScreenRelic<?>> ret = new ArrayList<>();
        if(AbstractDungeon.isPlayerInDungeon() && !AbstractDungeon.player.relics.isEmpty()) {
            for(String rID : customScreenRelicIds) {
                if(AbstractDungeon.player.hasRelic(rID)) ret.add((AbstractCustomScreenRelic<?>) AbstractDungeon.player.getRelic(rID));
            }
        }
        return ret;
    }

    public static ArrayList<AbstractCardScreenRelic> getPlayerCardScreenRelics() {
        ArrayList<AbstractCardScreenRelic> ret = new ArrayList<>();
        if(AbstractDungeon.isPlayerInDungeon() && !AbstractDungeon.player.relics.isEmpty()) {
            for(String rID : cardScreenRelicIds) {
                if(AbstractDungeon.player.hasRelic(rID)) ret.add((AbstractCardScreenRelic) AbstractDungeon.player.getRelic(rID));
            }
        }
        return ret;
    }

    public static void closeAllScreens() {
        for(AbstractCustomScreenRelic<?> r : getPlayerCustomScreenRelics()) if(r.isSelectionScreenUp()) r.selectScreen.close();
        for(AbstractCardScreenRelic r : getPlayerCardScreenRelics()) if(r.isSelectionScreenUp()) r.selectScreen.close();
    }
}
