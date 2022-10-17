package loadout.ui;

import basemod.TopPanelItem;
import com.badlogic.gdx.graphics.Texture;

public class LoadoutPointPanel extends TopPanelItem {
    private static final Texture IMG = new Texture("yourmodresources/images/icon.png");
    public static final String ID = "TopPanelItemExample";

    public LoadoutPointPanel(Texture image, String ID) {
        super(image, ID);
    }

    @Override
    protected void onClick() {

    }
}
