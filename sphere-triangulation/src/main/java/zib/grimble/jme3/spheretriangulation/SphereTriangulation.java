package zib.grimble.jme3.spheretriangulation;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.FastLightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.environment.generation.JobProgressListener;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Limits;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.materials.MaterialFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class SphereTriangulation extends SimpleApplication implements ActionListener {
    private static final Logger LOG = LoggerFactory.getLogger(SphereTriangulation.class);
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;

    private boolean postLightProbeInit = false;
    private JobProgressListener jobProgressListener;
    private MaterialFactory materialFactory;
    private long frame;
    private BulletAppState bulletAppState;
    private boolean physicsDebug;

    public static void main(String[] args) {
        var app = new SphereTriangulation();
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

    private void showSettings() {
        System.out.println();
        System.out.println("Renderer: " + renderer);
        System.out.println();
        System.out.println("App Settings");
        settings.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> System.out.format("%s: %s%n", e.getKey(), e.getValue()));
        System.out.println();
        System.out.println("Java Version: " + Runtime.version());
        EnumMap<Limits, Integer> limits = renderer.getLimits();
        System.out.println();
        System.out.println("Renderer Limits");
        limits.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey())).forEach(e -> System.out.format("%s: %s%n", e.getKey(), e.getValue()));
    }

    @Override
    public void simpleInitApp() {
        showSettings();
        initCamera();
        initKeys();
        setDisplayFps(false);
        setDisplayStatView(false);
        createPhysicSpace();
        createWorld();
        System.out.println("***********************************************************");
        System.out.println("If metal is not working, try to start application with GPU.");
        System.out.println("***********************************************************");
        System.out.println();
        System.out.println("Keys:");
        System.out.println();
        System.out.println(KeyAction.getUsage());
    }


    private void initCamera() {
        flyCam.setZoomSpeed(10);
        flyCam.setMoveSpeed(6);
        cam.setLocation(new Vector3f(0, 8, 20));
        cam.lookAt(new Vector3f(0, 2, 0), Vector3f.UNIT_Y);
    }

    private void initKeys() {
        LOG.info("Initializing keys");
        for (var action : KeyAction.values()) {
            inputManager.addMapping(action.toString(), new KeyTrigger((action.getKey())));
            inputManager.addListener(this, action.toString());
        }
    }

    private void createPhysicSpace() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0, -0.02f, 0));
        // bulletAppState.getPhysicsSpace().setAccuracy(1f / 120f);
        physicsDebug = false;
    }

    private void createWorld() {
        createGroundPlane();
        createSkybox();
        createLightsAndShadows();
        createLightProbe(() -> {
            postLightProbeInit = true;
        });
        System.out.println("Finished initializing");
    }

    private void createObjects() {
        materialFactory = MaterialFactory.get(assetManager);

        var massMat = materialFactory.createScuffedBluePlastic();
        var springMat = materialFactory.createPlastic(ColorRGBA.Red, 0f);
        var markerMat = materialFactory.createPlastic(ColorRGBA.Black, 0f);

        var m1 = Mass.createMass("mass-1", new Vector3f(0, 0, 6), 1.0f, massMat);
        var m2 = Mass.createMass("mass-2", new Vector3f(6, 0, 0), 1.0f, massMat);
        var m3 = Mass.createMass("mass-3", new Vector3f(6, 0, 6), 1.0f, massMat);
        var m4 = Mass.createMass("mass-4", new Vector3f(3, 6, 3), 1.0f, massMat);

        var massMap = new HashMap<String, Mass>();
        var springMap = new HashMap<String, Spring>();

        sub(m1, m2, m3, massMap, springMap, massMat, springMat, 1);
        sub(m2, m4, m3, massMap, springMap, massMat, springMat, 1);
        sub(m4, m1, m3, massMap, springMap, massMat, springMat, 1);
        sub(m1, m2, m4, massMap, springMap, massMat, springMat, 1);

        for (var mass : massMap.values()) {
            rootNode.attachChild(mass.getGeometry());
            bulletAppState.getPhysicsSpace().add(mass.getRigidBodyControl());
            LOG.debug("Added mass {} (physical mass: {})",
                    mass.getGeometry().getName(),
                    mass.getRigidBodyControl().getMass());
        }

        var totLen = 0f;
        for (var spring : springMap.values()) {
            rootNode.attachChild(spring.getGeometry());
            rootNode.addControl(spring);
            totLen += spring.getRestLength();
        }

        var avLen = totLen / springMap.size();
        for (var spring : springMap.values()) {
            spring.setRestLength(avLen);
        }
    }

    private void sub(Mass m1, Mass m2, Mass m3, HashMap<String, Mass> massMap, HashMap<String, Spring> springMap, Material massMat, Material springMat, int depth) {
        massMap.putIfAbsent(key(m1), m1);
        massMap.putIfAbsent(key(m2), m2);
        massMap.putIfAbsent(key(m3), m3);

        if (depth > 0) {
            var m12 = center(m1, m2, massMap, massMat);
            var m23 = center(m2, m3, massMap, massMat);
            var m13 = center(m1, m3, massMap, massMat);
            /*
                3

              13 23

            1  12    2
             */
            sub(m13, m23, m3, massMap, springMap, massMat, springMat, depth - 1);
            sub(m12, m23, m13, massMap, springMap, massMat, springMat, depth - 1);
            sub(m1, m12, m13, massMap, springMap, massMat, springMat, depth - 1);
            sub(m12, m2, m23, massMap, springMap, massMat, springMat, depth - 1);
        } else {
            springMap.putIfAbsent(key(m1, m2), Spring.createSpring(key(m1, m2), m1, m2, 3, 0.1f, 0.05f, springMat));
            springMap.putIfAbsent(key(m2, m3), Spring.createSpring(key(m2, m3), m2, m3, 3, 0.1f, 0.05f, springMat));
            springMap.putIfAbsent(key(m1, m3), Spring.createSpring(key(m1, m3), m1, m3, 3, 0.1f, 0.05f, springMat));
        }
    }

    private Mass center(Mass m1, Mass m2, HashMap<String, Mass> massMap, Material massMat) {
        final var key = key(m1, m2);
        var mass = massMap.get(key);
        if (mass == null) {
            final var m1loc = m1.getRigidBodyControl().getPhysicsLocation();
            final var m2loc = m2.getRigidBodyControl().getPhysicsLocation();

            mass = Mass.createMass(key,
                    m1loc.clone().interpolateLocal(m2loc, 0.5f),
                    1.0f,
                    massMat);

            massMap.put(key, mass);

        }
        return mass;
    }

    private String key(Mass m) {
        return m.getGeometry().getName();
    }

    private String key(Mass m1, Mass m2) {
        var k1 = key(m1);
        var k2 = key(m2);
        if (k1.compareTo(k2) < 0) {
            var h = k1;
            k1 = k2;
            k2 = h;
        }
        return "%s-%s".formatted(k1, k2);
    }

    private Spatial createGroundPlane() {
        var box = new Box(20, 0.01f, 20);

        var geometry = new Geometry("GroundPlane", box);

        geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff)));
        geometry.setLocalTranslation(0, -0.015f, 0);

        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        var rigidBodyControl = new RigidBodyControl(0);
        geometry.addControl(rigidBodyControl);
        rigidBodyControl.setCcdMotionThreshold(0.0001f);
        rigidBodyControl.setCcdSweptSphereRadius(0.0001f);
        rigidBodyControl.setRestitution(0.0001f);

        rootNode.attachChild(geometry);
        bulletAppState.getPhysicsSpace().add(rigidBodyControl);

        return geometry;
    }

    private void createSkybox() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "textures/sky/kloppenheim_06_puresky.jpg", SkyFactory.EnvMapType.EquirectMap));
    }

    private void createLightsAndShadows() {
        var ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(255, 255, 255, 0).mult(1f));
        rootNode.addLight(ambientLight);

        var directionalLight = new DirectionalLight(new Vector3f(1, -1, -1), ColorRGBA.White);
        rootNode.addLight(directionalLight);

        addShadowRenderer(directionalLight, 4096, 4);
        //addShadowFilter(directionalLight, 4096, 4);
        addBloomFilter();
    }

    private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
        var shadowRenderer = new DirectionalLightShadowRenderer(assetManager, shadowMapSize, nbSplits);
        shadowRenderer.setLight(light);
        viewPort.addProcessor(shadowRenderer);
    }

    private void addShadowFilter(DirectionalLight light, int shadowMapSize, int nbSplits) {
        var shadowFilter = new DirectionalLightShadowFilter(assetManager, shadowMapSize, nbSplits);
        shadowFilter.setLight(light);
        shadowFilter.setEnabled(true);
        var fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(shadowFilter);
        viewPort.addProcessor(fpp);
    }

    private void addBloomFilter() {
        var fpp = new FilterPostProcessor(assetManager);
        var bloomFilter = new BloomFilter(BloomFilter.GlowMode.Objects);
        bloomFilter.setBloomIntensity(2.5f);
        fpp.addFilter(bloomFilter);
        viewPort.addProcessor(fpp);

    }

    private void createLightProbe(Runnable runnable) {
        var environmentCamera = new EnvironmentCamera();
        stateManager.attach(environmentCamera);
        environmentCamera.initialize(stateManager, this);
        jobProgressListener = new JobProgressAdapter() {
            @Override
            public void done(Object result) {
                runnable.run();
            }
        };

        var lightProbe = FastLightProbeFactory.makeProbe(renderManager, assetManager, 256, Vector3f.ZERO, 1f, 100f, rootNode);
        jobProgressListener.done(null);
        // var lightProbe = LightProbeFactory.makeProbe(environmentCamera, rootNode, jobProgressListener);
        // objects outside of this radius will be black
        lightProbe.getArea().setRadius(20);
        lightProbe.setPosition(Vector3f.ZERO);
        rootNode.addLight(lightProbe);
    }

    private Material createShadedMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", color);
        material.setColor("Diffuse", color);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 64);
        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        return material;
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        var action = KeyAction.valueOf(name);
        System.out.printf("Key Action: %s Pressed: %b%n", name, isPressed);
        if (isPressed) {
            switch (action) {
                case TOGGLE_MOUSE:
                    inputManager.setCursorVisible(!inputManager.isCursorVisible());
                    break;
                case TOGGLE_PHYSICS_DEBUG:
                    physicsDebug = !physicsDebug;
                    bulletAppState.setDebugEnabled(physicsDebug);
                    break;
            }
        }

    }

    private enum KeyAction {
        TOGGLE_SHADOWS(KeyInput.KEY_F2),
        TOGGLE_MOUSE(KeyInput.KEY_SPACE),
        TOGGLE_ROTATE(KeyInput.KEY_R),
        TOGGLE_NORMALS(KeyInput.KEY_N),
        TOGGLE_TANGENTS(KeyInput.KEY_T),
        ROTATE_PLUS(KeyInput.KEY_P),
        ROTATE_MINUS(KeyInput.KEY_M),
        TOGGLE_PHYSICS_DEBUG(KeyInput.KEY_D);

        private final int key;
        private static final Map<Integer, String> KEY_MAP;

        static {
            KEY_MAP = Arrays.stream(KeyInput.class.getFields())
                    .filter(f -> f.getType() == int.class)
                    .collect(Collectors.toMap(f -> {
                        try {
                            return f.getInt(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }, Field::getName, (o, n) -> o));

        }

        KeyAction(int key) {
            this.key = key;
        }

        ;

        public int getKey() {
            return key;
        }

        public String getName() {
            return KEY_MAP.get(key);
        }

        public static String getUsage() {
            var stringWriter = new StringWriter();
            var writer = new PrintWriter(stringWriter);

            for (var action : values()) {
                writer.printf("%-14s %s%n", action.getName(), action);
            }

            return stringWriter.toString();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        frame++;
        if (postLightProbeInit) {
            postLightProbeInit = false;
            createObjects();
        }
    }
}
