package mrfast.sbf.mixins.transformers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
/**
 * Modified from Patcher under Creative Commons Attribution-NonCommercial-ShareAlike 4.0
 * https://github.com/Sk1erLLC/Patcher/blob/master/LICENSE.md
 *
 * @author Sk1erLLC
 */
@Mixin(BlockCrops.class)
public abstract class MixinBlockCrops extends BlockBush {

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        updateCropsMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        updateCropsMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    private void updateCropsMaxY(World world, BlockPos pos, Block block) {
        if(!SkyblockFeatures.config.cropBox) return;
        final IBlockState blockState = world.getBlockState(pos);

        if (Utils.GetMC().theWorld != null) {
            if(blockState.getValue(BlockCrops.AGE) == 7) {
                block.maxY = 1F;
                return;
            }
        }

        block.maxY = 0.25F;
    }
}