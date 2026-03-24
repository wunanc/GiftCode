# Contributing to GiftCode

First off, thank you for considering contributing to GiftCode! It's people like you that make the open-source community such a great place to learn, inspire, and create.

## 🐛 Reporting Bugs
If you find a bug in the plugin, please create an issue on our Issue Tracker.
When reporting a bug, please include:
- Your server version (e.g., Paper 1.20.1)
- The GiftCode plugin version
- A clear description of the issue
- Steps to reproduce the bug
- Any error logs from the console (using Pastebin or similar services)

## 💡 Suggesting Enhancements
We welcome new ideas! If you have a feature request, please open an issue and use the `[Feature]` tag. Explain how the feature would work and why it would be beneficial to most users.

## 💻 Pull Requests
We gladly accept Pull Requests (PRs) for bug fixes and new features. To ensure a smooth process:
1. **Fork** the repository and clone it locally.
2. **Branch** out from the `master` or `main` branch (`git checkout -b feature/your-feature-name`).
3. **Commit** your changes clearly and concisely.
4. **Test** your changes on a local Minecraft server to ensure nothing is broken.
5. **Push** to your fork and submit a Pull Request.

### Code Guidelines
- Please follow standard Java conventions (CamelCase, meaningful variable names).
- Ensure your code doesn't produce warnings.
- If you are adding console outputs, please use our internal `XLogger` utility class and prefer **MiniMessage** syntax over legacy `&` color codes.
- Add comments to complex logic to help other developers understand your code.

Thank you for your contribution!