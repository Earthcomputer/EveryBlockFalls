package net.earthcomputer.everyblockfalls;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.world.dimension.DimensionType;

public class EveryBlockFalls {

    public static int getFallRadius(DimensionType dimension) {
        return dimension == DimensionType.THE_NETHER ? 16 : 32;
    }

    public static boolean canFall(Block block) {
        return !block.getDefaultState().isAir()
                && !(block instanceof FluidBlock)
                && !(block instanceof FallingBlock)
                && block != Blocks.OBSIDIAN
                && block != Blocks.END_STONE
                && block != Blocks.BEDROCK
                && block != Blocks.PISTON
                && block != Blocks.STICKY_PISTON
                && block != Blocks.PISTON_HEAD
                && block != Blocks.MOVING_PISTON;
    }

    public static boolean shouldUpdateOthers(Block block) {
        return !(block instanceof FluidBlock)
                && block != Blocks.OBSIDIAN;
    }

}
