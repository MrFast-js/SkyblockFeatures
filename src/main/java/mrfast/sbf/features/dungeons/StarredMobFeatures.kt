package mrfast.sbf.features.dungeons

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.core.SkyblockInfo
import mrfast.sbf.core.SkyblockMobDetector
import mrfast.sbf.core.SkyblockMobDetector.SkyblockMob
import mrfast.sbf.events.CheckRenderEntityEvent
import mrfast.sbf.events.RenderEntityOutlineEvent
import mrfast.sbf.utils.ScoreboardUtil
import mrfast.sbf.utils.Utils
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

class StarredMobFeatures {
    var ticks = 0
    var inSpecialRoom = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inDungeons || !DungeonsFeatures.dungeonStarted) return
        if (!SkyblockFeatures.config.glowingStarredMobs && !SkyblockFeatures.config.hideNonStarredMobs) return
        ticks++
        if (ticks % 20 == 0) {
            ticks = 0
            val id = dungeonRoomId
            inSpecialRoom = id == 138 || id == 210 || id == -96 || id == -60
            starredMobs.entries.removeIf { (key): Map.Entry<SkyblockMob, Boolean> -> !key.getSkyblockMob().isEntityAlive }
            for (sbMob in SkyblockMobDetector.getLoadedSkyblockMobs()) {
                if (sbMob.skyblockMob.isInvisible) continue
                starredMobs[sbMob] = sbMob.mobNameEntity.displayName.unformattedText.contains("✯")
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (Utils.GetMC().theWorld == null || !Utils.inDungeons || SkyblockInfo.getLocation() == null) return
        if (event.type == RenderEntityOutlineEvent.Type.XRAY) return
        if (!SkyblockFeatures.config.glowingStarredMobs) return
        if (DungeonsFeatures.dungeonStarted && !inSpecialRoom) {
            starredMobs.forEach { (sbMob: SkyblockMob, starred: Boolean) ->
                if (starred) {
                    event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.boxStarredMobsColor)
                }
            }
        }
    }

    @SubscribeEvent
    fun checkRender(event: CheckRenderEntityEvent<*>) {
        if (!SkyblockFeatures.config.hideNonStarredMobs) return
        if (Utils.inDungeons && DungeonsFeatures.dungeonStarted && !inSpecialRoom) {
            val sbMob = SkyblockMobDetector.getSkyblockMob(event.entity)
            if (sbMob == null || !starredMobs.containsKey(sbMob)) return
            if (!starredMobs[sbMob]!!) {
                event.setCanceled(true)
            }
        }
    }

    @SubscribeEvent
    fun onWorldChanges(event: WorldEvent.Load?) {
        count = 0
    }

    var count = 0

    companion object {
        var starredMobs = HashMap<SkyblockMob, Boolean>()
        val dungeonRoomId: Int
            get() {
                if (!Utils.inDungeons || ScoreboardUtil.getSidebarLines().size < 2) return 0
                val line = ScoreboardUtil.getSidebarLines(true)[1]
                val roomInfo = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
                val roomIdString = roomInfo.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                return roomIdString.toInt()
            }
    }
}
