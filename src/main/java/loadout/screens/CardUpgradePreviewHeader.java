package loadout.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loadout.cardmods.*;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.ImageMaster;

import basemod.BaseMod;
import basemod.abstracts.AbstractCardModifier;
import basemod.cardmods.EtherealMod;
import basemod.cardmods.ExhaustMod;
import basemod.cardmods.InnateMod;
import basemod.cardmods.RetainMod;
import basemod.helpers.CardModifierManager;
import loadout.LoadoutMod;
import loadout.patches.AbstractCardPatch;

/**
 * Header that uses CardEffectButtons to tweak upgrade diffs.
 */
public class CardUpgradePreviewHeader implements HeaderButtonPlusListener, CardEffectButton.CardStuffProvider, ModifierButtonPlusListener {
    private static final float START_Y = Settings.HEIGHT - 180.0f * Settings.scale;
    private static final float SPACE_Y = 48.0f * Settings.scale;

    private static final String MODIFIER_MODE_TEXT = "Add/Remove Modifier on Upgrade";
    private static final String UPGRADE_VIEW_TEXT = "Back to Upgrade View";
    private static final float STAT_BUTTON_X = Settings.WIDTH / 2.0f - 180.0f * Settings.scale;
    //Minus this offset if in upgrade view
    private static final float UPGRADE_VIEW_X_OFFSET = 500.0f * Settings.scale;

    private final CardUpgradePreviewScreen screen;

    private final List<CardEffectButton> normalButtons = new ArrayList<>();
    private final List<CardEffectButton> additionalButtons = new ArrayList<>();
    private final List<ModifierButtonPlus> modifierButtons = new ArrayList<>();
    private final Map<ModifierButtonPlus, String> modifierButtonIds = new HashMap<>();

    private final HeaderButtonPlus modifierModeButton;
    private AbstractCard upgradedCardOriginalCopy;


    public CardUpgradePreviewHeader(CardUpgradePreviewScreen screen) {
        this.screen = screen;
        this.modifierModeButton = new HeaderButtonPlus(MODIFIER_MODE_TEXT, Settings.WIDTH / 2.0f, 200.0f * Settings.scale, this, true, ImageMaster.SETTINGS_ICON);
    }

    public void onOpen() {
        if (normalButtons.isEmpty()) {
            buildNormalButtons();
        }
        rebuildAdditionalButtons(screen.getUpgradedPreview());
        ensureModifierButtons();
        layoutModifierButtons();
        updateModifierModeButton();
    }

    private void buildNormalButtons() {
        normalButtons.clear();
        float x = STAT_BUTTON_X;
        float y = START_Y;
        addNormalRow(CardViewPopupHeader.clTEXT[3], 0, x, y);
        y -= SPACE_Y;
        addNormalRow(CardViewPopupHeader.TEXT[0], 1, x, y);
        y -= SPACE_Y;
        addNormalRow(StringUtils.capitalize(CardViewPopupHeader.TEXT_BLOCK), 2, x, y);
        y -= SPACE_Y;
        addNormalRow(CardViewPopupHeader.TEXT[1], 3, x, y);
        y -= SPACE_Y;
        addNormalRow("Misc", 4, x, y);
    }

    private void rebuildAdditionalButtons(AbstractCard card) {
        additionalButtons.clear();
        float y = START_Y - SPACE_Y * 5;

        for (String key : getAdditionalMagicKeys(card)) {
            addAdditionalMagicRow(key, STAT_BUTTON_X, y);
            y -= SPACE_Y;
        }
    }

    public void refreshModifierMode() {
        this.upgradedCardOriginalCopy = screen.getUpgradedPreview().makeStatEquivalentCopy();
        updateModifierModeButton();
        rebuildAdditionalButtons(screen.getUpgradedPreview());
        ensureModifierButtons();
        layoutModifierButtons();
        syncModifierButtonStates();
    }




    private void updateModifierModeButton() {
        String text = screen.modifierMode ? UPGRADE_VIEW_TEXT : MODIFIER_MODE_TEXT;
        modifierModeButton.text = text;
        modifierModeButton.textWidth = FontHelper.getSmartWidth(FontHelper.topPanelInfoFont, text, Float.MAX_VALUE, 0.0F);
        modifierModeButton.hb.resize(modifierModeButton.textWidth + 64.0F * Settings.scale, modifierModeButton.hb.height);
    }


