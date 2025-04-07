package loadout.util;

import com.evacipated.cardcrawl.modthespire.Loader;
import javassist.ClassPool;
import loadout.LoadoutMod;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import static loadout.LoadoutMod.logger;
import static loadout.LoadoutMod.startTime;

public abstract class AbstractAdder implements Runnable {
    private Thread t;
    protected String threadName;
    protected ClassFinder finder;

    public static ClassPool clazzPool = Loader.getClassPool();
    public static ClassLoader clazzLoader = clazzPool.getClassLoader();
    ArrayList<ClassInfo> list;

    public AbstractAdder(URL url, String threadName) throws URISyntaxException {
        this.finder = new ClassFinder();
        this.threadName = threadName;
        finder.add(new java.io.File(url.toURI()));
    }

    public AbstractAdder(String filePath, String threadName) {
        this.finder = new ClassFinder();
        this.threadName = threadName;
        finder.add(new java.io.File(filePath));
    }

    public void start() {
        if(t==null) {
            t = new Thread(this);
            t.start();
        }
    }

    protected void finish() {
        if(++LoadoutMod.numThreadsFinished == LoadoutMod.numThreadsTotal) {
            //if all finished
            long timeTaken = System.currentTimeMillis() - startTime;
            logger.info("Fnished auto adding stuff! Time Elapsed: " + timeTaken + "ms");
        }
    }
}
