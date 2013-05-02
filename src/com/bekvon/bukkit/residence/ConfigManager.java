/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
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
    protected int autoSaveInt;
    protected boolean flagsInherit;
    protected ChatColor chatColor;
    protected boolean chatEnable;
    protected int minMoveUpdate;
    protected FlagPermissions globalCreatorDefaults;
    protected FlagPermissions globalResidenceDefaults;
    protected Map<String,FlagPermissions> globalGroupDefaults;
    protected String language;
    protected boolean preventBuildInRent;
    protected boolean stopOnSaveError;
    protected boolean legacyperms;
    protected String namefix;
    protected boolean showIntervalMessages;
    protected boolean spoutEnable;
    protected boolean enableLeaseMoneyAccount;
    protected boolean enableDebug;
    protected List<Integer> customContainers;
    protected List<Integer> customBothClick;
    protected List<Integer> customRightClick;
    private boolean enforceAreaInsideArea;

    public ConfigManager(FileConfiguration config)
    {
        globalCreatorDefaults = new FlagPermissions();
        globalResidenceDefaults = new FlagPermissions();
        globalGroupDefaults = new HashMap<String,FlagPermissions>();
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
        autoSaveInt = config.getInt("Global.SaveInterval", 10);
        flagsInherit = config.getBoolean("Global.ResidenceFlagsInherit", false);
        minMoveUpdate = config.getInt("Global.MoveCheckInterval", 500);
        chatEnable = config.getBoolean("Global.ResidenceChatEnable", true);
        enforceAreaInsideArea = config.getBoolean("Global.EnforceAreaInsideArea", false);
        language = config.getString("Global.Language","English");
        globalCreatorDefaults = FlagPermissions.parseFromConfigNode("CreatorDefault", config.getConfigurationSection("Global"));
        globalResidenceDefaults = FlagPermissions.parseFromConfigNode("ResidenceDefault", config.getConfigurationSection("Global"));
        preventBuildInRent = config.getBoolean("Global.PreventRentModify", true);
        stopOnSaveError = config.getBoolean("Global.StopOnSaveFault",true);
        legacyperms = config.getBoolean("Global.LegacyPermissions",false);
        namefix = config.getString("Global.ResidenceNameRegex",null);//"[^a-zA-Z0-9\\-\\_]"
        showIntervalMessages = config.getBoolean("Global.ShowIntervalMessages", false);
        spoutEnable = config.getBoolean("Global.EnableSpout", false);
        enableLeaseMoneyAccount = config.getBoolean("Global.EnableLeaseMoneyAccount", true);
        enableDebug = config.getBoolean("Global.EnableDebug", false);
        customContainers = config.getIntegerList("Global.CustomContainers");
        customBothClick = config.getIntegerList("Global.CustomBothClick");
        customRightClick = config.getIntegerList("Global.CustomRightClick");
        ConfigurationSection node = config.getConfigurationSection("Global.GroupDefault");
        if(node!=null)
        {
            Set<String> keys = node.getConfigurationSection(defaultGroup).getKeys(false);
            if(keys!=null)
            {
                for(String key: keys)
                {
                    globalGroupDefaults.put(key, FlagPermissions.parseFromConfigNode(key, config.getConfigurationSection("Global.GroupDefault")));
                }
            }
        }
        try {
            chatColor = ChatColor.valueOf(config.getString("Global.ResidenceChatColor", "DARK_PURPLE"));
        } catch (Exception ex) {
            chatColor = ChatColor.DARK_PURPLE;
        }
    }

    public boolean useLegacyPermissions()
    {
        return legacyperms;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public String getResidenceNameRegex()
    {
        return namefix;
    }

    public boolean enableEconomy() {
        return enableEconomy && Residence.getEconomyManager()!=null;
    }

    public boolean enabledRentSystem()
    {
        return enableRentSystem && enableEconomy();
    }

    public boolean useLeases() {
        return useLeases;
    }

    public boolean allowAdminsOnly() {
        return adminsOnly;
    }
    public boolean allowEmptyResidences()
    {
        return allowEmptyResidences;
    }
    public int getInfoToolID()
    {
        return infoToolId;
    }
    public int getSelectionTooldID()
    {
        return selectionToolId;
    }

    public boolean getOpsAreAdmins()
    {
        return adminOps;
    }

    public String getMultiworldPlugin()
    {
        return multiworldPlugin;
    }

    public boolean autoRenewLeases()
    {
        return leaseAutoRenew;
    }

    public String getEconomySystem()
    {
        return economySystem;
    }

    public int getRentCheckInterval()
    {
        return rentCheckInterval;
    }

    public int getLeaseCheckInterval()
    {
        return leaseCheckInterval;
    }

    public int getAutoSaveInterval()
    {
        return autoSaveInt;
    }

    public boolean flagsInherit()
    {
        return flagsInherit;
    }

    public boolean chatEnabled()
    {
        return chatEnable;
    }

    public ChatColor getChatColor()
    {
        return chatColor;
    }

    public int getMinMoveUpdateInterval()
    {
        return minMoveUpdate;
    }

    public FlagPermissions getGlobalCreatorDefaultFlags()
    {
        return globalCreatorDefaults;
    }

    public FlagPermissions getGlobalResidenceDefaultFlags()
    {
        return globalResidenceDefaults;
    }

    public Map<String,FlagPermissions> getGlobalGroupDefaultFlags()
    {
        return globalGroupDefaults;
    }

    public String getLanguage()
    {
        return language;
    }

    public boolean preventRentModify()
    {
        return preventBuildInRent;
    }
    public boolean stopOnSaveError()
    {
        return stopOnSaveError;
    }
    public boolean showIntervalMessages()
    {
        return showIntervalMessages;
    }
    public boolean enableSpout()
    {
        return spoutEnable;
    }
    public boolean enableLeaseMoneyAccount()
    {
        return enableLeaseMoneyAccount;
    }
    public boolean debugEnabled()
    {
        return enableDebug;
    }
    
    public List<Integer> getCustomContainers()
    {
    	return customContainers;
    }
    
    public List<Integer> getCustomBothClick()
    {
    	return customBothClick;
    }
    
    public List<Integer> getCustomRightClick()
    {
    	return customRightClick;
    }

    public boolean getEnforceAreaInsideArea() {
        return enforceAreaInsideArea;
    }
}
