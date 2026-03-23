package top.wunanc.giftcode.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.manager.LanguageManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.ItemSerializer;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 玩家兑换处理类。
 * 这是插件最核心的功能，涉及到防并发抢码、异步转同步调度。
 */
public class ClaimCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;

    public ClaimCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 控制台不能兑换
        if (!(sender instanceof Player)) {
            lang.send(sender, "only_player");
            return;
        }
        Player player = (Player) sender;
        String uuid = args[0];

        lang.send(player, "verifying");

        // 将数据库查询和更新操作放入异步线程
        SchedulerUtil.runAsync(plugin, () -> {
            try {
                // 1. 查询该验证码当前的状态
                CodeData data = db.getCode(uuid);
                if (data == null) {
                    lang.send(player, "code_not_exist");
                    return;
                }
                if (data.getExpireTime() != -1 && data.getExpireTime() < System.currentTimeMillis()) {
                    lang.send(player, "code_expired");
                    return;
                }
                if (data.getRemaining() <= 0) {
                    lang.send(player, "code_depleted");
                    return;
                }

                // 2. 尝试抢码（原子扣除）。如果两个玩家同时请求，只有成功的那个返回 true
                boolean success = db.claimCode(uuid, System.currentTimeMillis());

                if (success) {
                    // 3. 抢码成功，开始发奖励。发奖励必须回到同步主线程。
                    if (data.getType().equals("item")) {
                        // 给予物品，需要 Folia 的 EntityScheduler
                        SchedulerUtil.runEntity(plugin, player, () -> {
                            String[] parts = data.getContent().split(":");
                            Material mat = Material.getMaterial(parts[0]);
                            int amount = Integer.parseInt(parts[1]);
                            ItemStack item = new ItemStack(mat != null ? mat : Material.STONE, amount);

                            // 如果玩家背包满了，多出来的物品会掉落在地上
                            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                            for (ItemStack leftover : leftovers.values()) {
                                player.getWorld().dropItem(player.getLocation(), leftover);
                            }
                            lang.send(player, "claim_success_item");
                        });
                    } else if (data.getType().equals("base64")) {
                        // 【新增的 Base64 复杂物品兑换逻辑】
                        SchedulerUtil.runEntity(plugin, player, () -> {
                            ItemStack item = ItemSerializer.fromBase64(data.getContent());
                            if (item != null) {
                                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                                for (ItemStack leftover : leftovers.values()) {
                                    player.getWorld().dropItem(player.getLocation(), leftover);
                                }
                                lang.send(player, "claim_success_item");
                            } else {
                                player.sendMessage("§c严重错误：物品数据损坏，无法发放！");
                            }
                        });
                    } else if (data.getType().equals("cmd")) {
                        // 执行全局命令，需要 Folia 的 GlobalRegionScheduler
                        SchedulerUtil.runGlobal(plugin, () -> {
                            // 替换 %player% 占位符为玩家真名
                            String cmdToRun = data.getContent().replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
                            lang.send(player, "claim_success_cmd");
                        });
                    }
                } else {
                    // 虽然查询时没过期/没用完，但扣除的瞬间被别人抢光了
                    lang.send(player, "claim_fail_fast");
                }
            } catch (SQLException e) {
                lang.send(player, "db_error", "error", e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) { return new ArrayList<>(); }

    @Override
    public String getPermission() { return null; } // 玩家兑换不需要特殊权限
}