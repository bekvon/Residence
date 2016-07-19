package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.HelpLines;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
	    Residence.msg(sender, lm.Invalid_Help);
	    return;
	}

	String separator = ChatColor.GOLD + "";
	String simbol = "-";
	if (sender instanceof Player)
	    simbol = "\u25AC";
	for (int i = 0; i < 10; i++) {
	    separator += simbol;
	}

	sender.sendMessage(ChatColor.GOLD + separator + " " + Residence.msg(lm.General_HelpPageHeader, path, page, pagecount) + " " + separator);
	int start = linesPerPage * (page - 1);
	int end = start + linesPerPage;
	for (int i = start; i < end; i++) {
	    if (helplines.size() > i) {

		if (helplines.get(i).getCommand() != null) {
		    HelpEntry sub = this.getSubEntry(helplines.get(i).getCommand());

		    String desc = "";
		    int y = 0;
		    for (String one : sub.lines) {
			desc += ChatColor.YELLOW + one;
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
		    sender.sendMessage(ChatColor.GREEN + " " + helplines.get(i).getDesc());
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
	String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage)
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = !name.equalsIgnoreCase("res") ? "/" + baseCmd + " " + name + " ? " + NextPage : "/" + baseCmd + " ? " + NextPage;
	String next = " {\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
	    + nextCmd + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	if (sender instanceof Player)
	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);
    }

    public void printHelp(CommandSender sender, int page, String path, boolean resadmin) {
	HelpEntry subEntry = this.getSubEntry(path);
	if (subEntry != null) {
	    subEntry.printHelp(sender, page, resadmin, path);
	} else {
	    Residence.msg(sender, lm.Invalid_Help);
	}
    }

    private List<HelpLines> getHelpData(CommandSender sender, boolean resadmin) {
	List<HelpLines> helplines = new ArrayList<HelpLines>();

	for (String one : lines) {
	    helplines.add(new HelpLines(null, one));
	}

	FlagPermissions GlobalFlags = Residence.getPermissionManager().getAllFlags();

	Map<String, String> unsortMap = new HashMap<String, String>();

	for (HelpEntry entry : subentrys) {

	    if (!name.equalsIgnoreCase("flags")) {
		if (ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && !resadmin)
		    continue;

		if (!ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && resadmin)
		    continue;

	    } else {
		if (GlobalFlags.getFlags().containsKey(entry.getName().toLowerCase())) {
		    Boolean state = GlobalFlags.getFlags().get(entry.getName().toLowerCase());
		    if (!state && !resadmin && !sender.hasPermission("residence.flag." + entry.getName().toLowerCase())) {
			continue;
		    }

		    // adding flag name and description for later sorting
		    unsortMap.put(entry.getName(), ChatColor.GREEN + entry.getName() + ChatColor.GOLD + " - " + entry.getDescription());
		    continue;
		}
	    }

	    helplines.add(new HelpLines(entry.getName(), ChatColor.GREEN + entry.getName() + ChatColor.GOLD + " - " + entry.getDescription()));
	}

	if (!unsortMap.isEmpty()) {
	    // Sorting flags help page by alphabet
	    unsortMap = Residence.getSortingManager().sortStringByKeyASC(unsortMap);
	    // Converting HashMap to helplines
	    for (Entry<String, String> one : unsortMap.entrySet()) {
		helplines.add(new HelpLines(one.getKey(), one.getValue()));
	    }
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
		if (key.equalsIgnoreCase("CommandHelp.SubCommands.res")) {
		    subcommandkeys.clear();
		    for (String one : Residence.getCommandFiller().getCommands()) {
			subcommandkeys.add(one);
		    }
		}
		for (String subkey : subcommandkeys) {
		    entry.subentrys.add(HelpEntry.parseHelp(node, key + ".SubCommands." + subkey));
		}
	    }
	}
	return entry;
    }

    @SuppressWarnings("deprecation")
    public Set<String> getSubCommands(CommandSender sender, String[] args) {
	File langFile = new File(new File(Residence.getDataLocation(), "Language"), "English.yml");
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

	    List<String> ArgsList = new ArrayList<String>();

	    int ii = 0;
	    for (Entry<List<String>, List<String>> one : Residence.getLocaleManager().CommandTab.entrySet()) {
		List<String> list = one.getKey();
		if (list.size() > ii && args.length > ii && list.get(ii).equalsIgnoreCase(args[ii])) {
		    ArgsList = one.getValue();
		}
		i++;
	    }

	    String NeededArg = null;
	    if (neededArgPlace < ArgsList.size() && neededArgPlace >= 0)
		NeededArg = ArgsList.get(neededArgPlace);

	    if (NeededArg != null) {

		List<String> list = new ArrayList<String>();

		if (NeededArg.contains("%%")) {
		    list.addAll(Arrays.asList(NeededArg.split("%%")));
		} else
		    list.add(NeededArg);

		for (String oneArg : list) {
		    switch (oneArg) {
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
			    List<ClaimedResidence> resList = Residence.getPlayerManager().getResidencePlayer(((Player) sender)).getResList();
			    for (ClaimedResidence oneRes : resList) {
				subCommands.add(oneRes.getName());
			    }
			} else {
			    ArrayList<String> resList = Residence.getResidenceManager().getResidenceList(Residence.getServerLandname(), true, false, false);
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
			for (Flags one : Flags.values()) {
			    subCommands.add(one.getName());
			}
			break;
		    case "[material]":
			for (Material one : Material.values()) {
			    subCommands.add(one.name().toLowerCase());
			}
			break;
		    case "[materialId]":
			for (Material one : Material.values()) {
			    subCommands.add(String.valueOf(one.getId()));
			}
			break;
		    case "[worldname]":
			for (World one : Bukkit.getWorlds()) {
			    subCommands.add(one.getName());
			}
			break;
		    default:
			subCommands.add(oneArg);
			break;
		    }
		}
	    }

	    String command = tempmeinPath.getCurrentPath().replace("CommandHelp.SubCommands.", "").replace(".SubCommands.", " ");
	    if (subCommands.size() > 0) {
		return subCommands;
	    }

	    if (subCommand)
		return tempmeinPath.getKeys(false);
	    Bukkit.dispatchCommand(sender, command + " ?");
	}
	return new HashSet<String>(Arrays.asList("?"));
    }
}
