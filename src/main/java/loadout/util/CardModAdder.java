package loadout.util;

import basemod.abstracts.AbstractCardModifier;
import javassist.CtClass;
import org.clapper.util.classutil.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static loadout.LoadoutMod.*;

public class CardModAdder extends AbstractAdder{

    public static AndClassFilter filter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new SuperClassFilter(clazzPool, AbstractCardModifier.class)});

    public CardModAdder(URL url, String threadName) throws URISyntaxException {
        super(url, threadName);
    }


    @Override
    public void run() {
        list = new ArrayList<>();
        finder.findClasses(list, (ClassFilter) filter);
        for(ClassInfo classInfo : list) {
            try {
                CtClass cls = clazzPool.get(classInfo.getClassName());
                Class<?extends AbstractCardModifier> acmC = (Class<? extends AbstractCardModifier>) clazzLoader.loadClass(cls.getName());
                if(cardModMap.containsKey(acmC.getName())) {
                    continue;
                }
                cardModMap.put(acmC.getName(),acmC);

            } catch (Exception ignored) {

            }

        }
        logger.info("Thread "+ threadName +" finished adding Card Mods!");
        finish();
    }
}
