package org.ISoma05.privecyChest.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestPlaceListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey ownerKey;

    public ChestPlaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "privacyowner");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block placed = e.getBlockPlaced();
        if (placed.getType() != Material.CHEST) return;

        ItemStack item = e.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        String owner = item.getItemMeta().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        if (owner == null) return;

        // Check Permission Requirement
        String req = plugin.getConfig().getString("required-permission", "everyone");
        if (!req.equalsIgnoreCase("everyone")
                && !e.getPlayer().hasPermission("privacychest.admin")
                && !e.getPlayer().hasPermission(req)) {

            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou do not have permission to place Private Chests.");
            return;
        }

        // Apply ownership to the placed block
        if (placed.getState() instanceof Chest) {
            Chest chest = (Chest) placed.getState();
            chest.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner);
            chest.update();

            // --- Handle Double Chest logic ---
            // If placed next to another chest, they might merge.
            // We must ensure that if they merge, the other chest belongs to the SAME owner.
            // If the other chest is public or owned by someone else, we should ideally prevent merge or error out.

            if (chest.getInventory().getHolder() instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) chest.getInventory().getHolder();
                Chest left = (Chest) dc.getLeftSide();
                Chest right = (Chest) dc.getRightSide();

                Chest otherHalf = null;
                if (left.getLocation().equals(chest.getLocation())) {
                    otherHalf = right;
                } else {
                    otherHalf = left;
                }

                if (otherHalf != null) {
                    String otherOwner = otherHalf.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);

                    // Case 1: Other chest is public (no owner)
                    if (otherOwner == null) {
                        // Adopt the public chest into this private chest
                        otherHalf.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner);
                        otherHalf.update();
                    }
                    // Case 2: Other chest has a different owner
                    else if (!otherOwner.equals(owner)) {
                        // Prevent placement/merge to avoid stealing/conflict
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("§cYou cannot place a private chest next to someone else's private chest.");
                    }
                    // Case 3: Same owner - allow and ensure data is synced (already done above)
                }
            }
        }
    }
}