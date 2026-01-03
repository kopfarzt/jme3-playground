package at.pst.jme.hello;

import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickAxis;
import com.jme3.input.controls.AnalogListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.AppSettings;
import com.jme3.util.TangentBinormalGenerator;

public class HelloInputSystem extends SimpleApplication {

	private Spatial bumpyRock;
	private boolean foundJoystick = false;

	public static void main(String[] args) {
		HelloInputSystem app = new HelloInputSystem();
		AppSettings settings = new AppSettings(true);
		settings.setUseJoysticks(true);
		app.setSettings(settings);

		app.start();
	}

	@Override
	public void simpleInitApp() {
		// setShowSettings(false);
		flyCam.setZoomSpeed(-30);
		flyCam.setMoveSpeed(5);

		bumpyRock = createBumbyRock();
		rootNode.attachChild(bumpyRock);

		DirectionalLight sun = new DirectionalLight(new Vector3f(1, 0, -2), ColorRGBA.White);
		rootNode.addLight(sun);

		initInputManager();

		System.out.println("Initialization done.");
		if(!foundJoystick) {
			System.out.println("Did not find a Joystick for Z-Axis (like Sidewinder). Rotation of geometry is disabled.");
		}
	}


	private void initInputManager() {
		Joystick[] joysticks = inputManager.getJoysticks();

		System.out.format("Found %d joysticks.%n", joysticks == null ? -1 : joysticks.length);
		List<String> mappings = new ArrayList<>();
		String zPositive = null;
		String zNegative = null;
		for (Joystick joystick : joysticks) {
			System.out.format("Joystick: %s%n", joystick);

			List<JoystickAxis> axes = joystick.getAxes();
			for (JoystickAxis axis : axes) {
				System.out.format("  Axis:%d %s%n", axis.getAxisId(), axis.getName());
				String positiveMapping = axis.getName() + "-POS";
				String negativeMapping = axis.getName() + "-NEG";
				inputManager.addMapping(positiveMapping);
				mappings.add(positiveMapping);
				inputManager.addMapping(negativeMapping);
				mappings.add(negativeMapping);
				axis.assignAxis(positiveMapping, negativeMapping);
				if(axis.getName().startsWith("Z")) {
					zPositive = positiveMapping;
					zNegative = negativeMapping;
					foundJoystick = true;
					System.out.format("Found Joystick '%s' with Z-Axis '%s'%n", joystick.getName(), axis.getName());
				}
			}
		}

		final String zPos = zPositive;
		final String zNeg = zNegative;
		final Spatial rotSpatial = bumpyRock;
		AnalogListener analogListener = new AnalogListener() {
			@Override
			public void onAnalog(String name, float value, float tpf) {
				if(name.equals(zPos)) {
					rotSpatial.rotate(0f, 0f, 5f * value);
				}
				else if(name.equals(zNeg)) {
					rotSpatial.rotate(0f, 0f, -5f * value);
				}
				// System.out.format("Analog: name: %s value: %.5f tpf: %.3f%n", name, value, tpf);
			}
		};

		inputManager.addListener(analogListener, mappings.toArray(String[]::new));

		//		inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
		//			@Override
		//			public void onConnected(Joystick joystick) {
		//				System.out.println("Joystick connected: " + joystick);
		//			}
		//
		//			@Override
		//			public void onDisconnected(Joystick joystick) {
		//				System.out.println("Joystick disconnected: " + joystick);
		//			}
		//		});


	}


	private Spatial createBumbyRock() {

		// material
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
		material.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Diffuse", ColorRGBA.White);
		material.setColor("Specular", ColorRGBA.White);
		material.setFloat("Shininess", 64);

		// mesh
		Sphere sphere = new Sphere(32, 32, 2);
		sphere.setTextureMode(TextureMode.Projected);
		TangentBinormalGenerator.generate(sphere);

		// geometry
		Geometry geometry = new Geometry("Sphere", sphere);
		geometry.setLocalTranslation(0,  2,  -2);
		geometry.rotate(1.6f, 0, 0);
		geometry.setMaterial(material);

		return geometry;
	}

	@Override
	public void simpleUpdate(float tpf) {
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}
}

