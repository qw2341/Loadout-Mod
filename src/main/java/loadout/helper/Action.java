package loadout.helper;

public interface Action {
    void execute();

    Action EMPTY_ACTION = () -> {};
}
