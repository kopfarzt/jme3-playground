package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.*;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.ui.ObservableRangedValueModel;

public class UiState extends BaseAppState {
    public static final String CONTINUE = "Continue";
    public static final String PAUSE = "Pause";
    private static final Logger LOG = LoggerFactory.getLogger(UiState.class);
    private Main app;
    private Node gui;
    private GameState gameState;

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

        int panelWidth = screenWidth / 4;
        int panelHeight = screenHeight;

        //mainPanel.setPreferredSize(new Vector3f(panelWidth, panelHeight, 0));


        var title = mainPanel.addChild(new Label("Vehicle"));
        title.setFontSize(32);

        var speedLabel = new Label("");
        var speedModel = new ObservableRangedValueModel(0, 100, 50);
        speedModel.addChangeCommand(model -> LOG.info("Value: %.2f".formatted(model.getValue())));
        var speedSlider = mainPanel.addChild(new Slider(speedModel, Axis.X));
        // speedSlider.setPreferredSize(new Vector3f((screenWidth / 3) - 20, 40, 0));


        var pauseButton = mainPanel.addChild(new Button(PAUSE));
        pauseButton.addClickCommands(source -> {
            gameState.setPaused(!getGameState().isPaused());
            pauseButton.setText(gameState.isPaused() ? CONTINUE : PAUSE);
        });

        var quitButton = mainPanel.addChild(new Button("Beenden"));
        quitButton.addClickCommands(source -> app.stop());

        mainPanel.setLocalTranslation(screenWidth - panelWidth, screenHeight, 1);

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

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}