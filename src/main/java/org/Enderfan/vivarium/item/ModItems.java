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

}
