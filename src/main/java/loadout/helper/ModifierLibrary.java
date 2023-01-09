package loadout.helper;

import basemod.abstracts.AbstractCardModifier;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import loadout.LoadoutMod;
import loadout.cardmods.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ModifierLibrary {
    public static HashMap<String,Class<? extends AbstractCardModifier>> modifiers = new HashMap<>();
    public static void initialize() {
        modifiers.put(EtherealMod.ID,EtherealMod.class);
        modifiers.put(ExhaustMod.ID,ExhaustMod.class);
        modifiers.put(InnateMod.ID,InnateMod.class);
        modifiers.put(RetainMod.ID,RetainMod.class);

        modifiers.put(PlayableMod.ID,PlayableMod.class);
        modifiers.put(UnexhaustMod.ID,UnexhaustMod.class);
        modifiers.put(UnplayableMod.ID,UnplayableMod.class);
        modifiers.put(XCostMod.ID,XCostMod.class);
        modifiers.put(UnetherealMod.ID,UnetherealMod.class);

        modifiers.put(AutoplayMod.ID, AutoplayMod.class);
        modifiers.put(FleetingMod.ID, FleetingMod.class);
        modifiers.put(GraveMod.ID, GraveMod.class);
        modifiers.put(SoulboundMod.ID,SoulboundMod.class);

        modifiers.put(GainGoldOnKillMod.ID, GainGoldOnKillMod.class);
        modifiers.put(GainHpOnKillMod.ID,GainHpOnKillMod.class);
        modifiers.put(GainGoldOnPlayMod.ID, GainGoldOnPlayMod.class);
        modifiers.put(HealOnPlayMod.ID, HealOnPlayMod.class);
    }

    public static AbstractCardModifier getModifier(String id) {
        try {
            return modifiers.get(id).getDeclaredConstructor(new Class[0]).newInstance();
        } catch (Exception e) {
            LoadoutMod.logger.error("Error importing card modifiers for card modifier: " + id);
        }
        return null;
    }
}
