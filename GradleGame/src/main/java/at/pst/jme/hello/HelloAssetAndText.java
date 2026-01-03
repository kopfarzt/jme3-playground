package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class HelloAssetAndText extends SimpleApplication {

	public static void main(String[] args) {
		HelloAssetAndText app = new HelloAssetAndText();
		app.start();
	}

	@Override
	public void simpleInitApp() {

		flyCam.setZoomSpeed(-30);
		flyCam.setMoveSpeed(10);

		rootNode.attachChild(createTeapot());

		rootNode.attachChild(createWall());

		rootNode.attachChild(createNinja());

		rootNode.attachChild(createTown());

		guiNode.detachAllChildren();
		guiNode.attachChild(createText("Hello World."));

		DirectionalLight sun = new DirectionalLight(new Vector3f(-0.1f, -0.7f, -1.0f), ColorRGBA.White);
		rootNode.addLight(sun);
	}

	private Spatial createTeapot() {
		Spatial model = assetManager.loadModel("Models/Teapot/Teapot.obj");
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Diffuse", ColorRGBA.Brown);
		material.setColor("Specular", ColorRGBA.White);
		material.setFloat("Shininess", 128);
		model.setMaterial(material);
		return model;
	}

	private Spatial createWall() {
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setTexture("ColorMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));

		Box box = new Box(2.5f, 2.5f, 1);

		Geometry geometry = new Geometry("Wall", box);
		geometry.setLocalTranslation(2, -2.5f, 0);
		geometry.setMaterial(material);

		return geometry;
	}

	private Spatial createNinja() {
		Spatial model = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
		model.scale(0.05f);
		model.rotate(0, -3, 0);
		model.setLocalTranslation(0,  -5,  -2);
		return model;
	}

	private Spatial createTown() {
		Spatial model = assetManager.loadModel("Scenes/town/main.scene");
		model.setLocalTranslation(0, -5.2f, 0);
		model.setLocalScale(2);
		return model;
	}

	private Spatial createText(String string) {
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText bitmapText = new BitmapText(guiFont);
		bitmapText.setSize(guiFont.getCharSet().getRenderedSize());
		bitmapText.setText(string);
		bitmapText.setLocalTranslation(300, bitmapText.getLineHeight(), 0);

		return bitmapText;
	}

	@Override
	public void simpleUpdate(float tpf) {
		//TODO: add update code
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}
}

