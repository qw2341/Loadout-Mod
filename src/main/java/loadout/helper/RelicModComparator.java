package loadout.helper;

import java.util.Comparator;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import basemod.patches.whatmod.WhatMod;


public class RelicModComparator implements Comparator<AbstractRelic> {
    private RelicNameComparator rNC = RelicNameComparator.INSTANCE;
    public static final RelicModComparator INSTANCE = new RelicModComparator();
    public RelicModComparator() {
        super();
    }

    @Override
    public int compare(AbstractRelic o1, AbstractRelic o2) {
        String r1 = getModName(o1);
        String r2 = getModName(o2);
        int ret = r1.compareToIgnoreCase(r2);
        return (ret==0)? rNC.compare(o1,o2):ret;
    }

    @Override
    public Comparator<AbstractRelic> reversed() {
        return (o1,o2) -> {
            int ret = getModName(o1).compareToIgnoreCase(getModName(o2));
            return (ret==0)? rNC.compare(o1,o2):-ret;
        };
    }

    /**
     * Retrieves the mod name from a relic's ID
     * @param a
     * @return The mod name of the relic a
     */
    public static String getModName(AbstractRelic a) {
        String modName = WhatMod.findModName(a.getClass());

        return modName==null? "Slay the Spire" : modName;
    }

    public static String getModDesc(Object a) {

        String modId = WhatMod.findModID(a.getClass());

        if(modId == null) {
            return "StsOrigPlaceholder";
        }

        ModInfo[] modInfos = Loader.MODINFOS;
        int modNum = modInfos.length;
        for (int i = 0; i<modNum; i++) {
            ModInfo info = modInfos[i];
            if (info.ID.equalsIgnoreCase(modId)) {
                return info.Description;
            }
        }
        return "";
    }

}
