package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

public class HelloStaticBody extends SimpleApplication {

	public static void main(String[] args) {
		HelloStaticBody app = new HelloStaticBody();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		bulletAppState.setDebugEnabled(true);
		// bulletAppState.setSpeed(0.1f);

		CollisionShape ballShape = new SphereCollisionShape(1f);

		PhysicsRigidBody ball1 = new PhysicsRigidBody(ballShape, 2f);
		physicsSpace.addCollisionObject(ball1);
		ball1.setPhysicsLocation(new Vector3f(0, 4, 0));

		PhysicsRigidBody ball2 = new PhysicsRigidBody(ballShape, PhysicsBody.massForStatic);
		physicsSpace.addCollisionObject(ball2);
		ball2.setPhysicsLocation(new Vector3f(0.1f, 0, 0));

		// ball2.applyCentralImpulse(new Vector3f(-25, 0, 0));
	}
}
