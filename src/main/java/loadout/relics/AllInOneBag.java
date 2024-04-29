package loadout.relics;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.google.gson.reflect.TypeToken;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import loadout.LoadoutMod;
import loadout.savables.RelicSavables;
import loadout.uiElements.UIElement;
import loadout.uiElements.XGGGIcon;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static loadout.LoadoutMod.*;


public class AllInOneBag implements UIElement, CustomSavable<RelicSavables> {
    public static boolean isSelectionScreenUp = true;

    public static final String ID = LoadoutMod.makeID("AllInOneBag");
    public static Texture IMG = null;
    private static Texture OUTLINE = null;

    public LoadoutBag loadoutBag;
    public TrashBin trashBin;
    public LoadoutCauldron loadoutCauldron;
    public CardPrinter cardPrinter;
    public CardShredder cardShredder;
    public CardModifier cardModifier;
    public EventfulCompass eventfulCompass;
    public PowerGiver powerGiver;
    public TildeKey tildeKey;
    public BottledMonster bottledMonster;
    public OrbBox orbBox;
    public BlightChest blightChest;
    public ArrayList<LoadoutRelic> loadoutRelics;
    public ArrayList<AbstractCustomScreenRelic<?>> customScreenRelics;
    public ArrayList<AbstractCardScreenRelic> cardScreenRelics;

    Color color = new Color();

    static final float SIDE_PANEL_X = 50.0F * Settings.scale;
    static float panelRelicRenderScale = Settings.scale;

    XGGGIcon xgggIcon;
    boolean showXGGG;
    float showXGGGTimer;

    public static AllInOneBag INSTANCE;

    public static Texture SIDE_PANEL_TAB;
    public static Texture SIDE_PANEL_ARROW;

    public boolean isDone;
    public boolean isAnimating;

    public Hitbox hb;

    private float rotation;

    public float currentX;
    public float currentY;
    public float targetX;
    public float targetY;
    public float scale;

    public AllInOneBag() {
        this.scale = Settings.scale;
        this.isDone = false;
        this.isAnimating = false;
        this.hb = new Hitbox(36.0f * Settings.scale,128.0f * Settings.scale);

        this.loadoutBag = new LoadoutBag();
        this.trashBin = new TrashBin();
        this.loadoutCauldron = new LoadoutCauldron();
        this.cardPrinter = new CardPrinter();
        this.cardShredder = new CardShredder();
        this.cardModifier = new CardModifier();
        this.eventfulCompass = new EventfulCompass();
        this.powerGiver = new PowerGiver();
        this.tildeKey = new TildeKey();
        this.bottledMonster = new BottledMonster();
        this.orbBox = new OrbBox();
        this.blightChest = new BlightChest();
        loadoutRelics = new ArrayList<>();
        loadoutRelics.add(this.loadoutBag);
        loadoutRelics.add(this.trashBin);
        loadoutRelics.add(this.loadoutCauldron);
        loadoutRelics.add(this.cardPrinter);
        loadoutRelics.add(this.cardShredder);
        loadoutRelics.add(this.cardModifier);
        loadoutRelics.add(this.eventfulCompass);
        loadoutRelics.add(this.powerGiver);
        loadoutRelics.add(this.tildeKey);
        loadoutRelics.add(this.bottledMonster);
        loadoutRelics.add(this.orbBox);
        loadoutRelics.add(this.blightChest);
        loadoutRelics.forEach(lr -> {
            lr.isObtained = true;
        });
        customScreenRelics = new ArrayList<>();
        customScreenRelics.add(this.loadoutBag);
        customScreenRelics.add(this.trashBin);
        customScreenRelics.add(this.loadoutCauldron);
        customScreenRelics.add(this.eventfulCompass);
        customScreenRelics.add(this.powerGiver);
        customScreenRelics.add(this.tildeKey);
        customScreenRelics.add(this.bottledMonster);
        customScreenRelics.add(this.orbBox);
        customScreenRelics.add(this.blightChest);

        cardScreenRelics = new ArrayList<>();
        cardScreenRelics.add(this.cardPrinter);
        cardScreenRelics.add(this.cardShredder);
        cardScreenRelics.add(this.cardModifier);

        xgggIcon = new XGGGIcon(this.currentX, this.currentY);
        xgggIcon.setAngle(330.0f);
        xgggIcon.scale = 0.5F;
        showXGGG = false;
        showXGGGTimer = 0.0f;

        initBagPos();

        for(LoadoutRelic cr: loadoutRelics){
            cr.currentX = -100f;
            cr.targetX = -100f;
            cr.currentY = currentY;
            cr.targetY = targetY;
            cr.scale = 0;
        }

        isSelectionScreenUp = false;
    }

