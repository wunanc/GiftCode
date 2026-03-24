package top.wunanc.giftcode;

import org.bstats.bukkit.Metrics;
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
        languageManager = new LanguageManager(this);
        languageManager.init();
        new XLogger(this, languageManager.getPrefix());
        XLogger.info("Language file loaded!");

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

        var cmd = getCommand("gc");
        if (cmd != null) {
            MainCommand mainCommand = new MainCommand(this, databaseManager, languageManager);
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        } else {
            XLogger.error("Command 'gc' registration failed! Closing plugins...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Metrics metrics = new Metrics(this, 30358);
        XLogger.info("GiftCode plugin is enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
            XLogger.info("The database connection is closed.");
        }
        XLogger.info("GiftCode plugin has been disabled.");
    }

    public LanguageManager getLang() {
        return languageManager;
    }
}