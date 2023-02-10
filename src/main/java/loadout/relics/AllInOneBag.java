package loadout.relics;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.mod.stslib.relics.ClickableRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnPlayerDeathRelic;
import com.evacipated.cardcrawl.mod.stslib.relics.OnReceivePowerRelic;
import com.megacrit.cardcrawl.audio.Sfx;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.ShaderHelper;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.patches.RelicPopUpPatch;
import loadout.savables.RelicSavables;
import loadout.uiElements.XGGGIcon;

import java.util.ArrayList;

import static loadout.LoadoutMod.*;
import static loadout.relics.AbstractCustomScreenRelic.isIsaacMode;

public class AllInOneBag extends CustomRelic implements ClickableRelic, CustomSavable<RelicSavables>, OnReceivePowerRelic, OnPlayerDeathRelic {
    protected static final Sfx landingSfx = AbstractCustomScreenRelic.landingSfx;

    public static boolean isSelectionScreenUp = true;

    public static final String ID = LoadoutMod.makeID("AllInOneBag");
    public static final Texture IMG = LoadoutBag.IMG;
    private static final Texture OUTLINE = LoadoutBag.OUTLINE;

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
    public ArrayList<CustomRelic> loadoutRelics;
    public ArrayList<AbstractCustomScreenRelic<?>> customScreenRelics;
    public ArrayList<AbstractCardScreenRelic> cardScreenRelics;

    Color color = new Color();

    static final float SIDE_PANEL_X = 50.0F * Settings.scale;

    XGGGIcon xgggIcon;

