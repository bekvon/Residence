package net.t00thpick1.residence;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.api.ResidenceArea;
import net.t00thpick1.residence.flags.move.StateAssurance;
import net.t00thpick1.residence.listeners.LoginLogoutListener;
import net.t00thpick1.residence.listeners.ToolListener;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.yaml.YAMLEconomyManager;
import net.t00thpick1.residence.protection.yaml.YAMLGroupManager;
import net.t00thpick1.residence.protection.yaml.YAMLResidenceManager;
import net.t00thpick1.residence.protection.yaml.YAMLWorldManager;
import net.t00thpick1.residence.selection.SelectionManager;
import net.t00thpick1.residence.selection.WorldEditSelectionManager;
import net.t00thpick1.residence.utils.CompatabilityManager;
import net.t00thpick1.residence.utils.metrics.Metrics;
import net.t00thpick1.residence.utils.zip.ZipLibrary;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
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
    public final static int saveVersion = 3;
    private static Residence instance;
    private YAMLResidenceManager rmanager;
    private SelectionManager smanager;
    private YAMLWorldManager wmanager;
    private Economy economy;
    private Permission permissions;
    private List<String> adminMode = new ArrayList<String>();
    private CompatabilityManager cmanager;

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (isInitialized()) {
            try {
                save();
                ZipLibrary.backup();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "SEVERE SAVE ERROR", ex);
            }
            getLogger().log(Level.INFO, "Disabled!");
        }
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

        File groupsFile = new File(dataFolder, "groups.yml");
        try {
            if (!groupsFile.isFile()) {
                groupsFile.createNewFile();
                FileConfiguration internalConfig = YamlConfiguration.loadConfiguration(getResource("groups.yml"));
                internalConfig.save(groupsFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new ConfigManager(getConfig());
        YAMLGroupManager.init(YamlConfiguration.loadConfiguration(groupsFile));
        File worldFolder = new File(dataFolder, "WorldConfigurations");
        if (!worldFolder.isDirectory()) {
            worldFolder.mkdirs();
        }
        try {
            wmanager = new YAMLWorldManager(worldFolder);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        cmanager = new CompatabilityManager();

        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if (p != null) {
            getLogger().log(Level.INFO, "Found Vault");
            setupVault();
        } else {
            getLogger().log(Level.INFO, "Vault NOT found!");
        }

        YAMLEconomyManager.init();
        if (!loadSaves()) {
            if (ConfigManager.getInstance().stopOnLoadError()) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
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
        FlagManager.initFlags();
        Plugin we = getServer().getPluginManager().getPlugin("WorldEdit");
        if (we != null) {
            smanager = new WorldEditSelectionManager(we);
            getLogger().log(Level.INFO, "Found WorldEdit");
        } else {
            smanager = new SelectionManager();
            getLogger().log(Level.INFO, "WorldEdit NOT found!");
        }

        PluginManager pm = getServer().getPluginManager();
        new ResidenceCommandExecutor(this);
        pm.registerEvents(new ToolListener(), this);
        pm.registerEvents(new LoginLogoutListener(), this);
        FlagManager.initFlags();

        (new BukkitRunnable() {
            public void run() {
                try {
                    Residence.getInstance().save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).runTaskTimer(this, 2000, ConfigManager.getInstance().getAutoSaveInterval() * 60 * 20);
        (new BukkitRunnable() {
            public void run() {
                Player[] p = getServer().getOnlinePlayers();
                for (Player player : p) {
                    ResidenceArea res = StateAssurance.getCurrentResidence(player.getName());
                    if (res != null && res.allowAction(FlagManager.HEALING)) {
                        double health = player.getHealth();
                        if (health < player.getMaxHealth() && !player.isDead()) {
                            player.setHealth(Math.min(health + 1, player.getMaxHealth()));
                        }
                    }
                }
            }
        }).runTaskTimer(this, 20, 20);
        if (ConfigManager.getInstance().isRent()) {
            (new BukkitRunnable() {
                public void run() {
                    YAMLEconomyManager.checkRent();
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

    public YAMLResidenceManager getResidenceManager() {
        return rmanager;
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

    private void save() throws IOException {
        wmanager.save();
        rmanager.save();
    }

    private boolean loadSaves() {
        File saveFolder = new File(getDataFolder(), "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!worldFolder.isDirectory()) {
                worldFolder.mkdirs();
            }
            rmanager = YAMLResidenceManager.load(worldFolder);
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to load save file", e);
            getLogger().info(LocaleLoader.getString("General.FailedLoad"));
            return false;
        }
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

    public YAMLWorldManager getWorldManager() {
        return wmanager;
    }
}
