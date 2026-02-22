package zib.grimble.jmonkeyengine;

import com.jme3.app.SimpleApplication;
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

public class Main extends SimpleApplication {
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;

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
//        setDisplayFps(false);
//        setDisplayStatView(false);
//        showSettings();
//        // createGui();
        initCamera();
        createLightsAndShadows();
        createGroundPlane();
        createObjects();

        GuiGlobals.initialize(this);

        var gameState = new GameState();
        stateManager.attach(gameState);
        var uiState = new UiState();
        stateManager.attach(uiState);
        uiState.setEnabled(false);
    }

    private void createGui() {
        // GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        var container = new Container();
        guiNode.attachChild(container);
        container.setLocalTranslation(100, 100, 0);
        container.addChild(new Label("Hello world."));
        getStateManager().attach(new MouseAppState(this));

        inputManager.setCursorVisible(false);
        flyCam.setEnabled(true);
    }

    private void showSettings() {
        System.out.println();
        System.out.printf("Renderer: %s%n", renderer);
        System.out.println();
        System.out.println("App Settings");
        settings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> System.out.format("%s: %s%n", e.getKey(), e.getValue()));
        System.out.println();
        System.out.format("Java Version: %s&n", Runtime.version());
        var limits = renderer.getLimits();
        System.out.println();
        System.out.println("Renderer Limits");
        limits.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> System.out.format("%s: %s%n", e.getKey(), e.getValue()));
    }

    private void initCamera() {
        flyCam.setZoomSpeed(10);
        flyCam.setMoveSpeed(6);
        cam.setLocation(new Vector3f(0, 8, 20));
        cam.lookAt(new Vector3f(0, 2, 0), Vector3f.UNIT_Y);
    }

    private void createLightsAndShadows() {
        var ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(255, 255, 255, 0).mult(1f));
        rootNode.addLight(ambientLight);

        var directionalLight = new DirectionalLight(new Vector3f(1, -1, -1), ColorRGBA.White);
        rootNode.addLight(directionalLight);

        addShadowRenderer(directionalLight, 4096, 4);
    }

    private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
        var shadowRenderer = new DirectionalLightShadowRenderer(assetManager, shadowMapSize, nbSplits);
        shadowRenderer.setLight(light);
        viewPort.addProcessor(shadowRenderer);
    }

    private Spatial createGroundPlane() {
        Box box = new Box(20, 0.01f, 20);

        var geometry = new Geometry("GroundPlane", box);

        geometry.setMaterial(MaterialFactory.get(assetManager).createPlastic(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff), 0.0f));
        geometry.setLocalTranslation(0, -0.015f, 0);

        geometry.setShadowMode(RenderQueue.ShadowMode.Receive);

        rootNode.attachChild(geometry);

        return geometry;
    }

    private void createObjects() {
        var mesh = new ParameterizedSurfaceGrid(
                new SuperSphere(1f, 0.6f, 0.6f),
                null, 50, 25, true, true);
        var geometry = new Geometry("first", mesh);
        geometry.setLocalTranslation(new Vector3f(0f, 0f, 0f));
        geometry.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
        geometry.setMaterial(MaterialFactory.get(assetManager).createRustyMetal());

        rootNode.attachChild(geometry);

//        MouseEventControl.addListenersToSpatial(geometry, new DefaultMouseListener() {
//            @Override
//            protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
//                System.out.printf("Clicked on %s.%n", target);
//            }
//        });
    }
}