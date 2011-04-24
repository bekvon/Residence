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
    private String defaultGroup;
    private boolean useLeases;
    private boolean enableBuySell;
    private boolean adminsOnly;
    private boolean allowEmptyResidences;
    private int infoToolId;
    private int selectionToolId;

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

}
