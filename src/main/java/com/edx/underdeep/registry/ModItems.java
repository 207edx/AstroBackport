package com.edx.underdeep.registry;

import com.edx.underdeep.UnderdeepMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, UnderdeepMod.MODID);

    public static final RegistryObject<Item> DEEPSLATE = ITEMS.register("deepslate",
            () -> new BlockItem(ModBlocks.DEEPSLATE.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));

    public static final RegistryObject<Item> COBBLED_DEEPSLATE = ITEMS.register("cobbled_deepslate",
            () -> new BlockItem(ModBlocks.COBBLED_DEEPSLATE.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));

    public static final RegistryObject<Item> DEEPSLATE_IRON_ORE = ITEMS.register("deepslate_iron_ore",
            () -> new BlockItem(ModBlocks.DEEPSLATE_IRON_ORE.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));

    private ModItems() {
    }
}
