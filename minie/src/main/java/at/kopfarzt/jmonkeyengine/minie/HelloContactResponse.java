package at.kopfarzt.jmonkeyengine.minie;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;

public class HelloContactResponse extends SimpleApplication {

	public static void main(String[] args) {
		HelloContactResponse app = new HelloContactResponse();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();

		bulletAppState.setDebugEnabled(true);
		// bulletAppState.setSpeed(0.1f);

		CollisionShape boxShape = new BoxCollisionShape(3f);
		PhysicsRigidBody box = new PhysicsRigidBody(boxShape, PhysicsBody.massForStatic);
		physicsSpace.addCollisionObject(box);
		box.setPhysicsLocation(new Vector3f(0, -4, 0));


		CollisionShape ballShape = new SphereCollisionShape(0.3f);

		final PhysicsRigidBody ball = new PhysicsRigidBody(ballShape, 2f);
		physicsSpace.addCollisionObject(ball);
		assert ball.isContactResponse();

		ball.setPhysicsLocation(new Vector3f(0, 4, 0));

		ActionListener actionListener = new ActionListener() {
			public void onAction(String action, boolean isPressed, float tpf) {
				if(action.equals("freefall") && isPressed) {
					ball.setContactResponse(false);
					ball.activate();
				}
			}
		};

		inputManager.addMapping("freefall", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(actionListener, "freefall");
	}
}
