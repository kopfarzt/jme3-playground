package at.pst.jme.hello;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jme3.anim.AnimComposer;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.action.Action;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

public class HelloAnimation extends SimpleApplication implements ActionListener {
	private static final Logger logger = Logger.getLogger(HelloAnimation.class.getName());
	private boolean walking = false;
	private AnimComposer otoComposer;
	private SkinningControl otoSkinningControl;

	public static void main(String[] args) {
		HelloAnimation app = new HelloAnimation();

		AppSettings appSettings = new AppSettings(true);
		app.setSettings(appSettings);
		app.setShowSettings(false);
		app.start();
	}

	@Override
	public void simpleInitApp() {
		viewPort.setBackgroundColor(ColorRGBA.DarkGray);

		DirectionalLight sun = new DirectionalLight(new Vector3f(-0.1f, -1f, -1f));
		rootNode.addLight(sun);

		Spatial player = assetManager.loadModel("/Models/Oto/Oto.mesh.xml");
		player.setLocalScale(0.5f);
		rootNode.attachChild(player);

		logger.log(Level.INFO, "Number of controls: {0}\n{1}", new Object[] {player.getNumControls(),
				IntStream.range(0, player.getNumControls())
				.mapToObj(i -> player.getControl(i).toString())
				.collect(Collectors.joining(", "))
		}
				);


		otoSkinningControl = player.getControl(SkinningControl.class);
		otoComposer = player.getControl(AnimComposer.class);


		inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "Walk");
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
		if(name.equals("Walk") && !isPressed) {
			Action currentAction = otoComposer.getCurrentAction();
			System.out.println("Current action: " + currentAction);
			otoComposer.setCurrentAction(walking ? "stand" : "Walk");
			walking = !walking;
		}
	}
}

