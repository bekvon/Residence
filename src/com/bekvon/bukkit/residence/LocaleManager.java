package com.bekvon.bukkit.residence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bekvon.bukkit.residence.containers.CommandStatus;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class LocaleManager {

    public static ArrayList<String> FlagList = new ArrayList<String>();
    public HashMap<List<String>, List<String>> CommandTab = new HashMap<List<String>, List<String>>();
    private Residence plugin;

    public String path = "CommandHelp.SubCommands.res.SubCommands.";

    public LocaleManager(Residence plugin) {
	this.plugin = plugin;
    }

    private static YamlConfiguration loadConfiguration(BufferedReader in, String language) {
	Validate.notNull(in, "File cannot be null");
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.load(in);
	} catch (FileNotFoundException ex) {
	} catch (IOException ex) {
	} catch (InvalidConfigurationException ex) {
	    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Residence] Your locale file for " + language
		+ " is incorect! Use http://yaml-online-parser.appspot.com/ to find issue.");
	    return null;
	}

	return config;
    }

    // Language file
    public void LoadLang(String lang) {

	File f = new File(plugin.getDataFolder(), "Language" + File.separator + lang + ".yml");

	BufferedReader in = null;
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}

	if (in == null)
	    return;

	YamlConfiguration conf = loadConfiguration(in, lang);

	if (conf == null) {
	    try {
		in.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    return;
	}

	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	ConfigReader c = new ConfigReader(conf, writer);
	c.getC().options().copyDefaults(true);

	StringBuilder header = new StringBuilder();
	header.append(System.getProperty("line.separator"));
	header.append("NOTE If you want to modify this file, it is HIGHLY recommended that you make a copy");
	header.append(System.getProperty("line.separator"));
	header.append("of this file and modify that instead. This file will be updated automatically by Residence");
	header.append(System.getProperty("line.separator"));
	header.append("when a newer version is detected, and your changes will be overwritten.  Once you ");
	header.append(System.getProperty("line.separator"));
	header.append("have a copy of this file, change the Language: option under the Residence config.yml");
	header.append(System.getProperty("line.separator"));
	header.append("to whatever you named your copy.");
	header.append(System.getProperty("line.separator"));

	c.getW().options().header(header.toString());

	for (lm lm : lm.values()) {
	    if (lm.getText() instanceof String)
		c.get(lm.getPath(), String.valueOf(lm.getText()));
	    else if (lm.getText() instanceof ArrayList<?>) {
		List<String> result = new ArrayList<String>();
		for (Object obj : (ArrayList<?>) lm.getText()) {
		    if (obj instanceof String) {
			result.add((String) obj);
		    }
		}
		c.get(lm.getPath(), result);
	    }

	    if (lm.getComments() != null)
		writer.addComment(lm.getPath(), lm.getComments());
	}

	writer.addComment("CommandHelp", "");

	c.get("CommandHelp.Description", "Contains Help for Residence");
	c.get("CommandHelp.SubCommands.res.Description", "Main Residence Command");
	c.get("CommandHelp.SubCommands.res.Info", Arrays.asList("&2Use &6/res [command] ? <page> &2to view more help Information."));

	for (Entry<String, CommandStatus> cmo : Residence.getCommandFiller().CommandList.entrySet()) {
	    String path = Residence.getLocaleManager().path + cmo.getKey() + ".";
	    try {
		Class<?> cl = Class.forName(Residence.getCommandFiller().packagePath + "." + cmo.getKey());
		if (cmd.class.isAssignableFrom(cl)) {
		    cmd cm = (cmd) cl.getConstructor().newInstance();
		    cm.getLocale(c, path);
		}
	    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
		| NoSuchMethodException | SecurityException e) {
		continue;
	    }
	}

	// custom commands

	c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
	    Arrays.asList("&eUsage: &6/resreload"));

	c.get("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file.");
	c.get("CommandHelp.SubCommands.res.SubCommands.resload.Info",
	    Arrays.asList("&eUsage: &6/resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."));

	c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Description", "Remove all residences from world");
	c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Info",
	    Arrays.asList("&eUsage: &6/res removeworld [worldname]", "Can only be used from console"));

	// Write back config
	try {
	    c.getW().save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    in.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
