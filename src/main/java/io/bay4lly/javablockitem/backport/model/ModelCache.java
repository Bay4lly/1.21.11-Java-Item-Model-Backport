package io.bay4lly.javablockitem.backport.model;

import io.bay4lly.javablockitem.backport.JavaBlockItemBackport;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central cache for all parsed custom models.
 * 
 * Models are parsed ONCE during resource reload and stored here.
 * The renderer and model loading plugin look up models from this cache.
 * This avoids any per-frame JSON parsing overhead.
 * 
 * Thread-safe via ConcurrentHashMap.
 */
public final class ModelCache {

    /** Map of model Identifier → parsed CustomModel */
    private static final Map<Identifier, CustomModel> CACHE = new ConcurrentHashMap<>();

    /** Set of model IDs that were detected as requiring custom rendering */
    private static final Set<Identifier> CUSTOM_RENDER_SET = ConcurrentHashMap.newKeySet();

    /** Set of model IDs that have been force-registered via the API */
    private static final Set<Identifier> FORCE_CUSTOM_SET = ConcurrentHashMap.newKeySet();

    /** Set of texture IDs that are used by any custom model */
    private static final Set<Identifier> TEXTURE_DEPENDENCIES = ConcurrentHashMap.newKeySet();

    private ModelCache() {} // Utility class

    // --- Cache Operations ---

    /**
     * Store a parsed model in the cache.
     */
    public static void put(Identifier id, CustomModel model) {
        CACHE.put(id, model);
        if (model.requiresCustomRenderer()) {
            CUSTOM_RENDER_SET.add(id);
            TEXTURE_DEPENDENCIES.addAll(model.getTextures().values());
            JavaBlockItemBackport.LOGGER.debug("[NewJavaBackport] Cached custom model: {}", id);
        }
    }

    /**
     * Retrieve a cached model by its Identifier.
     */
    public static CustomModel get(Identifier id) {
        return CACHE.get(id);
    }

    /**
     * Check if a model requires the custom renderer.
     */
    public static boolean requiresCustomRenderer(Identifier id) {
        return CUSTOM_RENDER_SET.contains(id) || FORCE_CUSTOM_SET.contains(id);
    }

    /**
     * Get an unmodifiable view of all cached models.
     */
    public static Map<Identifier, CustomModel> getAll() {
        return Collections.unmodifiableMap(CACHE);
    }

    /**
     * Get the set of all model IDs requiring custom rendering.
     */
    public static Set<Identifier> getCustomRenderSet() {
        return Collections.unmodifiableSet(CUSTOM_RENDER_SET);
    }

    /**
     * Force-register a model ID for custom rendering (API method).
     */
    public static void forceCustom(Identifier id) {
        FORCE_CUSTOM_SET.add(id);
        CustomModel model = CACHE.get(id);
        if (model != null) {
            model.setForceCustom(true);
        }
    }

    /**
     * Register a custom model via the API.
     */
    public static void register(Identifier id, CustomModel model) {
        model.setForceCustom(true);
        CACHE.put(id, model);
        CUSTOM_RENDER_SET.add(id);
        JavaBlockItemBackport.LOGGER.debug("[NewJavaBackport] API-registered custom model: {}", id);
    }

    /**
     * Clear all caches. Called at the start of resource reload.
     */
    public static void clear() {
        int prevSize = CACHE.size();
        CACHE.clear();
        CUSTOM_RENDER_SET.clear();
        TEXTURE_DEPENDENCIES.clear();
        // Note: FORCE_CUSTOM_SET is NOT cleared — API registrations persist across reloads
        JavaBlockItemBackport.LOGGER.debug("[NewJavaBackport] Cache cleared ({} models)", prevSize);
    }

    public static boolean isTextureUsed(Identifier id) {
        return TEXTURE_DEPENDENCIES.contains(id);
    }

    public static Set<Identifier> getAllUsedTextures() {
        return Collections.unmodifiableSet(TEXTURE_DEPENDENCIES);
    }

    /**
     * Get cache statistics for debugging.
     */
    public static String getStats() {
        return String.format("Cache: %d total, %d custom, %d force-registered",
                CACHE.size(), CUSTOM_RENDER_SET.size(), FORCE_CUSTOM_SET.size());
    }
}
