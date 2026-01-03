package at.kopfarzt.jmonkeyengine.astar.themed;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Line;

import at.kopfarzt.jmonkeyengine.astar.geonodes.GeoConnection;

public class ThemedConnection extends GeoConnection<ThemedNode> {

	protected ThemedWorld world;
	protected Geometry geometry;
	private ConnectionStatus status = ConnectionStatus.IDLE;

	public ThemedConnection(ThemedNode one, ThemedNode two, ThemedWorld world) {
		super(one, two);
		this.world = world;

		Line line = new Line(one.getPosition(), two.getPosition());

		geometry = new Geometry(name, line);
		Material material = world.createShadedMaterial(status.getColor());
		material.getAdditionalRenderState().setLineWidth(5);

		geometry.setMaterial(material);
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setStatus(ConnectionStatus status) {
		this.status = status;
		setColor(status.getColor());
	}

	public void setColor(ColorRGBA color) {
		Material material = geometry.getMaterial();
		material.setColor("Ambient", color);
		material.setColor("Diffuse", color);
		material.setColor("Specular", color);
	}

	@Override
	public void reset(ThemedNode one, ThemedNode two) {
		super.reset(one, two);
	}
}
