# Privecy-Chest

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-blue.svg)](https://www.minecraft.net/)
[![Bukkit/Spigot](https://img.shields.io/badge/Platform-Bukkit%2FSpigot-green.svg)](https://www.spigotmc.org/)

A powerful Minecraft Bukkit/Spigot plugin that allows players to password-protect their chests, ensuring privacy and security for their valuable items.

## 📋 Features

- 🔒 **Password Protection**: Secure your chests with custom passwords
- 🎯 **Easy Management**: Simple commands to create, set, and manage chest passwords
- 🖥️ **GUI Interface**: User-friendly graphical interface for password input
- 👥 **Permission System**: Admin permissions for server management
- ⚙️ **Configurable**: Fully customizable configuration options
- 🔄 **Data Persistence**: Secure password storage using YAML files

## 🚀 Installation

1. Download the latest release from the [Releases](https://github.com/usamabalhasal/Privecy-Chest/releases) page
2. Place the `.jar` file in your server's `plugins` directory
3. Restart your server or reload the plugin using `/reload`
4. The plugin will automatically generate default configuration files

## 📖 Usage

### Basic Commands

```
/privacychest [create|setpassword|resetpassword|access|reload]
```

**Aliases:** `pchest`, `pch`

### Available Commands

- `/privacychest create` - Create a new privacy chest
- `/privacychest setpassword <password>` - Set or change chest password
- `/privacychest resetpassword` - Reset chest password (admin only)
- `/privacychest access <password>` - Access a protected chest
- `/privacychest reload` - Reload plugin configuration (admin only)

### How to Use

1. **Create a Chest**: Place a chest in the world
2. **Set Password**: Use `/privacychest setpassword <yourpassword>` while looking at the chest
3. **Access Chest**: Use `/privacychest access <password>` or use the GUI interface
4. **Manage**: Use commands to reset passwords or reload configuration

## ⚙️ Configuration

The plugin automatically creates a `config.yml` file in the plugin directory with default settings. You can modify this file to customize plugin behavior.

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `privacychest.admin` | Allows bypassing chest protection, managing access, and reloading the plugin | OP |


## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Usama Balhasal**

- GitHub: [@Usama-Balhasal](https://github.com/Usama-Balhasal)

## 🐛 Issues

If you encounter any bugs or have feature requests, please [create an issue](https://github.com/usamabalhasal/Privecy-Chest/issues) on GitHub.

## 🙏 Acknowledgments

- Minecraft Bukkit/Spigot community for the excellent API
- Contributors and testers who help improve this plugin
- The Minecraft modding community for inspiration and support

---

**Note**: This plugin is compatible with Minecraft 1.21+ and requires Bukkit/Spigot API.