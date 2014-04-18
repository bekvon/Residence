package net.t00thpick1.residence;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private static ConfigManager instance;
    private FileConfiguration config;

    public ConfigManager(FileConfiguration fileConfiguration) {
        instance = this;
        this.config = fileConfiguration;
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public Material getSelectionToolType() {
        return Material.matchMaterial(config.getString("General.SelectionToolType"));
    }

    public Material getInfoToolType() {
        return Material.matchMaterial(config.getString("General.InfoToolType"));
    }

    public boolean isEconomy() {
        return config.getBoolean("Economy.Enabled", true);
    }

    public String getLocale() {
        return config.getString("General.Locale", "en_US");
    }

    public boolean stopOnLoadError() {
        return config.getBoolean("General.ShutdownIfFailLoad", true);
    }

    public int getRentCheckInterval() {
        return config.getInt("Economy.Rent.RentCheckInterval");
    }

    public boolean isRent() {
        return config.getBoolean("Economy.Rent.Enabled", true);
    }

    public int getAutoSaveInterval() {
        return config.getInt("General.AutoSaveInterval", 30);
    }

    public boolean noMessages() {
        return !config.getBoolean("General.EnterLeaveMessages", true);
    }

    public boolean isAutoRenewDefault() {
        return config.getBoolean("Economy.Rent.AutoRenewRentByDefault", true);
    }

    public boolean preserveUnregisteredFlags() {
        return config.getBoolean("General.PreserveUnregisteredFlags", true);
    }

    public boolean ignorePluginSpawns() {
        return config.getBoolean("General.IgnorePluginSpawns", true);
    }

    public boolean isAutoVert() {
        return config.getBoolean("General.AutoVertResidences", false);
    }
}
