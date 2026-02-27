package zib.grimble.jmonkeyengine;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.SuperSphere;
import zib.grimble.jme3.materials.MaterialFactory;

public class GameState extends BaseAppState {

    private SimpleApplication app;

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        initCamera();
        createLightsAndShadows();
        createGroundPlane();
        createObjects();
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
    }

    private void initCamera() {
        app.getFlyByCamera().setZoomSpeed(10);
        app.getFlyByCamera().setMoveSpeed(6);
        app.getCamera().setLocation(new Vector3f(0, 8, 20));
        app.getCamera().lookAt(new Vector3f(0, 2, 0), Vector3f.UNIT_Y);
    }

    private void createLightsAndShadows() {
        var ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(255, 255, 255, 0).mult(1f));
        app.getRootNode().addLight(ambientLight);

        var directionalLight = new DirectionalLight(new Vector3f(1, -1, -1), ColorRGBA.White);
        app.getRootNode().addLight(directionalLight);

        addShadowRenderer(directionalLight, 4096, 4);
    }

    private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
        var shadowRenderer = new DirectionalLightShadowRenderer(app.getAssetManager(), shadowMapSize, nbSplits);
        shadowRenderer.setLight(light);
        app.getViewPort().addProcessor(shadowRenderer);
    }

    private void createGroundPlane() {
        Box box = new Box(20, 0.01f, 20);

        var geometry = new Geometry("GroundPlane", box);

        geometry.setMaterial(MaterialFactory.get(app.getAssetManager()).createPlastic(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff), 0.0f));
        geometry.setLocalTranslation(0, -0.015f, 0);

        geometry.setShadowMode(RenderQueue.ShadowMode.Receive);

        app.getRootNode().attachChild(geometry);
    }

    private void createObjects() {
        var mesh = new ParameterizedSurfaceGrid(
                new SuperSphere(1f, 0.6f, 0.6f),
                null, 50, 25, true, true);
        var geometry = new Geometry("first", mesh);
        geometry.setLocalTranslation(new Vector3f(0f, 0f, 0f));
        geometry.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
        geometry.setMaterial(MaterialFactory.get(app.getAssetManager()).createRustyMetal());

        app.getRootNode().attachChild(geometry);
    }
}