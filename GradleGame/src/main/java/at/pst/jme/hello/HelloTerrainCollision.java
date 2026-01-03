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
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class HelloTerrainCollision extends SimpleApplication implements ActionListener {
	private static final Logger LOG = Logger.getLogger(HelloTerrainCollision.class.getName());

	private RigidBodyControl landscape;
	private BulletAppState bulletAppState;
	private CharacterControl player;
	private Map<Action, Boolean> actionMap = new EnumMap(Action.class);
	private boolean debug;

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
		HelloTerrainCollision app = new HelloTerrainCollision();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		flyCam.setMoveSpeed(1);
		viewPort.setBackgroundColor(ColorRGBA.Blue.interpolateLocal(ColorRGBA.White, 0.7f));

		Material terrainMaterial = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
		terrainMaterial.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		setTerrainTexture(terrainMaterial, "Tex1", "Textures/Terrain/splat/grass.jpg", 64f);
		setTerrainTexture(terrainMaterial, "Tex2", "Textures/Terrain/splat/dirt.jpg", 32f);
		setTerrainTexture(terrainMaterial, "Tex3", "Textures/Terrain/splat/road.jpg", 128f);

		Texture heightTexture = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
		HeightMap heightMap = new ImageBasedHeightMap(heightTexture.getImage());
		boolean loadedHeight = heightMap.load();
		LOG.log(Level.INFO, "Loaded height: {0}", loadedHeight);

		TerrainQuad terrainQuad = new TerrainQuad("terrain", 65, 513, heightMap.getHeightMap());

		terrainQuad.setMaterial(terrainMaterial);
		terrainQuad.setLocalTranslation(0, -200, 0);
		terrainQuad.setLocalScale(2, 1, 2);

		rootNode.attachChild(terrainQuad);

		TerrainLodControl terrainLodControl = new TerrainLodControl(terrainQuad, cam);
		terrainQuad.addControl(terrainLodControl);

		CollisionShape terrainShape = CollisionShapeFactory.createMeshShape(terrainQuad);
		landscape = new RigidBodyControl(terrainShape, 0);
		terrainQuad.addControl(landscape);

		CollisionShape playerShape = new CapsuleCollisionShape(1.5f, 6, 1);
		player = new CharacterControl(playerShape, 0.05f);
		player.setJumpSpeed(20);
		player.setFallSpeed(300);

		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
		physicsSpace.add(landscape);
		physicsSpace.add(player);

		player.setGravity(new Vector3f(0, -100, 0));
		player.setPhysicsLocation(new Vector3f(-100, 0, 0));

		setupKeys();
	}

	private void setTerrainTexture(Material material, String key, String name, float scale) {
		Texture texture = assetManager.loadTexture(name);
		texture.setWrap(WrapMode.Repeat);
		material.setTexture(key, texture);
		material.setFloat(key + "Scale", scale);
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
			LOG.log(Level.WARNING, "Unkown action: ''{0}''", name);
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		player.setWalkDirection(new Vector3f(0,  0,  0));

		float vfac = 0.1f;
		float hfac = 0.1f;
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

