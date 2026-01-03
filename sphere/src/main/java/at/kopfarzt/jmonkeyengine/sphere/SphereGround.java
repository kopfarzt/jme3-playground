package at.kopfarzt.jmonkeyengine.sphere;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.environment.generation.JobProgressListener;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.util.SkyFactory;
import com.jme3.util.SkyFactory.EnvMapType;

import at.kopfarzt.jmonkeyengine.materials.MaterialFactory;

public class SphereGround extends SimpleApplication implements ActionListener, PhysicsTickListener {
	private static final Logger LOG = LoggerFactory.getLogger(SphereGround.class);
	private static final String ACTION_TOGGLE_MOUSE = "ToggleMouse";
	private static final String ACTION_SPEED_INC = "SpeedIncrement";
	private static final String ACTION_SPEED_DEC = "SpeedDecrement";
	private static final int BALL_COUNT = 1;
	protected MaterialFactory materialFactory;

	public static void main(String[] args) {
		SphereGround app = new SphereGround();
		app.setShowSettings(false);
		AppSettings settings = new AppSettings(true);
		settings.setSamples(4);
		settings.put("Width", 1280);
		settings.put("Height", 1024);
		app.setSettings(settings);
		app.start();
	}

	private AmbientLight ambientLight;
	private DirectionalLight directionalLight;
	private Node audio;
	private Node shadowed;
	private BulletAppState bulletAppState;
	private Spline spline;
	private PhysicsSpace physicsSpace;

	private Set<RigidBodyControl> balls = new HashSet<>();

	private AudioNode ballHit;

	@Override
	public void simpleInitApp() {
		materialFactory = MaterialFactory.get(assetManager);

		initPhysicSpaceBulletAppState(false, 1f);
		initStatusDisplay();
		initCamera();
		initCrossHair();
		initKeys();

		createAudio();
		createEnvironment();
		createLightsAndShadows();
		createLightProbe(this::createObjects);
	}

	private void createAudio() {
		audio = new Node("Audio");
		rootNode.attachChild(audio);

		ballHit = new AudioNode(assetManager, "Sound/triangle.ogg", DataType.Buffer);
		audio.attachChild(ballHit);
		// ballHit.setPositional(true);
	}

	private void initPhysicSpaceBulletAppState(boolean debug, float speed) {
		LOG.info("Initializing bullet app state");
		bulletAppState = new BulletAppState();
		bulletAppState.setDebugEnabled(debug);
		bulletAppState.setSpeed(speed);
		stateManager.attach(bulletAppState);

		physicsSpace = bulletAppState.getPhysicsSpace();
		physicsSpace.addCollisionListener(new PhysicsCollisionListener() {

			@Override
			public void collision(PhysicsCollisionEvent event) {
				PhysicsCollisionObject a = event.getObjectA();
				PhysicsCollisionObject b = event.getObjectB();

				if(a != b && balls.contains(a) && balls.contains(b)) {
					ballHit.playInstance();
				}

				System.out.format("Collision: %s : %s%n", a.getUserObject(), b.getUserObject());
			}
		});
	}

	private void initStatusDisplay() {
		LOG.info("Initializing status display");
		setDisplayFps(true);
		setDisplayStatView(true);
	}

	private void initCamera() {
		LOG.info("Initializing camera");
		flyCam.setZoomSpeed(10);
		flyCam.setMoveSpeed(10);
		cam.setLocation(new Vector3f(-20, 20, 0));
		cam.lookAt(new Vector3f(0, 1, 0), Vector3f.UNIT_Y);
	}

	private void initCrossHair() {
		LOG.info("Initializing cross-hair");
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText text = new BitmapText(guiFont);
		text.setSize(guiFont.getCharSet().getRenderedSize());
		text.setText("+");
		text.setLocalTranslation(
				(settings.getWidth() - text.getLineWidth()) / 2f,
				(settings.getHeight() + text.getLineHeight()) / 2f,
				0f);
		guiNode.attachChild(text);
	}

	private void initKeys() {
		LOG.info("Initializing keys");
		inputManager.addMapping(ACTION_TOGGLE_MOUSE, new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping(ACTION_SPEED_INC, new KeyTrigger(KeyInput.KEY_ADD));
		inputManager.addMapping(ACTION_SPEED_DEC, new KeyTrigger(KeyInput.KEY_SUBTRACT));
		inputManager.addListener(this, ACTION_TOGGLE_MOUSE, ACTION_SPEED_INC, ACTION_SPEED_DEC);
	}

	private void createLightsAndShadows() {
		LOG.info("Creating lights and shadows");
		ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(255, 255, 255, 0).mult(1f));
		rootNode.addLight(ambientLight);

		directionalLight = new DirectionalLight(new Vector3f(1f, -1f, 1f), ColorRGBA.White);
		rootNode.addLight(directionalLight);

		addShadowRenderer();

		LOG.info("Creating shadowed root");
		shadowed = new Node("Shadowed");
		shadowed.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(shadowed);
	}

