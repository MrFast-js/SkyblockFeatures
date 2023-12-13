package mrfast.sbf.events;

import mrfast.sbf.features.items.CooldownTracker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class UseItemAbilityEvent extends Event {

    public CooldownTracker.ItemAbility ability;

    public UseItemAbilityEvent(CooldownTracker.ItemAbility ability) {
        this.ability = ability;
    }
}
