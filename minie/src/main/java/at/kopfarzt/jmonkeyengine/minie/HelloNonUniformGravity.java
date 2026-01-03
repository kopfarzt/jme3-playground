package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

import jme3utilities.debug.AxesVisualizer;
import jme3utilities.minie.FilterAll;

public class HelloNonUniformGravity extends SimpleApplication implements PhysicsTickListener {

	public static void main(String[] args) {
		HelloNonUniformGravity app = new HelloNonUniformGravity();
		app.start();
	}

	private float elapsedTime = 0;
	private PhysicsRigidBody planet;
	private Vector3f tmpVector = new Vector3f();

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		physicsSpace.addTickListener(this);
		physicsSpace.setAccuracy(0.005f);

		bulletAppState.setDebugEnabled(true);
		bulletAppState.setDebugGravityVectorFilter(new FilterAll(true));
		// bulletAppState.setSpeed(0.1f);

		CollisionShape planetShape = new SphereCollisionShape(0.1f);

		planet = new PhysicsRigidBody(planetShape, 1);
		physicsSpace.addCollisionObject(planet);

		planet.setEnableSleep(false);

		planet.setPhysicsLocation(new Vector3f(2, 0, 0));
		planet.applyCentralImpulse(new Vector3f(0, -1, 0));

		AxesVisualizer axesVisualizer = new AxesVisualizer(assetManager, 1);
		axesVisualizer.setLineWidth(AxesVisualizer.widthForSolid);

		rootNode.addControl(axesVisualizer);
		axesVisualizer.setEnabled(true);
	}

	public void prePhysicsTick(PhysicsSpace space, float timeStep) {

		planet.getPhysicsLocation(tmpVector);

		float r2 = tmpVector.lengthSquared();
		tmpVector.normalizeLocal();
		tmpVector.multLocal(-3f / r2);
		planet.setGravity(tmpVector);
	}

	public void physicsTick(PhysicsSpace space, float timeStep) {
	}
}
