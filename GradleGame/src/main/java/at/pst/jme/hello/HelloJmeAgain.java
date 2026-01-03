package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class HelloJmeAgain extends SimpleApplication {

	private Spatial autoRotate = null;

	public static void main(String[] args) {
		HelloJmeAgain app = new HelloJmeAgain();
		app.start();
	}

	@Override
	public void simpleInitApp() {

		flyCam.setZoomSpeed(15);
		flyCam.setMoveSpeed(10);

		Material red = phong(ColorRGBA.Red);
		Material green = phong(ColorRGBA.Green);
		Material blue = phong(ColorRGBA.Blue);
		Material gray = phong(ColorRGBA.Gray);
		Material bricks = bricks();

		Geometry x = materialMesh("X", new Line(new Vector3f(-1, 0, 0), new Vector3f(10, 0, 0)), red);
		Geometry y = materialMesh("Y", new Line(new Vector3f(0, -1, 0), new Vector3f(0, 10, 0)), green);
		Geometry z = materialMesh("Z", new Line(new Vector3f(0, 0, -1), new Vector3f(0, 0, 10)), blue);

		rootNode.attachChild(x);
		rootNode.attachChild(y);
		rootNode.attachChild(z);


		//    	Geometry ground = materialMesh("Ground", new Box(5, 0.05f, 5), gray);
		//    	ground.move(0, -0.06f, 0);
		//    	rootNode.attachChild(ground);

		Geometry sphere = materialMesh("Sphere", new Sphere(32, 32, 1.3f), red);
		sphere.setLocalTranslation(-3, 3, 0);
		Geometry box = materialMesh("Box", new Box(1, 1, 1), bricks);
		box.setLocalTranslation(0, 3, 0);
		Geometry cylinder = materialMesh("Cylinder", new Cylinder(2, 32, 1, 5), blue);
		cylinder.setLocalTranslation(3, 3, 0);

		autoRotate = box;


		rootNode.attachChild(sphere);
		rootNode.attachChild(box);
		rootNode.attachChild(cylinder);

		AmbientLight ambientLight = new AmbientLight(ColorRGBA.DarkGray);
		rootNode.addLight(ambientLight);

		DirectionalLight directionalLight = new DirectionalLight(new Vector3f(-1, -1, -1), ColorRGBA.White);
		rootNode.addLight(directionalLight);
	}

	protected Geometry materialMesh(String name, Mesh mesh, Material material) {
		Geometry geometry = new Geometry(name, mesh);
		geometry.setMaterial(material);
		return geometry;
	}

	protected Material phong(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		// material.setBoolean("BackfaceShadows", false);
		material.setColor("Specular", ColorRGBA.White);
		material.setColor("Diffuse", color);
		material.setColor("Ambient", color);
		material.setFloat("Shininess", 64f);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	protected Material bricks() {
		Material material = phong(ColorRGBA.White);
		material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
		material.setTexture("SpecularMap", assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
		return material;
	}

	@Override
	public void simpleUpdate(float tpf) {
		if(autoRotate != null) {
			autoRotate.rotate(0.07f * FastMath.DEG_TO_RAD, 0, 0.05f * FastMath.DEG_TO_RAD);
		}
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}
}

