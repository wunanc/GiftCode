package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.database.DatabaseManager;
import top.wunanc.giftcode.managers.LanguageManager;
import top.wunanc.giftcode.model.CodeData;
import top.wunanc.giftcode.util.ItemSerializer;
import top.wunanc.giftcode.util.SchedulerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 手持物品兑换码创建命令。
 * 将玩家手上的物品（包含完整 NBT）打包成兑换码。
 */
public class HandCommand implements SubCommand {
    private final GiftCode plugin;
    private final DatabaseManager db;
    private final LanguageManager lang;

    public HandCommand(GiftCode plugin, DatabaseManager db, LanguageManager lang) {
        this.plugin = plugin;
        this.db = db;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.send(sender, "only_player");
            return;
        }
        Player player = (Player) sender;

        if (args.length < 3) {
            lang.send(player, "usage_hand");
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            lang.send(player, "hand_empty");
            return;
        }

        int uses;
        long expireTime;

        try {
            uses = Integer.parseInt(args[1]);
            long expireSeconds = Long.parseLong(args[2]);
            expireTime = expireSeconds == -1 ? -1 : System.currentTimeMillis() + (expireSeconds * 1000L);
        } catch (NumberFormatException e) {
            lang.send(sender, "param_error_number");
            return;
        }

        // 核心步骤：将物品序列化为 Base64 字符串
        String base64Content = ItemSerializer.toBase64(item);
        // 生成一个用于聊天框展示的友好名称（不然满屏都是乱码 Base64）
        String friendlyName = item.getType().name() + " x" + item.getAmount();

        String uuid = UUID.randomUUID().toString();
        // 我们把这种类型记为 "base64"
        CodeData codeData = new CodeData(uuid, "base64", base64Content, uses, expireTime);

        SchedulerUtil.runAsync(plugin, () -> {
            try {
                db.insertCode(codeData);
                lang.send(sender, "create_success",
                        "uuid", uuid,
                        "type", "hand (NBT)",
                        "content", friendlyName); // 展示友好名称而不是 Base64
            } catch (SQLException e) {
                lang.send(sender, "db_error", "error", e.getMessage());
            }
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 2) completions.add(args[1].isEmpty() ? lang.getRaw("tab_create_uses") : args[1]);
        if (args.length == 3) completions.add(args[2].isEmpty() ? lang.getRaw("tab_create_expire") : args[2]);
        return completions;
    }

    @Override
    public String getPermission() {
        return "giftcode.admin";
    }
}