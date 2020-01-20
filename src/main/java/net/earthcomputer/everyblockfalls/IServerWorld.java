package net.earthcomputer.everyblockfalls;

import net.minecraft.block.Block;
import net.minecraft.server.world.ServerTickScheduler;

public interface IServerWorld {

    ServerTickScheduler<Block> getFallingTickScheduler();

}
