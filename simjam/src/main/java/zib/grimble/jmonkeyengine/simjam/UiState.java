package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.ui.ObservableRangedValueModel;

import java.util.Objects;

public class UiState extends BaseAppState {
    public static final String CONTINUE = "Continue";
    public static final String PAUSE = "Pause";
    private static final Logger LOG = LoggerFactory.getLogger(UiState.class);
    private Main app;
    private Node gui;
    private GameState gameState;
    private ObservableRangedValueModel speedModel;
    private ObservableRangedValueModel accModel;
    private ObservableRangedValueModel decModel;
    private boolean paused = false;
    private Spatial selectedVehicle;

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

        mainPanel.setPreferredSize(new Vector3f(panelWidth, panelHeight, 0));
        mainPanel.setLayout(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.Even));
        //mainPanel.setInsets(new Insets3f(15, 15, 15, 15));

        var title = mainPanel.addChild(new Label("Vehicle"));
        title.setFontSize(32);

        var speedLabel = mainPanel.addChild(new Label("Speed"));
        speedModel = new ObservableRangedValueModel(0, 100, 50);
        speedModel.addChangeCommand(model -> LOG.info("Value: %.2f".formatted(model.getValue())));
        var speedSlider = mainPanel.addChild(new Slider(speedModel, Axis.X));

        var accLabel = mainPanel.addChild(new Label("Acc"));
        accModel = new ObservableRangedValueModel(0, 10, 5);
        accModel.addChangeCommand(model -> LOG.info("Value: %.2f".formatted(model.getValue())));
        var accSlider = mainPanel.addChild(new Slider(accModel, Axis.X));

        var decLabel = mainPanel.addChild(new Label("Dec"));
        decModel = new ObservableRangedValueModel(0, 20, 10);
        decModel.addChangeCommand(model -> LOG.info("Value: %.2f".formatted(model.getValue())));
        var decSlider = mainPanel.addChild(new Slider(decModel, Axis.X));
        // speedSlider.setPreferredSize(new Vector3f((screenWidth / 3) - 20, 40, 0));


        var pauseButton = mainPanel.addChild(new Button(PAUSE));
        pauseButton.addClickCommands(source -> {
            paused = !paused;
            pauseButton.setText(paused ? CONTINUE : PAUSE);
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

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public Spatial getSelectedVehicle() {
        return selectedVehicle;
    }

    public void setSelectedVehicle(Spatial selectedVehicle) {
        LOG.info("selected vehicle: %s (%08x)".formatted(selectedVehicle, Objects.hashCode(selectedVehicle)));
        this.selectedVehicle = selectedVehicle;
    }
}