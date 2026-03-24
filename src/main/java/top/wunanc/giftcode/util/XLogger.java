package top.wunanc.giftcode.util;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class XLogger {
    public static XLogger instance;
    private final String prefix;
    public XLogger(@NotNull JavaPlugin plugin, String prefix) {
        instance = this;
        this.prefix = prefix;
        this.console = plugin.getServer().getConsoleSender();
    }

    public static XLogger setDebug(boolean debug) {
        instance.debug = debug;
        return instance;
    }

    private final ConsoleCommandSender console;
    private boolean debug = false;
    public static void info(String msg) {
        checkInstance();
        instance.console.sendMessage(LegacyToMiniMessage.parse(instance.prefix + "<green>" + msg));
    }

    public static void warn(String msg) {
        checkInstance();
        instance.console.sendMessage(LegacyToMiniMessage.parse(instance.prefix + "<yellow>" + msg));
    }

    public static void error(String msg) {
        checkInstance();
        instance.console.sendMessage(LegacyToMiniMessage.parse(instance.prefix + "<red>" + msg));
    }

    public static void debug(String msg) {
        checkInstance();
        if (!instance.debug) return;
        instance.console.sendMessage(LegacyToMiniMessage.parse(instance.prefix + "<aqua>" + msg));
    }

    private static void checkInstance() {
        if (instance == null) {
            throw new IllegalStateException("XLogger not initialized! Please call 'new XLogger()' first.");
        }
    }
}