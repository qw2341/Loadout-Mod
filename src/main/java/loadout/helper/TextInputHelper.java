package loadout.helper;

import com.badlogic.gdx.InputProcessor;

public class TextInputHelper implements InputProcessor {

    public TextInputReceiver delegate;

    public boolean digitOnly;

    public TextInputHelper(TextInputReceiver delegate, boolean digitOnly) {
        this.delegate = delegate;
        this.digitOnly = digitOnly;
    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        String charStr = String.valueOf(c);

        if (charStr.length() != 1) return false;

        if(digitOnly) {
            if (Character.isDigit(c)) {
                delegate.setTextField(delegate.getTextField() + charStr);
            } else return false;
        } else {
            if (Character.isDigit(c) || Character.isLetter(c) || (c >=32 && c<=126)) {
                delegate.setTextField(delegate.getTextField() + charStr);
            } else return false;
        }

        return true;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }
}
