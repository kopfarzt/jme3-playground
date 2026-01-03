package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

public class HelloDeactivation extends SimpleApplication implements PhysicsTickListener {

	public static void main(String[] args) {
		HelloDeactivation app = new HelloDeactivation();
		app.start();
	}

	private PhysicsRigidBody dynamicCube;
	private PhysicsRigidBody supportCube;

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		physicsSpace.addTickListener(this);

		bulletAppState.setDebugEnabled(true);


		BoxCollisionShape smallCubeShape = new BoxCollisionShape(0.5f);
		dynamicCube = new PhysicsRigidBody(smallCubeShape, 1);
		physicsSpace.addCollisionObject(dynamicCube);
		dynamicCube.setPhysicsLocation(new Vector3f(0, 4, 0));

		BoxCollisionShape largeCubeShape = new BoxCollisionShape(1);
		supportCube = new PhysicsRigidBody(largeCubeShape, PhysicsBody.massForStatic);
		physicsSpace.addCollisionObject(supportCube);

		SphereCollisionShape ballShape = new SphereCollisionShape(0.5f);
		PhysicsRigidBody bottomBody = new PhysicsRigidBody(ballShape, PhysicsBody.massForStatic);
		bottomBody.setPhysicsLocation(new Vector3f(0, -2, 0));
		physicsSpace.addCollisionObject(bottomBody);
	}

	public void prePhysicsTick(PhysicsSpace space, float timeStep) {
	}

	public void physicsTick(PhysicsSpace space, float timeStep) {
		if(!dynamicCube.isActive() && space.contains(supportCube)) {
			space.removeCollisionObject(supportCube);
		}
	}
}
