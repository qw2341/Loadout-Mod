package loadout.patches;

import basemod.ReflectionHacks;
import basemod.animations.AbstractAnimation;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.esotericsoftware.spine.Skeleton;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class PlayerRenderPatch {
    @SpirePatch2(clz = AbstractPlayer.class, method = "render")
    public static class RenderPatch {
        public static void Raw(CtBehavior ctBehavior) throws CannotCompileException {
            ctBehavior.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if(m.getClassName().equals(SpriteBatch.class.getName())
                            && m.getMethodName().equals("draw")) {
                        m.replace("if(loadout.relics.TildeKey.isPlayerMorphed()) {" +
                                "loadout.patches.PlayerRenderPatch.myPlayerRender($0, this);} else {" +
                                "$_ = $proceed($$);}");
                    }
                    if(m.getMethodName().equals("renderPlayerImage")) {
                        m.replace("if(loadout.relics.TildeKey.isPlayerMorphed()) {" +
                                "loadout.patches.PlayerRenderPatch.myPlayerRender($$, this);} else {" +
                                "$_ = $proceed($$);}");
                    }
                }
            });
        }
    }

    public static AbstractAnimation animation = null;

    public static void myPlayerRender(SpriteBatch sb, AbstractPlayer player) {
        if (animation != null && animation.type() == AbstractAnimation.Type.SPRITE) {
            animation.renderSprite(sb, player.drawX + player.animX, player.drawY + player.animY + AbstractDungeon.sceneOffsetY);
        } else if (ReflectionHacks.getPrivate(player, AbstractPlayer.class,"atlas") == null) {
            sb.setColor(player.tint.color);
            sb.draw(player.img, player.drawX - (float)player.img.getWidth() * Settings.scale / 2.0F + player.animX, player.drawY + player.animY + AbstractDungeon.sceneOffsetY, (float)player.img.getWidth() * Settings.scale, (float)player.img.getHeight() * Settings.scale, 0, 0, player.img.getWidth(), player.img.getHeight(), player.flipHorizontal, player.flipVertical);
        } else {
            player.state.update(Gdx.graphics.getDeltaTime());
            Skeleton skeleton = ReflectionHacks.getPrivate(player, AbstractPlayer.class, "skeleton");
            player.state.apply(skeleton);
            skeleton.updateWorldTransform();
            skeleton.setPosition(player.drawX + player.animX, player.drawY + player.animY + AbstractDungeon.sceneOffsetY);
            skeleton.setColor(player.tint.color);
            skeleton.setFlip(player.flipHorizontal, player.flipVertical);
            sb.end();
            CardCrawlGame.psb.begin();
            AbstractPlayer.sr.draw(CardCrawlGame.psb, skeleton);
            CardCrawlGame.psb.end();
            sb.begin();
            sb.setBlendFunction(770, 771);
        }
    }

}
