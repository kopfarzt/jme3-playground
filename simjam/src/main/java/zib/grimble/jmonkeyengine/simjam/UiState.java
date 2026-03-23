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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UiState extends BaseAppState {
    public static final String CONTINUE = "Continue";
    public static final String PAUSE = "Pause";
    private static final Logger LOG = LoggerFactory.getLogger(UiState.class);
    private Main app;
    private Node gui;
    private GameState gameState;
    private Label title;
    private ObservableRangedValueModel speedModel;
    private ObservableRangedValueModel accModel;
    private ObservableRangedValueModel decModel;
    private boolean paused = false;
    private Spatial selectedVehicle;
    private Slider speedSlider;
    private Label speedLabel;
    private Label accLabel;
    private Label decLabel;
    private Slider accSlider;
    private Slider decSlider;
    private Label infoLabel;

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

        title = mainPanel.addChild(new Label("Vehicle"));
        title.setFontSize(16);

        speedLabel = mainPanel.addChild(new Label("Max Speed"));
        speedModel = new ObservableRangedValueModel(0, 100, 50);
        speedModel.addChangeCommand(model -> {
            if (selectedVehicle != null) {
                var ctrl = selectedVehicle.getControl(VehicleControl.class);
                ctrl.setMaxSpeed((float) model.getValue());
            }
            updateGui();
        });
        speedSlider = mainPanel.addChild(new Slider(speedModel, Axis.X));

        accLabel = mainPanel.addChild(new Label("Acc"));
        accModel = new ObservableRangedValueModel(0, 10, 5);
        accModel.addChangeCommand(model -> {
            if (selectedVehicle != null) {
                var ctrl = selectedVehicle.getControl(VehicleControl.class);
                ctrl.setMaxAcc((float) model.getValue());
            }
            updateGui();
        });
        accSlider = mainPanel.addChild(new Slider(accModel, Axis.X));

        decLabel = mainPanel.addChild(new Label("Dec"));
        decModel = new ObservableRangedValueModel(0, 20, 10);
        decModel.addChangeCommand(model -> {
            if (selectedVehicle != null) {
                var ctrl = selectedVehicle.getControl(VehicleControl.class);
                ctrl.setMaxDec((float) model.getValue());
            }
            updateGui();
        });
        decSlider = mainPanel.addChild(new Slider(decModel, Axis.X));
        // speedSlider.setPreferredSize(new Vector3f((screenWidth / 3) - 20, 40, 0));

        var removeButton = mainPanel.addChild(new Button("Remove vehicle"));
        removeButton.addClickCommands(source -> {
            LOG.info("Remove vehicle {}.", selectedVehicle);
        });
        var insertButton = mainPanel.addChild(new Button("Insert vehicle"));
        insertButton.addClickCommands(source -> {
            LOG.info("Insert vehicle {}.", selectedVehicle);
        });
        var pauseButton = mainPanel.addChild(new Button(PAUSE));
        pauseButton.addClickCommands(source -> {
            paused = !paused;
            pauseButton.setText(paused ? CONTINUE : PAUSE);
        });

        var quitButton = mainPanel.addChild(new Button("Beenden"));
        quitButton.addClickCommands(source -> app.stop());

        infoLabel = mainPanel.addChild(new Label(""));

        mainPanel.setLocalTranslation(screenWidth - panelWidth, screenHeight, 1);

        return mainPanel;
    }

    protected List<VehicleControl> vehicles(VehicleControl start) {
        List<VehicleControl> result = new ArrayList<>();
        result.add(start);
        var cur = start.getPredecessor();
        while (cur != start) {
            result.add(cur);
            cur = cur.getPredecessor();
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        LOG.info("onEnable");

        if (gui == null) {
            gui = initializeGui();
            setSelectedVehicle(null);
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
        if (selectedVehicle != null) {
            var ctrl = selectedVehicle.getControl(VehicleControl.class);
            speedModel.setValue(ctrl.getMaxSpeed());
            accModel.setValue(ctrl.getMaxAcc());
            decModel.setValue(ctrl.getMaxDec());
        }
        updateGui();
    }

    public void updateGui() {
        if (selectedVehicle != null) {
            var ctrl = selectedVehicle.getControl(VehicleControl.class);
            title.setText(selectedVehicle.getName());
            speedLabel.setText("Max Speed: %.1f".formatted(ctrl.getMaxSpeed()));
            speedSlider.setCullHint(Spatial.CullHint.Never);
            accLabel.setText("Max Acc: %.1f".formatted(ctrl.getMaxAcc()));
            accSlider.setCullHint(Spatial.CullHint.Never);
            decLabel.setText("Max Dec: %.1f".formatted(ctrl.getMaxDec()));
            decSlider.setCullHint(Spatial.CullHint.Never);
            infoLabel.setText(vehicles(ctrl).stream()
                    .map(c -> c.getSpatial().getName())
                    .collect(Collectors.joining(" - ")));
        } else {
            title.setText("No selection");
            speedLabel.setText("Max Speed:");
            speedSlider.setCullHint(Spatial.CullHint.Always);
            accLabel.setText("Max Acc:");
            accSlider.setCullHint(Spatial.CullHint.Always);
            decLabel.setText("Max Dec:");
            decSlider.setCullHint(Spatial.CullHint.Always);
            infoLabel.setText("");

        }
    }
}