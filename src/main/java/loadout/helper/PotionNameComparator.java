package loadout.helper;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import loadout.LoadoutMod;

import java.util.Comparator;

public class PotionNameComparator implements Comparator<AbstractPotion> {

    public PotionNameComparator() {
        super();
        //initChineseSupport();
    }

    @Override
    public int compare(AbstractPotion o1, AbstractPotion o2) {
        String n1 = o1.name;
        String n2 = o2.name;

        if (LoadoutMod.languageSupport().equals("zhs")||!n1.substring(0,1).matches("[A-Za-z\\d]+")||!n2.substring(0,1).matches("[A-Za-z\\d]+")) {
            //return compareCharToChar(o1.name,o2.name,0);
            String i1 = PotionNameComparator.editModPotionId(o1.ID);
            String i2 = PotionNameComparator.editModPotionId(o2.ID);
            return i1.compareToIgnoreCase(i2);
        }
        return n1.compareToIgnoreCase(n2);
    }

    @Override
    public Comparator<AbstractPotion> reversed() {
        return (o1,o2) -> -compare(o1,o2);
    }


    public static String editModPotionId(String rawModId) {
        String retId = rawModId;
        if(Loader.isModLoadedOrSideloaded("RelicUpgradeLib")||Loader.isModLoadedOrSideloaded("RelicUpgradeMOD_OPM")) {
            if (retId.substring(0,3).matches("RU ")) {
                retId = retId.substring(3);
            }
        }
        if(retId.contains(":")) {
            retId = retId.split(":")[1];
        }
        return retId;
    }
}
