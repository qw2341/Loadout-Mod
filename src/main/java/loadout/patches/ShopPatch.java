package loadout.patches;

import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import loadout.relics.TildeKey;

public class ShopPatch {

    public static void modifyPrices(ShopScreen shopScreen) {
        int oldActualCost = ShopScreen.actualPurgeCost;
        shopScreen.applyDiscount(TildeKey.merchantPriceMult/ 100f, true);
        ShopScreen.actualPurgeCost = Math.round(oldActualCost * (TildeKey.merchantPriceMult/ 100f));
    }

    @SpirePatch2(clz = ShopScreen.class, method = "init")
    public static class InitPatch {
        @SpirePostfixPatch
        public static void Postfix(ShopScreen __instance) {
            modifyPrices(__instance);
        }
    }


    @SpirePatch2(clz = ShopScreen.class, method = "getNewPrice", paramtypez = {StoreRelic.class})
    public static class NewRelicPatch {
        @SpirePostfixPatch
        public static void Postfix(ShopScreen __instance, StoreRelic r) {
            r.price = MathUtils.round((float)r.price * (TildeKey.merchantPriceMult/ 100f));
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "getNewPrice", paramtypez = {StorePotion.class})
    public static class NewPotionPatch {
        @SpirePostfixPatch
        public static void Postfix(ShopScreen __instance, StorePotion r) {
            r.price = MathUtils.round((float)r.price * (TildeKey.merchantPriceMult/ 100f));
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "setPrice", paramtypez = {AbstractCard.class})
    public static class NewCardPatch {
        @SpirePostfixPatch
        public static void Postfix(ShopScreen __instance, AbstractCard card) {
            card.price = MathUtils.round((float)card.price * (TildeKey.merchantPriceMult/ 100f));
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "purgeCard")
    public static class PurgeCardPatch {
        @SpirePostfixPatch
        public static void Postfix() {

        }
    }
}