	private void addShadowRenderer() {
		LOG.info("Creating shadow renderer");
		DirectionalLightShadowRenderer shadowRenderer =
				new DirectionalLightShadowRenderer(assetManager, 4096, 4);
		shadowRenderer.setLight(directionalLight);
		viewPort.addProcessor(shadowRenderer);
	}

	private void createEnvironment() {
		rootNode.attachChild(SkyFactory.createSky(assetManager, "textures/sky/kloppenheim_06_puresky.jpg", EnvMapType.EquirectMap));
	}

	private void createLightProbe(Runnable postAction) {
		EnvironmentCamera environmentCamera = new EnvironmentCamera();
		stateManager.attach(environmentCamera);
		environmentCamera.initialize(stateManager, this);
		JobProgressListener<LightProbe> jobListener = new JobProgressAdapter<>() {
			@Override public void done(LightProbe result) {
				postAction.run();
			}
		};
		LightProbe lightProbe = LightProbeFactory.makeProbe(environmentCamera, rootNode, jobListener);
		// objects outside of this radius will be black
		lightProbe.getArea().setRadius(500);
		lightProbe.setPosition(v3(0, 0, 0));
		rootNode.addLight(lightProbe);
	}

	private void createObjects() {
		// createPlayground();
		// createFloatHeightMap();
		createHillHeightMap();
		createGround();

		for (int i = 0; i < BALL_COUNT; i++) {
			balls.add(createBall("ball" + i, v3(0, 30, 0f + 0.5f * (i - BALL_COUNT / 2))));
		}
	}

	private void createHillHeightMap() {
		try {
			HeightMap heightMap = new HillHeightMap(513, 5000, 1, 50);
			heightMap.setHeightScale(0.1f);
			heightMap.load();
			createHeightMap(heightMap.getScaledHeightMap());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createFloatHeightMap() {
		createHeightMap(new float[] {
				255, 255, 255, 255, 255,
				255, 0, 0, 0, 255,
				255, 0, 0, 0, 255,
				255, 0, 0, 0, 255,
				255, 255, 255, 255, 255
		});
	}

	private void createHeightMap(float[] heights) {

		int sqrt = (int)(FastMath.sqrt(heights.length) + 0.5f);

		TerrainQuad terrainQuad = new TerrainQuad("terrain", 65, sqrt, heights);
		Material material = materialFactory.createPlastic(ColorRGBA.fromRGBA255(0, 0, 255, 0), 0);
		terrainQuad.setMaterial(material);

		TerrainLodControl terrainLodControl = new TerrainLodControl(terrainQuad, cam);
		terrainQuad.addControl(terrainLodControl);

		terrainQuad.setShadowMode(ShadowMode.CastAndReceive);
		rootNode.attachChild(terrainQuad);

		CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(terrainQuad);
		collisionShape.setMargin(0.001f);

		RigidBodyControl terrain = new RigidBodyControl(collisionShape, 0);

		terrainQuad.addControl(terrain);
		terrain.setRestitution(0.5f);
		terrain.setUserObject("terrain");

		physicsSpace.add(terrain);

	}

	private void createPlayground() {
		LOG.info("Creating playground");

		createGround();

		LOG.info("Initializing other objects");

		Box box1 = new Box(5, 1, 5);
		Geometry geometry1 = new Geometry("Box", box1);
		geometry1.setMaterial(createShadedMaterial(ColorRGBA.Red));
		geometry1.rotate(0.1f, 0, 0);

		shadowed.attachChild(geometry1);
		addToPhysicsSpace("box1", geometry1, 0);

		Box box2 = new Box(5, 1, 5);
		Geometry geometry2 = new Geometry("Box2", box2);
		geometry2.setMaterial(createShadedMaterial(ColorRGBA.Red));
		geometry2.rotate(-0.1f, 0, 0);

		shadowed.attachChild(geometry2);
		addToPhysicsSpace("box2", geometry2, 0);

		//		spline = new Spline(SplineType.CatmullRom, List.of(
		//				new Vector3f(-8, 0.15f, -8),
		//				new Vector3f(8, 0.15f, -8),
		//				new Vector3f(8, 0.15f, 8),
		//				new Vector3f(-8, 0.15f, 8),
		//				new Vector3f(-8, 0.15f, -8)
		//				), 0.2f, true);
		//		Curve curve = new Curve(spline, 100);
		//		geometry = new Geometry("Spline", curve);
		//		geometry.setMaterial(createShadedMaterial(ColorRGBA.Magenta));
		//		shadowed.attachChild(geometry);
		//
		//		// TODO use this syntax
		//		@SuppressWarnings("unchecked")
		//		List<String>[] abc = new List[] {List.of(0)};
		//
		//		List<Float>[] xyz = null;
		//		Surface.createNurbsSurface(
		//				List.of(
		//						List.of(
		//								new Vector4f(-8, 1, -8, 1)
		//								)
		//						),
		//				xyz , 10, 10, 3, 3, true);
		//
		//		MeshCollisionShape collisionShape = new MeshCollisionShape(geometry.getMesh());
		//		RigidBodyControl rigidBodyControl = new RigidBodyControl(collisionShape, 0);
		//		geometry.addControl(rigidBodyControl);
		//		bulletAppState.getPhysicsSpace().add(rigidBodyControl);

	}

	private void createGround() {
		Box box = new Box(1000, 0.1f, 1000);
		Geometry geometry = new Geometry("GroundPlane", box);
		geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff)));
		geometry.setLocalTranslation(0, -0.15f, 0);

