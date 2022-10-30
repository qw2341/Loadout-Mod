package loadout.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import loadout.screens.StatModSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;
import static loadout.LoadoutMod.logger;
import static loadout.relics.LoadoutBag.isIsaacMode;

public class TildeKey extends CustomRelic implements ClickableRelic {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("TildeKey");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("compass_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("compass_relic.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);
    private boolean modSelected = true;
    public StatModSelectScreen statSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;

    public static boolean isHealthLocked = false;
    public static int healthLockAmount;
    public static boolean isMaxHealthLocked = false;
    public static int maxHealthLockAmount;
    public static boolean isGoldLocked = false;
    public static int goldLockAmount;

    public static boolean isKillAllMode = false;

    public static boolean isGodMode = false;

    public static boolean isInfiniteEnergy = false;

    public TildeKey() {
        super(ID, IMG, OUTLINE, AbstractRelic.RelicTier.SPECIAL, AbstractRelic.LandingSound.CLINK);




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
            if(statSelectScreen !=null) {
                isSelectionScreenUp = false;
                statSelectScreen.close();
            }
        }
    }

    @Override
    public void onRightClick() {
        if (!isObtained|| AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SETTINGS) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || PowerGiver.isSelectionScreenUp)
            return;

        if(isSelectionScreenUp) {

            if(statSelectScreen !=null) {
                isSelectionScreenUp = false;
                statSelectScreen.close();
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
        modSelected = false;
        isSelectionScreenUp = true;

        try {
            if (this.statSelectScreen == null) statSelectScreen = new StatModSelectScreen(this);
        } catch (NoClassDefFoundError e) {
            logger.info("Error: EventSelectScreen Class not found while opening potion select for cauldron!");
        }

        if (this.statSelectScreen != null) statSelectScreen.open();
    }

    @Override
    public void update()
    {
        super.update();

        if(isHealthLocked) AbstractDungeon.player.currentHealth = healthLockAmount;
        if(isMaxHealthLocked) AbstractDungeon.player.maxHealth = maxHealthLockAmount;
        if(isGoldLocked) AbstractDungeon.player.gold = goldLockAmount;

        if(isInfiniteEnergy && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            if(AbstractDungeon.player.energy.energy<999) AbstractDungeon.player.energy.energy = 999;
        }

//        if(isKillAllMode) {
//            MonsterGroup mg = AbstractDungeon.getMonsters();
//            if(mg != null) {
//                if ((mg.areMonstersBasicallyDead()||mg.areMonstersDead())) {
//
//                } else {
//                    for (AbstractMonster am: mg.monsters) {
//                        AbstractDungeon.actionManager.addToTop(new InstantKillAction(am));
//                    }
//                }
//            }
//        }

        if (!modSelected && statSelectScreen != null) {
            if (statSelectScreen.doneSelecting()) {
                modSelected = true;
                isSelectionScreenUp = false;
            } else {
                statSelectScreen.update();
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
        if (!modSelected && fakeHover) {
            statSelectScreen.render(sb);
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

        if (!modSelected && !fakeHover && statSelectScreen != null) {
            statSelectScreen.render(sb);
        }
    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new TildeKey();
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

    @Override
    public void atBattleStart() {
        if(isKillAllMode) {
            this.flash();
            for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
            }
        }


    }

    @Override
    public void onSpawnMonster(AbstractMonster monster) {

        if(isKillAllMode)  {
            this.flash();
            AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
        }
    }

    @Override
    public void atTurnStart() {
        if(isKillAllMode) {
            this.flash();
            for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
            }
        }
    }

    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        return isGodMode ? 0 : damageAmount;
    }
}
