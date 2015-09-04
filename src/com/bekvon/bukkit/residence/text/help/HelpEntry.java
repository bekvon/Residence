/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Administrator
 */
public class HelpEntry {
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    protected static int linesPerPage = 7;

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

    public static int getLinesPerPage() {
	return linesPerPage;
    }

    public static void setLinesPerPage(int lines) {
	linesPerPage = lines;
    }

    public void printHelp(CommandSender sender, int page) {
	List<String> helplines = this.getHelpData();
	int pagecount = (int) Math.ceil((double) helplines.size() / (double) linesPerPage);
	if (page > pagecount || page < 1) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidHelp"));
	    return;
	}
	sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("HelpPageHeader", ChatColor.YELLOW + name + ChatColor.RED + "." + ChatColor.YELLOW + page
	    + ChatColor.RED + "." + ChatColor.YELLOW + pagecount + ChatColor.RED));
	sender.sendMessage(ChatColor.DARK_AQUA + Residence.getLanguage().getPhrase("Description") + ": " + ChatColor.GREEN + desc);
	int start = linesPerPage * (page - 1);
	int end = start + linesPerPage;
	boolean alternatecolor = false;
	for (int i = start; i < end; i++) {
	    if (helplines.size() > i) {
		if (alternatecolor) {
		    sender.sendMessage(ChatColor.YELLOW + helplines.get(i));
		    alternatecolor = false;
		} else {
		    sender.sendMessage(ChatColor.GOLD + helplines.get(i));
		    alternatecolor = true;
		}
	    }
	}

	int NextPage = page + 1;
	NextPage = page < pagecount ? NextPage : page;
	int Prevpage = page - 1;
	Prevpage = page > 1 ? Prevpage : page;
	String prevCmd = !name.equalsIgnoreCase("res") ? "/res " + name + " ? " + Prevpage : "/res ? " + Prevpage;
	String prev = "[\"\",{\"text\":\"" + Residence.getLanguage().getPhrase("PrevInfoPage") + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = !name.equalsIgnoreCase("res") ? "/res " + name + " ? " + NextPage : "/res ? " + NextPage;
	String next = " {\"text\":\"" + Residence.getLanguage().getPhrase("NextInfoPage") + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

//	if (page < pagecount)
//	    sender.sendMessage(ChatColor.GRAY + "---<" + Residence.getLanguage().getPhrase("NextPage") + ">---");
//	else
//	    sender.sendMessage(ChatColor.GRAY + "-----------------------");
    }

    public void printHelp(CommandSender sender, int page, String path) {
	HelpEntry subEntry = this.getSubEntry(path);
	if (subEntry != null) {
	    subEntry.printHelp(sender, page);
	} else {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidHelp"));
	}
    }

    private List<String> getHelpData() {
	List<String> helplines = new ArrayList<String>();
	helplines.addAll(Arrays.asList(lines));
	if (subentrys.size() > 0)
	    helplines.add(ChatColor.LIGHT_PURPLE + "---" + Residence.getLanguage().getPhrase("SubCommands") + "---");
	for (HelpEntry entry : subentrys) {
	    helplines.add(ChatColor.GREEN + entry.getName() + ChatColor.YELLOW + " - " + entry.getDescription());
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
			entry.lines[i] = "- " + ChatColor.translateAlternateColorCodes('&', stringList.get(i));
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

}
