package loadout.util;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CtClass;
import loadout.screens.MonsterSelectScreen;
import org.clapper.util.classutil.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static loadout.LoadoutMod.*;

public class MonsterAdder extends AbstractAdder{

    public static AndClassFilter filter = new AndClassFilter(new ClassFilter[]{(ClassFilter) new NotClassFilter((ClassFilter) new InterfaceOnlyClassFilter()), (ClassFilter) new NotClassFilter((ClassFilter) new AbstractClassFilter()), (ClassFilter) new ClassModifiersClassFilter(1), new SuperClassFilter(clazzPool, AbstractMonster.class)});

    public MonsterAdder(URL url, String threadName) throws URISyntaxException {
        super(url, threadName);
    }

    public MonsterAdder(String filePath, String threadName) {
        super(filePath, threadName);
    }

    @Override
    public void run() {
        list = new ArrayList<>();
        finder.findClasses(list, filter);
        int len = list.size();
        for (int i = 0; i< len; i++) {
            ClassInfo classInfo = list.get(i);
            try {
                CtClass cls = clazzPool.get(classInfo.getClassName());
                if(monsterMap.containsKey(cls.getName())) continue;

                Class<?extends AbstractMonster> monsterC = (Class<? extends AbstractMonster>) clazzLoader.loadClass(cls.getName());
                monsterMap.put(cls.getName(), monsterC);

            } catch (Exception|Error e) {
                e.printStackTrace();
                //logger.info("Failed to initialize for " + classInfo.getClassName());
            }
        }
        logger.info("Thread "+ threadName +" finished adding monster!");
        finish();
    }
}
