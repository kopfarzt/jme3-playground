package zib.grimble.jmonkeyengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;

public class UiState extends BaseAppState {

    private SimpleApplication app;
    private Node guiNode;
    private Container mainMenu;
    private Node gui;

    @Override
    protected void initialize(Application _app) {
        app = (SimpleApplication) _app;
        this.guiNode = new Node("GuiNode");

        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        gui = buildGui();
        app.getGuiNode().attachChild(gui);
    }

    private Container buildGui() {
        mainMenu = new Container();

        var title = mainMenu.addChild(new Label("Mein Spiel"));
        title.setFontSize(32);

        var startButton = mainMenu.addChild(new Button("Spiel starten"));
        startButton.addClickCommands(source -> startGame());

        var quitButton = mainMenu.addChild(new Button("Beenden"));
        quitButton.addClickCommands(source -> app.stop());

        // Position setzen
        mainMenu.setLocalTranslation(
                app.getCamera().getWidth() / 2f - 100,
                app.getCamera().getHeight() / 2f + 100,
                0
        );

        return mainMenu;
    }

    private void startGame() {
        setEnabled(false);
        getStateManager().getState(GameState.class).setEnabled(true);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        app.getGuiNode().attachChild(gui);
    }

    @Override
    protected void onDisable() {
        app.getGuiNode().detachChild(gui);
    }
}