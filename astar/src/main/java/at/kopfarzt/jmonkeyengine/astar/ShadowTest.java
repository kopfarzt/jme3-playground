package at.kopfarzt.jmonkeyengine.astar;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;

public class ShadowTest extends SimpleApplication  {
	private static final Logger LOG = LoggerFactory.getLogger(ShadowTest.class);
	private float dirLightAngle = 0;
	private Vector3f dirLight = new Vector3f();

	public static void main(String[] args) {
		ShadowTest app = new ShadowTest();
		app.start();
	}

	private void setShadowMode(ShadowMode shadowMode, Spatial...spatials) {
		for (Spatial spatial : spatials) {
			spatial.setShadowMode(shadowMode);
		}
	}

	private static final Random random = new Random();
	private DirectionalLight directionalLight;

	@Override
	public void simpleInitApp() {

		Material material1 = createShadedMaterial(ColorRGBA.fromRGBA255(255, 192, 0, 255).mult(0.5f));
		Material material2 = createShadedMaterial(ColorRGBA.fromRGBA255(255, 0, 0, 255));

		Node shadowGroup = new Node("Shadow Group");

		for(int i=0; i<80; i++) {
			Spatial s = createSphere("s" + i,
					random.nextFloat(-10f,  10f),
					random.nextFloat(1f,  10f),
					random.nextFloat(-10f,  10f),
					random.nextFloat(0.2f,  1.5f),
					material1);
			shadowGroup.attachChild(s);
			s.setShadowMode(ShadowMode.CastAndReceive);
		}

		Spatial groundPlane = createGroundPlane(material2);
		shadowGroup.attachChild(groundPlane);
		groundPlane.setShadowMode(ShadowMode.CastAndReceive);

		rootNode.attachChild(shadowGroup);

		AmbientLight ambientLight = new AmbientLight(ColorRGBA.fromRGBA255(8, 8, 16, 0));
		rootNode.addLight(ambientLight);

		initCamera();
		//		setDisplayFps(false);
		//		setDisplayStatView(false);

		attachCoordinateAxes(Vector3f.ZERO);

		directionalLight = new DirectionalLight(new Vector3f(0, -1f, 0), ColorRGBA.White);
		rootNode.addLight(directionalLight);

		int shadowMapSize = 4096;
		int nbSplits = 3;

		addShadowRenderer(directionalLight, shadowMapSize, nbSplits);
		// addShadowFilter(directionalLight, shadowMapSize, nbSplits);
	}

	private void addShadowRenderer(DirectionalLight light, int shadowMapSize, int nbSplits) {
		DirectionalLightShadowRenderer shadowRenderer = new DirectionalLightShadowRenderer(assetManager, shadowMapSize, nbSplits);
		shadowRenderer.setLight(light);
		viewPort.addProcessor(shadowRenderer);
	}

	private void addShadowFilter(DirectionalLight light, int shadowMapSize, int nbSplits) {
		DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, shadowMapSize, nbSplits);
		shadowFilter.setLight(light);
		shadowFilter.setEnabled(true);
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		fpp.addFilter(shadowFilter);
		viewPort.addProcessor(fpp);
	}

	private void initCamera() {
		flyCam.setZoomSpeed(10);
		flyCam.setMoveSpeed(10);
		cam.setLocation(new Vector3f(0, 20, 20));
		cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
	}

	private Material createShadedMaterial(ColorRGBA color) {
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Ambient", color);
		material.setColor("Diffuse", color);
		material.setColor("Specular", color);
		material.setFloat("Shininess", 64);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	private Spatial createGroundPlane(Material material) {
		Box box = new Box(10, 0.1f, 10);
		Geometry geometry = new Geometry("GroundPlane", box);
		geometry.setMaterial(material);
		geometry.setLocalTranslation(0, -0.01f, 0);

		return geometry;
	}


	private Spatial createSphere(String name, float x, float y, float z, float r, Material material) {
		Sphere object = new Sphere(32, 32, r);
		// Box object = new Box(new Vector3f(-r, -0.1f, -r), new Vector3f(r, 0.1f, r));
		Geometry geometry = new Geometry(name, object);
		geometry.setMaterial(material);
		geometry.setLocalTranslation(x, y, z);

		return geometry;
	}

	private void attachCoordinateAxes(Vector3f pos) {
		Arrow arrow = new Arrow(Vector3f.UNIT_X);
		putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Y);
		putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

		arrow = new Arrow(Vector3f.UNIT_Z);
		putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
	}

	private Geometry putShape(Mesh shape, ColorRGBA color) {
		Geometry g = new Geometry("coordinate axis", shape);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.getAdditionalRenderState().setLineWidth(4);
		mat.setColor("Color", color);
		g.setMaterial(mat);
		rootNode.attachChild(g);
		return g;
	}

	@Override
	public void simpleUpdate(float tpf) {
		dirLightAngle += 0.01;
		dirLight.setX(0.2f * FastMath.cos(dirLightAngle));
		dirLight.setY(-1f);
		dirLight.setZ(FastMath.sin(dirLightAngle));
		directionalLight.setDirection(dirLight);
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}
}
