package mrfast.sbf.mixins.transformers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import mrfast.sbf.utils.Utils;
/**
 * Taken from Patcher under Creative Commons Attribution-NonCommercial-ShareAlike 4.0
 * https://github.com/Sk1erLLC/Patcher/blob/master/LICENSE.md
 *
 * @author Sk1erLLC
 */
@Mixin(BlockNetherWart.class)
public abstract class MixinBlockNetherWart extends BlockBush {

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        updateWartMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        return super.getSelectedBoundingBox(worldIn, pos);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        updateWartMaxY(worldIn, pos, worldIn.getBlockState(pos).getBlock());
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    private void updateWartMaxY(World world, BlockPos pos, Block block) {
        if (Utils.GetMC().theWorld != null) {
            if(world.getBlockState(pos).getValue(BlockNetherWart.AGE) == 3) {
                block.maxY = 1F;
                return; 
            }
        }
        block.maxY = 0.25F;
    }

}