package top.wunanc.giftcode.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClaimCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;

    public ClaimCommand(GiftCode plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用兑换码！");
            return;
        }
        Player player = (Player) sender;
        String uuid = args[0];

        player.sendMessage("§e正在验证兑换码，请稍候...");
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

                boolean success = db.claimCode(uuid, System.currentTimeMillis());
                if (success) {
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
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return new ArrayList<>(); }

    @Override
    public String getPermission() { return null; } // 玩家领码不需要特殊权限
}