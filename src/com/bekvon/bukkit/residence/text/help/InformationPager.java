package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.GetTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InformationPager {
    Residence plugin;

    public InformationPager(Residence plugin) {
	this.plugin = plugin;
    }

    public void printInfo(CommandSender sender, String title, String[] lines, int page) {
	printInfo(sender, title, Arrays.asList(lines), page);
    }

    public void printInfo(CommandSender sender, String title, List<String> lines, int page) {
	int perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;
	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + Residence.msg(lm.Invalid_Page));
	    return;
	}
	Residence.msg(sender, lm.InformationPage_TopLine, title);
	Residence.msg(sender, lm.InformationPage_Page, Residence.msg(lm.General_GenericPages, String.format("%d", page),
	    pagecount, lines.size()));
	for (int i = start; i < end; i++) {
	    if (lines.size() > i)
		sender.sendMessage(ChatColor.GREEN + lines.get(i));
	}
	if (pagecount > page)
	    Residence.msg(sender, lm.InformationPage_NextPage, Residence.msg(lm.General_NextPage));
	else
	    Residence.msg(sender, lm.InformationPage_NoNextPage);
    }

    public void printListInfo(CommandSender sender, String targetPlayer, List<ClaimedResidence> lines, int page, boolean resadmin) {
	if (targetPlayer != null)
	    lines = Residence.getSortingManager().sortResidences(lines);
	int perPage = 20;
	if (sender instanceof Player)
	    perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;

	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (page == -1) {
	    start = 0;
	    end = lines.size();
	    page = 1;
	    pagecount = 1;
	}

	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + Residence.msg(lm.Invalid_Page));
	    return;
	}
	if (targetPlayer != null)
	    Residence.msg(sender, lm.InformationPage_TopLine, Residence.msg(lm.General_Residences) + " - " + targetPlayer);
	Residence.msg(sender, lm.InformationPage_Page, Residence.msg(lm.General_GenericPages, String.format("%d", page),
	    pagecount, lines.size()));

	String cmd = "res";
	if (resadmin)
	    cmd = "resadmin";

	lines = lines.subList(start, end);

	Debug.D("Line Amount " + lines.size());

	if (!(sender instanceof Player)) {
	    printListWithDelay(sender, lines, start, resadmin);
	    return;
	}

	List<String> linesForConsole = new ArrayList<String>();
	int i = start;
	for (ClaimedResidence res : lines) {
	    i++;
//	    if (lines.size() <= i)
//		break;

//	    ClaimedResidence res = lines.get(i);
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(" " + Residence.msg(lm.General_Owner, res.getOwner()));
	    String worldInfo = "";

	    if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
		worldInfo += "&6 (&3";
		CuboidArea area = res.getAreaArray()[0];
		worldInfo += Residence.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc()
		    .getBlockZ());
		worldInfo += "&6; &3";
		worldInfo += Residence.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc()
		    .getBlockZ());
		worldInfo += "&6)";
		worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n " + Residence.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + Residence.msg(lm.Residence_IsRented);
		    StringB.append("\n " + Residence.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + Residence.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n " + Residence.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n " + Residence.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n " + Residence.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n " + Residence.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + Residence.msg(lm.Residence_IsForSale);
		StringB.append("\n " + Residence.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String tpFlag = "";
	    String moveFlag = "";
	    if (sender instanceof Player && !res.isOwner(sender)) {
		tpFlag = res.getPermissions().playerHas((Player) sender, "tp", true) ? ChatColor.DARK_GREEN + "T" : ChatColor.DARK_RED + "T";
		moveFlag = res.getPermissions().playerHas(sender.getName(), "move", true) ? ChatColor.DARK_GREEN + "M" : ChatColor.DARK_RED + "M";
	    }

	    String msg = Residence.msg(lm.Residence_ResList, (i + 1), res.getName(), res.getWorld(), tpFlag + moveFlag, ExtraString);

	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + Residence.getResidenceManager().convertToRaw(null, msg,
		    StringB.toString(), cmd + " tp " + res.getName()));
	    else {
		linesForConsole.add(msg + " " + StringB.toString().replace("\n", ""));
	    }
	}

	if (targetPlayer != null)
	    ShowPagination(sender.getName(), pagecount, page, cmd + " list " + targetPlayer);
	else
	    ShowPagination(sender.getName(), pagecount, page, cmd + " listall");
    }

    private void printListWithDelay(final CommandSender sender, final List<ClaimedResidence> lines, final int start, final boolean resadmin) {

	for (int i = 0; i < 100; i++) {
	    if (lines.size() <= i)
		break;

	    ClaimedResidence res = lines.get(i);
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(" " + Residence.msg(lm.General_Owner, res.getOwner()));
	    String worldInfo = "";

	    if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
		worldInfo += "&6 (&3";
		CuboidArea area = res.getAreaArray()[0];
		worldInfo += Residence.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc()
		    .getBlockZ());
		worldInfo += "&6; &3";
		worldInfo += Residence.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc()
		    .getBlockZ());
		worldInfo += "&6)";
		worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n " + Residence.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + Residence.msg(lm.Residence_IsRented);
		    StringB.append("\n " + Residence.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + Residence.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n " + Residence.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n " + Residence.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n " + Residence.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n " + Residence.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + Residence.msg(lm.Residence_IsForSale);
		StringB.append("\n " + Residence.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String msg = Residence.msg(lm.Residence_ResList, (start + i + 1), res.getName(), res.getWorld(), "", ExtraString);

	    msg = ChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
	    msg = msg.replaceAll("\\s{2}", " ");
	    sender.sendMessage(msg);
	}

	final List<ClaimedResidence> tlines = new ArrayList<ClaimedResidence>();

	if (lines.size() > 100)
	    tlines.addAll(lines.subList(100, lines.size()));

	if (tlines.isEmpty()) {
	    return;
	}

	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		printListWithDelay(sender, tlines, start + 100, resadmin);
		return;
	    }
	}, 5L);

    }

    public void ShowPagination(String target, int pageCount, int CurrentPage, String cmd) {
	if (target.equalsIgnoreCase("console"))
	    return;
	String separator = ChatColor.GOLD + "";
	String simbol = "\u25AC";
	for (int i = 0; i < 10; i++) {
	    separator += simbol;
	}

	if (pageCount == 1)
	    return;

	int NextPage = CurrentPage + 1;
	NextPage = CurrentPage < pageCount ? NextPage : CurrentPage;
	int Prevpage = CurrentPage - 1;
	Prevpage = CurrentPage > 1 ? Prevpage : CurrentPage;

	String prevCmd = "/" + cmd + " " + Prevpage;
	String prev = "\"\",{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage)
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = "/" + cmd + " " + NextPage;
	String next = " {\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
	    + nextCmd + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}";

	if (CurrentPage >= pageCount)
	    next = "{\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator + "\"}";

	if (CurrentPage <= 1)
	    prev = "{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage) + "\"}";

	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + target + " [" + prev + "," + next + "]");
    }
}
