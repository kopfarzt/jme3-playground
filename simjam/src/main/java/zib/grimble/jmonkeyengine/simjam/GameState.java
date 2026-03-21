package zib.grimble.jmonkeyengine.simjam;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.FastLightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.CircularBand;
import zib.grimble.jme3.materials.MaterialFactory;
import zib.grimble.jme3.nodes.CoordinateAxes;
import zib.grimble.jme3.service.DebugService;
import zib.grimble.jme3.service.HitService;
import zib.grimble.jme3.types.ScalingUVMap;

import java.util.*;

public class GameState extends BaseAppState implements ActionListener {
    public static final String VEHICLES = "VEHICLES";
    private static final Logger LOG = LoggerFactory.getLogger(GameState.class);
    private static final List<String> CAR_MODELS = List.of(
            "Kennel/ambulance.glb",
            "Kennel/ambulance.glb",
            "Kennel/delivery-flat.glb",
            "Kennel/delivery.glb",
            "Kennel/firetruck.glb",
            "Kennel/garbage-truck.glb",
            "Kennel/hatchback-sports.glb",
            "Kennel/police.glb",
            "Kennel/race-future.glb",
            "Kennel/race.glb",
            "Kennel/sedan-sports.glb",
            "Kennel/sedan.glb",
            "Kennel/suv-luxury.glb",
            "Kennel/suv.glb",
            "Kennel/taxi.glb",
            "Kennel/tractor-police.glb",
            "Kennel/tractor-shovel.glb",
            "Kennel/tractor.glb",
            "Kennel/truck-flat.glb",
            "Kennel/truck.glb",
            "Kennel/van.glb"
    );
    private static final Random RAND = new Random();
    private static final String PICK_TOGGLE = "PICK_TOGGLE";
    private static final String COORD_AXES_TOGGLE = "COORD_AXES_TOGGLE";
    private Main app;
    private JobProgressAdapter jobProgressListener;
    private boolean postLightProbeInit;
    private Map<String, Spatial> vehicleMap = new HashMap<>();
    private CoordinateAxes coordinateAxes;
    private Node vehiclesNode;
    private Spatial selectedVehicle;
    private Spatial selectedVehicleMarker;

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

        var inputManager = app.getInputManager();
        inputManager.addMapping(PICK_TOGGLE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping(COORD_AXES_TOGGLE, new KeyTrigger(KeyInput.KEY_C));

        inputManager.addListener(this, PICK_TOGGLE, COORD_AXES_TOGGLE);
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

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            switch (name) {
                case COORD_AXES_TOGGLE:
                    LOG.info("Toggling visibiliy of coordinate axes: {}", coordinateAxes);
                    coordinateAxes.toggleVisibility();
                    LOG.info("Coordinate axes: {}", coordinateAxes);
                    break;
                case PICK_TOGGLE:
                    var hitNode = HitService.get().getHit(app.getCamera(), vehiclesNode, app.getInputManager().getCursorPosition(), new Vector2f(3, 3), true);
                    if (hitNode != null) {
                        if (selectedVehicle != null) {
                            var ctrl = selectedVehicle.getControl(VehicleControl.class);
                            selectedVehicle = null;
                            ctrl.unmark(selectedVehicleMarker);
                        }
                        selectedVehicle = hitNode;
                        var ctrl = selectedVehicle.getControl(VehicleControl.class);
                        ctrl.mark(selectedVehicleMarker);
                        LOG.info("Marking Object: {}", hitNode);
                    } else if (selectedVehicle != null) {
                        LOG.info("Unmarking Object: {}", selectedVehicle);
                        var ctrl = selectedVehicle.getControl(VehicleControl.class);
                        selectedVehicle = null;
                        ctrl.unmark(selectedVehicleMarker);
                    }
                    break;
            }
        }
    }

    private void initCamera() {
        app.getFlyByCamera().setZoomSpeed(10);
        app.getFlyByCamera().setMoveSpeed(2);
        app.getCamera().setLocation(new Vector3f(0, 8, 20));
        app.getCamera().lookAt(new Vector3f(0, 2, 0), Vector3f.UNIT_Y);
    }

    private void createLightsAndShadows() {
        var ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(255, 255, 255, 0).mult(1f));
        app.getRootNode().addLight(ambientLight);

        var directionalLight = new DirectionalLight(new Vector3f(1, -0.5f, 0.5f), ColorRGBA.White);
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

        geometry.setMaterial(MaterialFactory.get(app.getAssetManager()).createPlastic(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff), 1.0f));
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

        geometry.setShadowMode(RenderQueue.ShadowMode.Receive);

        app.getRootNode().attachChild(geometry);
    }

    private Spatial createVehicle(String model, float scale) {
        var spatial = vehicleMap.get(model);

        if (spatial == null) {
            spatial = app.getAssetManager().loadModel(model);
            spatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
            spatial.setLocalScale(scale);
            spatial.depthFirstTraversal(s -> {
                if (s instanceof Geometry geo) {
                    var name = geo.getName();
                    LOG.info("Geometry: {} {}", name, geo.getMaterial().getParams());
                    if (name.equals("body_0")) {
                        LOG.info("Changing material");
                        geo.getMaterial().setFloat("Roughness", 0);
                    }
                    LOG.info("Geometry: {} {}", name, geo.getMaterial().getParams());
                }
            });
            vehicleMap.put(model, spatial);
        } else {
            spatial = spatial.clone();
        }

        return spatial;
    }

    private Spatial createRandomVehicle() {
        var model = CAR_MODELS.get(RAND.nextInt(CAR_MODELS.size()));
        return createVehicle(model, 0.2f);
    }

    private void createObjects() {
        coordinateAxes = DebugService.get().createCoordinateAxes(app.getAssetManager(), 3f);
        coordinateAxes.setLocalTranslation(0, 0, 0);
        app.getRootNode().attachChild(coordinateAxes);

        var sphere = new Sphere(10, 10, 0.1f);
        var sphereGeo = new Geometry("selectedMarker", sphere);
        sphereGeo.setMaterial(MaterialFactory.get(app.getAssetManager()).createPlastic(ColorRGBA.Red, 0.0f));
        app.getRootNode().attachChild(sphereGeo);
        sphereGeo.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        selectedVehicleMarker = sphereGeo;
        selectedVehicleMarker.setCullHint(Spatial.CullHint.Always);

        vehiclesNode = new Node(VEHICLES);
        app.getRootNode().attachChild(vehiclesNode);

        VehicleControl first = null;
        VehicleControl last = null;
        List<VehicleControl> controls = new ArrayList<>();
        for (float start = 0; start < 360; start += 170) {
            var vehicle = createRandomVehicle();
            var control = new VehicleControl(9.75f, start, 0);
            controls.add(control);
            if (first == null) {
                first = control;
                last = control;
            } else {
                control.setPredecessor(last);
                last = control;
            }
            vehicle.addControl(control);
            vehiclesNode.attachChild(vehicle);
        }
        first.setPredecessor(last);
        LOG.info("Controls: {}", controls.size());
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