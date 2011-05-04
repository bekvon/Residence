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
    protected boolean enableBuySell;
    protected boolean adminsOnly;
    protected boolean allowEmptyResidences;
    protected int infoToolId;
    protected int selectionToolId;
    protected boolean adminOps;

    public ConfigManager(Configuration config)
    {
        this.load(config);
    }

    public void load(Configuration config) {
        defaultGroup = config.getString("Global.DefaultGroup", "default");
        adminsOnly = config.getBoolean("Global.AdminOnlyCommands", false);
        useLeases = config.getBoolean("Global.UseLeaseSystem", false);
        enableBuySell = config.getBoolean("Global.EnableEconomy", false);
        allowEmptyResidences = config.getBoolean("Global.AllowEmptyResidences", true);
        infoToolId = config.getInt("Global.InfoToolId", Material.STRING.getId());
        selectionToolId = config.getInt("Global.SelectionToolId", Material.WOOD_AXE.getId());
        adminOps = config.getBoolean("Global.AdminOPs", true);
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public boolean buySellEnabled() {
        return enableBuySell;
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

}
