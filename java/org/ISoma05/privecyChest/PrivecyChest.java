package org.ISoma05.privecyChest;

import org.ISoma05.privecyChest.commands.PrivacyChestCommand;
import org.ISoma05.privecyChest.listeners.ChestPlaceListener;
import org.ISoma05.privecyChest.listeners.ChestProtectionListener;
import org.ISoma05.privecyChest.listeners.GUIManager;
import org.ISoma05.privecyChest.data.PasswordManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrivecyChest extends JavaPlugin {

    private PasswordManager passwordManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.passwordManager = new PasswordManager(getDataFolder());

        GUIManager guiManager = new GUIManager(this, passwordManager);
        getServer().getPluginManager().registerEvents(guiManager, this);

        getServer().getPluginManager().registerEvents(new ChestPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectionListener(this, passwordManager), this);

        if (getCommand("privacychest") != null) {
            PrivacyChestCommand pcc = new PrivacyChestCommand(this, passwordManager, guiManager);
            getCommand("privacychest").setExecutor(pcc);
            getCommand("privacychest").setTabCompleter(pcc);
        } else {
            getLogger().warning("Command 'privacychest' is missing in plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
    }
}