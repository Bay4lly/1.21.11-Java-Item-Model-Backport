package io.bay4lly.javablockitem.backport.model;

/**
 * Enumeration of the 6 cube face directions.
 * Maps to/from JSON direction strings.
 */
public enum FaceDirection {
    NORTH("north",  0,  0, -1),
    SOUTH("south",  0,  0,  1),
    EAST("east",    1,  0,  0),
    WEST("west",   -1,  0,  0),
    UP("up",        0,  1,  0),
    DOWN("down",    0, -1,  0);

    private final String jsonName;
    private final float nx, ny, nz;

    FaceDirection(String jsonName, float nx, float ny, float nz) {
        this.jsonName = jsonName;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
    }

    public String getJsonName() {
        return jsonName;
    }

    /** Get the face normal X component */
    public float getNx() { return nx; }
    /** Get the face normal Y component */
    public float getNy() { return ny; }
    /** Get the face normal Z component */
    public float getNz() { return nz; }

    /**
     * Get the 4 vertex positions for this face on a unit cube (from/to in 0-1 range).
     * Returns float[4][3] — 4 vertices, each with x,y,z.
     * Winding order is counter-clockwise when viewed from outside the cube.
     */
    public float[][] getVertices(float x0, float y0, float z0, float x1, float y1, float z1) {
        return switch (this) {
            case NORTH -> new float[][]{  // -Z face
                    {x1, y1, z0},
                    {x1, y0, z0},
                    {x0, y0, z0},
                    {x0, y1, z0}
            };
            case SOUTH -> new float[][]{  // +Z face
                    {x0, y1, z1},
                    {x0, y0, z1},
                    {x1, y0, z1},
                    {x1, y1, z1}
            };
            case EAST -> new float[][]{   // +X face
                    {x1, y1, z1},
                    {x1, y0, z1},
                    {x1, y0, z0},
                    {x1, y1, z0}
            };
            case WEST -> new float[][]{   // -X face
                    {x0, y1, z0},
                    {x0, y0, z0},
                    {x0, y0, z1},
                    {x0, y1, z1}
            };
            case UP -> new float[][]{     // +Y face
                    {x0, y1, z0},
                    {x0, y1, z1},
                    {x1, y1, z1},
                    {x1, y1, z0}
            };
            case DOWN -> new float[][]{   // -Y face
                    {x0, y0, z1},
                    {x0, y0, z0},
                    {x1, y0, z0},
                    {x1, y0, z1}
            };
        };
    }

    /**
     * Parse a JSON direction string to a FaceDirection.
     */
    public static FaceDirection fromString(String name) {
        if (name == null) return null;
        return switch (name.toLowerCase()) {
            case "north" -> NORTH;
            case "south" -> SOUTH;
            case "east" -> EAST;
            case "west" -> WEST;
            case "up" -> UP;
            case "down" -> DOWN;
            default -> null;
        };
    }

    public net.minecraft.util.math.Direction toVanilla() {
        return switch (this) {
            case NORTH -> net.minecraft.util.math.Direction.NORTH;
            case SOUTH -> net.minecraft.util.math.Direction.SOUTH;
            case EAST -> net.minecraft.util.math.Direction.EAST;
            case WEST -> net.minecraft.util.math.Direction.WEST;
            case UP -> net.minecraft.util.math.Direction.UP;
            case DOWN -> net.minecraft.util.math.Direction.DOWN;
        };
    }
}
