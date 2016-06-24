package com.bekvon.bukkit.residence.text.help;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.GetTime;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InformationPager {

    public static void printInfo(CommandSender sender, String title, String[] lines, int page) {
	InformationPager.printInfo(sender, title, Arrays.asList(lines), page);
    }

    public static void printInfo(CommandSender sender, String title, List<String> lines, int page) {
	int perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;
	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Invalid.Page"));
	    return;
	}
	sender.sendMessage(Residence.getLM().getMessage("InformationPage.TopLine", title));
	sender.sendMessage(Residence.getLM().getMessage("InformationPage.Page", Residence.getLM().getMessage("General.GenericPages", String.format("%d", page),
	    pagecount, lines.size())));
	for (int i = start; i < end; i++) {
	    if (lines.size() > i)
		sender.sendMessage(ChatColor.GREEN + lines.get(i));
	}
	if (pagecount > page)
	    sender.sendMessage(Residence.getLM().getMessage("InformationPage.NextPage", Residence.getLM().getMessage("General.NextPage")));
	else
	    sender.sendMessage(Residence.getLM().getMessage("InformationPage.NoNextPage"));
    }

    public static void printListInfo(CommandSender sender, String targetPlayer, List<ClaimedResidence> lines, int page, boolean resadmin) {
	if (targetPlayer != null)
	    lines = Residence.getSortingManager().sortResidences(lines);
	int perPage = 20;
	if (sender instanceof Player)
	    perPage = 6;
	int start = (page - 1) * perPage;
	int end = start + perPage;
	int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
	if (pagecount == 0)
	    pagecount = 1;
	if (page > pagecount) {
	    sender.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Invalid.Page"));
	    return;
	}
	if (targetPlayer != null)
	    sender.sendMessage(Residence.getLM().getMessage("InformationPage.TopLine", Residence.getLM().getMessage("General.Residences") + " - " + targetPlayer));
	sender.sendMessage(Residence.getLM().getMessage("InformationPage.Page", Residence.getLM().getMessage("General.GenericPages", String.format("%d", page),
	    pagecount, lines.size())));

	String cmd = "res";
	Debug.D("admin " + resadmin);
	if (resadmin)
	    cmd = "resadmin";

	for (int i = start; i < end; i++) {
	    if (lines.size() <= i)
		break;

	    ClaimedResidence res = lines.get(i);
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(" " + Residence.getLM().getMessage("General.Owner", res.getOwner()));
	    String worldInfo = "";

	    if (res.getPermissions().has("hidden", FlagCombo.FalseOrNone) && res.getPermissions().has("coords", FlagCombo.TrueOrNone) || resadmin) {
		worldInfo += "&6 (&3";
		CuboidArea area = res.getAreaArray()[0];
		worldInfo += Residence.getLM().getMessage("General.CoordsTop", area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc()
		    .getBlockZ());
		worldInfo += "&6; &3";
		worldInfo += Residence.getLM().getMessage("General.CoordsBottom", area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc()
		    .getBlockZ());
		worldInfo += "&6)";
		worldInfo = ChatColor.translateAlternateColorCodes('&', worldInfo);
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n " + Residence.getLM().getMessage("General.CreatedOn", GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + Residence.getLM().getMessage("Residence.IsRented");
		    StringB.append("\n " + Residence.getLM().getMessage("Residence.RentedBy", res.getRentedLand().player));
		} else {
		    ExtraString = " " + Residence.getLM().getMessage("Residence.IsForRent");
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n " + Residence.getLM().getMessage("General.Cost", rentable.cost, rentable.days));
		StringB.append("\n " + Residence.getLM().getMessage("Rentable.AllowRenewing", rentable.AllowRenewing));
		StringB.append("\n " + Residence.getLM().getMessage("Rentable.StayInMarket", rentable.StayInMarket));
		StringB.append("\n " + Residence.getLM().getMessage("Rentable.AllowAutoPay", rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + Residence.getLM().getMessage("Residence.IsForSale");
		StringB.append("\n " + Residence.getLM().getMessage("Economy.LandForSale") + " " + res.getSellPrice());
	    }

	    String tpFlag = "";
	    String moveFlag = "";
	    if (sender instanceof Player && !res.isOwner(sender)) {
		tpFlag = res.getPermissions().playerHas((Player) sender, "tp", true) ? ChatColor.DARK_GREEN + "T" : ChatColor.DARK_RED + "T";
		moveFlag = res.getPermissions().playerHas(sender.getName(), "move", true) ? ChatColor.DARK_GREEN + "M" : ChatColor.DARK_RED + "M";
	    }

	    String msg = Residence.getLM().getMessage("Residence.ResList", (i + 1), res.getName(), res.getWorld(), tpFlag + moveFlag, ExtraString);

	    if (sender instanceof Player)
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + Residence.getResidenceManager().convertToRaw(null, msg,
		    StringB.toString(), cmd + " tp " + res.getName()));
	    else
		sender.sendMessage(msg + " " + StringB.toString().replace("\n", ""));
	}
	if (targetPlayer != null)
	    ShowPagination(sender.getName(), pagecount, page, cmd + " list " + targetPlayer);
	else
	    ShowPagination(sender.getName(), pagecount, page, cmd + " listall");
    }

    public static void ShowPagination(String target, int pageCount, int CurrentPage, String cmd) {
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
	String prev = "\"\",{\"text\":\"" + separator + " " + Residence.getLM().getMessage("General.PrevInfoPage")
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
	    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	String nextCmd = "/" + cmd + " " + NextPage;
	String next = " {\"text\":\"" + Residence.getLM().getMessage("General.NextInfoPage") + " " + separator
	    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\""
	    + nextCmd + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}";

	if (CurrentPage >= pageCount)
	    next = "{\"text\":\"" + Residence.getLM().getMessage("General.NextInfoPage") + " " + separator + "\"}";

	if (CurrentPage <= 1)
	    prev = "{\"text\":\"" + separator + " " + Residence.getLM().getMessage("General.PrevInfoPage") + "\"}";

	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + target + " [" + prev + "," + next + "]");
    }
}
