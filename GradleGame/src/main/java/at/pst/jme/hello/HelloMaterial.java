package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

public class HelloMaterial extends SimpleApplication {

    private Spatial bumpyRock;
	private Spatial plane;
	private Spatial cube;

	public static void main(String[] args) {
        HelloMaterial app = new HelloMaterial();
        app.start();
    }

    @Override
    public void simpleInitApp() {
    	// setShowSettings(false);
    	flyCam.setZoomSpeed(-30);
    	flyCam.setMoveSpeed(5);
    	
    	cube = createTexturedCube();
		rootNode.attachChild(cube);
        
        plane = createTransparentPlane();
		rootNode.attachChild(plane);
        
        bumpyRock = createBumbyRock();
		rootNode.attachChild(bumpyRock);
        
        DirectionalLight sun = new DirectionalLight(new Vector3f(1, 0, -2), ColorRGBA.White);
        rootNode.addLight(sun);
    }

	private Spatial createTexturedCube() {
		
		// material
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
		
		// mesh
		Box box = new Box(1, 1, 1);
        
		// geometry
		Geometry geometry = new Geometry("Textured Box", box);
        geometry.setLocalTranslation(-3,  1.1f,  0);
        geometry.setMaterial(material);
        
		return geometry;
	}

    private Spatial createTransparentPlane() {
    	
    	// material
    	Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        material.setTexture("ColorMap", assetManager.loadTexture("Textures/ColoredTex/Monkey.png"));
        material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        // mesh
        Box box = new Box(1, 1, 0.01f);
        
        // geometry
        Geometry geometry = new Geometry("Plane", box);
        geometry.setQueueBucket(Bucket.Transparent);
        geometry.setMaterial(material);
		return geometry;
	}

	private Spatial createBumbyRock() {
		
		// material
		Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		material.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
		material.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
		material.setBoolean("UseMaterialColors", true);
		material.setColor("Diffuse", ColorRGBA.White);
		material.setColor("Specular", ColorRGBA.White);
		material.setFloat("Shininess", 64);
		
		// mesh
		Sphere sphere = new Sphere(32, 32, 2);
		sphere.setTextureMode(TextureMode.Projected);
		TangentBinormalGenerator.generate(sphere);
		
		// geometry
		Geometry geometry = new Geometry("Sphere", sphere);
		geometry.setLocalTranslation(0,  2,  -2);
		geometry.rotate(1.6f, 0, 0);
		geometry.setMaterial(material);
		
		return geometry;
	}

	@Override
    public void simpleUpdate(float tpf) {
		cube.rotate(0.01f * FastMath.DEG_TO_RAD, 0, -0.03f * FastMath.DEG_TO_RAD);
		plane.rotate(0f * FastMath.DEG_TO_RAD, 0.01f * FastMath.DEG_TO_RAD, 0f * FastMath.DEG_TO_RAD);
        bumpyRock.rotate(0.07f * FastMath.DEG_TO_RAD, 0, 0.05f * FastMath.DEG_TO_RAD);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}

