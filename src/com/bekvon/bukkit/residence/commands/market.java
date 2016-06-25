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
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
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

	switch (secondCommand.toLowerCase()) {

	case "list":
	    return commandResMarketList(args, player, page);
	case "autopay":
	    return commandResMarketAutoPay(args, resadmin, player);
	case "payrent":
	    return commandResMarketPayRent(args, resadmin, player);
	case "rentable":
	    return commandResMarketRentable(args, resadmin, player);
	case "rent":
	    return commandResMarketRent(args, resadmin, player);
	case "release":
	case "unrent":
	    if (args.length != 3 && args.length != 2)
		return false;

	    ClaimedResidence res = null;

	    if (args.length == 2)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else
		res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    Residence.UnrentConfirm.put(player.getName(), res.getName());

	    if (res.isRented()) {
		if (resadmin || Residence.isResAdminOn(player))
		    sender.sendMessage(Residence.getLM().getMessage("Rent.EvictConfirm", res.getName()));
		else if (Residence.getRentManager().getRentingPlayer(res).equalsIgnoreCase(sender.getName()))
		    sender.sendMessage(Residence.getLM().getMessage("Rent.UnrentConfirm", res.getName()));
		else
		    Residence.getRentManager().printRentInfo(player, res);
	    } else
		sender.sendMessage(Residence.getLM().getMessage("Rent.ReleaseConfirm", res.getName()));

	    return true;

	case "confirm":
	    if (!Residence.UnrentConfirm.containsKey(player.getName())) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return false;
	    }
	    String area = Residence.UnrentConfirm.remove(player.getName());
	    res = Residence.getResidenceManager().getByName(area);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    if (!res.isRented()) {
		Residence.getRentManager().removeFromForRent(player, res, resadmin);
		ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
		if (rPlayer != null && res != null && rPlayer.getMainResidence() == res) {
		    rPlayer.setMainResidence(null);
		}
	    } else
		Residence.getRentManager().unrent(player, area, resadmin);
	    return true;
	case "sign":
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

	    ClaimedResidence CurrentRes = Residence.getResidenceManager().getByLoc(sign.getLocation());

	    if (CurrentRes != null && !CurrentRes.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotOwner"));
		return true;
	    }

	    res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    boolean ForSale = res.isForSell();
	    boolean ForRent = res.isForRent();

	    int category = 1;
	    if (Residence.getSignUtil().getSigns().GetAllSigns().size() > 0)
		category = Residence.getSignUtil().getSigns().GetAllSigns().get(Residence.getSignUtil().getSigns().GetAllSigns().size() - 1).GetCategory() + 1;

	    if (ForSale || ForRent) {
		signInfo.setCategory(category);
		signInfo.setResidence(res);
		signInfo.setLocation(loc);
		Residence.getSignUtil().getSigns().addSign(signInfo);
		Residence.getSignUtil().saveSigns();
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		return true;
	    }

	    Residence.getSignUtil().CheckSign(res, 5);

	    return true;

	case "info":
	    res = null;
	    if (args.length == 2)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else if (args.length == 3)
		res = Residence.getResidenceManager().getByName(args[2]);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }
	    boolean sell = Residence.getTransactionManager().viewSaleInfo(res, player);
	    if (Residence.getConfigManager().enabledRentSystem() && res.isForRent()) {
		Residence.getRentManager().printRentInfo(player, res);
	    } else if (!sell) {
		sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
	    }
	    return true;
	case "buy":
	    res = null;
	    if (args.length == 2)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else if (args.length == 3)
		res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    sell = Residence.getTransactionManager().viewSaleInfo(res, player);
	    if (sell) {
		Residence.getTransactionManager().buyPlot(res, player, resadmin);
	    } else {
		sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
	    }
	    return true;
	case "unsell":
	    if (args.length != 3)
		return false;

	    Residence.getTransactionManager().removeFromSale(player, args[2], resadmin);
	    return true;

	case "sell":
	    if (args.length != 4)
		return false;

	    int amount;
	    try {
		amount = Integer.parseInt(args[3]);
	    } catch (Exception ex) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Amount"));
		return true;
	    }
	    Residence.getTransactionManager().putForSale(args[2], player, amount, resadmin);
	    return true;
	default:
	    return false;
	}
    }

    private static boolean commandResMarketRent(String[] args, boolean resadmin, Player player) {
	if (args.length < 2 || args.length > 4) {
	    return false;
	}
	boolean repeat = Residence.getConfigManager().isRentPlayerAutoPay();

	ClaimedResidence res = null;

	if (args.length == 4) {
	    if (args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (args[3].equalsIgnoreCase("f") || args[3].equalsIgnoreCase("false")) {
		repeat = false;
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}

	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 4)
	    res = Residence.getResidenceManager().getByName(args[2]);

	if (res != null)
	    Residence.getRentManager().rent(player, res, repeat, resadmin);
	else
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));

	return true;
    }

    private static boolean commandResMarketPayRent(String[] args, boolean resadmin, Player player) {
	if (args.length != 2 && args.length != 3) {
	    return false;
	}

	ClaimedResidence res = null;

	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else
	    res = Residence.getResidenceManager().getByName(args[2]);

	if (res != null)
	    Residence.getRentManager().payRent(player, res, resadmin);
	else
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	return true;
    }

    private static boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
	if (args.length < 5 || args.length > 8) {
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
	boolean AllowRenewing = Residence.getConfigManager().isRentAllowRenewing();
	if (args.length >= 6) {
	    String ag = args[5];
	    if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
		AllowRenewing = true;
	    } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
		AllowRenewing = false;
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}

	boolean StayInMarket = Residence.getConfigManager().isRentStayInMarket();
	if (args.length >= 7) {
	    String ag = args[6];
	    if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
		StayInMarket = true;
	    } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
		StayInMarket = false;
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}

	boolean AllowAutoPay = Residence.getConfigManager().isRentAllowAutoPay();
	if (args.length >= 8) {
	    String ag = args[7];
	    if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
		AllowAutoPay = true;
	    } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
		AllowAutoPay = false;
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
		return true;
	    }
	}

	Residence.getRentManager().setForRent(player, args[2], cost, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
	return true;
    }

    private static boolean commandResMarketAutoPay(String[] args, boolean resadmin, Player player) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return true;
	}
	if (args.length != 3 && args.length != 4) {
	    return false;
	}

	boolean value;

	String barg = "";
	ClaimedResidence res = null;
	if (args.length == 3) {
	    barg = args[2];
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	} else {
	    barg = args[3];
	    res = Residence.getResidenceManager().getByName(args[2]);
	}

	if (barg.equalsIgnoreCase("true") || barg.equalsIgnoreCase("t")) {
	    value = true;
	} else if (barg.equalsIgnoreCase("false") || barg.equalsIgnoreCase("f")) {
	    value = false;
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Boolean"));
	    return true;
	}

	if (res == null) {
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	    return true;
	}

	if (res.isRented() && res.getRentedLand().player.equalsIgnoreCase(player.getName())) {
	    Residence.getRentManager().setRentedRepeatable(player, res.getName(), value, resadmin);
	} else if (res.isForRent()) {
	    Residence.getRentManager().setRentRepeatable(player, res.getName(), value, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.RentReleaseInvalid", ChatColor.YELLOW + res.getName() + ChatColor.RED));
	}
	return true;
    }

    private static boolean commandResMarketList(String[] args, Player player, int page) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    player.sendMessage(Residence.getLM().getMessage("Economy.MarketDisabled"));
	    return true;
	}
	player.sendMessage(Residence.getLM().getMessage("General.MarketList"));
	if (args.length < 3)
	    return false;

	if (args[2].equalsIgnoreCase("sell")) {
	    Residence.getTransactionManager().printForSaleResidences(player, page);
	    return true;
	}
	if (args[2].equalsIgnoreCase("rent")) {
	    if (Residence.getConfigManager().enabledRentSystem()) {
		Residence.getRentManager().printRentableResidences(player, page);
	    }
	    return true;
	}
	return false;
    }

}
