<div align="center">

# 🎁GiftCode

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

[中文（简体）](https://github.com/wunanc/GiftCode/blob/master/README_CN.md) | English

## Introduction

A modern, lightweight, and highly customizable gift code/redeem code plugin for Minecraft servers.

GiftCode allows server administrators to generate unique gift codes. Players can redeem these codes to receive items, in-game currency, or execute specific commands. The plugin fully supports **MiniMessage** formatting, enabling vibrant and stylish messages with colors and gradients!

## Supported Versions
- 1.20.1+ (Paper, Folia)

## ✨ Key Features
- **Rich Text Support:** Fully utilizes modern MiniMessage syntax (supports `<gradient>`, `<rainbow>`, etc.).
- **Flexible Reward System:** Supports executing console commands, giving items, or granting temporary permissions.
- **Comprehensive Restrictions:** Set global usage limits, per-player usage limits, or expiration dates for each gift code.
- **High Performance:** Utilizes asynchronous tasks for data storage, ensuring no impact on the main server thread (TPS).
- **Developer API:** Provides a complete API for deep integration with other plugins.

## 📥 Installation Guide
1. Go to the [Releases](https://github.com/wunanc/GiftCode/releases) page and download the latest `GiftCode-x.x.x.jar`.
2. Place the downloaded `.jar` file into your server's `plugins` folder.
3. Restart the server to generate the default configuration files.
4. Modify `config.yml` as needed, then use `/gc reload` to reload the configuration.

## 🚀 Commands & Permissions

| Command                     | Description                     | Permission Node          |
|:----------------------------|:--------------------------------|:-------------------------|
| `/gc <code>`                | Redeem a gift code              | `giftcode.use` (Default) |
| `/gc create <item/cmd> ...` | Create a new gift code          | `giftcode.admin`         |
| `/gc hand ...`              | Edit a specified gift code      | `giftcode.admin`         |
| `/gc delete <code>`         | Delete a specified gift code    | `giftcode.admin`         |
| `/gc clear`                 | Clear expired and invalid codes | `giftcode.admin`         |
| `/gc list`                  | List all gift codes             | `giftcode.admin`         |
| `/gc reload`                | Reload the configuration file   | `giftcode.admin`         |

## 🛠️ Building from Source
To compile this project locally, you will need JDK 17+ and Maven:
```bash
git clone https://github.com/wunanc/GiftCode.git
cd GiftCode
mvn clean package
 ```

### 📄 LICENSE

This project is under the MIT license - see the [LICENSE](LICENSE) document for details.

### 👥 AUTHOR

- **wunanc | Hotguo** - *Main developer*

### 🙏 THANKS

Thank you to all the developers and users who contributed to this project!

### 📞 Support and feedback

- 🐛 [Report Bug](https://github.com/wunanc/GiftCode/issues)
- 💡 [Feature suggestions](https://github.com/wunanc/GiftCode/issues)
- 🐧 [QQ Group](https://qm.qq.com/q/OQ33f3SHeg)
- 📧 Contact authors: via GitHub Issues

---

<div align="center">

**Made with ❤️ by Hotguo**

⭐ If you like this project, consider giving it a Star!

[![bStats](https://bstats.org/signatures/bukkit/GiftCode.svg)](https://bstats.org/plugin/bukkit/GiftCode)

</div>