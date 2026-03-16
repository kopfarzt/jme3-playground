package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.FastLightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.CircularBand;
import zib.grimble.jme3.materials.MaterialFactory;
import zib.grimble.jme3.service.DebugService;
import zib.grimble.jme3.types.ScalingUVMap;

public class GameState extends BaseAppState {
    private static final Logger LOG = LoggerFactory.getLogger(GameState.class);

    private Main app;
    private JobProgressAdapter jobProgressListener;
    private boolean postLightProbeInit;

    @Override
    protected void initialize(Application app) {
        LOG.info("initialize");
        this.app = (Main) app;
        initCamera();
        createLightsAndShadows();
        createSkybox();
        createGroundPlane();
        createStreet();
        createLightProbe(() -> {
            postLightProbeInit = true;
        });
        this.app.camera(true);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        LOG.info("onEnable");
        app.camera(true);
    }

    @Override
    protected void onDisable() {
        LOG.info("onDisable");
    }

    @Override
    public void update(float tpf) {
        if (postLightProbeInit) {
            postLightProbeInit = false;
            createObjects();
        }
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

        addShadowRenderer(directionalLight, 1024, 4);
    }

    private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
        var shadowRenderer = new DirectionalLightShadowRenderer(app.getAssetManager(), shadowMapSize, nbSplits);
        shadowRenderer.setLight(light);

        app.getViewPort().addProcessor(shadowRenderer);
    }

    private void createGroundPlane() {
        var box = new Box(20, 0.01f, 20);

        var geometry = new Geometry("GroundPlane", box);

        geometry.setMaterial(MaterialFactory.get(app.getAssetManager()).createPlastic(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff), 0.0f));
        geometry.setLocalTranslation(0, -0.015f, 0);

        geometry.setShadowMode(RenderQueue.ShadowMode.Receive);

        app.getRootNode().attachChild(geometry);
    }

    private void createStreet() {
        var street = new ParameterizedSurfaceGrid(
                new CircularBand(9f, 10f),
                new ScalingUVMap(20f, 1f),
                100, 3,
                true, false);

        LOG.info("Street: {}", DebugService.get().buffersToString(street));


        var geometry = new Geometry("Street", street);

        // var streetMaterial = MaterialFactory.get(app.getAssetManager()).createPlastic(ColorRGBA.fromRGBA255(0xC4, 0xC4, 0xC4, 0xff), 0.0f);
        var streetMaterial = app.getAssetManager().loadMaterial("materials/street.j3m");
        //streetMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        geometry.setMaterial(streetMaterial);
        geometry.getLocalRotation().fromAngleAxis(-90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);

        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        app.getRootNode().attachChild(geometry);
    }

    private void createObjects() {
        var hatchback = app.getAssetManager().loadModel("Kennel/hatchback-sports.glb");
        hatchback.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        hatchback.setLocalScale(0.25f);
        hatchback.addControl(new VehicleControl(9.75f, 0, 10));
        app.getRootNode().attachChild(hatchback);

        var hb2 = hatchback.clone();
        hb2.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        hb2.setLocalScale(0.25f);
        hb2.addControl(new VehicleControl(9.75f, 30, 10));
        app.getRootNode().attachChild(hb2);

        var taxi = app.getAssetManager().loadModel("Kennel/taxi.glb");
        taxi.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        taxi.setLocalScale(0.25f);
        taxi.addControl(new VehicleControl(9.75f, 90, 10));
        app.getRootNode().attachChild(taxi);

        var sedan = app.getAssetManager().loadModel("Kennel/sedan.glb");
        sedan.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        sedan.setLocalScale(0.25f);
        sedan.addControl(new VehicleControl(9.75f, 180, 10));
        app.getRootNode().attachChild(sedan);

        sedan.depthFirstTraversal(spatial -> {
            if (spatial instanceof Geometry geo) {
                var name = geo.getName();
                LOG.info("Geometry: {} {}", name, geo.getMaterial().getParams());
                if (name.equals("body_0")) {
                    LOG.info("Changing material");
                    geo.getMaterial().setFloat("Roughness", 0);
                }
                LOG.info("Geometry: {} {}", name, geo.getMaterial().getParams());
            }
        });

    }

    private void createSkybox() {
        app.getRootNode().attachChild(SkyFactory.createSky(app.getAssetManager(), "textures/sky/kloppenheim_06_puresky.jpg", SkyFactory.EnvMapType.EquirectMap));
    }

    private void createLightProbe(Runnable runnable) {
        var environmentCamera = new EnvironmentCamera();
        app.getStateManager().attach(environmentCamera);
        environmentCamera.initialize(app.getStateManager(), app);
        jobProgressListener = new JobProgressAdapter() {
            @Override
            public void done(Object result) {
                runnable.run();
            }
        };

        var lightProbe = FastLightProbeFactory.makeProbe(app.getRenderManager(), app.getAssetManager(), 256, Vector3f.ZERO, 1f, 100f, app.getRootNode());
        jobProgressListener.done(null);
        // var lightProbe = LightProbeFactory.makeProbe(environmentCamera, rootNode, jobProgressListener);
        // objects outside of this radius will be black
        lightProbe.getArea().setRadius(20);
        lightProbe.setPosition(Vector3f.ZERO);
        app.getRootNode().addLight(lightProbe);
    }

}