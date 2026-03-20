package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
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

        var mainPanel = new Container();

        int screenWidth = app.getCamera().getWidth();
        int screenHeight = app.getCamera().getHeight();

        int panelWidth = screenWidth / 3;
        int panelHeight = screenHeight;

        mainPanel.setPreferredSize(new Vector3f(panelWidth, panelHeight, 0));

        mainPanel.setLocalTranslation(
                screenWidth - panelWidth,
                screenHeight,
                1
        );

        var title = mainPanel.addChild(new Label("Mein Spiel"));
        title.setFontSize(32);

        var startButton = mainPanel.addChild(new Button("Spiel starten"));
        startButton.addClickCommands(source -> {
            setEnabled(false);
            getStateManager().getState(GameState.class).setEnabled(true);
        });

        var dummyButton = mainPanel.addChild(new Button("Dummy"));
        dummyButton.addClickCommands(source -> {
            LOG.info("Dummy Button");
        });

        var quitButton = mainPanel.addChild(new Button("Beenden"));
        quitButton.addClickCommands(source -> app.stop());

        // Position setzen
        mainPanel.setLocalTranslation(
                app.getCamera().getWidth() / 2f - 100,
                app.getCamera().getHeight() / 2f + 100,
                0
        );

        return mainPanel;
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