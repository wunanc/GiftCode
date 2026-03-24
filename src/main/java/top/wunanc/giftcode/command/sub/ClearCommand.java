package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库清理命令。
 * 一键清除表内已经用完或者时间到期的垃圾记录。
 */
public class ClearCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;

    public ClearCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        lang.send(sender, "clear_start");

        SchedulerUtil.runAsync(plugin, () -> {
            try {
                // 删除动作，返回受影响（即被删除）的行数
                int count = db.clearInvalidCodes(System.currentTimeMillis());
                lang.send(sender, "clear_success", "count", String.valueOf(count));
            } catch (SQLException e) {
                lang.send(sender, "db_error", "error", e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        // 只有管理员才能执行清理操作
        return "giftcode.admin";
    }
}