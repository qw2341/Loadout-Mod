package loadout.helper;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import loadout.LoadoutMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PotionClassComparator implements Comparator<AbstractPotion> {


    public static final ArrayList<AbstractPlayer.PlayerClass> classList = new ArrayList<>();
    private static final ArrayList<ArrayList<String>> customPotionPools = new ArrayList<>();
    public static ArrayList<String> sharedList;
    public static ArrayList<String> redList;
    private PotionNameComparator pNC;

    public PotionClassComparator() {
        super();
        getClasses();
        createPotionPools();
        pNC = new PotionNameComparator();
    }

    /**
     * Compare two relics by class
     * red<green<blue<purple<shared
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return -1,0,1 smaller,equal,larger
     */
    @Override
    public int compare(AbstractPotion o1, AbstractPotion o2) {
        int r1 = potionClassToInt(o1);
        int r2 = potionClassToInt(o2);
        return r1<r2? -1 : r1==r2? pNC.compare(o1,o2) : 1;
    }

    @Override
    public Comparator<AbstractPotion> reversed() {
        return (o1,o2) -> {
            int r1 = potionClassToInt(o1);
            int r2 = potionClassToInt(o2);
            return r1<r2? 1 : r1==r2? pNC.compare(o1,o2) : -1;
        };
    }

    public static int potionClassToInt(AbstractPotion a) {
            int retVal = 0;
            int numClasses = customPotionPools.size();

            if(sharedList.contains(a.ID)) return retVal + numClasses;

            for (int i = 0; i < numClasses; i++) {
                if (customPotionPools.get(i).contains(a.ID))
                    return retVal + i;
            }
            //if not found, its shared
            return retVal + numClasses;
    }

    public static AbstractCard.CardColor getPotionClass(AbstractPotion ap) {

        for (int i = 0; i < customPotionPools.size(); i++) {
            if (customPotionPools.get(i).contains(ap.ID))
                return LoadoutMod.allCharacters.get(i).getCardColor();
        }

        return AbstractCard.CardColor.COLORLESS;
    }

    private void getClasses() {
        LoadoutMod.allCharacters.forEach(aP -> classList.add(aP.chosenClass));
    }

    private void createPotionPools() {
        for (AbstractPlayer.PlayerClass pC : classList) {
            customPotionPools.add(PotionHelper.getPotions(pC,false));
        }
        ArrayList<String> redList = customPotionPools.get(0);
        ArrayList<String> greenList = customPotionPools.get(1);
        sharedList = redList.stream().filter(greenList::contains).collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i< customPotionPools.size(); i++) {
            customPotionPools.get(i).removeAll(sharedList);
        }
    }

    public static AbstractPlayer getCharacterByClass(AbstractPlayer.PlayerClass c) {
        for (AbstractPlayer player : LoadoutMod.allCharacters) {
            if (player.chosenClass.equals(c)) {
                return player;
            }
        }
        return null;
    }

    public static Color getPotionColor(AbstractPotion r) {
        int charClass = potionClassToInt(r);
        ArrayList<AbstractPlayer> allChars = LoadoutMod.allCharacters;
        switch (charClass) {
            case 0:
                return new Color(-10132568);
            case 1:
                return new Color(2147418280);
            case 2:
                return new Color(-2016482392);
            case 3:
                return Color.PURPLE;
            default:
                break;
        }
        //if shared, return black
        return (charClass >= allChars.size()) ? Color.BLACK : allChars.get(charClass).getCardRenderColor();
    }
}
