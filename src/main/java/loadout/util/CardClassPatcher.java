package loadout.util;

import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.ClassModifiersClassFilter;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;

import com.megacrit.cardcrawl.cards.AbstractCard;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.expr.MethodCall;
import loadout.LoadoutMod;
import loadout.patches.AdditionalUpgradePatches;
import loadout.savables.CardModifications;

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

        String upgradePatchBgn = AdditionalUpgradePatches.class.getName() + ".additionalUpgrade(this, ";
        Boolean[] doUpgrade = new Boolean[]{true, true, true, true};
        String upgradePatchEnd = ");";
        /**
         * Dynamic Patches
         */
        for(ClassInfo classInfo : list) {
            try {
                CtClass ctClass = this.clazzPool.get(classInfo.getClassName());
                for (CtConstructor ctor : ctClass.getDeclaredConstructors()) {
                    ctor.insertAfter(src);
                }
//                LoadoutMod.logger.info("Finished patching {}!", classInfo.getClassName());
                CtMethod upgradeMethod = ctClass.getDeclaredMethod("upgrade");
                
                //Now Scan the method str for calls to upgradeDamage, upgradeBlock, upgradeMagicNumber, upgradeBaseCost
                //And set the booleans accordingly: cost, damage, block, magic
                //convert to string and concat the upgrade method call string, and finally inserts it
                upgradeMethod.instrument(new javassist.expr.ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        String methodName = m.getMethodName();
                        if (methodName.equals("upgradeBaseCost")) doUpgrade[0] = false;
                        else if (methodName.equals("upgradeDamage")) doUpgrade[1] = false;
                        else if (methodName.equals("upgradeBlock")) doUpgrade[2] = false;
                        else if (methodName.equals("upgradeMagicNumber")) doUpgrade[3] = false;
                    }
                });
                upgradeMethod.insertAfter(upgradePatchBgn + doUpgrade[0] + ", " + doUpgrade[1] + ", " + doUpgrade[2] + ", " + doUpgrade[3] + upgradePatchEnd);
            } catch (Exception ignored) {
                LoadoutMod.logger.info("Error patching {}!", classInfo.getClassName());
                //ignored.printStackTrace();
            }
            Arrays.fill(doUpgrade, true);
        }
    }
}
