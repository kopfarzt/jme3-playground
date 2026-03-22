package zib.grimble.jmonkeyengine.simjam;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class VehicleControl extends AbstractControl {
    private static final Logger LOG = LoggerFactory.getLogger(GameState.class);
    private final float radius;
    private final float LEN_TO_RAD = FastMath.DEG_TO_RAD * 0.5f;
    private final float RAD_TO_LEN = 1.0f / LEN_TO_RAD;
    private final UiState uiState;
    private float cur = 0;
    private float speed = 0;
    private float maxAcc = 2;
    private float maxDec = 5;
    private float maxSpeed = 50;
    private Vector3f pos = new Vector3f();
    private Quaternion rotation = new Quaternion();
    private VehicleControl predecessor;
    private Spatial marker;

    public VehicleControl(UiState uiState, float radius, float start, float speed) {
        this.uiState = uiState;
        this.radius = radius;
        this.cur = start * FastMath.DEG_TO_RAD;
        this.speed = speed;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (!uiState.isPaused()) {
            correctSpeed(tpf);
            cur = cur + speed * tpf * LEN_TO_RAD;
            if (cur > FastMath.TWO_PI) {
                cur -= FastMath.TWO_PI;
            }
            pos.set(radius * FastMath.cos(-cur), spatial.getLocalTranslation().y, radius * FastMath.sin(-cur));
            if (marker != null) {
                positionMarker();
            }
            spatial.setLocalTranslation(pos);
            spatial.setLocalRotation(rotation.fromAngleAxis(cur + FastMath.PI, Vector3f.UNIT_Y));
            //LOG.info("speed: %.2f tpf: %.2f cur: %.2f".formatted(speed, tpf, cur));
        }
    }

    private void positionMarker() {
        //LOG.info("position mark: %s (%08x) %s".formatted(spatial, Objects.hashCode(spatial), pos));
        marker.setLocalTranslation(pos.x, pos.y + 1, pos.z);
    }

    protected void correctSpeed(float tpf) {
        if (predecessor != null) {
            var dist = cur - predecessor.cur;
            if (dist < 0) {
                dist += FastMath.TWO_PI;
            }

            dist *= RAD_TO_LEN;

            var speedDiff = speed - predecessor.speed;

            // LOG.info("%s dist: %.2f".formatted(spatial.getName(), dist));
            if (dist > speed * 2) {
                // LOG.info("{}: {}", spatial.getName(), speed);
                if (speed < maxSpeed) {
                    speed += maxAcc * tpf;
                    if (speed > maxSpeed) {
                        speed = maxSpeed;
                    }
                }
            } else {
                if (speedDiff > 0) {
                    speed -= maxDec * tpf;
                    if (speed < 0) {
                        speed = 0;
                    }
                }
            }
        }
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

    public void mark(Spatial marker) {
        LOG.info("Mark %s (%08x)".formatted(spatial, Objects.hashCode(spatial)));
        marker.setCullHint(Spatial.CullHint.Never);
        this.marker = marker;
        positionMarker();
    }

    public void unmark(Spatial marker) {
        LOG.info("Unmark %s (%08x)".formatted(spatial, Objects.hashCode(spatial)));
        marker.setCullHint(Spatial.CullHint.Always);
        this.marker = null;
    }
}
