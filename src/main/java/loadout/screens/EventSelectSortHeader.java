package loadout.screens;


import basemod.BaseMod;
import basemod.DevConsole;
import basemod.ReflectionHacks;
import basemod.devcommands.act.ActCommand;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.*;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.neow.NeowRoom;
import com.megacrit.cardcrawl.rewards.chests.*;
import com.megacrit.cardcrawl.rooms.*;
import com.megacrit.cardcrawl.screens.DungeonTransitionScreen;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.vfx.combat.BattleStartEffect;
import loadout.LoadoutMod;
import loadout.relics.EventfulCompass;
import loadout.rooms.CustomChestRoom;


import java.util.*;

public class EventSelectSortHeader extends AbstractSortHeader {

    private static final UIStrings rUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("RelicSelectSortHeader"));
    public static final String[] rTEXT = rUiStrings.TEXT;

    private static final UIStrings cUiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("CardSelectSortHeader"));
    public static final String[] cTEXT = cUiStrings.TEXT;

    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(LoadoutMod.makeID("EventSelectSortHeader"));
    public static final String[] TEXT = uiStrings.TEXT;


    public boolean justSorted = false;


    private HeaderButtonPlus modButton;
    private HeaderButtonPlus nameButton;
    private HeaderButtonPlus actButton;

    private DropdownMenu modNameDropdown;

    private DropdownMenu actSelectDropdown;

    private DropdownMenu chestSelectDropdown;
    private HeaderButtonPlus chestButton;

    private HeaderButtonPlus shopButton;

    private HeaderButtonPlus campButton;

    private HeaderButtonPlus neowButton;
    private HeaderButtonPlus bossButton;
    private ArrayList<String> eventMods;
    private HashMap<String,String> eventModNames;

    private HashMap<String, Integer> acts;
    private HashMap<String, String> actNames;

    private String currentActSelection = "";

    private int currentChestTypeSelection = 0;


    public EventSelectSortHeader(EventSelectScreen eventSelectScreen, float startX) {
        super(eventSelectScreen);

        this.startX = startX;
        float xPosition = this.startX - 75.0f * Settings.scale;
        float yPosition = START_Y - 450.0f * Settings.yScale;


        this.nameButton = new HeaderButtonPlus(rTEXT[2], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);
        yPosition -= SPACE_Y;
        this.modButton = new HeaderButtonPlus(rTEXT[3], xPosition, yPosition, this, true ,false, HeaderButtonPlus.Alignment.RIGHT);


        xPosition = 50.0F;
        yPosition = START_Y - 5 * 52.0f * Settings.yScale;
        this.actButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, true , ImageMaster.MAP_ICON);
        this.actButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPosition -= 3 * 52.0f * Settings.yScale;
        this.chestButton = new HeaderButtonPlus(TEXT[1], xPosition, yPosition, this, true , ImageMaster.MAP_NODE_TREASURE);
        this.chestButton.alignment = HeaderButtonPlus.Alignment.LEFT;

        xPosition = 20.0f * Settings.scale;
        yPosition -= SPACE_Y;
        this.shopButton = new HeaderButtonPlus(TEXT[2], xPosition, yPosition, this, true , ImageMaster.MAP_NODE_MERCHANT);
        this.shopButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPosition -= SPACE_Y;
        this.campButton = new HeaderButtonPlus(TEXT[4], xPosition, yPosition, this, true , ImageMaster.MAP_NODE_REST);
        this.campButton.alignment = HeaderButtonPlus.Alignment.LEFT;
        yPosition -= SPACE_Y;
        this.neowButton = new HeaderButtonPlus(TEXT[9], xPosition, yPosition, this, true , ImageMaster.TP_FLOOR);
        this.neowButton.alignment = HeaderButtonPlus.Alignment.LEFT;



        eventMods = new ArrayList<>(findEventAddingMods());
        eventMods.remove("Slay the Spire");
        eventMods.sort(Comparator.naturalOrder());

        ArrayList<String> f = new ArrayList<>();
        String fName;
        for (String id : eventMods) {
            fName = eventModNames.get(id);
            if (fName.length() >= 12) fName = fName.substring(0,11) + "...";
            f.add(fName);
        }



        f.add(0, cTEXT[0]);
        f.add("Slay the Spire");
        this.modNameDropdown = new DropdownMenu(this, f,FontHelper.panelNameFont, Settings.CREAM_COLOR);

        acts = ReflectionHacks.getPrivateStatic(ActCommand.class, "acts");

        actNames = new HashMap<>();
        actNames.put(Exordium.ID, Exordium.NAME);
        actNames.put(TheCity.ID, TheCity.NAME);
        actNames.put(TheBeyond.ID, TheBeyond.NAME);
        actNames.put(TheEnding.ID, TheEnding.NAME);

        if(Loader.isModLoaded("actlikeit")) {
            try {
                Class<?> customDungeon = Class.forName("actlikeit.dungeons.CustomDungeon");
                Map<Integer, ArrayList<String>> actnumbers = new HashMap<>();
                Map<String, AbstractDungeon> dungeons = new HashMap<>();
                actnumbers = (Map<Integer, ArrayList<String>>) customDungeon.getDeclaredField("actnumbers").get(null);
                dungeons = (Map<String, AbstractDungeon>) customDungeon.getDeclaredField("dungeons").get(null);

                for (Integer i : actnumbers.keySet()) {
                    ArrayList<String> actsAtNumI = actnumbers.get(i);
                    for (String actId: actsAtNumI) {
                        acts.put(actId,i);
                        actNames.put(actId, actId);
                    }

                }

            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to import ActLikeIt dungeons");
                e.printStackTrace();
            }

        }
        ArrayList<String> arr = new ArrayList<>(actNames.values());

        this.actSelectDropdown = new DropdownMenu(this, arr, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        String[] chestTypes = new String[] {TEXT[5],TEXT[6],TEXT[7],TEXT[8]};
        this.chestSelectDropdown= new DropdownMenu(this, chestTypes, FontHelper.panelNameFont, Settings.CREAM_COLOR);

        this.buttons = new HeaderButtonPlus[] { this.nameButton, this.modButton, this.actButton, this.chestButton, this.shopButton, this.campButton};
        this.dropdownMenus = new DropdownMenu[] {this.chestSelectDropdown,this.actSelectDropdown, this.modNameDropdown};
        this.dropdownMenuHeaders = new String[] {TEXT[3],TEXT[0], "Mod"};

        this.searchBox = new TextSearchBox(this, 0.0f, START_Y, false);
    }

    private HashSet<String> findEventAddingMods() {

        eventModNames = ((EventSelectScreen)this.selectScreen).eventModNames;

        return ((EventSelectScreen)this.selectScreen).eventAddingMods;
    }



    @Override
    public void didChangeOrder(HeaderButtonPlus button, boolean isAscending) {
        if (button == this.nameButton) {
            clearActiveButtons();
            ((EventSelectScreen)this.selectScreen).sortAlphabetically(isAscending);
            resetOtherButtons();
        } else if (button == this.modButton) {
            clearActiveButtons();
            ((EventSelectScreen)this.selectScreen).sortByMod(isAscending);
            resetOtherButtons();
        } else if (button == this.actButton) {
            goToAct();
        } else if (button == this.chestButton) {
            AbstractChest chest = null;
            switch (this.currentChestTypeSelection) {
                case 3:
                    break;
                case 2:
                    chest = new LargeChest();
                    break;
                case 1:
                    chest = new MediumChest();
                    break;
                case 0:
                default:
                    chest = new SmallChest();
                    break;
            }
            this.selectScreen.close();
            if(this.currentChestTypeSelection == 3) {
                EventfulCompass.goToRoom(new TreasureRoomBoss());
            } else {
                CustomChestRoom treasureRoom = new CustomChestRoom();
                treasureRoom.chest = chest;
                EventfulCompass.goToRoom(treasureRoom);
            }

        } else if (button == this.shopButton) {
            this.selectScreen.close();
            EventfulCompass.goToRoom(new ShopRoom());
        } else if (button == this.campButton) {
            this.selectScreen.close();
            EventfulCompass.goToRoom(new RestRoom());
        } else if (button == this.neowButton) {
            this.selectScreen.close();
            EventfulCompass.goToRoom(new NeowRoom(false));
        } else {
            return;
        }
        this.justSorted = true;
        button.setActive(true);

    }


    @Override
    public void changedSelectionTo(DropdownMenu dropdownMenu, int i, String s) {

        if (dropdownMenu == this.modNameDropdown) {
            if (i == 0) {
                //if showing all
                ((EventSelectScreen)this.selectScreen).filterMod = null;
            } else if (i == (eventMods.size() + 2) - 1) {
                ((EventSelectScreen)this.selectScreen).filterMod = "Slay the Spire";
            } else {
                ((EventSelectScreen)this.selectScreen).filterMod = eventMods.get(i - 1);
            }
            ((EventSelectScreen)this.selectScreen).updateFilters();
        }
        if(dropdownMenu == this.actSelectDropdown) {
            this.currentActSelection = s;
        }
        if(dropdownMenu == this.chestSelectDropdown) {
            this.currentChestTypeSelection = i;
        }
    }

    public void goToAct() {
        String pickedActId = null;
        for (Map.Entry<String, String> entry : actNames.entrySet()) {
            if (Objects.equals(this.currentActSelection, entry.getValue())) {
                pickedActId = entry.getKey();
            }
        }
        int pickedActNum = acts.get(pickedActId) - 1;
        try {
            DevConsole.log("Skipping to act " + pickedActId);
            if (AbstractDungeon.floorNum <= 1) {
                AbstractDungeon.floorNum = 2;
            }

            this.prepareTransition();
            CardCrawlGame.nextDungeon = pickedActId;
            CardCrawlGame.dungeonTransitionScreen = new DungeonTransitionScreen(pickedActId);
            AbstractDungeon.actNum = pickedActNum;
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(pickedActNum != AbstractDungeon.actNum) {
//
//        }
    }

    private void prepareTransition() {
        selectScreen.close();
        AbstractDungeon.player.hand.group.clear();
        AbstractDungeon.actionManager.clear();
        AbstractDungeon.effectsQueue.clear();
        AbstractDungeon.effectList.clear();

        for(int i = AbstractDungeon.topLevelEffects.size() - 1; i > 0; --i) {
            if (AbstractDungeon.topLevelEffects.get(i) instanceof BattleStartEffect) {
                AbstractDungeon.topLevelEffects.remove(i);
            }
        }

    }

    @Override
    protected void renderButtons(SpriteBatch sb) {
        for (HeaderButtonPlus b : this.buttons) {
            b.render(sb);
        }

        float spaceY = 52.0f * Settings.yScale;
        float yPos = START_Y - 325.0f * Settings.yScale;

        float xPos = 0.0f;

        for (int i = 0; i< this.dropdownMenus.length ; i++) {

            DropdownMenu ddm = this.dropdownMenus[i];

            ddm.render(sb,xPos,yPos);
            yPos += 0.5f * spaceY;
            FontHelper.renderSmartText(sb, FontHelper.tipHeaderFont, dropdownMenuHeaders[i], xPos, yPos, 250.0F, 20.0F, Settings.GOLD_COLOR);
            yPos += 2.0f * spaceY;
        }

    }
}

