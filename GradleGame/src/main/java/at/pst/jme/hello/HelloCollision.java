package at.pst.jme.hello;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;

public class HelloCollision extends SimpleApplication implements ActionListener {
	private static final Logger logger = Logger.getLogger(HelloAnimation.class.getName());

	private enum Action {
		LEFT(new KeyTrigger(KeyInput.KEY_A)),
		RIGHT(new KeyTrigger(KeyInput.KEY_D)),
		UP(new KeyTrigger(KeyInput.KEY_W)),
		DOWN(new KeyTrigger(KeyInput.KEY_S)),
		JUMP(new KeyTrigger(KeyInput.KEY_SPACE)),
		DEBUG(new KeyTrigger(KeyInput.KEY_F11));

		private Trigger[] triggers;

		private Action(Trigger...triggers) {
			this.triggers = triggers;
		}

		public Trigger[] getTriggers() {
			return triggers;
		}
	}

	public static void main(String[] args) {
		HelloCollision app = new HelloCollision();
		app.setShowSettings(false);
		app.start();
	}

	private Spatial sceneModel;
	private BulletAppState bulletAppState;
	private RigidBodyControl landscape;
	private CharacterControl player;
	private Map<Action, Boolean> actionMap = new EnumMap(Action.class);
	private boolean debug;

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		flyCam.setMoveSpeed(100);

		sceneModel = createTown();

		CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
		landscape = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(landscape);

		CollisionShape playerShape = new CapsuleCollisionShape(1.5f, 6, 1);
		player = new CharacterControl(playerShape, 0.05f);
		player.setJumpSpeed(20);
		player.setFallSpeed(300);

		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
		physicsSpace.add(landscape);
		physicsSpace.add(player);

		player.setGravity(new Vector3f(0, -30, 0));
		player.setPhysicsLocation(new Vector3f(0,  10,  0));

		rootNode.addLight(new AmbientLight(ColorRGBA.White.mult(1.3f)));
		rootNode.addLight(new DirectionalLight(new Vector3f(1f, -1f, -1f).normalizeLocal(), ColorRGBA.White));
		rootNode.attachChild(sceneModel);

		setupKeys();

	}

	private Spatial createTown() {
		Spatial model = assetManager.loadModel("Scenes/town/main.scene");
		model.setLocalScale(2);
		return model;
	}

	private void setupKeys() {
		for (Action action : Action.values()) {
			inputManager.addMapping(action.name(), action.getTriggers());
		}
		inputManager.addListener(this, Stream.of(Action.values()).map(a -> a.name()).toArray(String[]::new));
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		try {
			Action action = Action.valueOf(name);
			switch(action) {
			case DEBUG:
				if(!isPressed) {
					debug = !debug;
					bulletAppState.setDebugEnabled(debug);
				}
				break;
			default:
				actionMap.put(action, isPressed);
				break;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Unkown action: ''{0}''", name);
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		player.setWalkDirection(new Vector3f(0,  0,  0));

		float vfac = 0.6f;
		float hfac = 0.4f;
		for (Entry<Action, Boolean> entry : actionMap.entrySet()) {
			if(entry.getValue()) {
				switch(entry.getKey()) {
				case LEFT:
					player.setWalkDirection(cam.getLeft().mult(hfac));
					break;
				case RIGHT:
					player.setWalkDirection(cam.getLeft().mult(-hfac));
					break;
				case UP:
					player.setWalkDirection(cam.getDirection().mult(vfac));
					break;
				case DOWN:
					player.setWalkDirection(cam.getDirection().mult(-vfac));
					break;
				case JUMP:
					player.jump(new Vector3f(0, 20, 0));
					entry.setValue(false);
					break;
				default:
					break;
				}
			}
		}

		cam.setLocation(player.getPhysicsLocation());
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}
}

