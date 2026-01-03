package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

public class HelloDamping extends SimpleApplication {

	public static void main(String[] args) {
		HelloDamping app = new HelloDamping();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		bulletAppState.setDebugEnabled(true);
		physicsSpace.setGravity(Vector3f.ZERO);

		BoxCollisionShape cubeShape = new BoxCollisionShape(0.5f);

		PhysicsRigidBody[] cubes = new PhysicsRigidBody[4];

		for (int i = 0; i < cubes.length; i++) {
			PhysicsRigidBody cube = new PhysicsRigidBody(cubeShape, 2);
			cubes[i] = cube;
			physicsSpace.addCollisionObject(cube);
			cube.setEnableSleep(false);
		}

		cubes[0].setPhysicsLocation(new Vector3f(0, 2, 0));
		cubes[1].setPhysicsLocation(new Vector3f(4, 2, 0));
		cubes[2].setPhysicsLocation(new Vector3f(0, -2, 0));
		cubes[3].setPhysicsLocation(new Vector3f(4, -2, 0));

		cubes[0].setDamping(0, 0);
		cubes[1].setDamping(0, 0.95f);
		cubes[2].setDamping(0.95f, 0);
		cubes[3].setDamping(0.95f, 0.95f);

		Vector3f impulse = new Vector3f(-1,  0,  0);
		Vector3f offset = new Vector3f(0, 1, 1);

		for (PhysicsRigidBody cube : cubes) {
			cube.applyImpulse(impulse, offset);
		}
	}
}
