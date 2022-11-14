package loadout.screens;

import basemod.interfaces.TextReceiver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.codedisaster.steamworks.SteamUtils;
import com.megacrit.cardcrawl.actions.unique.RemoveDebuffsAction;
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
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.panels.SeedPanel;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import loadout.LoadoutMod;
import loadout.helper.TextInputHelper;
import loadout.helper.TextInputReceiver;
import loadout.relics.TildeKey;

import java.util.ArrayList;


public class StatModSelectScreen extends SelectScreen<StatModSelectScreen.StatModButton>{
    protected enum ModType {
        HEALTH, MAX_HEALTH, MONEY
    }

    public static class StatModButton implements HeaderButtonPlusListener, TextInputReceiver {
        public ModType modType;

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

        public StatModButton(ModType mt) {
            this.modType = mt;

            this.x = 0;
            this.y = 0;

            switch (mt) {

                case HEALTH:
                    this.text = TopPanel.LABEL[3];
                    this.isLocked = TildeKey.isHealthLocked;
                    break;
                case MAX_HEALTH:
                    this.text = StatModSelectScreen.TEXT[0];
                    this.isLocked = TildeKey.isMaxHealthLocked;
                    break;
                case MONEY:
                    this.text = TopPanel.LABEL[4];
                    this.isLocked = TildeKey.isGoldLocked;
                    break;
            }
            this.text += ": ";
            this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text, Float.MAX_VALUE, 0.0F);

            this.hb = new Hitbox(x + this.textWidth/2 ,y,this.textWidth + HP_NUM_OFFSET_X,75);
            this.amount = "0";


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
                switch (this.modType) {
                    case HEALTH:
                        this.amount = String.valueOf(AbstractDungeon.player.currentHealth);
                        break;
                    case MAX_HEALTH:
                        this.amount = String.valueOf(AbstractDungeon.player.maxHealth);
                        break;
                    case MONEY:
                        this.amount = String.valueOf(AbstractDungeon.player.gold);
                        break;
                }
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


            switch (this.modType) {
                case HEALTH:
                    TildeKey.healthLockAmount = amtToSet;
                    AbstractDungeon.player.currentHealth = amtToSet;
                    break;
                case MAX_HEALTH:
                    TildeKey.maxHealthLockAmount = amtToSet;
                    AbstractDungeon.player.maxHealth = amtToSet;
                    break;
                case MONEY:
                    TildeKey.goldLockAmount = amtToSet;
                    AbstractDungeon.player.displayGold = amtToSet;
                    AbstractDungeon.player.gold = amtToSet;
                    break;
            }
            this.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text + amount, Float.MAX_VALUE, 0.0F);

        }

        public void render(SpriteBatch sb) {
//            if (this.isTyping) {
//                sb.setColor(Color.GRAY);
//            } else {
                sb.setColor(Color.WHITE);
//            }
            switch (this.modType) {
                case HEALTH:

                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                            this.text + this.amount, x + HP_NUM_OFFSET_X, y, (this.isTyping) ? Color.CYAN : Color.SALMON);
                    break;
                case MAX_HEALTH:
                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_HP, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                            this.text + this.amount, x + HP_NUM_OFFSET_X, y, (this.isTyping) ? Color.CYAN :  Color.SCARLET);
                    break;
                case MONEY:
                    if (this.hb.hovered) {
                        sb.draw(ImageMaster.TP_GOLD, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale * 1.2F, Settings.scale * 1.2F, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    else {
                        sb.draw(ImageMaster.TP_GOLD, this.x - 32.0F + 32.0F * Settings.scale, this.y - 32.0F * Settings.scale, 32.0F, 32.0F, 64.0F, 64.0F, Settings.scale, Settings.scale, 0.0F, 0, 0, 64, 64, false, false);
                    }
                    FontHelper.renderFontLeftTopAligned(sb, FontHelper.topPanelInfoFont,


                            this.text + this.amount, x + GOLD_NUM_OFFSET_X, y,  (this.isTyping) ? Color.CYAN : Settings.GOLD_COLOR);
                    break;

            }
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
            switch (this.modType) {

                case HEALTH:
                    TildeKey.isHealthLocked = boolToChange;
                    if(boolToChange) TildeKey.healthLockAmount = Integer.parseInt(this.amount);
                    break;
                case MAX_HEALTH:
                    TildeKey.isMaxHealthLocked = boolToChange;
                    if(boolToChange) TildeKey.maxHealthLockAmount = Integer.parseInt(this.amount);
                    break;
                case MONEY:
                    TildeKey.isGoldLocked = boolToChange;
                    if(boolToChange) TildeKey.goldLockAmount = Integer.parseInt(this.amount);
                    break;
            }
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



    public StatModSelectScreen(AbstractRelic owner) {
        super(owner);
        if (sortHeader == null) this.sortHeader = new StatModSortHeader(this);
        this.items = new ArrayList<StatModButton>();

        this.items.add(new StatModButton(ModType.HEALTH));
        this.items.add(new StatModButton(ModType.MAX_HEALTH));
        this.items.add(new StatModButton(ModType.MONEY));
    }


    @Override
    protected void sortOnOpen() {

    }

    @Override
    public void open() {
        super.open();

    }
    @Override
    public void close() {
        super.close();
        TildeKey.isSelectionScreenUp = false;

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
