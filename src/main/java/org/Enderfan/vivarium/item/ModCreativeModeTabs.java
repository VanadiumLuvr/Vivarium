package org.Enderfan.vivarium.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;
import org.Enderfan.vivarium.block.ModBlocks;

public class ModCreativeModeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Vivarium.MODID);

    public static final RegistryObject<CreativeModeTab> VIVARIUM_TAB = CREATIVE_MODE_TABS.register("vivarium_tab",
            () -> CreativeModeTab.builder()
                    // Set the Blade as the official tab icon
                    .icon(() -> new ItemStack(ModBlocks.VITAFLOWER.get()))
                    .title(Component.translatable("creativetab.vivarium_tab"))
                    .displayItems((parameters, output) ->
                    {
                        // Add your items in the exact order you want them to appear
                        output.accept(ModItems.BLADE.get());
                        output.accept(ModItems.BLOOD_BUCKET.get());
                        // If you have a Vitaflower block item, add it here too!
                        output.accept(ModBlocks.VITAFLOWER.get());
                    })
                    .build());
}