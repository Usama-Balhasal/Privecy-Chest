package org.ISoma05.privecyChest.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PasswordManager {

    private final File file;
    private final FileConfiguration config;

    public PasswordManager(File dataFolder) {
        this.file = new File(dataFolder, "passwords.yml");

        if (!file.exists()) {
            try {
                dataFolder.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }


    public boolean hasPassword(UUID uuid) {
        return config.isSet("passwords." + uuid.toString());
    }

    public String getPassword(UUID uuid) {
        return config.getString("passwords." + uuid.toString());
    }

    public void setPassword(UUID uuid, String password) {
        config.set("passwords." + uuid.toString(), password);
        save();
    }

    public void resetPassword(UUID uuid) {
        config.set("passwords." + uuid.toString(), null);
        save();
    }


    public List<UUID> getTrustedList(UUID owner) {
        List<String> raw = config.getStringList("trusted." + owner.toString());
        if (raw == null) return Collections.emptyList();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public boolean isTrusted(UUID owner, UUID maybeTrusted) {
        List<UUID> list = getTrustedList(owner);
        return list.contains(maybeTrusted);
    }

    public void addTrusted(UUID owner, UUID trusted) {
        List<String> raw = new ArrayList<>(config.getStringList("trusted." + owner.toString()));
        String s = trusted.toString();
        if (!raw.contains(s)) {
            raw.add(s);
            config.set("trusted." + owner.toString(), raw);
            save();
        }
    }

    public void removeTrusted(UUID owner, UUID trusted) {
        List<String> raw = new ArrayList<>(config.getStringList("trusted." + owner.toString()));
        String s = trusted.toString();
        if (raw.contains(s)) {
            raw.remove(s);
            config.set("trusted." + owner.toString(), raw);
            save();
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}