package loadout.screens;

import basemod.ReflectionHacks;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.Skeleton;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.monsters.exordium.AcidSlime_S;
import com.megacrit.cardcrawl.monsters.exordium.ApologySlime;
import com.megacrit.cardcrawl.monsters.exordium.SpikeSlime_S;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.SlowPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import loadout.LoadoutMod;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

import static loadout.screens.PowerSelectScreen.dummyMonster;

public class MonsterSelectScreen extends AbstractSelectScreen<MonsterSelectScreen.MonsterButton> {

    public static HashSet<String> noBGMBossList = new HashSet<>();
    static {
        noBGMBossList.add("Donu");
        noBGMBossList.add("Hexaghost");
        noBGMBossList.add("SlimeBoss");
        noBGMBossList.add("TheGuardian");
    }

    public static class MonsterButton {

        private Skeleton skeleton;
        private TextureAtlas atlas;
        private Texture img;
        public String id;
        public String name;

        public String modID;

        public int amount;
        public Hitbox hb;
        public float x;
        public float y;
        public AbstractMonster.EnemyType type;

        public MonsterStrings monsterStrings;
        public AbstractMonster instance;
        public Class<? extends AbstractMonster> mClass;
        public ArrayList<PowerTip> tips;


        public MonsterButton(Class<?extends AbstractMonster> amClass) {
            this.hb = new Hitbox(200.0f * Settings.scale,75.0f * Settings.yScale);
            this.modID = WhatMod.findModID(amClass);
            this.x = 0.0f;
            this.y = 0.0f;
            if (this.modID == null) this.modID = "Slay the Spire";
            this.tips = new ArrayList<>();
            this.mClass = amClass;
            try {
                try{
                    this.name = (String) amClass.getField("NAME").get(null);

                } catch(NoSuchFieldException nsfe) {

                    if(doesClassContainField(amClass,"monsterStrings")) {
                        this.monsterStrings = ReflectionHacks.getPrivateStatic(amClass, "monsterStrings");
                        this.name = this.monsterStrings.NAME;
                    }
                } catch (Exception e) {

                }


                this.instance = createMonster(amClass);

                this.id = this.instance.id;
                this.type = this.instance.type;
                if(this.name == null|| this.name.length() == 0) this.name = this.instance.name;
                if (this.name == null || this.name.length() == 0) this.name = "Unnamed Monster";
                //this.img = ReflectionHacks.getPrivate(this.instance,AbstractCreature.class,"img");
                //this.atlas =  ReflectionHacks.getPrivate(this.instance,AbstractCreature.class,"atlas");
                //this.skeleton = ReflectionHacks.getPrivate(this.instance,AbstractCreature.class,"skeleton");
                this.tips.add(new PowerTip(this.name, "HP: " + this.instance.maxHealth));
            } catch (Exception e) {
                LoadoutMod.logger.info("Failed to create monster button for : " + amClass.getName() + " Defaulting to sorry slime");
                this.instance = new ApologySlime();
                this.name = ApologySlime.NAME;
                this.id = this.instance.id;
                this.type = AbstractMonster.EnemyType.NORMAL;



            }
            this.instance.dispose();
            this.instance = null;

            try {
                this.id = (String) amClass.getField("ID").get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                this.id = "Unnamed Monster";
            }

            if(this.id == null || this.id.length() == 0) this.id = "Unnamed Monster";
            if (this.name == null) this.name = "Unnamed Monster";
            if (this.modID == null) this.modID = "Slay the Spire";
            if (this.type == null) this.type = AbstractMonster.EnemyType.NORMAL;




            this.amount = 0;
        }

        public static boolean doesClassContainField(Class clazz, String fieldName) {
            return Arrays.stream(clazz.getFields())
                    .anyMatch(f -> f.getName().equals(fieldName));
        }

