package zib.grimble.jme3.spheretriangulation;

import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Cylinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spring extends AbstractControl {
    private static final Logger LOG = LoggerFactory.getLogger(Spring.class);
    private Mass m1;
    private Mass m2;
    private float restLength;
    private float springConstant;
    private float damping;
    private Geometry geometry;

    private Spring() {
    }

    public static Spring createSpring(String name, Mass m1, Mass m2, float springConstant, float damping, float radius, Material material) {
        var s = new Spring();
        s.setM1(m1);
        s.setM2(m2);
        s.setRestLength(m1.getRigidBodyControl().getPhysicsLocation().distance(m2.getRigidBodyControl().getPhysicsLocation()));
        s.setSpringConstant(springConstant);
        s.setDamping(damping);

        var mesh = new Cylinder(2, 10, radius, 1);
        var geometry = new Geometry(name, mesh);
        geometry.setMaterial(material);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        s.setGeometry(geometry);

        s.updateGeometry();

        return s;
    }

    protected void updateGeometry() {
        var l1 = m1.getRigidBodyControl().getPhysicsLocation();
        var l2 = m2.getRigidBodyControl().getPhysicsLocation();

        var diff = l2.subtract(l1);
        var length = diff.length();
        var mid = l1.add(diff.mult(0.5f));
        geometry.setLocalScale(1, 1, length);

        var q = new Quaternion();
        q.lookAt(diff.normalizeLocal(), Vector3f.UNIT_Y);
        geometry.setLocalRotation(q);

        geometry.setLocalTranslation(mid);
    }

    @Override
    protected void controlUpdate(float tpf) {
        var body1 = m1.getRigidBodyControl();
        var l1 = body1.getPhysicsLocation();
        var v1 = body1.getLinearVelocity();

        var body2 = m2.getRigidBodyControl();
        var l2 = body2.getPhysicsLocation();
        var v2 = body2.getLinearVelocity();

        // direction
        var dir = l2.subtract(l1);

        // current length
        var curLen = dir.length();

        // normalized direction
        dir.normalize();


        // relative velocity
        var rv = v2.subtract(v1);
        // LOG.info("curLen: {}, dir: {}, rv: {}", curLen, dir, rv);

        // velocity along direction
        var v = rv.dot(dir);

        // force
        var f = -springConstant * (curLen - restLength) - damping * v;
        if (Math.abs(f) > 0.01) {
            var force = dir.mult(f);

            body2.applyCentralForce(force);
            body1.applyCentralForce(force.negate());
        }
        updateGeometry();
    }

    @Override
    protected void controlRender(RenderManager renderManager, ViewPort viewPort) {
    }


    public float getRestLength() {
        return restLength;
    }

    public void setRestLength(float restLength) {
        this.restLength = restLength;
    }

    public Mass getM2() {
        return m2;
    }

    public void setM2(Mass m2) {
        this.m2 = m2;
    }

    public Mass getM1() {
        return m1;
    }

    public void setM1(Mass m1) {
        this.m1 = m1;
    }

    public float getSpringConstant() {
        return springConstant;
    }

    public void setSpringConstant(float springConstant) {
        this.springConstant = springConstant;
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "%s{name: %s, m1: %s, m2: %s}".formatted(getClass().getName(), getGeometry().getName(), getM1(), getM2());
    }
}
