package top.wunanc.giftcode;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import top.wunanc.giftcode.command.MainCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;
import top.wunanc.giftcode.util.XLogger;

import java.sql.SQLException;

public final class GiftCode extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        // 1. 初始化语言管理器
        languageManager = new LanguageManager(this);
        languageManager.init();
        new XLogger(this, languageManager.getPrefix());
        XLogger.info("Language file loaded!");
        // 2. 初始化数据
        XLogger.info("Initializing the database...");
        databaseManager = new DatabaseManager(getDataFolder());
        try {
            databaseManager.init();
            XLogger.info("Database initialization successful!");
        } catch (SQLException e) {
            XLogger.error("Database initialization failed! Closing plugins...");
            XLogger.error("Error：" + e.getMessage());
            XLogger.error("SQLState: " + e.getSQLState());
            XLogger.error("ErrCode：" + e.getErrorCode());
            Bukkit.getPluginManager().disablePlugin(this);
        }


        // 3. 注册命令
        var cmd = getCommand("gc");
        if (cmd != null) {
            MainCommand mainCommand = new MainCommand(this, databaseManager, languageManager);
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        } else {
            XLogger.error("Command 'gc' registration failed! Closing plugins...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getLogger().info("GiftCode plugin is enabled!");
    }

    @Override
    public void onDisable() {
        // 安全关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
            XLogger.info("The database connection is closed.");
        }
        XLogger.info("GiftCode plugin has been disabled.");
    }

    // 提供给外部获取语言管理器实例的快捷方法
    public LanguageManager getLang() {
        return languageManager;
    }
}