    public void closeAllScreens() {
        for(AbstractCustomScreenRelic<?> r : customScreenRelics) {
            if(r.isSelectionScreenUp()) r.selectScreen.close();
        }
        if (tildeKey.morphMenu != null && tildeKey.morphMenu.isOpen()) tildeKey.morphMenu.close();
        for(AbstractCardScreenRelic r : cardScreenRelics) {
            if(r.isSelectionScreenUp()) r.selectScreen.close();
        }
    }

    public void onRightClick() {

        isSelectionScreenUp = !isSelectionScreenUp;
        if(isSelectionScreenUp){
            showRelics();
        } else {
            closeAllScreens();
            hideRelics();
        }
    }
    private void moveRelic(LoadoutRelic r) {
        if (r.currentX != r.targetX) {
            r.currentX = MathUtils.lerp(r.currentX, r.targetX, Gdx.graphics.getDeltaTime() * 6.0F);
            if (Math.abs(r.currentX - r.targetX) < 0.5F) {
                r.currentX = r.targetX;
            }
        }

        if (r.currentY != r.targetY) {
            r.currentY = MathUtils.lerp(r.currentY, r.targetY, Gdx.graphics.getDeltaTime() * 6.0F);
            if (Math.abs(r.currentY - r.targetY) < 0.5F) {
                r.currentY = r.targetY;
            }
        }

        if (r.hb != null) {
           if(isSelectionScreenUp) r.hb.move(r.currentX, r.currentY);
           else r.hb.move(-100.0f * Settings.scale,-100.0f * Settings.scale);
        }
    }

    private void move() {
        if (this.currentX != this.targetX) {
            this.currentX = MathUtils.lerp(this.currentX, this.targetX, Gdx.graphics.getDeltaTime() * 6.0F);
            if (Math.abs(this.currentX - this.targetX) < 0.5F) {
                this.currentX = this.targetX;
            }
        }

        if (this.currentY != this.targetY) {
            this.currentY = MathUtils.lerp(this.currentY, this.targetY, Gdx.graphics.getDeltaTime() * 6.0F);
            if (Math.abs(this.currentY - this.targetY) < 0.5F) {
                this.currentY = this.targetY;
            }
        }

        if (this.hb != null) {
            this.hb.move(this.currentX, this.currentY);
        }
    }

    public void initBagPos() {
        currentY = Settings.HEIGHT / 2f;
        targetY = currentY;
//        currentX = SIDE_PANEL_X + this.hb.width * 1.5f;
        currentX = -100f;
        targetX = currentX;
    }

    public void showButton() {
        targetX = hb.width / 2;
        targetY = Settings.HEIGHT / 2f;
    }

    public void showRelics(){
        isSelectionScreenUp = true;
        float xPos = SIDE_PANEL_X;
        targetX = xPos + this.hb.width * 1.5f;
        targetY = Settings.HEIGHT / 2f;
        float yPos = Settings.HEIGHT - 180.0F * Settings.yScale;
        float spaceY = 65.0F * Settings.scale;
        panelRelicRenderScale = Settings.scale;
        for (LoadoutRelic cr : loadoutRelics) {
            yPos -= spaceY;
            cr.targetX = xPos;
            cr.targetY = yPos;
        }
    }
    private void hideRelics(){
        isSelectionScreenUp = false;
        targetX = hb.width / 2;
        panelRelicRenderScale = 0;
        for (LoadoutRelic cr : loadoutRelics) {
            cr.targetX = -100;
        }
    }
    public void hideAllRelics(){
        isSelectionScreenUp = false;
        panelRelicRenderScale = 0;
        targetX = -100 * Settings.scale;
        for (LoadoutRelic cr : loadoutRelics) {
            cr.targetX = -100;
        }
    }

    private void showXGGG() {
        if(this.xgggIcon != null) {
            this.xgggIcon.setX(MathUtils.lerp(xgggIcon.getX(), this.currentX + 15.0F * Settings.scale + getOffsetX(),Gdx.graphics.getDeltaTime() * 6.0F));
            this.xgggIcon.setY(MathUtils.lerp(xgggIcon.getY(), this.currentY + 40.0F * Settings.scale, Gdx.graphics.getDeltaTime() * 6.0F));
            this.xgggIcon.scale = 0.5f;
        }

    }

