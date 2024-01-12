package mrfast.sbf.events;

import mrfast.sbf.API.ItemAbilityAPI;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class UseItemAbilityEvent extends Event {

    public ItemAbilityAPI.ItemAbility ability;

    public UseItemAbilityEvent(ItemAbilityAPI.ItemAbility ability) {
        this.ability = ability;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
