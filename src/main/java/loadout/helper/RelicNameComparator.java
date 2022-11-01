package loadout.helper;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
//import loadout.lib.net.sourceforge.pinyin4j.*;
//import loadout.lib.net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Comparator;

public class RelicNameComparator implements Comparator<AbstractRelic> {
    //private HanyuPinyinOutputFormat hpy;
    public static final RelicNameComparator INSTANCE = new RelicNameComparator();

    public RelicNameComparator() {
        super();
        //initChineseSupport();
    }

    @Override
    public int compare(AbstractRelic o1, AbstractRelic o2) {
        String n1 = getComparableRelicName(o1);
        String n2 = getComparableRelicName(o2);
        return n1.compareToIgnoreCase(n2);
    }

    @Override
    public Comparator<AbstractRelic> reversed() {
        return (o1,o2) -> -compare(o1,o2);
    }


    public static String editModRelicId(String rawModId) {
        String retId = rawModId;
        if(Loader.isModLoadedOrSideloaded("RelicUpgradeLib")||Loader.isModLoadedOrSideloaded("RelicUpgradeMOD_OPM")) {
            if (retId.length() > 3 && retId.substring(0,3).matches("RU ")) {
                retId = retId.substring(3);
            }
        }
        if(retId.contains(":")) {
            retId = retId.split(":")[1];
        }
        return retId;
    }

    public static String getComparableRelicName(AbstractRelic a) {
        if (LoadoutMod.languageSupport().equals("zhs")||LoadoutMod.languageSupport().equals("zht"))
            return RelicNameComparator.editModRelicId(a.relicId);
            //return compareCharToChar(o1.name,o2.name,0);
        if (a.name.length() >0 && !a.name.substring(0,1).matches("[A-Za-z\\d]+"))
            return RelicNameComparator.editModRelicId(a.relicId);
        return a.name;
    }

}
