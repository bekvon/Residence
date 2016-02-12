package com.bekvon.bukkit.residence;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.bekvon.bukkit.residence.containers.GuiItems;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.ParticleEffects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigManager {
    protected String defaultGroup;
    protected boolean useLeases;
    protected boolean ResMoneyBack;
    protected boolean enableEconomy;
    protected boolean adminsOnly;
    protected boolean allowEmptyResidences;
    protected boolean NoLava;
    protected boolean NoWater;
    protected boolean NoLavaPlace;
    protected boolean useBlockFall;
    protected boolean NoWaterPlace;
    protected boolean AutoCleanUp;
    protected boolean UseClean;
    protected boolean PvPFlagPrevent;
    protected boolean OverridePvp;
    protected boolean ResCreateCaseSensitive;
    protected boolean ResTpCaseSensitive;
    protected boolean BlockAnyTeleportation;
    protected int infoToolId;
    protected int AutoCleanUpDays;
    protected int selectionToolId;
    protected boolean adminOps;
    protected boolean AdminFullAccess;
    protected String multiworldPlugin;
    protected boolean enableRentSystem;
    protected boolean leaseAutoRenew;
    protected boolean ShortInfoUse;
    protected boolean OnlyLike;
    protected int rentCheckInterval;
    protected int chatPrefixLength;
    protected int leaseCheckInterval;
    protected int autoSaveInt;
    protected int FlowLevel;
    protected int PlaceLevel;
    protected int BlockFallLevel;
    protected int CleanLevel;
    protected int NewPlayerRangeX;
    protected int NewPlayerRangeY;
    protected int NewPlayerRangeZ;
    protected int VisualizerRange;
    protected int VisualizerShowFor;
    protected int VisualizerUpdateInterval;
    protected int MinimalResSize;
    protected int MinimalResX;
    protected int MinimalResY;
    protected int MinimalResZ;
    protected int TeleportDelay;
    protected int VisualizerRowSpacing;
    protected int VisualizerCollumnSpacing;
    protected boolean flagsInherit;
    protected ChatColor chatColor;
    protected boolean chatEnable;
    protected boolean actionBar;
    protected boolean ActionBarOnSelection;
    protected boolean visualizer;
    protected int minMoveUpdate;
    protected int MaxResCount;
    protected int MaxRentCount;
    protected int MaxSubzonesCount;
    protected int VoteRangeFrom;
    protected int HealInterval;
    protected int FeedInterval;
    protected int VoteRangeTo;
    protected FlagPermissions globalCreatorDefaults;
    protected FlagPermissions globalResidenceDefaults;
    protected Map<String, FlagPermissions> globalGroupDefaults;
    protected String language;
    protected String DefaultWorld;
    protected String DateFormat;
    protected String TimeZone;
    protected boolean preventBuildInRent;
    protected boolean PreventSubZoneRemoval;
    protected boolean stopOnSaveError;
    protected boolean legacyperms;
    protected String namefix;
    protected boolean showIntervalMessages;
    protected boolean ShowNoobMessage;
    protected boolean NewPlayerUse;
    protected boolean NewPlayerFree;
    protected boolean spoutEnable;
    protected boolean AutoMobRemoval;
    protected boolean BounceAnimation;
    protected boolean useFlagGUI;
    protected int AutoMobRemovalInterval;
    protected boolean enableLeaseMoneyAccount;
    protected boolean CouldronCompatability;
    protected boolean enableDebug = false;
    protected boolean versionCheck = true;
    protected boolean UUIDConvertion = true;
    protected boolean OfflineMode = false;
    protected boolean SelectionIgnoreY = false;
    protected boolean NoCostForYBlocks = false;
    protected boolean useVisualizer;
    protected List<Integer> customContainers;
    protected List<Integer> customBothClick;
    protected List<Integer> customRightClick;
    protected List<Integer> CleanBlocks;
    protected List<String> NoFlowWorlds;
    protected List<String> AutoCleanUpWorlds;
    protected List<String> NoPlaceWorlds;
    protected List<String> BlockFallWorlds;
    protected List<String> CleanWorlds;
    protected List<String> FlagsList;
    protected List<String> NegativePotionEffects;

    protected Location KickLocation;

    protected List<RandomTeleport> RTeleport = new ArrayList<RandomTeleport>();

    protected int rtCooldown;
    protected int rtMaxTries;

    protected ItemStack GuiTrue;
    protected ItemStack GuiFalse;
    protected ItemStack GuiRemove;

    private boolean enforceAreaInsideArea;

    protected ParticleEffects SelectedFrame;
    protected ParticleEffects SelectedSides;

    protected ParticleEffects OverlapFrame;
    protected ParticleEffects OverlapSides;

    private Residence plugin;

    public ConfigManager(FileConfiguration config, FileConfiguration flags, FileConfiguration groups, Residence plugin) {
	this.plugin = plugin;
	globalCreatorDefaults = new FlagPermissions();
	globalResidenceDefaults = new FlagPermissions();
	globalGroupDefaults = new HashMap<String, FlagPermissions>();
	UpdateConfigFile();
	this.load(flags, groups);
    }

    private synchronized static void copySetting(Configuration reader, Configuration writer, String path) {
	writer.set(path, reader.get(path));
    }

    public static String Colors(String text) {
	return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void ChangeConfig(String path, Boolean stage) {
	File f = new File(plugin.getDataFolder(), "config.yml");

	BufferedReader in = null;
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
	} catch (UnsupportedEncodingException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}

	if (in == null)
	    return;

	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

	if (!conf.isBoolean(path))
	    return;

	conf.set(path, stage);

	try {
	    conf.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Residence.getConfigManager().UpdateConfigFile();
    }

    public static List<Integer> GetConfig(String path, List<Integer> list, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, list);
	copySetting(conf, writer, path);
	return conf.getIntegerList(path);
    }

    public static Boolean GetConfig(String path, Boolean text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getBoolean(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static int GetConfig(String path, int text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getInt(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static Double GetConfig(String path, Double text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getDouble(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static String GetConfig(String path, String text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = conf.getString(path);
	if (colorize)
	    text = Colors(text);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> GetConfig(String path, List<String> text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = ColorsArray(conf.getStringList(path), colorize);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<Integer> GetConfigInt(String path, List<Integer> text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getIntegerList(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> ColorsArray(List<String> text, Boolean colorize) {
	List<String> temp = new ArrayList<String>();
	for (String part : text) {
	    if (colorize)
		part = Colors(part);
	    temp.add(Colors(part));
	}
	return temp;
    }

    void UpdateFlagFile() {

	File f = new File(plugin.getDataFolder(), "flags.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

	Set<String> sections = conf.getConfigurationSection("Global.FlagPermission").getKeys(false);
	for (String one : Locale.FlagList) {
	    if (sections.contains(one.toLowerCase()))
		continue;
	    conf.createSection("Global.FlagPermission." + one.toLowerCase());
	    conf.set("Global.FlagPermission." + one.toLowerCase(), false);
	}

	if (!conf.isConfigurationSection("Global.FlagGui"))
	    conf.createSection("Global.FlagGui");

	ConfigurationSection guiSection = conf.getConfigurationSection("Global.FlagGui");
	Set<String> flagGui = guiSection.getKeys(false);
	for (String one : Locale.FlagList) {
	    if (flagGui.contains(one.toLowerCase()))
		continue;

	    String lowOne = one.toLowerCase();
	    GuiItems uno = null;
	    try {
		uno = GuiItems.valueOf(lowOne);
	    } catch (IllegalArgumentException e) {
		continue;
	    }
	    if (uno == null)
		continue;

	    guiSection.createSection(lowOne);
	    guiSection.set(lowOne + ".Id", uno.getId());
	    guiSection.set(lowOne + ".Data", uno.getData());
	}

	try {
	    conf.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void UpdateGroupedFlagsFile() {

	File f = new File(plugin.getDataFolder(), "flags.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

	if (!conf.isConfigurationSection("Global.GroupedFlags")) {
	    conf.createSection("Global.GroupedFlags");
	    conf.set("Global.GroupedFlags.redstone", Arrays.asList("note", "pressure", "lever", "button", "diode"));
	    conf.set("Global.GroupedFlags.craft", Arrays.asList("brew", "table", "enchant"));
	    conf.set("Global.GroupedFlags.trusted", Arrays.asList("use", "tp", "build", "container", "bucket", "move", "leash", "animalkilling", "mobkilling", "shear",
		"chat"));
	    conf.set("Global.GroupedFlags.fire", Arrays.asList("ignite", "firespread"));

	    try {
		conf.save(f);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	for (String oneGroup : conf.getConfigurationSection("Global.GroupedFlags").getKeys(false)) {
	    for (String OneFlag : conf.getStringList("Global.GroupedFlags." + oneGroup)) {
		FlagPermissions.addFlagToFlagGroup(oneGroup, OneFlag);
	    }
	}
    }

    @SuppressWarnings("deprecation")
	void UpdateConfigFile() {

	File f = new File(plugin.getDataFolder(), "config.yml");

	BufferedReader in = null;
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));
	} catch (UnsupportedEncodingException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}

	if (in == null)
	    return;

	YamlConfiguration conf = YamlConfiguration.loadConfiguration(in);
	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	conf.options().copyDefaults(true);

	String defaultWorldName = Bukkit.getServer().getWorlds().size() > 0 ? Bukkit.getServer().getWorlds().get(0).getName() : "World";

	writer.addComment("Global", "These are Global Settings for Residence.");

	writer.addComment("Global.UUIDConvertion", "Starts UUID conversion on plugin startup", "DONT change this if you are not sure what you doing");
	UUIDConvertion = GetConfig("Global.UUIDConvertion", true, writer, conf);

	writer.addComment("Global.OfflineMode",
	    "If you running offline server, better to check this as true. This will help to solve issues with changing players UUID.");
	OfflineMode = GetConfig("Global.OfflineMode", false, writer, conf);

	writer.addComment("Global.versionCheck", "Players with residence.versioncheck permission node will be noticed about new residence version on login");
	versionCheck = GetConfig("Global.versionCheck", true, writer, conf);

	writer.addComment("Global.Language", "This loads the <language>.yml file in the Residence Language folder",
	    "All Residence text comes from this file. (NOT DONE YET)");
	language = GetConfig("Global.Language", "English", writer, conf, false);

	writer.addComment("Global.SelectionToolId", "Wooden Hoe is the default selection tool for Residence.",
	    "You can change it to another item ID listed here: http://www.minecraftwiki.net/wiki/Data_values");
	selectionToolId = GetConfig("Global.SelectionToolId", Material.WOOD_AXE.getId(), writer, conf);

	writer.addComment("Global.Selection.IgnoreY", "By setting this to true, all selections will be made from bedrock to sky ignoring Y coordinates");
	SelectionIgnoreY = GetConfig("Global.Selection.IgnoreY", false, writer, conf);
	writer.addComment("Global.Selection.NoCostForYBlocks", "By setting this to true, player will only pay for x*z blocks ignoring height",
	    "This will lower residence price by up to 256 times, so ajust block price BEFORE enabling this");
	NoCostForYBlocks = GetConfig("Global.Selection.NoCostForYBlocks", false, writer, conf);

	writer.addComment("Global.InfoToolId", "This determins which tool you can use to see info on residences, default is String.",
	    "Simply equip this tool and hit a location inside the residence and it will display the info for it.");
	infoToolId = GetConfig("Global.InfoToolId", Material.STRING.getId(), writer, conf);

	writer.addComment("Global.Optimizations.DefaultWorld", "Name of your mein residence world. Usualy normal starting world 'World'. Capitalization essential");
	DefaultWorld = GetConfig("Global.Optimizations.DefaultWorld", defaultWorldName, writer, conf, false);

	writer.addComment("Global.Optimizations.ResCreateCaseSensitive",
	    "When its true you can create residences with similar names but different capitalization. An example: Village and village are counted as different residences",
	    "When it's set to false you can't create residences with same names but different capitalizations");
	ResCreateCaseSensitive = GetConfig("Global.Optimizations.ResCreateCaseSensitive", false, writer, conf);

	writer.addComment("Global.Optimizations.ResTpCaseSensitive",
	    "When this set to true, when you are performing /res tp command and providing residence name, it should be exactly same as residence name. So in example Village is not same as village",
	    "When it's set to false you can teleport to residence with name Village even if you executing command /res tp village",
	    "Don't disable this if you already have some duplicating residences in your database as this will prevent players from teleporting to one of them");
	ResTpCaseSensitive = GetConfig("Global.Optimizations.ResTpCaseSensitive", true, writer, conf);

	writer.addComment("Global.Optimizations.BlockAnyTeleportation",
	    "When this set to true, any teleportation to residence where player dont have tp flag, action will be denyied",
	    "This can prevent from teleporting players to residence with 3rd party plugins like esentials /tpa");
	BlockAnyTeleportation = GetConfig("Global.Optimizations.BlockAnyTeleportation", true, writer, conf);

	writer.addComment("Global.Optimizations.MaxResCount", "Set this as low as posible depending of residence.max.res.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxResCount = GetConfig("Global.Optimizations.MaxResCount", 30, writer, conf);
	writer.addComment("Global.Optimizations.MaxRentCount", "Set this as low as posible depending of residence.max.rents.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxRentCount = GetConfig("Global.Optimizations.MaxRentCount", 10, writer, conf);
	writer.addComment("Global.Optimizations.MaxSubzoneCount", "Set this as low as posible depending of residence.max.subzones.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxSubzonesCount = GetConfig("Global.Optimizations.MaxSubzoneCount", 5, writer, conf);
	writer.addComment("Global.Optimizations.OverridePvp", "By setting this to true, regular pvp flag will be acting as overridepvp flag",
	    "Overridepvp flag tries to ignore any pvp protection in that residence by any other plugin");
	OverridePvp = GetConfig("Global.Optimizations.OverridePvp", false, writer, conf);

	// residence kick location
	writer.addComment("Global.Optimizations.KickLocation.Use",
	    "By setting this to true, when player kicks another player from residence, he will be teleported to this location instead of getting outside residence");
	Boolean UseKick = GetConfig("Global.Optimizations.KickLocation.Use", false, writer, conf);
	String KickLocationWorld = GetConfig("Global.Optimizations.KickLocation.World", defaultWorldName, writer, conf, false);
	Double KickLocationX = GetConfig("Global.Optimizations.KickLocation.X", 0.5, writer, conf);
	Double KickLocationY = GetConfig("Global.Optimizations.KickLocation.Y", 63.0, writer, conf);
	Double KickLocationZ = GetConfig("Global.Optimizations.KickLocation.Z", 0.5, writer, conf);
	writer.addComment("Global.Optimizations.KickLocation.Pitch", "Less than 0 - head up, more than 0 - head down. Range from -90 to 90");
	Double KickPitch = GetConfig("Global.Optimizations.KickLocation.Pitch", 0.0, writer, conf);
	writer.addComment("Global.Optimizations.KickLocation.Yaw", "Head position to left and right. Range from -180 to 180");
	Double KickYaw = GetConfig("Global.Optimizations.KickLocation.Yaw", 0.0, writer, conf);
	if (UseKick) {
	    World world = Bukkit.getWorld(KickLocationWorld);
	    if (world != null) {
		KickLocation = new Location(world, KickLocationX, KickLocationY, KickLocationZ);
		KickLocation.setPitch(KickPitch.floatValue());
		KickLocation.setYaw(KickYaw.floatValue());
	    }
	}

	writer.addComment("Global.Optimizations.ShortInfo.Use",
	    "By setting this to true, when checking residence info with /res info, you will get only names in list, by hovering on them, you will get flag list");
	ShortInfoUse = GetConfig("Global.Optimizations.ShortInfo.Use", false, writer, conf);

	// Vote range
	writer.addComment("Global.Optimizations.Vote.RangeFrom", "Range players can vote to, by default its from 0 to 10 points");
	VoteRangeFrom = GetConfig("Global.Optimizations.Vote.RangeFrom", 0, writer, conf);
	VoteRangeTo = GetConfig("Global.Optimizations.Vote.RangeTo", 10, writer, conf);

	writer.addComment("Global.Optimizations.Vote.OnlyLike", "If this true, players can onli give like for shop instead of point voting");
	OnlyLike = GetConfig("Global.Optimizations.Vote.OnlyLike", false, writer, conf);

	// Healing/Feed interval
	writer.addComment("Global.Optimizations.Intervals.Heal", "How often in seconds to heal/feed players in residence with appropriate flag",
	    "Bigger numbers can save some resources");
	HealInterval = GetConfig("Global.Optimizations.Intervals.Heal", 1, writer, conf);
	FeedInterval = GetConfig("Global.Optimizations.Intervals.Feed", 5, writer, conf);

	// negative potion effect list
	writer.addComment("Global.Optimizations.NegativePotionEffects",
	    "Potions containing one of thos effects will be ignored if residence dont have pvp true flag set");
	NegativePotionEffects = GetConfig("Global.Optimizations.NegativePotionEffects", Arrays.asList("blindness", "confusion", "harm", "hunger", "poison", "slow",
	    "slow_digging", "weakness", "wither"), writer, conf, false);

	writer.addComment("Global.MoveCheckInterval", "The interval, in milliseconds, between movement checks.", "Reducing this will increase the load on the server.",
	    "Increasing this will allow players to move further in movement restricted zones before they are teleported out.");
	minMoveUpdate = GetConfig("Global.MoveCheckInterval", 500, writer, conf);

	writer.addComment("Global.Tp.TeleportDelay", "The interval, in seconds, for teleportation.", "Use 0 to disable");
	TeleportDelay = GetConfig("Global.Tp.TeleportDelay", 3, writer, conf);

	if (conf.contains("Global.RandomTeleportation.WorldName")) {

	    String path = "Global.RandomTeleportation.";
	    String WorldName = conf.getString(path + "WorldName", defaultWorldName);

	    int MaxCoord = conf.getInt(path + "MaxCoord", 1000);
	    int MinCord = conf.getInt(path + "MinCord", 500);
	    int CenterX = conf.getInt(path + "CenterX", 0);
	    int CenterZ = conf.getInt(path + "CenterZ", 0);

	    RTeleport.add(new RandomTeleport(WorldName, MaxCoord, MinCord, CenterX, CenterZ));

	    GetConfig("Global.RandomTeleportation." + WorldName + ".MaxCord", MaxCoord, writer, conf);
	    GetConfig("Global.RandomTeleportation." + WorldName + ".MinCord", MinCord, writer, conf);
	    GetConfig("Global.RandomTeleportation." + WorldName + ".CenterX", CenterX, writer, conf);
	    GetConfig("Global.RandomTeleportation." + WorldName + ".CenterZ", CenterZ, writer, conf);
	} else {
	    if (conf.isConfigurationSection("Global.RandomTeleportation"))
		for (String one : conf.getConfigurationSection("Global.RandomTeleportation").getKeys(false)) {
		    String path = "Global.RandomTeleportation." + one + ".";

		    writer.addComment("Global.RandomTeleportation." + one, "World name to use this feature. Add annother one with appropriate name to enable random teleportation");

		    writer.addComment(path + "MaxCoord", "Max coordinate to teleport, setting to 1000, player can be teleported between -1000 and 1000 coordinates");
		    int MaxCoord = GetConfig(path + "MaxCoord", 1000, writer, conf);
		    writer.addComment(path + "MinCord",
			"If maxcord set to 1000 and mincord to 500, then player can be teleported between -1000 to -500 and 1000 to 500 coordinates");
		    int MinCord = GetConfig(path + "MinCord", 500, writer, conf);
		    int CenterX = GetConfig(path + "CenterX", 0, writer, conf);
		    int CenterZ = GetConfig(path + "CenterZ", 0, writer, conf);

		    RTeleport.add(new RandomTeleport(one, MaxCoord, MinCord, CenterX, CenterZ));
		}
	    else {
		String path = "Global.RandomTeleportation." + defaultWorldName + ".";

		writer.addComment(path + "WorldName", "World to use this function, set main residence world");
		String WorldName = GetConfig(path + "WorldName", defaultWorldName, writer, conf, false);

		writer.addComment(path + "MaxCoord", "Max coordinate to teleport, setting to 1000, player can be teleported between -1000 and 1000 coordinates");
		int MaxCoord = GetConfig(path + "MaxCoord", 1000, writer, conf);
		writer.addComment(path + "MinCord",
		    "If maxcord set to 1000 and mincord to 500, then player can be teleported between -1000 to -500 and 1000 to 500 coordinates");
		int MinCord = GetConfig(path + "MinCord", 500, writer, conf);
		int CenterX = GetConfig(path + "CenterX", 0, writer, conf);
		int CenterZ = GetConfig(path + "CenterZ", 0, writer, conf);
		RTeleport.add(new RandomTeleport(WorldName, MaxCoord, MinCord, CenterX, CenterZ));
	    }
	}

	writer.addComment("Global.RandomTeleportation.Cooldown", "How long force player to wait before using command again.");
	rtCooldown = GetConfig("Global.RandomTeleportation.Cooldown", 5, writer, conf);

	writer.addComment("Global.RandomTeleportation.MaxTries", "How many times to try find correct location for teleportation.",
	    "Keep it at low number, as player always can try again after delay");
	rtMaxTries = GetConfig("Global.RandomTeleportation.MaxTries", 20, writer, conf);

	writer.addComment("Global.Size.MinimalSize", "Minimal size of residence in blocks", "1000 is 10x10x10 residence size");
	MinimalResSize = GetConfig("Global.Size.MinimalSize", 100, writer, conf);
	MinimalResX = GetConfig("Global.Size.MinimalX", 10, writer, conf);
	MinimalResY = GetConfig("Global.Size.MinimalY", 10, writer, conf);
	MinimalResZ = GetConfig("Global.Size.MinimalZ", 10, writer, conf);

	writer.addComment("Global.SaveInterval", "The interval, in minutes, between residence saves.");
	autoSaveInt = GetConfig("Global.SaveInterval", 10, writer, conf);

	// Auto remove old residences
	writer.addComment("Global.AutoCleanUp.Use", "HIGHLY EXPERIMENTAL residence cleaning on server startup if player is offline for x days.",
	    "Players can bypass this wih residence.cleanbypass permission node");
	AutoCleanUp = GetConfig("Global.AutoCleanUp.Use", false, writer, conf);
	writer.addComment("Global.AutoCleanUp.Days", "For how long player should be offline to delete hes residence");
	AutoCleanUpDays = GetConfig("Global.AutoCleanUp.Days", 60, writer, conf);
	writer.addComment("Global.AutoCleanUp.Worlds", "Worlds to be included in check list");
	AutoCleanUpWorlds = GetConfig("Global.AutoCleanUp.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);
	
	writer.addComment("Global.LWC.Use", "HIGHLY EXPERIMENTAL residence cleaning on server startup if player is offline for x days.",
	    "Players can bypass this wih residence.cleanbypass permission node");
	AutoCleanUp = GetConfig("Global.AutoCleanUp.Use", false, writer, conf);
	writer.addComment("Global.LWC.Days", "For how long player should be offline to delete hes LWC protection on residence removal");
	AutoCleanUpDays = GetConfig("Global.AutoCleanUp.Days", 60, writer, conf);
	writer.addComment("Global.LWC.Worlds", "Worlds to be included in check list");
	AutoCleanUpWorlds = GetConfig("Global.AutoCleanUp.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);

	// Flow
	writer.addComment("Global.AntiGreef.Flow.Level", "Level from witch one to start lava and water flow blocking", "This dont have effect in residence area");
	FlowLevel = GetConfig("Global.AntiGreef.Flow.Level", 63, writer, conf);
	writer.addComment("Global.AntiGreef.Flow.NoLavaFlow", "With this set to true, lava flow outside residence is blocked");
	NoLava = GetConfig("Global.AntiGreef.Flow.NoLavaFlow", true, writer, conf);
	writer.addComment("Global.AntiGreef.Flow.NoWaterFlow", "With this set to true, water flow outside residence is blocked");
	NoWater = GetConfig("Global.AntiGreef.Flow.NoWaterFlow", true, writer, conf);
	NoFlowWorlds = GetConfig("Global.AntiGreef.Flow.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);

	// Place
	writer.addComment("Global.AntiGreef.Place.Level", "Level from witch one to start block lava and water place", "This don't have effect in residence area");
	PlaceLevel = GetConfig("Global.AntiGreef.Place.Level", 63, writer, conf);
	writer.addComment("Global.AntiGreef.Place.NoLavaPlace", "With this set to true, playrs cant place lava outside residence");
	NoLavaPlace = GetConfig("Global.AntiGreef.Place.NoLavaPlace", true, writer, conf);
	writer.addComment("Global.AntiGreef.Place.NoWaterPlace", "With this set to true, playrs cant place water outside residence");
	NoWaterPlace = GetConfig("Global.AntiGreef.Place.NoWaterPlace", true, writer, conf);
	NoPlaceWorlds = GetConfig("Global.AntiGreef.Place.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);

	// Sand fall
	writer.addComment("Global.AntiGreef.BlockFall.Use", "With this set to true, falling blocks will be deleted if they will land in different area");
	useBlockFall = GetConfig("Global.AntiGreef.BlockFall.Use", true, writer, conf);
	writer.addComment("Global.AntiGreef.BlockFall.Level", "Level from witch one to start block block's fall", "This don't have effect in residence area or outside");
	BlockFallLevel = GetConfig("Global.AntiGreef.BlockFall.Level", 62, writer, conf);
	BlockFallWorlds = GetConfig("Global.AntiGreef.BlockFall.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);

	// Res cleaning
	writer.addComment("Global.AntiGreef.ResCleaning.Use",
	    "With this set to true, after player removes its residence, all blocks listed below, will be replaced with air blocks",
	    "Effective way to prevent residence creating near greefing target and then remove it");
	UseClean = GetConfig("Global.AntiGreef.ResCleaning.Use", true, writer, conf);
	writer.addComment("Global.AntiGreef.ResCleaning.Level", "Level from whichone you want to replace blocks");
	CleanLevel = GetConfig("Global.AntiGreef.ResCleaning.Level", 63, writer, conf);
	writer.addComment("Global.AntiGreef.ResCleaning.Blocks", "Block list to be replaced", "By default only water and lava will be replaced");
	CleanBlocks = GetConfigInt("Global.AntiGreef.ResCleaning.Blocks", Arrays.asList(8, 9, 10, 11), writer, conf);
	CleanWorlds = GetConfig("Global.AntiGreef.ResCleaning.Worlds", Arrays.asList(defaultWorldName), writer, conf, false);

	writer.addComment("Global.AntiGreef.Flags.Prevent",
	    "By setting this to true flags from list will be protected from change while there is some one inside residence besides owner",
	    "Protects in example from people inviting some one and changing pvp flag to true to kill them");
	PvPFlagPrevent = GetConfig("Global.AntiGreef.Flags.Prevent", true, writer, conf);
	FlagsList = GetConfig("Global.AntiGreef.Flags.list", Arrays.asList("pvp"), writer, conf, false);

	writer.addComment("Global.DefaultGroup", "The default group to use if Permissions fails to attach or your not using Permissions.");
	defaultGroup = GetConfig("Global.DefaultGroup", "default", writer, conf, false);

	writer.addComment("Global.UseLeaseSystem", "Enable / Disable the Lease System.");
	useLeases = GetConfig("Global.UseLeaseSystem", false, writer, conf);

	writer.addComment("Global.DateFormat", "Sets date format when shown in example lease or rent expire date",
	    "How to use it properly, more information can be found at http://www.tutorialspoint.com/java/java_date_time.htm");
	DateFormat = GetConfig("Global.DateFormat", "E yyyy.MM.dd 'at' hh:mm:ss a zzz", writer, conf, false);

	writer.addComment("Global.TimeZone", "Sets time zone for showing date, usefull when server is in different country then main server player base",
	    "Full list of posible time zones can be found at http://www.mkyong.com/java/java-display-list-of-timezone-with-gmt/");
	TimeZone = GetConfig("Global.TimeZone", Calendar.getInstance().getTimeZone().getID(), writer, conf, false);

	writer.addComment("Global.ResMoneyBack", "Enable / Disable money returning on residence removal.");
	ResMoneyBack = GetConfig("Global.ResMoneyBack", false, writer, conf);

	writer.addComment("Global.LeaseCheckInterval", "The interval, in minutes, between residence lease checks (if leases are enabled).");
	leaseCheckInterval = GetConfig("Global.LeaseCheckInterval", 10, writer, conf);

	writer.addComment("Global.LeaseAutoRenew",
	    "Allows leases to automatically renew so long as the player has the money, if economy is disabled, this setting does nothing.");
	leaseAutoRenew = GetConfig("Global.LeaseAutoRenew", true, writer, conf);

	writer.addComment("Global.EnablePermissions", "Whether or not to use the Permissions system in conjunction with this config.");
	GetConfig("Global.EnablePermissions", true, writer, conf);

	writer.addComment("Global.LegacyPermissions", "Set to true if NOT using Permissions or PermissionsBukkit, or using a really old version of Permissions");
	legacyperms = GetConfig("Global.LegacyPermissions", false, writer, conf);

	writer.addComment("Global.EnableEconomy",
	    "Enable / Disable Residence's Economy System (iConomy, MineConomy, Essentials, BOSEconomy, and RealEconomy supported).");
	enableEconomy = GetConfig("Global.EnableEconomy", true, writer, conf);

	writer.addComment("Global.EnableRentSystem", "Enables or disables the Rent System");
	enableRentSystem = GetConfig("Global.EnableRentSystem", true, writer, conf);

	writer.addComment("Global.RentCheckInterval", "The interval, in minutes, between residence rent expiration checks (if the rent system is enabled).");
	rentCheckInterval = GetConfig("Global.RentCheckInterval", 10, writer, conf);

	writer.addComment("Global.ResidenceChatEnable", "Enable or disable residence chat channels.");
	chatEnable = GetConfig("Global.ResidenceChatEnable", true, writer, conf);

	writer.addComment("Global.ActionBar.General", "True for ActionBar - new component in 1.8", "False for old Messaging in chat enter/leave Residence messages");
	actionBar = GetConfig("Global.ActionBar.General", true, writer, conf);
	ActionBarOnSelection = GetConfig("Global.ActionBar.ShowOnSelection", true, writer, conf);

	writer.addComment("Global.ResidenceChatColor", "Color of residence chat.");
	try {
	    chatColor = ChatColor.valueOf(GetConfig("Global.ResidenceChatColor", "DARK_PURPLE", writer, conf, true));
	} catch (Exception ex) {
	    chatColor = ChatColor.DARK_PURPLE;
	}

	writer.addComment("Global.ResidenceChatPrefixLenght", "Max lenght of residence chat prefix including color codes");
	chatPrefixLength = GetConfig("Global.ResidenceChatPrefixLength", 16, writer, conf);

	writer.addComment("Global.AdminOnlyCommands",
	    "Whether or not to ignore the usual Permission flags and only allow OPs and groups with 'residence.admin' to change residences.");
	adminsOnly = GetConfig("Global.AdminOnlyCommands", false, writer, conf);

	writer.addComment("Global.AdminOPs", "Setting this to true makes server OPs admins.");
	adminOps = GetConfig("Global.AdminOPs", true, writer, conf);

	writer.addComment("Global.AdminFullAccess",
	    "Setting this to true server administration wont need to use /resadmin command to access admin command if they are op or have residence.admin permission node.");
	AdminFullAccess = GetConfig("Global.AdminFullAccess", false, writer, conf);

	writer.addComment("Global.MultiWorldPlugin",
	    "This is the name of the plugin you use for multiworld, if you dont have a multiworld plugin you can safely ignore this.",
	    "The only thing this does is check to make sure the multiworld plugin is enabled BEFORE Residence, to ensure properly loading residences for other worlds.");
	multiworldPlugin = GetConfig("Global.MultiWorldPlugin", "Multiverse-Core", writer, conf, false);

	writer.addComment("Global.ResidenceFlagsInherit", "Setting this to true causes subzones to inherit flags from their parent zones.");
	flagsInherit = GetConfig("Global.ResidenceFlagsInherit", true, writer, conf);

	writer.addComment("Global.PreventRentModify", "Setting this to false will allow rented residences to be modified by the renting player.");
	preventBuildInRent = GetConfig("Global.PreventRentModify", true, writer, conf);

	writer.addComment("Global.PreventSubZoneRemoval", "Setting this to true will prevent subzone deletion when subzone owner is not same as parent zone owner.");
	PreventSubZoneRemoval = GetConfig("Global.PreventSubZoneRemoval", true, writer, conf);

	writer.addComment("Global.StopOnSaveFault", "Setting this to false will cause residence to continue to load even if a error is detected in the save file.");
	stopOnSaveError = GetConfig("Global.StopOnSaveFault", true, writer, conf);

	writer.addComment(
	    "This is the residence name filter, that filters out invalid characters.  Google 'Java RegEx' or 'Java Regular Expressions' for more info on how they work.");
	namefix = GetConfig("Global.ResidenceNameRegex", "[^a-zA-Z0-9\\-\\_]", writer, conf, false);

	writer.addComment("Global.ShowIntervalMessages",
	    "Setting this to true sends a message to the console every time Residence does a rent expire check or a lease expire check.");
	showIntervalMessages = GetConfig("Global.ShowIntervalMessages", false, writer, conf);

	writer.addComment("Global.ShowNoobMessage", "Setting this to true sends a tutorial message to the new player when he places chest on ground.");
	ShowNoobMessage = GetConfig("Global.ShowNoobMessage", true, writer, conf);

	writer.addComment("Global.NewPlayer", "Setting this to true creates residence around players placed chest if he don't have any.",
	    "Only once every server restart if he still don't have any residence");
	NewPlayerUse = GetConfig("Global.NewPlayer.Use", false, writer, conf);
	writer.addComment("Global.NewPlayer.Free", "Setting this to true, residence will be created for free",
	    "By setting to false, money will be taken from player, if he has them");
	NewPlayerFree = GetConfig("Global.NewPlayer.Free", true, writer, conf);
	writer.addComment("Global.NewPlayer.Range", "Range from placed chest o both sides. By setting to 5, residence will be 5+5+1 = 11 blocks wide");
	NewPlayerRangeX = GetConfig("Global.NewPlayer.Range.X", 5, writer, conf);
	NewPlayerRangeY = GetConfig("Global.NewPlayer.Range.Y", 5, writer, conf);
	NewPlayerRangeZ = GetConfig("Global.NewPlayer.Range.Z", 5, writer, conf);

	writer.addComment("Global.CustomContainers",
	    "Experimental - The following settings are lists of block IDs to be used as part of the checks for the 'container' and 'use' flags when using mods.");
	customContainers = GetConfig("Global.CustomContainers", new ArrayList<Integer>(), writer, conf);
	customBothClick = GetConfig("Global.CustomBothClick", new ArrayList<Integer>(), writer, conf);
	customRightClick = GetConfig("Global.CustomRightClick", new ArrayList<Integer>(), writer, conf);

	writer.addComment("Global.Visualizer.Use", "With this enabled player will see particle effects to mark selection boundries");
	useVisualizer = GetConfig("Global.Visualizer.Use", true, writer, conf);
	writer.addComment("Global.Visualizer.Range", "Range in blocks to draw particle effects for player",
	    "Keep it no more as 30, as player cant see more than 16 blocks");
	VisualizerRange = GetConfig("Global.Visualizer.Range", 16, writer, conf);
	writer.addComment("Global.Visualizer.ShowFor", "For how long in miliseconds (5000 = 5sec) to show particle effects");
	VisualizerShowFor = GetConfig("Global.Visualizer.ShowFor", 5000, writer, conf);
	writer.addComment("Global.Visualizer.updateInterval", "How often in miliseconds update particles for player");
	VisualizerUpdateInterval = GetConfig("Global.Visualizer.updateInterval", 20, writer, conf);
	writer.addComment("Global.Visualizer.RowSpacing", "Spacing in blocks between particle effects for rows");
	VisualizerRowSpacing = GetConfig("Global.Visualizer.RowSpacing", 2, writer, conf);
	if (VisualizerRowSpacing < 1)
	    VisualizerRowSpacing = 1;
	writer.addComment("Global.Visualizer.CollumnSpacing", "Spacing in blocks between particle effects for collums");
	VisualizerCollumnSpacing = GetConfig("Global.Visualizer.CollumnSpacing", 2, writer, conf);
	if (VisualizerCollumnSpacing < 1)
	    VisualizerCollumnSpacing = 1;
	writer.addComment("Global.Visualizer.Selected",
	    "Particle effect names. Posible: explode, largeexplode, hugeexplosion, fireworksSpark, splash, wake, crit, magicCrit",
	    " smoke, largesmoke, spell, instantSpell, mobSpell, mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager, townaura",
	    " note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust, snowballpoof, snowshovel, slime, heart, barrier", " droplet, take, mobappearance");

	SelectedFrame = ParticleEffects.fromName(GetConfig("Global.Visualizer.Selected.Frame", "happyVillager", writer, conf, false));
	if (SelectedFrame == null) {
	    SelectedFrame = ParticleEffects.VILLAGER_HAPPY;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Frame with this name, it was set to default");
	}

	SelectedSides = ParticleEffects.fromName(GetConfig("Global.Visualizer.Selected.Sides", "reddust", writer, conf, false));
	if (SelectedSides == null) {
	    SelectedSides = ParticleEffects.REDSTONE;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}

	OverlapFrame = ParticleEffects.fromName(GetConfig("Global.Visualizer.Overlap.Frame", "FLAME", writer, conf, false));
	if (OverlapFrame == null) {
	    OverlapFrame = ParticleEffects.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Overlap Frame with this name, it was set to default");
	}

	OverlapSides = ParticleEffects.fromName(GetConfig("Global.Visualizer.Overlap.Sides", "FLAME", writer, conf, false));
	if (OverlapSides == null) {
	    OverlapSides = ParticleEffects.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}

	writer.addComment("Global.BounceAnimation", "Shows particle effect when player are being pushed back");
	BounceAnimation = GetConfig("Global.BounceAnimation", true, writer, conf);

	writer.addComment("Global.GUI.Enabled", "Enable or disable flag GUI");
	useFlagGUI = GetConfig("Global.GUI.Enabled", true, writer, conf);

	writer.addComment("Global.GUI.setTrue", "Item id and data to use when flag is set to true");

	int id = GetConfig("Global.GUI.setTrue.Id", 35, writer, conf);
	int data = GetConfig("Global.GUI.setTrue.Data", 13, writer, conf);

	Material Mat = Material.getMaterial(id);
	if (Mat == null)
	    Mat = Material.STONE;
	GuiTrue = new ItemStack(Mat, 1, (short) data);

	writer.addComment("Global.GUI.setFalse", "Item id and data to use when flag is set to false");
	id = GetConfig("Global.GUI.setFalse.Id", 35, writer, conf);
	data = GetConfig("Global.GUI.setFalse.Data", 14, writer, conf);

	Mat = Material.getMaterial(id);
	if (Mat == null)
	    Mat = Material.STONE;
	GuiFalse = new ItemStack(Mat, 1, (short) data);

	writer.addComment("Global.GUI.setRemove", "Item id and data to use when flag is set to remove");
	id = GetConfig("Global.GUI.setRemove.Id", 35, writer, conf);
	data = GetConfig("Global.GUI.setRemove.Data", 8, writer, conf);

	Mat = Material.getMaterial(id);
	if (Mat == null)
	    Mat = Material.STONE;
	GuiRemove = new ItemStack(Mat, 1, (short) data);

	writer.addComment("Global.AutoMobRemoval", "Default = false. Enabling this, residences with flag nomobs will be cleared from monsters in regular intervals.",
	    "This is quite heavy on server side, so enable only if you really need this feature");
	AutoMobRemoval = GetConfig("Global.AutoMobRemoval.Use", false, writer, conf);
	writer.addComment("Global.AutoMobRemoval.Interval", "How often in seconds to check for monsters in residences. Keep it at reasonable amount");
	AutoMobRemovalInterval = GetConfig("Global.AutoMobRemoval.Interval", 3, writer, conf);

	enforceAreaInsideArea = GetConfig("Global.EnforceAreaInsideArea", false, writer, conf);
	spoutEnable = GetConfig("Global.EnableSpout", false, writer, conf);
	enableLeaseMoneyAccount = GetConfig("Global.EnableLeaseMoneyAccount", true, writer, conf);

	writer.addComment("Global.CouldronCompatability",
	    "By setting this to true, partial compatability for kCouldron servers will be anabled. Action bar messages and selection visualizer will be disabled automaticaly as off incorrect compatability");
	CouldronCompatability = GetConfig("Global.CouldronCompatability", false, writer, conf);
	if (CouldronCompatability) {
	    useVisualizer = false;
	    actionBar = false;
	    ActionBarOnSelection = false;
	}

	try {
	    writer.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void load(FileConfiguration flags, FileConfiguration groups) {

	globalCreatorDefaults = FlagPermissions.parseFromConfigNode("CreatorDefault", flags.getConfigurationSection("Global"));
	globalResidenceDefaults = FlagPermissions.parseFromConfigNode("ResidenceDefault", flags.getConfigurationSection("Global"));

	ConfigurationSection node = groups.getConfigurationSection("Global.GroupDefault");
	if (node != null) {
	    Set<String> keys = node.getConfigurationSection(defaultGroup).getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    globalGroupDefaults.put(key, FlagPermissions.parseFromConfigNodeAsList(key, defaultGroup, "false"));
		}
	    }
	}
    }

    public boolean useVisualizer() {
	return useVisualizer;
    }

    public int getVisualizerRange() {
	return VisualizerRange;
    }

    public int getVisualizerShowFor() {
	return VisualizerShowFor;
    }

    public int getNewPlayerRangeX() {
	return NewPlayerRangeX;
    }

    public int getNewPlayerRangeY() {
	return NewPlayerRangeY;
    }

    public int getNewPlayerRangeZ() {
	return NewPlayerRangeZ;
    }

    public int getVisualizerRowSpacing() {
	return VisualizerRowSpacing;
    }

    public int getVisualizerCollumnSpacing() {
	return VisualizerCollumnSpacing;
    }

    public int getVisualizerUpdateInterval() {
	return VisualizerUpdateInterval;
    }

    public ParticleEffects getSelectedFrame() {
	return SelectedFrame;
    }

    public ParticleEffects getSelectedSides() {
	return SelectedSides;
    }

    public ParticleEffects getOverlapFrame() {
	return OverlapFrame;
    }

    public ParticleEffects getOverlapSides() {
	return OverlapSides;
    }

    public int getTeleportDelay() {
	return TeleportDelay;
    }

    public boolean useLegacyPermissions() {
	return legacyperms;
    }

    public int getMinimalResSize() {
	return MinimalResSize;
    }

    public int getMinimalResX() {
	return MinimalResX;
    }

    public int getMinimalResY() {
	return MinimalResY;
    }

    public int getMinimalResZ() {
	return MinimalResZ;
    }

    public String getDefaultGroup() {
	return defaultGroup;
    }

    public String getResidenceNameRegex() {
	return namefix;
    }

    public boolean enableEconomy() {
	return enableEconomy && Residence.getEconomyManager() != null;
    }

    public boolean enabledRentSystem() {
	return enableRentSystem && enableEconomy();
    }

    public boolean useLeases() {
	return useLeases;
    }

    public boolean useResMoneyBack() {
	return ResMoneyBack;
    }

    public boolean allowAdminsOnly() {
	return adminsOnly;
    }

    public boolean allowEmptyResidences() {
	return allowEmptyResidences;
    }

    public boolean isNoLava() {
	return NoLava;
    }

    public boolean isNoWater() {
	return NoWater;
    }

    public boolean isNoLavaPlace() {
	return NoLavaPlace;
    }

    public boolean isBlockFall() {
	return useBlockFall;
    }

    public boolean isNoWaterPlace() {
	return NoWaterPlace;
    }

    public boolean isUseResidenceFileClean() {
	return AutoCleanUp;
    }

    public int getResidenceFileCleanDays() {
	return AutoCleanUpDays;
    }

    public boolean isUseClean() {
	return UseClean;
    }

    public boolean isPvPFlagPrevent() {
	return PvPFlagPrevent;
    }

    public boolean isOverridePvp() {
	return OverridePvp;
    }

    public boolean isResTpCaseSensitive() {
	return ResTpCaseSensitive;
    }

    public boolean isBlockAnyTeleportation() {
	return BlockAnyTeleportation;
    }

    public boolean isResCreateCaseSensitive() {
	return ResCreateCaseSensitive;
    }

    public int getInfoToolID() {
	return infoToolId;
    }

    public int getSelectionTooldID() {
	return selectionToolId;
    }

    public boolean getOpsAreAdmins() {
	return adminOps;
    }

    public boolean getAdminFullAccess() {
	return AdminFullAccess;
    }

    public String getMultiworldPlugin() {
	return multiworldPlugin;
    }

    public boolean autoRenewLeases() {
	return leaseAutoRenew;
    }

    public boolean isShortInfoUse() {
	return ShortInfoUse;
    }

    public boolean isOnlyLike() {
	return OnlyLike;
    }

    public int getRentCheckInterval() {
	return rentCheckInterval;
    }

    public int getChatPrefixLength() {
	return chatPrefixLength;
    }

    public int getLeaseCheckInterval() {
	return leaseCheckInterval;
    }

    public int getAutoSaveInterval() {
	return autoSaveInt;
    }

    public int getFlowLevel() {
	return FlowLevel;
    }

    public int getPlaceLevel() {
	return PlaceLevel;
    }

    public int getBlockFallLevel() {
	return BlockFallLevel;
    }

    public int getCleanLevel() {
	return CleanLevel;
    }

    public boolean flagsInherit() {
	return flagsInherit;
    }

    public boolean chatEnabled() {
	return chatEnable;
    }

    public boolean useActionBar() {
	return actionBar;
    }

    public boolean useActionBarOnSelection() {
	return ActionBarOnSelection;
    }

    public ChatColor getChatColor() {
	return chatColor;
    }

    public int getMinMoveUpdateInterval() {
	return minMoveUpdate;
    }

    public int getMaxResCount() {
	return MaxResCount;
    }

    public int getMaxRentCount() {
	return MaxRentCount;
    }

    public int getMaxSubzonesCount() {
	return MaxSubzonesCount;
    }

    public int getVoteRangeFrom() {
	return VoteRangeFrom;
    }

    public int getHealInterval() {
	return HealInterval;
    }

    public int getFeedInterval() {
	return FeedInterval;
    }

    public int getVoteRangeTo() {
	return VoteRangeTo;
    }

    public FlagPermissions getGlobalCreatorDefaultFlags() {
	return globalCreatorDefaults;
    }

    public FlagPermissions getGlobalResidenceDefaultFlags() {
	return globalResidenceDefaults;
    }

    public Map<String, FlagPermissions> getGlobalGroupDefaultFlags() {
	return globalGroupDefaults;
    }

    public String getLanguage() {
	return language;
    }

    public String getDefaultWorld() {
	return DefaultWorld;
    }

    public String getDateFormat() {
	return DateFormat;
    }

    public String getTimeZone() {
	return TimeZone;
    }

    public boolean preventRentModify() {
	return preventBuildInRent;
    }

    public boolean isPreventSubZoneRemoval() {
	return PreventSubZoneRemoval;
    }

    public boolean stopOnSaveError() {
	return stopOnSaveError;
    }

    public boolean showIntervalMessages() {
	return showIntervalMessages;
    }

    public boolean ShowNoobMessage() {
	return ShowNoobMessage;
    }

    public boolean isNewPlayerUse() {
	return NewPlayerUse;
    }

    public boolean isNewPlayerFree() {
	return NewPlayerFree;
    }

    public boolean enableSpout() {
	return spoutEnable;
    }

    public boolean AutoMobRemoval() {
	return AutoMobRemoval;
    }

    public int AutoMobRemovalInterval() {
	return AutoMobRemovalInterval;
    }

    public boolean enableLeaseMoneyAccount() {
	return enableLeaseMoneyAccount;
    }

    public boolean CouldronCompatability() {
	return CouldronCompatability;
    }

    public boolean debugEnabled() {
	return enableDebug;
    }

    public boolean isSelectionIgnoreY() {
	return SelectionIgnoreY;
    }

    public boolean isNoCostForYBlocks() {
	return NoCostForYBlocks;
    }

    public boolean versionCheck() {
	return versionCheck;
    }

    public boolean isUUIDConvertion() {
	return UUIDConvertion;
    }

    public boolean isOfflineMode() {
	return OfflineMode;
    }

    public List<Integer> getCustomContainers() {
	return customContainers;
    }

    public List<Integer> getCustomBothClick() {
	return customBothClick;
    }

    public List<Integer> getCustomRightClick() {
	return customRightClick;
    }

    public List<Integer> getCleanBlocks() {
	return CleanBlocks;
    }

    public List<String> getNoFlowWorlds() {
	return NoFlowWorlds;
    }

    public List<String> getAutoCleanUpWorlds() {
	return AutoCleanUpWorlds;
    }

    public List<String> getNoPlaceWorlds() {
	return NoPlaceWorlds;
    }

    public List<String> getBlockFallWorlds() {
	return BlockFallWorlds;
    }

    public List<String> getNegativePotionEffects() {
	return NegativePotionEffects;
    }

    public List<String> getCleanWorlds() {
	return CleanWorlds;
    }

    public List<String> getProtectedFlagsList() {
	return FlagsList;
    }

    public boolean getEnforceAreaInsideArea() {
	return enforceAreaInsideArea;
    }

    public ItemStack getGuiTrue() {
	return GuiTrue;
    }

    public ItemStack getGuiFalse() {
	return GuiFalse;
    }

    public ItemStack getGuiRemove() {
	return GuiRemove;
    }

    public List<RandomTeleport> getRandomTeleport() {
	return RTeleport;
    }

    public int getrtCooldown() {
	return rtCooldown;
    }

    public Location getKickLocation() {
	return KickLocation;
    }

    public int getrtMaxTries() {
	return rtMaxTries;
    }

    public boolean BounceAnimation() {
	return BounceAnimation;
    }
}
