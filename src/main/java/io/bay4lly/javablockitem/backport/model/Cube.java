package io.bay4lly.javablockitem.backport.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a single cube element in a model.
 * 
 * Mirrors the structure of a Minecraft model JSON "elements" entry:
 * {
 *   "from": [x, y, z],
 *   "to": [x, y, z],
 *   "rotation": { ... },
 *   "faces": { "north": { ... }, ... }
 * }
 */
public class Cube {

    /** Corner coordinates: from[0]=x, from[1]=y, from[2]=z (0-16 scale) */
    private final float[] from;

    /** Corner coordinates: to[0]=x, to[1]=y, to[2]=z (0-16 scale) */
    private final float[] to;

    /** Optional rotation applied to this element */
    private Rotation rotation;

    /** Per-face data, keyed by direction */
    private final Map<FaceDirection, Face> faces;

    /** Whether to shade this element (default true) */
    private boolean shade = true;

    public Cube(float[] from, float[] to) {
        this.from = from;
        this.to = to;
        this.faces = new EnumMap<>(FaceDirection.class);
    }

    // --- Getters/Setters ---

    public float[] getFrom() {
        return from;
    }

    public float[] getTo() {
        return to;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Map<FaceDirection, Face> getFaces() {
        return faces;
    }

    public void addFace(FaceDirection direction, Face face) {
        faces.put(direction, face);
    }

    public boolean isShade() {
        return shade;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    /**
     * Get the center of this cube (used for default rotation origin).
     */
    public float[] getCenter() {
        return new float[]{
                (from[0] + to[0]) / 2f,
                (from[1] + to[1]) / 2f,
                (from[2] + to[2]) / 2f
        };
    }
}
