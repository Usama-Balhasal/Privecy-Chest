package org.ISoma05.privecyChest.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ISoma05.privecyChest.data.PasswordManager;
import org.ISoma05.privecyChest.listeners.GUIManager;

import java.util.*;

public class PrivacyChestCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final PasswordManager passwordManager;
    private final GUIManager guiManager;
    private final NamespacedKey ownerKey;

    public PrivacyChestCommand(JavaPlugin plugin, PasswordManager passwordManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.passwordManager = passwordManager;
        this.guiManager = guiManager;
        this.ownerKey = new NamespacedKey(plugin, "privacyowner");
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&6PChest » ") + "&r");
    }

    private boolean checkAccess(Player p) {
        String req = plugin.getConfig().getString("required-permission", "everyone");
        if (req.equalsIgnoreCase("everyone")) return true;
        if (p.hasPermission("privacychest.admin")) return true;
        if (p.hasPermission(req)) return true;

        p.sendMessage(prefix() + "§cYou do not have permission to use Privacy Chests.");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            // Allow console to reload if needed, but for now restricting to players as per structure
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // 1. OPEN GUI if no arguments
        if (args.length == 0) {
            if (!checkAccess(player)) return true;
            guiManager.openMainMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // 2. Admin Commands (Access & Reload)
        if (sub.equals("access") || sub.equals("reload")) {
            if (!player.hasPermission("privacychest.admin")) {
                player.sendMessage(prefix() + "§cYou do not have permission.");
                return true;
            }

            if (sub.equals("reload")) {
                plugin.reloadConfig();
                player.sendMessage(prefix() + "§aConfiguration reloaded successfully!");
                return true;
            }

            if (sub.equals("access")) {
                if (args.length < 2) {
                    player.sendMessage(prefix() + "§cUsage: /pchest access [everyone|<permission>]");
                    return true;
                }
                String newPerm = args[1];
                plugin.getConfig().set("required-permission", newPerm);
                plugin.saveConfig();
                player.sendMessage(prefix() + "§eAccess requirement set to: §b" + newPerm);
                return true;
            }
        }

        // Check general access for other commands
        if (!checkAccess(player)) return true;

        switch (sub) {
            case "setpassword":
                if (args.length < 2) {
                    player.sendMessage(prefix() + "§cUsage: /pchest setpassword <pw>");
                    return true;
                }
                if (passwordManager.hasPassword(player.getUniqueId())) {
                    player.sendMessage(prefix() + "§cYou already have a password set. Use GUI to manage.");
                    return true;
                }
                String pw = args[1];
                if (pw.length() < 4) {
                    player.sendMessage(prefix() + "§cPassword must be at least 4 characters.");
                    return true;
                }
                passwordManager.setPassword(player.getUniqueId(), pw);
                player.sendMessage(prefix() + "§aPassword set successfully.");
                return true;

            case "resetpassword":
                if (!passwordManager.hasPassword(player.getUniqueId())) {
                    player.sendMessage(prefix() + "§cYou don't have a password set.");
                    return true;
                }
                passwordManager.resetPassword(player.getUniqueId());
                player.sendMessage(prefix() + "§ePassword has been reset.");
                return true;

            case "create":
                if (args.length >= 2 && args[1].equalsIgnoreCase("all")) {
                    createAll(player);
                } else {
                    player.sendMessage(prefix() + "§eOpening menu to create chests...");
                    guiManager.openMainMenu(player);
                }
                return true;

            case "trust":
                player.sendMessage(prefix() + "§ePlease use the GUI: Type /privacychest");
                return true;

            default:
                player.sendMessage(prefix() + "§cUnknown command. Type /privacychest for the menu.");
                return true;
        }
    }

    private void createAll(Player player) {
        if (!passwordManager.hasPassword(player.getUniqueId())) {
            player.sendMessage(prefix() + "§cSet a password first!");
            return;
        }

        int count = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.CHEST) {
                if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING)) {
                    continue;
                }
                count += item.getAmount();
                player.getInventory().remove(item);
            }
        }

        if (count == 0) {
            player.sendMessage(prefix() + "§cYou have no normal chests in your inventory.");
            return;
        }

        ItemStack privateChests = guiManager.getPrivateChestItem(player);
        privateChests.setAmount(count);
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(privateChests);

        if (!left.isEmpty()) {
            player.sendMessage(prefix() + "§cInventory full! Dropping some items.");
            for (ItemStack i : left.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), i);
            }
        }

        player.sendMessage(prefix() + "§eConverted §b" + count + " §enormal chests into private chests!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("create", "setpassword", "resetpassword"));
            if (sender.hasPermission("privacychest.admin")) {
                completions.add("access");
                completions.add("reload");
            }
            return completions;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) return Collections.singletonList("all");
        return Collections.emptyList();
    }
}