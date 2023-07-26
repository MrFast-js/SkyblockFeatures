package mrfast.sbf.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class BlockChangeEvent extends Event {

    public BlockPos pos;
    public IBlockState oldBlockState;
    public IBlockState newBlockState;

    public BlockChangeEvent(BlockPos blockPos, IBlockState oldBlockState, IBlockState newBlockState) {
        this.pos = blockPos;
        this.oldBlockState = oldBlockState;
        this.newBlockState = newBlockState;
    }

    public IBlockState getOld() {
        return this.oldBlockState;
    }

    public IBlockState getNew() {
        return this.newBlockState;
    }
}
