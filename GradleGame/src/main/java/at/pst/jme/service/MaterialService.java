package at.pst.jme.service;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;

public class MaterialService {
	private Application app;

	public MaterialService(Application app) {
		this.app = app;
	}

	/**
	 * Create unshaded material.
	 * 
	 * @param color
	 * @return
	 */
	public Material createUnshadedMaterial(ColorRGBA color) {
		Material material = new Material(app.getAssetManager(), "/Common/MatDefs/Misc/Unshaded.j3md");
		material.setColor("Color", color);
		return material;
	}

	public Material createSimplePhongMaterial(ColorRGBA color) {
		Material material = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
		material.setBoolean("UseMaterialColors", true);
		// material.setBoolean("BackfaceShadows", false);
		material.setColor("Specular", ColorRGBA.White);
		material.setColor("Diffuse", color);
		material.setColor("Ambient", color);
		material.setFloat("Shininess", 64f);
		material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return material;
	}

	/**
	 * Create wireframe material.
	 * 
	 * @param color
	 * @param lineWidth
	 * @return
	 */
	public Material createWireframeMaterial(ColorRGBA color, float lineWidth) {
		Material material = new Material(app.getAssetManager(), "/Common/MatDefs/Misc/Unshaded.j3md");
		material.getAdditionalRenderState().setWireframe(true);
		material.getAdditionalRenderState().setLineWidth(lineWidth);
		material.setColor("Color", color);
		return material;
	}

	/**
	 * Create wireframe material.
	 * 
	 * @param color
	 * @return
	 */
	public Material createWireframeMaterial(ColorRGBA color) {
		return createWireframeMaterial(color, 1);
	}
}
