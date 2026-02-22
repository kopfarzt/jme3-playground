package zib.grimble.jmonkeyengine.sphere;

import com.jme3.audio.AudioNode;

import jme3utilities.SimpleControl;

public class SpatialAudioControl extends SimpleControl {
	private AudioNode audioNode;

	public SpatialAudioControl(AudioNode audioNode) {
		this.audioNode = audioNode;
		System.out.format("Node %s pos: %b ref: %.3f max: %.3f%n", audioNode.getName(), audioNode.isPositional(), audioNode.getRefDistance(), audioNode.getMaxDistance());
	}

	@Override
	public void update(float tpf) {
		audioNode.setLocalTranslation(getSpatial().getWorldTranslation());
	}

}
