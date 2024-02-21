package loadout.helper;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface RenderAction {
    void render(SpriteBatch sb);
    RenderAction EMPTY_ACTION = (a) -> {};
}
