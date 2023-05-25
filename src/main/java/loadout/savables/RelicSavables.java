package loadout.savables;

import java.io.Serializable;
import java.util.HashMap;

public class RelicSavables implements Serializable {
    public Object[][] modifierSave;
    public HashMap<String, Integer>[] powerGiverSave;
    public HashMap<String, String> tildeKeySave;

    public RelicSavables(Object[][] modifierSave, HashMap<String, Integer>[] powerGiverSave, HashMap<String, String> tildeKeySave) {
        this.modifierSave = modifierSave;
        this.powerGiverSave = powerGiverSave;
        this.tildeKeySave = tildeKeySave;
    }
}
