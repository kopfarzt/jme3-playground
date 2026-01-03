package at.pst.jme.hello;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.influencers.ParticleInfluencer;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.system.AppSettings;

public class HelloParticles extends SimpleApplication {
	private ParticleEmitter smoke3;
	private float angle;

	public static void main(String[] args) {
		HelloParticles app = new HelloParticles();
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1600, 900);
		settings.setSamples(4);
		app.setSettings(settings);
		app.setShowSettings(false);
		app.start();
	}

	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(10);
		flyCam.setZoomSpeed(15);

		rootNode.attachChild(createGrid());

		ParticleEmitter fire = createFire();
		fire.setLocalTranslation(3, 0, 0);
		rootNode.attachChild(fire);

		ParticleEmitter fire2 = createFire();
		fire2.setLocalTranslation(3, 0, -3);
		rootNode.attachChild(fire2);
		ParticleEmitter smoke2 = createSmoke(ColorRGBA.Brown, 10);
		smoke2.setLocalTranslation(3, 1, -3);
		rootNode.attachChild(smoke2);

		ParticleEmitter smoke = createSmoke(ColorRGBA.White, 10);
		smoke.setLocalTranslation(3, 1, 3);
		rootNode.attachChild(smoke);

		smoke3 = createSmoke(ColorRGBA.Blue, 50);
		smoke3.setStartSize(0.5f);
		smoke3.setEndSize(1f);
		rootNode.attachChild(smoke3);

		ParticleEmitter debris = createDebris();
		debris.setLocalTranslation(0, 0, 0);
		rootNode.attachChild(debris);
		debris.emitAllParticles();

		ParticleEmitter explosion = createExplosion();
		explosion.setLocalTranslation(-3, 1, 0);
		rootNode.attachChild(explosion);

		cam.setLocation(new Vector3f(0, 6, 10));
		cam.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
	}

	private Spatial createGrid() {
		Geometry geometry = new Geometry("GRID_XY", new Grid(100, 100, 0.1f));
		Material material = new Material(assetManager, "/Common/MatDefs/Misc/Unshaded.j3md");
		material.getAdditionalRenderState().setWireframe(true);
		material.setColor("Color", ColorRGBA.DarkGray);
		geometry.setMaterial(material);
		geometry.center();
		return geometry;
	}

	private ParticleEmitter createFire() {
		ParticleEmitter em = createEmitter("Fire", "Effects/Explosion/flame.png", 2, 2, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), 0.2f, 30);

		em.setStartColor(new ColorRGBA(1, 1, 0, 1));
		em.setEndColor(new ColorRGBA(1, 0, 0, 0));
		em.setStartSize(1.5f);
		em.setEndSize(0.01f);
		em.setLowLife(1);
		em.setHighLife(3);
		em.setRotateSpeed(2);

		return em;
	}

	private ParticleEmitter createDebris() {
		ParticleEmitter em = createEmitter("Debris", "Effects/Explosion/Debris.png", 3, 3, new Vector3f(0, 6, 0), new Vector3f(0, 6, 0), 0.6f, 100);

		em.setRotateSpeed(4);
		em.setSelectRandomImage(true);
		em.setStartColor(ColorRGBA.White);
		em.setStartSize(0.1f);
		em.setEndSize(0.1f);

		return em;
	}

	private ParticleEmitter createExplosion() {
		ParticleEmitter em = createEmitter("Flash", "Effects/Explosion/flash.png", 2, 2, new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.6f, 5);

		em.setRotateSpeed(0.1f);
		em.setSelectRandomImage(true);
		em.setStartColor(ColorRGBA.White);
		em.setEndColor(new ColorRGBA(0, 0, 1, 0));
		em.setStartSize(1.5f);
		em.setEndSize(0.01f);
		em.setLowLife(1);
		em.setHighLife(3);

		return em;
	}

	private ParticleEmitter createSmoke(ColorRGBA color, int count) {
		ParticleEmitter em = createEmitter("Flash", "Effects/Smoke/Smoke.png", 15, 1, new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), 0.6f, count);

		em.setRotateSpeed(0.1f);
		em.setSelectRandomImage(true);
		em.setStartColor(color);
		em.setLowLife(1);
		em.setHighLife(3);

		return em;
	}


	private ParticleEmitter createEmitter(String name, String textureName, int imagesX, int imagesY, Vector3f gravity, Vector3f initialVelocity, float velocityVariation, int count) {
		ParticleEmitter emitter = new ParticleEmitter(name, ParticleMesh.Type.Triangle, count);
		Material material = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
		material.setTexture("Texture", assetManager.loadTexture(textureName));
		emitter.setMaterial(material);
		emitter.setImagesX(imagesX);
		emitter.setImagesY(imagesY);

		emitter.setGravity(gravity);
		ParticleInfluencer particleInfluencer = emitter.getParticleInfluencer();
		particleInfluencer.setInitialVelocity(initialVelocity);
		particleInfluencer.setVelocityVariation(velocityVariation);
		return emitter;
	}

	@Override
	public void simpleUpdate(float tpf) {
		angle += 0.3f * FastMath.DEG_TO_RAD;

		smoke3.setLocalTranslation(FastMath.cos(angle) * 3, 1, FastMath.sin(angle) * 3);
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}
}

