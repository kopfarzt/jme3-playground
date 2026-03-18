package zib.grimble.jmonkeyengine.simjam;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleControl extends AbstractControl {
    private static final Logger LOG = LoggerFactory.getLogger(GameState.class);
    private final float radius;
    private float cur = 0;
    private float speed = 0;
    private float maxAcc = 2;
    private float maxDec = 5;
    private float maxSpeed = 50;
    private Vector3f pos = new Vector3f();
    private Quaternion rotation = new Quaternion();
    private VehicleControl predecessor;
    private float LEN_TO_RAD = FastMath.DEG_TO_RAD * 0.5f;
    private float RAD_TO_LEN = 1.0f / LEN_TO_RAD;

    public VehicleControl(float radius, float start, float speed) {
        this.radius = radius;
        this.cur = start * FastMath.DEG_TO_RAD;
        this.speed = speed;
    }

    @Override
    protected void controlUpdate(float tpf) {
        correctSpeed(tpf);
        cur = cur + speed * tpf * LEN_TO_RAD;
        if (cur > FastMath.TWO_PI) {
            cur -= FastMath.TWO_PI;
        }
        pos.set(radius * FastMath.cos(-cur), spatial.getLocalTranslation().y, radius * FastMath.sin(-cur));
        spatial.setLocalTranslation(pos);
        spatial.setLocalRotation(rotation.fromAngleAxis(cur + FastMath.PI, Vector3f.UNIT_Y));
        //LOG.info("speed: %.2f tpf: %.2f cur: %.2f".formatted(speed, tpf, cur));
    }

    protected void correctSpeed(float tpf) {
        var dist = cur - predecessor.cur;
        if (dist < 0) {
            dist += FastMath.TWO_PI;
        }

        dist *= RAD_TO_LEN;

        // LOG.info("{}: {}", spatial.getName(), speed);
        if (speed < maxSpeed && dist > speed * 2) {
            speed += maxAcc * tpf;
            if (speed > maxSpeed) {
                speed = maxSpeed;
            }
        }


        // LOG.info("Dist: %.2f".formatted(dist));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public VehicleControl getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(VehicleControl predecessor) {
        this.predecessor = predecessor;
    }
}
