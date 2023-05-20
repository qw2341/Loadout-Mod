package loadout.screens;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.AscendersBane;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.helpers.input.ScrollInputProcessor;
import com.megacrit.cardcrawl.helpers.steamInput.SteamInputHelper;
import com.megacrit.cardcrawl.localization.OrbStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.orbs.EmptyOrbSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import com.megacrit.cardcrawl.vfx.AscensionLevelUpTextEffect;
import loadout.LoadoutMod;
import loadout.helper.TextInputHelper;
import loadout.helper.TextInputReceiver;
import loadout.relics.AbstractCustomScreenRelic;
import loadout.relics.TildeKey;

import java.util.ArrayList;


public class StatModSelectScreen extends AbstractSelectScreen<StatModSelectScreen.StatModButton> {

    public interface StatModActions {
        int getAmount();

        void setAmount(int amountToSet);

        void onBoolChange(boolean boolToChange, int amount);
    }

    public static class StatModButton implements HeaderButtonPlusListener, TextInputReceiver {
        public static final float HITBOX_HEIGHT = 75.0f * Settings.yScale;

        public Hitbox hb;

        public float x;
        public float y;

        public String text;
        public float textWidth;

        public HeaderButtonPlus lockButton;

        public String amount;

        public boolean isLocked;

        private boolean isTyping;
        private InputProcessor prevInputProcessor;
        private float waitTimer = 0.0F;

        private StatModActions actions;

        private Texture icon;
        private float iconOffset;
        private Color textColor;
        public StatModButton(String text, boolean isLocked, Texture icon, float iconOffset, Color textColor, StatModActions actions) {
            //this.modType = mt;

            this.x = 0;
            this.y = 0;

            this.text = text;
            this.isLocked = isLocked;

            this.text += ": ";
            this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text, Float.MAX_VALUE, 0.0F);

            this.hb = new Hitbox(x + this.textWidth/2 ,y,this.textWidth + iconOffset,HITBOX_HEIGHT);
            this.amount = "0";

            this.icon = icon;
            this.iconOffset = iconOffset;
            this.textColor = textColor;
            this.actions = actions;

            this.lockButton = new HeaderButtonPlus(StatModSelectScreen.TEXT[1],this.x + HP_NUM_OFFSET_X + this.textWidth + 100.0f,this.y, this, false, true, HeaderButtonPlus.Alignment.LEFT);
            this.lockButton.isAscending = this.isLocked;

            this.isTyping = false;
        }

        public Hitbox updateControllerInput() {

                if (this.lockButton.hb.hovered) {
                    return this.lockButton.hb;
                }


            return null;
        }

        public void update() {
            if (this.isTyping && Gdx.input.isKeyPressed(67) && !amount.equals("") && this.waitTimer <= 0.0F) {

                amount = amount.substring(0, amount.length() - 1);
                this.waitTimer = 0.09F;
            }

            if (this.waitTimer > 0.0F) {
                this.waitTimer -= Gdx.graphics.getDeltaTime();
            }
            
            this.hb.move(this.x + this.textWidth/2 + HP_NUM_OFFSET_X, this.y);
            this.hb.update();

            this.lockButton.x = this.x + HP_NUM_OFFSET_X +  350.0f;
            this.lockButton.y = this.y;
            this.lockButton.update();

            if (this.hb.justHovered) {
                CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
            }

            if (this.hb.hovered && InputHelper.justClickedLeft) {
                this.hb.clickStarted = true;
            }
            if(!this.hb.hovered && (InputHelper.justClickedLeft || InputHelper.justClickedRight) && this.isTyping) {
                //cancel typing
                stopTyping();
                setAmountPlayer();
            }
            if (this.hb.clicked || this.hb.hovered && CInputActionSet.select.isJustPressed()) {
                CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
                this.hb.clicked = false;

                if(!isTyping) {
                    this.isTyping = true;

                    prevInputProcessor = Gdx.input.getInputProcessor();

                    Gdx.input.setInputProcessor(new TextInputHelper(this, true));
                    if (SteamInputHelper.numControllers == 1 && CardCrawlGame.clientUtils != null && CardCrawlGame.clientUtils.isSteamRunningOnSteamDeck()) {
                        CardCrawlGame.clientUtils.showFloatingGamepadTextInput(SteamUtils.FloatingGamepadTextInputMode.ModeSingleLine, 0, 0, Settings.WIDTH, (int)(Settings.HEIGHT * 0.25F));
                    }
                }





            }

            if(this.isTyping) {
                if (Gdx.input.isKeyJustPressed(66)) {
                    stopTyping();
                    setAmountPlayer();
                }
                else if (InputHelper.pressedEscape) {
                    InputHelper.pressedEscape = false;
                    stopTyping();
                }
            }

            if(!this.isTyping) {
                this.amount = String.valueOf(this.actions.getAmount());
            }

        }

