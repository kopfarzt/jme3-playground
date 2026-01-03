package at.kopfarzt.jmonkeyengine.astar.themed;

import com.jme3.math.ColorRGBA;

public enum ConnectionStatus {
	IDLE(ColorRGBA.Gray),
	ACTIVE(ColorRGBA.Yellow);

	private ColorRGBA color;

	private ConnectionStatus(ColorRGBA color) {
		this.color = color;
	}

	public ColorRGBA getColor() {
		return color;
	}
}
