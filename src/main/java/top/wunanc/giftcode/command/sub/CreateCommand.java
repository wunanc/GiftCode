package top.wunanc.giftcode.command.sub;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CreateCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;

    public CreateCommand(GiftCode plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("§c用法: /gc create <cmd|item> <次数> <过期时间秒,-1为永久> <具体内容...>");
            return;
        }

        String type = args[1].toLowerCase();
        int uses;
        long expireTime;
        String content;

        try {
            uses = Integer.parseInt(args[2]);
            long expireSeconds = Long.parseLong(args[3]);
            expireTime = expireSeconds == -1 ? -1 : System.currentTimeMillis() + (expireSeconds * 1000L);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c参数错误：<次数> 和 <过期时间> 必须是纯数字！");
            return;
        }

        if (type.equals("cmd")) {
            content = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        } else if (type.equals("item")) {
            Material mat = Material.matchMaterial(args[4]);
            if (mat == null) {
                sender.sendMessage("§c无效的物品ID：" + args[4]);
                return;
            }
            int amount = 1;
            if (args.length >= 6) {
                try { amount = Integer.parseInt(args[5]); }
                catch (NumberFormatException ignored) {}
            }
            content = mat.name() + ":" + amount;
        } else {
            sender.sendMessage("§c类型只能是 cmd 或 item！");
            return;
        }

        String uuid = UUID.randomUUID().toString();
        CodeData codeData = new CodeData(uuid, type, content, uses, expireTime);
        final String finalContent = content;

        SchedulerUtil.runAsync(plugin, () -> {
            try {
                db.insertCode(codeData);
                String miniMsg = "<green>成功创建兑换码！</green><newline>" +
                        "<green>UUID: </green><click:copy_to_clipboard:'" + uuid + "'>" +
                        "<hover:show_text:'<green>点击此处复制 UUID！'><yellow><u>" + uuid + "</u></yellow></hover></click><newline>" +
                        "<green>类型: </green><white>" + type + "</white> <green>内容: </green><white>" + finalContent + "</white>";
                sender.sendMessage(MiniMessage.miniMessage().deserialize(miniMsg));
            } catch (SQLException e) {
                sender.sendMessage("§c数据库错误：" + e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) {
            if ("cmd".startsWith(args[1].toLowerCase())) completions.add("cmd");
            if ("item".startsWith(args[1].toLowerCase())) completions.add("item");
        } else if (args.length == 3) {
            completions.add(args[2].isEmpty() ? "<数量/次数>" : args[2]);
        } else if (args.length == 4) {
            completions.add(args[3].isEmpty() ? "<过期时间秒,-1永久>" : args[3]);
        } else if (args.length == 5) {
            if (args[1].equalsIgnoreCase("item")) {
                for (Material mat : Material.values()) {
                    if (mat.name().toLowerCase().startsWith(args[4].toLowerCase())) {
                        completions.add(mat.name());
                    }
                }
            } else if (args[1].equalsIgnoreCase("cmd")) {
                completions.add(args[4].isEmpty() ? "<输入要执行的命令(支持%player%)>" : args[4]);
            }
        } else if (args.length == 6 && args[1].equalsIgnoreCase("item")) {
            completions.add(args[5].isEmpty() ? "<物品数量>" : args[5]);
        }
        return completions;
    }

    @Override
    public String getPermission() { return "giftcode.admin"; }
}