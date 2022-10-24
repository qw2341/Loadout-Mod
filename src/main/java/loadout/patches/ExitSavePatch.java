package loadout.patches;

import basemod.eventUtil.AddEventParams;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.evacipated.cardcrawl.modthespire.lib.*;
import javassist.CtBehavior;
import loadout.LoadoutMod;

import java.io.IOException;

@SpirePatch(clz = HeadlessApplication.class, method = "exit", paramtypez = {})
public class ExitSavePatch {

    @SpirePrefixPatch
    public static void Prefix() {
        try {
            LoadoutMod.favorites.save();
        } catch (IOException e) {
            LoadoutMod.logger.info("Failed to save favorites");
        }
    }


}
