package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.mcstats.Metrics;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.*;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.bekvon.bukkit.residence.zip.ZipLibrary;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Residence extends JavaPlugin {
    protected static Residence instance;
    protected ResidenceManager rmanager;
    protected SelectionManager smanager;
    protected PermissionManager gmanager;
    protected ConfigManager cmanager;
    protected TransactionManager tmanager;
    protected PermissionListManager pmanager;
    protected WorldItemManager imanager;
    protected WorldFlagManager wmanager;
    protected RentManager rentmanager;
    protected ChatManager chatmanager;
    protected HelpEntry helppages;
    protected ResidenceVaultAdapter vault;

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (isInitialized()) {
            try {
                save();
                ZipLibrary.backup();
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
            getLogger().log(Level.INFO, "[Residence] Disabled!");
        }
        instance = null;
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

        cmanager = new ConfigManager(getConfig());
        gmanager = new PermissionManager();
        imanager = new WorldItemManager();
        wmanager = new WorldFlagManager();
        chatmanager = new ChatManager();

        if (cmanager.isEconomyEnabled()) {
            Plugin p = getServer().getPluginManager().getPlugin("Vault");
            if (p != null) {
                ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
                if (vault.economyOK()) {
                    getLogger().log(Level.INFO, "Found Vault using economy: " + vault.getEconomyName());
                    this.vault = vault;
                    rentmanager = new RentManager();
                    tmanager = new TransactionManager();
                } else {
                    getLogger().log(Level.INFO, "Found Vault, but Vault reported no usable economy system...");
                }
            } else {
                getLogger().log(Level.INFO, "Vault NOT found! No economy features will be enabled");
            }
        }
        try {
            loadSaves();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Unable to load save file", e);
            throw e;
        }
        
        rmanager = new ResidenceManager();

        pmanager = new PermissionListManager();

        FlagPermissions.initValidFlags();
        Plugin p = getServer().getPluginManager().getPlugin("WorldEdit");
        if (p != null) {
            smanager = new WorldEditSelectionManager();
            getLogger().log(Level.INFO, "Found WorldEdit");
        } else {
            smanager = new SelectionManager();
            getLogger().log(Level.INFO, "WorldEdit NOT found!");
        }


        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ResidenceBlockListener(), this);
        pm.registerEvents(new ResidencePlayerListener(), this);
        pm.registerEvents(new ResidenceEntityListener(), this);

        (new BukkitRunnable() {
            public void run() {
                Residence.getInstance().save();
            }
        }).runTaskTimer(this, cmanager.getAutoSaveInterval() * 60 * 20, cmanager.getAutoSaveInterval() * 60 * 20);
        (new BukkitRunnable() {
            public void run() {
                // Heals
            }
        }).runTaskTimer(this, 20, 20);
        if (cmanager.enabledRentSystem()) {
            (new BukkitRunnable() {
                public void run() {
                    // Heals
                }
            }).runTaskTimer(this, cmanager.getRentCheckInterval() * 60 * 20, cmanager.getRentCheckInterval() * 60 * 20);
        }
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }

    public ResidenceManager getResidenceManager() {
        return rmanager;
    }

    public SelectionManager getSelectionManager() {
        return smanager;
    }

    public PermissionManager getPermissionManager() {
        return gmanager;
    }

    public EconomyInterface getEconomyManager() {
        return vault;
    }

    public ConfigManager getConfigManager() {
        return cmanager;
    }

    public TransactionManager getTransactionManager() {
        return tmanager;
    }

    public WorldItemManager getItemManager() {
        return imanager;
    }

    public WorldFlagManager getWorldFlags() {
        return wmanager;
    }

    public RentManager getRentManager() {
        return rentmanager;
    }

    public ChatManager getChatManager() {
        return chatmanager;
    }

    private void save() throws IOException {
        File saveFolder = new File(getDataFolder(), "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for (Entry<String, Object> entry : save.entrySet()) {
            File ymlSaveLoc = new File(worldFolder, "res_" + entry.getKey() + ".yml");
            File tmpFile = new File(worldFolder, "tmp_res_" + entry.getKey() + ".yml");
            yml = new YMLSaveHelper(tmpFile);
            yml.getRoot().put("Version", 1);
            World world = getServer().getWorld(entry.getKey());
            if (world != null)
                yml.getRoot().put("Seed", world.getSeed());
            yml.getRoot().put("Residences", (Map) entry.getValue());
            yml.save();
            if (ymlSaveLoc.isFile()) {
                File backupFolder = new File(worldFolder, "Backup");
                backupFolder.mkdirs();
                File backupFile = new File(backupFolder, "res_" + entry.getKey() + ".yml");
                if (backupFile.isFile()) {
                    backupFile.delete();
                }
                ymlSaveLoc.renameTo(backupFile);
            }
            tmpFile.renameTo(ymlSaveLoc);
        }

        // For Sale save
        File ymlSaveLoc = new File(saveFolder, "forsale.yml");
        File tmpFile = new File(saveFolder, "tmp_forsale.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.save();
        yml.getRoot().put("Version", 1);
        yml.getRoot().put("Economy", tmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "forsale.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // permlist save
        ymlSaveLoc = new File(saveFolder, "permlists.yml");
        tmpFile = new File(saveFolder, "tmp_permlists.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", 1);
        yml.getRoot().put("PermissionLists", pmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "permlists.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // rent save
        ymlSaveLoc = new File(saveFolder, "rent.yml");
        tmpFile = new File(saveFolder, "tmp_rent.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", 1);
        yml.getRoot().put("RentSystem", rentmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "rent.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        if (cmanager.showIntervalMessages()) {
            getLogger().info("[Residence] - Saved Residences...");
        }
    }

    protected boolean loadSaves() throws Exception {
        File saveFolder = new File(getDataFolder(), "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!saveFolder.isDirectory()) {
                getLogger().warning("Save directory does not exist...");
                save();
            }
            YMLSaveHelper yml;
            File loadFile;
            HashMap<String, Object> worlds = new HashMap<String, Object>();
            for (World world : getServer().getWorlds()) {
                loadFile = new File(worldFolder, "res_" + world.getName() + ".yml");
                if (loadFile.isFile()) {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    worlds.put(world.getName(), yml.getRoot().get("Residences"));
                }
            }
            rmanager = ResidenceManager.load(worlds);
            loadFile = new File(saveFolder, "forsale.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                tmanager = TransactionManager.load((Map) yml.getRoot().get("Economy"), gmanager, rmanager);
            }
            loadFile = new File(saveFolder, "permlists.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                pmanager = PermissionListManager.load((Map) yml.getRoot().get("PermissionLists"));
            }
            loadFile = new File(saveFolder, "rent.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                rentmanager = RentManager.load((Map) yml.getRoot().get("RentSystem"));
            }
            return true;
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public static Residence getInstance() {
        return instance;
    }
}

