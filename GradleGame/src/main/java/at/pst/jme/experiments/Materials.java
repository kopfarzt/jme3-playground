package at.pst.jme.experiments;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.TangentBinormalGenerator;

import at.pst.jme.service.DebugService;
import at.pst.jme.service.MaterialService;

public class Materials extends SimpleApplication implements ActionListener {

	private static final float GRID_SIZE = 3;

	private boolean rotate = false;
	private Node autoRotate;
	private DebugService debugService;
	private MaterialService materialService;

	private enum Action {
		ROTATE
	}

	public static void main(String[] args) {
		Materials app = new Materials();
		// app.setShowSettings(false);
		app.start();
	}

	@Override
	public void simpleInitApp() {

		flyCam.setZoomSpeed(15);
		flyCam.setMoveSpeed(1);

		debugService = new DebugService(this);
		materialService = new MaterialService(this);

		AmbientLight ambientLight = new AmbientLight(ColorRGBA.DarkGray);
		rootNode.addLight(ambientLight);

		Vector3f lightDir = new Vector3f(-1, -1, -1);
		DirectionalLight directionalLight = new DirectionalLight(lightDir, ColorRGBA.White);
		rootNode.addLight(directionalLight);

		Node debugNode = new Node();
		debugNode.attachChild(debugService.createGrid(100, 100, 0.1f, true, ColorRGBA.DarkGray));
		debugNode.attachChild(debugService.createCoordinateAxis(new Vector3f(-5, 0, -5)));
		Node markers = new Node();

		markers.attachChild(debugService.createArrow(Vector3f.ZERO, lightDir, ColorRGBA.Yellow));
		markers.attachChild(debugService.createMarker(lightDir, 0.1f, ColorRGBA.Yellow, ""));
		debugNode.attachChild(markers);

		rootNode.attachChild(debugNode);

		cam.setLocation(new Vector3f(0, 5, 10));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

		Material red = materialService.createSimplePhongMaterial(ColorRGBA.Red);
		Material green = materialService.createSimplePhongMaterial(ColorRGBA.Green);
		Material blue = materialService.createSimplePhongMaterial(ColorRGBA.Blue);

		Material earthMaterial = createEarthMaterial();
		Material monkeyMaterial = createMonkeyMaterial();
		Material woodMaterial = createWoodMaterial();

		autoRotate = new Node();
		autoRotate.attachChild(gridSphere(-1, 0, -1, materialService.createWireframeMaterial(ColorRGBA.Magenta)));
		autoRotate.attachChild(gridSphere(0, 0, -1, earthMaterial));

		Geometry monkeySphere = gridSphere(1, 0, -1, monkeyMaterial);
		monkeySphere.getMesh().scaleTextureCoordinates(new Vector2f(5f, 5f));
		autoRotate.attachChild(monkeySphere);

		Geometry woodSphere = gridSphere(-1, 0, 0, woodMaterial);
		TangentBinormalGenerator.generate(woodSphere.getMesh());
		autoRotate.attachChild(woodSphere);

		autoRotate.attachChild(gridSphere(0, 0, 0, green));
		autoRotate.attachChild(gridSphere(1, 0, 0, blue));

		autoRotate.attachChild(gridSphere(-1, 0, 1, red));
		autoRotate.attachChild(gridSphere(0, 0, 1, green));
		autoRotate.attachChild(gridSphere(1, 0, 1, blue));

		rootNode.attachChild(autoRotate);

		initActionListener();
	}

	private Material createWoodMaterial() {
		Material material = new Material(getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", false);
		// material.setBoolean("BackfaceShadows", false);
		material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Wood/everytexture.com-stock-wood-texture-00156/everytexture.com-stock-wood-texture-00156-diffuse-2048.jpg"));
		material.setTexture("NormalMap", assetManager.loadTexture("Textures/Wood/everytexture.com-stock-wood-texture-00156/everytexture.com-stock-wood-texture-00156-normal-2048.jpg"));
		// material.setTexture("ParallaxMap", assetManager.loadTexture("Textures/Wood/Wood_026_SD/Wood_026_height.png"));
		// material.setTexture("SpecularMap", assetManager.loadTexture("Textures/Wood/Wood_026_SD/Wood_026_basecolor.jpg"));
		material.setColor("Specular", ColorRGBA.White);
		material.setFloat("Shininess", 128f);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	private Material createMonkeyMaterial() {
		Material material = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		Texture texture = getAssetManager().loadTexture("Textures/ColoredTex/Monkey.png");
		texture.setWrap(WrapMode.Repeat);
		material.setTexture("ColorMap", texture);
		material.setColor("Color", ColorRGBA.Blue);
		return material;
	}

	private Material createEarthMaterial() {
		Material material = new Material(getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", false);
		// material.setBoolean("BackfaceShadows", false);
		material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Sky/Earth/Earth.jpg"));
		material.setColor("Specular", ColorRGBA.White);
		// material.setTexture("SpecularMap", assetManager.loadTexture("Textures/Sky/Earth/Earth.jpg"));
		material.setFloat("Shininess", 128f);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	protected Geometry gridSphere(int x, int y, int z, Material material) {
		Sphere sphere = new Sphere(64, 64, 0.3f * GRID_SIZE);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry geometry = new Geometry(String.format("Sphere (%d, %d, %d)",  x, y, z), sphere);
		geometry.setMaterial(material);
		geometry.setLocalTranslation(x * GRID_SIZE, y * GRID_SIZE, z * GRID_SIZE);
		return geometry;
	}



	private void initActionListener() {
		inputManager.addMapping(Action.ROTATE.name(), new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, Action.ROTATE.name());
	}

	@Override
	public void simpleUpdate(float tpf) {
		if(rotate) {
			autoRotate.rotate(0, 0.05f * FastMath.DEG_TO_RAD, 0);
		}
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(isPressed == false) {
			Action action = Action.valueOf(name);
			switch(action) {
			case ROTATE:
				rotate = !rotate;
				break;
			default:
				break;
			}
		}
	}
}

