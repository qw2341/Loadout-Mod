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
import com.megacrit.cardcrawl.helpers.TipTracker;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.ui.FtueTip;
import loadout.LoadoutMod;
import loadout.screens.RelicSelectScreen;
import loadout.util.TextureLoader;

import java.util.*;


import static loadout.LoadoutMod.*;

/**
 * Code from Hubris Mod's Backtick Relic, slightly modified
 */
public class LoadoutBag extends CustomRelic implements ClickableRelic {

    // ID, images, text.
    public static final boolean isIsaacMode = enableIsaacIcons || Loader.isModLoadedOrSideloaded("IsaacMod")||Loader.isModLoadedOrSideloaded("IsaacModExtend");
    public static final String ID = LoadoutMod.makeID("LoadoutBag");
    public static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("loadout_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("loadout_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("loadout_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("loadout_relic.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);
    private boolean relicSelected = true;
    public RelicSelectScreen relicSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;


    public Queue<AbstractRelic> relicSelection;

    public LoadoutBag() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);

        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
                tips.clear();
                //FieldAccessor.setFinalStatic(this.getClass().getDeclaredField("name"), relicStrings.NAME);
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        relicSelection = new LinkedList<AbstractRelic>();

    }

    @Override
    public String getUpdatedDescription()
    {
        return DESCRIPTIONS[0];
    }

    @Override
    public void onUnequip() {
        if(isSelectionScreenUp) {

            if(relicSelectScreen!=null) {
                isSelectionScreenUp = false;
                relicSelectScreen.close();
            }
        }
    }
    @Override
    public void onRightClick() {
        if (!isObtained||AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }

        if (LoadoutCauldron.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || BottledMonster.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(relicSelectScreen!=null) {
                isSelectionScreenUp = false;
                relicSelectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openRelicSelect();
    }



    private void openRelicSelect()
    {
        relicSelected = false;
        isSelectionScreenUp = true;
        //ArrayList<AbstractRelic> relics = relicsToDisplay;
        //sort if relic filter is enabled
        //boolean isSorting = Loader.isModLoadedOrSideloaded("RelicFilter")||Loader.isModLoadedOrSideloaded("Relic Filter")||Loader.isModLoadedOrSideloaded("RelicFilterMod");
        //logger.info("Relic Filter mod is installed: " + isSorting);
        try {
            if (this.relicSelectScreen == null) relicSelectScreen = new RelicSelectScreen(false,this);
        } catch (NoClassDefFoundError e) {
            logger.info("Error: RelicSelectScreen Class not found while opening relic select for bag!");
        }

        if (this.relicSelectScreen != null)
            relicSelectScreen.open(relicsToDisplay, relicObtainMultiplier);
    }

    @Override
    public void update()
    {
        super.update();
        if (!relicSelected && relicSelectScreen != null) {
            if (relicSelectScreen.doneSelecting()) {
                relicSelected = true;
                isSelectionScreenUp = false;
                //relicSelection.addAll(relicSelectScreen.getSelectedRelics());
                //getAndRemoveFirstRelic();
                ArrayList<AbstractRelic> relics = relicSelectScreen.getSelectedRelics();
                for (AbstractRelic r : relics) {
                    for(int i = 0; i< relicObtainMultiplier; i++) {
                        relicsToAdd.add(r.makeCopy());
                        r.playLandingSFX();
                        this.flash();
                    }
                }

            } else {
                relicSelectScreen.update();
                if (!hb.hovered) {
                    fakeHover = true;
                }
                hb.hovered = true;
            }
        }
    }

    @Override
    public void relicTip() {

    }

    @Override
    public void renderTip(SpriteBatch sb)
    {
        if (!relicSelected && fakeHover) {
            relicSelectScreen.render(sb);
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

        if (!relicSelected && !fakeHover) {
            relicSelectScreen.render(sb);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new LoadoutBag();
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
            CardCrawlGame.sound.play("RELIC_DROP_FLAT");
        }
    }
//    private AbstractRelic getAndRemoveFirstRelic() {
//        AbstractRelic r = relicSelection.poll();
//        r.makeCopy().instantObtain();
//        r.playLandingSFX();
//        return r;
//    }


}
