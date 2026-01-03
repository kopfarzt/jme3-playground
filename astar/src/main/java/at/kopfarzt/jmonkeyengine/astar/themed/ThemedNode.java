package at.kopfarzt.jmonkeyengine.astar.themed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;

import at.kopfarzt.jmonkeyengine.astar.geonodes.GeoNode;

/**
 * A {@link GeoNode} with a visible representation and a unique name.
 */
public class ThemedNode extends GeoNode {
	private static final Logger LOG = LoggerFactory.getLogger(ThemedNode.class);
	private static final Vector3f offScreen = new Vector3f(-100, -100, 0);
	protected ThemedWorld world;
	protected Geometry geometry;
	protected BitmapText textNode;
	protected NodeStatus status = NodeStatus.IDLE;
	protected boolean showLabel = true; 


	/**
	 * Create a new {@link ThemedNode} with a unique name.
	 * 
	 * @param name
	 * @param position
	 * @param world
	 */
	public ThemedNode(String name, Vector3f position, ThemedWorld world) {
		super(name, position);
		this.world = world;

		Sphere sphere = new Sphere(32, 32, 0.3f);
		sphere.setTextureMode(TextureMode.Projected);

		geometry = new Geometry(name, sphere);
		geometry.setLocalTranslation(position);
		geometry.setMaterial(world.createShadedMaterial(status.getColor()));

		textNode = world.createText(name);
	}

	public ThemedNode(String name, float x, float y, float z, ThemedWorld world) {
		this(name, new Vector3f(x, y, z), world);
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public BitmapText getTextNode() {
		return textNode;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
		this.status = status;
		setColor(status.getColor());
	}

	public void setColor(ColorRGBA color) {
		Material material = geometry.getMaterial();
		material.setColor("Ambient", color);
		material.setColor("Diffuse", color);
		material.setColor("Specular", color);
	}


	/**
	 * Move text to geometry coordinates.
	 * @param camera 
	 */
	public void adaptTextNode(Camera camera) {
		Vector3f newPos = offScreen;

		if(showLabel) {
			float dist = camera.distanceToNearPlane(position);

			if(dist > 0) {
				Vector3f screen = camera.getScreenCoordinates(position);
				newPos = new Vector3f(screen.getX() - textNode.getSize() / 2, screen.getY() - textNode.getLineHeight() / 2, 0);
			}
		}

		textNode.setLocalTranslation(newPos);
	}

	public boolean isShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}

	@Override
	public void reset() {
		super.reset();
	}
}
