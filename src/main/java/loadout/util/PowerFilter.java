package loadout.util;

import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

public class PowerFilter implements ClassFilter {

        public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
            if (classInfo.getClassName().contains("powers"))
                return true;
            return false;
        }

}
