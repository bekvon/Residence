package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.signsStuff.Signs;

public class market implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2600)
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
		Residence.msg(player, lm.Invalid_Residence);
		return true;
	    }

	    if (res.isRented()) {
		if (resadmin || Residence.isResAdminOn(player) || player.hasPermission("residence.market.evict")) {
		    Residence.UnrentConfirm.put(player.getName(), res.getName());
		    Residence.msg(sender, lm.Rent_EvictConfirm, res.getName());
		} else if (Residence.getRentManager().getRentingPlayer(res).equalsIgnoreCase(sender.getName())) {
		    Residence.UnrentConfirm.put(player.getName(), res.getName());
		    Residence.msg(sender, lm.Rent_UnrentConfirm, res.getName());
		} else
		    Residence.getRentManager().printRentInfo(player, res);
	    } else
		Residence.msg(sender, lm.Rent_ReleaseConfirm, res.getName());

	    return true;

	case "confirm":
	    if (!Residence.UnrentConfirm.containsKey(player.getName())) {
		Residence.msg(player, lm.Invalid_Residence);
		return false;
	    }
	    String area = Residence.UnrentConfirm.remove(player.getName());
	    res = Residence.getResidenceManager().getByName(area);
	    if (res == null) {
		Residence.msg(player, lm.Invalid_Residence);
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
		Residence.msg(player, lm.Sign_LookAt);
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
		Residence.msg(player, lm.Residence_NotOwner);
		return true;
	    }

	    res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		Residence.msg(player, lm.Invalid_Residence);
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
		Residence.msg(player, lm.Residence_NotForRentOrSell);
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
		Residence.msg(player, lm.Invalid_Residence);
		return true;
	    }
	    boolean sell = Residence.getTransactionManager().viewSaleInfo(res, player);
	    if (Residence.getConfigManager().enabledRentSystem() && res.isForRent()) {
		Residence.getRentManager().printRentInfo(player, res);
	    } else if (!sell) {
		Residence.msg(sender, lm.Residence_NotForRentOrSell);
	    }
	    return true;
	case "buy":
	    res = null;
	    if (args.length == 2)
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    else if (args.length == 3)
		res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		Residence.msg(player, lm.Invalid_Residence);
		return true;
	    }

	    sell = Residence.getTransactionManager().viewSaleInfo(res, player);
	    if (sell) {
		Residence.getTransactionManager().buyPlot(res, player, resadmin);
	    } else {
		Residence.msg(sender, lm.Residence_NotForRentOrSell);
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
		Residence.msg(player, lm.Invalid_Amount);
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
		Residence.msg(player, lm.Invalid_Boolean);
		return true;
	    }
	}

	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length > 2)
	    res = Residence.getResidenceManager().getByName(args[2]);

	if (res != null)
	    Residence.getRentManager().rent(player, res, repeat, resadmin);
	else
	    Residence.msg(player, lm.Invalid_Residence);

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
	    Residence.msg(player, lm.Invalid_Residence);
	return true;
    }

    private static boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
	if (args.length < 5 || args.length > 8) {
	    return false;
	}
	if (!Residence.getConfigManager().enabledRentSystem()) {
	    Residence.msg(player, lm.Rent_Disabled);
	    return true;
	}
	int days;
	int cost;
	try {
	    cost = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    Residence.msg(player, lm.Invalid_Cost);
	    return true;
	}
	try {
	    days = Integer.parseInt(args[4]);
	} catch (Exception ex) {
	    Residence.msg(player, lm.Invalid_Days);
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
		Residence.msg(player, lm.Invalid_Boolean);
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
		Residence.msg(player, lm.Invalid_Boolean);
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
		Residence.msg(player, lm.Invalid_Boolean);
		return true;
	    }
	}

	Residence.getRentManager().setForRent(player, args[2], cost, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
	return true;
    }

    private static boolean commandResMarketAutoPay(String[] args, boolean resadmin, Player player) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    Residence.msg(player, lm.Economy_MarketDisabled);
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
	    Residence.msg(player, lm.Invalid_Boolean);
	    return true;
	}

	if (res == null) {
	    Residence.msg(player, lm.Invalid_Residence);
	    return true;
	}

	if (res.isRented() && res.getRentedLand().player.equalsIgnoreCase(player.getName())) {
	    Residence.getRentManager().setRentedRepeatable(player, res.getName(), value, resadmin);
	} else if (res.isForRent()) {
	    Residence.getRentManager().setRentRepeatable(player, res.getName(), value, resadmin);
	} else {
	    Residence.msg(player, lm.Economy_RentReleaseInvalid, ChatColor.YELLOW + res.getName() + ChatColor.RED);
	}
	return true;
    }

    private static boolean commandResMarketList(String[] args, Player player, int page) {
	if (!Residence.getConfigManager().enableEconomy()) {
	    Residence.msg(player, lm.Economy_MarketDisabled);
	    return true;
	}
	Residence.msg(player, lm.General_MarketList);
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

    @Override
    public void getLocale(ConfigReader c, String path) {

	c.get(path + "Description", "Buy, Sell, or Rent Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res market ? for more Info"));

	path += "SubCommands.";

	c.get(path + "Info.Description", "Get economy Info on residence");
	c.get(path + "Info.Info", Arrays.asList("&eUsage: &6/res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "Info"), Arrays.asList("[residence]"));

	c.get(path + "list.Description", "Lists rentable and for sale residences.");
	c.get(path + "list.Info", Arrays.asList("&eUsage: &6/res market list [rent/sell]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "list"), Arrays.asList("rent%%sell"));

	c.get(path + "list.SubCommands.rent.Description", "Lists rentable residences.");
	c.get(path + "list.SubCommands.rent.Info", Arrays.asList("&eUsage: &6/res market list rent"));

	c.get(path + "list.SubCommands.sell.Description", "Lists for sale residences.");
	c.get(path + "list.SubCommands.sell.Info", Arrays.asList("&eUsage: &6/res market list sell"));

	c.get(path + "sell.Description", "Sell a residence");
	c.get(path + "sell.Info", Arrays.asList("&eUsage: &6/res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
	    "Another player can buy the residence with /res market buy"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "sell"), Arrays.asList("[residence]"));

	c.get(path + "sign.Description", "Set market sign");
	c.get(path + "sign.Info", Arrays.asList("&eUsage: &6/res market sign [residence]", "Sets market sign you are looking at."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "sign"), Arrays.asList("[residence]"));

	c.get(path + "buy.Description", "Buy a residence");
	c.get(path + "buy.Info", Arrays.asList("&eUsage: &6/res market buy [residence]", "Buys a Residence if its for sale."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "buy"), Arrays.asList("[residence]"));

	c.get(path + "unsell.Description", "Stops selling a residence");
	c.get(path + "unsell.Info", Arrays.asList("&eUsage: &6/res market unsell [residence]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "unsell"), Arrays.asList("[residence]"));

	c.get(path + "rent.Description", "ent a residence");
	c.get(path + "rent.Info", Arrays.asList("&eUsage: &6/res market rent [residence] <AutoPay>",
	    "Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "rent"), Arrays.asList("[cresidence]", "true%%false"));

	c.get(path + "rentable.Description", "Make a residence rentable.");
	c.get(path + "rentable.Info", Arrays.asList("&eUsage: &6/res market rentable [residence] [cost] [days] <AllowRenewing> <StayInMarket> <AllowAutoPay>",
	    "Makes a residence rentable for [cost] money for every [days] number of days.",
	    "If <AllowRenewing> is true, the residence will be able to be rented again before rent expires.",
	    "If <StayInMarket> is true, the residence will stay in market after last renter will be removed.",
	    "If <AllowAutoPay> is true, money for rent will be automaticaly taken from players balance if he chosen that option when renting"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "rentable"), Arrays.asList("[residence]", "1000", "7", "true", "true",
	    "true"));

	c.get(path + "autopay.Description", "Sets residence AutoPay to given value");
	c.get(path + "autopay.Info", Arrays.asList("&eUsage: &6/res market autopay <residence> [true/false]"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "autopay"), Arrays.asList("[residence]%%true%%false", "true%%false"));

	c.get(path + "payrent.Description", "Pays rent for defined residence");
	c.get(path + "payrent.Info", Arrays.asList("&eUsage: &6/res market payrent <residence>"));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "payrent"), Arrays.asList("[residence]"));

	c.get(path + "confirm.Description", "Confirms residence unrent/release action");
	c.get(path + "confirm.Info", Arrays.asList("&eUsage: &6/res market confirm"));

	c.get(path + "release.Description", "Remove a residence from rent or rentable.");
	c.get(path + "release.Info", Arrays.asList("&eUsage: &6/res market release [residence]",
	    "If you are the renter, this command releases the rent on the house for you.",
	    "If you are the owner, this command makes the residence not for rent anymore."));
	Residence.getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "release"), Arrays.asList("[residence]"));
    }

}
