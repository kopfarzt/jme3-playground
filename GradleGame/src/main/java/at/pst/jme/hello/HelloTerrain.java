package at.pst.jme.hello;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

public class HelloTerrain extends SimpleApplication {
	private static final Logger LOG = Logger.getLogger(HelloTerrain.class.getName());

	public static void main(String[] args) {
		HelloTerrain app = new HelloTerrain();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(50);
		viewPort.setBackgroundColor(ColorRGBA.Blue.interpolateLocal(ColorRGBA.White, 0.7f));

		Material terrainMaterial = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
		terrainMaterial.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

		setTerrainTexture(terrainMaterial, "Tex1", "Textures/Terrain/splat/grass.jpg", 64f);
		setTerrainTexture(terrainMaterial, "Tex2", "Textures/Terrain/splat/dirt.jpg", 32f);
		setTerrainTexture(terrainMaterial, "Tex3", "Textures/Terrain/splat/road.jpg", 128f);


		HeightMap heightMap = null;

		//		Texture heightTexture = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
		//		heightMap = new ImageBasedHeightMap(heightTexture.getImage());
		//		boolean loadedHeight = heightMap.load();
		//		LOG.log(Level.INFO, "Loaded height: {0}", loadedHeight);

		try {
			heightMap = new HillHeightMap(513, 10000, 10, 40);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e, () -> "Error generating heightMap.");
		}


		TerrainQuad terrainQuad = new TerrainQuad("terrain", 65, 513, heightMap.getHeightMap());

		terrainQuad.setMaterial(terrainMaterial);
		terrainQuad.setLocalTranslation(0, -200, 0);
		terrainQuad.setLocalScale(2, 1, 2);

		rootNode.attachChild(terrainQuad);

		TerrainLodControl terrainLodControl = new TerrainLodControl(terrainQuad, cam);
		terrainQuad.addControl(terrainLodControl);



	}

	private void setTerrainTexture(Material material, String key, String name, float scale) {
		Texture texture = assetManager.loadTexture(name);
		texture.setWrap(WrapMode.Repeat);
		material.setTexture(key, texture);
		material.setFloat(key + "Scale", scale);
	}

	@Override
	public void simpleUpdate(float tpf) {
		//TODO: add update code
	}

	@Override
	public void simpleRender(RenderManager rm) {
		//TODO: add render code
	}
}

