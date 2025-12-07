package org.ISoma05.privecyChest.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ISoma05.privecyChest.data.PasswordManager;

import java.util.*;

public class GUIManager implements Listener {

    private final JavaPlugin plugin;
    private final PasswordManager passwordManager;
    private final NamespacedKey ownerKey;

    public GUIManager(JavaPlugin plugin, PasswordManager passwordManager) {
        this.plugin = plugin;
        this.passwordManager = passwordManager;
        this.ownerKey = new NamespacedKey(plugin, "privacyowner");
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&6PChest » "));
    }

    // --- GUI Builders ---

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Privacy Chest Menu");

        // 1. Password Icon
        ItemStack pw = new ItemStack(Material.NAME_TAG);
        ItemMeta pwMeta = pw.getItemMeta();
        pwMeta.setDisplayName(ChatColor.GOLD + "Change Password");
        pwMeta.setLore(Arrays.asList("§7Click to see how to", "§7change your password."));
        pw.setItemMeta(pwMeta);
        inv.setItem(11, pw);

        // 2. Trust Icon
        ItemStack trust = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta trustMeta = trust.getItemMeta();
        trustMeta.setDisplayName(ChatColor.AQUA + "Manage Trusted Players");
        trustMeta.setLore(Arrays.asList("§7Add or remove players", "§7who can open your chests."));
        trust.setItemMeta(trustMeta);
        inv.setItem(13, trust);

        // 3. Get Chest Icon
        ItemStack getChest = new ItemStack(Material.CHEST);
        ItemMeta getMeta = getChest.getItemMeta();
        getMeta.setDisplayName(ChatColor.GREEN + "Get Private Chest");
        getMeta.setLore(Arrays.asList("§7Cost: §f1 Chest", "§eClick to craft!"));
        getChest.setItemMeta(getMeta);
        inv.setItem(15, getChest);

        // Fillers
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }

    public void openTrustMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Trusted Players");

        List<UUID> trusted = passwordManager.getTrustedList(player.getUniqueId());

        // List Trusted Players (Heads)
        for (UUID uuid : trusted) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (off.getName() != null) meta.setOwningPlayer(off);
            meta.setDisplayName(ChatColor.RED + (off.getName() != null ? off.getName() : "Unknown"));
            meta.setLore(Arrays.asList("§7UUID: " + uuid.toString().substring(0, 8) + "...", "§eClick to REMOVE trust"));
            // Store UUID on item to identify it
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, uuid.toString());
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        // Add "Add Player" Button at bottom
        ItemStack addBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta addMeta = addBtn.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "+ Add Player");
        addMeta.setLore(Collections.singletonList("§7Click to select online players"));
        addBtn.setItemMeta(addMeta);
        inv.setItem(49, addBtn); // Bottom center

        // Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(ChatColor.YELLOW + "Back");
        back.setItemMeta(bMeta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }

    public void openOnlineSelection(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Select Player to Trust");

        List<UUID> alreadyTrusted = passwordManager.getTrustedList(player.getUniqueId());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().equals(player.getUniqueId())) continue; // Don't list self
            if (alreadyTrusted.contains(p.getUniqueId())) continue; // Don't list already trusted

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(p);
            meta.setDisplayName(ChatColor.GREEN + p.getName());
            meta.setLore(Collections.singletonList("§eClick to ADD trust"));
            // Store UUID
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, p.getUniqueId().toString());
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.setDisplayName(ChatColor.YELLOW + "Back");
        back.setItemMeta(bMeta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }

    // --- Logic / Events ---

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_GRAY + "")) return; // Quick filter

        if (e.getCurrentItem() == null) return;
        Player player = (Player) e.getWhoClicked();

        // Prevent taking items from GUI
        if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())) {
            e.setCancelled(true);
        } else {
            // Allow moving items in own inventory, but better to just cancel all for simplicity in this menu
            if (e.isShiftClick()) e.setCancelled(true);
            return;
        }

        // MAIN MENU
        if (title.contains("Privacy Chest Menu")) {
            switch (e.getRawSlot()) {
                case 11: // Password
                    player.closeInventory();
                    player.sendMessage(prefix() + "§eTo change password: /pchest setpassword <newpass>");
                    break;
                case 13: // Trust
                    openTrustMenu(player);
                    break;
                case 15: // Get Chest
                    doCraftChest(player);
                    break;
            }
        }
        // TRUST MENU
        else if (title.contains("Trusted Players")) {
            if (e.getRawSlot() == 49) { // Add Button
                openOnlineSelection(player);
                return;
            }
            if (e.getRawSlot() == 45) { // Back
                openMainMenu(player);
                return;
            }

            // Remove Player Logic
            ItemStack clicked = e.getCurrentItem();
            if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
                String uuidStr = clicked.getItemMeta().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
                if (uuidStr != null) {
                    passwordManager.removeTrusted(player.getUniqueId(), UUID.fromString(uuidStr));
                    player.sendMessage(prefix() + "§cRemoved trust.");
                    openTrustMenu(player); // Refresh
                }
            }
        }
        // ONLINE SELECT MENU
        else if (title.contains("Select Player")) {
            if (e.getRawSlot() == 45) { // Back
                openTrustMenu(player);
                return;
            }

            ItemStack clicked = e.getCurrentItem();
            if (clicked.getType() == Material.PLAYER_HEAD && clicked.hasItemMeta()) {
                String uuidStr = clicked.getItemMeta().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
                if (uuidStr != null) {
                    passwordManager.addTrusted(player.getUniqueId(), UUID.fromString(uuidStr));
                    player.sendMessage(prefix() + "§aAdded trust.");
                    openTrustMenu(player); // Go back to list
                }
            }
        }
    }

    private void doCraftChest(Player p) {
        if (!passwordManager.hasPassword(p.getUniqueId())) {
            p.closeInventory();
            p.sendMessage(prefix() + "§cSet a password first!");
            return;
        }

        ItemStack cost = new ItemStack(Material.CHEST, 1);
        if (!p.getInventory().containsAtLeast(cost, 1)) {
            p.closeInventory();
            p.sendMessage(prefix() + "§cYou need a normal Chest in your inventory.");
            return;
        }

        p.getInventory().removeItem(cost);
        p.getInventory().addItem(getPrivateChestItem(p));
        p.sendMessage(prefix() + "§a+1 Private Chest");
    }

    public ItemStack getPrivateChestItem(Player p) {
        ItemStack chest = new ItemStack(Material.CHEST);
        ItemMeta meta = chest.getItemMeta();
        String displayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chest-name", "&bPrivate Chest"));
        meta.setDisplayName(displayName);
        meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, p.getUniqueId().toString());
        List<String> configLore = plugin.getConfig().getStringList("chest-lore");
        List<String> finalLore = new ArrayList<>();
        for (String line : configLore) {
            finalLore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%player%", p.getName())));
        }
        meta.setLore(finalLore);
        chest.setItemMeta(meta);
        return chest;
    }
}