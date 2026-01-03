package zib.grimble.jme3;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.*;
import zib.grimble.jme3.materials.MaterialFactory;
import zib.grimble.jme3.service.DebugService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class SurfaceTest extends SimpleApplication implements ActionListener {
    private static final Logger LOG = LoggerFactory.getLogger(SurfaceTest.class);
    public static final String LIGHTING = "Common/MatDefs/Light/Lighting.j3md";
    public static final String SHOW_NORMALS = "Common/MatDefs/Misc/ShowNormals.j3md";
    public static final String UNSHADED = "Common/MatDefs/Misc/Unshaded.j3md";

    public static void main(String[] args) {
        var app = new SurfaceTest();
        app.setShowSettings(true);
        app.setSettings(createSettings());
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initCamera();
        //		setDisplayFps(false);
        setDisplayStatView(false);
        createWorld();
        rootNode.attachChild(DebugService.get().createCoordinateAxes(assetManager, new Vector3f(5f, 0.0f, 0.0f)));
    }

    private void initCamera() {
        flyCam.setZoomSpeed(10);
        flyCam.setMoveSpeed(10);
        cam.setLocation(new Vector3f(0, 10, 30));
        cam.lookAt(new Vector3f(0, 5, 0), Vector3f.UNIT_Y);
    }

    private void createWorld() {
        createObjects();
        createLightsAndShadows();
    }

    private void createObjects() {
        rootNode.attachChild(createGroundPlane());


        //		var chess1 = ColorRGBA.fromRGBA255(0xF7, 0xC5, 0x6E, 0xFF);
        //		var chess2 = ColorRGBA.fromRGBA255(0x89, 0xDC, 0xE0, 0xFF);
        var chess1 = ColorRGBA.fromRGBA255(0xFF, 0x00, 0x00, 0xFF);
        var chess2 = ColorRGBA.fromRGBA255(0x00, 0x00, 0xFF, 0xFF);
        var chessboardTexture = chessboard(1024, 1024, 64, 64, chess1, ColorRGBA.White, chess2, ColorRGBA.Black);
        var earthTexture = assetManager.loadTexture("Textures/Sky/Earth/Earth.jpg");

        var yellowMaterial = shadedMaterial(ColorRGBA.fromRGBA255(0xFF, 0x66, 0x00, 0xff));
        var redMaterial = shadedMaterial(ColorRGBA.fromRGBA255(0xff, 0x00, 0x00, 0xff));
        var chessboard = texturedMaterial(Map.of(
                "DiffuseMap", chessboardTexture
        ));
        var earth = new Material(assetManager, LIGHTING);
        earth.setTexture("DiffuseMap", earthTexture);

        var materialFactory = MaterialFactory.get(assetManager);
        var coolMaterial = materialFactory.createPolishedScratchesMetal();
        var unshaded = unshadedMaterial(ColorRGBA.Magenta);


        earth.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);


        var image = new Material(assetManager, LIGHTING);
        image.setTexture("DiffuseMap", chessboardTexture);

        var font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        float height = 2.5f;
        float half = 12;
        var grid = new XZGrid("object-%.1f-%.1f", new Vector3f(-half, height, -half), new Vector3f(half, height, half), 4, 4);

        attachTexturedObject(rootNode, "Mesh Spiral Column",
                new ParameterizedSurfaceGrid(
                        new SpiralColumn(0.1f, 1f, 1800 * FastMath.DEG_TO_RAD, 10),
                        null, 400, 32, true, true),
                grid.current(),
                yellowMaterial, font
        );

        grid.next();

        attachTexturedObject(rootNode, "Mesh Spiral",
                new ParameterizedSurfaceGrid(
                        new Spiral(2, 0.5f, 720 * FastMath.DEG_TO_RAD, 2),
                        null, 400, 32, true, true),
                grid.current(),
                yellowMaterial, font
        );

        grid.next();

        attachTexturedObject(rootNode, "Mesh Waves",
                new ParameterizedSurfaceGrid(
                        new Waves(3),
                        null, 400, 400, true, true),
                grid.current(),
                yellowMaterial, font
        );


        // new line
        grid.next();

        attachTexturedObject(rootNode, "Interpolated Mesh Spiral",
                new ParameterizedSurfaceGrid(
                        new InterpolateFunctionsV(
                                new Spiral(2, 0.5f, 90 * FastMath.DEG_TO_RAD, 2),
                                new MeshSphere(2)),
                        null, 400, 32, true, true),
                grid.current(),
                yellowMaterial, font
        );


        grid.next();

        var sphere = new Sphere(32, 32, 2);
        attachTexturedObject(rootNode, "Sphere", sphere, grid.current(), chessboard, font);

        grid.next();

        attachTexturedObject(rootNode, "Mesh Sphere",
                new ParameterizedSurfaceGrid(
                        new MeshSphere(2),
                        null, 50, 25, true, true),
                grid.current(),
                chessboard, font
        );

        grid.next();

        attachTexturedObject(rootNode, "Material",
                new ParameterizedSurfaceGrid(
                        new MeshSphere(2),
                        null, 50, 25, false, true),
                grid.current(),
                coolMaterial, font
        );


        grid.nextLine();

        attachTexturedObject(rootNode, "Sphere", sphere, grid.current(), earth, font);
//        attachTexturedObject(rootNode, null,
//                TangentBinormalGenerator.genNormalLines(sphere, 0.25f),
//                grid.current(),
//                unshaded, font
//        );

        grid.next();

        var meshSphere = new ParameterizedSurfaceGrid(
                new MeshSphere(2),
                null, 50, 25, true, true);

        attachTexturedObject(rootNode, "Mesh Sphere",
                meshSphere,
                grid.current(),
                earth, font
        );
//        attachTexturedObject(rootNode, null,
//                TangentBinormalGenerator.genNormalLines(
//                        meshSphere, 0.5f),
//                grid.current(),
//                unshaded, font
//        );

        grid.next();


        attachTexturedObject(rootNode, "Quad",
                new Quad(4, 4),
                grid.current(),
                image, font
        );

        grid.nextLine();

        attachTexturedObject(rootNode, "SuperSphere 0.3",
                new ParameterizedSurfaceGrid(
                        new SuperSphere(2, 0.3f, 0.3f),
                        null, 50, 25, true, true),
                grid.current(),
                yellowMaterial, font
        );

        grid.next();

        attachTexturedObject(rootNode, "SuperSphere 1.5",
                new ParameterizedSurfaceGrid(
                        new SuperSphere(2, 1.5f, 1.5f),
                        null, 50, 25, true, true),
                grid.current(),
                yellowMaterial, font
        );

        grid.next();

        attachTexturedObject(rootNode, "SuperSphere 2.5",
                new ParameterizedSurfaceGrid(
                        new SuperSphere(2, 2.5f, 2.5f),
                        null, 50, 25, true, true),
                grid.current(),
                yellowMaterial, font
        );

        grid.next();

        attachTexturedObject(rootNode, "SuperSphere 0.5 1.5",
                new ParameterizedSurfaceGrid(
                        new SuperSphere(2, 0.5f, 1.5f),
                        null, 50, 25, true, true),
                grid.current(),
                yellowMaterial, font
        );

        rootNode.setShadowMode(ShadowMode.CastAndReceive);
    }

    private Spatial createGroundPlane() {
        Box box = new Box(20, 0.01f, 40);

        Geometry geometry = new Geometry("GroundPlane", box);

        var material = shadedMaterial(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff));

        geometry.setMaterial(material);
        geometry.setLocalTranslation(0, -0.015f, 0);

        return geometry;
    }

    private static AppSettings createSettings() {
        var appSettings = new AppSettings(true);
        appSettings.setResolution(1600, 1000);
        appSettings.setRenderer(AppSettings.LWJGL_OPENGL45);
        appSettings.setSamples(4);
        return appSettings;
    }

    private Spatial attachTexturedObject(Node rootNode, String name, Mesh mesh, Vector3f pos, Material material, BitmapFont font) {
        Geometry geometry = new Geometry(name, mesh);
        geometry.setLocalTranslation(pos.x, pos.y, pos.z);
        // geometry.getLocalRotation().fromAngleAxis(90f * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);
        geometry.setMaterial(material);

        rootNode.attachChild(geometry);

        if (name != null) {
            var label = new BitmapText(font);
            label.setText(name);
            label.setSize(0.5f);
            label.setLocalTranslation(pos.x, pos.y, pos.z + 3);
            rootNode.attachChild(label);
        }

        return geometry;
    }

    private Material unshadedMaterial(ColorRGBA color) {
        var material = new Material(assetManager, UNSHADED);
        material.setColor("Color", color);
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);

        // material.getAdditionalRenderState().setWireframe(true);
        return material;
    }

    private Material shadedMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, LIGHTING);
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", ColorRGBA.White.mult(0.2f));
        material.setColor("Diffuse", color);
        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 64);
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);

        // material.getAdditionalRenderState().setWireframe(true);
        return material;
    }

    private Material texturedMaterial(Map<String, Texture> textures) {
        Material material = new Material(assetManager, LIGHTING);

        // material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", ColorRGBA.White.mult(0.5f));
        //		material.setColor("Specular", ColorRGBA.White);
        // material.setFloat("Shininess", 64);

        if (textures != null) {
            for (Entry<String, Texture> entry : textures.entrySet()) {
                material.setTexture(entry.getKey(), entry.getValue());
            }
        }

        material.setColor("Specular", ColorRGBA.White);
        material.setFloat("Shininess", 64);
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
        return material;
    }

    private Texture chessboard(int totalWidth, int totalHeight, int rectWidth, int rectHeight, ColorRGBA back1, ColorRGBA back2, ColorRGBA front1, ColorRGBA front2) {
        return texture(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB, g -> {
            //			g.setColor(colorRgbaToColor(back));
            //			g.fillRect(0, 0, totalWidth, totalWidth);


            var numX = totalWidth / rectWidth;
            var addX = numX > 1 ? 1.0f / (numX - 1) : 1;
            var numY = totalHeight / rectHeight;
            var addY = numY > 1 ? 1.0f / (numY - 1) : 1;

            var facY = 0.0f;
            for (int y = 0, line = 0; y < totalHeight; y += rectHeight, line++, facY += addY) {
                var facX = 0.0f;
                var foreground = (line & 1) == 1;
                for (int x = 0; x < totalWidth; x += rectWidth, facX += addX) {

                    var color = foreground ?
                            new ColorRGBA().interpolateLocal(front1, front2, facX) :
                            new ColorRGBA().interpolateLocal(back1, back2, facY);
                    g.setColor(colorRgbaToColor(color));
                    g.fillRect(x, y, rectWidth, rectHeight);
                    foreground = !foreground;
                }
            }
        });
    }

    private Texture standardNormalMap(int totalWidth, int totalHeight) {
        return texture(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB, g -> {
            g.setColor(new Color(128, 128, 255));
            g.fillRect(0, 0, totalWidth, totalWidth);
        });
    }

    private Texture texture(int totalWidth, int totalHeight, int bufferedImageType, Consumer<Graphics2D> creator) {
        BufferedImage img = new BufferedImage(totalWidth, totalHeight, bufferedImageType);
        Graphics2D g = (Graphics2D) img.getGraphics();
        creator.accept(g);
        return new Texture2D(new AWTLoader().load(img, true));
        // return assetManager.loadTexture("Textures/Sky/Earth/Earth.jpg");
        // return assetManager.loadTexture("Textures/ColoredTex/Monkey.png");
    }

    private Color colorRgbaToColor(ColorRGBA rgba) {
        return new Color(rgba.r, rgba.g, rgba.b, rgba.a);
    }

    private void createLightsAndShadows() {
        var ambientLight = new AmbientLight(ColorRGBA.White.mult(0.3f));
        rootNode.addLight(ambientLight);

        var directionalLight = new DirectionalLight(new Vector3f(1f, -1f, -1f).normalizeLocal(), ColorRGBA.White.mult(1.5f));
        rootNode.addLight(directionalLight);

        // addShadowRenderer(directionalLight, 4096, 1);
        addShadowFilter(directionalLight, 4096, 3);
    }

    private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
        DirectionalLightShadowRenderer shadowRenderer = new DirectionalLightShadowRenderer(assetManager, shadowMapSize, nbSplits);
        shadowRenderer.setLight(light);
        viewPort.addProcessor(shadowRenderer);
    }

    private void addShadowFilter(DirectionalLight light, int shadowMapSize, int nbSplits) {
        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, shadowMapSize, nbSplits);
        shadowFilter.setLight(light);
        shadowFilter.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(shadowFilter);
        viewPort.addProcessor(fpp);
    }


    public class Waves extends ParameterizedSurface {

        private float width;

        public Waves(float width) {
            this.width = width;
        }

        @Override
        public Vector3f get(Vector2f param) {
            var vector = new Vector3f();
            vector.x = -width / 2 + param.x * width;
            vector.z = -width / 2 + param.y * width;
            float d = FastMath.sqr(vector.x * vector.x + vector.z * vector.z);
            vector.y = -2 / (d + FastMath.PI) * FastMath.sin(2 * d);
            return vector;
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
    }
}
