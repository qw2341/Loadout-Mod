package loadout.patches.othermods;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import ruina.cardmods.BlackSilenceRenderMod;

@SpirePatch(clz= BlackSilenceRenderMod.class, method = "renderHelper", paramtypez = {SpriteBatch.class, Color.class, TextureAtlas.AtlasRegion.class, float.class, float.class, AbstractCard.class}, requiredModId = "ruina", optional = true)
public class RuinaFix {
    @SpirePrefixPatch
    public static SpireReturn<Void> Prefix(SpriteBatch sb, Color color, TextureAtlas.AtlasRegion img, float drawX, float drawY, AbstractCard q) {
        if(sb == null || img == null || q == null) return SpireReturn.Return();
        return SpireReturn.Continue();
    }
}