    public AllInOneBag() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.FLAT);
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
        loadoutRelics.forEach(customRelic -> {
            customRelic.isObtained = true;
            RelicPopUpPatch.IsInsideAnotherRelicField.isInsideAnother.set(customRelic, Boolean.TRUE);
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

        cardScreenRelics = new ArrayList<>();
        cardScreenRelics.add(this.cardPrinter);
        cardScreenRelics.add(this.cardShredder);
        cardScreenRelics.add(this.cardModifier);

        if(isSelectionScreenUp) showRelics();
        else hideRelics();

        if(isXggg()) {
            xgggIcon = new XGGGIcon(this.currentX, this.currentY);
            xgggIcon.setAngle(330.0f);
            xgggIcon.scale = 0.75F;
        }
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    @Override
    public void relicTip() {}
    @Override
    public void onUnequip() {
        closeAllScreens();
    }
    public void closeAllScreens() {
        for(AbstractCustomScreenRelic<?> r : customScreenRelics) {
            if(r.isSelectionScreenUp()) r.selectScreen.close();
        }
        for(AbstractCardScreenRelic r : cardScreenRelics) {
            if(r.isSelectionScreenUp()) r.selectScreen.close();
        }
    }

    @Override
    public void onRightClick() {
        if (!isObtained) {
            // If it has been used this turn, the player doesn't actually have the relic (i.e. it's on display in the shop room), or it's the enemy's turn
            return; // Don't do anything.
        }
        isSelectionScreenUp = !isSelectionScreenUp;
        if(isSelectionScreenUp){
            showRelics();
        } else {
            closeAllScreens();
            hideRelics();
        }
    }
    private void moveRelic(AbstractRelic r) {
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
           else r.hb.move(0,0);
        }
    }
    public void showRelics(){
        float xPos = SIDE_PANEL_X;

        float yPos = Settings.HEIGHT - 180.0F * Settings.yScale;
        float spaceY = 65.0F * Settings.scale;
        for (CustomRelic cr : loadoutRelics) {
            yPos -= spaceY;
            cr.targetX = xPos;
            cr.targetY = yPos;
        }
    }
    private void hideRelics(){
        hideAllRelics();
    }
    public void hideAllRelics(){
        for (CustomRelic cr : loadoutRelics) {
            cr.targetX = -SIDE_PANEL_X;
        }
    }

    @Override
    public void update()
    {
        super.update();
        if(isXggg() && isInScreen()) {
            if(this.hovered()) {
                this.xgggIcon.setX(MathUtils.lerp(xgggIcon.getX(), this.currentX + 25.0F,Gdx.graphics.getDeltaTime() * 6.0F));
                this.xgggIcon.setY(MathUtils.lerp(xgggIcon.getY(), this.currentY + 25.0F, Gdx.graphics.getDeltaTime() * 6.0F));
            } else {
                this.xgggIcon.setX(MathUtils.lerp(xgggIcon.getX(), this.currentX,Gdx.graphics.getDeltaTime() * 6.0F * 2.0F));
                this.xgggIcon.setY(MathUtils.lerp(xgggIcon.getY(), this.currentY, Gdx.graphics.getDeltaTime() * 6.0F * 2.0F));
            }
            this.xgggIcon.update();
        }

        if(isObtained) {
            for (CustomRelic cr : loadoutRelics) {
                moveRelic(cr);
                cr.update();
                cr.hb.update();
                if(cr.hb.hovered && (InputHelper.justClickedRight || CInputHelper.isJustPressed(Input.Keys.BUTTON_A))) ((ClickableRelic)cr).onRightClick();
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
        return ReflectionHacks.getPrivateStatic(AbstractRelic.class, "offsetX");
    }

    @Override
    public void renderInTopPanel(SpriteBatch sb)
    {
        if (!Settings.hideRelics) {
            if(isXggg() && isInScreen()) {
                if(xgggIcon.getX() != this.currentX || xgggIcon.getY() != this.currentY) {
                    xgggIcon.render(sb);
                }
            }
            this.renderOutline(sb, true);
            if (this.grayscale) {
                ShaderHelper.setShader(sb, ShaderHelper.Shader.GRAYSCALE);
            }
            updateColor();
            sb.setColor(this.color);
            sb.draw(this.img, this.currentX - 64.0F + (float) getOffsetX(), this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0F, 0, 0, 128, 128, false, false);
            if (this.grayscale) {
                ShaderHelper.setShader(sb, ShaderHelper.Shader.DEFAULT);
            }

            this.renderCounter(sb, true);
            this.renderFlash(sb, true);
            this.hb.render(sb);
        }

        for (CustomRelic cr : loadoutRelics) {
            cr.renderInTopPanel(sb);
            if (cr.hb.hovered) cr.renderTip(sb);
        }

    }

    @Override
    public void render(SpriteBatch sb, boolean renderAmount, Color outlineColor) {
        if(isXggg() && isInScreen()) {
            if(xgggIcon.getX() != this.currentX || xgggIcon.getY() != this.currentY) {
                xgggIcon.render(sb);
            }
        }

        if (this.isSeen) {
            this.renderOutline(outlineColor, sb, false);
        } else {
            this.renderOutline(Color.LIGHT_GRAY, sb, false);
        }

        if (this.isSeen) {
            updateColor();
            sb.setColor(this.color);
        } else if (this.hb.hovered) {
            sb.setColor(Settings.HALF_TRANSPARENT_BLACK_COLOR);
        } else {
            sb.setColor(Color.BLACK);
        }

        if (AbstractDungeon.screen != null && AbstractDungeon.screen == AbstractDungeon.CurrentScreen.NEOW_UNLOCK) {
            if (this.largeImg == null) {
                sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, Settings.scale * 2.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 15.0F, Settings.scale * 2.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 15.0F, 0.0F, 0, 0, 128, 128, false, false);
            } else {
                sb.draw(this.largeImg, this.currentX - 128.0F, this.currentY - 128.0F, 128.0F, 128.0F, 256.0F, 256.0F, Settings.scale * 1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 30.0F, Settings.scale * 1.0F + MathUtils.cosDeg((float)(System.currentTimeMillis() / 5L % 360L)) / 30.0F, 0.0F, 0, 0, 256, 256, false, false);
            }
        } else {
            sb.draw(this.img, this.currentX - 64.0F, this.currentY - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, this.scale, this.scale, 0.0F, 0, 0, 128, 128, false, false);
        }

        if (this.hb.hovered && !CardCrawlGame.relicPopup.isOpen) {
            if (!this.isSeen) {
                if ((float)InputHelper.mX < 1400.0F * Settings.scale) {
                    TipHelper.renderGenericTip((float)InputHelper.mX + 60.0F * Settings.scale, (float)InputHelper.mY - 50.0F * Settings.scale, LABEL[1], MSG[1]);
                } else {
                    TipHelper.renderGenericTip((float)InputHelper.mX - 350.0F * Settings.scale, (float)InputHelper.mY - 50.0F * Settings.scale, LABEL[1], MSG[1]);
                }

                return;
            }

            this.renderTip(sb);
        }

        this.hb.render(sb);
    }

    private boolean isInScreen() {
        return this.currentX > 0 && this.currentX < Settings.WIDTH && this.currentY > 0 && this.currentY < Settings.HEIGHT;
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

    @Override
    public AbstractRelic makeCopy() {
        return new AllInOneBag();
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

    @Override
    public void onSpawnMonster(AbstractMonster monster) {
        powerGiver.onSpawnMonster(monster);
        tildeKey.onSpawnMonster(monster);
    }

    public void battleStartPreDraw() {
        powerGiver.battleStartPreDraw();
    }

    @Override
    public void atBattleStart() {
        tildeKey.atBattleStart();
    }

    @Override
    public void atTurnStart() {
        tildeKey.atTurnStart();
    }

    @Override
    public void onRefreshHand() {
        tildeKey.onRefreshHand();
    }

    @Override
    public int onAttackedToChangeDamage(DamageInfo info, int damageAmount) {
        return tildeKey.onAttackedToChangeDamage(info, damageAmount);
    }

    @Override
    public boolean onPlayerDeath(AbstractPlayer abstractPlayer, DamageInfo damageInfo) {
        return tildeKey.onPlayerDeath(abstractPlayer, damageInfo);
    }

    @Override
    public boolean onReceivePower(AbstractPower abstractPower, AbstractCreature abstractCreature) {
        return tildeKey.onReceivePower(abstractPower, abstractCreature);
    }

    @Override
    public int onReceivePowerStacks(AbstractPower power, AbstractCreature source, int stackAmount) {
        return tildeKey.onReceivePowerStacks(power, source, stackAmount);
    }

    @Override
    public void onPreviewObtainCard(AbstractCard c) {
        cardModifier.onPreviewObtainCard(c);
    }

    @Override
    public void onObtainCard(AbstractCard c) {
        cardModifier.onObtainCard(c);
    }
}
