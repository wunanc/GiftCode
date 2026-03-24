package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.managers.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 帮助菜单命令。
 * 会根据发送者的权限动态展示可用的命令列表。
 */
public class HelpCommand implements SubCommand {
    private final GiftCode plugin;
    private final LanguageManager lang;

    public HelpCommand(GiftCode plugin, LanguageManager lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 打印头部
        lang.send(sender, "help_header");

        // 所有玩家都能看到兑换命令
        lang.send(sender, "help_claim");

        // 只有拥有管理员权限的人才能看到后台管理命令
        if (sender.hasPermission("giftcode.admin")) {
            lang.send(sender, "help_create");
            lang.send(sender, "help_hand");
            lang.send(sender, "help_list");
            lang.send(sender, "help_delete");
            lang.send(sender, "help_clear");
            lang.send(sender, "help_reload");
        }

        // 打印尾部
        lang.send(sender, "help_footer");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        return null; // 所有人都可以使用 help 命令，只是看到的内容不同
    }
}