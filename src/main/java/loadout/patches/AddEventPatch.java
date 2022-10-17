package loadout.patches;

import basemod.BaseMod;
import basemod.eventUtil.AddEventParams;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import loadout.LoadoutMod;

/**
 * Credit to EventFilter Mod
 */

@SpirePatch(clz = BaseMod.class, method = "addEvent", paramtypez = {AddEventParams.class})
public class AddEventPatch {
    @SpirePrefixPatch
    public static void Prefix(AddEventParams params) {
        LoadoutMod.eventsToDisplay.add(params);
    }
}
