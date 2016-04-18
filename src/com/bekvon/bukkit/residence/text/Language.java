package com.bekvon.bukkit.residence.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.utils.YmlMaker;

public class Language {
    public FileConfiguration enlocale;
    public FileConfiguration customlocale;
    private Residence plugin;

    public Language(Residence plugin) {
	this.plugin = plugin;
    }

    /**
     * Reloads the config
     */
    public void LanguageReload() {
	customlocale = new YmlMaker((JavaPlugin) plugin, "Language/" + Residence.getConfigManager().getLanguage() + ".yml").getConfig();
	enlocale = new YmlMaker((JavaPlugin) plugin, "Language/English.yml").getConfig();
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
    public String getMessage(String key) {
	return getMessage(key, "");
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the path of the message
     * @param variables
     *            - the variables separated with %
     * @return the message
     */
    
    public String getMessage(String key, Object... variables) {
	if (!key.contains("Language.") && !key.contains("CommandHelp."))
	    key = "Language." + key;
	String missing = "Missing locale for " + key;
	String message = "";
	if (customlocale == null || !customlocale.contains(key))
	    message = enlocale.contains(key) == true ? enlocale.getString(key) : missing;
	message = customlocale.contains(key) == true ? customlocale.getString(key) : missing;

	for (int i = 1; i <= variables.length; i++) {
	    message = message.replace("%" + i, String.valueOf(variables[i - 1]));
	}
	return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the key of the message
     * @return the message
     */
    public String getDefaultMessage(String key) {
	if (!key.contains("Language.") && !key.contains("CommandHelp."))
	    key = "Language." + key;
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
    public List<String> getMessageList(String key) {
	if (!key.contains("Language.") && !key.contains("CommandHelp."))
	    key = "Language." + key;
	String missing = "Missing locale for " + key;
	if (customlocale.isList(key))
	    return ColorsArray(customlocale.getStringList(key));
	return enlocale.getStringList(key).size() > 0 ? ColorsArray(enlocale.getStringList(key)) : Arrays.asList(missing);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key
     *            - the key of the message
     * @return the message
     */
    public Set<String> getKeyList(String key) {
	if (customlocale.isConfigurationSection(key))
	    return customlocale.getConfigurationSection(key).getKeys(false);
	return enlocale.getConfigurationSection(key).getKeys(false);
    }

    /**
     * Check if key exists
     * 
     * @param key
     *            - the key of the message
     * @return true/false
     */
    public boolean containsKey(String key) {
	if (customlocale == null || !customlocale.contains(key))
	    return enlocale.contains(key);
	return customlocale.contains(key);
    }
    
    private static List<String> ColorsArray(List<String> text) {
	List<String> temp = new ArrayList<String>();
	for (String part : text) {
	    temp.add(Colors(part));
	}
	return temp;
    }

    private static String Colors(String text) {
	return ChatColor.translateAlternateColorCodes('&', text);
    }
}
