package zib.grimble.jmonkeyengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;

public class GameState extends BaseAppState {
    @Override
    protected void initialize(Application app) {
        System.out.printf("%s initialize%n", this.getClass());
    }

    @Override
    protected void cleanup(Application app) {
        System.out.printf("%s cleanup%n", this.getClass());
        super.cleanup();
    }

    @Override
    protected void onEnable() {
        System.out.printf("%s onEnable%n", this.getClass());
        if (getApplication() instanceof SimpleApplication sapp) {
            System.out.printf("%s enable fly-by camera%n", this.getClass());
            sapp.getFlyByCamera().setEnabled(true);
            sapp.getInputManager().setCursorVisible(false);
        }
    }

    @Override
    protected void onDisable() {
        System.out.printf("%s onDisable%n", this.getClass());
        if (getApplication() instanceof SimpleApplication sapp) {
            System.out.printf("%s disable fly-by camera%n", this.getClass());
            sapp.getFlyByCamera().setEnabled(true);
            sapp.getInputManager().setCursorVisible(false);
        }
    }
}
