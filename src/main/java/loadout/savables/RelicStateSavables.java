package loadout.savables;

import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.patches.AbstractRelicPatches;

import java.util.ArrayList;

public class RelicStateSavables implements CustomSavable<ArrayList<int[]>> {
    public static final String ID = LoadoutMod.makeID("RELIC_STATES");
    @Override
    public ArrayList<int[]> onSave() {
        ArrayList<int[]> ret = new ArrayList<>();
        int i = 0;
        for(AbstractRelic r: AbstractDungeon.player.relics) {
            if(AbstractRelicPatches.RelicCounterFields.isCounterLocked.get(r)){
                ret.add(new int[] {i, AbstractRelicPatches.RelicCounterFields.counterLockAmount.get(r)});
            }
            i++;
        }
        return ret;
    }

    @Override
    public void onLoad(ArrayList<int[]> sav) {
        try{
            for (int[] state : sav) {
                AbstractRelic r = AbstractDungeon.player.relics.get(state[0]);
                AbstractRelicPatches.RelicCounterFields.isCounterLocked.set(r, Boolean.TRUE);
                r.counter = state[1];
                AbstractRelicPatches.RelicCounterFields.counterLockAmount.set(r, state[1]);
            }
        } catch (Exception e) {
            LoadoutMod.logger.warn("Failed to load relic state saves!");
            e.printStackTrace();
        }
    }
}
