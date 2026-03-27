package org.Enderfan.vivarium.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks
{
    // The waiting room for your blocks
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Vivarium.MODID);

    // Registering the Vitaflower
    // We copy the properties of a Poppy so it breaks instantly, doesn't have a collision box, and sounds like grass
    public static final RegistryObject<Block> VITAFLOWER = registerBlock("vitaflower",
            () -> new VitaflowerBlock(BlockBehaviour.Properties.copy(Blocks.POPPY).noOcclusion().noCollission()));

    // Helper method that registers the block AND its inventory item at the same time
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Connects to your existing ModItems registry to make the block holdable
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block)
    {
        return ModItems.MOD_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}