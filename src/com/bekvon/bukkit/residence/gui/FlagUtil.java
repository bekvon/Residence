package com.bekvon.bukkit.residence.gui;

import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.ItemManager.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
	this.plugin = plugin;
    }

    public void load() {
	ConfigReader c = null;
	try {
	    c = new ConfigReader("flags.yml");
	} catch (Exception e1) {
	    e1.printStackTrace();
	}

	if (c != null) {
	    if (!c.getC().isConfigurationSection("Global.FlagPermission"))
		return;

	    Set<String> allFlags = c.getC().getConfigurationSection("Global.FlagPermission").getKeys(false);

	    for (String oneFlag : allFlags) {
		if (!c.getC().contains("Global.FlagGui." + oneFlag))
		    continue;
		String value = c.get("Global.FlagGui." + oneFlag, "WHITE_WOOL");
		value = value.replace("-", ":");
		CMIMaterial Mat = CMIMaterial.get(value);
		if (Mat == null) {
		    Residence.getInstance().consoleMessage(value);
		    Mat = CMIMaterial.STONE;
		}
		ItemStack item = Mat.newItemStack();
		flagData.addFlagButton(oneFlag.toLowerCase(), item);
	    }
	}
    }

    public FlagData getFlagData() {
	return flagData;
    }
}
