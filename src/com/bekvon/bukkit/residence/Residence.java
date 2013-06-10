/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.economy.BOSEAdapter;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.EssentialsEcoAdapter;
import com.bekvon.bukkit.residence.economy.IConomy5Adapter;
import com.bekvon.bukkit.residence.economy.IConomy6Adapter;
import com.bekvon.bukkit.residence.economy.RealShopEconomy;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import com.residence.mcstats.Metrics;
import com.residence.zip.ZipLibrary;

import cosine.boseconomy.BOSEconomy;
import fr.crafter.tickleman.realeconomy.RealEconomy;
import fr.crafter.tickleman.realplugin.RealPlugin;

/**
 * 
 * @author Gary Smoak - bekvon
 * 
 */
public class Residence extends JavaPlugin {

    protected static ResidenceManager rmanager;
    protected static SelectionManager smanager;
    protected static PermissionManager gmanager;
    protected static ConfigManager cmanager;
    protected static ResidenceBlockListener blistener;
    protected static ResidencePlayerListener plistener;
    protected static ResidenceEntityListener elistener;
    protected static ResidenceSpoutListener slistener;
    protected static TransactionManager tmanager;
    protected static PermissionListManager pmanager;
    protected static LeaseManager leasemanager;
    protected static WorldItemManager imanager;
    protected static WorldFlagManager wmanager;
    protected static RentManager rentmanager;
    protected static ChatManager chatmanager;
    protected static Server server;
    protected static HelpEntry helppages;
    protected static Language language;
    protected boolean firstenable = true;
    protected static EconomyInterface economy;
    public final static int saveVersion = 1;
    protected static File dataFolder;
    protected static int leaseBukkitId = -1;
    protected static int rentBukkitId = -1;
    protected static int healBukkitId = -1;
    protected static int autosaveBukkitId = -1;
    protected static boolean initsuccess = false;
    protected Map<String, String> deleteConfirm;
    protected static List<String> resadminToggle;
    private final static String[] validLanguages = { "English", "German", "French", "Hungarian", "Spanish", "Chinese", "Czech", "Brazilian" };
    private Runnable doHeals = new Runnable() {
        public void run() {
            plistener.doHeals();
        }
    };
    private Runnable rentExpire = new Runnable()
    {
        public void run() {
            rentmanager.checkCurrentRents();
            if (cmanager.showIntervalMessages()) {
                System.out.println("[Residence] - Rent Expirations checked!");
            }
        }
    };
    private Runnable leaseExpire = new Runnable() {
        public void run() {
            leasemanager.doExpirations();
            if (cmanager.showIntervalMessages()) {
                System.out.println("[Residence] - Lease Expirations checked!");
            }
        }
    };
    private Runnable autoSave = new Runnable() {
        public void run() {
            try {
                if (initsuccess) {
                    saveYml();
                }
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
        }
    };

    public Residence() {
    }

    public void reloadPlugin() {
        this.onDisable();
        this.reloadConfig();
        this.onEnable();

    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        server.getScheduler().cancelTask(healBukkitId);
        if (cmanager.useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        if (cmanager.enabledRentSystem()) {
            server.getScheduler().cancelTask(rentBukkitId);
        }
        if (initsuccess) {
            try {
                saveYml();
                ZipLibrary.backup();
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Disabled!");
        }
    }

    @Override
    public void onEnable() {
        try {
            initsuccess = false;
            deleteConfirm = new HashMap<String, String>();
            resadminToggle = new ArrayList<String>();
            server = this.getServer();
            dataFolder = this.getDataFolder();
            if (!dataFolder.isDirectory()) {
                dataFolder.mkdirs();
            }

            if (!new File(dataFolder, "config.yml").isFile()) {
                this.writeDefaultConfigFromJar();
            }
            if (this.getConfig().getInt("ResidenceVersion", 0) == 0) {
                this.writeDefaultConfigFromJar();
                this.getConfig().load("config.yml");
                System.out.println("[Residence] Config Invalid, wrote default...");
            }
            cmanager = new ConfigManager(this.getConfig());
            String multiworld = cmanager.getMultiworldPlugin();
            if (multiworld != null) {
                Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                if (plugin != null) {
                    if (!plugin.isEnabled()) {
                        System.out.println("[Residence] - Enabling multiworld plugin: " + multiworld);
                        server.getPluginManager().enablePlugin(plugin);
                    }
                }
            }
            gmanager = new PermissionManager(this.getConfig());
            imanager = new WorldItemManager(this.getConfig());
            wmanager = new WorldFlagManager(this.getConfig());
            chatmanager = new ChatManager();
            rentmanager = new RentManager();
            for (String lang : validLanguages) {
                try {
                    if (this.checkNewLanguageVersion(lang)) {
                        this.writeDefaultLanguageFile(lang);
                    }
                } catch (Exception ex) {
                    System.out.println("[Residence] Failed to update language file: " + lang + ".yml");
                    helppages = new HelpEntry("");
                    language = new Language();
                }
            }
            try {
                File langFile = new File(new File(dataFolder, "Language"), cmanager.getLanguage() + ".yml");
                if (langFile.isFile()) {
                    FileConfiguration langconfig = new YamlConfiguration();
                    langconfig.load(langFile);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                    HelpEntry.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    language = Language.parseText(langconfig, "Language");
                } else {
                    System.out.println("[Residence] Language file does not exist...");
                }
            } catch (Exception ex) {
                System.out.println("[Residence] Failed to load language file: " + cmanager.getLanguage() + ".yml, Error: " + ex.getMessage());
                Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                helppages = new HelpEntry("");
                language = new Language();
            }
            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                System.out.println("[Residence] Scanning for economy systems...");
                if (gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                    ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
                    if (vault.economyOK())
                    {
                        economy = vault;
                        System.out.println("[Residence] Found Vault using economy system: " + vault.getEconomyName());
                    }
                }
                if (economy == null) {
                    this.loadVaultEconomy();
                }
                if (economy == null) {
                    this.loadBOSEconomy();
                }
                if (economy == null) {
                    this.loadEssentialsEconomy();
                }
                if (economy == null) {
                    this.loadRealEconomy();
                }
                if (economy == null) {
                    this.loadIConomy();
                }
                if (economy == null) {
                    System.out.println("[Residence] Unable to find an economy system...");
                }
            }
            try {
                this.loadYml();
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Unable to load save file", e);
                throw e;
            }
            if (rmanager == null) {
                rmanager = new ResidenceManager();
            }
            if (leasemanager == null) {
                leasemanager = new LeaseManager(rmanager);
            }
            if (tmanager == null) {
                tmanager = new TransactionManager(rmanager, gmanager);
            }
            if (pmanager == null) {
                pmanager = new PermissionListManager();
            }
            if (firstenable) {
                if (!this.isEnabled()) {
                    return;
                }
                FlagPermissions.initValidFlags();
                Plugin p = server.getPluginManager().getPlugin("WorldEdit");
                if (p != null) {
                    smanager = new WorldEditSelectionManager(server);
                    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found WorldEdit");
                } else {
                    smanager = new SelectionManager(server);
                    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] WorldEdit NOT found!");
                }

                blistener = new ResidenceBlockListener();
                plistener = new ResidencePlayerListener();
                elistener = new ResidenceEntityListener();
                PluginManager pm = getServer().getPluginManager();
                pm.registerEvents(blistener, this);
                pm.registerEvents(plistener, this);
                pm.registerEvents(elistener, this);

                // pm.registerEvent(Event.Type.WORLD_LOAD, wlistener,
                // Priority.NORMAL, this);
                if (cmanager.enableSpout()) {
                    slistener = new ResidenceSpoutListener();
                    pm.registerEvents(slistener, this);
                }
                firstenable = false;
            } else {
                plistener.reload();
            }
            int autosaveInt = cmanager.getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = autosaveInt * 60 * 20;
            autosaveBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);
            healBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doHeals, 20, 20);
            if (cmanager.useLeases()) {
                int leaseInterval = cmanager.getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = leaseInterval * 60 * 20;
                leaseBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            if (cmanager.enabledRentSystem()) {
                int rentint = cmanager.getRentCheckInterval();
                if (rentint < 1) {
                    rentint = 1;
                }
                rentint = rentint * 60 * 20;
                rentBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, rentExpire, rentint, rentint);
            }
            Player[] players = getServer().getOnlinePlayers();
            for (Player player : players) {
                if (Residence.getPermissionManager().isResidenceAdmin(player)) {
                    turnResAdminOn(player);
                }
            }
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Enabled! Version " + this.getDescription().getVersion() + " by bekvon");
            initsuccess = true;
        } catch (Exception ex) {
            initsuccess = false;
            getServer().getPluginManager().disablePlugin(this);
            System.out.println("[Residence] - FAILED INITIALIZATION! DISABLED! ERROR:");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean validName(String name)
    {
        if (name.contains(":") || name.contains(".")) {
            return false;
        }
        if (cmanager.getResidenceNameRegex() == null) {
            return true;
        } else {
            String namecheck = name.replaceAll(cmanager.getResidenceNameRegex(), "");
            if (!name.equals(namecheck)) {
                return false;
            }
            return true;
        }
    }

    public static File getDataLocation() {
        return dataFolder;
    }

    public static ResidenceManager getResidenceManager() {
        return rmanager;
    }

    public static SelectionManager getSelectionManager() {
        return smanager;
    }

    public static PermissionManager getPermissionManager() {
        return gmanager;
    }

    public static EconomyInterface getEconomyManager() {
        return economy;
    }

    public static Server getServ() {
        return server;
    }

    public static LeaseManager getLeaseManager() {
        return leasemanager;
    }

    public static ConfigManager getConfigManager() {
        return cmanager;
    }

    public static TransactionManager getTransactionManager() {
        return tmanager;
    }

    public static WorldItemManager getItemManager() {
        return imanager;
    }

    public static WorldFlagManager getWorldFlags() {
        return wmanager;
    }

    public static RentManager getRentManager() {
        return rentmanager;
    }

    public static ResidencePlayerListener getPlayerListener() {
        return plistener;
    }

    public static ResidenceBlockListener getBlockListener() {
        return blistener;
    }

    public static ResidenceEntityListener getEntityListener() {
        return elistener;
    }

    public static ChatManager getChatManager() {
        return chatmanager;
    }

    public static Language getLanguage() {
        if (language == null) {
            language = new Language();
        }
        return language;
    }

    public static FlagPermissions getPermsByLoc(Location loc) {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        } else {
            return wmanager.getPerms(loc.getWorld().getName());
        }
    }