        public static AbstractMonster createMonster(Class<? extends AbstractMonster> amClass) {
            //Exceptions
            if(amClass.equals(AcidSlime_S.class)) {
                return new AcidSlime_S(0,0,0);
            } else if(amClass.equals(SpikeSlime_S.class)) {
                return new SpikeSlime_S(0,0,0);
            } else if(amClass.getName().equals("monsters.pet.ScapeGoatPet")) {
                return new ApologySlime();
            }


            Constructor<?>[] con = amClass.getDeclaredConstructors();
            if(con.length > 0) {
                Constructor<?> c = con[0];
                try {
                    int paramCt = c.getParameterCount();
                    Class[] params = c.getParameterTypes();
                    Object[] paramz = new Object[paramCt];

                    for (int i = 0 ; i< paramCt; i++) {
                        Class param = params[i];
                        if (int.class.isAssignableFrom(param)) {
                            paramz[i] = 1;
                        } else if (boolean.class.isAssignableFrom(param)) {
                            paramz[i] = true;
                        } else if (float.class.isAssignableFrom(param)) {
                            paramz[i] = 0.0F;
                        } else if (AbstractMonster.class.isAssignableFrom(param)) {
                            paramz[i] = dummyMonster;
                        }
                    }
                    //LoadoutMod.logger.info("Class: " + pClass.getName() + " with parameter: " + Arrays.toString(paramz));

                    return (AbstractMonster) c.newInstance(paramz);
                } catch (Exception e) {
                    LoadoutMod.logger.info("Error occurred while trying to instantiate class: " + c.getName());
                    //e.printStackTrace();
                    LoadoutMod.logger.info("Reverting to Apology Slime");
                    return new ApologySlime();
                }
            }
            LoadoutMod.logger.info("Failed to create monster, returning Apology Slime");
            return new ApologySlime();
        }

        public void update() {
            //this.hb.move(x,y);
            this.hb.update();

            if((this.hb.justHovered || MonsterSelectScreen.showPreviews) && this.instance == null) {
                //LoadoutMod.logger.info("just hovered, creating class");
                try{
                    this.instance = createMonster(this.mClass);
                } catch (Exception|Error e) {
                    LoadoutMod.logger.info("just hovered, failed to create class");
                }

            }

            if(!this.hb.hovered && this.instance != null && !MonsterSelectScreen.showPreviews) {
                this.instance.dispose();
                this.instance = null;
            }

            if(this.instance != null) {
                this.instance.hb.move(x,y);
                this.instance.drawX = x;
                this.instance.drawY = y;
                if(this.hb.hovered || MonsterSelectScreen.showPreviews) this.instance.update();
            }


            if(this.hb.clicked) {
                this.hb.clicked = false;
                if(AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                    MonsterGroup mg = AbstractDungeon.getMonsters();
                    float monsterDX = Settings.WIDTH / 2.0F;
                    float monsterDY = AbstractDungeon.player.drawY;
                    AbstractMonster lastMonster = null;

                    if(!mg.monsters.isEmpty()) {
                        lastMonster = mg.monsters.get(mg.monsters.size()-1);
                        monsterDX = lastMonster.drawX ;
                        monsterDY = lastMonster.drawY;
                    }
                    AbstractMonster m = createMonster(this.mClass);
                    m.drawX = monsterDX - (lastMonster != null ? calculateSmartDistance(lastMonster,m) : 200.0F) * Settings.scale;
                    m.drawY = monsterDY;
                    if(m.drawX < AbstractDungeon.player.drawX) {
                        m.drawX = (float) (Settings.WIDTH - 300.0F * Settings.scale + 20.0F * Math.random());
                        m.drawY += 300.0F;
                        if(m.drawY > Settings.HEIGHT-200.0F * Settings.yScale) {
                            m.drawY = (float) (Settings.HEIGHT * 0.3F  + 20.0F * Math.random());
                        }
                    }

                    m.hb.move(m.drawX,m.drawY);

                    m.init();
                    m.applyPowers();

                    if (ModHelper.isModEnabled("Lethality")) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new ApplyPowerAction((AbstractCreature)m, (AbstractCreature)m, (AbstractPower)new StrengthPower((AbstractCreature)m, 3), 3));
                    }

                    if (ModHelper.isModEnabled("Time Dilation")) {
                        AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new ApplyPowerAction((AbstractCreature)m, (AbstractCreature)m, (AbstractPower)new SlowPower((AbstractCreature)m, 0)));
                    }
                    m.showHealthBar();
                    m.createIntent();
                    if(m.type == AbstractMonster.EnemyType.BOSS && !noBGMBossList.contains(m.id)) {

                        CardCrawlGame.music.silenceTempBgmInstantly();
                        CardCrawlGame.music.silenceBGMInstantly();
                    }
                    m.usePreBattleAction();

