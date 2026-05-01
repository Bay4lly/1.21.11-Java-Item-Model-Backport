package io.bay4lly.javablockitem.backport.registry;

import io.bay4lly.javablockitem.backport.JavaBlockItemBackport;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Test item registration for demonstrating the backport system.
 * 
 * Registers a test item ("backport_test_item") that uses a custom model
 * with a -30° Z-axis rotation — something vanilla Minecraft cannot handle.
 */
public class BackportItems {

    /**
     * Test item that uses a model with non-vanilla rotation.
     * The model JSON at assets/newjavabackport/models/item/backport_test_item.json
     * contains a cube rotated -30° on the Z axis.
     */
    public static final Item BACKPORT_TEST_ITEM = new Item(
            new FabricItemSettings().maxCount(64)
    );

    /**
     * Register all test items.
     */
    public static void register() {
        // Register the test item
        Registry.register(
                Registries.ITEM,
                new Identifier(JavaBlockItemBackport.MOD_ID, "backport_test_item"),
                BACKPORT_TEST_ITEM
        );

        // Add to the Ingredients creative tab for easy access
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(BACKPORT_TEST_ITEM);
        });
    }
}
