package loadout.util;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import javassist.CtClass;
import loadout.screens.OrbSelectScreen;
import org.clapper.util.classutil.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

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
//                OrbSelectScreen.OrbButton ob;
//                try{
////                    Class.forName(orbC.getName(),false,clazzLoader);
//                    //ob = new OrbSelectScreen.OrbButton(orbC.getDeclaredConstructor(new Class[]{}).newInstance(null));
//                } catch (NoClassDefFoundError|NoSuchMethodException cnfe) {
//                    //logger.info(orbC.getName() + "does not exist");
//                    continue;
//                } catch (Exception|Error e) {
//                    e.printStackTrace();
//                    //logger.info("Other error occurred while creating button for " + orbC.getName());
//                    continue;
//                }

                if(orbMap.containsKey(orbC.getName())) {
                    continue;
                }
                orbMap.put(orbC.getName(),orbC);
                //orbsToDisplay.add(ob);

            } catch (Exception ignored) {

            }

        }
        logger.info("Thread "+ threadName +" finished adding orbs!");
        finish();
    }
}
