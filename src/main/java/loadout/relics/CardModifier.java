package loadout.relics;

import basemod.ReflectionHacks;
import basemod.abstracts.CustomSavable;
import basemod.helpers.CardModifierManager;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.BranchingUpgradesCard;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.SingleCardViewPopup;
import com.megacrit.cardcrawl.vfx.UpgradeShineEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import loadout.LoadoutMod;
import loadout.cardmods.InfiniteUpgradeMod;
import loadout.patches.AbstractCardPatch;
import loadout.patches.InfUpgradePatch;
import loadout.savables.CardModifications;
import loadout.savables.CustomSaver;
import loadout.savables.SerializableCardLite;
import loadout.screens.GCardSelectScreen;
import loadout.util.TextureLoader;
import rs.lazymankits.interfaces.cards.BranchableUpgradeCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static loadout.LoadoutMod.*;

public class CardModifier extends AbstractCardScreenRelic implements CustomSavable<Object[][]> {

    public static final String ID = LoadoutMod.makeID("CardModifier");
    public static Texture IMG = null;
    private static Texture OUTLINE = null;

    private static final CustomSaver<Integer, String[]> nameDescSaves;

    static {
        try {
            nameDescSaves = new CustomSaver<>("CustomNamesNDesc");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private HashMap<Integer, String[]> nameDescMap;

    public CardModifier() {
        super(ID, IMG, OUTLINE, AbstractRelic.LandingSound.CLINK, GCardSelectScreen.CardDisplayMode.UPGRADE);
        nameDescMap = new HashMap<>();
    }

    @Override
    protected void openSelectScreen() {
        if (AbstractDungeon.player.masterDeck != null) {
            if (selectScreen != null)
                selectScreen.open(AbstractDungeon.player.masterDeck, LoadoutMod.cardsToDisplay.size(),DESCRIPTIONS[1],false,false,true,false);
        } else setIsSelectionScreenUp(false);
    }

    @Override
    protected void doneSelectionLogics() {
        int count = selectScreen.selectedCards.size();
        float min = -15.0F * count;
        for (int i = 0; i < count; i++) {
            AbstractCard card = selectScreen.selectedCards.get(i);
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                upgradeCard(card);
            } else
                upgradeCard(card,Settings.WIDTH / 2.0F + (min + i * 30.0F) * Settings.scale - AbstractCard.IMG_WIDTH / 2.0f, Settings.HEIGHT / 2.0F);
        }
    }


    public static void upgradeCard(AbstractCard card, float x, float y) {
        card.upgrade();

        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
            card.superFlash();
        } else {
            AbstractDungeon.topLevelEffects.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy(), x, y));
            AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(x, y));
            CardCrawlGame.sound.play("CARD_UPGRADE");
        }

    }

    public static void upgradeCard(AbstractCard card) {
        upgradeCard(card,Settings.WIDTH / 2.0F,Settings.HEIGHT / 2.0F);
    }

    public static AbstractCard branchUpgradeCard(AbstractCard card, int branch, boolean upgradeEffects) {
        if(card instanceof BranchingUpgradesCard) {
            if (branch == 1) {
                ((BranchingUpgradesCard) card).doBranchUpgrade();
            } else {
                ((BranchingUpgradesCard) card).doNormalUpgrade();
            }
        } else if (Loader.isModLoaded("LazyManKits") && card instanceof BranchableUpgradeCard) {
            BranchableUpgradeCard b = ((BranchableUpgradeCard)card);
            b.setChosenBranch(branch);
            b.upgradeCalledOnSL();
        } else {
            logger.warn("{} is not a branch upgrade card!", card.cardID);
        }

        if(upgradeEffects) {
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                card.superFlash();
            } else {
                AbstractDungeon.topLevelEffects.add(new ShowCardBrieflyEffect(card.makeStatEquivalentCopy(), Settings.WIDTH / 2.0F,Settings.HEIGHT / 2.0F));
                AbstractDungeon.topLevelEffects.add(new UpgradeShineEffect(Settings.WIDTH / 2.0F,Settings.HEIGHT / 2.0F));
                CardCrawlGame.sound.play("CARD_UPGRADE");
            }

        }
        return card;
    }


    @Override
    public Object[][] onSave() {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            ArrayList<Object[]> ret = new ArrayList<>();
            nameDescMap.clear();

            for (int i = 0; i<len; i++) {
                AbstractCard card = AbstractDungeon.player.masterDeck.group.get(i);
                if (AbstractCardPatch.isCardModified(card)) {
                    AbstractCard unmoddedCopy = getUnmodifiedCopyCard(card.cardID);
                    Object[] cardStat = new Object[15];
                    cardStat[0] = i;
                    cardStat[1] = card.cost;
                    cardStat[2] = card.baseDamage;
                    cardStat[3] = card.baseBlock;
                    cardStat[4] = card.baseMagicNumber;
                    String magicNumberData = AbstractCardPatch.serializeAdditionalMagicNumbers(card);
                    cardStat[5] = magicNumberData.isEmpty() ? "0" : magicNumberData;
                    cardStat[6] = card.baseDraw;
                    cardStat[7] = card.baseDiscard;
                    cardStat[8] = card.color.toString();
                    cardStat[9] = card.type.toString();
                    cardStat[10] = card.rarity.toString();
                    cardStat[11] = card.misc;
                    cardStat[12] = card.timesUpgraded;
                    if (unmoddedCopy != null) {
                        String[] nd = new String[2];
                        nd[0] = card.originalName.equals(unmoddedCopy.originalName) ? null : card.originalName;
                        nd[1] = card.rawDescription.equals(unmoddedCopy.rawDescription) ? null : card.rawDescription;
                        nameDescMap.put(i, nd);
                    }


                    ret.add(cardStat);
                }

            }
            try {
                nameDescSaves.save(nameDescMap);
            } catch (IOException e) {
                logger.info("Failed to save custom name and desc!");
            }
            return ret.toArray(new Object[0][]);
        }


        return null;
    }

    @Override
    public void onLoad(Object[][] ret) {
        if (AbstractDungeon.player != null && AbstractDungeon.player.masterDeck != null) {
            int len = AbstractDungeon.player.masterDeck.group.size();
            if (ret != null && ret.length <= len && ret.length > 0 ) {
                try {
                    HashMap<Integer, String[]> nd = (HashMap<Integer, String[]>) nameDescSaves.load();
                    if(nd == null) nameDescMap.clear();
                    else nameDescMap = nd;
                } catch (IOException e) {
                    logger.info("Error loading custom names and desc");
                    e.printStackTrace();
                    nameDescMap.clear();
                }

                if(ret[0].length >= 13) {
                    for (int i = 0; i<ret.length; i++) {
                        AbstractCard card = AbstractDungeon.player.masterDeck.group.get((int)(double)ret[i][0]);
                        for (int j = card.timesUpgraded; j < (int)(double)ret[i][12]; j++) {card.upgrade();}
                        card.cost = (int)(double)ret[i][1];
                        card.costForTurn = card.cost;
                        card.baseDamage = (int)(double)ret[i][2];
                        card.baseBlock = (int)(double)ret[i][3];
                        card.baseMagicNumber = (int)(double)ret[i][4];
                        card.magicNumber = card.baseMagicNumber;
                        try {
                            //check
                            String data = (String) ret[i][5];
                            //logger.info("Now loading magic number array: {}", data);
                            if (!data.isEmpty()) {
                                if(data.contains(AbstractCardPatch.MAGIC_NUMBER_DELIMITER)) {
                                    AbstractCardPatch.deserializeAdditionalMagicNumbers(card, data);
                                } else
                                    card.baseHeal = Integer.parseInt(data);
                            } else
                                logger.warn("Received empty string while parsing additional magic numbers for card: {}", card.cardID);

                        } catch (Exception e) {
                            logger.warn("Failed to get magic number array! received: {}", ret[i][5]);
                            e.printStackTrace();
                        }

                        card.baseDraw = (int)(double)ret[i][6];
                        card.baseDiscard = (int)(double)ret[i][7];
                        try {
                            card.color = AbstractCard.CardColor.valueOf((String) ret[i][8]);
                        } catch (Exception e) {
                            try {
                                card.color = AbstractCard.CardColor.values()[(int)(double)ret[i][8]];
                            } catch (Exception ignore) {

                            }

                        }

                        try {
                            card.type = AbstractCard.CardType.valueOf((String) ret[i][9]);
                        } catch (Exception e) {
                            try {
                                card.type = AbstractCard.CardType.values()[(int)(double)ret[i][9]];
                            } catch (Exception ignore) {

                            }

                        }

                        try {
                            card.rarity = AbstractCard.CardRarity.valueOf((String) ret[i][10]);
                        } catch (Exception e) {
                            try {
                                card.rarity = AbstractCard.CardRarity.values()[(int)(double)ret[i][10]];
                            } catch (Exception ignore) {

                            }
                        }

                        card.misc = (int)(double)ret[i][11];
                        AbstractCardPatch.setCardModified(card,true);

//                        if() {
//                            if(ret[i][13] != null) {
//                                card.originalName = (String) ret[i][13];
//                                card.name = getUpgradedName(card);
//                                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
//                            }
//                            if(ret[i][14] != null) {
//                                card.rawDescription = (String) ret[i][14];
//                                card.initializeDescription();
//                            }
//                        }

                        if(CardModifierManager.hasModifier(card, InfiniteUpgradeMod.ID)) {
                            card.upgraded = false;
                            card.timesUpgraded = (int)(double)ret[i][12];
                            InfUpgradePatch.changeCardName(card);
                        }
                    }
                }
                if(nameDescMap != null && !nameDescMap.isEmpty()) {
                    for (Map.Entry<Integer, String[]> e: nameDescMap.entrySet()) {
                        AbstractCard card = AbstractDungeon.player.masterDeck.group.get(e.getKey());
                        String[] nd = e.getValue();
                        if(nd[0] != null) {
                                card.originalName = (String) nd[0];
                                card.name = getUpgradedName(card);
                                ReflectionHacks.privateMethod(AbstractCard.class, "initializeTitle").invoke(card);
                            }
                            if(nd[1] != null) {
                                card.rawDescription = (String) nd[1];
                                card.initializeDescription();
                            }
                    }
                }
            }
        }

    }

    public static String getUpgradedName(AbstractCard card) {
        return card.originalName + (card.timesUpgraded > 0 ? "+" + (card.timesUpgraded == 1 ? "" : card.timesUpgraded) : "");
    }

    public static AbstractCard getUnmodifiedCopyCard(String id) {
        try {
            return CardLibrary.getCard(id).getClass().getDeclaredConstructor(new Class[0]).newInstance();
        } catch (Exception e) {
            logger.info("Error obtaining a new copy for card: " + id);
        }
        return null;
    }

    public void onPreviewObtainCard(AbstractCard c) {
        onObtainCard(c);
    }

    public void onObtainCard(AbstractCard c) {
//        if(CardModifications.cardMap != null && CardModifications.cardMap.containsKey(c.cardID)) {
//            try {
//                CardModifications.modifyOnlyNumberIfExist(c);
//            } catch (Exception e) {
//                LoadoutMod.logger.info("Failed to modify: " + c.cardID + " when obtaining");
//            }
//        }
    }

}
