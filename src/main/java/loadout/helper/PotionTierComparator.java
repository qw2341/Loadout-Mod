package loadout.helper;

import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.Comparator;

public class PotionTierComparator implements Comparator<AbstractPotion> {

    private PotionNameComparator pNC;

    public PotionTierComparator() {
        super();
        pNC = new PotionNameComparator();
    }
    @Override
    public int compare(AbstractPotion o1, AbstractPotion o2) {
        int r1T = tierToInt(o1.rarity);
        int r2T = tierToInt(o2.rarity);
        //compare tier first, then compare name
        return r1T<r2T? -1: r1T==r2T? pNC.compare(o1,o2) : 1;
    }

    @Override
    public Comparator<AbstractPotion> reversed() {
        return (o1,o2) -> {
            int r1T = tierToInt(o1.rarity);
            int r2T = tierToInt(o2.rarity);
            //compare tier first, then compare name
            return r1T<r2T? 1: r1T==r2T? pNC.compare(o1,o2) : -1;
        };
    }

    private int tierToInt(AbstractPotion.PotionRarity potionRarity) {
        AbstractPotion.PotionRarity[] rarities = AbstractPotion.PotionRarity.values();
        if (potionRarity != null) {
            for (int i = 0; i<rarities.length; i++) {
                if(rarities[i].equals(potionRarity)) return i;
            }
        }
        return -1;
    }
}
