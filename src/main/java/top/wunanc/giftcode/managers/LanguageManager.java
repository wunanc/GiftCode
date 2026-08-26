package top.wunanc.giftcode.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import top.wunanc.giftcode.GiftCode;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        // 若磁盘上的语言文件缺少插件内置的新增 key，则直接用内置版本覆盖，
        // 保证 update_* 等新增提示能正常加载；只比对键是否存在，不比对值，
        // 因此用户自定义翻译内容不会被覆盖。
        ensureKeysComplete(langFile);
        prefix = langConfig.getString("prefix", "<gray>[</gray><gold>GiftCode</gold><gray>]</gray> ");
    }

    private void saveDefaultLang(String fileName) {
        File file = new File(plugin.getDataFolder() + "/lang", fileName);
        if (!file.exists()) {
            plugin.saveResource("lang/" + fileName, false);
        }
    }

    /**
     * 检查磁盘语言文件是否包含了内置语言文件里的全部 key。
     * 仅当存在缺失 key（如旧版本残留文件缺少新增提示）时，才用内置文件覆盖，
     * 以补全缺失的键；用户已存在且自定义的翻译值保留不动。
     */
    private void ensureKeysComplete(File langFile) {
        InputStream bundled = plugin.getResource("lang/" + langFile.getName());
        if (bundled == null) {
            return;
        }
        YamlConfiguration bundledConfig =
                YamlConfiguration.loadConfiguration(new InputStreamReader(bundled, StandardCharsets.UTF_8));
        boolean missing = bundledConfig.getKeys(false).stream().anyMatch(key -> !langConfig.contains(key, true));
        if (!missing) {
            return;
        }
        plugin.getLogger().warning("Language file " + langFile.getName()
                + " is missing some keys. Overwriting with the built-in version to complete it.");
        plugin.saveResource("lang/" + langFile.getName(), true);
        langConfig = YamlConfiguration.loadConfiguration(langFile);
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
