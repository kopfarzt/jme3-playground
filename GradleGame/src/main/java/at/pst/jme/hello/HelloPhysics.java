package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class HelloPhysics extends SimpleApplication implements ActionListener {

	private BulletAppState bulletAppState;

	private enum Action {
		SHOOT;
	}

	public static void main(String[] args) {
		HelloPhysics app = new HelloPhysics();
		// AppSettings appSettings = new AppSettings(true);
		// app.setSettings(appSettings);
		// app.setShowSettings(false);
		app.start();
	}

	@Override
	public void simpleInitApp() {

		flyCam.setZoomSpeed(15);
		flyCam.setMoveSpeed(10);

		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		initializeCamera();
		initializeActions();

		initializeFloor();
		initializeBrickWall();

		initializeCrosshairs();
	}

	private void initializeCamera() {
		cam.setLocation(new Vector3f(0, 6, 9));
		cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
	}

	private void initializeActions() {
		inputManager.addMapping(Action.SHOOT.name(), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, Action.SHOOT.name());
	}

	private void initializeFloor() {
		Box floor = new Box(10, 0.1f, 5);
		floor.scaleTextureCoordinates(new Vector2f(3, 6));

		Geometry geometry = new Geometry("floor", floor);
		geometry.setMaterial(initializeMaterial("Textures/Terrain/Pond/Pond.jpg"));
		geometry.setLocalTranslation(0, -0.1f, 0);
		rootNode.attachChild(geometry);

		RigidBodyControl rigidBodyControl = new RigidBodyControl(0);
		geometry.addControl(rigidBodyControl);
		bulletAppState.getPhysicsSpace().add(rigidBodyControl);
	}

	private void createCannonBall() {
		Sphere sphere = new Sphere(32, 32, 0.4f, true, false);
		sphere.setTextureMode(TextureMode.Projected);

		Geometry geometry = new Geometry("cannon ball", sphere);
		geometry.setMaterial(initializeMaterial("Textures/Terrain/Rock/Rock.PNG"));
		geometry.setLocalTranslation(cam.getLocation());
		rootNode.attachChild(geometry);

		RigidBodyControl cannonBallControl = new RigidBodyControl(1);
		geometry.addControl(cannonBallControl);
		bulletAppState.getPhysicsSpace().add(cannonBallControl);
		cannonBallControl.setLinearVelocity(cam.getDirection().mult(25));
	}

	private void initializeBrickWall() {

		float length = 0.48f;
		float height = 0.36f;
		float width = 0.18f;

		Box brick = new Box(length, height, width);
		brick.scaleTextureCoordinates(new Vector2f(1, 0.5f));

		Material material = initializeMaterial("Textures/Terrain/BrickWall/BrickWall.jpg");

		int hor = 6;
		int ver = 10;

		float curStart = -hor * length / 2;
		float curStartOffset = length / 4;
		float curHeight = 0;

		for(int j = 0; j < ver; j++) {
			for(int i = 0; i < hor; i++) {
				Geometry geometry = new Geometry("brick (" + j + ", " + i + ")", brick);
				geometry.setMaterial(material);
				geometry.setLocalTranslation(curStart + curStartOffset + 2 * i * length, curHeight + height, 0);
				rootNode.attachChild(geometry);

				RigidBodyControl rigidBodyControl = new RigidBodyControl(2);
				geometry.addControl(rigidBodyControl);
				bulletAppState.getPhysicsSpace().add(rigidBodyControl);
			}
			curStartOffset = -curStartOffset;
			curHeight += 2*height;
		}

	}

	private Material initializeMaterial(String path) {
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey textureKey = new TextureKey(path);
		textureKey.setGenerateMips(true);
		Texture texture = assetManager.loadTexture(textureKey);
		texture.setWrap(WrapMode.Repeat);
		material.setTexture("ColorMap", texture);

		return material;
	}

	private void initializeCrosshairs() {
		BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText text = new BitmapText(font);
		text.setSize(font.getCharSet().getRenderedSize() * 2);
		text.setText("+");
		text.setLocalTranslation(
				settings.getWidth() / 2 - text.getSize() / 3,
				settings.getHeight() / 2 + text.getLineHeight() / 2,
				0);
		guiNode.detachAllChildren();
		guiNode.attachChild(text);
	}

	@Override
	public void simpleUpdate(float tpf) {
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(!isPressed && name.equals(Action.SHOOT.name())) {
			createCannonBall();
		}
	}
}

