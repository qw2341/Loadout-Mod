package loadout.ui;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.InstantKillAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.WeightyImpactEffect;
import loadout.LoadoutMod;
import loadout.relics.AllInOneBag;
import loadout.relics.TildeKey;
import loadout.screens.PowerSelectScreen;
import loadout.uiElements.CreatureManipulationButton;
import loadout.uiElements.UIElement;

import java.util.ArrayList;

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
            AllInOneBag.getInstance().powerGiver.openSingle(creature);
        }));
        //kill
        this.buttons.add(new CreatureManipulationButton(TEXT[2], () -> {
            AbstractDungeon.actionManager.addToTop(new InstantKillAction(creature));
            AbstractDungeon.actionManager.addToTop((AbstractGameAction)new VFXAction((AbstractGameEffect)new WeightyImpactEffect(creature.hb.cX, creature.hb.cY)));
        }));
        //remove
//        if(!creature.isPlayer) this.buttons.add(new CreatureManipulationButton(TEXT[3], () -> {
//            AbstractDungeon.getCurrRoom().monsters.monsters.remove(creature);
//        }));
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
