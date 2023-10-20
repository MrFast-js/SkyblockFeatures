package mrfast.sbf.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
/**
 * Modified from Skytils 0.x under GNU Affero General Public License v3.0
 * https://github.com/Skytils/SkytilsMod/tree/0.x
 *
 * @author Sychic
 */
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
