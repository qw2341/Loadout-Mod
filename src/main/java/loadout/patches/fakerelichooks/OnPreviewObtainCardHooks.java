package loadout.patches.fakerelichooks;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.events.shrines.GremlinMatchGame;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.shop.ShopScreen;
import loadout.relics.AllInOneBag;

import java.util.ArrayList;

public class OnPreviewObtainCardHooks {
    @SpirePatch2(clz = AbstractDungeon.class, method = "getRewardCards")
    public static class DungeonHook {
        @SpirePostfixPatch
        public static ArrayList<AbstractCard> Postfix(ArrayList<AbstractCard> __result) {
            for(AbstractCard c : __result)
                AllInOneBag.INSTANCE.onPreviewObtainCard(c);
            return __result;
        }
    }

    @SpirePatch2(clz = TheLibrary.class, method = "buttonEffect")
    public static class TheLibraryHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class, localvars = {"card"})
        public static void Insert(AbstractCard card) {
            AllInOneBag.INSTANCE.onPreviewObtainCard(card);
        }
    }

    @SpirePatch2(clz = GremlinMatchGame.class, method = "initializeCards")
    public static class MatchGameHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class, localvars = {"c"})
        public static void Insert(AbstractCard c) {
            AllInOneBag.INSTANCE.onPreviewObtainCard(c);
        }
    }

    @SpirePatch2(clz = RewardItem.class, method = SpirePatch.CONSTRUCTOR, paramtypez = {AbstractCard.CardColor.class})
    public static class RewardItemHook {
        @SpireInsertPatch(locator = PlayerRelicsLocator.class, localvars = {"c"})
        public static void Insert(AbstractCard.CardColor colorType, AbstractCard c) {
            AllInOneBag.INSTANCE.onPreviewObtainCard(c);
        }
    }

    @SpirePatches2({
            @SpirePatch2(clz = ShopScreen.class, method = "initCards"),
            @SpirePatch2(clz = ShopScreen.class, method = "purchaseCard")})
    public static class ShopHook {
        @SpireInsertPatch(locator = PlayerRelicsLocatorAll.class, localvars = {"c"})
        public static void Insert(AbstractCard c) {
            AllInOneBag.INSTANCE.onPreviewObtainCard(c);
        }
    }
}