    public static FlagPermissions getPermsByLocForPlayer(Location loc, Player player)
    {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        } else {
            if (player != null)
                return wmanager.getPerms(player);
            else
                return wmanager.getPerms(loc.getWorld().getName());
        }
    }

    private void loadIConomy() {
        Plugin p = getServer().getPluginManager().getPlugin("iConomy");
        if (p != null) {
            if (p.getDescription().getVersion().startsWith("6")) {
                economy = new IConomy6Adapter((com.iCo6.iConomy) p);
            } else if (p.getDescription().getVersion().startsWith("5")) {
                economy = new IConomy5Adapter();
            } else {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] UNKNOWN iConomy version!");
                return;
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with iConomy! Version: " + p.getDescription().getVersion());
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] iConomy NOT found!");
        }
    }

    private void loadBOSEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("BOSEconomy");
        if (p != null) {
            economy = new BOSEAdapter((BOSEconomy) p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with BOSEconomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] BOSEconomy NOT found!");
        }
    }

    private void loadEssentialsEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Essentials");
        if (p != null) {
            economy = new EssentialsEcoAdapter((Essentials) p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with Essentials Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Essentials Economy NOT found!");
        }
    }

    private void loadRealEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("RealPlugin");
        if (p != null) {
            economy = new RealShopEconomy(new RealEconomy((RealPlugin) p));
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with RealShop Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] RealShop Economy NOT found!");
        }
    }

    private void loadVaultEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if (p != null) {
            ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
            if (vault.economyOK()) {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault using economy: " + vault.getEconomyName());
                economy = vault;
            } else {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault, but Vault reported no usable economy system...");
            }
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Vault NOT found!");
        }
    }

    public static boolean isResAdminOn(Player player) {
        if (resadminToggle.contains(player.getName())) {
            return true;
        }
        return false;
    }

    public static void turnResAdminOn(Player player) {
        resadminToggle.add(player.getName());
    }

    public static boolean isResAdminOn(String player) {
        if (resadminToggle.contains(player.toLowerCase())) {
            return true;
        }
        return false;
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for (Entry<String, Object> entry : save.entrySet()) {
            File ymlSaveLoc = new File(worldFolder, "res_" + entry.getKey() + ".yml");
            File tmpFile = new File(worldFolder, "tmp_res_" + entry.getKey() + ".yml");
            yml = new YMLSaveHelper(tmpFile);
            yml.getRoot().put("Version", saveVersion);
            World world = server.getWorld(entry.getKey());
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
        yml.getRoot().put("Version", saveVersion);
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

        // Leases save
        ymlSaveLoc = new File(saveFolder, "leases.yml");
        tmpFile = new File(saveFolder, "tmp_leases.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Leases", leasemanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "leases.yml");
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
        yml.getRoot().put("Version", saveVersion);
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
        yml.getRoot().put("Version", saveVersion);
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
            System.out.println("[Residence] - Saved Residences...");
        }
    }

    protected boolean loadYml() throws Exception {
        File saveFolder = new File(dataFolder, "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!saveFolder.isDirectory()) {
                this.getLogger().warning("Save directory does not exist...");
                this.getLogger().warning("Please restart server");
                return true;
            }
            YMLSaveHelper yml;
            File loadFile;
            HashMap<String, Object> worlds = new HashMap<String, Object>();
            for (World world : server.getWorlds()) {
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
            loadFile = new File(saveFolder, "leases.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                leasemanager = LeaseManager.load((Map) yml.getRoot().get("Leases"), rmanager);
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
            // System.out.print("[Residence] Loaded...");
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void writeDefaultConfigFromJar() {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "config.yml"), "config.yml", true)) {
            System.out.println("[Residence] Wrote default config...");
        }
    }

    private void writeDefaultLanguageFile(String lang) {
        File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
        outFile.getParentFile().mkdirs();
        if (this.writeDefaultFileFromJar(outFile, "languagefiles/" + lang + ".yml", true)) {
            System.out.println("[Residence] Wrote default " + lang + " Language file...");
        }
    }

    private boolean checkNewLanguageVersion(String lang) throws IOException, FileNotFoundException, InvalidConfigurationException {
        File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
        File checkFile = new File(new File(this.getDataFolder(), "Language"), "temp-" + lang + ".yml");
        if (outFile.isFile()) {
            FileConfiguration testconfig = new YamlConfiguration();
            testconfig.load(outFile);
            int oldversion = testconfig.getInt("FieldsVersion", 0);
            if (!this.writeDefaultFileFromJar(checkFile, "languagefiles/" + lang + ".yml", false)) {
                return false;
            }
            FileConfiguration testconfig2 = new YamlConfiguration();
            testconfig2.load(checkFile);
            int newversion = testconfig2.getInt("FieldsVersion", oldversion);
            if (checkFile.isFile()) {
                checkFile.delete();
            }
            if (newversion > oldversion) {
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld) {
        try {
            File fileBackup = new File(this.getDataFolder(), "backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if (jarloc.isFile()) {
                JarFile jar = new JarFile(jarloc);
                JarEntry entry = jar.getJarEntry(jarPath);
                if (entry != null && !entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    InputStreamReader isr = new InputStreamReader(in, "UTF8");
                    if (writeName.isFile()) {
                        if (backupOld) {
                            if (fileBackup.isFile()) {
                                fileBackup.delete();
                            }
                            writeName.renameTo(fileBackup);
                        } else {
                            writeName.delete();
                        }
                    }
                    FileOutputStream out = new FileOutputStream(writeName);
                    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes, 0, 512);
                    while (readbytes > -1) {
                        osw.write(tempbytes, 0, readbytes);
                        readbytes = isr.read(tempbytes, 0, 512);
                    }
                    osw.close();
                    isr.close();
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            System.out.println("[Residence] Failed to write file: " + writeName);
            return false;
        }
    }
}

