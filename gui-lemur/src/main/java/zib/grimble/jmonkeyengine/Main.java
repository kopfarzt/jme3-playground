package zib.grimble.jmonkeyengine;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.style.BaseStyles;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.SuperSphere;
import zib.grimble.jme3.materials.MaterialFactory;

import java.util.Comparator;

public class Main extends SimpleApplication implements ActionListener {
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;
    private static final String GUI_TOGGLE = "Gui Toggle";
    private GameState gameState;
    private UiState uiState;

    public static void main(String[] args) {
        var app = new Main();
        app.setShowSettings(false);
        var settings = new AppSettings(true);
        settings.setSamples(4);
        settings.setResolution(WIDTH, HEIGHT);
        settings.setRenderer(AppSettings.LWJGL_OPENGL45);
        settings.setGammaCorrection(true);
//        appSettings.setRenderer(AppSettings.LWJGL_OPENGL2); // to test Compatibility profile
        settings.setRenderer(AppSettings.LWJGL_OPENGL32); // to test Core 3.2 profile
        settings.setRenderer(AppSettings.LWJGL_OPENGL2);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        gameState = new GameState();
        stateManager.attach(gameState);
        uiState = new UiState();
        stateManager.attach(uiState);
        uiState.setEnabled(false);

        inputManager.addMapping(GUI_TOGGLE, new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, GUI_TOGGLE);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            switch (name) {
                case GUI_TOGGLE:
                    gameState.setEnabled(!gameState.isEnabled());
                    uiState.setEnabled(!uiState.isEnabled());

                    getFlyByCamera().setEnabled(gameState.isEnabled());
                    getInputManager().setCursorVisible(uiState.isEnabled());
                    break;
            }
        }
    }
}