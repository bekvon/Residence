package com.bekvon.bukkit.residence.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.shopStuff.Board;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.shopStuff.Vote;

public class shop implements cmd {

    @Override
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
		    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
		    return true;
		}
	    } else if (args.length == 3) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    try {
			VotePage = Integer.parseInt(args[2]);
			res = Residence.getResidenceManager().getByLoc(player.getLocation());
			if (res == null) {
			    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
			    return true;
			}
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
			return true;
		    }
		}

	    } else if (args.length == 4) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
		    return true;
		}
		try {
		    VotePage = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
		    return true;
		}
	    }

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
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
		sender.sendMessage(Residence.getLM().getMessage("Shop.NoVotes"));
		return true;
	    }

	    player.sendMessage(Residence.getLM().getMessage("Shop.VotesTopLine", separator, res.getName(), VotePage, pagecount, separator));

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

		String message = Residence.getLM().getMessage("Shop.VotesList", i, one.getName(), (Residence.getConfigManager().isOnlyLike()
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
	    String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLM().getMessage("General.PrevInfoPage")
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	    String nextCmd = "/res shop votes " + res.getName() + " " + NextPage;
	    String next = " {\"text\":\"" + Residence.getLM().getMessage("General.NextInfoPage") + " " + separator
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
		    player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
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
		sender.sendMessage(Residence.getLM().getMessage("Shop.NoVotes"));
		return true;
	    }

	    player.sendMessage(Residence.getLM().getMessage("Shop.ListTopLine", separator, Shoppage, pagecount, separator));

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
		    votestat = vote.getAmount() == 0 ? "" : Residence.getLM().getMessage("Shop.ListLiked", Residence.getShopSignUtilManager().getLikes(one
			.getKey()));
		} else
		    votestat = vote.getAmount() == 0 ? "" : Residence.getLM().getMessage("Shop.ListVoted", vote.getVote(), vote.getAmount());
		ClaimedResidence res = Residence.getResidenceManager().getByName(one.getKey());
		String owner = Residence.getResidenceManager().getByName(one.getKey()).getOwner();
		String message = Residence.getLM().getMessage("Shop.List", i, one.getKey(), owner, votestat);

		String desc = res.getShopDesc() == null ? Residence.getLM().getMessage("Shop.NoDesc") : Residence.getLM().getMessage(
		    "Shop.Desc", ChatColor.translateAlternateColorCodes('&', res.getShopDesc().replace("/n", "\n")));

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
	    String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLM().getMessage("General.PrevInfoPage")
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
	    String nextCmd = "/res shop list " + NextPage;
	    String next = " {\"text\":\"" + Residence.getLM().getMessage("General.NextInfoPage") + " " + separator
		+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
		+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

	    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

	    return true;
	}

	if (args.length == 2 && args[1].equalsIgnoreCase("DeleteBoard")) {

	    if (!resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return true;
	    }

	    ShopListener.Delete.add(player.getName());
	    player.sendMessage(Residence.getLM().getMessage("Shop.DeleteBoard"));
	    return true;
	}
	if (args.length > 2 && args[1].equalsIgnoreCase("setdesc")) {

	    ClaimedResidence res = null;

	    String desc = "";
	    if (args.length >= 2) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
		    return true;
		} else {
		    for (int i = 2; i < args.length; i++) {
			desc += args[i];
			if (i < args.length - 1)
			    desc += " ";
		    }
		}
	    }

	    if (res == null)
		return true;

	    if (!res.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Residence.NonAdmin"));
		return true;
	    }

	    res.setShopDesc(desc);
	    player.sendMessage(Residence.getLM().getMessage("Shop.DescChange", ChatColor.translateAlternateColorCodes('&', desc)));
	    return true;
	}
	if (args.length == 3 && args[1].equalsIgnoreCase("createboard")) {

	    if (!resadmin) {
		player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		return true;
	    }

	    if (!Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		player.sendMessage(Residence.getLM().getMessage("Select.Points"));
		return true;
	    }

	    int place = 1;
	    try {
		place = Integer.parseInt(args[2]);
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
		return true;
	    }

	    if (place < 1)
		place = 1;

	    Location loc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
	    Location loc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());

	    if (loc1.getBlockY() < loc2.getBlockY()) {
		player.sendMessage(Residence.getLM().getMessage("Shop.InvalidSelection"));
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
	    player.sendMessage(Residence.getLM().getMessage("Shop.NewBoard"));

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
			player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
			return true;
		    }
		    vote = Residence.getConfigManager().getVoteRangeTo();

		} else {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
			return true;
		    }

		    try {
			vote = Integer.parseInt(args[2]);
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
			return true;
		    }
		}
	    } else if (args.length == 2 && Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
		    return true;
		}
		vote = Residence.getConfigManager().getVoteRangeTo();
	    } else if (args.length == 4 && !Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByName(args[2]);
		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
		    return true;
		}
	    } else if (args.length == 3 && !Residence.getConfigManager().isOnlyLike()) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		    return true;
		}
		try {
		    vote = Integer.parseInt(args[3]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLM().getMessage("General.UseNumbers"));
		    return true;
		}
	    } else {
		return false;
	    }

	    resName = res.getName();

	    if (!res.getPermissions().has("shop", false)) {
		player.sendMessage(Residence.getLM().getMessage("Shop.CantVote"));
		return true;
	    }

	    if (vote < Residence.getConfigManager().getVoteRangeFrom() || vote > Residence.getConfigManager().getVoteRangeTo()) {
		player.sendMessage(Residence.getLM().getMessage("Shop.VotedRange", Residence.getConfigManager().getVoteRangeFrom(), Residence
		    .getConfigManager().getVoteRangeTo()));
		return true;
	    }

	    ConcurrentHashMap<String, List<ShopVote>> VoteList = Residence.getShopSignUtilManager().GetAllVoteList();

	    if (VoteList.containsKey(resName)) {
		List<ShopVote> list = VoteList.get(resName);
		boolean found = false;
		for (ShopVote OneVote : list) {
		    if (OneVote.getName().equalsIgnoreCase(player.getName())) {

			if (Residence.getConfigManager().isOnlyLike()) {
			    player.sendMessage(Residence.getLM().getMessage("Shop.AlreadyLiked", resName));
			    return true;
			}

			player.sendMessage(Residence.getLM().getMessage("Shop.VoteChanged", OneVote.getVote(), vote, resName));
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
			player.sendMessage(Residence.getLM().getMessage("Shop.Liked", resName));
		    else
			player.sendMessage(Residence.getLM().getMessage("Shop.Voted", vote, resName));
		}
	    } else {
		List<ShopVote> list = new ArrayList<ShopVote>();
		ShopVote newVote = new ShopVote(player.getName(), vote, System.currentTimeMillis());
		list.add(newVote);
		VoteList.put(resName, list);
		if (Residence.getConfigManager().isOnlyLike())
		    player.sendMessage(Residence.getLM().getMessage("Shop.Liked", resName));
		else
		    player.sendMessage(Residence.getLM().getMessage("Shop.Voted", vote, resName));
	    }
	    Residence.getShopSignUtilManager().saveShopVotes();
	    Residence.getShopSignUtilManager().BoardUpdate();
	    return true;
	}
	return false;
    }
}
