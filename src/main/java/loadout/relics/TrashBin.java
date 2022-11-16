package loadout.relics;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.RelicSelectScreen;
import loadout.util.TextureLoader;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;
import static loadout.relics.LoadoutBag.landingSfx;

public class TrashBin extends CustomRelic implements ClickableRelic {

    public static final String ID = LoadoutMod.makeID("TheBin");

    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("thebin_relic.png"));

    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("thebin_relic.png"));

    private boolean relicSelected = true;
    private RelicSelectScreen relicSelectScreen;
    private boolean fakeHover = false;
    public static boolean isSelectionScreenUp = false;


    public TrashBin() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);

        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
                //FieldAccessor.setFinalStatic(this.getClass().getDeclaredField("name"), relicStrings.NAME);
                tips.clear();
                flavorText = relicStrings.FLAVOR;
                tips.add(new PowerTip(relicStrings.NAME, description));

            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (LoadoutBag.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp || TildeKey.isSelectionScreenUp || BottledMonster.isSelectionScreenUp)
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

    private void openRelicSelect() {
        relicSelected = false;
        isSelectionScreenUp = true;

        ArrayList<AbstractRelic> playerRelics = AbstractDungeon.player.relics;

        ArrayList<AbstractRelic> relics = new ArrayList<>();
//        Set<String> rIDs = new LinkedHashSet<>();
//        //remove dupe
//        playerRelics.forEach(r -> rIDs.add(r.relicId));
//
//        //create a deep copy to prevent infinite loop
//        for (String rID : rIDs) {
//            relics.add(RelicLibrary.getRelic(rID).makeCopy());
//        }

        playerRelics.forEach((r)->relics.add(r.makeCopy()));
        try {
            if (this.relicSelectScreen == null)
                relicSelectScreen = new RelicSelectScreen(true, this);
        } catch (NoClassDefFoundError e) {
            logger.info("Error: RelicSelectScreen Class not found while opening relic select for bin!");
        }
        if (this.relicSelectScreen != null) relicSelectScreen.open(relics, 1);
    }

    @Override
    public void update() {
        super.update();

        if (!relicSelected && relicSelectScreen != null) {
            if (relicSelectScreen.doneSelecting()) {
                relicSelected = true;
                isSelectionScreenUp = false;
                HashSet<Integer> idxToRemove = relicSelectScreen.getRemovingRelics();
                if (!idxToRemove.isEmpty()) {
                    relicsToRemove.addAll(idxToRemove);
                    this.flash();
                }
                //justReorganized = false;
//                AbstractDungeon.player.reorganizeRelics();

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
        return new TrashBin();
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
}
