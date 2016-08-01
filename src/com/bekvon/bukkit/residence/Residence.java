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
import java.lang.reflect.Method;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.containers.ABInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.NMS;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.dynmap.DynMapListeners;
import com.bekvon.bukkit.residence.dynmap.DynMapManager;
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
import com.bekvon.bukkit.residence.allNms.v1_10Events;
import com.bekvon.bukkit.residence.allNms.v1_8Events;
import com.bekvon.bukkit.residence.allNms.v1_9Events;
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
import com.bekvon.bukkit.residence.protection.PlayerManager;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.selection.AutoSelection;
import com.bekvon.bukkit.residence.selection.SchematicsManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopSignUtil;
import com.bekvon.bukkit.residence.signsStuff.SignUtil;
import com.bekvon.bukkit.residence.spout.ResidenceSpout;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.utils.ActionBar;
import com.bekvon.bukkit.residence.utils.CrackShot;
import com.bekvon.bukkit.residence.utils.FileCleanUp;
import com.bekvon.bukkit.residence.utils.RandomTp;
import com.bekvon.bukkit.residence.utils.Sorting;
import com.bekvon.bukkit.residence.utils.TabComplete;
import com.bekvon.bukkit.residence.utils.VersionChecker;
import com.bekvon.bukkit.residence.utils.YmlMaker;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.bekvon.bukkit.residence.api.*;
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

    protected static String ResidenceVersion;
    protected static List<String> authlist;
    protected static ResidenceManager rmanager;
    protected static SelectionManager smanager;
    public static PermissionManager gmanager;
    protected static ConfigManager cmanager;

    protected static boolean spigotPlatform = false;

    protected static SignUtil signmanager;

    protected static ResidenceBlockListener blistener;
    protected static ResidencePlayerListener plistener;
    protected static ResidenceEntityListener elistener;
    protected static ResidenceSpoutListener slistener;
    protected static ResidenceSpout spout;

    protected static ResidenceFixesListener flistener;

    protected static SpigotListener spigotlistener;
    protected static ShopListener shlistener;
    protected static TransactionManager tmanager;
    protected static PermissionListManager pmanager;
    protected static LeaseManager leasemanager;
    public static WorldItemManager imanager;
    public static WorldFlagManager wmanager;
    protected static RentManager rentmanager;
    protected static ChatManager chatmanager;
    protected static Server server;
    public static HelpEntry helppages;
    protected static LocaleManager LocaleManager;
    protected static Language NewLanguageManager;
    protected static PlayerManager PlayerManager;
    protected static FlagUtil FlagUtilManager;
    protected static ShopSignUtil ShopSignUtilManager;
    protected static RandomTp RandomTpManager;
    protected static DynMapManager DynManager;
    protected static Sorting SortingManager;
    protected static ActionBar ABManager;
    protected static AutoSelection AutoSelectionManager;
    protected static SchematicsManager SchematicManager;

    protected static CommandFiller cmdFiller;

    protected static ZipLibrary zip;

    protected boolean firstenable = true;
    protected static EconomyInterface economy;
    private static int saveVersion = 1;
    public static File dataFolder;
    protected static int leaseBukkitId = -1;
    protected static int rentBukkitId = -1;
    protected static int healBukkitId = -1;
    protected static int feedBukkitId = -1;

    protected static int DespawnMobsBukkitId = -1;

    protected static int autosaveBukkitId = -1;
    protected static VersionChecker versionChecker;
    protected static boolean initsuccess = false;
    public static Map<String, String> deleteConfirm;
    public static Map<String, String> UnrentConfirm = new HashMap<String, String>();
    public static List<String> resadminToggle;
    private final static String[] validLanguages = { "English", "Czech", "Chinese", "ChineseTW" };
    public static ConcurrentHashMap<String, OfflinePlayer> OfflinePlayerList = new ConcurrentHashMap<String, OfflinePlayer>();
    public static WorldEditPlugin wep = null;
    public static WorldGuardPlugin wg = null;
    public static int wepid;

    private static String ServerLandname = "Server_Land";
    private static String ServerLandUUID = "00000000-0000-0000-0000-000000000000";
    private static String TempUserUUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private static ABInterface ab;
    private static NMS nms;
    static LWC lwc;

    public static HashMap<String, Long> rtMap = new HashMap<String, Long>();
    public static List<String> teleportDelayMap = new ArrayList<String>();
    public static HashMap<String, ClaimedResidence> teleportMap = new HashMap<String, ClaimedResidence>();

    public static String prefix = ChatColor.GREEN + "[" + ChatColor.GOLD + "Residence" + ChatColor.GREEN + "]" + ChatColor.GRAY;

    public static boolean isSpigot() {
	return spigotPlatform;
    }

    public static HashMap<String, ClaimedResidence> getTeleportMap() {
	return teleportMap;
    }

    public static List<String> getTeleportDelayMap() {
	return teleportDelayMap;
    }

    public static HashMap<String, Long> getRandomTeleportMap() {
	return rtMap;
    }

    // API
    private static ResidenceApi API = new ResidenceApi();
    private static MarketBuyInterface MarketBuyAPI = null;
    private static MarketRentInterface MarketRentAPI = null;
    private static ResidencePlayerInterface PlayerAPI = null;
    private static ResidenceInterface ResidenceAPI = null;
    private static ChatInterface ChatAPI = null;

    public static ResidencePlayerInterface getPlayerManagerAPI() {
	if (PlayerAPI == null)
	    PlayerAPI = PlayerManager;
	return PlayerAPI;
    }

    public static ResidenceInterface getResidenceManagerAPI() {
	if (ResidenceAPI == null)
	    ResidenceAPI = rmanager;
	return ResidenceAPI;
    }

    public static MarketRentInterface getMarketRentManagerAPI() {
	if (MarketRentAPI == null)
	    MarketRentAPI = rentmanager;
	return MarketRentAPI;
    }

    public static MarketBuyInterface getMarketBuyManagerAPI() {
	if (MarketBuyAPI == null)
	    MarketBuyAPI = tmanager;
	return MarketBuyAPI;

    }

    public static ChatInterface getResidenceChatAPI() {
	if (ChatAPI == null)
	    ChatAPI = chatmanager;
	return ChatAPI;
    }

    public static ResidenceApi getAPI() {
	return API;
    }
    // API end

    public static NMS getNms() {
	return nms;
    }

    public static ABInterface getAB() {
	return ab;
    }

    private Runnable doHeals = new Runnable() {
	@Override
	public void run() {
	    plistener.doHeals();
	}
    };

    private Runnable doFeed = new Runnable() {
	@Override
	public void run() {
	    plistener.feed();
	}
    };

    private Runnable DespawnMobs = new Runnable() {
	@Override
	public void run() {
	    plistener.DespawnMobs();
	}
    };

    private Runnable rentExpire = new Runnable() {
	@Override
	public void run() {
	    rentmanager.checkCurrentRents();
	    if (cmanager.showIntervalMessages()) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " - Rent Expirations checked!");
	    }
	}
    };
    private Runnable leaseExpire = new Runnable() {
	@Override
	public void run() {
	    leasemanager.doExpirations();
	    if (cmanager.showIntervalMessages()) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " - Lease Expirations checked!");
	    }
	}
    };
    private Runnable autoSave = new Runnable() {
	@Override
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
		Logger.getLogger("Minecraft").log(Level.SEVERE, Residence.prefix + " SEVERE SAVE ERROR", ex);
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

	if (getDynManager() != null)
	    getDynManager().getMarkerSet().deleteMarkerSet();

	if (initsuccess) {
	    try {
		saveYml();
		if (zip != null)
		    zip.backup();
	    } catch (Exception ex) {
		Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
	    }
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Disabled!");
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

	    ResidenceVersion = this.getDescription().getVersion();
	    authlist = this.getDescription().getAuthors();

	    cmdFiller = new CommandFiller();
	    cmdFiller.fillCommands();

	    SortingManager = new Sorting();

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

	    cmanager = new ConfigManager(this);
	    String multiworld = cmanager.getMultiworldPlugin();
	    if (multiworld != null) {
		Plugin plugin = server.getPluginManager().getPlugin(multiworld);
		if (plugin != null) {
		    if (!plugin.isEnabled()) {
			Bukkit.getConsoleSender().sendMessage(Residence.prefix + " - Enabling multiworld plugin: " + multiworld);
			server.getPluginManager().enablePlugin(plugin);
		    }
		}
	    }
	    FlagUtilManager = new FlagUtil(this);
	    getFlagUtilManager().load();

	    try {
		Class<?> c = Class.forName("org.bukkit.entity.Player");
		for (Method one : c.getDeclaredMethods()) {
		    if (one.getName().equalsIgnoreCase("Spigot"))
			spigotPlatform = true;
		}
	    } catch (Exception e) {
	    }

	    String packageName = getServer().getClass().getPackage().getName();
	    String[] packageSplit = packageName.split("\\.");
	    String version = packageSplit[packageSplit.length - 1].substring(0, packageSplit[packageSplit.length - 1].length() - 3);
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
		    Bukkit.shutdown();
		}
	    } catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | InstantiationException
		| ClassNotFoundException e) {
		System.out.println("Your server version is not compatible with this plugins version! Plugin will be disabled: " + version + " and server will shutdown");
		this.setEnabled(false);
		Bukkit.shutdown();
		return;
	    }

	    ABManager = new ActionBar();
	    version = packageSplit[packageSplit.length - 1];
	    try {
		Class<?> nmsClass;

		nmsClass = Class.forName("com.bekvon.bukkit.residence.actionBarNMS." + version);

		if (ABInterface.class.isAssignableFrom(nmsClass)) {
		    ab = (ABInterface) nmsClass.getConstructor().newInstance();
		} else {
		    System.out.println("Something went wrong, please note down version and contact author v:" + version);
		    this.setEnabled(false);
		    Bukkit.shutdown();
		}
	    } catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException | InstantiationException
		| ClassNotFoundException e) {
		ab = ABManager;
		return;
	    }

	    gmanager = new PermissionManager();
	    imanager = new WorldItemManager();
	    wmanager = new WorldFlagManager();

	    chatmanager = new ChatManager();
	    rentmanager = new RentManager();

	    LocaleManager = new LocaleManager(this);

	    PlayerManager = new PlayerManager();
	    ShopSignUtilManager = new ShopSignUtil(this);
	    RandomTpManager = new RandomTp(this);

	    zip = new ZipLibrary();

	    versionChecker = new VersionChecker(this);

	    Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
	    if (lwcp != null)
		lwc = ((LWCPlugin) lwcp).getLWC();

	    for (String lang : validLanguages) {
		YmlMaker langFile = new YmlMaker(this, "Language" + File.separator + lang + ".yml");
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
		} else {
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Language file does not exist...");
		}
		if (in != null)
		    in.close();
	    } catch (Exception ex) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Failed to load language file: " + cmanager.getLanguage()
		    + ".yml setting to default - English");

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
		} else {
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Language file does not exist...");
		}
		if (in != null)
		    in.close();
	    }
	    economy = null;
	    if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Scanning for economy systems...");
		if (gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
		    ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
		    if (vault.economyOK()) {
			economy = vault;
			Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Found Vault using economy system: " + vault.getEconomyName());
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
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Unable to find an economy system...");
		}
	    }

	    // Only fill if we need to convert player data
	    if (getConfigManager().isUUIDConvertion()) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Loading (" + Bukkit.getOfflinePlayers().length + ") player data");
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
		    if (player == null)
			continue;
		    String name = player.getName();
		    if (name == null)
			continue;
		    getOfflinePlayerMap().put(name.toLowerCase(), player);
		}
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Player data loaded: " + getOfflinePlayerMap().size());
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
		tmanager = new TransactionManager();
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

	    signmanager = new SignUtil(this);
	    Residence.getSignUtil().LoadSigns();

	    if (Residence.getConfigManager().isUseResidenceFileClean())
		FileCleanUp.cleanFiles();

	    if (firstenable) {
		if (!this.isEnabled()) {
		    return;
		}
		FlagPermissions.initValidFlags();

		setWorldEdit();
		setWorldGuard();

		blistener = new ResidenceBlockListener(this);
		plistener = new ResidencePlayerListener(this);
		elistener = new ResidenceEntityListener(this);
		flistener = new ResidenceFixesListener();

		shlistener = new ShopListener();
		spigotlistener = new SpigotListener();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(blistener, this);
		pm.registerEvents(plistener, this);
		pm.registerEvents(elistener, this);
		pm.registerEvents(flistener, this);
		pm.registerEvents(shlistener, this);

		// 1.8 event
		if (getVersionChecker().GetVersion() >= 1800)
		    pm.registerEvents(new v1_8Events(), this);

		// 1.9 event
		if (getVersionChecker().GetVersion() >= 1900)
		    pm.registerEvents(new v1_9Events(), this);

		// 1.10 event
		if (getVersionChecker().GetVersion() >= 11000)
		    pm.registerEvents(new v1_10Events(), this);

		// pm.registerEvent(Event.Type.WORLD_LOAD, wlistener,
		// Priority.NORMAL, this);
		if (cmanager.enableSpout()) {
		    slistener = new ResidenceSpoutListener();
		    pm.registerEvents(slistener, this);
		    spout = new ResidenceSpout(this);
		}
		firstenable = false;
	    } else {
		plistener.reload();
	    }

	    NewLanguageManager = new Language(this);
	    getLM().LanguageReload();

	    AutoSelectionManager = new AutoSelection();

	    if (wep != null)
		SchematicManager = new SchematicsManager();

	    try {
		Class.forName("org.bukkit.event.player.PlayerItemDamageEvent");
		getServer().getPluginManager().registerEvents(spigotlistener, this);
	    } catch (Exception e) {
	    }

	    if (getServer().getPluginManager().getPlugin("CrackShot") != null)
		getServer().getPluginManager().registerEvents(new CrackShot(), this);

	    // DynMap
	    Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
	    if (dynmap != null && getConfigManager().DynMapUse) {
		DynManager = new DynMapManager(this);
		getServer().getPluginManager().registerEvents(new DynMapListeners(), this);
		getDynManager().api = (DynmapAPI) dynmap;
		getDynManager().activate();
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
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Enabled! Version " + this.getDescription().getVersion() + " by Zrips");
	    initsuccess = true;

	    PlayerManager.fillList();

	} catch (Exception ex) {
	    initsuccess = false;
	    getServer().getPluginManager().disablePlugin(this);
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " - FAILED INITIALIZATION! DISABLED! ERROR:");
	    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
	    Bukkit.getServer().shutdown();
	}

	getShopSignUtilManager().LoadShopVotes();
	getShopSignUtilManager().LoadSigns();
	getShopSignUtilManager().BoardUpdate();
	getVersionChecker().VersionCheck(null);

    }

    public static SignUtil getSignUtil() {
	return signmanager;
    }

    public void consoleMessage(String message) {
	Bukkit.getConsoleSender().sendMessage(Residence.prefix + " " + message);
    }

    public static boolean validName(String name) {
	if (name.contains(":") || name.contains(".") || name.contains("|")) {
	    return false;
	}
	if (cmanager.getResidenceNameRegex() == null) {
	    return true;
	}
	String namecheck = name.replaceAll(cmanager.getResidenceNameRegex(), "");
	if (!name.equals(namecheck)) {
	    return false;
	}
	return true;
    }

    private void setWorldEdit() {
	Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
	if (plugin != null) {
	    smanager = new WorldEditSelectionManager(server, this);
	    wep = (WorldEditPlugin) plugin;
	    wepid = Residence.wep.getConfig().getInt("wand-item");
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Found WorldEdit");
	} else {
	    smanager = new SelectionManager(server, this);
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " WorldEdit NOT found!");
	}
    }

    private static void setWorldGuard() {
	Plugin wgplugin = server.getPluginManager().getPlugin("WorldGuard");
	if (wgplugin != null) {
	    try {
		Class.forName("com.sk89q.worldedit.BlockVector");
		Class.forName("com.sk89q.worldguard.bukkit.RegionContainer");
		Class.forName("com.sk89q.worldguard.protection.ApplicableRegionSet");
		Class.forName("com.sk89q.worldguard.protection.managers.RegionManager");
		Class.forName("com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion");
		Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
	    } catch (Exception e) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + ChatColor.RED
		    + " Found WorldGuard, but its not supported by Residence plugin. Please update WorldGuard to latest version");
		return;
	    }
	    wg = (WorldGuardPlugin) wgplugin;
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Found WorldGuard");
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

    public static ResidenceSpout getSpout() {
	return spout;
    }

    public static ResidenceSpoutListener getSpoutListener() {
	return slistener;
    }

    public static CommandFiller getCommandFiller() {
	if (cmdFiller == null) {
	    cmdFiller = new CommandFiller();
	    cmdFiller.fillCommands();
	}
	return cmdFiller;
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

    public static PermissionListManager getPermissionListManager() {
	return pmanager;
    }

    public static DynMapManager getDynManager() {
	return DynManager;
    }

    public static SchematicsManager getSchematicManager() {
	return SchematicManager;
    }

    public static AutoSelection getAutoSelectionManager() {
	return AutoSelectionManager;
    }

    public static Sorting getSortingManager() {
	return SortingManager;
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

    public static void setConfigManager(ConfigManager cm) {
	cmanager = cm;
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

    public static LocaleManager getLocaleManager() {
	return LocaleManager;
    }

    public static Language getLM() {
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

    public static WorldEditPlugin getWEplugin() {
	return wep;
    }

    public static String getResidenceVersion() {
	return ResidenceVersion;
    }

    public static List<String> getAuthors() {
	return authlist;
    }

    public static FlagPermissions getPermsByLoc(Location loc) {
	ClaimedResidence res = rmanager.getByLoc(loc);
	if (res != null) {
	    return res.getPermissions();
	}
	return wmanager.getPerms(loc.getWorld().getName());

    }

    public static FlagPermissions getPermsByLocForPlayer(Location loc, Player player) {
	ClaimedResidence res = rmanager.getByLoc(loc);
	if (res != null) {
	    return res.getPermissions();
	}
	if (player != null)
	    return wmanager.getPerms(player);

	return wmanager.getPerms(loc.getWorld().getName());
    }

    private void loadIConomy() {
	Plugin p = getServer().getPluginManager().getPlugin("iConomy");
	if (p != null) {
	    if (p.getDescription().getVersion().startsWith("6")) {
		economy = new IConomy6Adapter((com.iCo6.iConomy) p);
	    } else if (p.getDescription().getVersion().startsWith("5")) {
		economy = new IConomy5Adapter();
	    } else {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " UNKNOWN iConomy version!");
		return;
	    }
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Successfully linked with iConomy! Version: " + p.getDescription().getVersion());
	} else {
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " iConomy NOT found!");
	}
    }

    private void loadBOSEconomy() {
	Plugin p = getServer().getPluginManager().getPlugin("BOSEconomy");
	if (p != null) {
	    economy = new BOSEAdapter((BOSEconomy) p);
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Successfully linked with BOSEconomy!");
	} else {
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " BOSEconomy NOT found!");
	}
    }

    private void loadEssentialsEconomy() {
	Plugin p = getServer().getPluginManager().getPlugin("Essentials");
	if (p != null) {
	    economy = new EssentialsEcoAdapter((Essentials) p);
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Successfully linked with Essentials Economy!");
	} else {
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Essentials Economy NOT found!");
	}
    }

    private void loadRealEconomy() {
	Plugin p = getServer().getPluginManager().getPlugin("RealPlugin");
	if (p != null) {
	    economy = new RealShopEconomy(new RealEconomy((RealPlugin) p));
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Successfully linked with RealShop Economy!");
	} else {
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " RealShop Economy NOT found!");
	}
    }

    private void loadVaultEconomy() {
	Plugin p = getServer().getPluginManager().getPlugin("Vault");
	if (p != null) {
	    ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
	    if (vault.economyOK()) {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Found Vault using economy: " + vault.getEconomyName());
		economy = vault;
	    } else {
		Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Found Vault, but Vault reported no usable economy system...");
	    }
	} else {
	    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Vault NOT found!");
	}
    }

    public static boolean isResAdminOn(CommandSender sender) {
	if (sender instanceof Player)
	    return isResAdminOn((Player) sender);
	return true;
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

    private static void saveYml() throws IOException {
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
	    yml.getRoot().put("Residences", entry.getValue());
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
		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Loading save data for world " + world.getName() + "...");
		    yml = new YMLSaveHelper(loadFile);
		    yml.load();

		    worlds.put(world.getName(), yml.getRoot().get("Residences"));

		    int pass = (int) (System.currentTimeMillis() - time);
		    String PastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

		    Bukkit.getConsoleSender().sendMessage(Residence.prefix + " Loaded " + world.getName() + " data. (" + PastTime + ")");
		}
	    }

	    rmanager = getResidenceManager().load(worlds);

	    // Getting shop residences
	    Map<String, ClaimedResidence> resList = rmanager.getResidences();
	    for (Entry<String, ClaimedResidence> one : resList.entrySet()) {

		ResidencePermissions perms = one.getValue().getPermissions();
		if (!perms.has(Flags.shop, false))
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
		tmanager = new TransactionManager();
		tmanager.load((Map) yml.getRoot().get("Economy"));
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
//		rentmanager = new RentManager();
		rentmanager.load((Map) yml.getRoot().get("RentSystem"));
	    }

	    for (Player one : Bukkit.getOnlinePlayers()) {
		ResidencePlayer rplayer = Residence.getPlayerManager().getResidencePlayer(one);
		if (rplayer != null)
		    rplayer.recountRes();
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

    private static void remove(File newGroups, List<String> list) throws IOException {

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

    private static void copy(File source, File target) throws IOException {
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

    public static boolean isPlayerExist(CommandSender sender, String name, boolean inform) {
	if (Residence.getPlayerUUID(name) != null)
	    return true;
	if (inform)
	    sender.sendMessage(msg(lm.Invalid_Player));
	@SuppressWarnings("unused")
	String a = "%%__USER__%%";
	@SuppressWarnings("unused")
	String b = "%%__RESOURCE__%%";
	@SuppressWarnings("unused")
	String c = "%%__NONCE__%%";
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
	if (getOfflinePlayerMap().containsKey(Name.toLowerCase())) {
	    return getOfflinePlayerMap().get(Name.toLowerCase());
	}
	Player player = Bukkit.getPlayer(Name);
	OfflinePlayer offPlayer = null;
	if (player != null)
	    offPlayer = player;
	if (offPlayer == null)
	    offPlayer = Bukkit.getOfflinePlayer(Name);
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
	return null;
    }

    public static boolean isDisabledWorldListener(World world) {
	return isDisabledWorldListener(world.getName());
    }

    public static boolean isDisabledWorldListener(String worldname) {
	if (getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableListeners)
	    return true;
	return false;
    }

    public static boolean isDisabledWorldCommand(World world) {
	return isDisabledWorldCommand(world.getName());
    }

    public static boolean isDisabledWorldCommand(String worldname) {
	if (getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableCommands)
	    return true;
	return false;
    }

//    public static void msg(Player player, String path, Object... variables) {
//	if (player != null)
//	    if (Residence.getLM().containsKey(path))
//		player.sendMessage(Residence.getLM().getMessage(path, variables));
//	    else
//		player.sendMessage(path);
//    }

    public static String msg(String path) {
	return Residence.getLM().getMessage(path);
    }

    public static void msg(CommandSender sender, String text) {
	if (sender != null && text.length() > 0)
	    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static void msg(Player player, String text) {
	if (player != null && text.length() > 0)
	    player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static void msg(CommandSender sender, lm lm, Object... variables) {
	if (sender != null)
	    if (Residence.getLM().containsKey(lm.getPath())) {
		String msg = Residence.getLM().getMessage(lm, variables);
		if (msg.length() > 0)
		    sender.sendMessage(msg);
	    } else {
		String msg = lm.getPath();
		if (msg.length() > 0)
		    sender.sendMessage(lm.getPath());
	    }
    }

    public static List<String> msgL(lm lm) {
	return Residence.getLM().getMessageList(lm);
    }

    public static String msg(lm lm, Object... variables) {
	return Residence.getLM().getMessage(lm, variables);
    }

}
