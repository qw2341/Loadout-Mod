package loadout.relics;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import code.ui.TransmutationTable;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.*;
import com.evacipated.cardcrawl.mod.stslib.relics.OnPlayerDeathRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnReceivePowerRelic;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.TalkAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.SFXAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.input.InputAction;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.GiantHead;
import com.megacrit.cardcrawl.monsters.ending.SpireShield;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;
import com.megacrit.cardcrawl.monsters.exordium.Lagavulin;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;
import loadout.LoadoutMod;
import loadout.screens.*;
import loadout.util.Wiz;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static loadout.LoadoutMod.logger;

public class TildeKey extends AbstractCustomScreenRelic<StatModSelectScreen.StatModButton> implements OnReceivePowerRelic, OnPlayerDeathRelic, CustomSavable<HashMap<String,String>> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("TildeKey");
    private static Texture IMG = null;
    private static Texture OUTLINE = null;

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
    public static AbstractMonster.Intent setIntent = null;
    public static final String setIntentKey = "setIntent";

    public static int rewardMultiplier = 1;
    public static final String rewardMultiplierKey = "rewardMultiplier";

    public static boolean isRewardDuped = false;
    private static final String isRewardDupedKey = "isRewardDuped";

    public static boolean enableRelicCounterEdit = false;
    private static final String enableRelicCounterEditKey = "enableRelicCounterEdit";

    public static boolean infiniteCampfireActions = false;
    public static final String infiniteCampfireActionsKey = "infiniteCampfireActions";

    public static boolean isOrbLocked = false;
    private static final String isOrbLockedKey = "isOrbLocked";
    public static int orbLockAmount = 0;

    private static final String orbLockAmountKey = "orbLockAmount";

    public static boolean isEnergyLocked = false;
    private static final String isEnergyLockedKey = "isEnergyLocked";
    public static int energyLockAmount = 3;
    private static final String energyLockAmountKey = "energyLockAmount";
    public static boolean isMaxEnergyLocked = false;
    private static final String isMaxEnergyLockedKey = "isMaxEnergyLocked";
    public static int maxEnergyLockAmount = 3;
    private static final String maxEnergyLockAmountKey = "maxEnergyLockAmount";

    public static int enemyAttackMult = 100;
    private static final String enemyAttackMultKey = "enemyAttackMult";
    public static int maxHandSize = 10;
    private static final String maxHandSizeKey = "maxHandSize";
    public static int drawPerTurn = 5;
    private static final String drawPerTurnKey = "drawPerTurn";
    public static boolean isDrawPerTurnLocked = false;
    private static final String isDrawPerTurnLockedKey = "isDrawPerTurnLocked";

    public static boolean isEMCLocked = false;
    private static final String isEMCLockedKey = "isEMCLocked";
    public static int EMCLockAmount = 3;
    private static final String EMCLockAmountKey = "EMCLockAmount";

    private final InputAction gKey;
    private final InputAction kKey;


    public static int playerAttackMult = 100;
    private static final String playerAttackMultKey = "playerAttackMult";

    public static final String playerMorphKey = "currentMorph";

    public static String currentMorph = "";

    public AbstractSelectScreen<CharacterSkinSelectScreen.CharacterButton> morphMenu;

    public MonsterEditScreen monsterEditMenu;

    public static AbstractCreature morphee;

    public static AbstractCreature target;

    public static Skeleton skeletonBackup;
    public static TextureAtlas atlasBackup;
    public static AnimationState stateBackup;
    public static AnimationStateData stateDataBackup;
    public static float hbWBackup = 100f;
    public static float hbHBackup = 100f;
    public boolean justClickedMiddle = false;

    public static HashSet<String> NO_FLIP_LIST = new HashSet<>();

    public static int merchantPriceMult = 100;

    public static final String merchantPriceMultKey = "merchantPriceMult";

    public static int potionPotencyMult = 100;
    public static final String potionPotencyMultKey = "potionPotencyMult";

    public TildeKey() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.CLINK);
        this.gKey = new InputAction(Input.Keys.G);
        this.kKey = new InputAction(Input.Keys.K);

    }

    @Override
    public void onCtrlRightClick() {
        killAllMonsters();
    }
    @Override
    public void onShiftRightClick() {
        spareAllMonsters();
    }

    @Override
    protected AbstractSelectScreen<StatModSelectScreen.StatModButton> getNewSelectScreen() {
        return new StatModSelectScreen(this);
    }

    public void openMorphMenu() {
//        AllInOneBag.INSTANCE.closeAllScreens();
        if(morphMenu == null) morphMenu = new CharacterSkinSelectScreen(this);
        if(!morphMenu.isOpen()) morphMenu.open();
    }

    public void openMonsterEditMenu() {
        if(monsterEditMenu == null) monsterEditMenu = new MonsterEditScreen(this);
        if(!monsterEditMenu.isOpen()) monsterEditMenu.open();
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public void renderInTopPanel(SpriteBatch sb) {
        super.renderInTopPanel(sb);
        if (morphMenu != null && morphMenu.isOpen()) {
            morphMenu.render(sb);
        }
        if(monsterEditMenu != null && monsterEditMenu.isOpen()) {
            monsterEditMenu.render(sb);
        }
    }

    @Override
    public void update()
    {
        super.update();

        if(morphMenu != null && morphMenu.isOpen()) {
            morphMenu.update();
        }

        if(monsterEditMenu != null && monsterEditMenu.isOpen()) {
            monsterEditMenu.update();
        }



        if (CardCrawlGame.isInARun() && AbstractDungeon.isPlayerInDungeon() && Wiz.adp() != null && this.isObtained) {


            if(isHealthLocked) AbstractDungeon.player.currentHealth = healthLockAmount;
            if(isMaxHealthLocked) AbstractDungeon.player.maxHealth = maxHealthLockAmount;
            if(isGoldLocked) {
                AbstractDungeon.player.gold = goldLockAmount;
                AbstractDungeon.player.displayGold = goldLockAmount;
            }
            try {
                if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    if(isInfiniteEnergy && EnergyPanel.getCurrentEnergy() <999) EnergyPanel.setEnergy(999);
                    if(!isInfiniteEnergy) {
                        if(isEnergyLocked) EnergyPanel.totalCount = energyLockAmount;
                        if(isMaxEnergyLocked) {
                            AbstractDungeon.player.energy.energyMaster = maxEnergyLockAmount;
                            AbstractDungeon.player.energy.energy = maxEnergyLockAmount;
                        }
                    }
                    if(isAlwaysPlayerTurn && !(AbstractDungeon.getCurrRoom()).skipMonsterTurn) (AbstractDungeon.getCurrRoom()).skipMonsterTurn = true;
                }
            } catch (NullPointerException npe) {

            }


            if(Loader.isModLoaded("projecte")) {
                if(isEMCLocked) {
                    TransmutationTable.PLAYER_EMC = EMCLockAmount;
                }
            }

            if(enableRelicCounterEdit && AbstractDungeon.player != null && AbstractDungeon.player.relics != null) {
                Iterator<AbstractRelic> it = AbstractDungeon.player.relics.iterator();
                AbstractRelic r = null;
                while(it.hasNext()) {
                    r = it.next();
                    if (r.hb.hovered) {
                        break;
                    }
                }
                if(r != null) {
                    if(InputHelper.scrolledUp) r.counter++;
                    if(InputHelper.scrolledDown) r.counter--;
                    if(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
                        if(!justClickedMiddle){
                            if(r.counter != Integer.MAX_VALUE) r.counter = Integer.MAX_VALUE;
                            else r.counter = 0;
                            justClickedMiddle = true;
                        }
                    } else {
                        justClickedMiddle = false;
                    }
                }
            }

            if(LoadoutMod.isXggg()) {
                if(gKey.isJustPressed()) {
                    int roll = MathUtils.random(2);
                    if (roll == 0) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_MERCHANT_MA"));
                    } else if (roll == 1) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_MERCHANT_MB"));
                    } else {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_MERCHANT_MC"));
                    }
                    addToBot(new TalkAction(true, "~晚~ ~上~ ~好~~", 1.0F, 2.0F));
                }
                if(kKey.isJustPressed()) {
                    int roll = MathUtils.random(2);
                    if (roll == 0) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_CULTIST_1A"));
                    } else if (roll == 1) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_CULTIST_1B"));
                    } else {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new SFXAction("VO_CULTIST_1C"));
                    }
                    addToBot((AbstractGameAction)new TalkAction(true, RelicLibrary.getRelic("CultistMask").DESCRIPTIONS[1], 1.0F, 2.0F));
                }
            }
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
        canGoToAnyRooms = false;
        isAlwaysPlayerTurn = false;
        isDrawCardsTillLimit = false;
        isNegatingDebuffs = false;
        setIntent = null;
        rewardMultiplier = 1;
        isRewardDuped = false;
        enableRelicCounterEdit = false;
        infiniteCampfireActions = false;
        orbLockAmount = 1;
        isOrbLocked = false;
        isEnergyLocked = false;
        energyLockAmount = 3;
        isMaxEnergyLocked = false;
        maxEnergyLockAmount = 3;
        enemyAttackMult = 100;
        maxHandSize = 10;
        BaseMod.MAX_HAND_SIZE = BaseMod.DEFAULT_MAX_HAND_SIZE;

        isDrawPerTurnLocked = false;
        drawPerTurn = 5;

        isEMCLocked = false;
        EMCLockAmount = 0;

        playerAttackMult = 100;
        currentMorph = "";

        merchantPriceMult = 100;
        potionPotencyMult = 100;
    }

    public static void spareAllMonsters() {
        if(AbstractDungeon.getMonsters()== null) return;
        AbstractDungeon.actionManager.addToTop(new AbstractGameAction() {
            @Override
            public void update() {
                if(!this.isDone) {
                    this.isDone = true;
                    AbstractDungeon.getCurrRoom().endBattle();
                }
            }
        });
    }

    public static void killAllMonsters() {
        if(AbstractDungeon.getMonsters()== null) return;
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (monster != null) {
                AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
                if(!isKillAllMode && !Settings.FAST_MODE) {
                    AbstractDungeon.actionManager.addToTop((AbstractGameAction)new WaitAction(0.8F));
                    AbstractDungeon.actionManager.addToTop((AbstractGameAction)new VFXAction((AbstractGameEffect)new WeightyImpactEffect(monster.hb.cX, monster.hb.cY)));
                }
            }
        }
    }

    public static void setMonsterIntent(AbstractMonster am, AbstractMonster.Intent intent) {
        am.setMove((byte) 1, intent);
    }

    public static void setMonsterDamage(AbstractMonster am, int damagePercent) {
        if (am == null) return;

        float mult = (damagePercent / 100.0f);
        for(DamageInfo di : am.damage) {
            di.output = di.base;
            di.output *= mult;
        }
        try {
            am.createIntent();
        } catch (NullPointerException ignored) {

        }

    }


    public void atBattleStart() {
        if(isKillAllMode) {
            this.flash();
            killAllMonsters();
        }

        if(setIntent != null) {
            this.flash();
            for (AbstractMonster am: AbstractDungeon.getCurrRoom().monsters.monsters) {
                setMonsterIntent(am,setIntent);
            }
        }

        if(enemyAttackMult != 100) {
            for (AbstractMonster am: AbstractDungeon.getCurrRoom().monsters.monsters) setMonsterDamage(am, enemyAttackMult);
        }
    }

    public void onSpawnMonster(AbstractMonster monster) {

        if(isKillAllMode)  {
            this.flash();
            AbstractDungeon.actionManager.addToTop(new InstantKillAction(monster));
        }

        if(setIntent != null) {
            this.flash();
            setMonsterIntent(monster,setIntent);
        }

        if(enemyAttackMult != 100) {
            setMonsterDamage(monster, enemyAttackMult);
        }
    }

    public void atTurnStart() {
        if(isKillAllMode) {
            this.flash();
            killAllMonsters();
        }
        if(setIntent != null) {
            this.flash();
            for (AbstractMonster am: AbstractDungeon.getCurrRoom().monsters.monsters) {
                setMonsterIntent(am,setIntent);
            }
        }

    }

    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        return isGodMode ? 0 : damageAmount;
    }

    public void onRefreshHand() {
        if ( isDrawCardsTillLimit && AbstractDungeon.actionManager.actions.isEmpty() && !AbstractDungeon.player.hasPower("No Draw") && !AbstractDungeon.isScreenUp && !AbstractDungeon.actionManager.turnHasEnded) {
            if ((AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.player.hand.size() < BaseMod.MAX_HAND_SIZE && (
                AbstractDungeon.player.discardPile.size() > 0 || AbstractDungeon.player.drawPile.size() > 0)) {
                flash();
                addToBot((AbstractGameAction)new DrawCardAction((AbstractCreature)AbstractDungeon.player, BaseMod.MAX_HAND_SIZE - AbstractDungeon.player.hand.size()));
            }
        }
        if(isOrbLocked && AbstractDungeon.actionManager.actions.isEmpty() && !AbstractDungeon.isScreenUp ) {
            if ((AbstractDungeon.getCurrRoom()).phase == AbstractRoom.RoomPhase.COMBAT && AbstractDungeon.player.maxOrbs != orbLockAmount) {
                flash();
                modifyPlayerOrbs(orbLockAmount - AbstractDungeon.player.maxOrbs);
            }
        }

    }

    public static void modifyPlayerOrbs(int diff) {
        AbstractDungeon.player.maxOrbs += diff;
        if(diff >0) {

            int i;
            for(i = 0; i < diff; ++i) {
                AbstractDungeon.player.orbs.add(new EmptyOrbSlot());
            }

            for(i = 0; i < AbstractDungeon.player.orbs.size(); ++i) {
                ((AbstractOrb)AbstractDungeon.player.orbs.get(i)).setSlot(i, AbstractDungeon.player.maxOrbs);
            }
        } else if (diff < 0) {
            if (AbstractDungeon.player.maxOrbs < 0) {
                AbstractDungeon.player.maxOrbs = 0;
            }

            if (!AbstractDungeon.player.orbs.isEmpty()) {
                AbstractDungeon.player.orbs = new ArrayList<>(AbstractDungeon.player.orbs.subList(0, AbstractDungeon.player.maxOrbs));
            }

            for(int i = 0; i < AbstractDungeon.player.orbs.size(); ++i) {
                ((AbstractOrb)AbstractDungeon.player.orbs.get(i)).setSlot(i, AbstractDungeon.player.maxOrbs);
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
        if(setIntent != null) sav.put(setIntentKey, String.valueOf(setIntent));
        else sav.put(setIntentKey,"ALL");
        sav.put(rewardMultiplierKey, String.valueOf(rewardMultiplier));
        sav.put(isRewardDupedKey, String.valueOf(isRewardDuped));
        sav.put(enableRelicCounterEditKey, String.valueOf(enableRelicCounterEdit));
        sav.put(infiniteCampfireActionsKey, String.valueOf(infiniteCampfireActions));
        sav.put(orbLockAmountKey, String.valueOf(orbLockAmount));
        sav.put(isOrbLockedKey, String.valueOf(isOrbLocked));
        sav.put(isEnergyLockedKey, String.valueOf(isEnergyLocked));
        sav.put(energyLockAmountKey, String.valueOf(energyLockAmount));
        sav.put(isMaxEnergyLockedKey, String.valueOf(isMaxEnergyLocked));
        sav.put(maxEnergyLockAmountKey, String.valueOf(maxEnergyLockAmount));
        sav.put(enemyAttackMultKey, String.valueOf(enemyAttackMult));
        sav.put(maxHandSizeKey, String.valueOf(maxHandSize));
        sav.put(isDrawPerTurnLockedKey, String.valueOf(isDrawPerTurnLocked));
        sav.put(isEMCLockedKey, String.valueOf(isEMCLocked));
        sav.put(EMCLockAmountKey, String.valueOf(EMCLockAmount));

        drawPerTurn = AbstractDungeon.player.masterHandSize;
        sav.put(drawPerTurnKey, String.valueOf(drawPerTurn));

        sav.put(playerAttackMultKey, String.valueOf(playerAttackMult));
        sav.put(playerMorphKey, currentMorph);

        sav.put(merchantPriceMultKey, String.valueOf(merchantPriceMult));
        sav.put(potionPotencyMultKey, String.valueOf(potionPotencyMult));
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

            String intent = sav.get(setIntentKey);
            if (intent.equals("ALL")) setIntent = null;
            else setIntent = AbstractMonster.Intent.valueOf(intent);

            rewardMultiplier = Integer.parseInt(sav.get(rewardMultiplierKey));
            isRewardDuped = Boolean.parseBoolean(sav.get(isRewardDupedKey));
            enableRelicCounterEdit = Boolean.parseBoolean(sav.get(enableRelicCounterEditKey));
            infiniteCampfireActions = Boolean.parseBoolean(sav.get(infiniteCampfireActionsKey));
            orbLockAmount = Integer.parseInt(sav.get(orbLockAmountKey));
            isOrbLocked = Boolean.parseBoolean(sav.get(isOrbLockedKey));
            isEnergyLocked = Boolean.parseBoolean(sav.get(isEnergyLockedKey));
            energyLockAmount = Integer.parseInt(sav.get(energyLockAmountKey));
            isMaxEnergyLocked = Boolean.parseBoolean(sav.get(isMaxEnergyLockedKey));
            maxEnergyLockAmount = Integer.parseInt(sav.get(maxEnergyLockAmountKey));
            enemyAttackMult = Integer.parseInt(sav.get(enemyAttackMultKey));
            maxHandSize = Integer.parseInt(sav.get(maxHandSizeKey));
            BaseMod.MAX_HAND_SIZE = maxHandSize;

            isEMCLocked = Boolean.parseBoolean(sav.getOrDefault(isEMCLockedKey, "false"));
            EMCLockAmount = Integer.parseInt(sav.getOrDefault(EMCLockAmountKey, "0"));

            isDrawPerTurnLocked = Boolean.parseBoolean(sav.get(isDrawPerTurnLockedKey));
            drawPerTurn = Integer.parseInt(sav.get(drawPerTurnKey));

            int diff = AbstractDungeon.player.gameHandSize - AbstractDungeon.player.masterHandSize;
            AbstractDungeon.player.masterHandSize = drawPerTurn;
            AbstractDungeon.player.gameHandSize = drawPerTurn + diff;

            playerAttackMult = Integer.parseInt(sav.get(playerAttackMultKey));
            currentMorph = sav.get(playerMorphKey);

            merchantPriceMult = Integer.parseInt(sav.get(merchantPriceMultKey));
            if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.shopScreen != null) {
                AbstractDungeon.shopScreen.applyDiscount(TildeKey.merchantPriceMult / 100f, true);
            }
            potionPotencyMult = Integer.parseInt(sav.get(potionPotencyMultKey));
            if(AbstractDungeon.isPlayerInDungeon()) {
                for (AbstractPotion p : AbstractDungeon.player.potions) {
                    p.initializeData();
                }
            }
        } catch (Exception e) {
            logger.info("Loading save for TildeKey failed, reverting to default");
            e.printStackTrace();
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

    public void battleStartPreDraw() {

    }

    public float atDamageModify(float damage, AbstractCard c) {
        return damage * (playerAttackMult / 100.0f);
    }

    public static void morph(AbstractCreature morphee, AbstractCreature morphTarget) {

        if(morphee == null || morphTarget == null) return;

        if(morphee instanceof AbstractPlayer && morphTarget.getClass().getName().equals(morphee.getClass().getName())) {
            resetPlayerMorph();
            return;
        }

        logger.info("Morphing " + morphee.name + " to " + morphTarget.name);

        if(morphee instanceof AbstractPlayer && (currentMorph == null || currentMorph.equals(""))) {
            //if first time
            skeletonBackup = ReflectionHacks.getPrivate(morphee, AbstractCreature.class, "skeleton");
            atlasBackup = ReflectionHacks.getPrivate(morphee, AbstractCreature.class, "atlas");
            stateBackup = morphee.state;
            stateDataBackup = ReflectionHacks.getPrivate(morphee, AbstractCreature.class, "stateData");
            hbWBackup = morphee.hb_w;
            hbHBackup = morphee.hb_h;
        }

        ReflectionHacks.setPrivate(morphee, AbstractCreature.class, "skeleton", ReflectionHacks.getPrivate(morphTarget, AbstractCreature.class, "skeleton"));
        ReflectionHacks.setPrivate(morphee, AbstractCreature.class, "atlas", ReflectionHacks.getPrivate(morphTarget, AbstractCreature.class, "atlas"));
        ReflectionHacks.setPrivate(morphee, AbstractCreature.class, "stateData", ReflectionHacks.getPrivate(morphTarget, AbstractCreature.class, "stateData"));
        morphee.state = morphTarget.state;
        if(! (morphee instanceof  AbstractPlayer)) morphee.name = morphTarget.name;
        morphee.hb.resize(morphTarget.hb.width,morphTarget.hb.height);
        morphee.hb.move(morphee.drawX, morphee.drawY);

        AnimationStateData stateData = ((AnimationStateData)ReflectionHacks.getPrivate(morphee,AbstractCreature.class,"stateData"));
        if (stateData != null) {
            SkeletonData sd = stateData.getSkeletonData();
            if(sd != null) {
                Array<Animation> anim = sd.getAnimations();
                Animation hit = null;
                Animation idle = null;
                if(anim != null && anim.size > 0) {
                    //Exceptions
                    if (morphTarget instanceof GiantHead) {
                        AnimationState.TrackEntry e = morphee.state.setAnimation(0, "idle_open", true);
                        e.setTime(e.getEndTime() * MathUtils.random());
                        e.setTimeScale(0.5F);
                    } else {
                        for (Animation an: anim) {
                            if (idle == null && (an.getName().equalsIgnoreCase("idle") || an.getName().contains("idle") || an.getName().contains("Idle"))) {
                                idle = an;
                            }
                            if (hit == null && (an.getName().equalsIgnoreCase("hit") || an.getName().contains("hit") || an.getName().contains("Hit"))) {
                                hit = an;
                            }
                        }

                        if (idle == null) idle = anim.get(0);
                        if (hit == null) hit = anim.size > 1 ? anim.get(1) : anim.get(0);
                        try {
                            AnimationState.TrackEntry e = morphee.state.setAnimation(0, idle, true);
                            stateData.setMix(hit, idle, 0.1F);
                            e.setTimeScale(0.6F);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }

            }
        }


        if(morphee instanceof AbstractPlayer) {
            //if player
            currentMorph = morphTarget.getClass().getName();
//            if(morphTarget instanceof AbstractMonster)
//                morphee.flipHorizontal = !NO_FLIP_LIST.contains(morphTarget.getClass().getName());
//            else if (morphTarget instanceof AbstractPlayer)
//                morphee.flipHorizontal = false;
        }
        else {
            if(morphTarget instanceof AbstractMonster) {
                morphee.flipHorizontal = morphee.drawX < AbstractDungeon.player.drawX;
                if (NO_FLIP_LIST.contains(morphTarget.getClass().getName())) morphee.flipHorizontal = !morphee.flipHorizontal;
            } else if (morphTarget instanceof AbstractPlayer) {
                morphee.flipHorizontal = !NO_FLIP_LIST.contains(morphee.getClass().getName());
            }

        }

    }

    public static boolean shouldFlip() {
        Class<?> clazz;
        if(currentMorph == null || currentMorph.isEmpty() || NO_FLIP_LIST.contains(currentMorph)) return false;
        try {
            clazz = Class.forName(currentMorph);
            return AbstractMonster.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    public static AbstractCreature getMorphTarget(String id) throws ClassNotFoundException {
//        if(baseGameMonsterMap.containsKey(id)) {
//            return MonsterSelectScreen.MonsterButton.createMonster(baseGameMonsterMap.get(id));
//        } else if (monsterMap.containsKey(id)) {
//            return MonsterSelectScreen.MonsterButton.createMonster(monsterMap.get(id));
//        }
        final AbstractCreature[] target = {null};
        Class<?> clazz;

        try {
            clazz = Class.forName(id);
            if (AbstractPlayer.class.isAssignableFrom(clazz)){
                Constructor con = clazz.getDeclaredConstructor(String.class);
                con.setAccessible(true);
                target[0] = (AbstractPlayer) con.newInstance(AbstractDungeon.name);
            }
            else if (AbstractMonster.class.isAssignableFrom(clazz))
                target[0] = MonsterSelectScreen.MonsterButton.createMonster((Class<? extends AbstractMonster>) clazz);
            else
                target[0] = (AbstractCreature) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();

            logger.info("Failed to obtain morph target, trying a different approach");
            if(AllInOneBag.INSTANCE.bottledMonster.selectScreen != null) {
                AllInOneBag.INSTANCE.bottledMonster.selectScreen.getList().forEach(mb -> {
                    if(mb.mClass.getName().equals(id)) {
                        target[0] = mb.instance;
                    }
                });
            }

        }

        if(target[0] == null) throw new ClassNotFoundException();

        return target[0];
    }

    public static boolean isNotFightingSurrounded() {
        return AbstractDungeon.getCurrMapNode() != null && AbstractDungeon.getCurrRoom() instanceof MonsterRoom && !AbstractDungeon.lastCombatMetricKey.equals(MonsterHelper.SHIELD_SPEAR_ENC);
    }

    public static void flipPlayer() {
//        if (currentMorph != null && !currentMorph.equals("") ) {
//            if(shouldFlip()) {
//                if(!NO_FLIP_LIST.contains(currentMorph))
//                    AbstractDungeon.player.flipHorizontal = isNotFightingSurrounded();
//            }
//            if(AbstractDungeon.getCurrMapNode() != null && AbstractDungeon.getCurrRoom() instanceof MonsterRoom && AbstractDungeon.lastCombatMetricKey.equals(MonsterHelper.SHIELD_SPEAR_ENC)){
//                if(AbstractDungeon.getMonsters().monsters.get(1).hasPower(BackAttackPower.POWER_ID)) AbstractDungeon.player.flipHorizontal = !AbstractDungeon.player.flipHorizontal;
//            }

            //logger.info("Flipping result is " + AbstractDungeon.player.flipHorizontal);
//        }
    }

    public static void morphAndFlip() {
        if(currentMorph != null && !currentMorph.equals("")) {
            try {
                AbstractCreature creature = getMorphTarget(currentMorph);
                morph(AbstractDungeon.player,creature);
            } catch (Exception e) {
                logger.info("Failed to morph and flip, here's why: ");
                e.printStackTrace();
                restorePlayerMorph();
            }

//            flipPlayer();
//            Skeleton pSk = ReflectionHacks.getPrivate(AbstractDungeon.player, AbstractCreature.class, "skeleton");
//            pSk.setFlipX(AbstractDungeon.player.flipHorizontal);
        }
    }

    public static void resetPlayerMorph() {
        logger.info("Resetting player morph");
        restorePlayerMorph();
        currentMorph = "";
    }

    public static void restorePlayerMorph() {
        if(currentMorph == null || currentMorph.equals("")) return;

        if(skeletonBackup != null) ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractCreature.class, "skeleton",skeletonBackup);
        if(atlasBackup != null) ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractCreature.class, "atlas",atlasBackup);
        if(stateBackup != null) AbstractDungeon.player.state = stateBackup;
        if(stateDataBackup != null)ReflectionHacks.setPrivate(AbstractDungeon.player, AbstractCreature.class, "stateData", stateDataBackup);

        AbstractDungeon.player.hb.resize(hbWBackup, hbHBackup);
        AbstractDungeon.player.hb.move(AbstractDungeon.player.drawX, AbstractDungeon.player.drawY);
        AbstractDungeon.player.flipHorizontal = false;
    }

    public static boolean isPlayerMorphed() {
        return !(currentMorph == null || currentMorph.equals(""));
    }

    static {
        NO_FLIP_LIST.add(SpireShield.class.getName());
    }
}
