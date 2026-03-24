<div align="center">

# 🎁GiftCode

[![Documentation](https://img.shields.io/badge/在线文档-点击跳转-70f3ff?logo=readthedocs)](https://mc.wunanc.top/giftcode/)
[![GitHub Repository](https://img.shields.io/badge/开源地址-GitHub-blue?logo=github)](https://github.com/wunanc/GiftCode)
[![bStats](https://img.shields.io/badge/bStats-Statistics-eacd76?logo=google-analytics)](https://bstats.org/plugin/bukkit/GiftCode/30358)

[![Latest Build](https://img.shields.io/github/v/release/wunanc/GiftCode?label=%E6%9C%80%E6%96%B0%E6%9E%84%E5%BB%BA%E4%B8%8B%E8%BD%BD&logo=github&color=0aa344)](https://github.com/wunanc/GiftCode/releases/latest)

[![Modrinth](https://img.shields.io/badge/To-Modrinth-1bd96a)](https://modrinth.com/plugin/lunadeer-dominion)
[![Spigot](https://img.shields.io/badge/To-Spigot-ed8106)](https://www.spigotmc.org/resources/dominion.119514/)

![](https://img.shields.io/github/downloads/wunanc/GiftCode/total?logo=github&label=Github%E4%B8%8B%E8%BD%BD%E9%87%8F)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/wunanc/GiftCode)

</div>

---

中文（简体） | [English](README.md)

## 简介

一款现代化、轻量级且高度可定制的 Minecraft 服务器礼包码/兑换码插件。

GiftCode 允许服主生成专属礼包码，玩家输入后可获得物品、游戏币或执行特定指令。插件全面支持 **MiniMessage** 格式，让你的提示信息拥有炫酷的颜色和渐变效果！

## 支持版本
- 1.20.1+ (Paper、Folia)

## ✨ 核心特性
- **富文本支持:** 全面采用现代化的 MiniMessage 语法（支持 `<gradient>`, `<rainbow>` 等）。
- **灵活的奖励机制:** 支持执行后台指令、给予物品或临时权限。
- **丰富的限制条件:** 可设置礼包码的全局兑换次数、单人兑换次数或过期时间。
- **极致性能:** 采用异步任务处理数据存储，保证主线程（TPS）不卡顿。
- **开发者 API:** 提供完善的 API，方便其他插件进行深度集成。

## 📥 安装指南
1. 前往[Releases](https://github.com/wunanc/GiftCode/releases) 页面下载最新版本的 `GiftCode-x.x.x.jar`。
2. 将下载的 `.jar` 文件放入服务器的 `plugins` 文件夹中。
3. 重启服务器以生成默认配置文件。
4. 按需修改 `config.yml`，然后使用 `/gc reload` 重载即可。

## 🚀 指令与权限

| 指令                          | 说明          | 权限节点                  |
|:----------------------------|:------------|:----------------------|
| `/gc <兑换码>`                 | 玩家兑换礼包      | `giftcode.use` (默认拥有) |
| `/gc create <item/cmd> ...` | 创建新的礼包码     | `giftcode.admin`      |
| `/gc hand ...`              | 编辑指定礼包码     | `giftcode.admin`      |
| `/gc delete <兑换码>`          | 删除指定礼包码     | `giftcode.admin`      |
| `/gc clear`                 | 清理过期和无效的礼包码 | `giftcode.admin`      |
| `/gc list`                  | 列出所有礼包码     | `giftcode.admin`      |
| `/gc reload`                | 重载配置文件      | `giftcode.admin`      |

## 🛠️ 源码构建
如果你想在本地编译此项目，你需要安装 JDK 17+ 以及相应的构建工具 Maven：
```bash
git clone https://github.com/wunanc/GiftCode.git
cd GiftCode
mvn clean package
```