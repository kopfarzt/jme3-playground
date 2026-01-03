package at.kopfarzt.jmonkeyengine.astar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.graph.MutableValueGraph;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;

import at.kopfarzt.jmonkeyengine.astar.themed.ThemedConnection;
import at.kopfarzt.jmonkeyengine.astar.themed.ThemedNode;
import at.kopfarzt.jmonkeyengine.astar.themed.ThemedWorld;

public class AStar extends SimpleApplication implements ActionListener {
	private static final Logger LOG = LoggerFactory.getLogger(AStar.class);
	private static final String ACTION_TOGGLE_MOUSE = "ToggleMouse";
	private static final String ACTION_SELECT = "Select";
	private static final String ACTION_FOLLOW_CAM = "FollowCam";
	private static final String ACTION_SHOW_LABELS = "ShowLabels";
	// private SpotLight spotLight;
	private ThemedWorld world;
	private Node camFollower;
	private boolean followCam = false;
	private boolean showLabels = true;

	public static void main(String[] args) {
		AStar app = new AStar();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		initCamera();
		initCrossHair();
		initKeys();
		setDisplayFps(false);
		setDisplayStatView(false);

		attachCoordinateAxes(Vector3f.ZERO);

		createWorld();


		LOG.info("World: {}", world);
	}

	private void createLightsAndShadows() {
		AmbientLight ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(8, 8, 16, 0));
		rootNode.addLight(ambientLight);

		DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1f, -1f, -1f), ColorRGBA.White);
		rootNode.addLight(directionalLight);

		//		spotLight = new SpotLight();
		//		spotLight.setColor(ColorRGBA.White);
		//		spotLight.setSpotInnerAngle(15 * FastMath.DEG_TO_RAD);
		//		spotLight.setSpotOuterAngle(35 * FastMath.DEG_TO_RAD);
		//		spotLight.setSpotRange(100);
		//		rootNode.addLight(spotLight);

		addShadowRenderer(directionalLight, 4096, 1);
		// addShadowFilter(directionalLight, 4096, 1);
	}

	private void initCamera() {
		flyCam.setZoomSpeed(10);
		flyCam.setMoveSpeed(10);
		cam.setLocation(new Vector3f(0, 20, 20));
		cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
	}

	private void initKeys() {
		inputManager.addMapping(ACTION_SELECT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping(ACTION_TOGGLE_MOUSE, new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping(ACTION_FOLLOW_CAM, new KeyTrigger(KeyInput.KEY_F));
		inputManager.addMapping(ACTION_SHOW_LABELS, new KeyTrigger(KeyInput.KEY_L));
		inputManager.addListener(this, ACTION_SELECT, ACTION_TOGGLE_MOUSE, ACTION_FOLLOW_CAM, ACTION_SHOW_LABELS);
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

	private void createWorld() {
		createThemedWorld();
		createObjects();
		createLightsAndShadows();
	}

	private void createObjects() {
		Node worldNode = world.createWorldNode();
		rootNode.attachChild(worldNode);

		Node worldGuiNode = world.createWorldGuiNode();
		guiNode.attachChild(worldGuiNode);

		Spatial groundPlane = createGroundPlane();
		rootNode.attachChild(groundPlane);

		Spatial cylinder = createCamFollower();
		rootNode.attachChild(cylinder);

		worldNode.setShadowMode(ShadowMode.CastAndReceive);
		cylinder.setShadowMode(ShadowMode.CastAndReceive);
		groundPlane.setShadowMode(ShadowMode.Receive);
	}

	private void createThemedWorld() {
		world = new ThemedWorld(assetManager);

		ThemedNode n1 = new ThemedNode("01", -9.2f, 0.0f, -9.8f, world);
		ThemedNode n2 = new ThemedNode("02", -7.5f, 2.0f, 6.1f, world);
		ThemedNode n3 = new ThemedNode("03", -5.1f, 1.0f, 8.4f, world);
		ThemedNode n4 = new ThemedNode("04", -5.0f, 0.0f, 1.3f, world);
		ThemedNode n5 = new ThemedNode("05", -3.3f, 0.5f, -8.1f, world);
		ThemedNode n6 = new ThemedNode("06", -1.1f, 0.0f, -4.5f, world);
		ThemedNode n7 = new ThemedNode("07", 1.4f, 2.0f, 3.7f, world);
		ThemedNode n8 = new ThemedNode("08", 3.2f, 0.0f, 5.3f, world);
		ThemedNode n9 = new ThemedNode("09", 5.8f, 1.5f, 7.1f, world);
		ThemedNode n10 = new ThemedNode("10", 7.3f, 0.7f, 1.5f, world);
		ThemedNode n11 = new ThemedNode("11", 9.1f, 3.0f, -4.0f, world);

		MutableValueGraph<ThemedNode,ThemedConnection> graph = world.getGraph();

		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
		graph.addNode(n4);
		graph.addNode(n5);
		graph.addNode(n6);
		graph.addNode(n7);
		graph.addNode(n8);
		graph.addNode(n9);
		graph.addNode(n10);
		graph.addNode(n11);

		// Construct a directed graph. Going from lower node numbers
		// to higher node numbers should be possible with a visually
		// looking shortest path. Going in the other direction from higher
		// node numbers to lower node numbers can sometimes produce
		// long paths because only some connections are going back.

		world.connect(n1, n2, n4, n5);
		world.connect(n2, n1, n3, n4, n7);
		world.connect(n3, n8, n7, n9);
		world.connect(n4, n6, n7, n10);
		world.connect(n5, n6, n11);
		world.connect(n6, n10, n11);
		world.connect(n7, n2, n8, n10);
		world.connect(n8, n9, n10);
		world.connect(n9, n8, n10);
		world.connect(n9, n7, n10);
		world.connect(n10, n9, n11);
		world.connect(n11, n6);

		LOG.debug("Build Graph: {}", graph);
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

	private Spatial createGroundPlane() {
		Box box = new Box(10, 0.01f, 10);

		Geometry geometry = new Geometry("GroundPlane", box);

		geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0xC4, 0x73, 0x35, 0xff)));
		geometry.setLocalTranslation(0, -0.015f, 0);

		return geometry;
	}

	private Node createCamFollower() {
		//		Cylinder cylinder = new Cylinder(2, 32, 0.3f, 10);
		//		Geometry geometry = new Geometry("Cylinder", cylinder);
		//		geometry.setLocalTranslation(0, 0, 5);
		//		geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0x35, 0x73, 0xc4, 0xff)));

		Box box = new Box(0.3f, 0.1f, 5f);
		Geometry geometry = new Geometry("Street", box);
		geometry.setLocalTranslation(0, 0, 5);
		geometry.setMaterial(createShadedMaterial(ColorRGBA.fromRGBA255(0x40, 0x40, 0x40, 0xff)));

		camFollower = new Node();
		camFollower.attachChild(geometry);

		return camFollower;
	}

	private void attachCoordinateAxes(Vector3f pos) {
		Arrow arrow = new Arrow(Vector3f.UNIT_X);
		putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Y);
		putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Z);
		putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
	}

	private Geometry putShape(Mesh shape, ColorRGBA color) {
		Geometry g = new Geometry("coordinate axis", shape);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.getAdditionalRenderState().setLineWidth(4);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		rootNode.attachChild(g);
		return g;
	}

	@Override
	public void simpleUpdate(float tpf) {
		//		spotLight.setPosition(cam.getLocation());
		//		spotLight.setDirection(cam.getDirection());

		Camera camera = getCamera();

		world.getGraph().nodes().stream().forEach(n -> {
			n.adaptTextNode(camera);
		});

		if(followCam) {
			camFollower.lookAt(cam.getLocation(), Vector3f.UNIT_Y);
		}
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		switch(name) {
		case ACTION_TOGGLE_MOUSE:
			if(!isPressed) {
				// flyCam.setEnabled(!flyCam.isEnabled());
				inputManager.setCursorVisible(!inputManager.isCursorVisible());
				LOG.info("FlyCam enabled: {} cursor visible: {}", flyCam.isEnabled(), inputManager.isCursorVisible());
			}
			break;
		case ACTION_FOLLOW_CAM:
			if(!isPressed) {
				followCam = !followCam;
				LOG.info("FollowCam enabled: {}", followCam);
			}
			break;
		case ACTION_SHOW_LABELS:
			if(!isPressed) {
				showLabels = !showLabels;
				LOG.info("ShowLabels enabled: {}", showLabels);
				world.getGraph().nodes().stream().forEach(n -> {
					n.setShowLabel(showLabels);
				});

			}
			break;
		case ACTION_SELECT:
			if(!isPressed) {
				LOG.debug(ACTION_SELECT);

				CollisionResults collisions = new CollisionResults();

				Ray ray = new Ray(cam.getLocation(), cam.getDirection());

				world.getNodesNode().collideWith(ray, collisions);

				LOG.debug("Collisions: {}", collisions.size());

				CollisionResult collision = collisions.getClosestCollision();

				LOG.debug("Collision: {}", collision);

				if(collision != null) {
					String collName = collision.getGeometry().getName();
					// TODO solve more efficiently
					ThemedNode node = world.getGraph().nodes().stream().filter(n -> collName.equals(n.getName())).findAny().get();
					if(node != null) {
						LOG.info("Hit node: {}", node.getName());
						world.selectStartEndNode(node);
					}

					world.findPath();
				}

				//				logPathCost("02-07", "07-08", "08-10", "10-11");
				//				logPathCost("02-07", "07-10", "10-11");
			}
			break;
		}
	}
}
