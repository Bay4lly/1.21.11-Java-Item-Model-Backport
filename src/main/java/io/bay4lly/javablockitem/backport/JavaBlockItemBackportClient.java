package io.bay4lly.javablockitem.backport;

import io.bay4lly.javablockitem.backport.loader.BackportModelLoadingPlugin;
import io.bay4lly.javablockitem.backport.loader.BackportResourceReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

/**
 * Client-side initializer for the backport mod.
 * 
 * This is where we hook into the model loading pipeline:
 * 1. Register a ResourceReloadListener to parse model JSONs during resource reload
 * 2. Register a ModelLoadingPlugin to intercept and replace incompatible models
 */
public class JavaBlockItemBackportClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register our resource reload listener (parses & caches models on reload)
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new BackportResourceReloadListener());

        // Register the model loading plugin (intercepts model baking)
        ModelLoadingPlugin.register(new BackportModelLoadingPlugin());
    }
}
