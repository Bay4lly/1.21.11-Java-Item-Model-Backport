package io.bay4lly.javablockitem.backport;

import io.bay4lly.javablockitem.backport.registry.BackportItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for the Java Block/Item Backport mod.
 * 
 * This mod provides a compatibility layer that allows Minecraft 1.20.5+ / 1.21.11+
 * style item/block model JSON formats (especially Blockbench-style with arbitrary
 * rotation angles) to be loaded and rendered in 1.20.1.
 * 
 * It works by:
 * 1. Intercepting model JSON loading via Fabric's ModelLoadingPlugin
 * 2. Detecting unsupported features (e.g., non-22.5° rotation angles)
 * 3. Building custom BakedModel implementations that render via vertex math
 * 4. Caching parsed models to avoid per-frame overhead
 */
public class JavaBlockItemBackport implements ModInitializer {
    public static final String MOD_ID = "newjavabackport";
    public static final Logger LOGGER = LoggerFactory.getLogger("NewJavaBackport");

    @Override
    public void onInitialize() {
        LOGGER.info("[NewJavaBackport] Initializing 1.21.11 model backport system...");

        // Register test items to demonstrate the custom renderer
        BackportItems.register();
    }
}