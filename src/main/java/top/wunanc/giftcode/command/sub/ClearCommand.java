package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClearCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;

    public ClearCommand(GiftCode plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§e正在清理数据库中过期和已用完的兑换码...");
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                int count = db.clearInvalidCodes(System.currentTimeMillis());
                sender.sendMessage("§a清理完成！共删除了 §e" + count + " §a条失效兑换码！");
            } catch (SQLException e) {
                sender.sendMessage("§c数据库清理时发生错误！");
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return new ArrayList<>(); }

    @Override
    public String getPermission() { return "giftcode.admin"; }
}