    private void hideXGGG() {
        if(this.xgggIcon != null) {
            this.xgggIcon.setX(MathUtils.lerp(xgggIcon.getX(), this.currentX -10.0f * Settings.scale,Gdx.graphics.getDeltaTime() * 6.0F * 2.0F));
            this.xgggIcon.setY(MathUtils.lerp(xgggIcon.getY(), this.currentY, Gdx.graphics.getDeltaTime() * 6.0F * 2.0F));
            this.xgggIcon.scale = MathUtils.lerp(this.xgggIcon.scale, 0,Gdx.graphics.getDeltaTime() * 6.0F * 2.0F);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        this.renderInTopPanel(sb);
    }

    @Override
    public void update()
    {
        if (!this.isDone) {
            if (this.isAnimating) {
                if (this.hb.hovered) {
                    this.scale = Settings.scale * 1.5F;
                } else {
                    this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale * 1.1F);
                }
            } else if (this.hb.hovered) {
                this.scale = Settings.scale * 1.25F;
            } else {
                this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale);
            }


            this.rotation = 0f;

            if (this.currentX != this.targetX) {
                this.currentX = MathUtils.lerp(this.currentX, this.targetX, Gdx.graphics.getDeltaTime() * 6.0F);
                if (Math.abs(this.currentX - this.targetX) < 0.5F) {
                    this.currentX = this.targetX;
                }
            }

            if (this.currentY != this.targetY) {
                this.currentY = MathUtils.lerp(this.currentY, this.targetY, Gdx.graphics.getDeltaTime() * 6.0F);
                if (Math.abs(this.currentY - this.targetY) < 0.5F) {
                    this.currentY = this.targetY;
                }
            }

            if (this.currentY == this.targetY && this.currentX == this.targetX) {
                this.isDone = true;
                if (AbstractDungeon.topPanel != null) {
                    AbstractDungeon.topPanel.adjustRelicHbs();
                }

                this.hb.move(this.currentX, this.currentY);
            }

            this.scale = Settings.scale;


            if (this.hb != null) {
                this.hb.update();
            }

        } else {
            this.hb.update();

            this.scale = MathHelper.scaleLerpSnap(this.scale, Settings.scale);
        }

        if(isXggg()) {
            if(this.hb.hovered) {
                showXGGG();
            } else {
                if(showXGGG) {
                    showXGGGTimer -= Gdx.graphics.getDeltaTime();
                    if(showXGGGTimer < 0) {
                        showXGGG = false;
                    }
                    showXGGG();
                } else {
                    hideXGGG();
                }
            }
            if(this.xgggIcon != null) this.xgggIcon.update();
        }
        move();

        if(this.hb.justHovered) {
            CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
        }