        public void stopTyping() {
            this.isTyping = false;
            Gdx.input.setInputProcessor((InputProcessor)new ScrollInputProcessor());
        }

        private void setAmountPlayer() {

            int amtToSet;
            if (this.amount.equals("")) {
                amtToSet = 0;
            } else {
                try {
                    amtToSet = Integer.parseInt(this.amount);
                } catch (Exception e) {
                    amtToSet = Integer.MAX_VALUE;
                }
            }


            this.actions.setAmount(amtToSet);
            this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text + amount, Float.MAX_VALUE, 0.0F);

        }

        public void render(SpriteBatch sb) {
            sb.setColor(Color.WHITE);

            if (this.hb.hovered) {
                sb.draw(this.icon, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, icon.getWidth(), icon.getHeight(), false, false);
            } else {
                sb.draw(this.icon, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, icon.getWidth(), icon.getHeight(), false, false);
            }
            FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                    this.text + this.amount, x + this.iconOffset, y, (this.isTyping) ? Color.CYAN : this.textColor);


            this.lockButton.render(sb);
            this.hb.render(sb);
        }

        @Override
        public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
            if(button == this.lockButton) {
                changeBool(isAscending);
            }
        }
        
        private void changeBool(boolean boolToChange) {
            this.isLocked = boolToChange;
            this.actions.onBoolChange(boolToChange, Integer.parseInt(this.amount));
        }

        @Override
        public void setTextField(String textToSet) {
            this.amount = textToSet;
        }

        @Override
        public String getTextField() {
            return this.amount;
        }
    }

    public SeedPanel seedPanel = new SeedPanel();

    public static float GOLD_NUM_OFFSET_X = 65.0F * Settings.scale;

    public static float HP_NUM_OFFSET_X = 60.0F * Settings.scale;

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("StatModSelectScreen"));
    public static final String[] TEXT = UiStrings.TEXT;



    public StatModSelectScreen(AbstractCustomScreenRelic<StatModButton> owner) {
        super(owner);
        if (sortHeader == null) this.sortHeader = new StatModSortHeader(this);
        itemHeight = StatModButton.HITBOX_HEIGHT;
        this.items.add(new StatModButton(TopPanel.LABEL[3], TildeKey.isHealthLocked, ImageMaster.TP_HP, HP_NUM_OFFSET_X, Color.SALMON, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.currentHealth;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.healthLockAmount = amountToSet;
                AbstractDungeon.player.currentHealth = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isHealthLocked = boolToChange;
                if(boolToChange) TildeKey.healthLockAmount = amount;
            }
        }));

        this.items.add(new StatModButton(StatModSelectScreen.TEXT[0], TildeKey.isMaxHealthLocked, ImageMaster.TP_HP, HP_NUM_OFFSET_X, Color.SALMON, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.maxHealth;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.maxHealthLockAmount = amountToSet;
                AbstractDungeon.player.maxHealth = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isMaxHealthLocked = boolToChange;
                if(boolToChange) TildeKey.maxHealthLockAmount = amount;
            }
        }));

        this.items.add(new StatModButton(TopPanel.LABEL[4], TildeKey.isGoldLocked, ImageMaster.TP_GOLD, GOLD_NUM_OFFSET_X, Color.GOLD, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.gold;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.goldLockAmount = amountToSet;
                AbstractDungeon.player.displayGold = amountToSet;
                AbstractDungeon.player.gold = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isGoldLocked = boolToChange;
                if(boolToChange) TildeKey.goldLockAmount = amount;
            }
        }));

        StatModButton rewardButton = new StatModButton(TEXT[2], TildeKey.isRewardDuped, ImageMaster.INTENT_BUFF, GOLD_NUM_OFFSET_X, Color.FOREST, new StatModActions() {
            @Override
            public int getAmount() {
                return TildeKey.rewardMultiplier;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.rewardMultiplier = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isRewardDuped = boolToChange;
            }
        });
        rewardButton.lockButton.text = TEXT[3];
        this.items.add(rewardButton);

        this.items.add(new StatModButton(((OrbStrings) ReflectionHacks.getPrivateStatic(EmptyOrbSlot.class, "orbString")).NAME,
                TildeKey.isOrbLocked, ImageMaster.ORB_LIGHTNING, GOLD_NUM_OFFSET_X, Color.GOLD, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.maxOrbs;
            }

            @Override
            public void setAmount(int amountToSet) {

                TildeKey.orbLockAmount = amountToSet;
                int diff = amountToSet - AbstractDungeon.player.maxOrbs;

                AbstractDungeon.player.masterMaxOrbs = amountToSet;

                TildeKey.modifyPlayerOrbs(diff);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isOrbLocked = boolToChange;
                if(boolToChange) TildeKey.orbLockAmount = amount;
            }
        }));

        this.items.add(new StatModButton(EnergyPanel.LABEL[0], TildeKey.isEnergyLocked, ImageMaster.ORB_PLASMA, GOLD_NUM_OFFSET_X, Color.FIREBRICK, new StatModActions() {
            @Override
            public int getAmount() {
                return EnergyPanel.getCurrentEnergy();
            }

            @Override
            public void setAmount(int amountToSet) {
                EnergyPanel.setEnergy(amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isEnergyLocked = boolToChange;
                if(boolToChange) TildeKey.energyLockAmount = amount;
            }
        }));

        this.items.add(new StatModButton(TEXT[4], TildeKey.isMaxEnergyLocked, ImageMaster.ORB_PLASMA, GOLD_NUM_OFFSET_X, Color.FIREBRICK, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.energy.energy;
            }

            @Override
            public void setAmount(int amountToSet) {
                AbstractDungeon.player.energy.energy = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isMaxEnergyLocked = boolToChange;
                if(boolToChange) TildeKey.maxEnergyLockAmount = amount;
            }
        }));

        this.items.add(new StatModButton(TEXT[5], false, ImageMaster.RUN_HISTORY_MAP_ICON_BOSS, GOLD_NUM_OFFSET_X, Color.SCARLET, new StatModActions() {
            @Override
            public int getAmount() {
                return TildeKey.enemyAttackMult;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.enemyAttackMult = amountToSet;
                MonsterGroup mg = AbstractDungeon.getMonsters();
                if(mg != null) {
                    for (AbstractMonster am : mg.monsters)
                        TildeKey.setMonsterDamage(am, amountToSet);
                }
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }));

        this.items.add(new StatModButton(TEXT[8], false, ImageMaster.INTENT_ATK_7, GOLD_NUM_OFFSET_X, Color.SCARLET, new StatModActions() {
            @Override
            public int getAmount() {
                return TildeKey.playerAttackMult;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.playerAttackMult = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }));

        this.items.add(new StatModButton(TEXT[6], false, ImageMaster.DECK_BTN_BASE, GOLD_NUM_OFFSET_X, Color.WHITE, new StatModActions() {
            @Override
            public int getAmount() {
                return BaseMod.MAX_HAND_SIZE;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.maxHandSize = amountToSet;
                BaseMod.MAX_HAND_SIZE = amountToSet;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }));

        this.items.add(new StatModButton(TEXT[7], TildeKey.isDrawPerTurnLocked, ImageMaster.INTENT_DEFEND_BUFF, GOLD_NUM_OFFSET_X, Color.WHITE, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.player.masterHandSize;
            }

            @Override
            public void setAmount(int amountToSet) {
                TildeKey.drawPerTurn = amountToSet;
                int diff = AbstractDungeon.player.gameHandSize - AbstractDungeon.player.masterHandSize;
                AbstractDungeon.player.masterHandSize = amountToSet;
                AbstractDungeon.player.gameHandSize = amountToSet + diff;
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                TildeKey.isDrawPerTurnLocked = boolToChange;
                if (boolToChange) TildeKey.drawPerTurn = amount;
            }
        }));

        this.items.add(new StatModButton(AscensionLevelUpTextEffect.TEXT[0], false, ImageMaster.TP_ASCENSION, GOLD_NUM_OFFSET_X, Color.WHITE, new StatModActions() {
            @Override
            public int getAmount() {
                return AbstractDungeon.ascensionLevel;
            }

            @Override
            public void setAmount(int amountToSet) {
                amountToSet = Math.min(amountToSet, 20);//hard coding it since no where to get non-hard coded version

                if(AbstractDungeon.isAscensionMode) {
                    if(AbstractDungeon.ascensionLevel < 10 && amountToSet >= 10) {
                        if(AbstractDungeon.player.masterDeck.group.stream().noneMatch(c -> c.cardID.equals(AscendersBane.ID)))
                            AbstractDungeon.player.masterDeck.addToBottom(new AscendersBane());
                    } else if (AbstractDungeon.ascensionLevel >= 10 && amountToSet < 10) {
                        AbstractCard bane = AbstractDungeon.player.masterDeck.findCardById(AscendersBane.ID);
                        if(bane != null)
                            AbstractDungeon.player.masterDeck.removeCard(bane);
                    }
                }

                AbstractDungeon.ascensionLevel = amountToSet;
                AbstractDungeon.topPanel.setupAscensionMode();
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {

            }
        }));

    }


    @Override
    protected void sortOnOpen() {

    }

    @Override
    protected boolean testFilters(StatModButton item) {
        return false;
    }

    @Override
    protected void updateItemClickLogic() {

    }

    @Override
    public void close() {
        super.close();

        for(StatModButton smb : this.items) {
            if(smb.isTyping) smb.stopTyping();
        }

        if(this.seedPanel.shown) this.seedPanel.close();

        MonsterGroup mg = AbstractDungeon.getMonsters();
        if(TildeKey.isKillAllMode && mg != null && !mg.areMonstersDead()) {
            this.owner.flash();
            TildeKey.killAllMonsters();
        }
        if(TildeKey.isNegatingDebuffs && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            AbstractDungeon.actionManager.addToTop(new RemoveDebuffsAction(AbstractDungeon.player));
        }
    }

    @Override
    public void updateFilters() {

    }

    @Override
    public void sort(boolean isAscending) {

    }

    @Override
    protected void callOnOpen() {

    }

    @Override
    protected void updateList(ArrayList<StatModButton> list) {
        for (StatModButton smb : list)
        {

            smb.update();
            if (smb.hb.hovered)
            {
                hoveredItem = smb;
            }
//            if (smb.hb.clicked) {
//                smb.hb.clicked = false;
//
//            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<StatModButton> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;

        for(StatModButton smb: this.items) {
            if (col == 1) {
                col = 0;
                row += 1;
            }
            curX = (START_X+ 400.0F * Settings.scale + SPACE_X * col);
            curY = (scrollY - SPACE * row);

            smb.x = curX;
            smb.y = curY;

            smb.render(sb);

            col += 1;
        }
        calculateScrollBounds();
    }

    @Override
    public void update() {
        if(!seedPanel.shown) super.update();
        if(seedPanel.shown) seedPanel.update();

    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);

        if(this.seedPanel.shown) this.seedPanel.render(sb);
    }
}