                    mg.add(m);


                    for (AbstractRelic r : AbstractDungeon.player.relics) {
                        r.onSpawnMonster(m);
                    }
                }
            }
        }
        public static float calculateSmartDistance(AbstractMonster m1, AbstractMonster m2) {
            return (m1.hb_w + m2.hb_w)/2.0F;
        }

        public void render(SpriteBatch sb) {
            if(this.hb != null) {
                this.hb.render(sb);

//                if (atlas == null) {
//                    sb.setColor(this.instance.tint.color);
//                    if (this.img != null) {
//                        sb.draw(this.img, this.instance.drawX - (float)128.0F * Settings.scale / 2.0F + this.instance.animX, this.instance.drawY + this.instance.animY, (float)128.0F * Settings.scale, (float)128.0F * Settings.scale, 0, 0, this.img.getWidth(), this.img.getHeight(), this.instance.flipHorizontal, this.instance.flipVertical);
//                    }
//                } else {
//
//                    this.instance.state.update(Gdx.graphics.getDeltaTime());
//                    this.instance.state.apply(skeleton);
//                    skeleton.updateWorldTransform();
//                    skeleton.setPosition(this.instance.drawX + this.instance.animX, this.instance.drawY + this.instance.animY);
//                    skeleton.setColor(this.instance.tint.color);
//                    skeleton.setFlip(this.instance.flipHorizontal, this.instance.flipVertical);
//
//                    sb.end();
//                    CardCrawlGame.psb.begin();
//                    sr.draw(CardCrawlGame.psb, skeleton);
//                    CardCrawlGame.psb.end();
//                    sb.begin();
//                    sb.setBlendFunction(770, 771);
//                }
//
//                if (this.hb.hovered && atlas == null) {
//                    sb.setBlendFunction(770, 1);
//                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.1F));
//                    if (this.img != null) {
//                        sb.draw(this.img, this.instance.drawX - (float)128.0F * Settings.scale / 2.0F + this.instance.animX, this.instance.drawY + this.instance.animY, (float)128.0f * Settings.scale, (float)128.0F * Settings.scale, 0, 0, this.img.getWidth(), this.img.getHeight(), this.instance.flipHorizontal, this.instance.flipVertical);
//                        sb.setBlendFunction(770, 771);
//                    }
//                }
                if(this.hb.hovered || MonsterSelectScreen.showPreviews) {
                    try {
                        if(this.instance != null) this.instance.render(sb);
                    } catch (Exception ignored) {

                    }
                }
                if (this.hb.hovered) {
                    sb.setBlendFunction(770, 1);
                    sb.setColor(new Color(1.0F, 1.0F, 1.0F, 0.3F));
                    sb.draw(ImageMaster.CHAR_OPT_HIGHLIGHT, x+40.0F,y-64.0F, 64.0F, 64.0F, 300.0f, 100.0f, Settings.scale, Settings.scale, 0.0F, 0, 0, 256, 256, false, false);
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.GOLD_COLOR);
                    sb.setBlendFunction(770, 771);

                    TipHelper.queuePowerTips(InputHelper.mX + 60.0F * Settings.scale, InputHelper.mY + 180.0F * Settings.scale, this.tips);
                } else {
                    FontHelper.renderSmartText(sb,FontHelper.buttonLabelFont,this.name,x+150.0f / 2,y + 20.0f,200.0f,25.0f,Settings.CREAM_COLOR);
                }

                //
            }
        }
    }



    private static final Comparator<MonsterButton> BY_TYPE = Comparator.comparing(m -> m.type);
    private static final Comparator<MonsterButton> BY_NAME = Comparator.comparing(m -> m.name);
    private static final Comparator<MonsterButton> BY_MOD = Comparator.comparing(m -> m.modID);

    private static final Comparator<MonsterButton> BY_ID = Comparator.comparing(m -> m.id);

    public AbstractMonster.EnemyType filterType = null;
    public boolean filterAll = true;
    public boolean filterFavorites = false;

    public static boolean showPreviews = false;



    public MonsterSelectScreen(AbstractRelic owner) {
        super(owner);

        this.items.addAll(LoadoutMod.monstersToDisplay);
        this.itemsClone = LoadoutMod.monstersToDisplay;
        this.sortHeader = new MonsterSelectSortHeader(this);

        this.currentSortOrder = SortOrder.ASCENDING;
        this.currentSortType = SortType.MOD;
        this.defaultSortType = SortType.MOD;

        this.itemHeight = 150.0F;
    }


    private boolean testTextFilter(MonsterButton mb) {
        if (mb.id != null && StringUtils.containsIgnoreCase(mb.id,((MonsterSelectSortHeader)this.sortHeader).searchBox.filterText)) return true;
        if (mb.name != null && StringUtils.containsIgnoreCase(mb.name,((MonsterSelectSortHeader)this.sortHeader).searchBox.filterText)) return true;
        return false;
    }

    protected boolean testFilters(MonsterButton mb) {

        boolean favCheck = filterAll || (filterFavorites);
        boolean textCheck = sortHeader == null || ((MonsterSelectSortHeader)this.sortHeader).searchBox.filterText.equals("") || testTextFilter(mb);
        boolean typeCheck = filterType == null || mb.type == filterType;
        return favCheck && textCheck && typeCheck;
    }

    @Override
    public void updateFilters() {
        super.updateFilters();

        if(!filterFavorites)
            scrolledUsingBar(0.0f);
    }

    @Override
    public void sort(boolean isAscending) {
        switch (currentSortType) {
            case NAME:
                sortAlphabetically(isAscending);
                break;
            case MOD:
                sortByMod(isAscending);
                break;
            case TYPE:
                sortByType(isAscending);
                break;
        }
    }

    @Override
    protected void callOnOpen() {

    }

    @Override
    protected void updateItemClickLogic() {

    }

    public void sortByType(boolean isAscending) {
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(BY_TYPE.thenComparing(BY_NAME));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(BY_TYPE.reversed().thenComparing(BY_NAME));
        }
        this.currentSortType = SortType.TYPE;
        scrolledUsingBar(0.0F);
    }

    public void sortByMod(boolean isAscending) {
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            this.items.sort(BY_MOD.thenComparing(BY_ID));
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            this.items.sort(BY_MOD.reversed().thenComparing(BY_ID));
        }
        this.currentSortType = SortType.MOD;
        scrolledUsingBar(0.0F);
    }

    public void sortAlphabetically(boolean isAscending) {
        if (isAscending) {
            this.currentSortOrder = SortOrder.ASCENDING;
            if (shouldSortById()) this.items.sort(BY_ID);
            else this.items.sort(BY_NAME);
        } else {
            this.currentSortOrder = SortOrder.DESCENDING;
            if (shouldSortById()) this.items.sort(BY_ID.reversed());
            else this.items.sort(BY_NAME.reversed());
        }
        this.currentSortType = SortType.NAME;
        scrolledUsingBar(0.0F);
    }

    @Override
    protected void updateList(ArrayList<MonsterButton> list) {
        if (this.confirmButton.hb.hovered) return;

        for (MonsterButton mb : list)
        {

            mb.hb.move(mb.x  + 150.0f, mb.y);
            mb.update();

            if (mb.hb.hovered)
            {
                hoveredItem = mb;
            }
            if(mb.hb.hovered && InputHelper.justClickedLeft) {
                mb.hb.clicked = true;
            }
        }
    }

    @Override
    protected void renderList(SpriteBatch sb, ArrayList<MonsterButton> list) {
        row += 1;
        col = 0;
        float curX;
        float curY;

        char prevFirst = '\0';
        String prevMod = "";

        itemsPerLine = showPreviews ? 4 : 5;


        float spaceX = (SPACE_X + (showPreviews ? 125.0F * Settings.scale : 0.0F));
        float spaceY = (SPACE + (showPreviews ? 150.0F * Settings.yScale : 0.0F));

        for (Iterator<MonsterButton> it = list.iterator(); it.hasNext(); ) {
            MonsterButton m = it.next();
            if(LoadoutMod.enableCategory&&this.currentSortType!=null) {
                if (currentSortType == SortType.NAME) {

                    char pFirst = (shouldSortById() || m.name== null || m.name.length() == 0) ?   m.id.toUpperCase().charAt(0) : m.name.toUpperCase().charAt(0);

                    if (pFirst != prevFirst) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevFirst = pFirst;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevFirst != '\0') {
                            msg = String.valueOf(prevFirst).toUpperCase() + ":";
                            desc = "";
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                } else if (currentSortType == SortType.MOD) {
                    String pMod = m.modID;
                    if (pMod == null) pMod = "Slay the Spire";
                    if (!pMod.equals(prevMod)) {
                        row++;
                        scrollTitleCount++;

                        //if new type, render new texts
                        prevMod = pMod;

                        String msg = "Undefined:";
                        String desc = "Error";
                        if (prevMod != null) {
                            msg = prevMod + ":";
                            desc = "";
                        }
                        //remove other lines
                        if (desc.contains("NL")) {
                            desc = desc.split(" NL ")[0];
                        } else if (desc.equals("StsOrigPlaceholder")) {
                            desc = PowerSelectScreen.TEXT[6];
                        }

                        FontHelper.renderSmartText(sb, FontHelper.buttonLabelFont, msg, START_X - 50.0F * Settings.scale, this.scrollY + 4.0F * Settings.scale - SPACE * this.row, 99999.0F, 0.0F, Settings.GOLD_COLOR);
                        if (LoadoutMod.enableDesc) FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, desc, START_X - 50.0F * Settings.scale +

                                FontHelper.getSmartWidth(FontHelper.buttonLabelFont, msg, 99999.0F, 0.0F), this.scrollY - 0.0F * Settings.scale - SPACE * this.row, 99999.0F, 20.0F, Settings.CREAM_COLOR);
                        row++;
                        col = 0;
                    }
                }
            }


            if (col == itemsPerLine) {
                col = 0;
                row += 1;
            }
            curX = (START_X + spaceX * col);
            curY = (scrollY - spaceY * row);

            m.x = curX;
            m.y = curY;

            m.render(sb);
            col += 1;
        }


    }
    public static AbstractMonster spawnMonster(Class<? extends AbstractMonster> monsterClass, float x, float y) {

        AbstractMonster m = MonsterButton.createMonster(monsterClass);
        m.drawX = x;
        m.drawY = y;

        m.hb.move(m.drawX,m.drawY);

        m.init();
        m.applyPowers();

        if (ModHelper.isModEnabled("Lethality")) {
            AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new ApplyPowerAction((AbstractCreature)m, (AbstractCreature)m, (AbstractPower)new StrengthPower((AbstractCreature)m, 3), 3));
        }

        if (ModHelper.isModEnabled("Time Dilation")) {
            AbstractDungeon.actionManager.addToBottom((AbstractGameAction)new ApplyPowerAction((AbstractCreature)m, (AbstractCreature)m, (AbstractPower)new SlowPower((AbstractCreature)m, 0)));
        }
        m.showHealthBar();
        m.createIntent();
        if(m.type == AbstractMonster.EnemyType.BOSS && !noBGMBossList.contains(m.id)) {

            CardCrawlGame.music.silenceTempBgmInstantly();
            CardCrawlGame.music.silenceBGMInstantly();
        }
        m.usePreBattleAction();

        for (AbstractRelic r : AbstractDungeon.player.relics) {
            r.onSpawnMonster(m);
        }

        return m;
    }



    }
