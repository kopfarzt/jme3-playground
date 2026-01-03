package at.kopfarzt.jmonkeyengine.minie;

import java.util.Random;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;

public class HelloMadMallet extends SimpleApplication {

	private static boolean correct = true;

	public static void main(String[] args) {
		correct = new Random().nextBoolean();

		System.out.println("correct mass distribution: " + correct);

		HelloMadMallet app = new HelloMadMallet();
		app.start();
	}

	public Vector3f v3 (float x, float y, float z) {
		return new Vector3f(x, y, z);
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		bulletAppState.setDebugEnabled(true);
		bulletAppState.setDebugAxisLength(1);
		stateManager.attach(bulletAppState);

		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
		physicsSpace.setGravity(v3(0, -50, 0));

		float headLength = 1;
		float headRadius = 0.5f;
		Vector3f hes = v3(headLength / 2, headRadius, headRadius);
		CylinderCollisionShape headShape = new CylinderCollisionShape(hes, PhysicsSpace.AXIS_X);

		float handleLength = 3;
		float handleRadius = 0.3f;
		hes.set(handleRadius, handleRadius, handleLength / 2);
		CylinderCollisionShape handleShape = new CylinderCollisionShape(hes, PhysicsSpace.AXIS_Z);


		CompoundCollisionShape malletShape = new CompoundCollisionShape();
		malletShape.addChildShape(handleShape, 0, 0, handleLength / 2);
		malletShape.addChildShape(headShape, 0, 0, handleLength);

		// individual values are only considered when correction is applied
		float handleMass = 0.5f;
		float headMass = 1f;

		PhysicsRigidBody mallet = new PhysicsRigidBody(malletShape, handleMass + headMass);

		// apply correction?
		if(correct) {

			Vector3f inertiaVector = new Vector3f();
			// head has 75% of mass
			Transform correction = malletShape.principalAxes(BufferUtils.createFloatBuffer(handleMass, headMass), null, inertiaVector);
			malletShape.correctAxes(correction);

			mallet.setInverseInertiaLocal(Vector3f.UNIT_XYZ.divide(inertiaVector));
		}

		mallet.setPhysicsLocation(v3(0, 4, 0));
		mallet.setAngularDamping(0.9f);

		physicsSpace.addCollisionObject(mallet);

		CylinderCollisionShape discShape = new CylinderCollisionShape(5, 0.5f, PhysicsSpace.AXIS_Y);
		PhysicsRigidBody disc = new PhysicsRigidBody(discShape, PhysicsBody.massForStatic);
		physicsSpace.addCollisionObject(disc);
		disc.setPhysicsLocation(v3(0, -3, 0));

		cam.setLocation(v3(10, 2.75f, 0));
		cam.lookAt(v3(0, -2.75f, 0), Vector3f.UNIT_Y);
	}
}
