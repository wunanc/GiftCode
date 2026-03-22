package top.wunanc.giftcode.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.sub.ClaimCommand;
import top.wunanc.giftcode.command.sub.ClearCommand;
import top.wunanc.giftcode.command.sub.CreateCommand;
import top.wunanc.giftcode.command.sub.ListCommand;
import top.wunanc.giftcode.database.DatabaseManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final SubCommand claimCommand; // 默认的兑换命令没有前缀

    public MainCommand(GiftCode plugin, DatabaseManager db) {
        // 注册子命令
        subCommands.put("create", new CreateCommand(plugin, db));
        subCommands.put("list", new ListCommand(plugin, db));
        subCommands.put("clear", new ClearCommand(plugin, db));

        // 注册没有子命令前缀的玩家兑换动作
        this.claimCommand = new ClaimCommand(plugin, db);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c用法: /gc <兑换码> 或 /gc <create|list|clear> ...");
            return true;
        }

        String subName = args[0].toLowerCase();
        if (subCommands.containsKey(subName)) {
            SubCommand sub = subCommands.get(subName);
            if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
                sender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }
            sub.execute(sender, args);
        } else {
            // 如果第一个参数不是任何子命令，则当作 UUID 进行兑换处理
            claimCommand.execute(sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("giftcode.admin")) {
                if ("create".startsWith(args[0].toLowerCase())) list.add("create");
                if ("list".startsWith(args[0].toLowerCase())) list.add("list");
                if ("clear".startsWith(args[0].toLowerCase())) list.add("clear");
            }
            return list;
        }

        String subName = args[0].toLowerCase();
        if (subCommands.containsKey(subName) && sender.hasPermission(subCommands.get(subName).getPermission())) {
            return subCommands.get(subName).tabComplete(sender, args);
        }

        return new ArrayList<>();
    }
}