    private void ensureModifierButtons() {
        if (!modifierButtons.isEmpty()) {
            return;
        }
//        addModifierButton(StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]), UnplayableMod.ID, new UnplayableMod());
        ModifierButtonPlus unplayableButton = new ModifierButtonPlus(StringUtils.capitalize(GameDictionary.UNPLAYABLE.NAMES[0]), 0f , 0f, this, new ModifierButtonPlus.CustomModifierLogics() {
            @Override
            public void customRemovalLogic() {
                CardModifierManager.removeModifiersById(getCard(), UnplayableMod.ID, true);
                CardModifierManager.addModifier(getCard(), new PlayableMod());
            }

            @Override
            public void customAddingLogic() {
                CardModifierManager.removeModifiersById(getCard(), PlayableMod.ID, true);
                CardModifierManager.addModifier(getCard(), new UnplayableMod());
            }

            @Override
            public boolean getStatus() {
                return getCard().cost == -2;
            }
        });
        modifierButtons.add(unplayableButton);
        modifierButtonIds.put(unplayableButton, UnplayableMod.ID);
//        addModifierButton(StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]), ExhaustMod.ID, new ExhaustMod());
        ModifierButtonPlus exhaustButton = new ModifierButtonPlus(StringUtils.capitalize(GameDictionary.EXHAUST.NAMES[0]), 0f , 0f, this, new ModifierButtonPlus.CustomModifierLogics() {
            @Override
            public void customRemovalLogic() {
                if(CardModifierManager.hasModifier(getCard(),ExhaustMod.ID)) {
                    CardModifierManager.removeModifiersById(getCard(), ExhaustMod.ID, true);
                } else {
                    CardModifierManager.addModifier(getCard(), new UnexhaustMod());
                }
            }

            @Override
            public void customAddingLogic() {
                if(CardModifierManager.hasModifier(getCard(),UnexhaustMod.ID)){
                    CardModifierManager.removeModifiersById(getCard(), UnexhaustMod.ID, true);
                } else {
                    CardModifierManager.addModifier(getCard(), new ExhaustMod());
                }
            }

            @Override
            public boolean getStatus() {
                return getCard().exhaust;
            }
        });
        modifierButtons.add(exhaustButton);
        modifierButtonIds.put(exhaustButton, ExhaustMod.ID);
//        addModifierButton(StringUtils.capitalize(GameDictionary.ETHEREAL.NAMES[0]), EtherealMod.ID, new EtherealMod());
        ModifierButtonPlus etherealButton = new ModifierButtonPlus(StringUtils.capitalize(GameDictionary.ETHEREAL.NAMES[0]), 0f , 0f, this, new ModifierButtonPlus.CustomModifierLogics() {
            @Override
            public void customRemovalLogic() {
                CardModifierManager.removeModifiersById(getCard(), EtherealMod.ID, true);
                CardModifierManager.addModifier(getCard(), new UnetherealMod());
            }

            @Override
            public void customAddingLogic() {
                CardModifierManager.removeModifiersById(getCard(), UnetherealMod.ID, true);
                CardModifierManager.addModifier(getCard(), new EtherealMod());
            }

            @Override
            public boolean getStatus() {
                return getCard().isEthereal;
            }
        });
        modifierButtons.add(etherealButton);
        modifierButtonIds.put(etherealButton, EtherealMod.ID);

        //addModifierButton(StringUtils.capitalize(GameDictionary.INNATE.NAMES[0]), InnateMod.ID, new InnateMod());
        ModifierButtonPlus innateButton = new ModifierButtonPlus(StringUtils.capitalize(GameDictionary.INNATE.NAMES[0]), 0f , 0f, this, new ModifierButtonPlus.CustomModifierLogics() {
            @Override
            public void customRemovalLogic() {
                CardModifierManager.removeModifiersById(getCard(), InnateMod.ID, true);
            }

            @Override
            public void customAddingLogic() {
                CardModifierManager.addModifier(getCard(), new InnateMod());
            }

            @Override
            public boolean getStatus() {
                return getCard().isInnate;
            }
        });
        modifierButtons.add(innateButton);
        modifierButtonIds.put(innateButton, InnateMod.ID);
        //addModifierButton(StringUtils.capitalize(GameDictionary.RETAIN.NAMES[0]), RetainMod.ID, new RetainMod());
        ModifierButtonPlus retainButton = new ModifierButtonPlus(StringUtils.capitalize(GameDictionary.RETAIN.NAMES[0]), 0f , 0f, this, new ModifierButtonPlus.CustomModifierLogics() {
            @Override
            public void customRemovalLogic() {
                CardModifierManager.removeModifiersById(getCard(), RetainMod.ID, true);
            }

            @Override
            public void customAddingLogic() {
                CardModifierManager.addModifier(getCard(), new RetainMod());
            }

            @Override
            public boolean getStatus() {
                return getCard().retain;
            }
        });
        modifierButtons.add(retainButton);
        modifierButtonIds.put(retainButton, RetainMod.ID);
        addModifierButton("X " + CardViewPopupHeader.clTEXT[3], XCostMod.ID, new XCostMod());
        addModifierButton(BaseMod.getKeywordTitle("autoplay"), AutoplayMod.ID, new AutoplayMod());
        addModifierButton(BaseMod.getKeywordTitle("soulbound"), SoulboundMod.ID, new SoulboundMod());
        addModifierButton(BaseMod.getKeywordTitle("fleeting"), FleetingMod.ID, new FleetingMod());
        addModifierButton(BaseMod.getKeywordTitle("grave"), GraveMod.ID, new GraveMod());
        addModifierButton(CardViewPopupHeader.TEXT[11], GainGoldOnKillMod.ID, new GainGoldOnKillMod());
        addModifierButton(CardViewPopupHeader.TEXT[12], GainHpOnKillMod.ID, new GainHpOnKillMod());
        addModifierButton(CardViewPopupHeader.TEXT[13], GainGoldOnPlayMod.ID, new GainGoldOnPlayMod());
        addModifierButton(CardViewPopupHeader.TEXT[14], HealOnPlayMod.ID, new HealOnPlayMod());
        addModifierButton(CardViewPopupHeader.TEXT[15], RandomUpgradeOnKillMod.ID, new RandomUpgradeOnKillMod());
        addModifierButton(CardViewPopupHeader.TEXT[16], GainDamageOnKill.ID, new GainDamageOnKill());
        addModifierButton(CardViewPopupHeader.TEXT[17], GainMagicOnKillMod.ID, new GainMagicOnKillMod());
        addModifierButton(CardViewPopupHeader.TEXT[19], LifestealMod.ID, new LifestealMod());
        addModifierButton(CardViewPopupHeader.TEXT[20], InevitableMod.ID, new InevitableMod());
        addModifierButton(CardViewPopupHeader.TEXT[22], InfiniteUpgradeMod.ID, new InfiniteUpgradeMod());
        addModifierButton(DieNextTurnMod.description.replace("NL", "").trim(), DieNextTurnMod.ID, new DieNextTurnMod());
        addModifierButton(CardViewPopupHeader.TEXT[29], StickyMod.ID, new StickyMod());
        addModifierButton(CardViewPopupHeader.TEXT[0], DamageMod.ID, new DamageMod());
        addModifierButton(CardViewPopupHeader.TEXT[33], DamageAOEMod.ID, new DamageAOEMod());
        addModifierButton(StringUtils.capitalize(CardViewPopupHeader.TEXT_BLOCK), BlockMod.ID, new BlockMod());
        addModifierButton(CardViewPopupHeader.TEXT[3], DrawMod.ID, new DrawMod());
        addModifierButton(CardViewPopupHeader.TEXT_DISCARD, DiscardMod.ID, new DiscardMod());
        addModifierButton(CardViewPopupHeader.TEXT[34], ExhaustCardMod.ID, new ExhaustCardMod());
    }

