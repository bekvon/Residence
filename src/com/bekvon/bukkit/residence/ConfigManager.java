/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.ParticleEffects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Administrator
 */
public class ConfigManager {
    protected String defaultGroup;
    protected boolean useLeases;
    protected boolean ResMoneyBack;
    protected boolean enableEconomy;
    protected boolean adminsOnly;
    protected boolean allowEmptyResidences;
    protected int infoToolId;
    protected int selectionToolId;
    protected boolean adminOps;
    protected String multiworldPlugin;
    protected boolean enableRentSystem;
    protected boolean leaseAutoRenew;
    protected int rentCheckInterval;
    protected int leaseCheckInterval;
    protected int autoSaveInt;
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
    protected FlagPermissions globalCreatorDefaults;
    protected FlagPermissions globalResidenceDefaults;
    protected Map<String, FlagPermissions> globalGroupDefaults;
    protected String language;
    protected boolean preventBuildInRent;
    protected boolean stopOnSaveError;
    protected boolean legacyperms;
    protected String namefix;
    protected boolean showIntervalMessages;
    protected boolean ShowNoobMessage;
    protected boolean spoutEnable;
    protected boolean AutoMobRemoval;
    protected int AutoMobRemovalInterval;
    protected boolean enableLeaseMoneyAccount;
    protected boolean enableDebug = false;
    protected boolean versionCheck;
    protected boolean useVisualizer;
    protected List<Integer> customContainers;
    protected List<Integer> customBothClick;
    protected List<Integer> customRightClick;
    private boolean enforceAreaInsideArea;

    protected ParticleEffects SelectedFrame;
    protected ParticleEffects SelectedSides;

    protected ParticleEffects OverlapFrame;
    protected ParticleEffects OverlapSides;

    public ConfigManager(FileConfiguration config, FileConfiguration flags, FileConfiguration groups) {
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

    public static List<Integer> GetConfigIntArray(String path, List<Integer> list, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, list);
	copySetting(conf, writer, path);
	return conf.getIntegerList(path);
    }

