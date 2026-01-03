package zib.grimble.jme3;

import com.jme3.math.Vector3f;

/**
 * Simple grid parallel to the XZ plane. if the y values
 * of min and max are not the same, the plane will be
 * tilted along the z-axes.
 */
public class XZGrid {
    private Vector3f min;
    private Vector3f max;
    private int xSteps;
    private int ySteps;
    private Vector3f cur;
    private Vector3f xStep;
    private Vector3f zStep;
    private String currentName;
    private String nameFormat;

    public XZGrid(String nameFormat, Vector3f min, Vector3f max, int xSteps, int zSteps) {
        this.nameFormat = nameFormat;
        this.min = min;
        this.max = max;
        this.xSteps = xSteps;
        this.ySteps = zSteps;
        cur = new Vector3f(0, 0, 0);
        xStep = xSteps > 1 ? new Vector3f(max.x - min.x, max.y - min.y, 0).mult(1f / (xSteps - 1)) : Vector3f.ZERO;
        zStep = zSteps > 1 ? new Vector3f(0, 0, max.z - min.z).mult(1f / (zSteps - 1)) : Vector3f.ZERO;
    }

    public Vector3f next() {
        cur.x += 1;
        if (cur.x >= xSteps || cur.z < 0) {
            cur.x = 0;
            cur.z += 1;
        }

        return get(cur);
    }

    public Vector3f nextLine() {
        cur.x = 0;
        cur.z += 1;
        return get(cur);
    }

    public Vector3f get(Vector3f pos) {
        return min.add(xStep.mult(pos.x)).add(zStep.mult(pos.z));
    }

    public Vector3f current() {
        return get(cur);
    }

    public String currentName() {
        return nameFormat.formatted(cur.x, cur.z);
    }
}