    private void addModifierButton(String label, String modifierId, AbstractCardModifier modifier) {
        ModifierButtonPlus button = new ModifierButtonPlus(label, 0.0f, 0.0f, this, modifier);
        modifierButtons.add(button);
        modifierButtonIds.put(button, modifierId);
    }

    private void layoutModifierButtons() {
        float startX = Settings.WIDTH / 2.0f + UPGRADE_VIEW_X_OFFSET;
        float colSpacing = 240.0f * Settings.scale;
        int index = 0;
        for (HeaderButtonPlus button : modifierButtons) {
            int col = index % 2;
            int row = index / 2;
            button.x = startX + col * colSpacing;
            button.y = START_Y - row * SPACE_Y;
            index++;
        }
    }

    private void syncModifierButtonStates() {
        for (ModifierButtonPlus button : modifierButtons) {
            button.updateStatus();
        }
    }

    private Set<String> getAdditionalUpgradeModifierIds() {
        String[] mods = AbstractCardPatch.getCardAdditionalModifiers(screen.getSourceCard());
        Set<String> ids = new HashSet<>();
        if (mods != null) {
            ids.addAll(Arrays.asList(mods));
        }
        return ids;
    }

    private void addNormalRow(String label, int index, float x, float y) {
        CardEffectButton ceb = new CardEffectButton(null, x, y, label, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                Integer[] diffs = screen.getNormalUpgradeDiffs();
                if (diffs == null || diffs.length <= index || diffs[index] == null) return 0;
                return diffs[index];
            }

            @Override
            public void setAmount(int amountToSet) {
                screen.setNormalUpgrade(index, amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                // not used for numeric diff
            }
        }, this);
        normalButtons.add(ceb);
    }

    private void addAdditionalMagicRow(String key, float x, float y) {
        CardEffectButton ceb = new CardEffectButton(null, x, y, key, new StatModSelectScreen.StatModActions() {
            @Override
            public int getAmount() {
                return screen.getAdditionalMagicUpgradeDiffs(key);
            }

            @Override
            public void setAmount(int amountToSet) {
                screen.setAdditionalMagicUpgrade(key, amountToSet);
            }

            @Override
            public void onBoolChange(boolean boolToChange, int amount) {
                // not used for numeric diff
            }
        }, this);
        additionalButtons.add(ceb);
    }

    public void update() {
        modifierModeButton.update();
        if (!screen.modifierMode) {
            for (CardEffectButton ceb : normalButtons) {
                ceb.update();
            }
            for (CardEffectButton ceb : additionalButtons) {
                ceb.update();
            }
        }

        if (screen.modifierMode) {
            for (HeaderButtonPlus button : modifierButtons) {
                button.update();
            }
        }
    }

    public void render(SpriteBatch sb) {
        float titleY = START_Y + 60.0f * Settings.scale;
        FontHelper.renderSmartText(sb, FontHelper.cardTitleFont, CardViewPopupHeader.TEXT[38], STAT_BUTTON_X, titleY, Color.GOLD);

        if (!screen.modifierMode) {
            for (CardEffectButton ceb : normalButtons) {
                ceb.render(sb);
            }
            for (CardEffectButton ceb : additionalButtons) {
                ceb.render(sb);
            }
        }

        modifierModeButton.render(sb);
        if (screen.modifierMode) {
            for (HeaderButtonPlus button : modifierButtons) {
                button.render(sb);
            }
        }
    }

    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == modifierModeButton) {
            screen.modifierMode = !screen.modifierMode;

            refreshModifierMode();
            screen.refreshPreview();
            return;
        }
