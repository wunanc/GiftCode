package top.wunanc.giftcode;

import org.bukkit.plugin.java.JavaPlugin;
import top.wunanc.giftcode.command.MainCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;

import java.sql.SQLException;

public final class GiftCode extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        // 1. 初始化语言管理器
        languageManager = new LanguageManager(this);
        languageManager.init();
        getLogger().info("语言文件加载成功！ / Language file loaded!");

        // 2. 初始化数据库
        getLogger().info("正在初始化数据库...");
        databaseManager = new DatabaseManager(getDataFolder());
        try {
            databaseManager.init();
            getLogger().info("数据库初始化成功！");
        } catch (SQLException e) {
            java.util.logging.Logger logger = getLogger();
            getLogger().severe("数据库初始化失败！插件将无法正常工作。");
            logger.throwing(getClass().getName(), "onEnable", e);
        }

        // 3. 注册命令
        MainCommand mainCommand = new MainCommand(this, databaseManager, languageManager);
        if (getCommand("gc") != null) {
            getCommand("gc").setExecutor(mainCommand);
            getCommand("gc").setTabCompleter(mainCommand);
        }

        getLogger().info("GiftCode 插件已启用！(支持 Folia 与多语言)");
    }

    @Override
    public void onDisable() {
        // 安全关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("数据库连接已关闭。");
        }
        getLogger().info("GiftCode 插件已卸载。");
    }

    // 提供给外部获取语言管理器实例的快捷方法
    public LanguageManager getLang() {
        return languageManager;
    }
}