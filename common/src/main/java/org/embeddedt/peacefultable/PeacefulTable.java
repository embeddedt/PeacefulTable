package org.embeddedt.peacefultable;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.embeddedt.peacefultable.block.PeacefulTableBlock;

public class PeacefulTable {
    public static final String MOD_ID = "peacefultable";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final CreativeModeTab TAB = CreativeTabRegistry.create(new ResourceLocation(MOD_ID, "tab"), () ->
            new ItemStack(PeacefulTable.PEACEFUL_TABLE.get()));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registry.BLOCK_REGISTRY);
    private static final BlockBehaviour.Properties PEACEFUL_TABLE_PROPS = BlockBehaviour.Properties.of(Material.WOOD)
            .explosionResistance(5.0f)
            .destroyTime(2.0f)
            .randomTicks();
    public static final RegistrySupplier<Block> PEACEFUL_TABLE = BLOCKS.register("peaceful_table", () ->
            new PeacefulTableBlock(PEACEFUL_TABLE_PROPS, false));

    /*
    public static final RegistrySupplier<Block> ADVANCED_PEACEFUL_TABLE = BLOCKS.register("advanced_peaceful_table", () ->
            new PeacefulTableBlock(PEACEFUL_TABLE_PROPS, true));

     */

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);
    public static final RegistrySupplier<Item> PEACEFUL_TABLE_ITEM = ITEMS.register("peaceful_table", () ->
            new BlockItem(PEACEFUL_TABLE.get(), new Item.Properties().tab(TAB)));

    /*
    public static final RegistrySupplier<Item> ADVANCED_PEACEFUL_TABLE_ITEM = ITEMS.register("advanced_peaceful_table", () ->
            new BlockItem(ADVANCED_PEACEFUL_TABLE.get(), new Item.Properties().tab(TAB)));

     */
    
    public static void init() {
        BLOCKS.register();
        ITEMS.register();
    }
}
