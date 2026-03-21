package top.wunanc.giftcode.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GiftCodeCommand implements CommandExecutor, TabCompleter {
    private final GiftCode plugin;
    private final DatabaseManager db;

    public GiftCodeCommand(GiftCode plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§c用法: /gc <兑换码> 或者 /gc create ...");
            return true;
        }

        // 管理员创建命令
        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("giftcode.admin")) {
                sender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }
            // 统一的参数格式判断：/gc create <cmd/item> <次数> <过期时间秒,-1永久> <具体内容...>
            if (args.length < 5) {
                sender.sendMessage("§c用法: /gc create <cmd|item> <次数> <过期时间秒,-1为永久> <具体内容...>");
                return true;
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
                return true;
            }

            // 根据类型解析具体内容
            if (type.equals("cmd")) {
                content = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
            } else if (type.equals("item")) {
                Material mat = Material.matchMaterial(args[4]);
                if (mat == null) {
                    sender.sendMessage("§c无效的物品ID：" + args[4]);
                    return true;
                }
                int amount = 1; // 默认数量为1
                if (args.length >= 6) {
                    try {
                        amount = Integer.parseInt(args[5]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§c参数错误：<物品数量> 必须是纯数字！");
                        return true;
                    }
                }
                content = mat.name() + ":" + amount;
            } else {
                sender.sendMessage("§c类型只能是 cmd 或 item！");
                return true;
            }

            String uuid = UUID.randomUUID().toString();
            CodeData codeData = new CodeData(uuid, type, content, uses, expireTime);
            final String finalContent = content;

            // 异步存入数据库
            SchedulerUtil.runAsync(plugin, () -> {
                try {
                    db.insertCode(codeData);

                    // 使用 MiniMessage 构造可点击、可悬浮的富文本组件
                    String miniMsg = "<green>成功创建兑换码！</green><newline>" +
                            "<green>UUID: </green><click:copy_to_clipboard:'" + uuid + "'>" +
                            "<hover:show_text:'<green>点击此处复制 UUID 到剪贴板！'><yellow><u>" + uuid + "</u></yellow></hover></click><newline>" +
                            "<green>类型: </green><white>" + type + "</white> <green>内容: </green><white>" + finalContent + "</white>";

                    Component message = MiniMessage.miniMessage().deserialize(miniMsg);
                    sender.sendMessage(message);

                } catch (SQLException e) {
                    sender.sendMessage("§c数据库错误：" + e.getMessage());
                    e.printStackTrace();
                }
            });
            return true;
        }

        // 玩家兑换逻辑
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用兑换码！");
            return true;
        }
        Player player = (Player) sender;
        String uuid = args[0];

        player.sendMessage("§e正在验证兑换码，请稍候...");

        // 异步查询与处理，防止卡顿
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                CodeData data = db.getCode(uuid);
                if (data == null) {
                    player.sendMessage("§c兑换码不存在！");
                    return;
                }
                if (data.getExpireTime() != -1 && data.getExpireTime() < System.currentTimeMillis()) {
                    player.sendMessage("§c兑换码已过期！");
                    return;
                }
                if (data.getRemaining() <= 0) {
                    player.sendMessage("§c兑换码已被领完！");
                    return;
                }

                // 尝试原子扣除次数
                boolean success = db.claimCode(uuid, System.currentTimeMillis());
                if (success) {
                    // 发放奖励 (需要回到同步线程)
                    if (data.getType().equals("item")) {
                        SchedulerUtil.runEntity(plugin, player, () -> {
                            String[] parts = data.getContent().split(":");
                            Material mat = Material.getMaterial(parts[0]);
                            int amount = Integer.parseInt(parts[1]);
                            ItemStack item = new ItemStack(mat != null ? mat : Material.STONE, amount);

                            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                            for (ItemStack leftover : leftovers.values()) {
                                player.getWorld().dropItem(player.getLocation(), leftover);
                            }
                            player.sendMessage("§a兑换成功！获得了物品！");
                        });
                    } else if (data.getType().equals("cmd")) {
                        SchedulerUtil.runGlobal(plugin, () -> {
                            String cmdToRun = data.getContent().replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
                            player.sendMessage("§a兑换成功！");
                        });
                    }
                } else {
                    player.sendMessage("§c兑换失败，手慢了，兑换码可能刚被抢完或过期！");
                }
            } catch (SQLException e) {
                player.sendMessage("§c兑换时发生数据库错误！");
                e.printStackTrace();
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("giftcode.admin")) return completions;

        // 根据玩家输入的参数位置（args.length）提供不同的补全提示
        if (args.length == 1) {
            if ("create".startsWith(args[0].toLowerCase())) completions.add("create");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            if ("cmd".startsWith(args[1].toLowerCase())) completions.add("cmd");
            if ("item".startsWith(args[1].toLowerCase())) completions.add("item");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // 如果玩家还没输入，显示提示；如果已经输入数字，保留原样（避免删掉玩家打的字）
            completions.add(args[2].isEmpty() ? "<数量/次数>" : args[2]);
        } else if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            completions.add(args[3].isEmpty() ? "<过期时间秒,-1永久>" : args[3]);
        } else if (args.length == 5 && args[0].equalsIgnoreCase("create")) {
            if (args[1].equalsIgnoreCase("item")) {
                for (Material mat : Material.values()) {
                    if (mat.name().toLowerCase().startsWith(args[4].toLowerCase())) {
                        completions.add(mat.name());
                    }
                }
            } else if (args[1].equalsIgnoreCase("cmd")) {
                completions.add(args[4].isEmpty() ? "<输入要执行的命令(支持%player%)>" : args[4]);
            }
        } else if (args.length == 6 && args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("item")) {
            completions.add(args[5].isEmpty() ? "<物品数量>" : args[5]);
        }

        return completions;
    }
}