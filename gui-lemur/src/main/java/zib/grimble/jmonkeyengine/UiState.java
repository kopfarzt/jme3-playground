package zib.grimble.jmonkeyengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;

public class UiState extends BaseAppState {
    private Container gui;

    @Override
    protected void initialize(Application app) {
        System.out.printf("%s initialize%n", this.getClass());
        gui = new Container();
        gui.setLocalTranslation(10, 10, 0);

        gui.addChild(new Label("Pause"));
        var resumeButton = new Button("Resume");
        gui.addChild(resumeButton);
        resumeButton.addClickCommands(b -> {
            getStateManager().getState(GameState.class).setEnabled(true);
            setEnabled(false);
        });
        var quitButton = new Button("Quit");
        gui.addChild(quitButton);
        quitButton.addClickCommands(b -> getApplication().stop());
    }

    @Override
    protected void cleanup(Application app) {
        System.out.printf("%s cleanup%n", this.getClass());
        gui.removeFromParent();
    }

    @Override
    protected void onEnable() {
        System.out.printf("%s onEnable%n", this.getClass());
        if (getApplication() instanceof SimpleApplication sapp) {
            sapp.getGuiNode().attachChild(gui);
        }
    }

    @Override
    protected void onDisable() {
        System.out.printf("%s onDisable%n", this.getClass());
        gui.removeFromParent();
    }
}
