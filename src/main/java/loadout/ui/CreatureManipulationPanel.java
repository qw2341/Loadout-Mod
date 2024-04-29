package loadout.ui;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;
import loadout.LoadoutMod;
import loadout.relics.AllInOneBag;
import loadout.relics.BottledMonster;
import loadout.relics.TildeKey;
import loadout.screens.MonsterSelectScreen;
import loadout.screens.PowerSelectScreen;
import loadout.uiElements.CreatureManipulationButton;
import loadout.uiElements.UIElement;

import java.util.ArrayList;

import static loadout.screens.MonsterSelectScreen.MonsterButton.calculateSmartDistance;

public class CreatureManipulationPanel implements UIElement {

    private static final UIStrings UiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CreatureManipulationPanel"));
    public static final String[] TEXT = UiStrings.TEXT;
    public AbstractCreature creature;
    public ArrayList<CreatureManipulationButton> buttons;

    private float oldMX = 0;
    private float oldMY = 0;
    private float drawXDiff = 0, drawYDiff = 0;
    public boolean isHidden;

    public CreatureManipulationPanel(AbstractCreature creature) {
        this.isHidden = true;
        this.creature = creature;
        this.buttons = new ArrayList<>();
        //move
        this.buttons.add(new CreatureManipulationButton(TEXT[0], () -> {
            oldMX = InputHelper.mX;
            oldMY = InputHelper.mY;
            drawXDiff = creature.drawX - creature.hb_x;
            drawYDiff = creature.drawY - creature.hb_y;
        }, () -> {
            ReflectionHacks.privateMethod(AbstractCreature.class,"refreshHitboxLocation").invoke(creature);
            oldMX = 0;
            oldMY = 0;
        }, () -> {
            if(oldMX != 0 && oldMY != 0) {
                float xDiff = InputHelper.mX - oldMX;
                float yDiff = InputHelper.mY - oldMY;

                creature.drawX += xDiff;
                creature.drawY += yDiff;

                //LoadoutMod.logger.info("Creature X moved to {}", creature.drawX);
                //LoadoutMod.logger.info("Creature Y moved to {}", creature.drawY);

            }

            oldMX = InputHelper.mX;
            oldMY = InputHelper.mY;
        }));
        //edit
        this.buttons.add(new CreatureManipulationButton(TEXT[1], () -> {
            AllInOneBag.INSTANCE.powerGiver.openSingle(creature);
        }));
        //morph
        this.buttons.add(new CreatureManipulationButton(TEXT[5], () -> {
            TildeKey.morphee = creature;
            AllInOneBag.INSTANCE.tildeKey.openMorphMenu();
        }));
        //kill
        this.buttons.add(new CreatureManipulationButton(TEXT[2], () -> {
            AbstractDungeon.actionManager.addToTop(new InstantKillAction(creature));
            if(!Settings.FAST_MODE) AbstractDungeon.actionManager.addToTop((AbstractGameAction)new VFXAction((AbstractGameEffect)new WeightyImpactEffect(creature.hb.cX, creature.hb.cY)));
        }));
        //remove
        this.buttons.add(new CreatureManipulationButton(TEXT[6], () -> {
            TildeKey.target = creature;
            AllInOneBag.INSTANCE.tildeKey.openMonsterEditMenu();
        }));
        //dupe
        if(creature instanceof AbstractMonster) this.buttons.add(new CreatureManipulationButton(TEXT[4], () -> {
            AbstractRoom ar = AbstractDungeon.getCurrRoom();
            if(ar!=null && ar.monsters!= null) {
                AbstractMonster monsterTemp = MonsterSelectScreen.spawnMonster((Class<? extends AbstractMonster>) creature.getClass(),creature.drawX - calculateSmartDistance(creature, creature) + 30.0F * (float) Math.random(), creature.drawY + 20.0F * (float) Math.random());
                BottledMonster.copyMonster((AbstractMonster) creature, monsterTemp);
                //ar.monsters.monsters.add(monsterTemp);
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(monsterTemp, false, -99));
            }
        }));
    }

    @Override
    public void render(SpriteBatch sb) {
        if(isHidden) return;

        for(CreatureManipulationButton cb : buttons) {
            cb.render(sb);
        }
    }

    @Override
    public void update() {
        if(AbstractDungeon.isScreenUp || !LoadoutMod.enableCreatureManipulation) this.isHidden = true;
        if(isHidden) return;

        boolean isHoveringButtons = false;
        float panelX = creature.hb.x + creature.hb.width / 2f;
        //if(panelX + CreatureManipulationButton.ROW_WIDTH > Settings.WIDTH) panelX = creature.hb.x - creature.hb.width / 2f - CreatureManipulationButton.ROW_WIDTH;
        float panelY = creature.hb.y + creature.hb.height / 2f;
        //if(panelX + CreatureManipulationButton.ROW_WIDTH > Settings.WIDTH) panelX = creature.hb.x - creature.hb.width / 2f - CreatureManipulationButton.ROW_WIDTH;
        for(CreatureManipulationButton cb : buttons) {
            cb.move(panelX, panelY);
            panelY -= CreatureManipulationButton.ROW_RENDER_HEIGHT;
            cb.update();
            if(cb.hb.hovered) isHoveringButtons = true;
        }

        if(!isHoveringButtons && (InputHelper.justClickedLeft || InputHelper.justClickedRight)) {
            this.isHidden = true;
        }
    }
}
