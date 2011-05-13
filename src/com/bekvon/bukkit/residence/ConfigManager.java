/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import org.bukkit.Material;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class ConfigManager {
    protected String defaultGroup;
    protected boolean useLeases;
    protected boolean enableEconomy;
    protected boolean adminsOnly;
    protected boolean allowEmptyResidences;
    protected int infoToolId;
    protected int selectionToolId;
    protected boolean adminOps;
    protected String multiworldPlugin;
    protected boolean enableRentSystem;

    public ConfigManager(Configuration config)
    {
        this.load(config);
    }

    public void load(Configuration config) {
        defaultGroup = config.getString("Global.DefaultGroup", "default");
        adminsOnly = config.getBoolean("Global.AdminOnlyCommands", false);
        useLeases = config.getBoolean("Global.UseLeaseSystem", false);
        enableEconomy = config.getBoolean("Global.EnableEconomy", false);
        allowEmptyResidences = config.getBoolean("Global.AllowEmptyResidences", true);
        infoToolId = config.getInt("Global.InfoToolId", Material.STRING.getId());
        selectionToolId = config.getInt("Global.SelectionToolId", Material.WOOD_AXE.getId());
        adminOps = config.getBoolean("Global.AdminOPs", true);
        multiworldPlugin = config.getString("Global.MultiWorldPlugin");
        enableRentSystem = config.getBoolean("Global.EnableRentSystem", false);
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public boolean enableEconomy() {
        return enableEconomy;
    }

    public boolean enabledRentSystem()
    {
        return enableRentSystem;
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

}
