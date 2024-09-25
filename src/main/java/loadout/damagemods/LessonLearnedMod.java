package loadout.damagemods;

import com.evacipated.cardcrawl.mod.stslib.damagemods.AbstractDamageModifier;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

public class LessonLearnedMod extends AbstractOnKillMod{
    public LessonLearnedMod(Supplier<Integer> getValue) {
        super(getValue);
    }

    @Override
    public void onKill(DamageInfo info, int lastDamageTaken, int overkillAmount, AbstractCreature target, int amount) {
        ArrayList<AbstractCard> possibleCards = new ArrayList<>();
        AbstractCard theCard = null;

        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.canUpgrade()) {
                possibleCards.add(c);
            }
        }

        int upgradesToPerform = Math.min(getValue.get(), possibleCards.size());

        for (int i = 0; i < upgradesToPerform; i++) {
            theCard = possibleCards.get(i);
            theCard.upgrade();
            AbstractDungeon.player.bottledCardUpgradeCheck(theCard);

            // Add effects for each upgraded card
            AbstractDungeon.effectsQueue.add(new UpgradeShineEffect((float) Settings.WIDTH / (upgradesToPerform + 1) * (i + 1) , (float) Settings.HEIGHT / 2.0F));
            AbstractDungeon.topLevelEffectsQueue.add(new ShowCardBrieflyEffect(theCard.makeStatEquivalentCopy()));
            addToTop(new WaitAction(Settings.ACTION_DUR_MED));
        }
    }

    @Override
    public AbstractDamageModifier makeCopy() {
        return new LessonLearnedMod(getValue);
    }
}