//        String modifierId = modifierButtonIds.get(button);
//        if (modifierId != null) {
//            screen.setAdditionalUpgradeModifier(modifierId, isAscending);
//        }
//        System.out.println("Presetting modifier array for source card is: " + Arrays.toString(AbstractCardPatch.CardModificationFields.additionalModifiers.get(screen.getSourceCard())));
        AbstractCardPatch.mergeCardAdditionalModifiers(screen.getSourceCard(), getDifferences(this.upgradedCardOriginalCopy, screen.getUpgradedPreview()));
//        LoadoutMod.logger.info("Modifier set!, array is: {}", Arrays.toString(getDifferences(this.upgradedCardOriginalCopy, screen.getUpgradedPreview())));
//        System.out.println("Modifier set!, diff array is: " + Arrays.toString(getDifferences(this.upgradedCardOriginalCopy, screen.getUpgradedPreview())));
//        System.out.println("Modifier array for source card is: " + Arrays.toString(AbstractCardPatch.CardModificationFields.additionalModifiers.get(screen.getSourceCard())));
        screen.refreshPreview();
//        syncModifierButtonStates();
        refreshModifierMode();
    }


    public void resetOtherButtons() {
        // no-op
    }


    public void clearActiveButtons() {
        // no-op
    }

    /**
     * We can get the multiplier from LoadoutMod directly.
     * DO NOT CHANGE!
     */
    @Override
    public int getMultiplier() {
        return LoadoutMod.universalMultiplier;
    }

    @Override
    public AbstractCard getCard() {
        return screen.modifierMode ? screen.getUpgradedPreview() : screen.getSourceCard();
    }

    private Set<String> getAdditionalMagicKeys(AbstractCard card) {
        return AbstractCardPatch.CardModificationFields.additionalMagicNumbers.get(card).keySet();
    }

    /**
     * Returns the differences between the base and upgraded card. Format: '+' means the upgrade has this modifier and the original does not, '-' means the original has this modifier and the upgrade does not
     * Do need to heed the special cases for original game modifiers like "exhaust" etc.
     * @param base
     * @param upgraded
     * @return a string array of differences using the format described above, i.e.["+loadout:modifier1", "-loadout:modifier2"]
     */
    public static String[] getDifferences(AbstractCard base, AbstractCard upgraded) {
        ArrayList<AbstractCardModifier> baseModifiers = CardModifierManager.modifiers(base);
        ArrayList<AbstractCardModifier> upgradedModifiers = CardModifierManager.modifiers(upgraded);
        Set<String> baseIds = new HashSet<>();
        Set<String> upgradedIds = new HashSet<>();
        for (AbstractCardModifier mod : baseModifiers) {
            baseIds.add(mod.identifier(null));
        }
        for (AbstractCardModifier mod : upgradedModifiers) {
            upgradedIds.add(mod.identifier(null));
        }

        Set<String> added = new HashSet<>(upgradedIds);
        added.removeAll(baseIds);

        Set<String> removed = new HashSet<>(baseIds);
        removed.removeAll(upgradedIds);

        String[] result = new String[added.size() + removed.size()];
        int i = 0;
        for (String s : added) {
            result[i++] = "+" + s;
        }
        for (String s : removed) {
            result[i++] = "-" + s;
        }

        return result;
    }
}