package loadout.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

/**
 * From BaseMod's Potion Tips class
 */
public class SuperClassFilter implements ClassFilter{
        private ClassPool pool;
        private CtClass baseClass;

        public SuperClassFilter(ClassPool pool, Class<?> baseClass) throws NotFoundException {
            this.pool = pool;
            this.baseClass = pool.get(baseClass.getName());
        }

        public boolean accept(ClassInfo classInfo, ClassFinder classFinder) {
            try {
                for(CtClass ctClass = this.pool.get(classInfo.getClassName()); ctClass != null; ctClass = ctClass.getSuperclass()) {
                    if (ctClass.equals(this.baseClass)) {
                        return true;
                    }
                }
            } catch (NotFoundException ignored) {
            }

            return false;
        }
}
