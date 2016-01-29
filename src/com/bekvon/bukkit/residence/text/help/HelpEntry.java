/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.containers.HelpLines;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class HelpEntry {
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    protected static int linesPerPage = 8;

    public HelpEntry(String entryname) {
	name = entryname;
	subentrys = new ArrayList<HelpEntry>();
	lines = new String[0];
    }

    public String getName() {
	if (name == null)
	    return "";
	return name;
    }

    public void setName(String inname) {
	name = inname;
    }

    public void setDescription(String description) {
	desc = description;
    }

    public String getDescription() {
	if (desc == null)
	    return "";
	return desc;
    }

    public void printHelp(CommandSender sender, int page, boolean resadmin, String path) {
	List<HelpLines> helplines = this.getHelpData(sender, resadmin);
	path = "/" + path.replace(".", " ") + " ";
	int pagecount = (int) Math.ceil((double) helplines.size() / (double) linesPerPage);
	if (page > pagecount || page < 1) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidHelp"));
	    return;
	}

	String separator = ChatColor.GOLD + "";
	String simbol = "\u25AC";
	for (int i = 0; i < 5; i++) {
	    separator += simbol;
	}

	sender.sendMessage(ChatColor.GOLD + separator + " " + Residence.getLanguage().getPhrase("HelpPageHeader", ChatColor.YELLOW + path + ChatColor.GOLD + "|"
	    + ChatColor.YELLOW + page + ChatColor.GOLD + "|" + ChatColor.YELLOW + pagecount + ChatColor.GOLD) + " " + separator);
	int start = linesPerPage * (page - 1);
	int end = start + linesPerPage;
	for (int i = start; i < end; i++) {
	    if (helplines.size() > i) {

		if (helplines.get(i).getCommand() != null) {
		    HelpEntry sub = this.getSubEntry(helplines.get(i).getCommand());

		    String desc = "";
		    int y = 0;
		    for (String one : sub.lines) {
			desc += ChatColor.GOLD + one;
			y++;
			if (y < sub.lines.length) {
			    desc += "\n";
			}
		    }

		    if (resadmin)
			path = path.replace("/res ", "/resadmin ");

		    String msg = "[\"\",{\"text\":\"" + ChatColor.GOLD + " " + helplines.get(i).getDesc()
			+ "\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + path + helplines.get(i).getCommand()
			+ " \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + desc + "\"}]}}}]";

		    if (sender instanceof Player)
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + msg);
		    else
			sender.sendMessage(helplines.get(i).getDesc());

		} else
		    sender.sendMessage(ChatColor.GOLD + " " + helplines.get(i).getDesc());
	    }
	}

	if (pagecount == 1)
	    return;

	int NextPage = page + 1;
	NextPage = page < pagecount ? NextPage : page;
	int Prevpage = page - 1;
	Prevpage = page > 1 ? Prevpage : page;

	String baseCmd = resadmin ? "resadmin" : "res";
	String prevCmd = !name.equalsIgnoreCase("res") ? "/" + baseCmd + " " + name + " ? " + Prevpage : "/" + baseCmd + " ? " + Prevpage;
	String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLanguage().getPhrase("PrevInfoPage")
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = !name.equalsIgnoreCase("res") ? "/" + baseCmd + " " + name + " ? " + NextPage : "/" + baseCmd + " ? " + NextPage;
	String next = " {\"text\":\"" + Residence.getLanguage().getPhrase("NextInfoPage") + " " + separator + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
	    + nextCmd + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	if (sender instanceof Player)
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);
    }

    public void printHelp(CommandSender sender, int page, String path, boolean resadmin) {
	HelpEntry subEntry = this.getSubEntry(path);
	if (subEntry != null) {
	    subEntry.printHelp(sender, page, resadmin, path);
	} else {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidHelp"));
	}
    }

    private List<HelpLines> getHelpData(CommandSender sender, boolean resadmin) {
	List<HelpLines> helplines = new ArrayList<HelpLines>();

	for (String one : lines) {
	    helplines.add(new HelpLines(null, one));
	}

//	helplines.addAll(Arrays.asList(lines));
//	if (subentrys.size() > 0)
//	    helplines.add(ChatColor.LIGHT_PURPLE + "---" + Residence.getLanguage().getPhrase("SubCommands") + "---");

	FlagPermissions GlobalFlags = Residence.getPermissionManager().getAllFlags();

	for (HelpEntry entry : subentrys) {

	    if (!name.equalsIgnoreCase("flags")) {
		if (ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && !resadmin)
		    continue;

		if (!ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && resadmin)
		    continue;
	    } else {
		if (GlobalFlags.getFlags().containsKey(entry.getName().toLowerCase())) {
		    Boolean state = GlobalFlags.getFlags().get(entry.getName().toLowerCase());
		    if (!state && !resadmin) {
			continue;
		    }
		}
	    }

	    helplines.add(new HelpLines(entry.getName(), ChatColor.GREEN + entry.getName() + ChatColor.GOLD + " - " + entry.getDescription()));
	}
	return helplines;
    }

    public boolean containesEntry(String name) {
	return this.getSubEntry(name) != null;
    }

    public HelpEntry getSubEntry(String name) {
	String[] split = name.split("\\.");
	HelpEntry entry = this;
	for (String entryname : split) {
	    entry = entry.findSubEntry(entryname);
	    if (entry == null)
		return null;
	}
	return entry;
    }

    private HelpEntry findSubEntry(String name) {
	for (HelpEntry entry : subentrys) {
	    if (entry.getName().equalsIgnoreCase(name))
		return entry;
	}
	return null;
    }

    public void addSubEntry(HelpEntry entry) {
	if (!subentrys.contains(entry)) {
	    subentrys.add(entry);
	}
    }

    public void removeSubEntry(HelpEntry entry) {
	if (subentrys.contains(entry)) {
	    subentrys.remove(entry);
	}
    }

    public int getSubEntryCount() {
	return subentrys.size();
    }

    public static HelpEntry parseHelp(FileConfiguration node, String key) {
	String split[] = key.split("\\.");
	String thisname = split[split.length - 1];
	HelpEntry entry = new HelpEntry(thisname);
	ConfigurationSection keysnode = node.getConfigurationSection(key);
	Set<String> keys = null;
	if (keysnode != null)
	    keys = keysnode.getKeys(false);
	if (keys != null) {
	    if (keys.contains("Info")) {
		List<String> stringList = node.getStringList(key + ".Info");
		if (stringList != null) {
		    entry.lines = new String[stringList.size()];
		    for (int i = 0; i < stringList.size(); i++) {
			entry.lines[i] = ChatColor.translateAlternateColorCodes('&', stringList.get(i));
		    }
		}
	    }
	    if (keys.contains("Description")) {
		entry.desc = node.getString(key + ".Description");
	    }
	    if (keys.contains("SubCommands")) {
		Set<String> subcommandkeys = node.getConfigurationSection(key + ".SubCommands").getKeys(false);
		for (String subkey : subcommandkeys) {
		    entry.subentrys.add(HelpEntry.parseHelp(node, key + ".SubCommands." + subkey));
		}
	    }
	}
	return entry;
    }

