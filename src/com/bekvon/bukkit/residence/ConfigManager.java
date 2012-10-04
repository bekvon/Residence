/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Administrator
 */
public class ConfigManager {
	protected String defaultGroup;
	protected boolean useLeases;
	protected boolean enableEconomy;
	protected String economySystem;
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
	protected boolean flagsInherit;
	protected ChatColor chatColor;
	protected boolean chatEnable;
	protected int minMoveUpdate;
	protected Map<String, Boolean> globalCreatorDefaults;
	protected Map<String, Boolean> globalResidenceDefaults;
	protected Map<String,Map<String, Boolean>> globalGroupDefaults;
	protected String language;
	protected boolean preventBuildInRent;
	protected boolean legacyperms;
	protected String namefix;
	protected boolean showIntervalMessages;
	protected boolean spoutEnable;
	protected boolean enableLeaseMoneyAccount;
	protected boolean enableDebug;
	protected String url;
	protected String user;
	protected String pass;
	protected List<Integer> customContainers;
	protected List<Integer> customBothClick;
	protected List<Integer> customRightClick;

	public ConfigManager(FileConfiguration config) {
		globalCreatorDefaults = new HashMap<String, Boolean>();
		globalResidenceDefaults = new HashMap<String, Boolean>();
		globalGroupDefaults = new HashMap<String,Map<String, Boolean>>();
		this.load(config);
	}

	private void load(FileConfiguration config) {
		defaultGroup = config.getString("Global.DefaultGroup", "default").toLowerCase();
		adminsOnly = config.getBoolean("Global.AdminOnlyCommands", false);
		useLeases = config.getBoolean("Global.UseLeaseSystem", false);
		leaseAutoRenew = config.getBoolean("Global.LeaseAutoRenew", true);
		enableEconomy = config.getBoolean("Global.EnableEconomy", false);
		economySystem = config.getString("Global.EconomySystem", "iConomy");
		infoToolId = config.getInt("Global.InfoToolId", Material.STRING.getId());
		selectionToolId = config.getInt("Global.SelectionToolId", Material.WOOD_AXE.getId());
		adminOps = config.getBoolean("Global.AdminOPs", true);
		multiworldPlugin = config.getString("Global.MultiWorldPlugin");
		enableRentSystem = config.getBoolean("Global.EnableRentSystem", false);
		rentCheckInterval = config.getInt("Global.RentCheckInterval", 10);
		leaseCheckInterval = config.getInt("Global.LeaseCheckInterval", 10);
		flagsInherit = config.getBoolean("Global.ResidenceFlagsInherit", false);
		minMoveUpdate = config.getInt("Global.MoveCheckInterval", 500);
		chatEnable = config.getBoolean("Global.ResidenceChatEnable", true);
		language = config.getString("Global.Language","English");
		preventBuildInRent = config.getBoolean("Global.PreventRentModify", true);
		legacyperms = config.getBoolean("Global.LegacyPermissions",false);
		namefix = config.getString("Global.ResidenceNameRegex",null);//"[^a-zA-Z0-9\\-\\_]"
		showIntervalMessages = config.getBoolean("Global.ShowIntervalMessages", false);
		spoutEnable = config.getBoolean("Global.EnableSpout", false);
		enableLeaseMoneyAccount = config.getBoolean("Global.EnableLeaseMoneyAccount", true);
		enableDebug = config.getBoolean("Global.EnableDebug", false);
		url = "jdbc:mysql://"+config.getString("Global.MySQL.URL.IP")+":"+config.getString("Global.MySQL.URL.PORT")+"/"+config.getString("Global.MySQL.URL.DATABASE");
		pass = config.getString("Global.MySQL.Password");
		user = config.getString("Global.MySQL.Username");
		customContainers = config.getIntegerList("Global.CustomContainers");
		customBothClick = config.getIntegerList("Global.CustomBothClick");
		customRightClick = config.getIntegerList("Global.CustomRightClick");
		//TODO PARSE AND EDIT DEFAULTS
		globalCreatorDefaults = null;
		globalResidenceDefaults = null;
		ConfigurationSection node = config.getConfigurationSection("Global.GroupDefault");
		if(node!=null) {
			Set<String> keys = node.getConfigurationSection(defaultGroup).getKeys(false);
			if(keys!=null) {
				String group = null;
				Map<String, Boolean> Perms = new HashMap<String, Boolean>();
				for(String key: keys) {
					//TODO PARSE VALUE
					Boolean value = null;
					Perms.put(key, value);
				}
				globalGroupDefaults.put(group, Perms);
			}
		}
		try {
			chatColor = ChatColor.valueOf(config.getString("Global.ResidenceChatColor", "DARK_PURPLE"));
		} catch (Exception ex) {
			chatColor = ChatColor.DARK_PURPLE;
		}
	}

	public boolean useLegacyPermissions() {
		return legacyperms;
	}

	public String getDefaultGroup() {
		return defaultGroup;
	}

	public String getResidenceNameRegex() {
		return namefix;
	}

	public boolean enableEconomy() {
		return enableEconomy && Residence.getEconomyManager()!=null;
	}

	public boolean enabledRentSystem() {
		return enableRentSystem && enableEconomy();
	}

	public boolean useLeases() {
		return useLeases;
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

	public String getEconomySystem() {
		return economySystem;
	}

	public int getRentCheckInterval() {
		return rentCheckInterval;
	}

	public int getLeaseCheckInterval() {
		return leaseCheckInterval;
	}

	public boolean flagsInherit() {
		return flagsInherit;
	}

	public boolean chatEnabled() {
		return chatEnable;
	}

	public ChatColor getChatColor() {
		return chatColor;
	}

	public int getMinMoveUpdateInterval() {
		return minMoveUpdate;
	}

	public Map<String, Boolean> getGlobalCreatorDefaultFlags() {
		return globalCreatorDefaults;
	}

	public Map<String, Boolean> getGlobalResidenceDefaultFlags() {
		return globalResidenceDefaults;
	}

	public Map<String,Map<String, Boolean>> getGlobalGroupDefaultFlags() {
		return globalGroupDefaults;
	}

	public String getLanguage() {
		return language;
	}

	public boolean preventRentModify() {
		return preventBuildInRent;
	}
	public boolean showIntervalMessages() {
		return showIntervalMessages;
	}
	public boolean enableSpout() {
		return spoutEnable;
	}
	public boolean enableLeaseMoneyAccount() {
		return enableLeaseMoneyAccount;
	}
	public boolean debugEnabled() {
		return enableDebug;
	}
	public String getSQLUrl() {
		return url;
	}
	public String getSQLUser() {
		return user;
	}
	public String getSQLPass() {
		return pass;
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

	public boolean isEnderManBuildEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEntityInteractEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCreatureSpawnEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPaintingListenerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEntityCombustEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExplosionEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPvpListenerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlockBreakEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlockPlaceEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlockSpreadEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPistonListenerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlockFromToEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isBlockFireEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
}
