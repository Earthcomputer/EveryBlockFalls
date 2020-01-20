package net.earthcomputer.everyblockfalls.mixin;

import net.earthcomputer.everyblockfalls.EveryBlockFalls;
import net.earthcomputer.everyblockfalls.IServerWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockState.class)
public abstract class MixinBlockState {

    @Shadow public abstract Block getBlock();

    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onOnBlockAdded(World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        if (EveryBlockFalls.canFall(getBlock()) && world instanceof IServerWorld)
            ((IServerWorld) world).getFallingTickScheduler().schedule(pos, getBlock(), 2);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    private void onNeighborUpdate(Direction dir, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> ci) {
        if (EveryBlockFalls.canFall(getBlock()) && EveryBlockFalls.shouldUpdateOthers(neighborState.getBlock()) && world instanceof IServerWorld)
            ((IServerWorld) world).getFallingTickScheduler().schedule(pos, getBlock(), 2);
    }

}
