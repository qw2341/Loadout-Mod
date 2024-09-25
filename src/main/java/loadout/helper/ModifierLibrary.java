package loadout.helper;

import basemod.abstracts.AbstractCardModifier;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import com.jcraft.jorbis.Block;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import loadout.LoadoutMod;
import loadout.cardmods.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ModifierLibrary {
    public static HashMap<String,Class<? extends AbstractCardModifier>> modifiers = new HashMap<>();
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardModifierStrings"));
    public static String[] TEXT = uiStrings.TEXT;
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
        modifiers.put(RandomUpgradeOnKillMod.ID, RandomUpgradeOnKillMod.class);
        modifiers.put(GainDamageOnKill.ID, GainDamageOnKill.class);
        modifiers.put(GainMagicOnKillMod.ID, GainMagicOnKillMod.class);
        modifiers.put(LifestealMod.ID, LifestealMod.class);
        modifiers.put(InevitableMod.ID, InevitableMod.class);
        modifiers.put(InfiniteUpgradeMod.ID, InfiniteUpgradeMod.class);
        modifiers.put(DieNextTurnMod.ID, DieNextTurnMod.class);

        modifiers.put(StickyMod.ID, StickyMod.class);
        modifiers.put(DamageMod.ID, DamageMod.class);
        modifiers.put(BlockMod.ID, BlockMod.class);
        modifiers.put(DamageAOEMod.ID, DamageAOEMod.class);
        modifiers.put(DrawMod.ID, DrawMod.class);
        modifiers.put(DiscardMod.ID, DiscardMod.class);
        modifiers.put(ExhaustCardMod.ID, ExhaustCardMod.class);

        LifestealMod.onLoad();
        DieNextTurnMod.onLoad();

        DamageMod.onLoad();
        BlockMod.onLoad();
        DamageAOEMod.onLoad();
    }

    public static AbstractCardModifier getModifier(String id) {
        Class<? extends AbstractCardModifier> modC;
        try {
            if(modifiers.containsKey(id)) {
                modC = modifiers.get(id);
            } else {
                modC = tryGetModifier(id);
                if(modC != null) {
                    modifiers.put(id, modC);
                } else {
                    LoadoutMod.logger.error("Could not find modifier: " + id);
                    return null;
                }
            }

            return modC.getDeclaredConstructor(new Class[0]).newInstance();
        } catch (Exception e) {
            LoadoutMod.logger.error("Error importing card modifiers for card modifier: " + id);
        }
        return null;
    }

    public static void initOtherModifiers() {
        for (Class<? extends AbstractCardModifier> modClass : LoadoutMod.cardModMap.values()) {
            String ID;
            try {
                ID = (String) modClass.getDeclaredField("ID").get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                ID = modClass.getSimpleName();
            }

            modifiers.putIfAbsent(ID, modClass);
        }
    }

    protected static Class<? extends AbstractCardModifier> tryGetModifier(String ID) {
        String longID = LoadoutMod.cardModIDMap.get(ID);
        return LoadoutMod.cardModMap.get(longID);
    }
}
