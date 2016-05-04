package com.bekvon.bukkit.residence.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.signsStuff.Signs;

public class market implements cmd {

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

	if (args.length == 1) {
	    return false;
	}
	String secondCommand = args[1].toLowerCase();
	if (secondCommand.equals("list")) {
	    return commandResMarketList(args, resadmin, player, page);
	}
	if (secondCommand.equals("autorenew")) {
	    return commandResMarketAutorenew(args, resadmin, player, page);
	}
	if (secondCommand.equals("rentable")) {
	    return commandResMarketRentable(args, resadmin, player, page);
	}
	if (secondCommand.equals("rent")) {
	    return commandResMarketRent(args, resadmin, player, page);
	}
	if (secondCommand.equals("release")) {
	    if (args.length != 3) {
		return false;
	    }
	    if (Residence.getRentManager().isRented(args[2])) {
		Residence.getRentManager().removeFromForRent(player, args[2], resadmin);
	    } else {
		Residence.getRentManager().unrent(player, args[2], resadmin);
	    }
	    return true;
	}
	if (secondCommand.equals("sign")) {
	    if (args.length != 3) {
		return false;
	    }
	    Block block = Residence.getNms().getTargetBlock(player, 10);

	    if (!(block.getState() instanceof Sign)) {
		player.sendMessage(Residence.getLM().getMessage("Sign.LookAt"));
		return true;
	    }

	    Sign sign = (Sign) block.getState();

	    Signs signInfo = new Signs();

	    Signs oldSign = Residence.getSignUtil().getSignFromLoc(sign.getLocation());

	    if (oldSign != null)
		signInfo = oldSign;

	    Location loc = sign.getLocation();

	    String landName = null;

	    ClaimedResidence CurrentRes = Residence.getResidenceManager().getByLoc(sign.getLocation());

	    if (CurrentRes == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    if (!CurrentRes.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
		return true;
	    }

	    final ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    landName = res.getName();

	    boolean ForSale = Residence.getTransactionManager().isForSale(landName);
	    boolean ForRent = Residence.getRentManager().isForRent(landName);

	    int category = 1;
	    if (Residence.getSignUtil().getSigns().GetAllSigns().size() > 0)
		category = Residence.getSignUtil().getSigns().GetAllSigns().get(Residence.getSignUtil().getSigns().GetAllSigns().size() - 1).GetCategory() + 1;

	    if (ForSale || ForRent) {
		signInfo.setCategory(category);
		signInfo.setResidence(landName);
		signInfo.setWorld(loc.getWorld().getName());
		signInfo.setX(loc.getBlockX());
		signInfo.setY(loc.getBlockY());
		signInfo.setZ(loc.getBlockZ());
		signInfo.updateLocation();
		Residence.getSignUtil().getSigns().addSign(signInfo);
		Residence.getSignUtil().saveSigns();
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		return true;
	    }

	    Residence.getSignUtil().CheckSign(res, 5);

	    return true;
	}
	if (secondCommand.equals("info")) {
	    if (args.length == 2) {
		String areaname = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		boolean sell = Residence.getTransactionManager().viewSaleInfo(areaname, player);
		if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isForRent(areaname)) {
		    Residence.getRentManager().printRentInfo(player, areaname);
		} else if (!sell) {
		    sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		}
	    } else if (args.length == 3) {
		boolean sell = Residence.getTransactionManager().viewSaleInfo(args[2], player);
		if (Residence.getConfigManager().enabledRentSystem() && Residence.getRentManager().isForRent(args[2])) {
		    Residence.getRentManager().printRentInfo(player, args[2]);
		} else if (!sell) {
		    sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		}
	    } else {
		return false;
	    }
	    return true;
	}
	if (secondCommand.equals("buy")) {
	    if (args.length != 3) {
		return false;
	    }
	    Residence.getTransactionManager().buyPlot(args[2], player, resadmin);
	    return true;
	}
	if (secondCommand.equals("unsell")) {
	    if (args.length != 3) {
		return false;
	    }
	    Residence.getTransactionManager().removeFromSale(player, args[2], resadmin);
	    return true;
	}
	if (secondCommand.equals("sell")) {
	    if (args.length != 4) {
		return false;
	    }
	    int amount;
	    try {
		amount = Integer.parseInt(args[3]);
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		return true;
	    }
	    Residence.getTransactionManager().putForSale(args[2], player, amount, resadmin);
	    return true;
	}
	return false;
    }

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player, int page) {
	if (args.length < 3 || args.length > 4) {
	    return false;
	}
	boolean repeat = false;
	if (args.length == 4) {
	    if (args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (!args[3].equalsIgnoreCase("f") && !args[3].equalsIgnoreCase("false")) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}
	Residence.getRentManager().rent(player, args[2], repeat, resadmin);
	return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player, int page) {
	if (args.length < 5 || args.length > 6) {
	    return false;
	}
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    player.sendMessage(Residence.getLM().getMessage("Rent.Disabled"));
	    return true;
	}
	int days;
	int cost;
	try {
	    cost = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Cost"));
	    return true;
	}
	try {
	    days = Integer.parseInt(args[4]);
	} catch (Exception ex) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Days"));
	    return true;
	}
	boolean repeat = false;
	if (args.length == 6) {
	    if (args[5].equalsIgnoreCase("t") || args[5].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (!args[5].equalsIgnoreCase("f") && !args[5].equalsIgnoreCase("false")) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}
	Residence.getRentManager().setForRent(player, args[2], cost, days, repeat, resadmin);
	return true;
    }

    private boolean commandResMarketAutorenew(String[] args, boolean resadmin, Player player, int page) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return true;
	}
	if (args.length != 4) {
	    return false;
	}
	boolean value;
	if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t")) {
	    value = true;
	} else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")) {
	    value = false;
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
	    return true;
	}
	if (Residence.getRentManager().isRented(args[2]) && Residence.getRentManager().getRentingPlayer(args[2]).equalsIgnoreCase(player.getName())) {
	    Residence.getRentManager().setRentedRepeatable(player, args[2], value, resadmin);
	} else if (Residence.getRentManager().isForRent(args[2])) {
	    Residence.getRentManager().setRentRepeatable(player, args[2], value, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.RentReleaseInvalid", ChatColor.YELLOW + args[2] + ChatColor.RED));
	}
	return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player, int page) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return true;
	}
	player.sendMessage(Residence.getLM().getMessage("General.MarketList"));
	Residence.getTransactionManager().printForSaleResidences(player);
	if (Residence.getConfigManager().enabledRentSystem()) {
	    Residence.getRentManager().printRentableResidences(player);
	}
	return true;
    }

}
