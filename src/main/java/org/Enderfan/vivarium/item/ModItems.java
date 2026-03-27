package org.Enderfan.vivarium.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.Enderfan.vivarium.Vivarium;

public class ModItems {
    public static final DeferredRegister<Item> MOD_ITEMS = DeferredRegister.create(Registries.ITEM, Vivarium.MODID);

    public static final RegistryObject<Item> BLADE = MOD_ITEMS.register("blade",
            () -> new BladeItem(new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.item.Item> BLOOD_BUCKET = MOD_ITEMS.register("blood_bucket",
            () -> new net.minecraft.world.item.BucketItem(org.Enderfan.vivarium.fluid.ModFluids.SOURCE_BLOOD,
                    new net.minecraft.world.item.Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

}
