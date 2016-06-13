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
	    return commandResMarketList(args, resadmin, player, page);
	case "autopay":
	    return commandResMarketAutoPay(args, resadmin, player, page);
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

	    String area = null;

	    if (args.length == 2)
		area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    else
		area = args[2];

	    if (area == null) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return true;
	    }

	    Residence.UnrentConfirm.put(player.getName(), area);

	    if (Residence.getRentManager().isRented(area)) {
		if (resadmin || Residence.isResAdminOn(player))
		    sender.sendMessage(Residence.getLM().getMessage("Rent.EvictConfirm", area));
		else if (Residence.getRentManager().getRentingPlayer(area).equalsIgnoreCase(sender.getName()))
		    sender.sendMessage(Residence.getLM().getMessage("Rent.UnrentConfirm", area));
		else
		    Residence.getRentManager().printRentInfo(player, area);
	    } else
		sender.sendMessage(Residence.getLM().getMessage("Rent.ReleaseConfirm", area));

	    return true;

	case "confirm":

	    if (!Residence.UnrentConfirm.containsKey(player.getName())) {
		player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
		return false;
	    }

	    area = Residence.UnrentConfirm.remove(player.getName());

	    if (!Residence.getRentManager().isRented(area)) {
		Residence.getRentManager().removeFromForRent(player, area, resadmin);
		ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
		ClaimedResidence res = Residence.getResidenceManager().getByName(area);
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

	    String landName = null;

	    ClaimedResidence CurrentRes = Residence.getResidenceManager().getByLoc(sign.getLocation());

	    if (CurrentRes != null && !CurrentRes.isOwner(player) && !resadmin) {
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
		signInfo.setLocation(loc);
//		signInfo.updateLocation();
		Residence.getSignUtil().getSigns().addSign(signInfo);
		Residence.getSignUtil().saveSigns();
	    } else {
		player.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		return true;
	    }

	    Residence.getSignUtil().CheckSign(res, 5);

	    return true;

	case "info":
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
	case "buy":
	    if (args.length == 2) {
		String areaname = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		boolean sell = Residence.getTransactionManager().viewSaleInfo(areaname, player);
		if (sell) {
		    Residence.getTransactionManager().buyPlot(areaname, player, resadmin);
		} else {
		    sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		}
	    } else if (args.length == 3) {
		boolean sell = Residence.getTransactionManager().viewSaleInfo(args[2], player);
		if (sell) {
		    Residence.getTransactionManager().buyPlot(args[2], player, resadmin);
		} else {
		    sender.sendMessage(Residence.getLM().getMessage("Residence.NotForRentOrSell"));
		}
	    } else {
		return false;
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

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player) {
	if (args.length < 2 || args.length > 4) {
	    return false;
	}
	boolean repeat = Residence.getConfigManager().isRentPlayerAutoPay();

	String area = null;

	if (args.length == 4) {
	    area = args[2];
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
	    area = Residence.getResidenceManager().getNameByLoc(player.getLocation());

	if (area != null)
	    Residence.getRentManager().rent(player, area, repeat, resadmin);
	else
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));

	return true;
    }

    private boolean commandResMarketPayRent(String[] args, boolean resadmin, Player player) {
	if (args.length != 2 && args.length != 3) {
	    return false;
	}

	String area = null;

	if (args.length == 2)
	    area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	else {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
	    if (res != null)
		area = res.getName();
	}

	if (area != null)
	    Residence.getRentManager().payRent(player, area, resadmin);
	else
	    player.sendMessage(Residence.getLM().getMessage("Invalid.Residence"));
	return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
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

    private boolean commandResMarketAutoPay(String[] args, boolean resadmin, Player player, int page) {
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

	if (Residence.getRentManager().isRented(res.getName()) && Residence.getRentManager().getRentingPlayer(res.getName()).equalsIgnoreCase(player.getName())) {
	    Residence.getRentManager().setRentedRepeatable(player, res.getName(), value, resadmin);
	} else if (Residence.getRentManager().isForRent(res.getName())) {
	    Residence.getRentManager().setRentRepeatable(player, res.getName(), value, resadmin);
	} else {
	    player.sendMessage(Residence.getLM().getMessage("Economy.RentReleaseInvalid", ChatColor.YELLOW + res.getName() + ChatColor.RED));
	}
	return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player, int page) {
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
