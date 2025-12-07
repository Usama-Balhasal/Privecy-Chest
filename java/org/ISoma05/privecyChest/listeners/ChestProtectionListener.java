package org.ISoma05.privecyChest.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ISoma05.privecyChest.data.PasswordManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChestProtectionListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey ownerKey;
    private final PasswordManager passwordManager;

    public ChestProtectionListener(JavaPlugin plugin, PasswordManager passwordManager) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "privacyowner");
        this.passwordManager = passwordManager;
    }

    private String prefix() {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix", "&6PChest » "));
    }

    private String getOwnerUUIDString(Chest chest) {
        if (chest == null) return null;
        return chest.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST) return;
        if (!(event.getClickedBlock().getState() instanceof Chest)) return;

        Chest chest = (Chest) event.getClickedBlock().getState();
        String ownerId = getOwnerUUIDString(chest);

        if (ownerId == null) return; // Public chest

        Player player = event.getPlayer();
        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerId);
        } catch (IllegalArgumentException e) {
            return;
        }

        // 1. Owner Access
        if (player.getUniqueId().equals(ownerUUID)) return;

        // 2. Trusted Access
        if (passwordManager.isTrusted(ownerUUID, player.getUniqueId())) return;

        // 3. ADMIN BYPASS
        if (player.hasPermission("privacychest.admin")) {
            player.sendMessage(prefix() + "§c[Admin] §7Bypassing protection of §f" + ownerUUID.toString());
            return;
        }

        // Deny
        event.setCancelled(true);
        player.sendMessage(prefix() + "§cThis chest is locked. You do not have access.");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_LOCKED, 1f, 1f);
    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.CHEST) return;
        if (!(event.getBlock().getState() instanceof Chest)) return;

        Chest chest = (Chest) event.getBlock().getState();
        String ownerId = getOwnerUUIDString(chest);

        if (ownerId == null) return; // Public chest

        Player player = event.getPlayer();
        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerId);
        } catch (IllegalArgumentException e) {
            return;
        }

        // Security Check
        boolean isOwner = player.getUniqueId().equals(ownerUUID);
        boolean isAdmin = player.hasPermission("privacychest.admin");

        if (!isOwner && !isAdmin) {
            event.setCancelled(true);
            player.sendMessage(prefix() + "§cOnly the owner can break this private chest.");
            return;
        }

        if (isAdmin && !isOwner) {
            player.sendMessage(prefix() + "§c[Admin] §7Breaking private chest.");
        }

        // Handle Drops
        event.setDropItems(false);

        // 1. Drop contents
        for (ItemStack content : chest.getInventory().getContents()) {
            if (content != null && content.getType() != Material.AIR) {
                chest.getWorld().dropItemNaturally(chest.getLocation(), content);
            }
        }

        // 2. Drop the custom Private Chest Item
        ItemStack dropItem = createDropItem(ownerId);
        chest.getWorld().dropItemNaturally(chest.getLocation(), dropItem);

        // 3. Handle Double Chests
        // If it's a double chest, we are breaking ONE side.
        // The other side will remain as a single chest.
        // We must ensure the other side retains its privacy data (it should by default, but we check).
        InventoryHolder holder = chest.getInventory().getHolder();
        if (holder instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) holder;
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();

            // Note: The block breaking event only breaks the specific block clicked.
            // We do NOT need to manually break the other half.
            // Spigot automatically updates the other half to be a Single Chest.
            // Since PersistentData is stored on the TileEntity, the remaining block keeps its data.
            // However, we must ensure we don't accidentally drop a second chest item for the other half here.
            // We only drop the custom item for the block being broken, which we did above.
        }

        // Clear data (good practice, though block removal wipes it anyway)
        chest.getPersistentDataContainer().remove(ownerKey);
        chest.update();
    }

    private ItemStack createDropItem(String uuidStr) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = "Unknown";
            try {
                OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                if (p.getName() != null) name = p.getName();
            } catch (Exception ignored) {
            }

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chest-name", "&bPrivate Chest")));
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, uuidStr);

            List<String> configLore = plugin.getConfig().getStringList("chest-lore");
            List<String> finalLore = new ArrayList<>();
            for (String line : configLore) {
                finalLore.add(ChatColor.translateAlternateColorCodes('&', line.replace("%player%", name)));
            }
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}