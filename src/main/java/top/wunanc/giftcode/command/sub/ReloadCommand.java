package top.wunanc.giftcode.command.sub;

import org.bukkit.command.CommandSender;
import top.wunanc.giftcode.GiftCode;
import top.wunanc.giftcode.command.SubCommand;
import top.wunanc.giftcode.manager.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 重载命令。
 * 用于重新读取 config.yml 和对应的语言文件。
 */
public class ReloadCommand implements SubCommand {
    private final GiftCode plugin;
    private final LanguageManager lang;

    public ReloadCommand(GiftCode plugin, LanguageManager lang) {
        this.plugin = plugin;
        this.lang = lang;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 1. 重载 Bukkit 自带的 config.yml 缓存
        plugin.reloadConfig();

        // 2. 重新执行语言管理器的初始化（读取最新的语言文件内容）
        lang.init();

        // 3. 发送重载成功的提示消息
        lang.send(sender, "reload_success");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // 重载命令不需要额外的参数，直接返回空列表
        return new ArrayList<>();
    }

    @Override
    public String getPermission() {
        // 只有管理员可以执行重载
        return "giftcode.admin";
    }
}