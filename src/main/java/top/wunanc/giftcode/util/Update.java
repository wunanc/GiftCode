package top.wunanc.giftcode.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件更新检查器 (Update checker)
 * 在插件加载时异步请求远程 update.json，若发现新版本则在控制台输出一次更新通知，
 * 并贴出各平台的下载页面链接。
 */
public final class Update {

    // 远程更新信息地址
    private static final String UPDATE_URL = "https://api.wunanc.top/MCPlugins/meta/GiftCode/update.json";
    // HTTP 请求超时时间
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private Update() {
        // 工具类，禁止实例化
    }

    /**
     * 在异步线程中检查插件更新，避免阻塞服务器启动主线程。
     *
     * @param plugin 当前插件实例
     */
    public static void checkUpdate(@NotNull JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String body = fetch(UPDATE_URL);
                if (body == null || body.isBlank()) {
                    return;
                }

                JsonObject root = JsonParser.parseString(body).getAsJsonObject();
                if (!root.has("latestVersion")) {
                    return;
                }

                JsonObject latest = root.getAsJsonObject("latestVersion");
                String remoteVersion = latest.has("name") ? latest.get("name").getAsString() : null;
                if (remoteVersion == null || remoteVersion.isBlank()) {
                    return;
                }

                String currentVersion = plugin.getPluginMeta().getVersion();
                if (!isNewer(remoteVersion, currentVersion)) {
                    // 当前已是最新版本，无需提示
                    return;
                }

                String summary = latest.has("summary") ? latest.get("summary").getAsString() : "";
                List<PlatformLink> links = parseLinks(latest);
                notifyUpdate(currentVersion, remoteVersion, summary, links);
            } catch (Exception ignored) {
                // 网络异常或解析失败时静默跳过，不影响插件正常运行
            }
        });
    }

    /**
     * 发起 GET 请求获取远程文本。
     */
    private static String fetch(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(TIMEOUT)
                .header("User-Agent", "GiftCode-UpdateChecker")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        return response.body();
    }

    /**
     * 解析 updateLogLinks 中的各平台下载链接。
     */
    private static List<PlatformLink> parseLinks(JsonObject latest) {
        List<PlatformLink> links = new ArrayList<>();
        if (!latest.has("updateLogLinks")) {
            return links;
        }
        JsonArray array = latest.getAsJsonArray("updateLogLinks");
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject obj = element.getAsJsonObject();
            String name = obj.has("name") ? obj.get("name").getAsString() : null;
            String url = obj.has("url") ? obj.get("url").getAsString() : null;
            if (name != null && url != null && !url.isBlank()) {
                links.add(new PlatformLink(name, url));
            }
        }
        return links;
    }

    /**
     * 比较版本号，判断远程版本是否比本地版本更新 (仅当更高时返回 true)。
     * 支持 "2.0.0" 这种点分语义化版本；若无法解析则按字典序比较。
     */
    static boolean isNewer(String remote, String current) {
        if (remote.equals(current)) {
            return false;
        }
        int[] r = parseVersion(remote);
        int[] c = parseVersion(current);
        if (r == null || c == null) {
            return remote.compareTo(current) > 0;
        }
        int len = Math.max(r.length, c.length);
        for (int i = 0; i < len; i++) {
            int rv = i < r.length ? r[i] : 0;
            int cv = i < c.length ? c[i] : 0;
            if (rv != cv) {
                return rv > cv;
            }
        }
        return false;
    }

    private static int[] parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }
        String[] parts = version.split("\\.");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                nums[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return nums;
    }

    /**
     * 向控制台输出一次更新通知，并贴出各平台下载链接。
     */
    private static void notifyUpdate(String current, String remote,
                                     String summary, List<PlatformLink> links) {
        XLogger.info("========================================");
        XLogger.warn("插件有更新！当前版本: " + current + " -> 最新版本: " + remote);
        if (!summary.isBlank()) {
            XLogger.info(summary);
        }
        if (links.isEmpty()) {
            XLogger.info("下载地址: " + UPDATE_URL);
        } else {
            XLogger.info("可前往以下平台下载最新版本：");
            for (PlatformLink link : links) {
                XLogger.info(link.name() + ": " + link.url());
            }
        }
        XLogger.info("========================================");
    }

    /**
     * 平台下载链接的轻量封装。
     */
    private record PlatformLink(String name, String url) {
    }
}
