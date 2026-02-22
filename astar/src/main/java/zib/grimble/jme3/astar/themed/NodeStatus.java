package zib.grimble.jme3.astar.themed;

import com.jme3.math.ColorRGBA;

public enum NodeStatus {
	IDLE(ColorRGBA.Gray),
	PATH(ColorRGBA.Yellow),
	START(ColorRGBA.Red),
	END(ColorRGBA.Green);

	private ColorRGBA color;

	private NodeStatus(ColorRGBA color) {
		this.color = color;
	}

	public ColorRGBA getColor() {
		return color;
	}
}
