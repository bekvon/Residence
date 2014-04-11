package net.t00thpick1.residence.protection.yaml;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.UsernameUUIDCache;
import net.t00thpick1.residence.utils.uuid.NameFetcherRunnable;
import net.t00thpick1.residence.utils.uuid.UUIDFetcherRunnable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class YAMLUsernameUUIDCache implements UsernameUUIDCache {
    private FileConfiguration fileCache;
    private File saveLocation;

    public YAMLUsernameUUIDCache(File file) throws IOException {
        if (!file.isFile()) {
            file.createNewFile();
        }
        fileCache = YamlConfiguration.loadConfiguration(file);
        saveLocation = file;
    }

    public String getCachedName(UUID uuid) {
        if (uuid == null) {
            return "Server Land";
        }
        Player player = Residence.getInstance().getServer().getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        String name = fileCache.getString(uuid.toString());
        if (name == null) {
            grabUsernameFromMojang(uuid);
        }
        return name;
    }

    public void cacheName(UUID uuid, String name) {
        String old = fileCache.getString(name);
        if (old != null && !old.equalsIgnoreCase(uuid.toString())) {
            System.out.println(old);
            System.out.println(uuid.toString());
            // Refresh our cache for replaced user
            grabUsernameFromMojang(UUID.fromString(old));
        }
        fileCache.set(uuid.toString(), name);
        fileCache.set(name, uuid.toString());
    }

    @SuppressWarnings("deprecation")
    public UUID getCachedUUID(String name) {
        Player player = Residence.getInstance().getServer().getPlayer(name);
        if (player != null) {
            return player.getUniqueId();
        }
        String uuid = fileCache.getString(name);
        if (uuid != null) {
            return UUID.fromString(uuid);
        }
        grabUUIDFromMojang(name);
        return null;
    }

    private void grabUsernameFromMojang(UUID id) {
        new NameFetcherRunnable(id).runTaskAsynchronously(Residence.getInstance());
    }

    private void grabUUIDFromMojang(String name) {
        new UUIDFetcherRunnable(name).runTaskAsynchronously(Residence.getInstance());
    }

    public void save() throws IOException {
        fileCache.save(saveLocation);
    }
}
