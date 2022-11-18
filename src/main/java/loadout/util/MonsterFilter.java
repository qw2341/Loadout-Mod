package loadout.util;

import org.apache.commons.lang3.StringUtils;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

import java.util.HashSet;

public class MonsterFilter implements ClassFilter {
    private final boolean isModded;
    public MonsterFilter(boolean isModded) {
        this.isModded = isModded;
    }
    //    private static HashSet<String> bList = new HashSet<>();
//    static {
//        bList.add("isaacModExtend.monsters.SirenHelper");
//        bList.add("HalationCode.monsters.ElsaMaria");
//    }
    private static final HashSet<String> wList = new HashSet<>();
    static {
        wList.add("downfall.monsters");
        wList.add("com.megacrit.cardcrawl.mod.replay.monsters.replay");
        wList.add("Gensokyo.monsters");
        wList.add("elementarium.monsters");
        wList.add("menagerie.monsters");
        wList.add("abyss.monsters");
    }


    @Override
    public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
        String cName = classInfo.getClassName();
        if(isModded) {
            return wList.stream().anyMatch(cName::contains) && !StringUtils.containsIgnoreCase(cName,"Helper") && !StringUtils.containsIgnoreCase(cName,"Abstract");
        } else
            return cName.contains("monsters");
    }
}
