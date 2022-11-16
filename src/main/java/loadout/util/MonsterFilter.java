package loadout.util;

import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

public class MonsterFilter implements ClassFilter {
    @Override
    public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
        return classInfo.getClassName().contains("monsters");
    }
}
