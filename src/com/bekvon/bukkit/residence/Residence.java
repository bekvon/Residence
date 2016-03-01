package com.bekvon.bukkit.residence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
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
import com.bekvon.bukkit.residence.gui.FlagUtil;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidenceFixesListener;
import com.bekvon.bukkit.residence.allNms.v1_8Events;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.listeners.SpigotListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopSignUtil;
import com.bekvon.bukkit.residence.signsStuff.SignUtil;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.CrackShot;
import com.bekvon.bukkit.residence.utils.FileCleanUp;
import com.bekvon.bukkit.residence.utils.RandomTp;
import com.bekvon.bukkit.residence.utils.TabComplete;
import com.bekvon.bukkit.residence.utils.VersionChecker;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.residence.mcstats.Metrics;
import com.residence.zip.ZipLibrary;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import cosine.boseconomy.BOSEconomy;
import fr.crafter.tickleman.realeconomy.RealEconomy;
import fr.crafter.tickleman.realplugin.RealPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.OfflinePlayer;

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

    protected static SignUtil signmanager;

    protected static ResidenceBlockListener blistener;
    protected static ResidencePlayerListener plistener;
    protected static ResidenceEntityListener elistener;
    protected static ResidenceSpoutListener slistener;

    protected static ResidenceFixesListener flistener;

    protected static SpigotListener spigotlistener;
    protected static ShopListener shlistener;
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
    protected static Locale LocaleManager;
    protected static NewLanguage NewLanguageManager;
    protected static PlayerManager PlayerManager;
    protected static FlagUtil FlagUtilManager;
    protected static ShopSignUtil ShopSignUtilManager;
    protected static RandomTp RandomTpManager;

    public static Plugin instance2;
    protected boolean firstenable = true;
    protected static EconomyInterface economy;
    public final static int saveVersion = 1;
    protected static File dataFolder;
    protected static int leaseBukkitId = -1;
    protected static int rentBukkitId = -1;
    protected static int healBukkitId = -1;
    protected static int feedBukkitId = -1;

    protected static int DespawnMobsBukkitId = -1;

    protected static int autosaveBukkitId = -1;
    protected static VersionChecker versionChecker;
    protected static boolean initsuccess = false;
    public static Map<String, String> deleteConfirm;
    protected static List<String> resadminToggle;
    private final static String[] validLanguages = { "English", "German", "French", "Hungarian", "Spanish", "Chinese", "Czech", "Brazilian", "Polish", "Lithuanian" };
    public static ConcurrentHashMap<String, OfflinePlayer> OfflinePlayerList = new ConcurrentHashMap<String, OfflinePlayer>();
    public static WorldEditPlugin wep = null;
    public static WorldGuardPlugin wg = null;
    public static int wepid;

    private static String ServerLandname = "Server_Land";
    private static String ServerLandUUID = "00000000-0000-0000-0000-000000000000";
    private static String TempUserUUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private static NMS nms;
    static LWC lwc;

    public static NMS getNms() {
	return nms;
    }

    private Runnable doHeals = new Runnable() {
	public void run() {
	    plistener.doHeals();
	}
    };

    private Runnable doFeed = new Runnable() {
	public void run() {
	    plistener.feed();
	}
    };

    private Runnable DespawnMobs = new Runnable() {
	public void run() {
	    plistener.DespawnMobs();
	}
    };

    private Runnable rentExpire = new Runnable() {
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
		    Bukkit.getScheduler().runTaskAsynchronously(Residence.this, new Runnable() {
			@Override
			public void run() {
			    try {
				saveYml();
			    } catch (IOException e) {
				e.printStackTrace();
			    }
			    return;
			}
		    });
		}
	    } catch (Exception ex) {
		Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
	    }
	}
    };

    public void reloadPlugin() {
	this.onDisable();
	this.reloadConfig();
	this.onEnable();
    }

    @Override
    public void onDisable() {
	server.getScheduler().cancelTask(autosaveBukkitId);
	server.getScheduler().cancelTask(healBukkitId);
	server.getScheduler().cancelTask(feedBukkitId);

	server.getScheduler().cancelTask(DespawnMobsBukkitId);

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

//	    instance = this;
	    initsuccess = false;
	    deleteConfirm = new HashMap<String, String>();
	    resadminToggle = new ArrayList<String>();
	    server = this.getServer();
	    dataFolder = this.getDataFolder();
	    if (!dataFolder.isDirectory()) {
		dataFolder.mkdirs();
	    }

	    if (!new File(dataFolder, "groups.yml").isFile() && !new File(dataFolder, "flags.yml").isFile() && new File(dataFolder, "config.yml").isFile()) {
		this.ConvertFile();
	    }

	    if (!new File(dataFolder, "config.yml").isFile()) {
		this.writeDefaultConfigFromJar();
	    }
	    if (!new File(dataFolder, "flags.yml").isFile()) {
		this.writeDefaultFlagsFromJar();
	    }
	    if (!new File(dataFolder, "groups.yml").isFile()) {
		this.writeDefaultGroupsFromJar();
	    }
	    this.getCommand("res").setTabCompleter(new TabComplete());
	    this.getCommand("resadmin").setTabCompleter(new TabComplete());
	    this.getCommand("residence").setTabCompleter(new TabComplete());

//	    Residence.getConfigManager().UpdateConfigFile();

//	    if (this.getConfig().getInt("ResidenceVersion", 0) == 0) {
//		this.writeDefaultConfigFromJar();
//		this.getConfig().load("config.yml");
//		System.out.println("[Residence] Config Invalid, wrote default...");
//	    }

	    FileConfiguration canfig = YamlConfiguration.loadConfiguration(new File(dataFolder, "config.yml"));
	    FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(dataFolder, "flags.yml"));
	    FileConfiguration groups = YamlConfiguration.loadConfiguration(new File(dataFolder, "groups.yml"));
	    cmanager = new ConfigManager(canfig, flags, groups, this);
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
	    FlagUtilManager = new FlagUtil(this);
	    getFlagUtilManager().load();

	    String packageName = getServer().getClass().getPackage().getName();

	    String[] packageSplit = packageName.split("\\.");
	    String version = packageSplit[packageSplit.length - 1].split("(?<=\\G.{4})")[0];
	    try {
		Class<?> nmsClass;
		if (Residence.getConfigManager().CouldronCompatability())
		    nmsClass = Class.forName("com.bekvon.bukkit.residence.allNms.v1_7_Couldron");
		else
		    nmsClass = Class.forName("com.bekvon.bukkit.residence.allNms." + version);
		if (NMS.class.isAssignableFrom(nmsClass)) {
		    nms = (NMS) nmsClass.getConstructor().newInstance();
		} else {
		    System.out.println("Something went wrong, please note down version and contact author v:" + version);
		    this.setEnabled(false);
		}
	    } catch (ClassNotFoundException e) {
		System.out.println("Your server version is not compatible with this plugins version! Plugin will be disabled: " + version);
		this.setEnabled(false);
		return;
	    } catch (InstantiationException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    } catch (IllegalArgumentException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    } catch (NoSuchMethodException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    } catch (SecurityException e) {
		e.printStackTrace();
		this.setEnabled(false);
	    }

	    gmanager = new PermissionManager(groups, flags);

	    imanager = new WorldItemManager(flags);
	    wmanager = new WorldFlagManager(flags, groups);

	    chatmanager = new ChatManager();
	    rentmanager = new RentManager();

	    LocaleManager = new Locale(this);

	    PlayerManager = new PlayerManager(this);
	    ShopSignUtilManager = new ShopSignUtil(this);
	    RandomTpManager = new RandomTp(this);

	    Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
	    if (lwcp != null)
		lwc = ((LWCPlugin) lwcp).getLWC();

	    for (String lang : validLanguages) {
		YmlMaker langFile = new YmlMaker((JavaPlugin) this, "Language" + File.separator + lang + ".yml");
		if (langFile != null) {
		    langFile.saveDefaultConfig();
		}
	    }

	    for (String lang : validLanguages) {
		getLocaleManager().LoadLang(lang);
	    }

	    Residence.getConfigManager().UpdateFlagFile();

	    try {
		File langFile = new File(new File(dataFolder, "Language"), cmanager.getLanguage() + ".yml");

		BufferedReader in = null;
		try {
		    in = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), "UTF8"));
		} catch (UnsupportedEncodingException e1) {
		    e1.printStackTrace();
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}

		if (langFile.isFile()) {
		    FileConfiguration langconfig = new YamlConfiguration();
		    langconfig.load(in);
		    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
		    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
		    language = Language.parseText(langconfig, "Language");
		} else {
		    System.out.println("[Residence] Language file does not exist...");
		}
	    } catch (Exception ex) {
		System.out.println("[Residence] Failed to load language file: " + cmanager.getLanguage() + ".yml setting to default - English");

		File langFile = new File(new File(dataFolder, "Language"), "English.yml");

		BufferedReader in = null;
		try {
		    in = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), "UTF8"));
		} catch (UnsupportedEncodingException e1) {
		    e1.printStackTrace();
		} catch (FileNotFoundException e1) {
		    e1.printStackTrace();
		}

		if (langFile.isFile()) {
		    FileConfiguration langconfig = new YamlConfiguration();
		    langconfig.load(in);
		    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
		    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
		    language = Language.parseText(langconfig, "Language");
		} else {
		    System.out.println("[Residence] Language file does not exist...");
		}
	    }
	    economy = null;
	    if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
		System.out.println("[Residence] Scanning for economy systems...");
		if (gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
		    ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
		    if (vault.economyOK()) {
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

	    // Only fill if we need to convert player data
	    if (getConfigManager().isUUIDConvertion()) {
		Bukkit.getConsoleSender().sendMessage("[Residence] Loading (" + Bukkit.getOfflinePlayers().length + ") player data");
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
		    if (player == null)
			continue;
		    String name = player.getName();
		    if (name == null)
			continue;
		    getOfflinePlayerMap().put(name.toLowerCase(), player);
		}
		Bukkit.getConsoleSender().sendMessage("[Residence] Player data loaded: " + getOfflinePlayerMap().size());
	    } else {		
		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
		    @Override
		    public void run() {
			for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			    if (player == null)
				continue;
			    String name = player.getName();
			    if (name == null)
				continue;
			    getOfflinePlayerMap().put(name.toLowerCase(), player);
			}
			return;
		    }
		});
	    }

	    if (rmanager == null) {
		rmanager = new ResidenceManager(this);
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

	    try {
		this.loadYml();
	    } catch (Exception e) {
		this.getLogger().log(Level.SEVERE, "Unable to load save file", e);
		throw e;
	    }

	    if (Residence.getConfigManager().isUseResidenceFileClean())
		FileCleanUp.cleanFiles();

	    if (firstenable) {
		if (!this.isEnabled()) {
		    return;
		}
		FlagPermissions.initValidFlags();
		Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
		if (plugin != null) {
		    smanager = new WorldEditSelectionManager(server, this);
		    wep = (WorldEditPlugin) plugin;
		    wepid = ((WorldEditPlugin) Residence.wep).getConfig().getInt("wand-item");
		    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found WorldEdit");
		} else {
		    smanager = new SelectionManager(server, this);
		    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] WorldEdit NOT found!");
		}

		Plugin wgplugin = server.getPluginManager().getPlugin("WorldGuard");
		if (wgplugin == null) {
		    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] WorldGuard NOT found!");
		} else {
		    wg = (WorldGuardPlugin) wgplugin;
		    Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found WorldGuard");
		}

		blistener = new ResidenceBlockListener();
		plistener = new ResidencePlayerListener(this);
		elistener = new ResidenceEntityListener();
		flistener = new ResidenceFixesListener();

		shlistener = new ShopListener();
		spigotlistener = new SpigotListener();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blistener, this);
		pm.registerEvents(plistener, this);
		pm.registerEvents(elistener, this);
		pm.registerEvents(flistener, this);

		pm.registerEvents(shlistener, this);

		if (Bukkit.getVersion().toString().contains("Spigot") || Bukkit.getVersion().toString().contains("spigot"))
		    pm.registerEvents(spigotlistener, this);

		NewLanguageManager = new NewLanguage(this);
		getLM().LanguageReload();

		// 1.8 event
		if (VersionChecker.GetVersion() >= 1800)
		    pm.registerEvents(new v1_8Events(), this);

		if (getServer().getPluginManager().getPlugin("CrackShot") != null)
		    pm.registerEvents(new CrackShot(), this);

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
	    healBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doHeals, 20, Residence.getConfigManager().getHealInterval() * 20);
	    feedBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doFeed, 20, Residence.getConfigManager().getFeedInterval() * 20);
	    if (Residence.getConfigManager().AutoMobRemoval())
		DespawnMobsBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, DespawnMobs, 20 * Residence.getConfigManager().AutoMobRemovalInterval(), 20
		    * Residence.getConfigManager().AutoMobRemovalInterval());

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
	    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
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

	    PlayerManager.fillList();

	} catch (Exception ex) {
	    initsuccess = false;
	    getServer().getPluginManager().disablePlugin(this);
	    System.out.println("[Residence] - FAILED INITIALIZATION! DISABLED! ERROR:");
	    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
	}

	Residence.setSignUtil(this);
	Residence.getSignUtil().LoadSigns();

	getShopSignUtilManager().LoadShopVotes();
	getShopSignUtilManager().BoardUpdate();

	versionChecker = new VersionChecker(this);
	versionChecker.VersionCheck(null);
    }

    public static SignUtil getSignUtil() {
	return signmanager;
    }

    public static void setSignUtil(Residence plugin) {
	signmanager = new SignUtil(plugin);
    }

    public void consoleMessage(String message) {
	ConsoleCommandSender console = Bukkit.getConsoleSender();
	console.sendMessage("[Residence] " + message);
    }

    public static void sendMessage(Player player, String message) {
	player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
    }

    public static boolean validName(String name) {
	if (name.contains(":") || name.contains(".") || name.contains("|")) {
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

    public Residence getPlugin() {
	return this;
    }

    public static VersionChecker getVersionChecker() {
	return versionChecker;
    }

    public static LWC getLwc() {
	return lwc;
    }

    public static File getDataLocation() {
	return dataFolder;
    }

    public static ShopSignUtil getShopSignUtilManager() {
	return ShopSignUtilManager;
    }

    public static ResidenceManager getResidenceManager() {
	return rmanager;
    }

    public static SelectionManager getSelectionManager() {
	return smanager;
    }

    public static FlagUtil getFlagUtilManager() {
	return FlagUtilManager;
    }

    public static PermissionManager getPermissionManager() {
	return gmanager;
    }

    public static RandomTp getRandomTpManager() {
	return RandomTpManager;
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

    public static PlayerManager getPlayerManager() {
	return PlayerManager;
    }

    public static HelpEntry getHelpPages() {
	return helppages;
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

    public static Locale getLocaleManager() {
	return LocaleManager;
    }

    public static NewLanguage getLM() {
	return NewLanguageManager;
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

    public static FlagPermissions getPermsByLocForPlayer(Location loc, Player player) {
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
	if (resadminToggle.contains(player))
	    return true;
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
	    yml.getRoot().put("Residences", (Map<?, ?>) entry.getValue());
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean loadYml() throws Exception {
	File saveFolder = new File(dataFolder, "Save");
	try {
	    File worldFolder = new File(saveFolder, "Worlds");
	    if (!saveFolder.isDirectory()) {
		this.getLogger().warning("Save directory does not exist...");
		this.getLogger().warning("Please restart server");
		return true;
	    }
	    long time;
	    YMLSaveHelper yml;
	    File loadFile;
	    HashMap<String, Object> worlds = new HashMap<>();
	    for (World world : getServ().getWorlds()) {
		loadFile = new File(worldFolder, "res_" + world.getName() + ".yml");
		if (loadFile.isFile()) {
		    time = System.currentTimeMillis();
		    this.getLogger().info("Loading save data for world " + world.getName() + "...");
		    yml = new YMLSaveHelper(loadFile);
		    yml.load();
		    worlds.put(world.getName(), yml.getRoot().get("Residences"));

		    int pass = (int) (System.currentTimeMillis() - time);
		    String PastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

		    this.getLogger().info("Loaded " + world.getName() + " data. (" + PastTime + ")");
		}
	    }

	    rmanager = getResidenceManager().load(worlds);

	    // Getting shop residences
	    Map<String, ClaimedResidence> resList = rmanager.getResidences();
	    for (Entry<String, ClaimedResidence> one : resList.entrySet()) {

		ResidencePermissions perms = one.getValue().getPermissions();
		if (!perms.has("shop", false))
		    continue;

		rmanager.addShop(one.getValue().getName());
	    }

	    if (Residence.getConfigManager().isUUIDConvertion()) {
		Residence.getConfigManager().ChangeConfig("Global.UUIDConvertion", false);
	    }

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

    private void writeDefaultGroupsFromJar() {
	if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "groups.yml"), "groups.yml", true)) {
	    System.out.println("[Residence] Wrote default groups...");
	}
    }

    private void writeDefaultFlagsFromJar() {
	if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "flags.yml"), "flags.yml", true)) {
	    System.out.println("[Residence] Wrote default flags...");
	}
    }

    private void ConvertFile() {
	File file = new File(this.getDataFolder(), "config.yml");

	File file_old = new File(this.getDataFolder(), "config_old.yml");

	File newfile = new File(this.getDataFolder(), "groups.yml");

	File newTempFlags = new File(this.getDataFolder(), "flags.yml");

	try {
	    copy(file, file_old);
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	try {
	    copy(file, newfile);
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	try {
	    copy(file, newTempFlags);
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	File newGroups = new File(this.getDataFolder(), "config.yml");

	List<String> list = new ArrayList<String>();
	list.add("ResidenceVersion");
	list.add("Global.Flags");
	list.add("Global.FlagPermission");
	list.add("Global.ResidenceDefault");
	list.add("Global.CreatorDefault");
	list.add("Global.GroupDefault");
	list.add("Groups");
	list.add("GroupAssignments");
	list.add("ItemList");

	try {
	    remove(newGroups, list);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	File newConfig = new File(this.getDataFolder(), "groups.yml");
	list.clear();
	list = new ArrayList<String>();
	list.add("ResidenceVersion");
	list.add("Global");
	list.add("ItemList");

	try {
	    remove(newConfig, list);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	File newFlags = new File(this.getDataFolder(), "flags.yml");
	list.clear();
	list = new ArrayList<String>();
	list.add("ResidenceVersion");
	list.add("GroupAssignments");
	list.add("Groups");
	list.add("Global.Language");
	list.add("Global.SelectionToolId");
	list.add("Global.InfoToolId");
	list.add("Global.MoveCheckInterval");
	list.add("Global.SaveInterval");
	list.add("Global.DefaultGroup");
	list.add("Global.UseLeaseSystem");
	list.add("Global.LeaseCheckInterval");
	list.add("Global.LeaseAutoRenew");
	list.add("Global.EnablePermissions");
	list.add("Global.LegacyPermissions");
	list.add("Global.EnableEconomy");
	list.add("Global.EnableRentSystem");
	list.add("Global.RentCheckInterval");
	list.add("Global.ResidenceChatEnable");
	list.add("Global.UseActionBar");
	list.add("Global.ResidenceChatColor");
	list.add("Global.AdminOnlyCommands");
	list.add("Global.AdminOPs");
	list.add("Global.MultiWorldPlugin");
	list.add("Global.ResidenceFlagsInherit");
	list.add("Global.PreventRentModify");
	list.add("Global.StopOnSaveFault");
	list.add("Global.ResidenceNameRegex");
	list.add("Global.ShowIntervalMessages");
	list.add("Global.VersionCheck");
	list.add("Global.CustomContainers");
	list.add("Global.CustomBothClick");
	list.add("Global.CustomRightClick");

	try {
	    remove(newFlags, list);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void remove(File newGroups, List<String> list) throws IOException {

	YamlConfiguration conf = YamlConfiguration.loadConfiguration(newGroups);
	conf.options().copyDefaults(true);

	for (String one : list) {
	    conf.set(one, null);
	}
	try {
	    conf.save(newGroups);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void copy(File source, File target) throws IOException {
	InputStream in = new FileInputStream(source);
	OutputStream out = new FileOutputStream(target);
	byte[] buf = new byte[1024];
	int len;
	while ((len = in.read(buf)) > 0) {
	    out.write(buf, 0, len);
	}
	in.close();
	out.close();
    }

//    private void writeDefaultLanguageFile(String lang) {
//	File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
//	outFile.getParentFile().mkdirs();
//	if (this.writeDefaultFileFromJar(outFile, "languagefiles/" + lang + ".yml", true)) {
//	    System.out.println("[Residence] Wrote default " + lang + " Language file...");
//	}
//    }
//
//    private boolean checkNewLanguageVersion(String lang) throws IOException, FileNotFoundException, InvalidConfigurationException {
//	File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
//	File checkFile = new File(new File(this.getDataFolder(), "Language"), "temp-" + lang + ".yml");
//	if (outFile.isFile()) {
//	    FileConfiguration testconfig = new YamlConfiguration();
//	    testconfig.load(outFile);
//	    int oldversion = testconfig.getInt("FieldsVersion", 0);
//	    if (!this.writeDefaultFileFromJar(checkFile, "languagefiles/" + lang + ".yml", false)) {
//		return false;
//	    }
//	    FileConfiguration testconfig2 = new YamlConfiguration();
//	    testconfig2.load(checkFile);
//	    int newversion = testconfig2.getInt("FieldsVersion", oldversion);
//	    if (checkFile.isFile()) {
//		checkFile.delete();
//	    }
//	    if (newversion > oldversion) {
//		return true;
//	    }
//	    return false;
//	}
//	return true;
//    }

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
		jar.close();
	    }
	    return false;
	} catch (Exception ex) {
	    System.out.println("[Residence] Failed to write file: " + writeName);
	    return false;
	}
    }

    public static boolean isPlayerExist(Player player, String name, boolean inform) {
	if (Residence.getPlayerUUID(name) != null)
	    return true;
	if (inform)
	    player.sendMessage(getLM().getMessage("Language.InvalidPlayer"));
	return false;

    }

    public static UUID getPlayerUUID(String playername) {
//	if (Residence.getConfigManager().isOfflineMode())
//	    return null;
	Player p = Residence.getServ().getPlayer(playername);
	if (p == null) {
	    if (getOfflinePlayerMap().containsKey(playername.toLowerCase()))
		return getOfflinePlayerMap().get(playername.toLowerCase()).getUniqueId();
	} else
	    return p.getUniqueId();
	return null;
    }

    public static ConcurrentHashMap<String, OfflinePlayer> getOfflinePlayerMap() {
	return OfflinePlayerList;
    }

    @SuppressWarnings("deprecation")
    public static OfflinePlayer getOfflinePlayer(String Name) {
	if (OfflinePlayerList.containsKey(Name.toLowerCase())) {
	    return getOfflinePlayerMap().get(Name.toLowerCase());
	}
	OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(Name);
	if (offPlayer != null)
	    getOfflinePlayerMap().put(Name.toLowerCase(), offPlayer);
	return offPlayer;
    }

    public static String getPlayerUUIDString(String playername) {
	UUID playerUUID = Residence.getPlayerUUID(playername);
	if (playerUUID != null)
	    return playerUUID.toString();
	return null;
    }

    public static String getPlayerName(String uuid) {
	try {
	    return Residence.getPlayerName(UUID.fromString(uuid));
	} catch (IllegalArgumentException ex) {
	}
	return null;
    }

    public static String getServerLandname() {
	return ServerLandname;
    }

    public static String getServerLandUUID() {
	return ServerLandUUID;
    }

    public static String getTempUserUUID() {
	return TempUserUUID;
    }

    public static String getPlayerName(UUID uuid) {
	OfflinePlayer p = Residence.getServ().getPlayer(uuid);
	if (p == null)
	    p = Residence.getServ().getOfflinePlayer(uuid);
	if (p != null)
	    return p.getName();
	else
	    return null;
    }
}
