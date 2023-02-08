package loadout.savables;

import java.io.Serializable;
import java.util.HashMap;

public class RelicSavables implements Serializable {
    public Integer[][] modifierSave;
    public HashMap<String, Integer>[] powerGiverSave;
    public HashMap<String, String> tildeKeySave;

    public RelicSavables(Integer[][] modifierSave, HashMap<String, Integer>[] powerGiverSave, HashMap<String, String> tildeKeySave) {
        this.modifierSave = modifierSave;
        this.powerGiverSave = powerGiverSave;
        this.tildeKeySave = tildeKeySave;
    }
}
