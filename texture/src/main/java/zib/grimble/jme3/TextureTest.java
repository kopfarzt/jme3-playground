package zib.grimble.jme3;

import com.jme3.app.SimpleApplication;
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
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Limits;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;
import com.jme3.util.TangentBinormalGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.SuperSphere;
import zib.grimble.jme3.iterators.InterpolatingIterator;
import zib.grimble.jme3.materials.MaterialFactory;
import zib.grimble.jme3.service.DebugService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TextureTest extends SimpleApplication implements ActionListener {
    private static final Logger LOG = LoggerFactory.getLogger(TextureTest.class);
    private static final Random RND = new Random();

    private static final Vector3f NO_ROTATE = Vector3f.ZERO;
    private static final Vector3f STD_ROTATE = new Vector3f(0.0001f, 0.0013f, 0.0016f);
    private static final Vector3f INC_ROTATE = new Vector3f(0.0001f, 0.00065f, 0.0008f);
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 900;
    private Vector3f CUR_ROTATE = STD_ROTATE;
    private boolean rotating = true;

    private boolean postLightProbeInit = false;

    private Map<String, TexturedObject> texturedObjects = new HashMap<>();
    private Map<String, TexturedObject> rotatingObjects = new HashMap<>();
    private Map<String, TexturedObject> normalObjects = new HashMap<>();
    private Map<String, TexturedObject> tangentObjects = new HashMap<>();

    private MaterialFactory materialFactory;
    private Material normalsMat;
    private Material tangentsMat;
    private JobProgressListener jobProgressListener;
    private long frame = 0;

    private enum KeyAction {
        TOGGLE_SHADOWS(KeyInput.KEY_F2),
        TOGGLE_MOUSE(KeyInput.KEY_SPACE),
        TOGGLE_ROTATE(KeyInput.KEY_R),
        TOGGLE_NORMALS(KeyInput.KEY_N),
        TOGGLE_TANGENTS(KeyInput.KEY_T),
        ROTATE_PLUS(KeyInput.KEY_P),
        ROTATE_MINUS(KeyInput.KEY_M),
        ;

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

    public static void main(String[] args) {
        var app = new TextureTest();
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

    private void createWorld() {
        createGroundPlane();
        createSkybox();
        createLightsAndShadows();
        createLightProbe(() -> {
            postLightProbeInit = true;
        });
        System.out.println("Finished initializing");
    }

    private void createSkybox() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "textures/sky/kloppenheim_06_puresky.jpg", EnvMapType.EquirectMap));
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
        lightProbe.setPosition(v3(0, 0, 0));
        rootNode.addLight(lightProbe);
    }

    private void createObjects() {
        LOG.debug("Creating objects");
        var single = false;

        materialFactory = MaterialFactory.get(assetManager);

        var colorPurple = ColorRGBA.fromRGBA255(0x6c, 0x25, 0xbe, 0xff);
        var colorYellow = ColorRGBA.fromRGBA255(0xbe, 0xa9, 0x25, 0xff);

        var chessboard = createTexturedMaterial(Map.of(
                "DiffuseMap", createChessboard(1024, 1024, 64, 64, colorPurple, colorYellow)
        ));

        var blue = assetManager.loadMaterial("materials/metal-painted-blue-scratched.j3m");
        var pbr = createPbr(m -> m.setFloat("Roughness", 0f));
        var metallicChessboard = createPbr(m -> {
                    m.setTexture("RoughnessMap", createChessboard(1024, 1024, 32, 32, ColorRGBA.DarkGray, ColorRGBA.Black));
                    m.setTexture("BaseColorMap", createChessboard(1024, 1024, 128, 128, colorPurple, colorYellow));
                }
        );

        var lig = assetManager.loadMaterial("materials/lighting-painted-blue-scratched.j3m");

        normalsMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        normalsMat.setColor("Color", ColorRGBA.Magenta);
        normalsMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);

        tangentsMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tangentsMat.setColor("Color", ColorRGBA.Cyan);
        tangentsMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);

        float height = 1.5f;
        float half = 7;
        var grid = new XZGrid("object-%.1f-%.1f", v3(-half, height, -half), v3(half, height, half), 6, 6);

        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), createShadedMaterial(ColorRGBA.Red), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject("Chess 1", grid.current(), chessboard, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject("Chess 2", grid.current(), chessboard, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            var metChessGeometry = addTexturedObject(grid.currentName(), grid.current(), metallicChessboard, STD_ROTATE, true);
            logGeometry(metChessGeometry);
        }
        grid.next();

        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createMetal(ColorRGBA.fromRGBA255(168, 78, 18, 255), 0), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            var blueGeometry = addTexturedObject(grid.currentName(), grid.current(), blue, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), pbr, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), lig, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createScratchedPaintMetal(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), assetManager.loadMaterial("materials/metal-scratched.j3m"), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            final var galvanizedMetal = materialFactory.createGalvanizedMetal();
            var texture = assetManager.loadTexture("materials/glow.png");
            texture.setWrap(WrapMode.Repeat);

            galvanizedMetal.setTexture("EmissiveMap", texture);
            addTexturedObject(grid.currentName(), grid.current(), galvanizedMetal, STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createPolishedScratchesMetal(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createRustyMetal(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createRustyBumpedMetal(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createScratchedAluminiumMetal(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createScuffedBluePlastic(), STD_ROTATE, true);
        }
        grid.next();
        if (!single) {
            addTexturedObject(grid.currentName(), grid.current(), materialFactory.createPlastic(ColorRGBA.fromRGBA255(168, 78, 18, 255), 0), STD_ROTATE, true);
        }

        if (!single) {

            var colIt = new InterpolatingIterator(0f, 1f, 20);

            while (colIt.hasNext()) {
                var col = new ColorRGBA();
                var hsb = Color.getHSBColor(colIt.next(), 1f, 1f).getRGBColorComponents(null);
                col.setAsSrgb(hsb[0], hsb[1], hsb[2], 1f);
                if (colIt.hasNext()) {
                    grid.next();
                    addTexturedObject(grid.currentName(), grid.current(), materialFactory.createMetal(col, 0), STD_ROTATE, true);
                }
            }
        }
    }

    private void logGeometry(Geometry geometry) {
        LOG.info("Logging spatial: {}", geometry);
        var mesh = geometry.getMesh();
        var buffers = mesh.getBuffers();
        for (var buffer : buffers) {
            var vbuf = buffer.getValue();
            LOG.info("Found buffer '{}', format: {} elements: {} components: {} type: {} usage: {}", vbuf.getName(), vbuf.getFormat(), vbuf.getNumElements(), vbuf.getNumComponents(), vbuf.getBufferType(), vbuf.getUsage());
        }

        LOG.info("Coordinate buffers:");
        LOG.info(DebugService.get().buffersToString(mesh, VertexBuffer.Type.Position, VertexBuffer.Type.TexCoord, VertexBuffer.Type.Normal, VertexBuffer.Type.Tangent));
        LOG.info("Index buffers:");
        LOG.info(DebugService.get().buffersToString(mesh, VertexBuffer.Type.Index));
    }


    private Material createBlue() {
        var mat = assetManager.loadMaterial("materials/metal-painted-blue-scratched.j3m");
        var normalMap = assetManager.loadTexture("textures/metal/scratchedpaint/metal_0015_normal_directx_1k.png");
        normalMap.setWrap(WrapMode.Repeat);
        normalMap.setMinFilter(Texture.MinFilter.Trilinear);
        normalMap.setMagFilter(Texture.MagFilter.Bilinear);
        normalMap.setAnisotropicFilter(8);
        //normalMap.setImageType(Texture.Type.NormalMap); // Wichtig!
        // mat.setTexture("NormalMap", normalMap);
        return mat;
    }

    private Material createPbr(Consumer<Material> consumer) {
        var material = new Material(assetManager, "Common/MatDefs/Light/PBRLighting.j3md");
        consumer.accept(material);
        return material;
    }

    private Spatial createGroundPlane() {
        Box box = new Box(20, 0.01f, 20);

        Geometry geometry = new Geometry("GroundPlane", box);

        geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff)));
        geometry.setLocalTranslation(0, -0.015f, 0);

        geometry.setShadowMode(ShadowMode.Receive);

        rootNode.attachChild(geometry);

        return geometry;
    }

    private Material createShadedMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", color);
        material.setColor("Diffuse", color);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 64);
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        return material;
    }

    private Geometry addTexturedObject(String name, Vector3f translation, Material material, Vector3f rotate, boolean normalize) {
        boolean COMPUTE_NORMALS = true;
        boolean COMPUTE_TANGENTS = true;

//        Mesh mesh = new ParameterizedSurfaceGrid(
//                new MeshSphere(1f),
//                null, 30, 15, COMPUTE_NORMALS);
        Mesh mesh = new ParameterizedSurfaceGrid(
                new SuperSphere(1f, 0.6f, 0.6f),
                null, 50, 25, COMPUTE_NORMALS, normalize);
//              null, 25, 15, COMPUTE_NORMALS, name.equals("Chess 2"));
//        Mesh mesh = new ParameterizedSurfaceGrid(
//                new SuperSphere(1f, 0.6f, 0.6f),
//                null, 4, 3, COMPUTE_NORMALS);

        var geometry = new Geometry(name, mesh);
        geometry.setLocalTranslation(translation);
        geometry.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
        geometry.setMaterial(material);

        final var texturedObject = new TexturedObject(geometry, rotate);
        texturedObjects.put(geometry.getName(), texturedObject);
        rotatingObjects.put(geometry.getName(), texturedObject);

        rootNode.attachChild(geometry);
        geometry.setShadowMode(ShadowMode.CastAndReceive);

        if (COMPUTE_NORMALS) {
            var normals = TangentBinormalGenerator.genNormalLines(mesh, 0.25f);
            var normalsGeo = new Geometry(name, normals);
            normalsGeo.setLocalTranslation(translation);
            normalsGeo.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
            normalsGeo.setMaterial(normalsMat);
            var normalObject = new TexturedObject(normalsGeo, rotate);
            texturedObjects.put(geometry.getName() + " Normals", normalObject);
            rotatingObjects.put(geometry.getName() + " Normals", normalObject);
            normalObjects.put(geometry.getName() + " Normals", normalObject);
            rootNode.attachChild(normalsGeo);
            normalsGeo.setShadowMode(ShadowMode.CastAndReceive);
            normalObject.toggleVisbility();
        }

        // For PBRLighting, Tangents are needed, see: https://github.com/jMonkeyEngine/jmonkeyengine/issues/1903
        if (COMPUTE_TANGENTS) {
            // MikktspaceTangentGenerator.generate(geometry);
            var tangents = TangentBinormalGenerator.genTbnLines(mesh, 0.25f);
            var tangentsGeo = new Geometry(name, tangents);
            tangentsGeo.setLocalTranslation(translation);
            tangentsGeo.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
            tangentsGeo.setMaterial(tangentsMat);
            var tangentObject = new TexturedObject(tangentsGeo, rotate);
            texturedObjects.put(geometry.getName() + " Tangents", tangentObject);
            rotatingObjects.put(geometry.getName() + " Tangents", tangentObject);
            tangentObjects.put(geometry.getName() + " TangentsNormals", tangentObject);
            rootNode.attachChild(tangentsGeo);
            tangentsGeo.setShadowMode(ShadowMode.CastAndReceive);
            tangentObject.toggleVisbility();
        }

        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);

        return geometry;
    }

    private Material createTexturedMaterial(Map<String, Texture> textures) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");

        if (textures != null) {
            for (Entry<String, Texture> entry : textures.entrySet()) {
                material.setTexture(entry.getKey(), entry.getValue());
            }
        }

        material.setFloat("Shininess", 64);
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        return material;
    }

    private Texture2D createSingleColor(ColorRGBA color) {
        Texture2D texture = materialFactory.createTexture(1, 1, BufferedImage.TYPE_INT_ARGB, g -> {
            g.setColor(colorRgbaToColor(color));
            g.fillRect(0, 0, 1, 1);
        });
        texture.setWrap(WrapMode.Repeat);
        return texture;
    }

    private Texture createChessboard(int totalWidth, int totalHeight, int rectWidth, int rectHeight, ColorRGBA back, ColorRGBA front) {
        Texture2D texture = materialFactory.createTexture(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB, g -> {
            g.setColor(colorRgbaToColor(back));
            g.fillRect(0, 0, totalWidth, totalWidth);

            g.setColor(colorRgbaToColor(front));

            for (int y = 0, l = 0; y < totalHeight; y += rectHeight, l++) {
                for (int x = (l & 1) == 1 ? rectWidth : 0; x < totalWidth; x += 2 * rectWidth) {
                    g.fillRect(x, y, rectWidth, rectHeight);
                }
            }
        });
        texture.setWrap(WrapMode.Repeat);
        return texture;
    }

    private Color colorRgbaToColor(ColorRGBA rgba) {
        return new Color(rgba.r, rgba.g, rgba.b, rgba.a);
    }

    private Vector3f v3(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private static class TexturedObject {
        private Spatial.CullHint cullHint = Spatial.CullHint.Always;
        private Spatial spatial;
        private Vector3f rotate = NO_ROTATE;

        public TexturedObject(Spatial spatial) {
            this.spatial = spatial;
        }

        public TexturedObject(Spatial spatial, Vector3f rotate) {
            this.spatial = spatial;
            this.rotate = rotate;
        }

        public boolean isRotating() {
            return rotate != NO_ROTATE;
        }

        public void toggleVisbility() {
            var cullHint = spatial.getCullHint();
            spatial.setCullHint(this.cullHint);
            this.cullHint = cullHint;
        }

        public Spatial getSpatial() {
            return spatial;
        }

        public Vector3f getRotate() {
            return rotate;
        }

        public void setRotate(Vector3f rotate) {
            this.rotate = rotate;
        }

        public boolean rotate() {
            if (isRotating()) {
                spatial.rotate(rotate.x, rotate.y, rotate.z);
                return true;
            }
            return false;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        frame++;
        if (postLightProbeInit) {
            postLightProbeInit = false;
            createObjects();
        } else {
            for (var rotatingObject : rotatingObjects.values()) {
                rotatingObject.rotate();
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
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
                case TOGGLE_SHADOWS:
                    System.out.println("Toggle Shadows");
                    for (var texturedObject : texturedObjects.values()) {
                        var spatial = texturedObject.getSpatial();
                        var shadowMode = spatial.getShadowMode();
                        shadowMode = shadowMode == ShadowMode.CastAndReceive ?
                                ShadowMode.Off : ShadowMode.CastAndReceive;
                        spatial.setShadowMode(shadowMode);
                    }
                    break;
                case TOGGLE_ROTATE:
                    if (rotating) {
                        setRotations(NO_ROTATE);
                    } else {
                        setRotations(CUR_ROTATE);
                    }
                    rotating = !rotating;
                    break;
                case TOGGLE_NORMALS:
                    normalObjects.values().forEach(TexturedObject::toggleVisbility);
                    break;
                case TOGGLE_TANGENTS:
                    tangentObjects.values().forEach(TexturedObject::toggleVisbility);
                    break;
                case ROTATE_PLUS:
                    CUR_ROTATE = CUR_ROTATE.add(INC_ROTATE);
                    setRotations(CUR_ROTATE);
                    break;
                case ROTATE_MINUS:
                    CUR_ROTATE = CUR_ROTATE.subtract(INC_ROTATE);
                    setRotations(CUR_ROTATE);
                    break;
            }
        }
    }

    private void setRotations(Vector3f rotate) {
        rotatingObjects.values().forEach(r -> r.setRotate(rotate));
    }
}
