package top.wunanc.giftcode;

import org.bukkit.plugin.java.JavaPlugin;
import top.wunanc.giftcode.command.GiftCodeCommand;
import top.wunanc.giftcode.database.DatabaseManager;

import java.sql.SQLException;

public final class GiftCode extends JavaPlugin {
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("正在初始化数据库...");
        databaseManager = new DatabaseManager(getDataFolder());
        try {
            databaseManager.init();
            getLogger().info("数据库初始化成功！");
        } catch (SQLException e) {
            getLogger().severe("数据库初始化失败！插件将无法正常工作。");
            e.printStackTrace();
        }

        // 注册指令
        GiftCodeCommand commandHandler = new GiftCodeCommand(this, databaseManager);
        getCommand("gc").setExecutor(commandHandler);
        getCommand("gc").setTabCompleter(commandHandler);

        getLogger().info("GiftCode 插件已启用！(Folia 兼容支持)");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("数据库连接已关闭。");
        }
        getLogger().info("GiftCode 插件已卸载。");
    }
}