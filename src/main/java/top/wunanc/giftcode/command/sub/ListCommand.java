package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.ItemSerializer;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 分页展示当前数据库中兑换码的子命令。
 */
public class ListCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ListCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int targetPage = 1;
        if (args.length >= 2) {
            try { targetPage = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
        }
        final int finalTargetPage = targetPage;

        lang.send(sender, "querying");

        SchedulerUtil.runAsync(plugin, () -> {
            try {
                // 计算总条数和分页信息
                int total = db.getTotalCodesCount();
                int totalPages = (int) Math.ceil(total / 10.0);
                if (totalPages == 0) totalPages = 1;

                int page = Math.max(1, Math.min(finalTargetPage, totalPages));
                int offset = (page - 1) * 10;

                // 从数据库取当前页的数据（LIMIT 10）
                List<CodeData> codes = db.getCodes(10, offset);

                lang.send(sender, "list_header", "page", String.valueOf(page), "total", String.valueOf(totalPages));

                if (codes.isEmpty()) {
                    lang.send(sender, "list_empty");
                } else {
                    for (CodeData code : codes) {
                        String expireStr = code.getExpireTime() == -1 ? "∞" : sdf.format(new Date(code.getExpireTime()));

                        // 【修改点】：如果类型是 base64，反序列化获取简短名称用于展示
                        String displayContent = code.getContent();
                        if (code.getType().equals("base64")) {
                            ItemStack decoded = ItemSerializer.fromBase64(code.getContent());
                            if (decoded != null) {
                                displayContent = "[包含 NBT 的 " + decoded.getType().name() + " x" + decoded.getAmount() + "]";
                            } else {
                                displayContent = "[损坏的物品数据]";
                            }
                        }

                        // 输出格式化后的每一条数据记录
                        lang.send(sender, "list_format",
                                "uuid", code.getUuid(),
                                "type", code.getType(),
                                "content", displayContent, // 这里传入我们处理好的 displayContent
                                "left", String.valueOf(code.getRemaining()),
                                "expire", expireStr);
                    }
                }

                lang.send(sender, "list_footer");

                // 提示可点击的下一页
                if (page < totalPages) {
                    lang.send(sender, "list_next_page", "next", String.valueOf(page + 1));
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
            list.add(args[1].isEmpty() ? lang.getRaw("tab_list_page") : args[1]);
        }
        return list;
    }

    @Override
    public String getPermission() { return "giftcode.admin"; }
}