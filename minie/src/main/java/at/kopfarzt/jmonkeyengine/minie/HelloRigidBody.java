package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

public class HelloRigidBody extends SimpleApplication {

	public static void main(String[] args) {
		HelloRigidBody app = new HelloRigidBody();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		bulletAppState.setDebugEnabled(true);
		bulletAppState.setSpeed(0.1f);

		physicsSpace.setGravity(new Vector3f(0, -100, 0));

		float radius = 0.1f;
		CollisionShape ballShape = new SphereCollisionShape(radius);

		PhysicsRigidBody ccdBall = new PhysicsRigidBody(ballShape, 1f);
		physicsSpace.addCollisionObject(ccdBall);
		ccdBall.setCcdMotionThreshold(radius);
		ccdBall.setCcdSweptSphereRadius(radius);
		ccdBall.setPhysicsLocation(new Vector3f(-1, 4, 0));

		PhysicsRigidBody controlBall = new PhysicsRigidBody(ballShape, 1f);
		physicsSpace.addCollisionObject(controlBall);
		controlBall.setPhysicsLocation(new Vector3f(1, 4, 0));

		CylinderCollisionShape discShape = new CylinderCollisionShape(2, 0.05f, PhysicsSpace.AXIS_Y);
		PhysicsRigidBody disc = new PhysicsRigidBody(discShape, PhysicsBody.massForStatic);
		physicsSpace.addCollisionObject(disc);
	}
}
