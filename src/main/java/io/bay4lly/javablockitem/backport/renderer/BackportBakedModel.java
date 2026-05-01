package io.bay4lly.javablockitem.backport.renderer;

import io.bay4lly.javablockitem.backport.model.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BackportBakedModel implements BakedModel {

    private final BakedModel fallbackModel;

    private final List<BakedQuad> generalQuads;
    private final Map<Direction, List<BakedQuad>> faceQuads = new EnumMap<>(Direction.class);
    private final Sprite particleSprite;

    private static final int VERTEX_SIZE = 8;
    private static final int QUAD_SIZE = 4 * VERTEX_SIZE;

    public BackportBakedModel(CustomModel customModel, BakedModel fallbackModel, Identifier modelId, Function<Identifier, Sprite> spriteGetter) {
        this.fallbackModel = fallbackModel;
        this.particleSprite = fallbackModel.getParticleSprite();

        for (Direction dir : Direction.values()) {
            faceQuads.put(dir, new ArrayList<>());
        }
        this.generalQuads = new ArrayList<>();

        buildAllQuads(customModel, spriteGetter);

        // Memory optimization: shrink arrays down to their exact sizes
        ((ArrayList<BakedQuad>) this.generalQuads).trimToSize();
        for (List<BakedQuad> quads : faceQuads.values()) {
            ((ArrayList<BakedQuad>) quads).trimToSize();
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        if (face == null) return generalQuads;
        return faceQuads.getOrDefault(face, Collections.emptyList());
    }

    @Override
    public boolean useAmbientOcclusion() { return fallbackModel.useAmbientOcclusion(); }

    @Override
    public boolean hasDepth() { return fallbackModel.hasDepth(); }

    @Override
    public boolean isSideLit() { return fallbackModel.isSideLit(); }

    @Override
    public boolean isBuiltin() { return false; }

    @Override
    public Sprite getParticleSprite() { return particleSprite; }

    @Override
    public ModelTransformation getTransformation() { return fallbackModel.getTransformation(); }

    @Override
    public ModelOverrideList getOverrides() { return fallbackModel.getOverrides(); }

    private void buildAllQuads(CustomModel customModel, Function<Identifier, Sprite> spriteGetter) {
        for (Cube cube : customModel.getElements()) {
            buildCubeQuads(cube, customModel, spriteGetter);
        }
    }

    private void buildCubeQuads(Cube cube, CustomModel customModel, Function<Identifier, Sprite> spriteGetter) {
        float[] from = cube.getFrom();
        float[] to = cube.getTo();

        float x0 = from[0] / 16f;
        float y0 = from[1] / 16f;
        float z0 = from[2] / 16f;
        float x1 = to[0] / 16f;
        float y1 = to[1] / 16f;
        float z1 = to[2] / 16f;

        Rotation rotation = cube.getRotation();

        for (Map.Entry<FaceDirection, Face> entry : cube.getFaces().entrySet()) {
            FaceDirection dir = entry.getKey();
            Face face = entry.getValue();

            float[][] vertices = dir.getVertices(x0, y0, z0, x1, y1, z1);

            if (rotation != null && (!rotation.isVanillaCompatible() || rotation.getAngle() != 0)) {
                applyRotation(vertices, rotation);
            }

            float[] uv = face.getNormalizedUv();
            applyUvRotation(uv, face.getUvRotation());
            float[] normal = computeNormal(vertices);

            Identifier texIdentifier = customModel.resolveTexture(face.getTexture());
            Sprite sprite = (texIdentifier != null) ? spriteGetter.apply(texIdentifier) : particleSprite;
            if (sprite == null) sprite = particleSprite;

            int[] vertexData = buildQuadVertexData(vertices, uv, normal, sprite);

            BakedQuad quad = new BakedQuad(
                    vertexData,
                    face.getTintIndex(),
                    directionFromNormal(normal),
                    sprite,
                    cube.isShade()
            );

            // CULLFACE HANDLING: If a face has a cullface property, assign it to that direction
            if (face.getCullface() != null) {
                Direction cullDir = face.getCullface().toVanilla();
                if (cullDir != null) {
                    faceQuads.get(cullDir).add(quad);
                    continue;
                }
            }
            generalQuads.add(quad);
        }
    }

    private void applyRotation(float[][] vertices, Rotation rotation) {
        float[] origin = rotation.getOrigin();
        float ox = origin[0] / 16f, oy = origin[1] / 16f, oz = origin[2] / 16f;

        for (float[] v : vertices) { v[0] -= ox; v[1] -= oy; v[2] -= oz; }

        if (rotation.is3D()) {
            if (rotation.getAngleX() != 0) applyRotationAxis(vertices, "x", rotation.getAngleX());
            if (rotation.getAngleY() != 0) applyRotationAxis(vertices, "y", rotation.getAngleY());
            if (rotation.getAngleZ() != 0) applyRotationAxis(vertices, "z", rotation.getAngleZ());
        } else {
            if (rotation.getAngle() != 0) applyRotationAxis(vertices, rotation.getAxis(), rotation.getAngle());
        }

        for (float[] v : vertices) { v[0] += ox; v[1] += oy; v[2] += oz; }

        if (rotation.isRescale() && !rotation.is3D()) {
            float angleRad = (float) Math.toRadians(rotation.getAngle());
            float scale = 1.0f / Math.max(Math.abs((float)Math.cos(angleRad)), Math.abs((float)Math.sin(angleRad)));
            for (float[] v : vertices) {
                v[0] = ox + (v[0] - ox) * scale;
                v[1] = oy + (v[1] - oy) * scale;
                v[2] = oz + (v[2] - oz) * scale;
            }
        }
    }

    private void applyRotationAxis(float[][] vertices, String axis, float angle) {
        float angleRad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(angleRad), sin = (float) Math.sin(angleRad);
        for (float[] v : vertices) {
            float x = v[0], y = v[1], z = v[2];
            switch (axis) {
                case "x" -> { v[1] = y * cos - z * sin; v[2] = y * sin + z * cos; }
                case "y" -> { v[0] = x * cos + z * sin; v[2] = -x * sin + z * cos; }
                case "z" -> { v[0] = x * cos - y * sin; v[1] = x * sin + y * cos; }
            }
        }
    }

    private void applyUvRotation(float[] uv, int rotation) {
        if (rotation == 0) return;
        float u1 = uv[0], v1 = uv[1], u2 = uv[2], v2 = uv[3];
        switch (rotation) {
            case 90 -> { uv[0] = v1; uv[1] = 1f - u2; uv[2] = v2; uv[3] = 1f - u1; }
            case 180 -> { uv[0] = 1f - u2; uv[1] = 1f - v2; uv[2] = 1f - u1; uv[3] = 1f - v1; }
            case 270 -> { uv[0] = 1f - v2; uv[1] = u1; uv[2] = 1f - v1; uv[3] = u2; }
        }
    }

    private float[] computeNormal(float[][] v) {
        float e1x = v[1][0] - v[0][0], e1y = v[1][1] - v[0][1], e1z = v[1][2] - v[0][2];
        float e2x = v[2][0] - v[0][0], e2y = v[2][1] - v[0][1], e2z = v[2][2] - v[0][2];
        float nx = e1y * e2z - e1z * e2y, ny = e1z * e2x - e1x * e2z, nz = e1x * e2y - e1y * e2x;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0001f) { nx /= len; ny /= len; nz /= len; }
        return new float[]{nx, ny, nz};
    }

    private int[] buildQuadVertexData(float[][] positions, float[] uv, float[] normal, Sprite sprite) {
        int[] data = new int[QUAD_SIZE];
        float[][] uvCorners = { {uv[0], uv[1]}, {uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]} };
        int packedNormal = packNormal(normal[0], normal[1], normal[2]);

        for (int i = 0; i < 4; i++) {
            int offset = i * VERTEX_SIZE;
            data[offset] = Float.floatToRawIntBits(positions[i][0]);
            data[offset + 1] = Float.floatToRawIntBits(positions[i][1]);
            data[offset + 2] = Float.floatToRawIntBits(positions[i][2]);
            data[offset + 3] = 0xFFFFFFFF;
            data[offset + 4] = Float.floatToRawIntBits(sprite.getMinU() + (sprite.getMaxU() - sprite.getMinU()) * uvCorners[i][0]);
            data[offset + 5] = Float.floatToRawIntBits(sprite.getMinV() + (sprite.getMaxV() - sprite.getMinV()) * uvCorners[i][1]);
            data[offset + 6] = 0; // Light is handled by the block renderer for blocks
            data[offset + 7] = packedNormal;
        }
        return data;
    }

    private int packNormal(float x, float y, float z) {
        return (((byte) (x * 127)) & 0xFF) | ((((byte) (y * 127)) & 0xFF) << 8) | ((((byte) (z * 127)) & 0xFF) << 16);
    }

    private Direction directionFromNormal(float[] normal) {
        float maxDot = -1; Direction best = Direction.NORTH;
        for (Direction dir : Direction.values()) {
            float dot = normal[0] * dir.getOffsetX() + normal[1] * dir.getOffsetY() + normal[2] * dir.getOffsetZ();
            if (dot > maxDot) { maxDot = dot; best = dir; }
        }
        return best;
    }
}
