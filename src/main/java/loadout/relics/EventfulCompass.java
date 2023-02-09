package loadout.relics;

import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import loadout.LoadoutMod;
import loadout.screens.AbstractSelectScreen;
import loadout.screens.EventSelectScreen;
import loadout.util.TextureLoader;

import static loadout.LoadoutMod.*;

public class EventfulCompass extends AbstractCustomScreenRelic<EventSelectScreen.EventButton> {

    // ID, images, text.
    public static final String ID = LoadoutMod.makeID("EventfulCompass");
    private static final Texture IMG = (isIsaacMode) ? TextureLoader.getTexture(makeRelicPath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicPath("compass_relic.png"));
    private static final Texture OUTLINE = (isIsaacMode) ? TextureLoader.getTexture(makeRelicOutlinePath("compass_relic_alt.png")) : TextureLoader.getTexture(makeRelicOutlinePath("compass_relic.png"));

    public EventfulCompass() {
        super(ID, IMG, OUTLINE, RelicTier.SPECIAL, LandingSound.CLINK);
    }

    @Override
    protected AbstractSelectScreen<EventSelectScreen.EventButton> getNewSelectScreen() {
        return new EventSelectScreen(this);
    }

    @Override
    protected void doneSelectionLogics() {

    }

    @Override
    public AbstractRelic makeCopy()
    {
        return new EventfulCompass();
    }

}
