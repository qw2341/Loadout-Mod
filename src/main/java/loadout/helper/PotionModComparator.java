package loadout.helper;

import basemod.patches.whatmod.WhatMod;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.megacrit.cardcrawl.potions.AbstractPotion;

import java.util.Comparator;


public class PotionModComparator implements Comparator<AbstractPotion> {
    private PotionNameComparator pNC;

    public PotionModComparator() {
        super();
        pNC = new PotionNameComparator();
    }

    @Override
    public int compare(AbstractPotion o1, AbstractPotion o2) {
        String r1 = getModName(o1);
        String r2 = getModName(o2);
        int ret = r1.compareToIgnoreCase(r2);
        return (ret==0)? pNC.compare(o1,o2):ret;
    }

    @Override
    public Comparator<AbstractPotion> reversed() {
        return (o1,o2) -> {
            int ret = getModName(o1).compareToIgnoreCase(getModName(o2));
            return (ret==0)? pNC.compare(o1,o2):-ret;
        };
    }

    /**
     * Retrieves the mod name from a relic's ID
     * @param a
     * @return The mod name of the relic a
     */
    public static String getModName(Object a) {
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
