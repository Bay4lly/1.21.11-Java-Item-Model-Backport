package io.bay4lly.javablockitem.backport.api;

import io.bay4lly.javablockitem.backport.model.CustomModel;
import io.bay4lly.javablockitem.backport.model.ModelCache;
import net.minecraft.util.Identifier;

/**
 * Public API for the NewJavaBackport mod.
 * 
 * Other mods can use this API to:
 * - Register custom models programmatically
 * - Force-flag specific models for custom rendering
 * - Query the model cache
 * 
 * Usage example:
 * <pre>
 *   // Register a programmatically-built model
 *   BackportAPI.register(new Identifier("mymod", "item/my_item"), myCustomModel);
 *   
 *   // Force a model to use the custom renderer even if it appears vanilla-compatible
 *   BackportAPI.forceCustom(new Identifier("mymod", "item/my_item"));
 *   
 *   // Check if a model is using custom rendering
 *   boolean isCustom = BackportAPI.isCustomRendered(new Identifier("mymod", "item/my_item"));
 * </pre>
 */
public final class BackportAPI {

    private BackportAPI() {} // Utility class

    /**
     * Register a custom model for the given identifier.
     * This model will always use the custom renderer.
     * 
     * Call this during mod initialization (onInitializeClient).
     * 
     * @param id The model identifier (e.g., "mymod:item/my_item")
     * @param model The custom model to register
     */
    public static void register(Identifier id, CustomModel model) {
        ModelCache.register(id, model);
    }

    /**
     * Force a model to use the custom renderer, even if it appears vanilla-compatible.
     * Useful when you know a model has features that the auto-detection might miss.
     * 
     * Can also be done in JSON with "backport:force": true.
     * 
     * @param id The model identifier to force
     */
    public static void forceCustom(Identifier id) {
        ModelCache.forceCustom(id);
    }

    /**
     * Check if a model is currently flagged for custom rendering.
     * 
     * @param id The model identifier to check
     * @return true if the model will use the custom renderer
     */
    public static boolean isCustomRendered(Identifier id) {
        return ModelCache.requiresCustomRenderer(id);
    }

    /**
     * Get a cached custom model by identifier.
     * 
     * @param id The model identifier
     * @return The CustomModel, or null if not in cache
     */
    public static CustomModel getModel(Identifier id) {
        return ModelCache.get(id);
    }

    /**
     * Get cache statistics for debugging.
     * 
     * @return A string with cache stats
     */
    public static String getCacheStats() {
        return ModelCache.getStats();
    }
}
