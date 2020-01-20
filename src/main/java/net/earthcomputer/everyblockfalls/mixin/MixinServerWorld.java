package net.earthcomputer.everyblockfalls.mixin;

import net.earthcomputer.everyblockfalls.EveryBlockFalls;
import net.earthcomputer.everyblockfalls.IServerWorld;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements IServerWorld {

    @Shadow public abstract List<ServerPlayerEntity> getPlayers(Predicate<? super ServerPlayerEntity> predicate);

    @Unique private ServerTickScheduler<Block> fallingTickScheduler = new ServerTickScheduler<>(
            (ServerWorld) (Object) this,
            block -> !EveryBlockFalls.canFall(block),
            Registry.BLOCK::getId, Registry.BLOCK::get,
            this::tickFalling);

    protected MixinServerWorld(LevelProperties levelProperties, DimensionType dimensionType, BiFunction<World, Dimension, ChunkManager> chunkManagerProvider, Profiler profiler, boolean isClient) {
        super(levelProperties, dimensionType, chunkManagerProvider, profiler, isClient);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;tick()V", ordinal = 0))
    private void tickScheduler(CallbackInfo ci) {
        fallingTickScheduler.tick();
    }

    @Override
    public ServerTickScheduler<Block> getFallingTickScheduler() {
        return fallingTickScheduler;
    }

    @Unique private void tickFalling(ScheduledTick<Block> tileTick) {
        if (FallingBlock.canFallThrough(getBlockState(tileTick.pos.down())) && tileTick.pos.getY() >= 0) {
            double x = tileTick.pos.getX() + 0.5;
            double z = tileTick.pos.getZ() + 0.5;
            int radius = EveryBlockFalls.getFallRadius(dimension.getType());
            if (!getPlayers(player -> {
                double dx = player.getX() - x;
                double dz = player.getZ() - z;
                return dx * dx + dz * dz <= radius * radius;
            }).isEmpty()) {
                FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(
                        this,
                        tileTick.pos.getX() + 0.5,
                        tileTick.pos.getY(),
                        tileTick.pos.getZ() + 0.5,
                        getBlockState(tileTick.pos));
                spawnEntity(fallingBlockEntity);
            }
        }
    }
}
