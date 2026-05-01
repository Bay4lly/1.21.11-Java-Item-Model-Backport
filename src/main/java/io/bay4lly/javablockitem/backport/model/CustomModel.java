package io.bay4lly.javablockitem.backport.model;

import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal representation of a parsed model JSON.
 * 
 * This is the fully resolved model that contains all elements (cubes),
 * texture references, and display transforms. It is built once during
 * resource reload and cached for use by the custom BakedModel renderer.
 */
public class CustomModel {

    /** All cube elements in this model */
    private final List<Cube> elements;

    /** Map of texture variable name → texture Identifier (e.g., "#0" → "minecraft:block/stone") */
    private final Map<String, Identifier> textures;

    /** Whether this model requires the custom renderer (has unsupported features) */
    private boolean requiresCustomRenderer;

    /** Optional parent model identifier */
    private Identifier parent;

    /** Whether this model was force-flagged via "backport:force": true */
    private boolean forceCustom;

    /** Sanitized JSON string (vanilla-compatible) for the ModelResolver */
    private String sanitizedJson;

    public CustomModel() {
        this.elements = new ArrayList<>();
        this.textures = new HashMap<>();
        this.requiresCustomRenderer = false;
        this.forceCustom = false;
        this.sanitizedJson = null;
    }

    // --- Element Management ---

    public void addElement(Cube cube) {
        elements.add(cube);
        // Auto-detect if this element requires custom rendering
        if (cube.getRotation() != null && !cube.getRotation().isVanillaCompatible()) {
            requiresCustomRenderer = true;
        }
    }

    public List<Cube> getElements() {
        return elements;
    }

    // --- Texture Management ---

    public void putTexture(String key, Identifier textureId) {
        textures.put(key, textureId);
    }

    public Map<String, Identifier> getTextures() {
        return textures;
    }

    /**
     * Resolve a texture reference like "#0" or "#texture" to an actual Identifier.
     */
    /**
     * Resolve a texture reference like "#0" or "#texture" to an actual Identifier.
     * Follows parent hierarchy if not found in this model.
     */
    public Identifier resolveTexture(String ref) {
        if (ref == null) return null;
        // Strip leading '#' if present
        String key = ref.startsWith("#") ? ref.substring(1) : ref;
        
        // 1. Look in this model
        Identifier id = textures.get(key);
        if (id != null) return id;
        
        // 2. Look in parent models (recursively)
        if (parent != null) {
            CustomModel parentModel = ModelCache.get(parent);
            if (parentModel != null) {
                return parentModel.resolveTexture(ref);
            }
        }
        
        return null;
    }

    // --- Renderer Flags ---

    public boolean requiresCustomRenderer() {
        return requiresCustomRenderer || forceCustom;
    }

    public void setRequiresCustomRenderer(boolean value) {
        this.requiresCustomRenderer = value;
    }

    public boolean isForceCustom() {
        return forceCustom;
    }

    public void setForceCustom(boolean forceCustom) {
        this.forceCustom = forceCustom;
        if (forceCustom) {
            this.requiresCustomRenderer = true;
        }
    }

    // --- Parent ---

    public Identifier getParent() {
        return parent;
    }

    public void setParent(Identifier parent) {
        this.parent = parent;
    }

    public String getSanitizedJson() {
        return sanitizedJson;
    }

    public void setSanitizedJson(String sanitizedJson) {
        this.sanitizedJson = sanitizedJson;
    }

    @Override
    public String toString() {
        return "CustomModel{elements=" + elements.size()
                + ", textures=" + textures.size()
                + ", customRenderer=" + requiresCustomRenderer
                + ", force=" + forceCustom + "}";
    }
}
