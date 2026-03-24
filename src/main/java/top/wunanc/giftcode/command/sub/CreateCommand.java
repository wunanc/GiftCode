package top.wunanc.giftcode.command.sub;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 创建兑换码的子命令。
 * 管理员使用，支持物品发放或控制台命令执行。
 */
public class CreateCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;

    public CreateCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 校验基本参数数量
        if (args.length < 5) {
            lang.send(sender, "usage_create");
            return;
        }

        String type = args[1].toLowerCase();
        int uses;
        long expireTime;
        String content;

        // 解析使用次数和过期时间
        try {
            uses = Integer.parseInt(args[2]);
            long expireSeconds = Long.parseLong(args[3]);
            // -1 代表永久不过期，否则计算出到期的时间戳
            expireTime = expireSeconds == -1 ? -1 : System.currentTimeMillis() + (expireSeconds * 1000L);
        } catch (NumberFormatException e) {
            lang.send(sender, "param_error_number");
            return;
        }

        // 根据类型解析具体要奖励的内容
        if (type.equals("cmd")) {
            // 将第5个参数及其后面的内容合并为一条完整的命令字符串
            content = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        } else if (type.equals("item")) {
            Material mat = Material.matchMaterial(args[4]);
            if (mat == null) {
                lang.send(sender, "param_error_item", "item", args[4]);
                return;
            }
            int amount = 1; // 默认给1个物品
            if (args.length >= 6) {
                try { amount = Integer.parseInt(args[5]); }
                catch (NumberFormatException ignored) {}
            }
            content = mat.name() + ":" + amount;
        } else {
            lang.send(sender, "param_error_type");
            return;
        }

        // 生成兑换码唯一标识符
        String uuid = UUID.randomUUID().toString();
        CodeData codeData = new CodeData(uuid, type, content, uses, expireTime);
        final String finalContent = content; // Lambda 需要 final 变量

        // 异步存入 SQLite 数据库，避免阻塞服务器主线程
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                db.insertCode(codeData);
                // 使用语言包发送带可点击复制的成功信息
                lang.send(sender, "create_success",
                        "uuid", uuid,
                        "type", type,
                        "content", finalContent);
            } catch (SQLException e) {
                lang.send(sender, "db_error", "error", e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        // 这里的补全逻辑专门为各个参数位置提供人性化的辅助提示
        if (args.length == 2) {
            if ("cmd".startsWith(args[1].toLowerCase())) completions.add("cmd");
            if ("item".startsWith(args[1].toLowerCase())) completions.add("item");
        } else if (args.length == 3) {
            completions.add(args[2].isEmpty() ? lang.getRaw("tab_create_uses") : args[2]);
        } else if (args.length == 4) {
            completions.add(args[3].isEmpty() ? lang.getRaw("tab_create_expire") : args[3]);
        } else if (args.length == 5) {
            if (args[1].equalsIgnoreCase("item")) {
                for (Material mat : Material.values()) {
                    if (mat.name().toLowerCase().startsWith(args[4].toLowerCase())) {
                        completions.add(mat.name());
                    }
                }
            } else if (args[1].equalsIgnoreCase("cmd")) {
                completions.add(args[4].isEmpty() ? lang.getRaw("tab_create_cmd") : args[4]);
            }
        } else if (args.length == 6 && args[1].equalsIgnoreCase("item")) {
            completions.add(args[5].isEmpty() ? lang.getRaw("tab_create_item_amount") : args[5]);
        }
        return completions;
    }

    @Override
    public String getPermission() { return "giftcode.admin"; }
}