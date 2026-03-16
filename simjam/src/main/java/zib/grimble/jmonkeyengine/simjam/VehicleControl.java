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
    private Vector3f pos = new Vector3f();
    private float speed = 0;
    private Quaternion rotation = new Quaternion();

    public VehicleControl(float radius, float start, float speed) {
        this.radius = radius;
        this.cur = start * FastMath.DEG_TO_RAD;
        this.speed = speed;
    }

    @Override
    protected void controlUpdate(float tpf) {
        cur = cur + speed * tpf * FastMath.DEG_TO_RAD;
        pos.set(radius * FastMath.cos(cur), spatial.getLocalTranslation().y, radius * FastMath.sin(cur));
        spatial.setLocalTranslation(pos);
        spatial.setLocalRotation(rotation.fromAngleAxis(-cur, Vector3f.UNIT_Y));
        //LOG.info("speed: %.2f tpf: %.2f cur: %.2f".formatted(speed, tpf, cur));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
