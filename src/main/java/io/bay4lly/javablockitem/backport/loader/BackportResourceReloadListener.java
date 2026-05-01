package io.bay4lly.javablockitem.backport.loader;

import io.bay4lly.javablockitem.backport.JavaBlockItemBackport;
import io.bay4lly.javablockitem.backport.model.CustomModel;
import io.bay4lly.javablockitem.backport.model.ModelCache;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Resource reload listener that scans all model JSONs during resource reload.
 * 
 * This runs BEFORE model baking, so by the time ModelLoadingPlugin runs,
 * the cache is already populated with all detected custom models.
 * 
 * Scanning paths:
 * - assets/[namespace]/models/item/*.json
 * - assets/[namespace]/models/block/*.json
 * 
 * Uses Minecraft's resource pipeline (ResourceManager.findResources),
 * NOT filesystem scanning.
 */
public class BackportResourceReloadListener implements IdentifiableResourceReloadListener {

    private static final Identifier LISTENER_ID =
            new Identifier(JavaBlockItemBackport.MOD_ID, "model_scanner");

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(
            Synchronizer synchronizer,
            ResourceManager manager,
            Profiler prepareProfiler,
            Profiler applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        // Prepare phase: scan and parse all model JSONs
        return CompletableFuture.supplyAsync(() -> {
            prepareProfiler.startTick();
            prepareProfiler.push("backport_model_scan");

            // Clear previous cache (API forced-models are preserved inside clear())
            ModelCache.clear();

            int scanned = 0;
            int custom = 0;

            // Scan all models under "models" directory (including custom folders)
            scanned += scanModels(manager, "models");

            custom = ModelCache.getCustomRenderSet().size();

            JavaBlockItemBackport.LOGGER.info(
                    "[NewJavaBackport] Resource scan complete: {} models scanned, {} require custom rendering",
                    scanned, custom);

            prepareProfiler.pop();
            prepareProfiler.endTick();

            return null;
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(v -> {
            // Apply phase: nothing to do — cache is already populated
            applyProfiler.startTick();
            applyProfiler.push("backport_model_apply");
            JavaBlockItemBackport.LOGGER.debug("[NewJavaBackport] {}", ModelCache.getStats());
            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }

    /**
     * Scan all model resources under the given prefix (e.g., "models/item").
     * Uses ResourceManager.findResources which searches ALL namespaces including other mods.
     * 
     * @return Number of models scanned
     */
    private int scanModels(ResourceManager manager, String prefix) {
        int count = 0;

        // findResources searches across ALL namespaces (vanilla, fabric, other mods)
        Map<Identifier, Resource> resources = manager.findResources(prefix, 
                id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (InputStream stream = resource.getInputStream()) {
                // Build the model identifier (strip "models/" prefix and ".json" suffix)
                String path = resourceId.getPath();
                // path = "models/item/foo.json" → we want "item/foo"
                String modelPath = path.substring("models/".length(), path.length() - ".json".length());
                Identifier modelId = new Identifier(resourceId.getNamespace(), modelPath);

                CustomModel model = ModelJsonParser.parse(stream, modelId);
                if (model != null) {
                    ModelCache.put(modelId, model);
                    count++;
                }
            } catch (Exception e) {
                JavaBlockItemBackport.LOGGER.warn("[NewJavaBackport] Error scanning {}: {}",
                        resourceId, e.getMessage());
            }
        }

        return count;
    }
}
