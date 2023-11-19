package mrfast.sbf.events;

import mrfast.sbf.core.SkyblockMobDetector.SkyblockMob;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SkyblockMobEvent extends Event {
    SkyblockMob sbMob;
    public Float partialTicks;
    public SkyblockMobEvent(SkyblockMob sbMob,Float partialTicks) {
        this.sbMob=sbMob;
        this.partialTicks = partialTicks;
    }

    public SkyblockMob getSbMob() {
        return sbMob;
    }

    public static class Spawn extends SkyblockMobEvent {
        public Spawn(SkyblockMob sbMob) {
            super(sbMob,null);
        }
        @Override
        public boolean isCancelable() {
            return false;
        }
    }

    public static class Death extends SkyblockMobEvent {
        public Death(SkyblockMob sbMob) {
            super(sbMob,null);
        }
        @Override
        public boolean isCancelable() {
            return false;
        }
    }


    public static class Render extends SkyblockMobEvent {
        public Render(SkyblockMob sbMob,float partialTicks) {
            super(sbMob,partialTicks);
        }

        @Override
        public boolean isCancelable() {
            return false;
        }
    }
}
