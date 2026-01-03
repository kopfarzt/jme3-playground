package zib.grimble.jme3.spheretriangulation;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zib.grimble.jme3.geometry.ParameterizedSurfaceGrid;
import zib.grimble.jme3.geometry.psurfaces.MeshSphere;

public class Mass {
    private static final Logger LOG = LoggerFactory.getLogger(Mass.class);
    private float mass;
    private Geometry geometry;
    private Mesh mesh;
    private RigidBodyControl rigidBodyControl;

    private Mass() {
    }

    public static Mass createMass(String name, Vector3f location, float mass, Material material) {
        var m = new Mass();

        var radius = mass / 20;

        var mesh1 = new ParameterizedSurfaceGrid(new MeshSphere(radius), null, 20, 10, true, true);
        LOG.info("Bound 1: {}", mesh1.getBound());
//        var mesh2 = new Sphere(20, 30, radius);
//        LOG.info("Bound 2: {}", mesh2.getBound());

        m.setMass(mass);
        m.setMesh(mesh1);

        var geometry = new Geometry(name, m.getMesh());
        geometry.setMaterial(material);
        geometry.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        geometry.setLocalTranslation(location);
        m.setGeometry(geometry);

        var collShape = new SphereCollisionShape(radius);
        var rigidBodyControl = new RigidBodyControl(mass);
        rigidBodyControl.setCollisionShape(collShape);
        geometry.addControl(rigidBodyControl);

        m.setRigidBodyControl(rigidBodyControl);

        rigidBodyControl.setCcdMotionThreshold(0.0001f);
        rigidBodyControl.setCcdSweptSphereRadius(0.0001f);
        rigidBodyControl.setRestitution(0.0001f);

        return m;
    }

    public RigidBodyControl getRigidBodyControl() {
        return rigidBodyControl;
    }

    private void setRigidBodyControl(RigidBodyControl rigidBodyControl) {
        this.rigidBodyControl = rigidBodyControl;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Mesh getMesh() {
        return mesh;
    }

    @Override
    public String toString() {
        return "%s{name: %s, phys: %s, mass: %f}".formatted(getClass().getSimpleName(), getGeometry().getName(), getRigidBodyControl().getPhysicsLocation(), getRigidBodyControl().getMass());
    }
}

