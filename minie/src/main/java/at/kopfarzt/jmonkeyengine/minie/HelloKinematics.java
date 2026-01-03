package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class HelloKinematics extends SimpleApplication implements PhysicsTickListener {

	public static void main(String[] args) {
		HelloKinematics app = new HelloKinematics();
		app.start();
	}

	private float elapsedTime = 0;
	private PhysicsRigidBody kinematicBall;

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
		physicsSpace.addTickListener(this);

		bulletAppState.setDebugEnabled(true);
		// bulletAppState.setSpeed(0.1f);

		CollisionShape ballShape = new SphereCollisionShape(1f);

		PhysicsRigidBody ball1 = new PhysicsRigidBody(ballShape, 2f);
		physicsSpace.addCollisionObject(ball1);
		ball1.setPhysicsLocation(new Vector3f(0, 4, 0));

		kinematicBall = new PhysicsRigidBody(ballShape);
		physicsSpace.addCollisionObject(kinematicBall);
		kinematicBall.setKinematic(true);

		// ball2.applyCentralImpulse(new Vector3f(-25, 0, 0));
	}

	public void prePhysicsTick(PhysicsSpace space, float timeStep) {
		float angle = elapsedTime * FastMath.PI / 0.8f;

		float radius = 0.4f;

		float x = radius * FastMath.sin(angle);
		float y = radius * FastMath.cos(angle);
		kinematicBall.setPhysicsLocation(new Vector3f(x, y, 0));
		elapsedTime += timeStep;
	}

	public void physicsTick(PhysicsSpace space, float timeStep) {
	}
}
