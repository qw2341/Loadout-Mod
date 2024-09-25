package loadout.util;

import com.megacrit.cardcrawl.cards.AbstractCard;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import loadout.LoadoutMod;
import loadout.patches.AdditionalUpgradePatches;
import loadout.savables.CardModifications;
import org.clapper.util.classutil.*;

import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Took some inspiration from Occult Patch from anniv5, the Packmaster
 * and BaseMod's WhatMod
 */
public class CardClassPatcher implements Runnable{
    private ClassFinder finder;
    private String threadName;
    private ClassPool clazzPool;
    ArrayList<ClassInfo> list;
    private Thread t;

    public CardClassPatcher(URL url, String threadName, ClassPool clazzPool) throws URISyntaxException {
        this.finder = new ClassFinder();
        this.threadName = threadName;
        finder.add(new java.io.File(url.toURI()));
        this.clazzPool = clazzPool;
    }

    public CardClassPatcher(String filePath, String threadName, ClassPool clazzPool) {
        this.finder = new ClassFinder();
        this.threadName = threadName;
        finder.add(new java.io.File(filePath));
        this.clazzPool = clazzPool;
    }

    public void start() {
//        if(t==null) {
//            t = new Thread(this);
//            t.start();
//        }
        run();
    }

    @Override
    public void run() {
        list = new ArrayList<>();

        ClassFilter filter = new AndClassFilter(
                new NotClassFilter(new InterfaceOnlyClassFilter()),
                new NotClassFilter(new AbstractClassFilter()),
                new ClassModifiersClassFilter(Modifier.PUBLIC),
                new SuperClassFilter(this.clazzPool, AbstractCard.class)
        );

        finder.findClasses(list, filter);
        String src = CardModifications.class.getName() + ".modifyIfExist(this);";

        String upgradePatch = AdditionalUpgradePatches.class.getName() + ".additionalUpgrade(this)";
        for(ClassInfo classInfo : list) {
            try {
                CtClass ctClass = this.clazzPool.get(classInfo.getClassName());
                for (CtConstructor ctor : ctClass.getDeclaredConstructors()) {
                    ctor.insertAfter(src);
                }
//                LoadoutMod.logger.info("Finished patching {}!", classInfo.getClassName());
                ctClass.getDeclaredMethod("upgrade").insertAfter(upgradePatch);
            } catch (Exception ignored) {
                LoadoutMod.logger.info("Error patching {}!", classInfo.getClassName());
            }
        }
    }
}
