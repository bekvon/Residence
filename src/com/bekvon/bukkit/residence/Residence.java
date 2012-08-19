/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;
import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.economy.*;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.*;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import cosine.boseconomy.BOSEconomy;
import fr.crafter.tickleman.realeconomy.RealEconomy;
import fr.crafter.tickleman.realplugin.RealPlugin;
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
import net.minecraft.server.SharedConstants;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

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
    protected static int leaseBukkitId=-1;
    protected static int rentBukkitId=-1;
    protected static int healBukkitId=-1;
    protected static int autosaveBukkitId=-1;
    protected static boolean initsuccess = false;
    protected Map<String,String> deleteConfirm;
    protected static List<String> resadminToggle;
    private final String[] validLanguages = { "English","German","French","Hungarian","Spanish","Chinese" };
    private Runnable doHeals = new Runnable() {
        public void run() {
            plistener.doHeals();
        }
    };
    private Runnable rentExpire = new Runnable()
    {
        public void run() {
            rentmanager.checkCurrentRents();
            if(cmanager.showIntervalMessages())
                System.out.println("[Residence] - Rent Expirations checked!");
        }
    };
    private Runnable leaseExpire = new Runnable() {
        public void run() {
            leasemanager.doExpirations();
            if(cmanager.showIntervalMessages())
                System.out.println("[Residence] - Lease Expirations checked!");
        }
    };
    private Runnable autoSave = new Runnable() {
        public void run() {
            try {
                if(initsuccess)
                {
                    saveYml();
                }
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
        }
    };

    public Residence() {
    }

    public void reloadPlugin()
    {
        server.getPluginManager().disablePlugin(this);
        server.getPluginManager().enablePlugin(this);
    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        server.getScheduler().cancelTask(healBukkitId);
        if (cmanager.useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        if(cmanager.enabledRentSystem())
        {
            server.getScheduler().cancelTask(rentBukkitId);
        }
        if(initsuccess)
        {
            try {
                saveYml();
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
            if(this.getConfig().getInt("ResidenceVersion", 0) == 0)
            {
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
            for(String lang : validLanguages)
            {
                try{
                    if(this.checkNewLanguageVersion(lang))
                        this.writeDefaultLanguageFile(lang);
                } catch (Exception ex){
                    System.out.println("[Residence] Failed to update language file: "+lang+".yml");
                    //Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                    helppages = new HelpEntry("");
                    language = new Language();
                }
            }
            try
            {
                File langFile = new File(new File(dataFolder, "Language"), cmanager.getLanguage() + ".yml");
                if(langFile.isFile())
                {
                    FileConfiguration langconfig = new YamlConfiguration();
                    langconfig.load(langFile);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                    HelpEntry.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    language = Language.parseText(langconfig, "Language");
                }
                else
                    System.out.println("[Residence] Language file does not exist...");
            }
            catch (Exception ex)
            {
                System.out.println("[Residence] Failed to load language file: " + cmanager.getLanguage() + ".yml, Error: " + ex.getMessage());
                Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                helppages = new HelpEntry("");
                language = new Language();
            }
            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                    System.out.println("[Residence] Scanning for economy systems...");
                    if(gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter)
                    {
                        ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
                        if(vault.economyOK())
                        {
                            economy = vault;
                            System.out.println("[Residence] Found Vault using economy system: " + vault.getEconomyName());
                        }
                    }
                    if(economy == null)
                        this.loadVaultEconomy();
                    if(economy == null)
                        this.loadBOSEconomy();
                    if(economy == null)
                        this.loadEssentialsEconomy();
                    if(economy == null)
                        this.loadRealEconomy();
                    if(economy == null)
                        this.loadIConomy();
                    if(economy == null)
                        System.out.println("[Residence] Unable to find an economy system...");
            }
            this.loadYml();
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
                if(!this.isEnabled())
                    return;
                FlagPermissions.initValidFlags();
                Plugin p = server.getPluginManager().getPlugin("WorldEdit");
                if(p!=null){
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

                //pm.registerEvent(Event.Type.WORLD_LOAD, wlistener, Priority.NORMAL, this);
                if(cmanager.enableSpout())
                {
                    slistener = new ResidenceSpoutListener();
                    pm.registerEvents(slistener, this);
                }
                firstenable = false;
            }
            else
            {
                plistener.reload();
            }
            int autosaveInt = cmanager.getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = (autosaveInt * 60) * 20;
            autosaveBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);
            healBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doHeals, 20, 20);
            if (cmanager.useLeases()) {
                int leaseInterval = cmanager.getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = (leaseInterval * 60) * 20;
                leaseBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            if(cmanager.enabledRentSystem())
            {
                int rentint = cmanager.getRentCheckInterval();
                if(rentint < 1)
                    rentint = 1;
                rentint = (rentint * 60) * 20;
                rentBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, rentExpire, rentint, rentint);
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
        if(name.contains(":") || name.contains("."))
            return false;
        if(cmanager.getResidenceNameRegex() == null)
        {
        	return true;
        }else{
	        String namecheck = name.replaceAll(cmanager.getResidenceNameRegex(), "");
	        if(!name.equals(namecheck))
	            return false;
	        return Residence.validString(name);
        }
    }

    public static boolean validString(String string)
    {
        for(int i = 0; i < string.length(); i++)
        {
            if(SharedConstants.allowedCharacters.indexOf(string.charAt(i)) < 0)
            {
                return false;
            }
        }
        return true;
    }

    public static File getDataLocation()
    {
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

    public static WorldItemManager getItemManager()
    {
        return imanager;
    }

    public static WorldFlagManager getWorldFlags()
    {
        return wmanager;
    }

    public static RentManager getRentManager()
    {
        return rentmanager;
    }

    public static ResidencePlayerListener getPlayerListener()
    {
        return plistener;
    }

    public static ResidenceBlockListener getBlockListener()
    {
        return blistener;
    }

    public static ResidenceEntityListener getEntityListener()
    {
        return elistener;
    }

    public static ChatManager getChatManager()
    {
        return chatmanager;
    }

    public static Language getLanguage()
    {
        if(language==null)
            language = new Language();
        return language;
    }

    public static FlagPermissions getPermsByLoc(Location loc)
    {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if(res!=null)
            return res.getPermissions();
        else
            return wmanager.getPerms(loc.getWorld().getName());
    }

    private void loadIConomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("iConomy");
        if (p != null) {
            if(p.getDescription().getVersion().startsWith("6"))
            {
                economy = new IConomy6Adapter((com.iCo6.iConomy)p);
            }
            else if(p.getDescription().getVersion().startsWith("5"))
            {
                economy = new IConomy5Adapter();
            }
            else
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] UNKNOWN iConomy version!");
                return;
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with iConomy! Version: " + p.getDescription().getVersion());
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] iConomy NOT found!");
        }
    }

    private void loadBOSEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("BOSEconomy");
        if (p != null) {
            economy = new BOSEAdapter((BOSEconomy)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with BOSEconomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] BOSEconomy NOT found!");
        }
    }

    private void loadEssentialsEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("Essentials");
        if (p != null) {
            economy = new EssentialsEcoAdapter((Essentials)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with Essentials Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Essentials Economy NOT found!");
        }
    }

    private void loadRealEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("RealPlugin");
        if (p != null) {
            economy = new RealShopEconomy(new RealEconomy((RealPlugin)p));
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with RealShop Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] RealShop Economy NOT found!");
        }
    }

    private void loadVaultEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if(p!=null)
        {
            ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
            if(vault.economyOK())
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault using economy: " + vault.getEconomyName());
                economy = vault;
            }
            else
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault, but Vault reported no usable economy system...");
            }
        } else
        {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Vault NOT found!");
        }
    }

    public static boolean isResAdminOn(Player player){
    	if(resadminToggle.contains(player.getName())){
    		return true;
    	}
    	return false;
    }
    public static void turnResAdminOn(Player player){
    	resadminToggle.add(player.getName());
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for(Entry<String, Object> entry : save.entrySet())
        {
            yml = new YMLSaveHelper(new File(worldFolder, "res_"+entry.getKey()+".yml"));
            yml.getRoot().put("Version", saveVersion);
            yml.getRoot().put("Seed", server.getWorld(entry.getKey()).getSeed());
            yml.getRoot().put("Residences", entry.getValue());
            yml.save();
        }
        yml = new YMLSaveHelper(new File(saveFolder,"forsale.yml"));
        yml.save();
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Economy", tmanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"leases.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Leases", leasemanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"permlists.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("PermissionLists", pmanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"rent.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("RentSystem", rentmanager.save());
        yml.save();
        if(cmanager.showIntervalMessages())
            System.out.println("[Residence] - Saved Residences...");
    }

    protected boolean loadYml() throws Exception
    {
        File saveFolder = new File(dataFolder, "Save");
        try {
            File oldFile = new File(dataFolder, "res.yml");
            if(oldFile.isFile() && !saveFolder.isDirectory())
            {
                System.out.println("[Residence] Upgrading to new save system...");
                this.oldLoadYMLSave(oldFile);
                this.saveYml();
                oldFile.delete();
                oldFile = new File(dataFolder, "res.yml.bak");
                if(oldFile.isFile())
                    oldFile.delete();
            }
            else
            {
                File worldFolder = new File(saveFolder, "Worlds");
                if(!saveFolder.isDirectory())
                {
                    System.out.println("[Residence] Save directory does not exist...");
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
                        /*Object obj = yml.getRoot().get("Seed");
                        Long seed = 0L;
                        if (obj != null) {
                            if (obj instanceof Long) {
                                seed = (Long) obj;
                            } else if (obj instanceof Integer) {
                                seed = ((Integer) obj).longValue();
                            }
                        }
                        if(seed==0 || seed == world.getSeed())*/
                        worlds.put(world.getName(), yml.getRoot().get("Residences"));
                        /*else
                        {
                            if(seed != 0)
                            {
                                File tempfile = new File(worldFolder,"res_worldseed_"+seed+".yml");
                                int i = 0;
                                while(tempfile == null || tempfile.isFile())
                                {
                                    tempfile = new File(worldFolder,"res_worldseed_"+seed+"_"+i+".yml");
                                    i++;
                                }
                                System.out.println("[Residence] Save Error: World Seed mis-match! world: " + world.getName() + " seed: " + world.getSeed() + " expected: " + seed + ".  Renaming to " + tempfile.getName());
                                loadFile.renameTo(tempfile);
                            }
                            else
                            {
                                File tempfile = new File(worldFolder,"res_unknown.yml");
                                int i = 0;
                                while(tempfile == null || tempfile.isFile())
                                {
                                    tempfile = new File(worldFolder,"res_unknown_"+i+".yml");
                                    i++;
                                }
                                System.out.println("[Residence] Save Error: World Seed missing! world: " + world.getName() + ". Renaming to " + tempfile.getName());
                                loadFile.renameTo(tempfile);
                            }
                        }*/
                    }
                }
                rmanager = ResidenceManager.load(worlds);
                loadFile = new File(saveFolder, "forsale.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    tmanager = TransactionManager.load((Map<String, Integer>) yml.getRoot().get("Economy"), gmanager, rmanager);
                }
                loadFile = new File(saveFolder, "leases.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    leasemanager = LeaseManager.load((Map<String, Long>) yml.getRoot().get("Leases"), rmanager);
                }
                loadFile = new File(saveFolder, "permlists.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    pmanager = PermissionListManager.load((Map<String, Object>) yml.getRoot().get("PermissionLists"));
                }
                loadFile = new File(saveFolder, "rent.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    rentmanager = RentManager.load((Map<String, Object>) yml.getRoot().get("RentSystem"));
                }
                //System.out.print("[Residence] Loaded...");
            }
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw(ex);
        }
    }

    private boolean oldLoadYMLSave(File saveLoc) throws Exception {
        if (saveLoc.isFile()) {
            YMLSaveHelper yml = new YMLSaveHelper(saveLoc);
            yml.load();
            rmanager = ResidenceManager.loadMap((Map<String, Object>) yml.getRoot().get("Residences"), new ResidenceManager());
            tmanager = TransactionManager.load((Map<String, Integer>) yml.getRoot().get("Economy"), gmanager, rmanager);
            leasemanager = LeaseManager.load((Map<String, Long>) yml.getRoot().get("Leases"), rmanager);
            pmanager = PermissionListManager.load((Map<String, Object>) yml.getRoot().get("PermissionLists"));
            rentmanager = RentManager.load((Map<String, Object>) yml.getRoot().get("RentSystem"));
            System.out.print("[Residence] Loaded Residences...");
            return true;
        } else {
            System.out.println("[Residence] Save File not found...");
            return false;
        }
    }

    private void writeDefaultConfigFromJar()
    {
        if(this.writeDefaultFileFromJar(new File(this.getDataFolder(), "config.yml"), "config.yml", true))
            System.out.println("[Residence] Wrote default config...");
    }

    private void writeDefaultLanguageFile(String lang)
    {
        File outFile = new File(new File(this.getDataFolder(),"Language"), lang+".yml");
        outFile.getParentFile().mkdirs();
        if(this.writeDefaultFileFromJar(outFile, "languagefiles/"+lang+".yml", true))
        {
            System.out.println("[Residence] Wrote default "+lang+" Language file...");
        }
    }

    private boolean checkNewLanguageVersion(String lang) throws IOException, FileNotFoundException, InvalidConfigurationException
    {
        File outFile = new File(new File(this.getDataFolder(),"Language"), lang+".yml");
        File checkFile = new File(new File(this.getDataFolder(),"Language"), "temp-"+lang+".yml");
        if(outFile.isFile())
        {
            FileConfiguration testconfig = new YamlConfiguration();
            testconfig.load(outFile);
            int oldversion = testconfig.getInt("FieldsVersion", 0);
            if(!this.writeDefaultFileFromJar(checkFile, "languagefiles/"+lang+".yml", false))
                return false;
            FileConfiguration testconfig2 = new YamlConfiguration();
            testconfig2.load(checkFile);
            int newversion = testconfig2.getInt("FieldsVersion", oldversion);
            if(checkFile.isFile())
                checkFile.delete();
            if(newversion>oldversion)
                return true;
            return false;
        }
        return true;
    }

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld)
    {
        try {
            File fileBackup = new File(this.getDataFolder(),"backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if(jarloc.isFile())
            {
                JarFile jar = new JarFile(jarloc);
                JarEntry entry = jar.getJarEntry(jarPath);
                if(entry!=null && !entry.isDirectory())
                {
                    InputStream in = jar.getInputStream(entry);
                    InputStreamReader isr = new InputStreamReader(in, "UTF8");
                    if(writeName.isFile())
                    {
                        if(backupOld)
                        {
                            if(fileBackup.isFile())
                                fileBackup.delete();
                            writeName.renameTo(fileBackup);
                        }
                        else
                            writeName.delete();
                    }
                    FileOutputStream out = new FileOutputStream(writeName);
                    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes,0,512);
                    while(readbytes>-1)
                    {
                    	osw.write(tempbytes,0,readbytes);
                        readbytes = isr.read(tempbytes,0,512);
                    }
                    osw.close();
                    isr.close();
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            System.out.println("[Residence] Failed to write file: " + writeName + " from the Residence jar file, Error:" + ex);
            return false;
        }
    }
}