    public static Boolean GetConfigBoolean(String path, Boolean text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getBoolean(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static int GetConfigInt(String path, int text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getInt(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static Double GetConfigDouble(String path, Double text, CommentedYamlConfiguration writer, YamlConfiguration conf) {
	conf.addDefault(path, text);
	text = conf.getDouble(path);
	copySetting(conf, writer, path);
	return text;
    }

    public static String GetConfigString(String path, String text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = conf.getString(path);
	if (colorize)
	    text = Colors(text);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> GetConfigArray(String path, List<String> text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = ColorsArray(conf.getStringList(path), colorize);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<Integer> GetConfigIntArray(String path, List<Integer> text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
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

    @SuppressWarnings("deprecation")
    void UpdateConfigFile() {

	File f = new File(Residence.instance.getDataFolder(), "config.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	conf.options().copyDefaults(true);

	writer.addComment("Global", "These are Global Settings for Residence.");
	writer.addComment("Global.Language", "This loads the <language>.yml file in the Residence Language folder",
	    "All Residence text comes from this file. (NOT DONE YET)");
	language = GetConfigString("Global.Language", "English", writer, conf, false);

	writer.addComment("Global.SelectionToolId", "Wooden Hoe is the default selection tool for Residence.",
	    "You can change it to another item ID listed here: http://www.minecraftwiki.net/wiki/Data_values");
	selectionToolId = GetConfigInt("Global.SelectionToolId", Material.WOOD_AXE.getId(), writer, conf);

	writer.addComment("Global.InfoToolId", "This determins which tool you can use to see info on residences, default is String.",
	    "Simply equip this tool and hit a location inside the residence and it will display the info for it.");
	infoToolId = GetConfigInt("Global.InfoToolId", Material.STRING.getId(), writer, conf);

	writer.addComment("Global.MoveCheckInterval", "The interval, in milliseconds, between movement checks.",
	    "Reducing this will increase the load on the server.",
	    "Increasing this will allow players to move further in movement restricted zones before they are teleported out.");
	minMoveUpdate = GetConfigInt("Global.MoveCheckInterval", 500, writer, conf);

	writer.addComment("Global.Tp.TeleportDelay", "The interval, in seconds, for teleportation.",
	    "Use 0 to disable");
	TeleportDelay = GetConfigInt("Global.Tp.TeleportDelay", 3, writer, conf);

	writer.addComment("Global.Size.MinimalSize", "Minimal size of residence in blocks", "1000 is 10x10x10 residence size");
	MinimalResSize = GetConfigInt("Global.Size.MinimalSize", 100, writer, conf);
	MinimalResX = GetConfigInt("Global.Size.MinimalX", 10, writer, conf);
	MinimalResY = GetConfigInt("Global.Size.MinimalY", 10, writer, conf);
	MinimalResZ = GetConfigInt("Global.Size.MinimalZ", 10, writer, conf);

	writer.addComment("Global.SaveInterval", "The interval, in minutes, between residence saves.");
	autoSaveInt = GetConfigInt("Global.SaveInterval", 10, writer, conf);

	writer.addComment("Global.DefaultGroup", "The default group to use if Permissions fails to attach or your not using Permissions.");
	defaultGroup = GetConfigString("Global.DefaultGroup", "default", writer, conf, false);

	writer.addComment("Global.UseLeaseSystem", "Enable / Disable the Lease System.");
	useLeases = GetConfigBoolean("Global.UseLeaseSystem", false, writer, conf);
	
	writer.addComment("Global.ResMoneyBack", "Enable / Disable money returning on residence removal.");
	ResMoneyBack = GetConfigBoolean("Global.ResMoneyBack", false, writer, conf);
	
	writer.addComment("Global.LeaseCheckInterval", "The interval, in minutes, between residence lease checks (if leases are enabled).");
	leaseCheckInterval = GetConfigInt("Global.LeaseCheckInterval", 10, writer, conf);

	writer.addComment("Global.LeaseAutoRenew",
	    "Allows leases to automatically renew so long as the player has the money, if economy is disabled, this setting does nothing.");
	leaseAutoRenew = GetConfigBoolean("Global.LeaseAutoRenew", true, writer, conf);

	writer.addComment("Global.EnablePermissions",
	    "Whether or not to use the Permissions system in conjunction with this config.");
	GetConfigBoolean("Global.EnablePermissions", true, writer, conf);

	writer.addComment("Global.LegacyPermissions", "Set to true if NOT using Permissions or PermissionsBukkit, or using a really old version of Permissions");
	legacyperms = GetConfigBoolean("Global.LegacyPermissions", false, writer, conf);

	writer.addComment("Global.EnableEconomy",
	    "Enable / Disable Residence's Economy System (iConomy, MineConomy, Essentials, BOSEconomy, and RealEconomy supported).");
	enableEconomy = GetConfigBoolean("Global.EnableEconomy", true, writer, conf);

	writer.addComment("Global.EnableRentSystem", "Enables or disables the Rent System");
	enableRentSystem = GetConfigBoolean("Global.EnableRentSystem", true, writer, conf);

	writer.addComment("Global.RentCheckInterval", "The interval, in minutes, between residence rent expiration checks (if the rent system is enabled).");
	rentCheckInterval = GetConfigInt("Global.RentCheckInterval", 10, writer, conf);

	writer.addComment("Global.ResidenceChatEnable", "Enable or disable residence chat channels.");
	chatEnable = GetConfigBoolean("Global.ResidenceChatEnable", true, writer, conf);

	writer.addComment("Global.ActionBar.General", "True for ActionBar - new component in 1.8", "False for old Messaging in chat enter/leave Residence messages");
	actionBar = GetConfigBoolean("Global.ActionBar.General", true, writer, conf);
	ActionBarOnSelection = GetConfigBoolean("Global.ActionBar.ShowOnSelection", true, writer, conf);

	writer.addComment("Global.ResidenceChatColor", "Color of residence chat.");
	try {
	    chatColor = ChatColor.valueOf(GetConfigString("Global.ResidenceChatColor", "DARK_PURPLE", writer, conf, true));
	} catch (Exception ex) {
	    chatColor = ChatColor.DARK_PURPLE;
	}

	writer.addComment("Global.AdminOnlyCommands",
	    "Whether or not to ignore the usual Permission flags and only allow OPs and groups with 'residence.admin' to change residences.");
	adminsOnly = GetConfigBoolean("Global.AdminOnlyCommands", false, writer, conf);

	writer.addComment("Global.AdminOPs", "Setting this to true makes server OPs admins.");
	adminOps = GetConfigBoolean("Global.AdminOPs", true, writer, conf);

	writer.addComment("Global.MultiWorldPlugin",
	    "This is the name of the plugin you use for multiworld, if you dont have a multiworld plugin you can safely ignore this.",
	    "The only thing this does is check to make sure the multiworld plugin is enabled BEFORE Residence, to ensure properly loading residences for other worlds.");
	multiworldPlugin = GetConfigString("Global.MultiWorldPlugin", "Multiverse-Core", writer, conf, false);

	writer.addComment("Global.ResidenceFlagsInherit", "Setting this to true causes subzones to inherit flags from their parent zones.");
	flagsInherit = GetConfigBoolean("Global.ResidenceFlagsInherit", true, writer, conf);

	writer.addComment("Global.PreventRentModify", "Setting this to false will allow rented residences to be modified by the renting player.");
	preventBuildInRent = GetConfigBoolean("Global.PreventRentModify", true, writer, conf);

	writer.addComment("Global.StopOnSaveFault", "Setting this to false will cause residence to continue to load even if a error is detected in the save file.");
	stopOnSaveError = GetConfigBoolean("Global.StopOnSaveFault", true, writer, conf);

	writer.addComment(
	    "This is the residence name filter, that filters out invalid characters.  Google 'Java RegEx' or 'Java Regular Expressions' for more info on how they work.");
	namefix = GetConfigString("Global.ResidenceNameRegex", "[^a-zA-Z0-9\\-\\_]", writer, conf, false);

	writer.addComment("Global.ShowIntervalMessages",
	    "Setting this to true sends a message to the console every time Residence does a rent expire check or a lease expire check.");
	showIntervalMessages = GetConfigBoolean("Global.ShowIntervalMessages", false, writer, conf);

	writer.addComment("Global.ShowNoobMessage",
	    "Setting this to true sends a tutorial message to the new player when he places chest on ground.");
	ShowNoobMessage = GetConfigBoolean("Global.ShowNoobMessage", true, writer, conf);

	writer.addComment("Global.CustomContainers",
	    "Experimental - The following settings are lists of block IDs to be used as part of the checks for the 'container' and 'use' flags when using mods.");
	customContainers = GetConfigIntArray("Global.CustomContainers", new ArrayList<Integer>(), writer, conf);
	customBothClick = GetConfigIntArray("Global.CustomBothClick", new ArrayList<Integer>(), writer, conf);
	customRightClick = GetConfigIntArray("Global.CustomRightClick", new ArrayList<Integer>(), writer, conf);

	writer.addComment("Global.Visualizer.Use", "With this enabled player will see particle effects to mark selection boundries");
	useVisualizer = GetConfigBoolean("Global.Visualizer.Use", true, writer, conf);
	writer.addComment("Global.Visualizer.Range", "Range in blocks to draw particle effects for player",
	    "Keep it no more as 30, as player cant see more than 16 blocks");
	VisualizerRange = GetConfigInt("Global.Visualizer.Range", 25, writer, conf);
	writer.addComment("Global.Visualizer.ShowFor", "For how long in miliseconds (5000 = 5sec) to show particle effects");
	VisualizerShowFor = GetConfigInt("Global.Visualizer.ShowFor", 5000, writer, conf);
	writer.addComment("Global.Visualizer.updateInterval", "How often in miliseconds update particles for player");
	VisualizerUpdateInterval = GetConfigInt("Global.Visualizer.updateInterval", 20, writer, conf);
	writer.addComment("Global.Visualizer.RowSpacing", "Spacing in blocks between particle effects for rows");
	VisualizerRowSpacing = GetConfigInt("Global.Visualizer.RowSpacing", 2, writer, conf);
	if (VisualizerRowSpacing < 1)
	    VisualizerRowSpacing = 1;
	writer.addComment("Global.Visualizer.CollumnSpacing", "Spacing in blocks between particle effects for collums");
	VisualizerCollumnSpacing = GetConfigInt("Global.Visualizer.CollumnSpacing", 2, writer, conf);
	if (VisualizerCollumnSpacing < 1)
	    VisualizerCollumnSpacing = 1;
	writer.addComment("Global.Visualizer.Selected",
	    "Particle effect names. Posible: explode, largeexplode, hugeexplosion, fireworksSpark, splash, wake, crit, magicCrit",
	    " smoke, largesmoke, spell, instantSpell, mobSpell, mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager, townaura",
	    " note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust, snowballpoof, snowshovel, slime, heart, barrier",
	    " droplet, take, mobappearance");

	SelectedFrame = ParticleEffects.fromName(GetConfigString("Global.Visualizer.Selected.Frame", "happyVillager", writer, conf, false));
	if (SelectedFrame == null) {
	    SelectedFrame = ParticleEffects.VILLAGER_HAPPY;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Frame with this name, it was set to default");
	}

	SelectedSides = ParticleEffects.fromName(GetConfigString("Global.Visualizer.Selected.Sides", "reddust", writer, conf, false));
	if (SelectedSides == null) {
	    SelectedSides = ParticleEffects.REDSTONE;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}

	OverlapFrame = ParticleEffects.fromName(GetConfigString("Global.Visualizer.Overlap.Frame", "FLAME", writer, conf, false));
	if (OverlapFrame == null) {
	    OverlapFrame = ParticleEffects.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Overlap Frame with this name, it was set to default");
	}

	OverlapSides = ParticleEffects.fromName(GetConfigString("Global.Visualizer.Overlap.Sides", "FLAME", writer, conf, false));
	if (OverlapSides == null) {
	    OverlapSides = ParticleEffects.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}
	
	writer.addComment("Global.AutoMobRemoval",
	    "Default = false. Enabling this, residences with flag nomobs will be cleared from monsters in regular intervals.","This is quite heavy on server side, so enable only if you really need this feature");
	AutoMobRemoval = GetConfigBoolean("Global.AutoMobRemoval.Use", false, writer, conf);
	writer.addComment("Global.AutoMobRemoval.Interval",
	    "How often in seconds to check for monsters in residences. Keep it at reasonable amount");
	AutoMobRemovalInterval = GetConfigInt("Global.AutoMobRemoval.Interval", 3, writer, conf);
	
	enforceAreaInsideArea = GetConfigBoolean("Global.EnforceAreaInsideArea", false, writer, conf);
	spoutEnable = GetConfigBoolean("Global.EnableSpout", false, writer, conf);
	enableLeaseMoneyAccount = GetConfigBoolean("Global.EnableLeaseMoneyAccount", true, writer, conf);

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

    public int getInfoToolID() {
	return infoToolId;
    }

    public int getSelectionTooldID() {
	return selectionToolId;
    }

    public boolean getOpsAreAdmins() {
	return adminOps;
    }

    public String getMultiworldPlugin() {
	return multiworldPlugin;
    }

    public boolean autoRenewLeases() {
	return leaseAutoRenew;
    }

    public int getRentCheckInterval() {
	return rentCheckInterval;
    }

    public int getLeaseCheckInterval() {
	return leaseCheckInterval;
    }

    public int getAutoSaveInterval() {
	return autoSaveInt;
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

    public boolean preventRentModify() {
	return preventBuildInRent;
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

    public boolean debugEnabled() {
	return enableDebug;
    }

    public boolean versionCheck() {
	return versionCheck;
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

    public boolean getEnforceAreaInsideArea() {
	return enforceAreaInsideArea;
    }
}
