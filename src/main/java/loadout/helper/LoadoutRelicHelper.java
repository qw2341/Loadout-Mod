package loadout.helper;

import loadout.LoadoutMod;
import loadout.relics.*;

import java.util.HashSet;

public class LoadoutRelicHelper {
    public static HashSet<String> loadoutRelicIds;

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
    }
}
