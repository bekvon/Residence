package com.bekvon.bukkit.residence.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.shopStuff.Board;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.shopStuff.Vote;

public class shop implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1700)
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}

	if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("votes") || args[1].equalsIgnoreCase("likes"))) {

	    int VotePage = 1;

	    ClaimedResidence res = null;
	    if (args.length == 2) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    Residence.msg(player, lm.Residence_NotIn);
		    return true;
		}
	    } else if (args.length == 3) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    try {
			VotePage = Integer.parseInt(args[2]);
			res = Residence.getResidenceManager().getByLoc(player.getLocation());
			if (res == null) {
			    Residence.msg(player, lm.Residence_NotIn);
			    return true;
			}
		    } catch (Exception ex) {
			Residence.msg(player, lm.General_UseNumbers);
			return true;
		    }
		}

	    } else if (args.length == 4) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    Residence.msg(player, lm.Residence_NotIn);
		    return true;
		}
		try {
		    VotePage = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    Residence.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    }

	    if (res == null) {
		Residence.msg(player, lm.Residence_NotIn);
		return true;
	    }

	    Map<String, List<ShopVote>> ShopList = Residence.getShopSignUtilManager().GetAllVoteList();

	    List<ShopVote> VoteList = new ArrayList<ShopVote>();
	    if (ShopList.containsKey(res.getName())) {
		VoteList = ShopList.get(res.getName());
	    }

	    String separator = ChatColor.GOLD + "";
	    String simbol = "\u25AC";
	    for (int i = 0; i < 5; i++) {
		separator += simbol;
	    }
	    int pagecount = (int) Math.ceil((double) VoteList.size() / (double) 10);
	    if (page > pagecount || page < 1) {
		Residence.msg(sender, lm.Shop_NoVotes);
		return true;
	    }

	    Residence.msg(player, lm.Shop_VotesTopLine, separator, res.getName(), VotePage, pagecount, separator);

	    int start = VotePage * 10 - 9;
	    int end = VotePage * 10 + 1;
	    int position = 0;
	    int i = start;
	    for (ShopVote one : VoteList) {
		position++;

		if (position < start)
		    continue;

		if (position >= end)
		    break;

		Date dNow = new Date(one.getTime());
		SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
		ft.setTimeZone(TimeZone.getTimeZone(Residence.getConfigManager().getTimeZone()));
		String timeString = ft.format(dNow);

		String message = Residence.msg(lm.Shop_VotesList, i, one.getName(), (Residence.getConfigManager().isOnlyLike()
		    ? "" : one.getVote()), timeString);
		player.sendMessage(message);
		i++;
	    }

	    if (pagecount == 1)
		return true;

	    int NextPage = page + 1;
	    NextPage = page < pagecount ? NextPage : page;
	    int Prevpage = page - 1;
	    Prevpage = page > 1 ? Prevpage : page;

	    String prevCmd = "/res shop votes " + res.getName() + " " + Prevpage;
	    String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage)
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	    String nextCmd = "/res shop votes " + res.getName() + " " + NextPage;
	    String next = " {\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

	    return true;
	}
	if ((args.length == 2 || args.length == 3) && args[1].equalsIgnoreCase("list")) {

	    int Shoppage = 1;

	    if (args.length == 3) {
		try {
		    Shoppage = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    Residence.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    }

	    Map<String, Double> ShopList = Residence.getShopSignUtilManager().getSortedShopList();

	    String separator = ChatColor.GOLD + "";
	    String simbol = "\u25AC";
	    for (int i = 0; i < 5; i++) {
		separator += simbol;
	    }
	    int pagecount = (int) Math.ceil((double) ShopList.size() / (double) 10);
	    if (page > pagecount || page < 1) {
		Residence.msg(sender, lm.Shop_NoVotes);
		return true;
	    }

	    Residence.msg(player, lm.Shop_ListTopLine, separator, Shoppage, pagecount, separator);

	    int start = Shoppage * 10 - 9;
	    int end = Shoppage * 10 + 1;
	    int position = 0;
	    int i = start;
	    for (Entry<String, Double> one : ShopList.entrySet()) {
		position++;

		if (position < start)
		    continue;

		if (position >= end)
		    break;

		Vote vote = Residence.getShopSignUtilManager().getAverageVote(one.getKey());
		String votestat = "";

		if (Residence.getConfigManager().isOnlyLike()) {
		    votestat = vote.getAmount() == 0 ? "" : Residence.msg(lm.Shop_ListLiked, Residence.getShopSignUtilManager().getLikes(one
			.getKey()));
		} else
		    votestat = vote.getAmount() == 0 ? "" : Residence.msg(lm.Shop_ListVoted, vote.getVote(), vote.getAmount());
		ClaimedResidence res = Residence.getResidenceManager().getByName(one.getKey());
		String owner = Residence.getResidenceManager().getByName(one.getKey()).getOwner();
		String message = Residence.msg(lm.Shop_List, i, one.getKey(), owner, votestat);

		String desc = res.getShopDesc() == null ? Residence.msg(lm.Shop_NoDesc) : Residence.msg(
		    lm.Shop_Desc, ChatColor.translateAlternateColorCodes('&', res.getShopDesc().replace("/n", "\n")));

		String prev = "[\"\",{\"text\":\"" + " " + message
		    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/res tp " + one.getKey()
		    + " \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + desc + "\"}]}}}]";

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev);

		i++;
	    }

	    if (pagecount == 1)
		return true;

	    int NextPage = page + 1;
	    NextPage = page < pagecount ? NextPage : page;
	    int Prevpage = page - 1;
	    Prevpage = page > 1 ? Prevpage : page;

	    String prevCmd = "/res shop list " + Prevpage;
	    String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.msg(lm.General_PrevInfoPage)
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	    String nextCmd = "/res shop list " + NextPage;
	    String next = " {\"text\":\"" + Residence.msg(lm.General_NextInfoPage) + " " + separator
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

	    return true;
	}

	if (args.length == 2 && args[1].equalsIgnoreCase("DeleteBoard")) {

	    if (!resadmin) {
		Residence.msg(player, lm.General_NoPermission);
		return true;
	    }

	    ShopListener.Delete.add(player.getName());
	    Residence.msg(player, lm.Shop_DeleteBoard);
	    return true;
	}
	if (args.length > 2 && args[1].equalsIgnoreCase("setdesc")) {

	    ClaimedResidence res = null;

	    String desc = "";
	    if (args.length >= 2) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    Residence.msg(player, lm.Residence_NotIn);
		    return true;
		}
		for (int i = 2; i < args.length; i++) {
		    desc += args[i];
		    if (i < args.length - 1)
			desc += " ";
		}
	    }

	    if (res == null)
		return true;

	    if (!res.isOwner(player) && !resadmin) {
		Residence.msg(player, lm.Residence_NonAdmin);
		return true;
	    }

	    res.setShopDesc(desc);
	    Residence.msg(player, lm.Shop_DescChange, ChatColor.translateAlternateColorCodes('&', desc));
	    return true;
	}
	if (args.length == 3 && args[1].equalsIgnoreCase("createboard")) {

	    if (!resadmin) {
		Residence.msg(player, lm.General_NoPermission);
		return true;
	    }

	    if (!Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		Residence.msg(player, lm.Select_Points);
		return true;
	    }

	    int place = 1;
	    try {
		place = Integer.parseInt(args[2]);
	    } catch (Exception ex) {
		Residence.msg(player, lm.General_UseNumbers);
		return true;
	    }

	    if (place < 1)
		place = 1;

	    Location loc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
	    Location loc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());

	    if (loc1.getBlockY() < loc2.getBlockY()) {
		Residence.msg(player, lm.Shop_InvalidSelection);
		return true;
	    }

	    Board newTemp = new Board();
	    newTemp.setStartPlace(place);
	    newTemp.setWorld(loc1.getWorld().getName());
	    newTemp.setTX(loc1.getBlockX());
	    newTemp.setTY(loc1.getBlockY());
	    newTemp.setTZ(loc1.getBlockZ());
	    newTemp.setBX(loc2.getBlockX());
	    newTemp.setBY(loc2.getBlockY());
	    newTemp.setBZ(loc2.getBlockZ());

	    newTemp.GetTopLocation();
	    newTemp.GetBottomLocation();

	    newTemp.GetLocations();

	    Residence.getShopSignUtilManager().addBoard(newTemp);
	    Residence.msg(player, lm.Shop_NewBoard);

	    Residence.getShopSignUtilManager().BoardUpdate();
	    Residence.getShopSignUtilManager().saveSigns();

	    return true;

	}
	if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("vote") || args[1].equalsIgnoreCase("like"))) {
	    String resName = "";
	    int vote = 5;
	    ClaimedResidence res = null;
	    if (args.length == 3) {

		if (Residence.getConfigManager().isOnlyLike()) {

		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			Residence.msg(player, lm.Invalid_Residence);
			return true;
		    }
		    vote = Residence.getConfigManager().getVoteRangeTo();

		} else {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			Residence.msg(player, lm.Residence_NotIn);
			return true;
		    }

		    try {
			vote = Integer.parseInt(args[2]);
		    } catch (Exception ex) {
			Residence.msg(player, lm.General_UseNumbers);
			return true;
		    }
		}
	    } else if (args.length == 2 && Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    Residence.msg(player, lm.Residence_NotIn);
		    return true;
		}
		vote = Residence.getConfigManager().getVoteRangeTo();
	    } else if (args.length == 4 && !Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    Residence.msg(player, lm.Invalid_Residence);
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    Residence.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    } else if (args.length == 3 && !Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    Residence.msg(player, lm.Invalid_Residence);
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    Residence.msg(player, lm.General_UseNumbers);
		    return true;
		}
	    } else {
		return false;
	    }

	    resName = res.getName();

	    if (!res.getPermissions().has("shop", false)) {
		Residence.msg(player, lm.Shop_CantVote);
		return true;
	    }

	    if (vote < Residence.getConfigManager().getVoteRangeFrom() || vote > Residence.getConfigManager().getVoteRangeTo()) {
		Residence.msg(player, lm.Shop_VotedRange, Residence.getConfigManager().getVoteRangeFrom(), Residence
		    .getConfigManager().getVoteRangeTo());
		return true;
	    }

	    ConcurrentHashMap<String, List<ShopVote>> VoteList = Residence.getShopSignUtilManager().GetAllVoteList();

	    if (VoteList.containsKey(resName)) {
		List<ShopVote> list = VoteList.get(resName);
		boolean found = false;
		for (ShopVote OneVote : list) {
		    if (OneVote.getName().equalsIgnoreCase(player.getName())) {

			if (Residence.getConfigManager().isOnlyLike()) {
			    Residence.msg(player, lm.Shop_AlreadyLiked, resName);
			    return true;
			}

			Residence.msg(player, lm.Shop_VoteChanged, OneVote.getVote(), vote, resName);
			OneVote.setVote(vote);
			OneVote.setTime(System.currentTimeMillis());
			found = true;
			break;
		    }
		}
		if (!found) {
		    ShopVote newVote = new ShopVote(player.getName(), vote, System.currentTimeMillis());
		    list.add(newVote);

		    if (Residence.getConfigManager().isOnlyLike())
			Residence.msg(player, lm.Shop_Liked, resName);
		    else
			Residence.msg(player, lm.Shop_Voted, vote, resName);
		}
	    } else {
		List<ShopVote> list = new ArrayList<ShopVote>();
		ShopVote newVote = new ShopVote(player.getName(), vote, System.currentTimeMillis());
		list.add(newVote);
		VoteList.put(resName, list);
		if (Residence.getConfigManager().isOnlyLike())
		    Residence.msg(player, lm.Shop_Liked, resName);
		else
		    Residence.msg(player, lm.Shop_Voted, vote, resName);
	    }
	    Residence.getShopSignUtilManager().saveShopVotes();
	    Residence.getShopSignUtilManager().BoardUpdate();
	    return true;
	}
	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Manage residence shop");
	c.get(path + "Info", Arrays.asList("Manages residence shop feature"));

	// Sub commands
	path += "SubCommands.";
	c.get(path + "list.Description", "Shows list of res shops");
	c.get(path + "list.Info", Arrays.asList("&eUsage: &6/res shop list", "Shows full list of all residences with shop flag"));

	c.get(path + "vote.Description", "Vote for residence shop");
	c.get(path + "vote.Info", Arrays.asList("&eUsage: &6/res shop vote <residence> [amount]", "Votes for current or defined residence"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "vote"), Arrays.asList("[residence]", "10"));

	c.get(path + "like.Description", "Give like for residence shop");
	c.get(path + "like.Info", Arrays.asList("&eUsage: &6/res shop like <residence>", "Gives like for residence shop"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "like"), Arrays.asList("[residenceshop]"));

	c.get(path + "votes.Description", "Shows res shop votes");
	c.get(path + "votes.Info", Arrays.asList("&eUsage: &6/res shop votes <residence> <page>", "Shows full vote list of current or defined residence shop"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "votes"), Arrays.asList("[residenceshop]"));

	c.get(path + "likes.Description", "Shows res shop likes");
	c.get(path + "likes.Info", Arrays.asList("&eUsage: &6/res shop likes <residence> <page>", "Shows full like list of current or defined residence shop"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "likes"), Arrays.asList("[residenceshop]"));

	c.get(path + "setdesc.Description", "Sets residence shop description");
	c.get(path + "setdesc.Info", Arrays.asList("&eUsage: &6/res shop setdesc [text]", "Sets residence shop description. Color code supported. For new line use /n"));

	c.get(path + "createboard.Description", "Create res shop board");
	c.get(path + "createboard.Info", Arrays.asList("&eUsage: &6/res shop createboard [place]",
	    "Creates res shop board from selected area. Place - position from which to start filling board"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "createboard"), Arrays.asList("1"));

	c.get(path + "deleteboard.Description", "Deletes res shop board");
	c.get(path + "deleteboard.Info", Arrays.asList("&eUsage: &6/res shop deleteboard", "Deletes res shop board bi right clicking on one of signs"));
    }
}
