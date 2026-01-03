package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

public class HelloAudio extends SimpleApplication implements ActionListener {

	private static final String ACTION_SHOOT = "Shoot";
	private AudioNode audioGun;
	private AudioNode audioNature;

	public static void main(String[] args) {
		HelloAudio app = new HelloAudio();
		// app.setShowSettings(false);;
		app.start();
	}

	@Override
	public void simpleInitApp() {

		flyCam.setMoveSpeed(3);

		Box b = new Box(1, 1, 1);
		Geometry geom = new Geometry("Box", b);

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/fakeLighting.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		geom.setMaterial(mat);

		initAudioNodes();
		initKeys();

		rootNode.attachChild(geom);
		audioNature.play();
	}

	private void initAudioNodes() {
		audioGun = new AudioNode(assetManager, "Sound/Effects/Gun.wav", DataType.Buffer);
		audioGun.setPositional(false);
		audioGun.setLooping(false);
		audioGun.setVolume(2);
		rootNode.attachChild(audioGun);

		audioNature = new AudioNode(assetManager, "/Sound/Environment/Ocean Waves.ogg", DataType.Stream);
		audioNature.setPositional(true);
		audioNature.setLooping(true);
		audioNature.setVolume(3);
		rootNode.attachChild(audioNature);
	}

	private void initKeys() {
		inputManager.addMapping(ACTION_SHOOT, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, ACTION_SHOOT);

	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(!isPressed && name.equals(ACTION_SHOOT)) {
			audioGun.playInstance();
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		listener.setLocation(cam.getLocation());
		listener.setRotation(cam.getRotation());
	}
}

