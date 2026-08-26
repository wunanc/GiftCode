<div align="center">

# 🎁 GiftCode 🎁

A modern, lightweight, and highly customizable gift code / redeem code plugin for Minecraft servers.

[![Documentation](https://img.shields.io/badge/Online%20Docs-Click%20to%20Visit-70f3ff?logo=readthedocs)](https://mc.wunanc.top/giftcode/)
[![GitHub Repository](https://img.shields.io/badge/Open%20Source-GitHub-blue?logo=github)](https://github.com/wunanc/GiftCode)
[![bStats](https://img.shields.io/badge/bStats-Statistics-eacd76?logo=google-analytics)](https://bstats.org/plugin/bukkit/GiftCode/30358)

[![Latest Build](https://img.shields.io/github/v/release/wunanc/GiftCode?label=Latest%20Build%20Download&logo=github&color=0aa344)](https://github.com/wunanc/GiftCode/releases/latest)

[![Modrinth](https://img.shields.io/badge/To-Modrinth-1bd96a)](https://modrinth.com/project/ozJTYG4R)
[![Spigot](https://img.shields.io/badge/To-Spigot-ed8106)](https://www.spigotmc.org/resources/giftcode.133723/)

![](https://img.shields.io/github/downloads/wunanc/GiftCode/total?logo=github&label=GitHub%20Downloads)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/wunanc/GiftCode)

</div>

---

English | [中文（简体）](Introduction_CN.md)

## What is GiftCode?

GiftCode lets server administrators generate unique gift codes that players redeem to receive items, in-game currency, or run specific commands. Every message is rendered with **MiniMessage**, so your prompts can show vivid colors and gradients instead of plain text.

## Supported Versions
- Minecraft `1.20.1+`
- Server software: **Paper** and **Folia** (Bukkit/Spigot-compatible API)

## ✨ Features
- **Rich Text Support:** Full modern MiniMessage syntax with `<gradient>`, `<rainbow>`, hover text, and more.
- **Flexible Rewards:** Grant items, give in-game currency, run console commands, or hand out temporary permissions.
- **Powerful Restrictions:** Set global redemption limits, per-player limits, or an expiration date for every code.
- **High Performance:** Asynchronous data storage keeps the main thread (TPS) smooth.
- **Developer API:** A complete API is provided for deep integration with other plugins.

## 📥 Installation
1. Download the latest `GiftCode-x.x.x.jar` from the [Releases](https://github.com/wunanc/GiftCode/releases/latest) page.
2. Drop the `.jar` into your server's `plugins` folder.
3. Restart the server to generate the default config files.
4. Edit `config.yml` as needed, then run `/gc reload` to apply changes.

## 🚀 Commands & Permissions

| Command                     | Description                 | Permission Node          |
|:----------------------------|:----------------------------|:-------------------------|
| `/gc <code>`                | Redeem a gift code          | `giftcode.use` (Default) |
| `/gc create <item/cmd> ...` | Create a new gift code      | `giftcode.admin`         |
| `/gc hand ...`              | Edit a specified gift code  | `giftcode.admin`         |
| `/gc delete <code>`         | Delete a gift code          | `giftcode.admin`         |
| `/gc clear`                 | Clear expired/invalid codes | `giftcode.admin`         |
| `/gc list`                  | List all gift codes         | `giftcode.admin`         |
| `/gc reload`                | Reload the config file      | `giftcode.admin`         |

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](https://github.com/wunanc/GiftCode/blob/master/LICENSE) file for details.

## 👥 Contributors

<a href="https://github.com/wunanc/GiftCode/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=wunanc/GiftCode"  alt="Contributors"/>
</a>

## 📞 Support & Feedback
- 🐛 [Report a Bug](https://github.com/wunanc/GiftCode/issues)
- 💡 [Suggest a Feature](https://github.com/wunanc/GiftCode/issues)
- 🐧 [QQ Group](https://qm.qq.com/q/OQ33f3SHeg)

## bStats Statistics

<div align="center">
<img src="https://bstats.org/signatures/bukkit/GiftCode.svg" alt="" width="100%">
</div>
