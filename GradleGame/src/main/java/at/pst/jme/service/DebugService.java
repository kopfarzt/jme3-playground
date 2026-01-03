package at.pst.jme.service;

import java.util.logging.Logger;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Sphere;

public class DebugService {
	private static final Logger LOG = Logger.getLogger(DebugService.class.getCanonicalName());
	private Application app;
	private MaterialService materialService;

	public DebugService(Application app) {
		this.app = app;
		materialService = new MaterialService(app);
	}

	public Node createCoordinateAxis(Vector3f pos) {
		Node node = new Node("Coordinates");
		node.attachChild(createArrow(pos, Vector3f.UNIT_X, ColorRGBA.Red));
		node.attachChild(createArrow(pos, Vector3f.UNIT_Y, ColorRGBA.Green));
		node.attachChild(createArrow(pos, Vector3f.UNIT_Z, ColorRGBA.Blue));
		return node;
	}

	public Node createCoordinateAxis() {
		return createCoordinateAxis(Vector3f.ZERO);
	}

	public Spatial createGrid(int x, int y, float lineDist, boolean center, ColorRGBA color) {
		Geometry geometry = new Geometry("Grid XY", new Grid(x, y, lineDist));
		Material material = materialService.createWireframeMaterial(color);
		geometry.setMaterial(material);
		if(center) {
			geometry.center();
		}
		return geometry;

	}

	public Geometry createArrow(Vector3f pos, Vector3f dir, ColorRGBA color) {
		Arrow arrow = new Arrow(dir);
		Geometry geometry = new Geometry("Axis", arrow);
		Material material = materialService.createWireframeMaterial(color);
		geometry.setMaterial(material);
		geometry.setLocalTranslation(pos);
		return geometry;
	}

	public Geometry createMarker(Vector3f pos, float radius, ColorRGBA color, String text) {
		Sphere sphere = new Sphere(8, 8, radius);
		Geometry geometry = new Geometry("Marker", sphere);
		Material material = materialService.createUnshadedMaterial(color);
		geometry.setMaterial(material);
		geometry.setLocalTranslation(pos);
		return geometry;
	}
}
