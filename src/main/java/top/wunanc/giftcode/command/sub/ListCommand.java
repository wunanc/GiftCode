package top.wunanc.giftcode.command.sub;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ListCommand(GiftCode plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int targetPage = 1;
        if (args.length >= 2) {
            try { targetPage = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        final int finalTargetPage = targetPage;

        sender.sendMessage("§e正在查询数据，请稍候...");
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                int total = db.getTotalCodesCount();
                int totalPages = (int) Math.ceil(total / 10.0);
                if (totalPages == 0) totalPages = 1;

                int page = Math.max(1, Math.min(finalTargetPage, totalPages));
                int offset = (page - 1) * 10;

                List<CodeData> codes = db.getCodes(10, offset);

                sender.sendMessage("§8================ §6兑换码列表 (§e" + page + "§6/§e" + totalPages + "§6) §8================");
                if (codes.isEmpty()) {
                    sender.sendMessage("§7当前没有任何兑换码记录。");
                } else {
                    for (CodeData code : codes) {
                        String expireStr = code.getExpireTime() == -1 ? "永久" : sdf.format(new Date(code.getExpireTime()));

                        // MiniMessage: 富文本，包含点击复制UUID的悬浮字
                        String mm = "<click:copy_to_clipboard:'" + code.getUuid() + "'>" +
                                "<hover:show_text:'<green>点击复制 UUID</green>'><yellow>[复制]</yellow></hover></click> " +
                                "<white>类型:</white> <gold>" + code.getType() + "</gold> " +
                                "<white>内容:</white> <green>" + code.getContent() + "</green> " +
                                "<white>剩余:</white> <aqua>" + code.getRemaining() + "</aqua> " +
                                "<white>过期:</white> <red>" + expireStr + "</red>";

                        sender.sendMessage(MiniMessage.miniMessage().deserialize(mm));
                    }
                }
                sender.sendMessage("§8==================================================");
                if (page < totalPages) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gray>输入 </gray><click:run_command:'/gc list " + (page + 1) + "'>" +
                                    "<hover:show_text:'<green>点击前往下一页</green>'><yellow><u>/gc list " + (page + 1) + "</u></yellow></hover></click><gray> 查看下一页</gray>"));
                }

            } catch (SQLException e) {
                sender.sendMessage("§c数据库查询失败！");
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 2) list.add(args[1].isEmpty() ? "<页码>" : args[1]);
        return list;
    }

    @Override
    public String getPermission() { return "giftcode.admin"; }
}