package loadout.util;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import javassist.CtClass;
import org.clapper.util.classutil.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import static loadout.LoadoutMod.*;

public class OrbAdder extends AbstractAdder{

    public static AndClassFilter filter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new SuperClassFilter(clazzPool, AbstractOrb.class)});


    public OrbAdder(URL url, String threadName) throws URISyntaxException {
        super(url, threadName);
    }

    @Override
    public void run() {
        list = new ArrayList<>();
        finder.findClasses(list, (ClassFilter) filter);
        for(ClassInfo classInfo : list) {
            try {
                CtClass cls = clazzPool.get(classInfo.getClassName());
                Class<?extends AbstractOrb> orbC = (Class<? extends AbstractOrb>) clazzLoader.loadClass(cls.getName());
                if(orbMap.containsKey(orbC.getName())) {
                    continue;
                }
                orbMap.put(orbC.getName(),orbC);

            } catch (Exception ignored) {

            }

        }
        logger.info("Thread "+ threadName +" finished adding orbs!");
        finish();
    }
}
