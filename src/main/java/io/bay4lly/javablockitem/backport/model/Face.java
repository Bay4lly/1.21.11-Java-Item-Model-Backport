package io.bay4lly.javablockitem.backport.model;

/**
 * Represents a single face of a cube element.
 * 
 * JSON format:
 * {
 *   "uv": [u1, v1, u2, v2],
 *   "texture": "#0",
 *   "rotation": 0,
 *   "cullface": "north",
 *   "tintindex": -1
 * }
 */
public class Face {

    /** UV coordinates [u1, v1, u2, v2] in 0-16 range */
    private final float[] uv;

    /** Texture reference (e.g., "#0", "#texture") */
    private final String texture;

    /** Face UV rotation in degrees (0, 90, 180, 270) */
    private final int uvRotation;

    /** Cull face direction, or null if no culling */
    private final FaceDirection cullface;

    /** Tint index for color providers (-1 = none) */
    private final int tintIndex;

    public Face(float[] uv, String texture, int uvRotation, FaceDirection cullface, int tintIndex) {
        this.uv = uv;
        this.texture = texture;
        this.uvRotation = uvRotation;
        this.cullface = cullface;
        this.tintIndex = tintIndex;
    }

    public float[] getUv() {
        return uv;
    }

    public String getTexture() {
        return texture;
    }

    public int getUvRotation() {
        return uvRotation;
    }

    public FaceDirection getCullface() {
        return cullface;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    /**
     * Get normalized UV coordinates (0.0-1.0 range from 0-16 model range).
     */
    public float[] getNormalizedUv() {
        return new float[]{
                uv[0] / 16f,
                uv[1] / 16f,
                uv[2] / 16f,
                uv[3] / 16f
        };
    }
}
