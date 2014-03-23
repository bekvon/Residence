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
        return Material.matchMaterial(config.getString("General.InformationToolType"));
    }

    public boolean isEconomy() {
        return config.getBoolean("Economy.Enabled");
    }

    public String getLocale() {
        return config.getString("General.Locale");
    }

    public boolean stopOnLoadError() {
        return config.getBoolean("General.ShutdownIfFailLoad");
    }

    public int getRentCheckInterval() {
        return config.getInt("Economy.Rent.RentCheckInterval");
    }

    public boolean isRent() {
        return config.getBoolean("Economy.Rent.Enabled");
    }

    public int getAutoSaveInterval() {
        return config.getInt("General.AutoSaveInterval");
    }

    public String getDefaultEnterMessage() {
        if (noMessages()) {
            return null;
        }
        return config.getString("General.DefaultEnterMessage");
    }

    public String getDefaultLeaveMessage() {
        if (noMessages()) {
            return null;
        }
        return config.getString("General.DefaultLeaveMessage");
    }

    private boolean noMessages() {
        return config.getBoolean("General.EnterLeaveMessages");
    }

    public boolean isAutoRenewDefault() {
        return config.getBoolean("Economy.AutoRenewRentByDefault");
    }
}