        if(this.hb.hovered && (InputHelper.justClickedLeft || InputHelper.justClickedRight)) {
            InputHelper.justClickedLeft = false;
            InputHelper.justClickedRight = false;
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.2F);
            this.onRightClick();
        }


        for (LoadoutRelic cr : loadoutRelics) {
            moveRelic(cr);
            cr.update();
            cr.hb.update();
            if(cr.hb.justHovered) {
                CardCrawlGame.sound.playA("UI_HOVER", -0.3F);
            }
            if(cr.hb.hovered && (InputHelper.justClickedRight || InputHelper.justClickedLeft || CInputHelper.isJustPressed(Input.Keys.BUTTON_A))) {
                InputHelper.justClickedLeft = false;
                InputHelper.justClickedRight = false;

                cr.playLandingSFX();
                cr.onRightClick();
            }
        }

    }

    private void updateColor() {
        this.color.r = (MathUtils.cosDeg((float)(System.currentTimeMillis() / 10L % 360L)) + 1.25F) / 2.3F;
        this.color.g = (MathUtils.cosDeg((float)((System.currentTimeMillis() + 1000L) / 10L % 360L)) + 1.25F) / 2.3F;
        this.color.b = (MathUtils.cosDeg((float)((System.currentTimeMillis() + 2000L) / 10L % 360L)) + 1.25F) / 2.3F;
        this.color.a = 1.0F;
    }

    private float getOffsetX() {
        return 0;
    }

    public void renderInTopPanel(SpriteBatch sb)
    {
        for (LoadoutRelic cr : loadoutRelics) {
            cr.scale = MathHelper.scaleLerpSnap(cr.scale, panelRelicRenderScale);
            cr.renderInTopPanel(sb);
            if (cr.hb.hovered) {
                Configurator.setLevel(TipHelper.class.getName(), Level.FATAL);
                cr.renderTip(sb);
                Configurator.setLevel(TipHelper.class.getName(), Level.INFO);
            }
        }

        if(isXggg() && isInScreen()) {
            if(xgggIcon.getX() != this.currentX || xgggIcon.getY() != this.currentY) {
                xgggIcon.render(sb);
            }
        }

        updateColor();
        sb.setColor(this.color);
        sb.draw(SIDE_PANEL_TAB, this.currentX - 19.0f, this.currentY - 64.0F, 15.5F, 64.0F, 31.0F, 128.0F, this.scale, this.scale, 0.0F, 0, 0, 31, 128, false, false);
        sb.draw(SIDE_PANEL_ARROW,this.currentX - 20.0f, this.currentY - 16.0F, 16.0F, 16.0F, 32.0F, 32.0F, this.scale, this.scale, 0.0F, 0, 0, 32, 32, isSelectionScreenUp, false);

        this.hb.render(sb);

    }


    private boolean isInScreen() {
        return this.currentX > 0 && this.currentX < Settings.WIDTH && this.currentY > 0 && this.currentY < Settings.HEIGHT;
    }

    @Override
    public Type savedType() {
        return new TypeToken<RelicSavables>(){}.getType();
    }

    @Override
    public RelicSavables onSave() {
        return new RelicSavables(cardModifier.onSave(), powerGiver.onSave(), tildeKey.onSave());
    }

    @Override
    public void onLoad(RelicSavables save) {
        if(save == null) return;
        cardModifier.onLoad(save.modifierSave);
        powerGiver.onLoad(save.powerGiverSave);
        tildeKey.onLoad(save.tildeKeySave);
    }

    public void onSpawnMonster(AbstractMonster monster) {
        powerGiver.onSpawnMonster(monster);
        tildeKey.onSpawnMonster(monster);
    }

    public void battleStartPreDraw() {
        powerGiver.battleStartPreDraw();
    }

    public void atBattleStart() {
        tildeKey.atBattleStart();
    }

    public void atTurnStart() {
        tildeKey.atTurnStart();
    }

    public void onRefreshHand() {
        tildeKey.onRefreshHand();
    }

    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        return tildeKey.onAttackedToChangeDamage(info, damageAmount);
    }

    public boolean onPlayerDeath(AbstractPlayer abstractPlayer, DamageInfo damageInfo) {
        return tildeKey.onPlayerDeath(abstractPlayer, damageInfo);
    }

    public boolean onReceivePower(AbstractPower abstractPower, AbstractCreature abstractCreature) {
        return tildeKey.onReceivePower(abstractPower, abstractCreature);
    }

    public int onReceivePowerStacks(AbstractPower power, AbstractCreature source, int stackAmount) {
        return tildeKey.onReceivePowerStacks(power, source, stackAmount);
    }

    public void onPreviewObtainCard(AbstractCard c) {
        cardModifier.onPreviewObtainCard(c);
    }

    public void onObtainCard(AbstractCard c) {
        cardModifier.onObtainCard(c);
    }


    public void onMonsterDeath(AbstractMonster m) {
        this.showXGGG = true;
        switch (m.type) {
            case NORMAL:
                this.showXGGGTimer = 2.5F;
                break;
            case ELITE:
                this.showXGGGTimer = 7.5F;
                break;
            case BOSS:
                this.showXGGGTimer = 15.0F;
                break;
            default:
                this.showXGGGTimer = 5.0F;
                break;
        }
    }

    public void xgggSay(String msg) {
        if(!isXggg()) return;
        showXGGG = true;
        showXGGGTimer = 4.0F;
        xgggIcon.say(msg);
    }

    public static void XGGGSay(String msg) {
        AllInOneBag.INSTANCE.xgggSay(msg);
    }

    public float atDamageModify(float damage, AbstractCard c) {
        return tildeKey.atDamageModify(damage, c);
    }

    public void onStartingNewRun() {
        powerGiver.resetPowers();
        TildeKey.resetToDefault();
        tildeKey.selectScreen = tildeKey.getNewSelectScreen();
    }
}
