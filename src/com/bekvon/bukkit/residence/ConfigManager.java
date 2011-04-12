/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class ConfigManager {

    private boolean worldbuild;
    private boolean worldignite;
    private boolean worldfirespread;
    private boolean worlduse;
    private boolean worlddamage;
    private boolean worldcreeper;
    private boolean worldtnt;
    private boolean worldpvp;
    private String defaultGroup;
    private boolean useLeases;
    private boolean enableBuySell;
    private boolean adminsOnly;
    private boolean allowEmptyResidences;

    public ConfigManager(Configuration config)
    {
        this.load(config);
    }

    public void load(Configuration config) {
        defaultGroup = config.getString("DefaultGroupName", "default");
        adminsOnly = config.getBoolean("AdminsOnly", false);
        worldbuild = config.getBoolean("WorldFlags.build", true);
        worldignite = config.getBoolean("WorldFlags.ignite", true);
        worldfirespread = config.getBoolean("WorldFlags.firespread", true);
        worlduse = config.getBoolean("WorldFlags.use", true);
        worlddamage = config.getBoolean("WorldFlags.damage", true);
        worldcreeper = config.getBoolean("WorldFlags.creeper", true);
        worldtnt = config.getBoolean("WorldFlags.tnt", true);
        worldpvp = config.getBoolean("WorldFlags.pvp", true);
        useLeases = config.getBoolean("UseLeaseSystem", false);
        enableBuySell = config.getBoolean("EnableBuySell", false);
        allowEmptyResidences = config.getBoolean("AllowEmptyResidences", true);
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

    public boolean worldPvpEnabled() {
        return worldpvp;
    }

    public boolean worldDamageEnabled() {
        return worlddamage;
    }
    public boolean worldCreeperEnabled()
    {
        return worldcreeper;
    }
    public boolean worldTNTEnabled()
    {
        return worldtnt;
    }

    public boolean worldUseEnabled() {
        return worlduse;
    }

    public boolean worldFireSpreadEnabled() {
        return worldfirespread;
    }
    public boolean worldIgniteEnabled()
    {
        return worldignite;
    }

    public boolean worldBuildEnabled() {
        return worldbuild;
    }
    public boolean allowEmptyResidences()
    {
        return allowEmptyResidences;
    }

}
