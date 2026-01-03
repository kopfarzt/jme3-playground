package at.pst.jme.hello;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;

public class HelloPicking extends SimpleApplication implements ActionListener {

	private Node shootables;
	private Node markersNode;
	private List<Spatial> markers;

	private static final Random random = new Random();

	public static void main(String[] args) {
		HelloPicking app = new HelloPicking();
		app.setShowSettings(false);
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1024, 768);
		settings.setVSync(false);
		settings.setSamples(4);
		app.setSettings(settings);
		app.start();
	}

	@Override
	public void simpleInitApp() {

		setDisplayStatView(false);
		flyCam.setZoomSpeed(15);
		flyCam.setMoveSpeed(10);

		initCrossHair();

		shootables = initShootables();
		rootNode.attachChild(shootables);

		markersNode = initMarkers();
		rootNode.attachChild(markersNode);

		initKeys();

		DirectionalLight sun = new DirectionalLight(new Vector3f(1, 0, -2), ColorRGBA.White);
		rootNode.addLight(sun);

	}

	private void initKeys() {
		inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, "Shoot");
	}

	private void initCrossHair() {
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText text = new BitmapText(guiFont);
		text.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		text.setText("+");
		text.setLocalTranslation(
				(settings.getWidth() - text.getLineWidth()) / 2f,
				(settings.getHeight() + text.getLineHeight()) / 2f,
				0f);
		guiNode.attachChild(text);
	}

	private Node initShootables() {
		Node node = new Node("Shootables");

		for(int i=0; i<15; i++) {
			Sphere sphere = new Sphere(70, 70, random.nextFloat(0.5f, 1f));
			Geometry geometry = new Geometry("Sphere-" + i, sphere);
			geometry.setMaterial(phong(ColorRGBA.randomColor()));
			geometry.setLocalTranslation(random.nextFloat(-5f, 5f), random.nextFloat(-5f, 5f), random.nextFloat(-5f, 5f));
			node.attachChild(geometry);
		}

		Box box = new Box(15f, 0.1f, 15f);
		Geometry geometry = new Geometry("Box", box);
		geometry.setMaterial(unshaded(ColorRGBA.DarkGray));
		geometry.setLocalTranslation(0, -5, -5);
		node.attachChild(geometry);


		Spatial golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
		golem.scale(0.5f);
		golem.setLocalTranslation(-1f,  1.5f,  0.6f);
		node.attachChild(golem);

		AnimComposer golemComposer = golem.getControl(AnimComposer.class);
		golemComposer.setCurrentAction("Walk");

		return node;
	}

	private Node initMarkers() {
		Node node = new Node("Markers");
		markers = new ArrayList<>();
		for(int i=0; i<15; i++) {
			Sphere sphere = new Sphere(30, 30, 0.05f);
			Geometry geometry = new Geometry("Marker-" + i, sphere);
			geometry.setMaterial(phong(ColorRGBA.Red));
			geometry.setLocalTranslation(random.nextFloat(-5f, 5f), random.nextFloat(-5f, 5f), random.nextFloat(-5f, 5f));
			markers.add(geometry);
		}

		return node;
	}

	protected Material unshaded(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", color);
		return material;
	}

	protected Material phong(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Specular", ColorRGBA.White);
		material.setColor("Diffuse", color);
		material.setColor("Ambient", color);
		material.setFloat("Shininess", 64f);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}




	@Override
	public void simpleUpdate(float tpf) {
		//TODO: add update code
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(name.equals("Shoot") && !isPressed) {
			System.out.println("Boom");

			markersNode.detachAllChildren();

			CollisionResults collisions = new CollisionResults();

			Ray ray = new Ray(cam.getLocation(), cam.getDirection());

			shootables.collideWith(ray, collisions);

			System.out.format("Collisions: %d%n", collisions.size());

			int i = 0;
			for (CollisionResult coll : collisions) {
				if(i < markers.size()) {
					Spatial marker = markers.get(i);

					marker.setLocalTranslation(coll.getContactPoint());
					markersNode.attachChild(marker);
				}
				i++;
			}

		}
	}
}

