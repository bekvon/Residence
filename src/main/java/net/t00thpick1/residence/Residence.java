package net.t00thpick1.residence;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.protection.ProtectionFactory;
import net.t00thpick1.residence.selection.SelectionManager;
import net.t00thpick1.residence.selection.WorldEditSelectionManager;
import net.t00thpick1.residence.utils.CompatabilityManager;
import net.t00thpick1.residence.utils.metrics.Metrics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Residence extends JavaPlugin {
    public enum BackEndType {
        YAML,
        MYSQL,
        WORLDGUARD;
    }

    public final static int saveVersion = 3;
    private BackEndType backend = BackEndType.YAML;
    private static Residence instance;
    private SelectionManager smanager;
    private Economy economy;
    private Permission permissions;
    private List<String> adminMode = new ArrayList<String>();
    private CompatabilityManager cmanager;

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Saving");
        ProtectionFactory.save();
        getLogger().info("Disabled!");
        instance = null;
    }

    private void setupVault() {
        RegisteredServiceProvider<Economy> econProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (econProvider != null) {
            economy = econProvider.getProvider();
        }
        RegisteredServiceProvider<Permission> groupProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (groupProvider != null) {
            permissions = groupProvider.getProvider();
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        File dataFolder = getDataFolder();
        if (!dataFolder.isDirectory()) {
            dataFolder.mkdirs();
        }

        if (!new File(dataFolder, "config.yml").isFile()) {
            saveDefaultConfig();
        }

        new ConfigManager(getConfig());

        cmanager = new CompatabilityManager();

        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if (p != null) {
            getLogger().log(Level.INFO, "Found Vault");
            setupVault();
        } else {
            getLogger().log(Level.INFO, "Vault NOT found!");
        }

        FlagManager.initFlags();
        try {
            ProtectionFactory.init(this);
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        File commandsFile = new File(dataFolder, "commandhelp.yml");
        try {
            if (!commandsFile.isFile()) {
                commandsFile.createNewFile();
                FileOutputStream out = new FileOutputStream(commandsFile);
                InputStream in = getResource("commandhelp.yml");
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buf)) > 0) {
                    out.write(buf, 0, bytesRead);
                }
                out.close();
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HelpManager.init(YamlConfiguration.loadConfiguration(commandsFile));
        Plugin we = getServer().getPluginManager().getPlugin("WorldEdit");
        if (we != null) {
            smanager = new WorldEditSelectionManager(we);
            getLogger().log(Level.INFO, "Found WorldEdit");
        } else {
            smanager = new SelectionManager();
            getLogger().log(Level.INFO, "WorldEdit NOT found!");
        }
        new ResidenceCommandExecutor(this);
        (new BukkitRunnable() {
            public void run() {
                ProtectionFactory.save();
            }
        }).runTaskTimer(this, 2000, ConfigManager.getInstance().getAutoSaveInterval() * 60 * 20);
        if (ConfigManager.getInstance().isRent()) {
            (new BukkitRunnable() {
                public void run() {
                    ResidenceAPI.getEconomyManager().checkRent();
                }
            }).runTaskTimer(this, 20, ConfigManager.getInstance().getRentCheckInterval() * 60 * 20);
        }
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }

    public SelectionManager getSelectionManager() {
        return smanager;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public Economy getEconomy() {
        return economy;
    }

    public static Residence getInstance() {
        return instance;
    }

    public CompatabilityManager getCompatabilityManager() {
        return cmanager;
    }

    public void deactivateAdminMode(Player player) {
        adminMode.remove(player.getName());
    }

    public void activateAdminMode(Player player) {
        adminMode.add(player.getName());
    }

    public boolean isAdminMode(Player player) {
        return adminMode.contains(player.getName());
    }

    public BackEndType getBackend() {
        return backend;
    }
}
