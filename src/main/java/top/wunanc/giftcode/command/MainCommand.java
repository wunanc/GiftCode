package top.wunanc.giftcode.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.sub.ClaimCommand;
import top.wunanc.giftcode.command.sub.ClearCommand;
import top.wunanc.giftcode.command.sub.CreateCommand;
import top.wunanc.giftcode.command.sub.ListCommand;
import top.wunanc.giftcode.command.sub.ReloadCommand; // <-- 新增的导入
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.manager.LanguageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主命令分发器。
 * 负责接收用户的 /gc 输入，并将其分发到对应的子命令处理类中。
 */
public class MainCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final SubCommand claimCommand;
    private final LanguageManager lang;

    public MainCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.lang = lang;
        // 注册所有带有二级前缀的子命令
        subCommands.put("create", new CreateCommand(plugin, db, lang));
        subCommands.put("list", new ListCommand(plugin, db, lang));
        subCommands.put("clear", new ClearCommand(plugin, db, lang));

        // <-- 在这里把 ReloadCommand 注册进去 -->
        subCommands.put("reload", new ReloadCommand(plugin, lang));

        // 兑换命令没有前缀
        this.claimCommand = new ClaimCommand(plugin, db, lang);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            lang.send(sender, "usage_main");
            return true;
        }

        String subName = args[0].toLowerCase();
        // 检查用户输入的是否是已注册的子命令 (create, list, clear, reload)
        if (subCommands.containsKey(subName)) {
            SubCommand sub = subCommands.get(subName);
            if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
                lang.send(sender, "no_permission");
                return true;
            }
            sub.execute(sender, args);
        } else {
            // 如果不是已知子命令，视为 UUID 进行兑换
            claimCommand.execute(sender, args);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // 第一层补全：提示子命令
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("giftcode.admin")) {
                if ("create".startsWith(args[0].toLowerCase())) list.add("create");
                if ("list".startsWith(args[0].toLowerCase())) list.add("list");
                if ("clear".startsWith(args[0].toLowerCase())) list.add("clear");

                // <-- 在 Tab 补全中加入 reload 提示 -->
                if ("reload".startsWith(args[0].toLowerCase())) list.add("reload");
            }
            return list;
        }

        // 第二层及以后：交还给对应的子命令类
        String subName = args[0].toLowerCase();
        if (subCommands.containsKey(subName)) {
            SubCommand sub = subCommands.get(subName);
            if (sender.hasPermission(sub.getPermission())) {
                return sub.tabComplete(sender, args);
            }
        }

        return new ArrayList<>();
    }
}