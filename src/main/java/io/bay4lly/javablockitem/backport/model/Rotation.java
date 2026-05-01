package io.bay4lly.javablockitem.backport.model;

/**
 * Represents the rotation of a cube element.
 * 
 * Vanilla Minecraft only supports rotation angles that are multiples of 22.5°
 * (i.e., -45, -22.5, 0, 22.5, 45). This class supports ANY float angle,
 * which is the key feature this mod backports.
 * 
 * JSON format:
 * {
 *   "origin": [8, 8, 8],
 *   "axis": "y",
 *   "angle": -30,
 *   "rescale": false
 * }
 */
public class Rotation {

    /** The rotation axis: "x", "y", or "z". Null if 3D rotation. */
    private final String axis;

    /** The rotation angle in degrees for 1D rotation */
    private final float angle;

    // 3D rotation angles
    private final float angleX;
    private final float angleY;
    private final float angleZ;
    private final boolean is3D;

    /** The pivot point (origin) of the rotation [x, y, z] in model space (0-16) */
    private final float[] origin;

    /** Whether to rescale the element to fit after rotation */
    private final boolean rescale;

    // 1D rotation constructor
    public Rotation(String axis, float angle, float[] origin, boolean rescale) {
        this.axis = axis;
        this.angle = angle;
        this.angleX = 0;
        this.angleY = 0;
        this.angleZ = 0;
        this.is3D = false;
        this.origin = origin;
        this.rescale = rescale;
    }

    // 3D rotation constructor
    public Rotation(float x, float y, float z, float[] origin, boolean rescale) {
        this.axis = null;
        this.angle = 0;
        this.angleX = x;
        this.angleY = y;
        this.angleZ = z;
        this.is3D = true;
        this.origin = origin;
        this.rescale = rescale;
    }

    public String getAxis() {
        return axis;
    }

    public float getAngle() {
        return angle;
    }

    public float getAngleX() {
        return angleX;
    }

    public float getAngleY() {
        return angleY;
    }

    public float getAngleZ() {
        return angleZ;
    }

    public boolean is3D() {
        return is3D;
    }

    public float[] getOrigin() {
        return origin;
    }

    public boolean isRescale() {
        return rescale;
    }

    public boolean hasMultiAxis() {
        return is3D;
    }

    /**
     * Check if this rotation is compatible with vanilla Minecraft's model system.
     * Vanilla only supports single axis angles that are multiples of 22.5°.
     */
    public boolean isVanillaCompatible() {
        if (is3D) {
            // 3D rotations are never vanilla compatible unless all are 0
            return angleX == 0 && angleY == 0 && angleZ == 0;
        }

        if (angle == 0.0f) return true;

        // Check if angle is an exact multiple of 22.5
        float divided = angle / 22.5f;
        // Allow small floating-point tolerance
        return Math.abs(divided - Math.round(divided)) < 0.001f
                && Math.abs(angle) <= 45.0f;
    }

    @Override
    public String toString() {
        if (is3D) {
            return String.format("Rotation{3D, angles=[%.1f, %.1f, %.1f], origin=[%f, %f, %f], rescale=%b}", 
                angleX, angleY, angleZ, origin[0], origin[1], origin[2], rescale);
        }
        return "Rotation{axis=" + axis + ", angle=" + angle
                + ", origin=[" + origin[0] + "," + origin[1] + "," + origin[2] + "]"
                + ", rescale=" + rescale
                + ", vanillaOK=" + isVanillaCompatible() + "}";
    }
}
