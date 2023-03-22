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
        boolean isModded = !threadName.equals("StSMonsterThread");
        finder.findClasses(list, filter);
        int len = list.size();
        for (int i = 0; i< len; i++) {
            ClassInfo classInfo = list.get(i);
            try {
                CtClass cls = clazzPool.get(classInfo.getClassName());
                //logger.info("Class: " + classInfo.getClassName() + (isPower ? " is Power": isMonster ? " is Monster" : " is neither"));
                if(monsterIDS.contains(cls.getName())) continue;

                monsterIDS.add(cls.getName());

                Class<?extends AbstractMonster> monsterC = (Class<? extends AbstractMonster>) clazzLoader.loadClass(cls.getName());
                //logger.info("Trying to create monster button for: " + monsterC.getName());
                if(monsterC.getName().equals("isaacModExtend.monsters.SirenHelper") || monsterC.getName().equals("HalationCode.monsters.ElsaMaria") ) continue;
                try{
                    monstersToDisplay.add(new MonsterSelectScreen.MonsterButton(monsterC, isModded));
                } catch (Exception e) {
                    logger.info("Failed to create monster button for: " + monsterC.getName());
                    continue;
                } catch (NoClassDefFoundError noClassDefFoundError) {
                    continue;
                }

            } catch (Exception e) {
                logger.info("Failed to initialize for " + classInfo.getClassName());
            }
        }
        logger.info("Thread ${threadName} finished adding monster!");
    }
}
