package zib.grimble.jmonkeyengine;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiState extends BaseAppState {
    private static final Logger LOG = LoggerFactory.getLogger(UiState.class);

    private Main app;
    private Node gui;

    @Override
    public void initialize(Application _app) {
        LOG.info("initialize");
        app = (Main) _app;
    }

    private Container initializeGui() {
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        var mainMenu = new Container();

        var title = mainMenu.addChild(new Label("Mein Spiel"));
        title.setFontSize(32);

        var startButton = mainMenu.addChild(new Button("Spiel starten"));
        startButton.addClickCommands(source -> {
            setEnabled(false);
            getStateManager().getState(GameState.class).setEnabled(true);
        });

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

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        LOG.info("onEnable");

        if (gui == null) {
            gui = initializeGui();
        }

        app.getGuiNode().attachChild(gui);
        app.camera(false);
    }

    @Override
    protected void onDisable() {
        LOG.info("onDisable");
        app.getGuiNode().detachChild(gui);
    }
}