package top.wunanc.giftcode.util;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class SchedulerUtil {

    // 运行异步任务 (用于数据库操作)
    public static void runAsync(Plugin plugin, Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
    }

    // 运行全局同步任务 (用于执行控制台命令)
    public static void runGlobal(Plugin plugin, Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    // 运行实体同步任务 (用于给玩家发物品)
    public static void runEntity(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
    }
}