package net.earthcomputer.everyblockfalls.mixin;

import net.earthcomputer.everyblockfalls.IServerWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.DummyClientTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk {

    @Shadow @Final private World world;
    @Shadow @Final private ChunkPos pos;

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Shadow public abstract void setShouldSave(boolean shouldSave);

    @Unique private TickScheduler<Block> fallingTickScheduler = DummyClientTickScheduler.get();

    @Inject(method = "disableTickSchedulers", at = @At("TAIL"))
    private void onDisableTickSchedulers(CallbackInfo ci) {
        if (fallingTickScheduler instanceof ChunkTickScheduler) {
            ((ChunkTickScheduler<Block>) fallingTickScheduler)
                    .tick(((IServerWorld) world).getFallingTickScheduler(), pos -> getBlockState(pos).getBlock());
            fallingTickScheduler = DummyClientTickScheduler.get();
        } else if (fallingTickScheduler instanceof SimpleTickScheduler) {
            ((IServerWorld) world).getFallingTickScheduler().scheduleAll(((SimpleTickScheduler<Block>) fallingTickScheduler).stream());
            fallingTickScheduler = DummyClientTickScheduler.get();
        }
    }

    @Inject(method = "enableTickSchedulers", at = @At("TAIL"))
    private void onEnableTickSchedulers(ServerWorld world, CallbackInfo ci) {
        if (fallingTickScheduler == DummyClientTickScheduler.<Block>get()) {
            fallingTickScheduler = new SimpleTickScheduler<>(
                    Registry.BLOCK::getId,
                    ((IServerWorld) world).getFallingTickScheduler().getScheduledTicksInChunk(pos, true, false));
            setShouldSave(true);
        }
    }

}
