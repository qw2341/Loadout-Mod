package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.EventSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;
import static loadout.relics.LoadoutBag.isIsaacMode;

public class EventfulCompass extends CustomRelic implements ClickableRelic {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("EventfulCompass");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("compass_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("compass_relic.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);
    private boolean eventSelected = true;
    public EventSelectScreen eventSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;

    public EventfulCompass() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK);




        if(isIsaacMode) {
            try {
                RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID+"Alt");
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
            if(eventSelectScreen!=null) {
                isSelectionScreenUp = false;
                eventSelectScreen.close();
            }
        }
    }

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || TildeKey.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(eventSelectScreen!=null) {
                isSelectionScreenUp = false;
                eventSelectScreen.close();
            }
            return;
        }

        if (AbstractDungeon.isScreenUp) {
            AbstractDungeon.dynamicBanner.hide();
            AbstractDungeon.overlayMenu.cancelButton.hide();
            AbstractDungeon.previousScreen = AbstractDungeon.screen;
        }

        openEventSelect();
    }



    private void openEventSelect()
    {
        eventSelected = false;
        isSelectionScreenUp = true;

        try {
            if (this.eventSelectScreen == null) eventSelectScreen = new EventSelectScreen(this);
        } catch (NoClassDefFoundError e) {
            logger.info("Error: EventSelectScreen Class not found while opening potion select for cauldron!");
        }

        if (this.eventSelectScreen != null) eventSelectScreen.open();
    }

    @Override
    public void update()
    {
        super.update();

        if (!eventSelected && eventSelectScreen != null) {
            if (eventSelectScreen.doneSelecting()) {
                eventSelected = true;
                isSelectionScreenUp = false;
            } else {
                eventSelectScreen.update();
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
        if (!eventSelected && fakeHover) {
            eventSelectScreen.render(sb);
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

        if (!eventSelected && !fakeHover && eventSelectScreen != null) {
            eventSelectScreen.render(sb);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new EventfulCompass();
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
            CardCrawlGame.sound.play("RELIC_DROP_CLINK");
        }
    }


}
