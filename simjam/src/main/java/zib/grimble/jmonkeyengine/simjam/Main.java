package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication implements ActionListener {
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;
    private static final String GUI_TOGGLE = "Gui Toggle";
    private GameState gameState;
    private UiState uiState;

    public static void main(String[] args) {
        var app = new Main();
        var settings = new AppSettings(true);
        settings.setSamples(4);
        settings.setResolution(WIDTH, HEIGHT);
        settings.setRenderer(AppSettings.LWJGL_OPENGL45);
        settings.setGammaCorrection(true);
//        appSettings.setRenderer(AppSettings.LWJGL_OPENGL2); // to test Compatibility profile
        // settings.setRenderer(AppSettings.LWJGL_OPENGL32); // to test Core 3.2 profile
        // settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        app.setSettings(settings);
        app.setShowSettings(false);
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping(GUI_TOGGLE, new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, GUI_TOGGLE);

        uiState = new UiState();
        stateManager.attach(uiState);
        uiState.setEnabled(false);

        gameState = new GameState();
        stateManager.attach(gameState);
        gameState.setEnabled(true);

        gameState.setUiState(uiState);

        //flyCam.setDragToRotate(true);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            switch (name) {
                case GUI_TOGGLE:
                    gameState.setEnabled(!gameState.isEnabled());
                    uiState.setEnabled(!uiState.isEnabled());
                    break;
            }
        }
    }

    public void camera(boolean state) {
        getFlyByCamera().setEnabled(state);
        getInputManager().setCursorVisible(!state);
    }
}