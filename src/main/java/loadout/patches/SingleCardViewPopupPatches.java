package loadout.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;

import loadout.portraits.CardPortraitManager;

public class SingleCardViewPopupPatches {
    
    @SpirePatch(clz = SingleCardViewPopup.class, method = "loadPortraitImg", paramtypez = {})
    public static class LoadPortraitPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(SingleCardViewPopup __instance, AbstractCard ___card) {
            String assetId = CardPortraitManager.INSTANCE.getResolvedAssetId(___card);
            if (assetId != null) {
                Texture portraitTexture = CardPortraitManager.INSTANCE.getLargeDisposableTexture(assetId);
                if (portraitTexture != null) {
                    ReflectionHacks.setPrivate(__instance, SingleCardViewPopup.class, "portraitImg", portraitTexture);
                    return SpireReturn.Return();
                }
            }

            return SpireReturn.Continue();
        }
    }
}
