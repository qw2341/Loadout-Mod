package loadout.helper;

import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.Comparator;

public class RelicTierComparator implements Comparator<AbstractRelic> {

    private final RelicNameComparator rNC = RelicNameComparator.INSTANCE;
    public static final RelicTierComparator INSTANCE = new RelicTierComparator();

    public RelicTierComparator() {
        super();
    }
    @Override
    public int compare(AbstractRelic r1, AbstractRelic r2) {
        int r1T = tierToInt(r1.tier);
        int r2T = tierToInt(r2.tier);
        //compare tier first, then compare name
        return r1T<r2T? -1: r1T==r2T? rNC.compare(r1,r2) : 1;
    }

    @Override
    public Comparator<AbstractRelic> reversed() {
        return (o1,o2) -> {
            int r1T = tierToInt(o1.tier);
            int r2T = tierToInt(o2.tier);
            //compare tier first, then compare name
            return r1T<r2T? 1: r1T==r2T? rNC.compare(o1,o2) : -1;
        };
    }

    private int tierToInt(AbstractRelic.RelicTier relicTier) {
        if (relicTier != null) {
            switch (relicTier) {
                case STARTER:
                    return 0;
                case COMMON:
                    return 1;
                case UNCOMMON:
                    return 2;
                case RARE:
                    return 3;
                case BOSS:
                    return 4;
                case SHOP:
                    return 5;
                case SPECIAL:
                    return 6;
                case DEPRECATED:
                    return 7;
                default:
                    return 6;
            }
        }
        return -1;
    }
}
