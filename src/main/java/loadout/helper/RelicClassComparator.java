package loadout.helper;

import basemod.BaseMod;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RelicClassComparator implements Comparator<AbstractRelic> {


    public static final ArrayList<AbstractCard.CardColor> classList = new ArrayList<>();
    public static final ArrayList<HashMap<String,AbstractRelic>> customRelicPools = new ArrayList<>();


    //public static final RelicClassComparator INSTANCE = new RelicClassComparator();
    private RelicNameComparator rNC = RelicNameComparator.INSTANCE;

    public RelicClassComparator() {
        super();
        getClasses();
        createCustomRelicPool();
    }

    /**
     * Compare two relics by class
     * red<green<blue<purple<shared
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return -1,0,1 smaller,equal,larger
     */
    @Override
    public int compare(AbstractRelic o1, AbstractRelic o2) {
        int r1 = relicClassToInt(o1);
        int r2 = relicClassToInt(o2);
        return r1<r2? -1 : r1==r2? rNC.compare(o1,o2) : 1;
    }

    @Override
    public Comparator<AbstractRelic> reversed() {
        return (o1,o2) -> {
            int r1 = relicClassToInt(o1);
            int r2 = relicClassToInt(o2);
            return r1<r2? 1 : r1==r2? rNC.compare(o1,o2) : -1;
        };
    }

    public static int relicClassToInt(AbstractRelic a) {
        if (Loader.isModLoadedOrSideloaded("RelicUpgradeLib")||Loader.isModLoadedOrSideloaded("RelicUpgradeMOD_OPM")) {
            //if from relic upgrade find and use the original relic class
            //greater than 3 since "RU " must be followed by another relic id
            if(a.relicId.length() > 3 && a.relicId.substring(0,3).matches("RU ")) {
                String i1 = RelicNameComparator.editModRelicId(a.relicId);
                return relicClassToInt(RelicLibrary.getRelic(i1));
            }
        }

        if (RelicLibrary.redList.contains(a)) {
            return 0;
        } else if (RelicLibrary.greenList.contains(a)) {
            return 1;
        } else if (RelicLibrary.blueList.contains(a)) {
            return 2;
        } else if (RelicLibrary.whiteList.contains(a)) {
            return 3;
        } else {
            int retVal = 4;
            int numClasses = customRelicPools.size();
            for (int i = 0; i < numClasses; i++) {
                if (customRelicPools.get(i).containsKey(a.relicId))
                    return retVal + i;
            }
            //if not found, its shared
            return retVal + numClasses;
        }
    }

    private void getClasses() {
        classList.addAll(LoadoutMod.customRelics.keySet());
    }

    private void createCustomRelicPool() {
        for (AbstractCard.CardColor color : classList) {
            customRelicPools.add(LoadoutMod.customRelics.get(color));
        }
    }

    public static AbstractPlayer getCharacterByColor(AbstractCard.CardColor color) {
        for (AbstractPlayer player : LoadoutMod.allCharacters) {
            if (player.getCardColor().equals(color)) {
                return player;
            }
        }
        return null;
    }
    public static String getCharacterNameByColor(AbstractCard.CardColor color) {
        AbstractPlayer ap = getCharacterByColor(color);
        return ap == null? color.toString() : ap.getLocalizedCharacterName();
    }
    public static Color getRelicColor(AbstractRelic r) {
        int charClass = relicClassToInt(r);
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

    public static AbstractCard.CardColor getRelicCardColor(AbstractRelic ar) {
        for (AbstractCard.CardColor cc : LoadoutMod.allRelics.keySet()) {
            if (LoadoutMod.allRelics.get(cc).containsValue(ar)) return cc;
        }

        return AbstractCard.CardColor.COLORLESS;
    }

    public static AbstractCard.CardColor getRelicCardColor(String relidId) {
        for (AbstractCard.CardColor cc : LoadoutMod.allRelics.keySet()) {
            if (LoadoutMod.allRelics.get(cc).containsKey(relidId)) return cc;
        }

        return AbstractCard.CardColor.COLORLESS;
    }


}
