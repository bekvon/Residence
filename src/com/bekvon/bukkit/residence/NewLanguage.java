package com.bekvon.bukkit.residence;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class NewLanguage {
    public static FileConfiguration enlocale;
    public static FileConfiguration customlocale;

    static {
	customlocale = new YmlMaker((JavaPlugin) Residence.instance, "Language/" + Residence.getConfigManager().language + ".yml").getConfig();
	enlocale = new YmlMaker((JavaPlugin) Residence.instance, "Language/English.yml").getConfig();
	if (customlocale == null)
	    customlocale = enlocale;
    }

    private NewLanguage() {
    }

    /**
     * Reloads the config
     */
    public static void reload() {
	customlocale = new YmlMaker((JavaPlugin) Residence.instance, "Language/" + Residence.getConfigManager().language + ".yml").getConfig();
	enlocale = new YmlMaker((JavaPlugin) Residence.instance, "Language/English.yml").getConfig();
	if (customlocale == null)
	    customlocale = enlocale;
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the key of the message
     * @return the message
     */
    public static String getMessage(String key) {
	String missing = "Missing locale for " + key;
	if (customlocale == null || !customlocale.contains(key))
	    return enlocale.contains(key) == true ? ChatColor.translateAlternateColorCodes('&', enlocale.getString(key)) : missing;
	return customlocale.contains(key) == true ? ChatColor.translateAlternateColorCodes('&', customlocale.getString(key)) : missing;
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the key of the message
     * @return the message
     */
    public static String getDefaultMessage(String key) {
	String missing = "Missing locale for " + key;
	return enlocale.contains(key) == true ? ChatColor.translateAlternateColorCodes('&', enlocale.getString(key)) : missing;
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the key of the message
     * @return the message
     */
    public static List<String> getMessageList(String key) {
	String missing = "Missing locale for " + key;
	if (customlocale.isList(key))
	    return Locale.ColorsArray(customlocale.getStringList(key), true);
	return enlocale.getStringList(key).size() > 0 ? Locale.ColorsArray(enlocale.getStringList(key), true) : Arrays.asList(missing);
    }

    /**
     * Check if key exists
     * 
     * @param key
     *            - the key of the message
     * @return true/false
     */
    public static boolean containsKey(String key) {
	if (customlocale == null || !customlocale.contains(key))
	    return enlocale.contains(key);
	return customlocale.contains(key);
    }
}
