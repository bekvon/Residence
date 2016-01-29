package com.bekvon.bukkit.residence.gui;

import java.io.File;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.CommentedYamlConfiguration;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
	this.plugin = plugin;
    }

    public void load() {
	File f = new File(plugin.getDataFolder(), "flags.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	conf.options().copyDefaults(true);
	Set<String> allFlags = conf.getConfigurationSection("Global.FlagPermission").getKeys(false);

	for (String oneFlag : allFlags) {
	    if (!conf.contains("Global.FlagGui." + oneFlag))
		continue;

	    if (!conf.contains("Global.FlagGui." + oneFlag + ".Id"))
		continue;

	    if (!conf.contains("Global.FlagGui." + oneFlag + ".Data"))
		continue;

	    int id = ConfigManager.GetConfig("Global.FlagGui." + oneFlag + ".Id", 35, writer, conf);
	    int data = ConfigManager.GetConfig("Global.FlagGui." + oneFlag + ".Data", 0, writer, conf);

	    @SuppressWarnings("deprecation")
	    Material Mat = Material.getMaterial(id);
	    if (Mat == null)
		Mat = Material.STONE;
	    ItemStack item = new ItemStack(Mat, 1, (short) data);
	    flagData.addFlagButton(oneFlag.toLowerCase(), item);
	}
    }

    public FlagData getFlagData() {
	return flagData;
    }
}
