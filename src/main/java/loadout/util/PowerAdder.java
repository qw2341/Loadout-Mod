package loadout.util;

import com.megacrit.cardcrawl.powers.AbstractPower;
import javassist.CtClass;
import loadout.LoadoutMod;
import org.clapper.util.classutil.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static loadout.LoadoutMod.logger;

public class PowerAdder extends AbstractAdder{

    public static AndClassFilter filter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new SuperClassFilter(clazzPool, AbstractPower.class)});

    public PowerAdder(URL url, String threadName) throws URISyntaxException {
        super(url, threadName);
    }

    @Override
    public void run() {
        list = new ArrayList<>();
        finder.findClasses(list, (ClassFilter) filter);
        for(ClassInfo classInfo : list) {
            try {
                CtClass cls = clazzPool.get(classInfo.getClassName());
                Class<?extends AbstractPower> powerC = (Class<? extends AbstractPower>) clazzLoader.loadClass(cls.getName());
                try{
                    Class.forName(powerC.getName(),false,clazzLoader);
                } catch (ClassNotFoundException|NoClassDefFoundError cnfe) {
                    logger.info(powerC.getName() + "does not exist");
                    continue;
                }

                String pID = null;

                pID = powerC.getName();
                if(LoadoutMod.powersToDisplay.containsKey(pID)) {
                    continue;
                }

                LoadoutMod.powersToDisplay.put(pID, (Class<? extends AbstractPower>) powerC);

            } catch (Exception ignored) {

            }

        }
        logger.info("Thread "+ threadName +" finished adding power!");
    }
}