//    public static Set<String> getSubCommands(String[] args) {
//	File langFile = new File(new File(Residence.getDataLocation(), "Language"), "English.yml");
//
//	Set<String> subCommands = new HashSet<String>(Arrays.asList(""));
//
//	if (langFile.isFile()) {
//	    FileConfiguration node = new YamlConfiguration();
//	    try {
//		node.load(langFile);
//	    } catch (FileNotFoundException e) {
//		e.printStackTrace();
//	    } catch (IOException e) {
//		e.printStackTrace();
//	    } catch (InvalidConfigurationException e) {
//		e.printStackTrace();
//	    }
//
//	    subCommands = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands").getKeys(false);
//	    ConfigurationSection meinPath = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands");
//
//	    String pathKey = convertArgs(args);
//	    String key = pathKey + ".SubCommands";
//
//	    Debug.D("" + key);
//	    
//	    if (key == "") {
//		return subCommands;
//	    } else {
//		if (meinPath.contains(key)) {
//		    return meinPath.getConfigurationSection(key).getKeys(false);
//		} else {
//		    String[] arg = new String[args.length - 1];
//		    for (int i = 0; i < args.length - 1; i++) {
//			arg[i] = args[i];
//		    }
//		    pathKey = convertArgs(arg);
//		    key = pathKey + ".SubCommands";
//		    if (meinPath.contains(key)) {
//			return meinPath.getConfigurationSection(key).getKeys(false);
//		    }
//		}
//	    }
//
//	}
//	return new HashSet<String>(Arrays.asList("?"));
//    }

    public Set<String> getSubCommands(CommandSender sender, String[] args) {
	File langFile = new File(new File(Residence.getDataLocation(), "Language"), "English.yml");
//
	Set<String> subCommands = new HashSet<String>();

	if (langFile.isFile()) {
	    FileConfiguration node = new YamlConfiguration();
	    try {
		node.load(langFile);
	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (InvalidConfigurationException e) {
		e.printStackTrace();
	    }

	    ConfigurationSection meinPath = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands");
	    ConfigurationSection tempmeinPath = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands");

	    if (args.length == 1)
		return meinPath.getKeys(false);

	    boolean ok = true;
	    int i = 0;
	    while (ok) {

		if (args[i].equalsIgnoreCase(""))
		    return tempmeinPath.getKeys(false);

		if (!tempmeinPath.isConfigurationSection(args[i] + ".SubCommands"))
		    break;

		tempmeinPath = tempmeinPath.getConfigurationSection(args[i] + ".SubCommands");

		i++;
	    }

	    int neededArgPlace = args.length - 2 - i;

	    boolean subCommand = true;
	    if (tempmeinPath.isConfigurationSection(args[i])) {
		subCommand = false;
		tempmeinPath = tempmeinPath.getConfigurationSection(args[i]);
	    }

	    String Args = (tempmeinPath.isString("Args") ? tempmeinPath.getString("Args") : null);

	    List<String> ArgsList = new ArrayList<String>();

	    if (Args != null)
		if (Args.contains(" "))
		    ArgsList.addAll(Arrays.asList(Args.split(" ")));
		else
		    ArgsList.add(Args);

	    String NeededArg = null;
	    if (neededArgPlace < ArgsList.size() && neededArgPlace >= 0)
		NeededArg = ArgsList.get(neededArgPlace);

	    if (NeededArg != null)
		switch (NeededArg) {
		case "[playername]":
		    for (Player one : Bukkit.getOnlinePlayers())
			subCommands.add(one.getName());
		    break;
		case "[residence]":
		    if (sender instanceof Player) {
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
			if (res != null) {
			    String resName = res.getName();
			    if (resName != null)
				subCommands.add(resName);
			}
			ArrayList<String> resList = Residence.getResidenceManager().getResidenceList(((Player) sender).getName(), true, false);
			if (resList.size() > 0)
			    subCommands.addAll(resList);
		    } else {
			ArrayList<String> resList = Residence.getResidenceManager().getResidenceList("Server_land", true, false);
			if (resList.size() > 0)
			    subCommands.addAll(resList);
		    }
		    break;
		case "[cresidence]":
		    if (sender instanceof Player) {
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
			if (res != null) {
			    String resName = res.getName();
			    if (resName != null)
				subCommands.add(resName);
			}
		    }
		    break;
		case "[residenceshop]":
		    subCommands.addAll(Residence.getResidenceManager().getShops());
		    break;
		case "[flag]":
		    FlagPermissions GlobalFlags = Residence.getPermissionManager().getAllFlags();
		    for (Entry<String, Boolean> one : GlobalFlags.getFlags().entrySet()) {
			subCommands.add(one.getKey());
		    }
		    break;
		case "[deposit/withdraw]":
		    subCommands.add("deposit");
		    subCommands.add("withdraw");
		    break;
		case "[enter/leave]":
		    subCommands.add("enter");
		    subCommands.add("leave");
		    break;
		case "[renew/cost]":
		    subCommands.add("renew");
		    subCommands.add("cost");
		    break;
		case "[true/false]":
		    subCommands.add("true");
		    subCommands.add("false");
		    break;
		case "[true/false/remove]":
		    subCommands.add("true");
		    subCommands.add("false");
		    subCommands.add("remove");
		    break;
		case "[blacklist/ignorelist]":
		    subCommands.add("blacklist");
		    subCommands.add("ignorelist");
		    break;
		case "[material]":
		    for (Material one : Material.values()) {
			subCommands.add(one.name().toLowerCase());
		    }
		    break;
		}

	    String command = tempmeinPath.getCurrentPath().replace("CommandHelp.SubCommands.", "").replace(".SubCommands.", " ");
	    if (subCommands.size() > 0) {
		return subCommands;
	    }

	    if (subCommand)
		return tempmeinPath.getKeys(false);
	    else {
		Bukkit.dispatchCommand(sender, command + " ?");
	    }
	}
	return new HashSet<String>(Arrays.asList("?"));
    }
}
