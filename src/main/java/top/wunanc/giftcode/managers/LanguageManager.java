package top.wunanc.giftcode.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import top.wunanc.giftcode.GiftCode;

import java.io.File;

/**
 * 多语言管理器 (Language Manager)
 * 负责加载语言文件，处理占位符，并将文本转换为 MiniMessage 组件。
 */
public class LanguageManager {
    private final GiftCode plugin;
    private YamlConfiguration langConfig;
    private String prefix;

    public LanguageManager(GiftCode plugin) {
        this.plugin = plugin;
    }

    /**
     * 初始化语言系统，释放默认语言包并加载。
     */
    public void init() {
        plugin.saveDefaultConfig();
        String langName = plugin.getConfig().getString("language", "zh_cn");

        saveDefaultLang("zh_cn.yml");
        saveDefaultLang("en_us.yml");

        File langFile = new File(plugin.getDataFolder() + "/lang", langName + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + langName + ".yml not found. Falling back to zh_cn.yml");
            langFile = new File(plugin.getDataFolder() + "/lang", "zh_cn.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
        prefix = langConfig.getString("prefix", "<gray>[</gray><gold>GiftCode</gold><gray>]</gray> ");
    }

    private void saveDefaultLang(String fileName) {
        File file = new File(plugin.getDataFolder() + "/lang", fileName);
        if (!file.exists()) {
            plugin.saveResource("lang/" + fileName, false);
        }
    }

    /**
     * 获取带有前缀的 Component 文本组件
     * @param key 语言文件中的键
     * @param placeholders 占位符 (格式: "变量名1", "值1", "变量名2", "值2")
     * @return 格式化好的文本组件
     */
    public Component get(String key, String... placeholders) {
        String msg = langConfig.getString(key, "<red>Missing language key: " + key + ",try delete language file,regenerate</red>");

        // 替换动态变量（例如把 %uuid% 替换为实际的 uuid 字符串）
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }

        return MiniMessage.miniMessage().deserialize(prefix + msg);
    }

    /**
     * 快速向发送者发送一条语言消息
     */
    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    /**
     * 获取无前缀、未解析的纯文本字符串
     * 专门用于 Tab 命令补全等不支持 MiniMessage 富文本的地方
     * @param key 语言文件中的键
     * @return 纯文本字符串
     */
    public String getRaw(String key) {
        return langConfig.getString(key, "<" + key + ">");
    }

    public String getPrefix() { return langConfig.getString("prefix", "<gray>[</gray><gold>GiftCode</gold><gray>]</gray> "); }
}