package loadout.relics;

import basemod.BaseMod;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnPlayerDeathRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnReceivePowerRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.DexterityPower;
import com.megacrit.cardcrawl.powers.FocusPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import loadout.LoadoutMod;
import loadout.screens.StatModSelectScreen;
import loadout.util.TextureLoader;

import java.util.HashMap;

import static loadout.LoadoutMod.*;
import static loadout.LoadoutMod.logger;
import static loadout.relics.LoadoutBag.isIsaacMode;

public class TildeKey extends CustomRelic implements ClickableRelic, OnReceivePowerRelic, OnPlayerDeathRelic, CustomSavable<HashMap<String,String>> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("TildeKey");
    //TODO: Make non Isaac version icon
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("tildekey_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("tildekey_relic_alt.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("tildekey_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("tildekey_relic_alt.png"));

    protected static final Sfx landingSfx = new Sfx(makeSoundPath("choir.wav"), false);
    private boolean modSelected = true;
    public StatModSelectScreen statSelectScreen;
    private boolean fakeHover = false;

    public static boolean isSelectionScreenUp = false;

    public static boolean isHealthLocked = false;
    private static final String isHealthLockedKey = "isHealthLocked";
    public static int healthLockAmount = 100;
    private static final String healthLockAmountKey = "healthLockAmount";
    public static boolean isMaxHealthLocked = false;
    private static final String isMaxHealthLockedKey = "isMaxHealthLocked";
    public static int maxHealthLockAmount = 100;
    private static final String maxHealthLockAmountKey = "maxHealthLockAmount";
    public static boolean isGoldLocked = false;
    private static final String isGoldLockedKey = "isGoldLocked";
    public static int goldLockAmount = 100;

    private static final String goldLockAmountKey = "goldLockAmount";
    public static boolean isKillAllMode = false;
    private static final String isKillAllModeKey = "isKillAllMode";

    public static boolean isGodMode = false;
    private static final String isGodModeKey = "isGodMode";

    public static boolean isInfiniteEnergy = false;
    private static final String isInfiniteEnergyKey = "isInfiniteEnergy";

    public static boolean canGoToAnyRooms = false;
    private static final String canGoToAnyRoomsKey = "canGoToAnyRooms";

    public static boolean isAlwaysPlayerTurn = false;
    private static final String isAlwaysPlayerTurnKey = "isAlwaysPlayerTurn";
    public static boolean isDrawCardsTillLimit = false;
    private static final String isDrawCardsTillLimitKey = "isDrawCardsTillLimit";
    public static boolean isNegatingDebuffs = false;
    private static final String isNegatingDebuffsKey = "isNegatingDebuffs";

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
        if (LoadoutBag.isSelectionScreenUp || TrashBin.isSelectionScreenUp || CardPrinter.isSelectionScreenUp || CardShredder.isSelectionScreenUp || CardModifier.isSelectionScreenUp || LoadoutCauldron.isSelectionScreenUp || PowerGiver.isSelectionScreenUp || EventfulCompass.isSelectionScreenUp)
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

        openStatModSelect();
    }



    private void openStatModSelect()
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

        if (AbstractDungeon.isPlayerInDungeon()) {
            if(isHealthLocked) AbstractDungeon.player.currentHealth = healthLockAmount;
            if(isMaxHealthLocked) AbstractDungeon.player.maxHealth = maxHealthLockAmount;
            if(isGoldLocked) {
                AbstractDungeon.player.gold = goldLockAmount;
                AbstractDungeon.player.displayGold = goldLockAmount;
            }

            if(AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                if(isInfiniteEnergy && EnergyPanel.getCurrentEnergy() <999) EnergyPanel.setEnergy(999);
                if(isAlwaysPlayerTurn && !(AbstractDungeon.getCurrRoom()).skipMonsterTurn) (AbstractDungeon.getCurrRoom()).skipMonsterTurn = true;


//                if(isKillAllMode) {
//                    MonsterGroup mg = AbstractDungeon.getMonsters();
//                    if(mg != null) {
//                        if (AbstractDungeon.actionManager.actions.isEmpty() && !(mg.areMonstersDead()||mg.areMonstersBasicallyDead())) {
//                            for (AbstractMonster am: mg.monsters) {
//                                AbstractDungeon.actionManager.addToTop(new InstantKillAction(am));
//                            }
//                        }
//                    }
//                }
            }


        }







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

    public static void resetToDefault() {
        isHealthLocked = false;
        healthLockAmount = 100;
        isMaxHealthLocked = false;
        maxHealthLockAmount = 100;
        isGoldLocked = false;
        goldLockAmount = 100;
        isKillAllMode = false;
        isGodMode = false;
        isInfiniteEnergy = false;
        isAlwaysPlayerTurn = false;
        isDrawCardsTillLimit = false;
        isNegatingDebuffs = false;
    }

    public static void killAllMonsters() {
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
            //monster.isDead = true;
        }
    }

    @Override
    public void atBattleStart() {
        if(isKillAllMode) {
            this.flash();
            killAllMonsters();
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
            killAllMonsters();
        }
    }



    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        return isGodMode ? 0 : damageAmount;
    }

    @Override
    public void onRefreshHand() {
        if ( isDrawCardsTillLimit && AbstractDungeon.actionManager.actions.isEmpty() && !AbstractDungeon.player.hasPower("No Draw") && !AbstractDungeon.isScreenUp && !AbstractDungeon.actionManager.turnHasEnded) {
            if ((AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.player.hand.size() < BaseMod.MAX_HAND_SIZE && (
                AbstractDungeon.player.discardPile.size() > 0 || AbstractDungeon.player.drawPile.size() > 0)) {
                flash();
                addToTop((AbstractGameAction)new RelicAboveCreatureAction((AbstractCreature)AbstractDungeon.player, this));
                addToBot((AbstractGameAction)new DrawCardAction((AbstractCreature)AbstractDungeon.player, BaseMod.MAX_HAND_SIZE - AbstractDungeon.player.hand.size()));
            }
        }
    }



    @Override
    public HashMap<String, String> onSave() {
        HashMap<String, String> sav = new HashMap<>();
        sav.put(isHealthLockedKey, String.valueOf(isHealthLocked));
        sav.put(healthLockAmountKey, String.valueOf(healthLockAmount));
        sav.put(isMaxHealthLockedKey, String.valueOf(isMaxHealthLocked));
        sav.put(maxHealthLockAmountKey, String.valueOf(maxHealthLockAmount));
        sav.put(isGoldLockedKey, String.valueOf(isGoldLocked));
        sav.put(goldLockAmountKey, String.valueOf(goldLockAmount));
        sav.put(isKillAllModeKey, String.valueOf(isKillAllMode));
        sav.put(isGodModeKey, String.valueOf(isGodMode));
        sav.put(isInfiniteEnergyKey, String.valueOf(isInfiniteEnergy));
        sav.put(canGoToAnyRoomsKey, String.valueOf(canGoToAnyRooms));
        sav.put(isAlwaysPlayerTurnKey, String.valueOf(isAlwaysPlayerTurn));
        sav.put(isDrawCardsTillLimitKey, String.valueOf(isDrawCardsTillLimit));
        sav.put(isNegatingDebuffsKey, String.valueOf(isNegatingDebuffs));
        return sav;
    }

    @Override
    public void onLoad(HashMap<String, String> sav) {
        if(sav == null) {
            resetToDefault();
            return;
        }

        try {
            isHealthLocked = Boolean.parseBoolean(sav.get(isHealthLockedKey));
            healthLockAmount = Integer.parseInt(sav.get(healthLockAmountKey));
            isMaxHealthLocked = Boolean.parseBoolean(sav.get(isMaxHealthLockedKey));
            maxHealthLockAmount = Integer.parseInt(sav.get(maxHealthLockAmountKey));
            isGoldLocked = Boolean.parseBoolean(sav.get(isGoldLockedKey));
            goldLockAmount = Integer.parseInt(sav.get(goldLockAmountKey));
            isKillAllMode = Boolean.parseBoolean(sav.get(isKillAllModeKey));
            isGodMode = Boolean.parseBoolean(sav.get(isGodModeKey));
            isInfiniteEnergy = Boolean.parseBoolean(sav.get(isInfiniteEnergyKey));
            canGoToAnyRooms = Boolean.parseBoolean(sav.get(canGoToAnyRoomsKey));
            isAlwaysPlayerTurn = Boolean.parseBoolean(sav.get(isAlwaysPlayerTurnKey));
            isDrawCardsTillLimit = Boolean.parseBoolean(sav.get(isDrawCardsTillLimitKey));
            isNegatingDebuffs = Boolean.parseBoolean(sav.get(isNegatingDebuffsKey));
        } catch (Exception e) {
            logger.info("Loading save for TildeKey failed, reverting to default");
            resetToDefault();
        }

    }

    /**
     * Used RockBottom code from IsaacModExtend
     * @param power
     * @param abstractCreature
     * @return
     */
    @Override
    public boolean onReceivePower(AbstractPower power, AbstractCreature abstractCreature) {
        if (isNegatingDebuffs) {
            if (power.type == AbstractPower.PowerType.DEBUFF) {
                this.flash();
                return false;
            } else {
                if (power.amount < 0 && power.canGoNegative) this.flash();
                return power.amount >= 0 || !power.canGoNegative;
            }
        }
        return true;
    }

    /**
    * Used RockBottom code from IsaacModExtend
    */
    @Override
    public int onReceivePowerStacks(AbstractPower power, AbstractCreature source, int stackAmount) {
        if (isNegatingDebuffs && (power instanceof StrengthPower || power instanceof DexterityPower || power instanceof FocusPower)) {
            if (stackAmount < 0) this.flash();
            return Math.max(stackAmount, 0);
        }
        return stackAmount;
    }

    @Override
    public boolean onPlayerDeath(AbstractPlayer abstractPlayer, DamageInfo damageInfo) {
        if(isGodMode) {
            AbstractDungeon.player.currentHealth = AbstractDungeon.player.maxHealth;
            return false;
        }

        return true;
    }
}
