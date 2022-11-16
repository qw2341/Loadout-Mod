package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.*;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Cultist;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.MonsterSelectScreen;
import loadout.screens.PowerSelectScreen;
import loadout.util.TextureLoader;

import static com.megacrit.cardcrawl.core.AbstractCreature.sr;
import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;

public class BottledMonster extends CustomRelic implements ClickableRelic {
    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("BottledMonster");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("bottle_relic.png")) : TextureLoader.getTexture(makeRelicPath("bottle_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic.png")) : TextureLoader.getTexture(makeRelicOutlinePath("bottle_relic.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);

    private boolean monsterSelected = true;
    public MonsterSelectScreen monsterSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;


    public BottledMonster() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    @Override
    public String getUpdatedDescription()
    {
        return DESCRIPTIONS[0];
    }

    @Override
    public void relicTip() {

    }
    @Override
    public void onUnequip() {
        if(isSelectionScreenUp) {
            if(monsterSelectScreen !=null) {
                isSelectionScreenUp = false;
                monsterSelectScreen.close();
            }
        }
    }

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(monsterSelectScreen !=null) {
                isSelectionScreenUp = false;
                monsterSelectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openMonsterSelect();
    }



    private void openMonsterSelect()
    {
        monsterSelected = false;
        isSelectionScreenUp = true;
        try {
            if (this.monsterSelectScreen == null) monsterSelectScreen = new MonsterSelectScreen(this);
        } catch (NoClassDefFoundError e) {
            logger.info("Error: PowerSelectScreen Class not found while opening potion select for Potion of Powers!");
        }
        if (this.monsterSelectScreen != null) monsterSelectScreen.open();
    }

    @Override
    public void update()
    {
        super.update();

        if (!monsterSelected && monsterSelectScreen != null) {
            if (monsterSelectScreen.doneSelecting()) {
                monsterSelected = true;
                isSelectionScreenUp = false;
            } else {
                monsterSelectScreen.update();
                if (!hb.hovered) {
                    fakeHover = true;
                }
                hb.hovered = true;
            }
        }
    }

    @Override
    public void renderTip(SpriteBatch sb)
    {
        if (!monsterSelected && fakeHover) {
            monsterSelectScreen.render(sb);
        }
        if (fakeHover) {
            fakeHover = false;
            hb.hovered = false;
        } else {
            super.renderTip(sb);
        }
    }


    @Override
    public void renderInTopPanel(SpriteBatch sb)
    {
        super.renderInTopPanel(sb);

        if (!monsterSelected && !fakeHover) {
            monsterSelectScreen.render(sb);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new BottledMonster();
    }

    @Override
    public void playLandingSFX() {
        if (isIsaacMode) {
            if (CardCrawlGame.MUTE_IF_BG && Settings.isBackgrounded) {
                return;
            } else if (landingSfx != null) {
                landingSfx.play(Settings.SOUND_VOLUME * Settings.MASTER_VOLUME);
            } else {
                logger.info("Missing landing sound!");
            }
        } else {
            CardCrawlGame.sound.play("RELIC_DROP_HEAVY");
        }
    }
}
