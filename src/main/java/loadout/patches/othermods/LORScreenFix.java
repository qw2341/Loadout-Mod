package loadout.patches.othermods;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import lor.LORUtils;

import java.util.ArrayList;

@SpirePatch(clz = LORUtils.class, method = "openCardRewardsScreen", paramtypez = {ArrayList.class, boolean.class, int.class, String.class}, requiredModId = "Library of Ruina", optional = true)
public class LORScreenFix {
    public static void Prefix(ArrayList<AbstractCard> cards, boolean allowSkip, int appearnum, String text) {
        LoadoutMod.isScreenUp = true;
        InputHelper.isMouseDown = false;
    }

}