		shadowed.attachChild(geometry);
		addToPhysicsSpace("ground", geometry, 0);

	}

	private RigidBodyControl createBall(String name, Vector3f pos) {
		LOG.info("Creating ball");
		//		Sphere sphere = new Sphere(32, 32, 0.8f, true, false);
		float radius = 0.1f;
		Sphere sphere = new Sphere(32, 32, radius);
		sphere.setTextureMode(TextureMode.Projected);
		Geometry geometry = new Geometry(name, sphere);
		geometry.setLocalTranslation(pos);
		geometry.setMaterial(materialFactory.createPolishedScratchesMetal());

		shadowed.attachChild(geometry);
		RigidBodyControl ball = addToPhysicsSpace(name, geometry, 1000);
		// ball.setSleepingThresholds(0.1f, 0.1f);
		ball.setCcdMotionThreshold(radius / 10);
		ball.setCcdSweptSphereRadius(radius / 10);
		ball.setRestitution(0.5f);

		// AudioNode audioNode = new AudioNode(assetManager, "Sound/ball-rolling.wav", DataType.Buffer);
		AudioNode audioNode = new AudioNode(assetManager, "Sound/beep.wav", DataType.Buffer);
		audioNode.setName(name);
		audioNode.setLooping(true);
		audioNode.setPositional(true);
		audioNode.setRefDistance(0.2f);
		audioNode.setMaxDistance(600f);
		audioNode.setVolume(3);

		geometry.addControl(new SpatialAudioControl(audioNode));
		audio.attachChild(audioNode);
		audioNode.play();

		return ball;
	}


	private Vector3f v3(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}

	private RigidBodyControl addToPhysicsSpace(String name, Spatial geometry, float mass) {
		RigidBodyControl rigidBodyControl = new RigidBodyControl(mass);
		rigidBodyControl.setUserObject(name);
		geometry.addControl(rigidBodyControl);
		bulletAppState.getPhysicsSpace().add(rigidBodyControl);
		return rigidBodyControl;
	}

	private Material createShadedMaterial(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Ambient", color);
		material.setColor("Diffuse", color);
		material.setColor("Specular", color);
		material.setFloat("Shininess", 64);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		switch(name) {
		case ACTION_TOGGLE_MOUSE:
			if(!isPressed) {
				inputManager.setCursorVisible(!inputManager.isCursorVisible());
				System.out.println("Cursor is not " + (inputManager.isCursorVisible() ? "visible" : "invisible"));
			}
			break;
		case ACTION_SPEED_INC:
			if(!isPressed) {
				flyCam.setMoveSpeed(flyCam.getMoveSpeed() * 1.1f);
				System.out.println("FlyCam Speed: " + flyCam.getMoveSpeed());
			}
			break;
		case ACTION_SPEED_DEC:
			if(!isPressed) {
				flyCam.setMoveSpeed(flyCam.getMoveSpeed() * 0.9f);
				System.out.println("FlyCam Speed: " + flyCam.getMoveSpeed());
			}
			break;
		}
	}

	@Override
	public void prePhysicsTick(PhysicsSpace space, float timeStep) {
	}

	@Override
	public void physicsTick(PhysicsSpace space, float timeStep) {

	}

	@Override
	public void simpleUpdate(float tpf) {
		listener.setLocation(cam.getLocation());
		listener.setRotation(cam.getRotation());
	}
}
