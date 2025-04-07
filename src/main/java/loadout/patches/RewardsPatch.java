package loadout.patches;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

import io.chaofan.sts.chaofanmod.utils.ChaofanModEnums;
import loadout.relics.TildeKey;

@SpirePatch(clz = CombatRewardScreen.class, method = "setupItemReward")
public class RewardsPatch {
    @SpirePostfixPatch
    public static void Postfix(CombatRewardScreen __instance) {
        if(__instance.rewards != null && TildeKey.rewardMultiplier != 1) {
            ArrayList<RewardItem> rewardItems = new ArrayList<>();
            RewardItem relicLinkee = null;
            for(RewardItem ri : __instance.rewards) {
                RewardItem tempReward;

                switch (ri.type) {

                    case CARD:
                        for(int j = 0; j< TildeKey.rewardMultiplier; j++) {
                            if(TildeKey.isRewardDuped) {
                                tempReward = new RewardItem(0);
                                tempReward.type = RewardItem.RewardType.CARD;
                                tempReward.text = RewardItem.TEXT[2];
                                tempReward.cards = ri.cards.stream().map(AbstractCard::makeStatEquivalentCopy).collect(Collectors.toCollection(ArrayList::new));
                            } else {
                                if(j == 0) {
                                    tempReward = ri;

                                }
                                else tempReward = new RewardItem();
                            }

                            rewardItems.add(tempReward);
                        }
                        break;
                    case GOLD:
                        for(int j = 0; j< TildeKey.rewardMultiplier; j++) {
                            rewardItems.add(new RewardItem(ri.goldAmt));
                        }
                        break;
                    case RELIC:
                        for(int j = 0; j< TildeKey.rewardMultiplier; j++) {
                            if(TildeKey.isRewardDuped) {
                                tempReward = new RewardItem(ri.relic.makeCopy());

                            }
                            else {
                                if(j==0) {
                                    ri.relicLink = null;
                                    tempReward = ri;
                                }
                                else tempReward = new RewardItem(AbstractDungeon.returnRandomRelic(AbstractDungeon.returnRandomRelicTier()));
                            }
                            relicLinkee = tempReward;
                            rewardItems.add(tempReward);
                        }
                        break;
                    case POTION:
                        for(int j = 0; j< TildeKey.rewardMultiplier; j++) {
                            if(TildeKey.isRewardDuped) tempReward = new RewardItem(ri.potion.makeCopy());
                            else {
                                if(j==0) tempReward = ri;
                                else tempReward = new RewardItem(AbstractDungeon.returnRandomPotion());
                            }
                            rewardItems.add(tempReward);
                        }
                        break;

                    case SAPPHIRE_KEY:
                        if(relicLinkee != null) {
                            ri.relicLink = relicLinkee;
                            relicLinkee.relicLink = ri;
                        }
                        rewardItems.add(ri);
                        break;
                    case STOLEN_GOLD:
                    case EMERALD_KEY:
                    default:
                        if(Loader.isModLoaded("chaofanmod")) {
                            if(ri.type == ChaofanModEnums.CHAOFAN_MOD_RUBY_KEY) {
                                if(relicLinkee != null) {
                                    ri.relicLink = relicLinkee;
                                    relicLinkee.relicLink = ri;
                                }
                            }
                        }
                        rewardItems.add(ri);
                        break;
                }


            }


            __instance.rewards = rewardItems;
            __instance.positionRewards();
        }
    }
}
