package loadout.helper;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.relics.*;

import java.util.ArrayList;
import java.util.HashSet;

public class LoadoutRelicHelper {
    public static HashSet<String> loadoutRelicIds;
    public static HashSet<String> customScreenRelicIds;
    public static HashSet<String> cardScreenRelicIds;

    public static final HashSet<Class> LOADOUT_RELIC_CLASSES = new HashSet<>();

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
//        loadoutRelicIds.add(AllInOneBag.ID);
        loadoutRelicIds.add(BlightChest.ID);

        customScreenRelicIds = new HashSet<>();
        customScreenRelicIds.add(LoadoutBag.ID);
        customScreenRelicIds.add(TrashBin.ID);
        customScreenRelicIds.add(LoadoutCauldron.ID);
        customScreenRelicIds.add(EventfulCompass.ID);
        customScreenRelicIds.add(PowerGiver.ID);
        customScreenRelicIds.add(TildeKey.ID);
        customScreenRelicIds.add(BottledMonster.ID);
        customScreenRelicIds.add(OrbBox.ID);
        customScreenRelicIds.add(BlightChest.ID);

        cardScreenRelicIds = new HashSet<>();
        cardScreenRelicIds.add(CardPrinter.ID);
        cardScreenRelicIds.add(CardShredder.ID);
        cardScreenRelicIds.add(CardModifier.ID);

//        LOADOUT_RELIC_CLASSES.add(AllInOneBag.class);
        LOADOUT_RELIC_CLASSES.add(AllInOneBagUp.class);
        LOADOUT_RELIC_CLASSES.add(LoadoutBag.class);
        LOADOUT_RELIC_CLASSES.add(TrashBin.class);
        LOADOUT_RELIC_CLASSES.add(LoadoutCauldron.class);
        LOADOUT_RELIC_CLASSES.add(EventfulCompass.class);
        LOADOUT_RELIC_CLASSES.add(PowerGiver.class);
        LOADOUT_RELIC_CLASSES.add(TildeKey.class);
        LOADOUT_RELIC_CLASSES.add(BottledMonster.class);
        LOADOUT_RELIC_CLASSES.add(OrbBox.class);
        LOADOUT_RELIC_CLASSES.add(BlightChest.class);
        LOADOUT_RELIC_CLASSES.add(CardPrinter.class);
        LOADOUT_RELIC_CLASSES.add(CardShredder.class);
        LOADOUT_RELIC_CLASSES.add(CardModifier.class);

    }

}
