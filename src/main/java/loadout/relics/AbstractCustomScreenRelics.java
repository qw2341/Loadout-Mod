package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;


public abstract class AbstractCustomScreenRelics extends CustomRelic implements ClickableRelic {
    public static final boolean isIsaacMode = enableIsaacIcons || Loader.isModLoadedOrSideloaded("IsaacMod")||Loader.isModLoadedOrSideloaded("IsaacModExtend");

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);

    public static boolean isSelectionScreenUp = false;
    protected boolean itemSelected = false;
    public AbstractSelectScreen selectScreen;

    private boolean fakeHover = false;

    public AbstractCustomScreenRelics(String id, Texture texture, Texture outline, RelicTier tier, LandingSound sfx) {
        super(id, texture, outline, tier, sfx);
        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(id+"Alt");
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void relicTip() {

    }
    @Override
    public void onUnequip() {
        if(isSelectionScreenUp) {
            if(selectScreen !=null) {
                isSelectionScreenUp = false;
                selectScreen.close();
            }
        }
    }

    public abstract boolean isOtherRelicScreenOpen();

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || PowerGiver.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(selectScreen !=null) {
                isSelectionScreenUp = false;
                selectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openSelectScreen();
    }

    protected abstract void openSelectScreen();

    @Override
    public void update()
    {
        super.update();

        if (!itemSelected && selectScreen != null) {
            if (selectScreen.doneSelecting()) {
                itemSelected = true;
                isSelectionScreenUp = false;
            } else {
                selectScreen.update();
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
        if (!itemSelected && fakeHover) {
            selectScreen.render(sb);
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

        if (!itemSelected && !fakeHover) {
            selectScreen.render(sb);
        }
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
            super.playLandingSFX();
        }
    }
}
