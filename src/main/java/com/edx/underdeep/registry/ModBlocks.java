package com.edx.underdeep.registry;

import com.edx.underdeep.UnderdeepMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ToolType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, UnderdeepMod.MODID);

    public static final RegistryObject<Block> DEEPSLATE = BLOCKS.register("deepslate",
            () -> new Block(Block.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(3.5F, 6.0F)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(0)));

    public static final RegistryObject<Block> COBBLED_DEEPSLATE = BLOCKS.register("cobbled_deepslate",
            () -> new Block(Block.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(3.5F, 6.0F)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(0)));

    public static final RegistryObject<Block> DEEPSLATE_IRON_ORE = BLOCKS.register("deepslate_iron_ore",
            () -> new Block(Block.Properties.create(Material.ROCK, MaterialColor.STONE)
                    .hardnessAndResistance(4.0F, 6.0F)
                    .harvestTool(ToolType.PICKAXE)
                    .harvestLevel(1)));

    private ModBlocks() {
    }
}
