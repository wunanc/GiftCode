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
 * 手动删除指定兑换码的命令
 */
public class DeleteCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;

    public DeleteCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            lang.send(sender, "usage_delete");
            return;
        }

        String uuid = args[1];
        lang.send(sender, "deleting");

        // 数据库操作必须放入异步线程
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                boolean success = db.deleteCode(uuid);
                if (success) {
                    lang.send(sender, "delete_success", "uuid", uuid);
                } else {
                    lang.send(sender, "delete_not_found");
                }
            } catch (SQLException e) {
                lang.send(sender, "db_error", "error", e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) {
            list.add(args[1].isEmpty() ? lang.getRaw("tab_delete_code") : args[1]);
        }
        return list;
    }

    @Override
    public String getPermission() {
        return "giftcode.admin";
    }
}