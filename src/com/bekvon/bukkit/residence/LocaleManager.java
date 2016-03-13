package com.bekvon.bukkit.residence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleManager {

    public static ArrayList<String> FlagList = new ArrayList<String>();
    private Residence plugin;

    public LocaleManager(Residence plugin) {
	this.plugin = plugin;
    }

    public static String GetConfig(String path, String text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	text = text.replace("\"", "\'");
	conf.addDefault(path, text);
	text = conf.getString(path);
	if (colorize)
	    text = Colors(text);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> GetConfig(String path, List<String> text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = ColorsArray(conf.getStringList(path), colorize);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> ColorsArray(List<String> text, Boolean colorize) {
	List<String> temp = new ArrayList<String>();
	for (String part : text) {
	    if (colorize)
		part = Colors(part);
	    temp.add(Colors(part));
	}
	return temp;
    }

    private synchronized static void copySetting(Configuration reader, Configuration writer, String path) {
	writer.set(path, reader.get(path));
    }

    public static String Colors(String text) {
	return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static YamlConfiguration loadConfiguration(BufferedReader in, String language) {
	Validate.notNull(in, "File cannot be null");
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.load(in);
	} catch (FileNotFoundException ex) {
	} catch (IOException ex) {
	} catch (InvalidConfigurationException ex) {
	    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Residence] Your locale file for " + language
		+ " is incorect! Use http://yaml-online-parser.appspot.com/ to find issue.");
	    return null;	   
	}

	return config;
    }

    // Language file
    public void LoadLang(String lang) {

	File f = new File(plugin.getDataFolder(), "Language" + File.separator + lang + ".yml");

	BufferedReader in = null;
	try {
	    in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}

	if (in == null)
	    return;

	YamlConfiguration conf = loadConfiguration(in, lang);

	if (conf == null) {
	    return;
	}

	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	conf.options().copyDefaults(true);

	StringBuilder header = new StringBuilder();
	header.append(System.getProperty("line.separator"));
	header.append("NOTE If you want to modify this file, it is HIGHLY recommended that you make a copy");
	header.append(System.getProperty("line.separator"));
	header.append("of this file and modify that instead. This file will be updated automatically by Residence");
	header.append(System.getProperty("line.separator"));
	header.append("when a newer version is detected, and your changes will be overwritten.  Once you ");
	header.append(System.getProperty("line.separator"));
	header.append("have a copy of this file, change the Language: option under the Residence config.yml");
	header.append(System.getProperty("line.separator"));
	header.append("to whatever you named your copy.");
	header.append(System.getProperty("line.separator"));
	conf.options().copyDefaults(true);

	writer.options().header(header.toString());

	GetConfig("Language.Invalid.Player", "&cInvalid player name...", writer, conf, true);
	GetConfig("Language.Invalid.Residence", "&cInvalid Residence...", writer, conf, true);
	GetConfig("Language.Invalid.Subzone", "&cInvalid Subzone...", writer, conf, true);
	GetConfig("Language.Invalid.Direction", "&cInvalid Direction...", writer, conf, true);
	GetConfig("Language.Invalid.Amount", "&cInvalid Amount...", writer, conf, true);
	GetConfig("Language.Invalid.Cost", "&cInvalid Cost...", writer, conf, true);
	GetConfig("Language.Invalid.Days", "&cInvalid number of days...", writer, conf, true);
	GetConfig("Language.Invalid.Material", "&cInvalid Material...", writer, conf, true);
	GetConfig("Language.Invalid.Boolean", "&cInvalid value, must be &6true(t) &cor &6false(f)", writer, conf, true);
	GetConfig("Language.Invalid.Area", "&cInvalid Area...", writer, conf, true);
	GetConfig("Language.Invalid.Group", "&cInvalid Group...", writer, conf, true);
	GetConfig("Language.Invalid.MessageType", "&cMessage type must be enter or remove.", writer, conf, true);
	GetConfig("Language.Invalid.Flag", "&cInvalid Flag...", writer, conf, true);
	GetConfig("Language.Invalid.FlagState", "&cInvalid flag state, must be &6true(t)&c, &6false(f)&c, or &6remove(r)", writer, conf, true);
	GetConfig("Language.Invalid.List", "&eUnknown list type, must be &6blacklist &eor &6ignorelist.", writer, conf, true);
	GetConfig("Language.Invalid.Page", "&eInvalid Page...", writer, conf, true);
	GetConfig("Language.Invalid.Help", "&cInvalid Help Page...", writer, conf, true);
	GetConfig("Language.Invalid.NameCharacters", "&cName contained unallowed characters...", writer, conf, true);

	GetConfig("Language.Area.Exists", "&cArea name already exists.", writer, conf, true);
	GetConfig("Language.Area.Create", "&eResidence Area created, ID &6%1", writer, conf, true);
	GetConfig("Language.Area.DiffWorld", "&cArea is in a different world from residence.", writer, conf, true);
	GetConfig("Language.Area.Collision", "&cArea collides with residence &6%1", writer, conf, true);
	GetConfig("Language.Area.SubzoneCollision", "&cArea collides with subzone &6%1", writer, conf, true);
	GetConfig("Language.Area.NonExist", "&cNo such area exists.", writer, conf, true);
	GetConfig("Language.Area.InvalidName", "&cInvalid Area Name...", writer, conf, true);
	GetConfig("Language.Area.ToSmallTotal", "&cSelected area smaller than allowed minimal (&6%1&c)", writer, conf, true);
	GetConfig("Language.Area.ToSmallX", "&cYour &6X &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more.", writer, conf, true);
	GetConfig("Language.Area.ToSmallY", "&cYour selection height (&6%1&c) is too small. &eAllowed &6%2 and more.", writer, conf, true);
	GetConfig("Language.Area.ToSmallZ", "&cYour &6Z &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more.", writer, conf, true);
	GetConfig("Language.Area.Rename", "&eRenamed area &6%1 &eto &6%2", writer, conf, true);
	GetConfig("Language.Area.Remove", "&eRemoved area &6%1...", writer, conf, true);
	GetConfig("Language.Area.Name", "&eName: &2%1", writer, conf, true);
	GetConfig("Language.Area.RemoveLast", "&cCannot remove the last area in a residence.", writer, conf, true);
	GetConfig("Language.Area.NotWithinParent", "&cArea is not within parent area.", writer, conf, true);
	GetConfig("Language.Area.Update", "&eArea Updated...", writer, conf, true);
	GetConfig("Language.Area.MaxPhysical", "&eYou've reached the max physical areas allowed for your residence.", writer, conf, true);
	GetConfig("Language.Area.SizeLimit", "&eArea size is not within your allowed limits.", writer, conf, true);
	GetConfig("Language.Area.HighLimit", "&cYou cannot protect this high up, your limit is &6%1", writer, conf, true);
	GetConfig("Language.Area.LowLimit", "&cYou cannot protect this deep, your limit is &6%1", writer, conf, true);

	GetConfig("Language.Select.Points", "&eSelect two points first before using this command!", writer, conf, true);
	GetConfig("Language.Select.Overlap", "&cSelected points overlap with &6%1 &cregion!", writer, conf, true);
	GetConfig("Language.Select.WorldGuardOverlap", "&cSelected points overlap with &6%1 &cWorldGuard region!", writer, conf, true);
	GetConfig("Language.Select.Success", "&eSelection Successful!", writer, conf, true);
	GetConfig("Language.Select.Fail", "&cInvalid select command...", writer, conf, true);
	GetConfig("Language.Select.Bedrock", "&eSelection expanded to your lowest allowed limit.", writer, conf, true);
	GetConfig("Language.Select.Sky", "&eSelection expanded to your highest allowed limit.", writer, conf, true);
	GetConfig("Language.Select.Area", "&eSelected area &6%1 &eof residence &6%2", writer, conf, true);
	GetConfig("Language.Select.Tool", "&e- Selection Tool: &6%1", writer, conf, true);
	GetConfig("Language.Select.PrimaryPoint", "&ePlaced &6Primary &eSelection Point %1", writer, conf, true);
	GetConfig("Language.Select.SecondaryPoint", "&ePlaced &6Secondary &eSelection Point %1", writer, conf, true);
	GetConfig("Language.Select.Primary", "&ePrimary selection: &6%1", writer, conf, true);
	GetConfig("Language.Select.Secondary", "&eSecondary selection: &6%1", writer, conf, true);
	GetConfig("Language.Select.TooHigh", "&cWarning, selection went above top of map, limiting.", writer, conf, true);
	GetConfig("Language.Select.TooLow", "&cWarning, selection went below bottom of map, limiting.", writer, conf, true);
	GetConfig("Language.Select.TotalSize", "&eSelection total size: &6%1", writer, conf, true);
	GetConfig("Language.Select.AutoEnabled", "&eAuto selection mode turned &6ON&e. To disable it write &6/res select auto", writer, conf, true);
	GetConfig("Language.Select.AutoDisabled", "&eAuto selection mode turned &6OFF&e. To enable it again write &6/res select auto", writer, conf, true);
	GetConfig("Language.Select.Disabled", "&cYou don't have access to selections commands", writer, conf, true);

	GetConfig("Language.Sign.Updated", "&6%1 &esigns updated!", writer, conf, true);
	GetConfig("Language.Sign.TopLine", "[market]", writer, conf, true);
	GetConfig("Language.Sign.DateFormat", "YY/MM/dd HH:mm", writer, conf, true);
	GetConfig("Language.Sign.ForRentTopLine", "&8For Rent", writer, conf, true);
	GetConfig("Language.Sign.ForRentPriceLine", "&8%1&f/&8%2&f/&8%3", writer, conf, true);
	GetConfig("Language.Sign.ForRentResName", "&8%1", writer, conf, true);
	GetConfig("Language.Sign.ForRentBottomLine", "&9Available", writer, conf, true);
	GetConfig("Language.Sign.RentedAutorenewTrue", "&2%1", writer, conf, true);
	GetConfig("Language.Sign.RentedAutorenewFalse", "&c%1", writer, conf, true);
	GetConfig("Language.Sign.RentedTopLine", "%1", writer, conf, true);
	GetConfig("Language.Sign.RentedPriceLine", "&8%1&f/&8%2&f/&8%3", writer, conf, true);
	GetConfig("Language.Sign.RentedResName", "&8%1", writer, conf, true);
	GetConfig("Language.Sign.RentedBottomLine", "&1%1", writer, conf, true);
	GetConfig("Language.Sign.ForSaleTopLine", "&8For Sale", writer, conf, true);
	GetConfig("Language.Sign.ForSalePriceLine", "&8%1", writer, conf, true);
	GetConfig("Language.Sign.ForSaleResName", "&8%1", writer, conf, true);
	GetConfig("Language.Sign.ForSaleBottomLine", "&5Available", writer, conf, true);
	GetConfig("Language.Sign.LookAt", "&cYou are not looking at sign", writer, conf, true);

	GetConfig("Language.Flag.Set", "&eFlag (&6%1&e) set for &6%2 &eto &6%3 &estate", writer, conf, true);
	GetConfig("Language.Flag.SetFailed", "&cYou dont have access to &6%1 &cflag", writer, conf, true);
	GetConfig("Language.Flag.CheckTrue", "&eFlag &6%1 &eapplies to player &6%2 &efor residence &6%3&e, value = &6%4", writer, conf, true);
	GetConfig("Language.Flag.CheckFalse", "&eFlag &6%1 &edoes not apply to player &6%2 &efor residence.", writer, conf, true);
	GetConfig("Language.Flag.Cleared", "&eFlags Cleared.", writer, conf, true);
	GetConfig("Language.Flag.Default", "&eFlags set to default.", writer, conf, true);
	GetConfig("Language.Flag.Deny", "&cYou dont have &6%1 &cpermission<s> here.", writer, conf, true);
	GetConfig("Language.Flag.SetDeny", "&cOwner does not have access to flag &6%1", writer, conf, true);
	GetConfig("Language.Flag.ChangeDeny", "&cYou cant change &6%1 &cflag state while there is &6%2 &cplayer(s) inside.", writer, conf, true);

	GetConfig("Language.Bank.NoAccess", "&cYou dont have bank access.", writer, conf, true);
	GetConfig("Language.Bank.Name", " &eBank: &6%1", writer, conf, true);
	GetConfig("Language.Bank.NoMoney", "&cNot enough money in the bank.", writer, conf, true);
	GetConfig("Language.Bank.Deposit", "&eYou deposit &6%1 &einto the residence bank.", writer, conf, true);
	GetConfig("Language.Bank.Withdraw", "&eYou withdraw &6%1 from the residence bank.", writer, conf, true);

	GetConfig("Language.Subzone.Rename", "&eRenamed subzone &6%1 &eto &6%2", writer, conf, true);
	GetConfig("Language.Subzone.Remove", "&eSubzone &6%1 &eremoved.", writer, conf, true);
	GetConfig("Language.Subzone.Create", "&eCreated Subzone &6%1", writer, conf, true);
	GetConfig("Language.Subzone.CreateFail", "&cUnable to create subzone &6%1", writer, conf, true);
	GetConfig("Language.Subzone.Exists", "&cSubzone &6%1 &calready exists.", writer, conf, true);
	GetConfig("Language.Subzone.Collide", "&cSubzone collides with subzone &6%1", writer, conf, true);
	GetConfig("Language.Subzone.MaxDepth", "&cYou have reached the maximum allowed subzone depth.", writer, conf, true);
	GetConfig("Language.Subzone.SelectInside", "&eBoth selection points must be inside the residence.", writer, conf, true);
	GetConfig("Language.Subzone.CantCreate", "&cYou dont have permission to create residence subzone.", writer, conf, true);
	GetConfig("Language.Subzone.CantDelete", "&cYou dont have permission to delete residence subzone.", writer, conf, true);
	GetConfig("Language.Subzone.CantDeleteNotOwnerOfParent", "&cYou are not owner of parent residence to delete this subzone.", writer, conf, true);
	GetConfig("Language.Subzone.CantContract", "&cYou dont have permission to contract residence subzone.", writer, conf, true);
	GetConfig("Language.Subzone.CantExpand", "&cYou dont have permission to expand residence subzone.", writer, conf, true);
	GetConfig("Language.Subzone.DeleteConfirm", "&eAre you sure you want to delete subzone &6%1&e, use &6/res confirm &eto confirm.", writer, conf, true);
	GetConfig("Language.Subzone.OwnerChange", "&eSubzone &6%1 &eowner changed to &6%2", writer, conf, true);

	GetConfig("Language.Residence.Bought", "&eYou bought residence &6%1", writer, conf, true);
	GetConfig("Language.Residence.Buy", "&6%1 &ehas bought residence &6%2 &efrom you.", writer, conf, true);
	GetConfig("Language.Residence.BuyTooBig", "&cThis residence has areas bigger then your allowed max.", writer, conf, true);
	GetConfig("Language.Residence.NotForSale", "&cResidence is not for sale.", writer, conf, true);
	GetConfig("Language.Residence.ForSale", "&eResidence &6%1 &eis now for sale for &6%2", writer, conf, true);
	GetConfig("Language.Residence.StopSelling", "&cResidence is no longer for sale.", writer, conf, true);
	GetConfig("Language.Residence.TooMany", "&cYou already own the max number of residences your allowed to.", writer, conf, true);
	GetConfig("Language.Residence.MaxRent", "&cYou already are renting the maximum number of residences your allowed to.", writer, conf, true);
	GetConfig("Language.Residence.AlreadyRent", "&cResidence is already for rent...", writer, conf, true);
	GetConfig("Language.Residence.NotForRent", "&cResidence not for rent...", writer, conf, true);
	GetConfig("Language.Residence.NotForRentOrSell", "&cResidence not for rent or sell...", writer, conf, true);
	GetConfig("Language.Residence.NotRented", "&cResidence not rented.", writer, conf, true);
	GetConfig("Language.Residence.Unrent", "&eResidence &6%1 &ehas been unrented.", writer, conf, true);
	GetConfig("Language.Residence.RemoveRentable", "&eResidence &6%1 &eis no longer rentable.", writer, conf, true);
	GetConfig("Language.Residence.ForRentSuccess", "&eResidence &6%1 &eis now for rent for &6%2 &eevery &6%3 &edays.", writer, conf, true);
	GetConfig("Language.Residence.RentSuccess", "&eYou have rented Residence &6%1 &efor &6%2 &edays.", writer, conf, true);
	GetConfig("Language.Residence.AlreadyRented", "&eResidence &6%1 &ehas currently been rented to &6%2", writer, conf, true);
	GetConfig("Language.Residence.AlreadyExists", "&cA residence named &6%1 &calready exists.", writer, conf, true);
	GetConfig("Language.Residence.Create", "&eYou have created residence &6%1&e!", writer, conf, true);
	GetConfig("Language.Residence.Rename", "&eRenamed Residence &6%1 &eto &6%2", writer, conf, true);
	GetConfig("Language.Residence.Remove", "&eResidence &6%1 &ehas been removed...", writer, conf, true);
	GetConfig("Language.Residence.MoveDeny", "&cYou dont have movement permission for Residence &6%1", writer, conf, true);
	GetConfig("Language.Residence.TeleportNoFlag", "&cYou dont have teleport access for that residence.", writer, conf, true);
	GetConfig("Language.Residence.FlagDeny", "&cYou dont have &6%1 &cpermission for Residence &6%2", writer, conf, true);
	GetConfig("Language.Residence.GiveLimits", "&cCannot give residence to target player, because it is outside the target players limits.", writer, conf, true);
	GetConfig("Language.Residence.Give", "&eYou give residence &6%1 &eto player &6%2", writer, conf, true);
	GetConfig("Language.Residence.Recieve", "&eYou have recieved residence &6%1 &efrom player &6%2", writer, conf, true);
	GetConfig("Language.Residence.List", " &a%1%2 &e- &6World&e: &6%3", writer, conf, true);
	GetConfig("Language.Residence.TeleportNear", "&eTeleported to near residence.", writer, conf, true);
	GetConfig("Language.Residence.SetTeleportLocation", "&eTeleport Location Set...", writer, conf, true);
	GetConfig("Language.Residence.PermissionsApply", "&ePermissions applied to residence.", writer, conf, true);
	GetConfig("Language.Residence.NotOwner", "&cYou are not owner of this residence", writer, conf, true);
	GetConfig("Language.Residence.RemovePlayersResidences", "&eRemoved all residences belonging to player &6%1", writer, conf, true);
	GetConfig("Language.Residence.NotIn", "&cYou are not in a Residence.", writer, conf, true);
	GetConfig("Language.Residence.PlayerNotIn", "&cPlayer standing not in your Residence area.", writer, conf, true);
	GetConfig("Language.Residence.Kicked", "&eYou were kicked from residence", writer, conf, true);
	GetConfig("Language.Residence.In", "&eYou are standing in Residence &6%1", writer, conf, true);
	GetConfig("Language.Residence.OwnerChange", "&eResidence &6%1 &eowner changed to &6%2", writer, conf, true);
	GetConfig("Language.Residence.NonAdmin", "&cYou are not a Residence admin.", writer, conf, true);
	GetConfig("Language.Residence.Line", "&eResidence: &6%1 ", writer, conf, true);
	GetConfig("Language.Residence.RentedBy", "&eRented by: &6%1", writer, conf, true);
	GetConfig("Language.Residence.MessageChange", "&eMessage Set...", writer, conf, true);
	GetConfig("Language.Residence.CantDeleteResidence", "&cYou dont have permission to delete residence.", writer, conf, true);
	GetConfig("Language.Residence.CantExpandResidence", "&cYou dont have permission to expand residence.", writer, conf, true);
	GetConfig("Language.Residence.CantContractResidence", "&cYou dont have permission to contract residence.", writer, conf, true);
	GetConfig("Language.Residence.NoResHere", "&cThere is no residence in there.", writer, conf, true);
	GetConfig("Language.Residence.OwnerNoPermission", "&cThe owner does not have permission for this.", writer, conf, true);
	GetConfig("Language.Residence.ParentNoPermission", "&cYou don't have permission to make changes to the parent zone.", writer, conf, true);
	GetConfig("Language.Residence.ChatDisabled", "&eResidence Chat Disabled...", writer, conf, true);
	GetConfig("Language.Residence.DeleteConfirm", "&eAre you sure you want to delete residence &6%1&e, use &6/res confirm &eto confirm.", writer, conf, true);

	GetConfig("Language.Rent.Disabled", "&cRent is disabled...", writer, conf, true);
	GetConfig("Language.Rent.DisableRenew", "&eResidence &6%1 &ewill now no longer re-rent upon expire.", writer, conf, true);
	GetConfig("Language.Rent.EnableRenew", "&eResidence &6%1 &ewill now automatically re-rent upon expire.", writer, conf, true);
	GetConfig("Language.Rent.Expire", "&eRent Expire Time: &6%1", writer, conf, true);
	GetConfig("Language.Rent.ModifyDeny", "&cCannot modify a rented residence.", writer, conf, true);
	GetConfig("Language.Rent.Days", "&eRent days: &6%1", writer, conf, true);
	GetConfig("Language.Rent.Rented", " &6(Rented)", writer, conf, true);

	GetConfig("Language.Rentable.Land", "&eRentable Land: &6", writer, conf, true);
	GetConfig("Language.Rentable.AutoRenew", "&eRentable Auto Renew: &6%1", writer, conf, true);
	GetConfig("Language.Rentable.DisableRenew", "&6%1 &ewill no longer renew rentable status upon expire.", writer, conf, true);
	GetConfig("Language.Rentable.EnableRenew", "&6%1 &ewill now automatically renew rentable status upon expire.", writer, conf, true);

	GetConfig("Language.Economy.LandForSale", "&eLand For Sale:", writer, conf, true);
	GetConfig("Language.Economy.NotEnoughMoney", "&cYou dont have enough money.", writer, conf, true);
	GetConfig("Language.Economy.MoneyCharged", "&eCharged &6%1 &eto your &6%2 &eaccount.", writer, conf, true);
	GetConfig("Language.Economy.MoneyAdded", "&eGot &6%1 &eto your &6%2 &eaccount.", writer, conf, true);
	GetConfig("Language.Economy.MoneyCredit", "&eCredited &6%1 &eto your &6%2 &eaccount.", writer, conf, true);
	GetConfig("Language.Economy.RentReleaseInvalid", "&eResidence &6%1 &eis not rented or for rent.", writer, conf, true);
	GetConfig("Language.Economy.RentSellFail", "&cCannot sell a Residence if it is for rent.", writer, conf, true);
	GetConfig("Language.Economy.SellRentFail", "&cCannot rent a Residence if it is for sale.", writer, conf, true);
	GetConfig("Language.Economy.OwnerBuyFail", "&cCannot buy your own land!", writer, conf, true);
	GetConfig("Language.Economy.OwnerRentFail", "&cCannot rent your own land!", writer, conf, true);
	GetConfig("Language.Economy.AlreadySellFail", "&eResidence already for sale!", writer, conf, true);
	GetConfig("Language.Economy.LeaseRenew", "&eLease valid until &6%1", writer, conf, true);
	GetConfig("Language.Economy.LeaseRenewMax", "&eLease renewed to maximum allowed", writer, conf, true);
	GetConfig("Language.Economy.LeaseNotExpire", "&eNo such lease, or lease does not expire.", writer, conf, true);
	GetConfig("Language.Economy.LeaseRenewalCost", "&eRenewal cost for area &6%1 &eis &6%2", writer, conf, true);
	GetConfig("Language.Economy.LeaseInfinite", "&eLease time set to infinite...", writer, conf, true);
	GetConfig("Language.Economy.MarketDisabled", "&cEconomy Disabled!", writer, conf, true);
	GetConfig("Language.Economy.SellAmount", "&eSell Amount: &2%1", writer, conf, true);
	GetConfig("Language.Economy.LeaseExpire", "&eLease Expire Time: &2%1", writer, conf, true);

	GetConfig("Language.Expanding.North", "&eExpanding North &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Expanding.West", "&eExpanding West &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Expanding.South", "&eExpanding South &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Expanding.East", "&eExpanding East &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Expanding.Up", "&eExpanding Up &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Expanding.Down", "&eExpanding Down &6%1 &eblocks", writer, conf, true);

	GetConfig("Language.Contracting.North", "&eContracting North &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Contracting.West", "&eContracting West &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Contracting.South", "&eContracting South &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Contracting.East", "&eContracting East &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Contracting.Up", "&eContracting Up &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Contracting.Down", "&eContracting Down &6%1 &eblocks", writer, conf, true);

	GetConfig("Language.Shifting.North", "&eShifting North &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Shifting.West", "&eShifting West &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Shifting.South", "&eShifting South &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Shifting.East", "&eShifting East &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Shifting.Up", "&eShifting Up &6%1 &eblocks", writer, conf, true);
	GetConfig("Language.Shifting.Down", "&eShifting Down &6%1 &eblocks", writer, conf, true);

	GetConfig("Language.Limits.PGroup", "&7- &ePermissions Group:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.RGroup", "&7- &eResidence Group:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.Admin", "&7- &eResidence Admin:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.CanCreate", "&7- &eCan Create Residences:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxRes", "&7- &eMax Residences:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxEW", "&7- &eMax East/West Size:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxNS", "&7- &eMax North/South Size:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxUD", "&7- &eMax Up/Down Size:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MinMax", "&7- &eMin/Max Protection Height:&3 %1 to %2", writer, conf, true);
	GetConfig("Language.Limits.MaxSub", "&7- &eMax Subzone Depth:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxRents", "&7- &eMax Rents:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.EnterLeave", "&7- &eCan Set Enter/Leave Messages:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.NumberOwn", "&7- &eNumber of Residences you own:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.Cost", "&7- &eResidence Cost Per Block:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.Sell", "&7- &eResidence Sell Cost Per Block:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.Flag", "&7- &eFlag Permissions:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.MaxDays", "&7- &eMax Lease Days:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.LeaseTime", "&7- &eLease Time Given on Renew:&3 %1", writer, conf, true);
	GetConfig("Language.Limits.RenewCost", "&7- &eRenew Cost Per Block:&3 %1", writer, conf, true);

	GetConfig("Language.Gui.Set.Title", "&6%1 flags", writer, conf, true);
	GetConfig("Language.Gui.Pset.Title", "&6%1 %2 flags", writer, conf, true);
	GetConfig("Language.Gui.Actions", Arrays.asList("&2Left click to enable", "&cRight click to disable", "&eShift + left click to remove"), writer, conf, true);

	GetConfig("Language.InformationPage.TopLine", "&e---< &a %1 &e >---", writer, conf, true);
	GetConfig("Language.InformationPage.Page", "&e-----< &6%1 &e>-----", writer, conf, true);
	GetConfig("Language.InformationPage.NextPage", "&e-----< &6%1 &e>-----", writer, conf, true);
	GetConfig("Language.InformationPage.NoNextPage", "&e-----------------------", writer, conf, true);

	GetConfig("Language.Chat.ChatChannelChange", "&eChanged residence chat channel to &6%1!", writer, conf, true);
	GetConfig("Language.Chat.ChatChannelLeave", "&eLeft residence chat", writer, conf, true);

	GetConfig("Language.Chat.JoinFirst", "&4Join residence chat channel first...", writer, conf, true);
	GetConfig("Language.Chat.InvalidChannel", "&4Invalid Channel...", writer, conf, true);
	GetConfig("Language.Chat.InvalidColor", "&4Incorrect color code", writer, conf, true);
	GetConfig("Language.Chat.NotInChannel", "&4Player is not in channel", writer, conf, true);
	GetConfig("Language.Chat.Kicked", "&6%1 &ewas kicked from &6%2 &echannel", writer, conf, true);
	GetConfig("Language.Chat.InvalidPrefixLength", "&4Prefix is to long. Allowed length: %1", writer, conf, true);
	GetConfig("Language.Chat.ChangedColor", "&eResidence chat channel color changed to %1", writer, conf, true);
	GetConfig("Language.Chat.ChangedPrefix", "&eResidence chat channel prefix changed to %1", writer, conf, true);

	GetConfig("Language.Shop.ListTopLine", "&6%1 &eShop list - Page &6%2 &eof &6%3 %4", writer, conf, true);
	GetConfig("Language.Shop.List", " &e%1. &6%2 &e(&6%3&e) %4", writer, conf, true);
	GetConfig("Language.Shop.ListVoted", "&e%1 (&6%2&e)", writer, conf, true);
	GetConfig("Language.Shop.ListLiked", "&eLikes: &0%1", writer, conf, true);

	GetConfig("Language.Shop.VotesTopLine", "&6%1 &e%2 residence vote list &6- &ePage &6%3 &eof &6%4 %5", writer, conf, true);
	GetConfig("Language.Shop.VotesList", " &e%1. &6%2 &e%3 &7%4", writer, conf, true);

	GetConfig("Language.Shop.NoDesc", "&6No description", writer, conf, true);
	GetConfig("Language.Shop.Desc", "&6Description:\n%1", writer, conf, true);
	GetConfig("Language.Shop.DescChange", "&6Description changed to: %1", writer, conf, true);
	GetConfig("Language.Shop.NewBoard", "&6Successfully added new shop sign board", writer, conf, true);
	GetConfig("Language.Shop.DeleteBoard", "&6Right click sign of board you want to delete", writer, conf, true);
	GetConfig("Language.Shop.DeletedBoard", "&6Sign board removed", writer, conf, true);
	GetConfig("Language.Shop.IncorrectBoard", "&cThis is not sign board, try performing command again and clicking correct sign", writer, conf, true);
	GetConfig("Language.Shop.InvalidSelection", "&cLeft click with selection tool top left sign and then right click bottom right", writer, conf, true);
	GetConfig("Language.Shop.VoteChanged", "&6Vote changed from &e%1 &6to &e%2 &6for &e%3 &6residence", writer, conf, true);
	GetConfig("Language.Shop.Voted", "&6You voted, and gave &e%1 &6votes to &e%2 &6residence", writer, conf, true);
	GetConfig("Language.Shop.Liked", "&6You liked &e%1 &6residence", writer, conf, true);
	GetConfig("Language.Shop.AlreadyLiked", "&6You already liked &e%1 &6residence", writer, conf, true);
	GetConfig("Language.Shop.NoVotes", "&cThere is no registered votes for this residence", writer, conf, true);
	GetConfig("Language.Shop.CantVote", "&cResidence don't have shop flag set to true", writer, conf, true);
	GetConfig("Language.Shop.VotedRange", "&6Vote range is from &e%1 &6to &e%2", writer, conf, true);
	GetConfig("Language.Shop.SignLines.1", "&e--== &8%1 &e==--", writer, conf, true);
	GetConfig("Language.Shop.SignLines.2", "&9%1", writer, conf, true);
	GetConfig("Language.Shop.SignLines.3", "&4%1", writer, conf, true);
	GetConfig("Language.Shop.SignLines.4", "&8%1&e (&8%2&e)", writer, conf, true);
	GetConfig("Language.Shop.SignLines.Likes4", "&9Likes: &8%2", writer, conf, true);

	GetConfig("Language.RandomTeleport.TpLimit", "&eYou can't teleport so fast, please wait &6%1 &esec and try again", writer, conf, true);
	GetConfig("Language.RandomTeleport.TeleportSuccess", "&eTeleported to X:&6%1&e, Y:&6%2&e, Z:&6%3 &elocation", writer, conf, true);
	GetConfig("Language.RandomTeleport.IncorrectLocation", "&6Could not find correct teleport location, please wait &e%1 &6sec and try again.", writer, conf,
	    true);
	GetConfig("Language.RandomTeleport.TeleportStarted", "&eTeleportation started, don't move for next &6%4 &esec.", writer, conf, true);

	GetConfig("Language.General.DisabledWorld", "&cResidence plugin is disabled in this world", writer, conf, true);
	GetConfig("Language.General.UseNumbers", "&cPlease use numbers...", writer, conf, true);
	writer.addComment("Language.CantPlaceLava", "Replace all text with '' to disable this message");
	GetConfig("Language.General.CantPlaceLava", "&cYou can't place lava outside residence and higher than &6%1 &cblock level", writer, conf, true);
	writer.addComment("Language.CantPlaceWater", "Replace all text with '' to disable this message");
	GetConfig("Language.General.CantPlaceWater", "&cYou can't place Water outside residence and higher than &6%1 &cblock level", writer, conf, true);
	GetConfig("Language.General.NoPermission", "&cYou dont have permission for this.", writer, conf, true);
	GetConfig("Language.General.DefaultUsage", "&eType &6/%1 ? &efor more info", writer, conf, true);
	GetConfig("Language.General.MaterialGet", "&eThe material name for ID &6%1 &eis &6%2", writer, conf, true);
	GetConfig("Language.General.MarketList", "&e---- &6Market List &e----", writer, conf, true);
	GetConfig("Language.General.Separator", "&e----------------------------------------------------", writer, conf, true);
	GetConfig("Language.General.AdminOnly", "&cOnly admins have access to this command.", writer, conf, true);
	GetConfig("Language.General.InfoTool", "&e- Info Tool: &6%1", writer, conf, true);
	GetConfig("Language.General.ListMaterialAdd", "&6%1 &eadded to the residence &6%2", writer, conf, true);
	GetConfig("Language.General.ListMaterialRemove", "&6%1 &eremoved from the residence &6%2", writer, conf, true);
	GetConfig("Language.General.ItemBlacklisted", "&cYou are blacklisted from using this item here.", writer, conf, true);
	GetConfig("Language.General.WorldPVPDisabled", "&cWorld PVP is disabled.", writer, conf, true);
	GetConfig("Language.General.NoPVPZone", "&cNo PVP zone.", writer, conf, true);
	GetConfig("Language.General.InvalidHelp", "&cInvalid help page.", writer, conf, true);

	GetConfig("Language.General.TeleportDeny", "&cYou dont have teleport access.", writer, conf, true);
	GetConfig("Language.General.TeleportSuccess", "&eTeleported!", writer, conf, true);
	GetConfig("Language.General.TeleportConfirm",
	    "&cThis teleport is not safe, you will fall for &6%1 &cblocks. Use &6/res tpconfirm &cto perform teleportation anyways.", writer, conf, true);
	GetConfig("Language.General.TeleportStarted",
	    "&eTeleportation to &6%1 &estarted, don't move for next &6%2 &esec.", writer, conf, true);
	GetConfig("Language.General.TeleportCanceled",
	    "&eTeleportation canceled!", writer, conf, true);
	GetConfig("Language.General.NoTeleportConfirm", "&eThere is no teleports waiting for confirmation!", writer, conf, true);
	GetConfig("Language.General.HelpPageHeader", "&eHelp Pages - &6%1 &e- Page <&6%2 &eof &6%3&e>", writer, conf, true);
	GetConfig("Language.General.ListExists", "&cList already exists...", writer, conf, true);
	GetConfig("Language.General.ListRemoved", "&eList removed...", writer, conf, true);
	GetConfig("Language.General.ListCreate", "&eCreated list &6%1", writer, conf, true);
	GetConfig("Language.General.PhysicalAreas", "&ePhysical Areas", writer, conf, true);
	GetConfig("Language.General.CurrentArea", "&eCurrent Area: &6%1", writer, conf, true);
	GetConfig("Language.General.TotalSize", "&eTotal size: &6%1", writer, conf, true);
	GetConfig("Language.General.TotalWorth", "&eTotal worth of residence: &6%1 &e(&6%2&e)", writer, conf, true);
	GetConfig("Language.General.NotOnline", "&eTarget player must be online.", writer, conf, true);
	GetConfig("Language.General.NextPage", "&eNext Page", writer, conf, true);
	GetConfig("Language.General.NextInfoPage", "&2| &eNext Page &2>>>", writer, conf, true);
	GetConfig("Language.General.PrevInfoPage", "&2<<< &ePrev Page &2|", writer, conf, true);
	GetConfig("Language.General.GenericPage", "&ePage &6%1 &eof &6%2", writer, conf, true);
	GetConfig("Language.General.WorldEditNotFound", "&cWorldEdit was not detected.", writer, conf, true);
	GetConfig("Language.General.CoordsTop", "&eX:&6%1 &eY:&6%2 &eZ:&6%3", writer, conf, true);
	GetConfig("Language.General.CoordsBottom", "&eX:&6%1 &eY:&6%2 &eZ:&6%3", writer, conf, true);
	GetConfig("Language.General.AdminToggleTurnOn", "&eAutomatic resadmin toggle turned &6On", writer, conf, true);
	GetConfig("Language.General.AdminToggleTurnOff", "&eAutomatic resadmin toggle turned &6Off", writer, conf, true);
	GetConfig("Language.General.NoSpawn", "&eYou do not have &6move &epermissions at your spawn point. Relocating", writer, conf, true);
	GetConfig("Language.General.CompassTargetReset", "&eYour compass has been reset", writer, conf, true);
	GetConfig("Language.General.CompassTargetSet", "&eYour compass now points to &6%1", writer, conf, true);
	GetConfig("Language.General.Ignorelist", "&2Ignorelist:&6", writer, conf, true);
	GetConfig("Language.General.Blacklist", "&cBlacklist:&6", writer, conf, true);
	GetConfig("Language.General.LandCost", "&eLand cost: &6%1", writer, conf, true);
	GetConfig("Language.General.True", "&2True", writer, conf, true);
	GetConfig("Language.General.False", "&cFalse", writer, conf, true);
	GetConfig("Language.General.Land", "&eLand: &6%1", writer, conf, true);
	GetConfig("Language.General.Cost", "&eCost: &6%1 &eper &6%2 &edays", writer, conf, true);
	GetConfig("Language.General.Status", "&eStatus: %1", writer, conf, true);
	GetConfig("Language.General.Available", "&2Available", writer, conf, true);
	GetConfig("Language.General.Size", " &eSize: &6%1", writer, conf, true);
	GetConfig("Language.General.Flags", "&eFlags: &6%1", writer, conf, true);
	GetConfig("Language.General.YourFlags", "&eYour flags: &6%1", writer, conf, true);
	GetConfig("Language.General.GroupFlags", "&eGroup flags: &6%1", writer, conf, true);
	GetConfig("Language.General.OthersFlags", "&eOthers flags: &6%1", writer, conf, true);
	GetConfig("Language.General.Moved", "&eMoved...", writer, conf, true);
	GetConfig("Language.General.Name", "&eName: &6%1", writer, conf, true);
	GetConfig("Language.General.Lists", "&eLists: &6", writer, conf, true);
	GetConfig("Language.General.Residences", "&eResidences&6", writer, conf, true);
	GetConfig("Language.General.Owner", "&eOwner: &6%1", writer, conf, true);
	GetConfig("Language.General.World", "&eWorld: &6%1", writer, conf, true);
	GetConfig("Language.General.Subzones", "&eSubzones", writer, conf, true);
	writer.addComment("Language.General.NewPlayerInfo", "The below lines represent various messages residence sends to the players.",
	    "Note that some messages have variables such as %1 that are inserted at runtime.");
	GetConfig("Language.General.NewPlayerInfo",
	    "&eIf you want to create protected area for your house, please use wooden axe to select opposite sides of your home and execute command &2/res create YourResidenceName",
	    writer, conf, true);

	writer.addComment("CommandHelp", "");

	GetConfig("CommandHelp.Description", "Contains Help for Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.Description", "Main Residence Command", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.Info", Arrays.asList("&2Use &6/res [command] ? <page> &2to view more help Information."), writer, conf, true);

	// res select
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.Description", "Selection Commands", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.Info",
	    Arrays.asList("This command selects areas for usage with residence.", "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Description", "Display selected coordinates", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Info", Arrays.asList("&eUsage: &6/res select coords"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Description", "Display selected size", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Info", Arrays.asList("&eUsage: &6/res select size"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Description", "Turns on auto selection tool", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Info", Arrays.asList("&eUsage: &6/res select auto [playername]"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Description", "Display selection cost", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Info", Arrays.asList("&eUsage: &6/res select cost"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Description", "Expand Selection Vertically", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Info",
	    Arrays.asList("&eUsage: &6/res select vert", "Will expand selection as high and as low as allowed."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Description", "Expand Selection to Sky", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Info", Arrays.asList("&eUsage: &6/res select sky",
	    "Expands as high as your allowed to go."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Description", "Expand Selection to Bedrock", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Info",
	    Arrays.asList("&eUsage: &6/res select bedrock", "Expands as low as your allowed to go."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Description", "Expand selection in a direction.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Info",
	    Arrays.asList("&eUsage: &6/res select expand <amount>", "Expands <amount> in the direction your looking."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Description", "Shift selection in a direction", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Info",
	    Arrays.asList("&eUsage: &6/res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Description", "Select the chunk your currently in.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Info",
	    Arrays.asList("&eUsage: &6/res select chunk", "Selects the chunk your currently standing in."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Description", "Select a existing area in a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Info",
	    Arrays.asList("&eUsage: &6/res select residence <residence>", "Selects a existing area in a residence."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Description", "Set selection using the current WorldEdit selection.",
	    writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Info",
	    Arrays.asList("&eUsage: &6/res select worldedit", "Sets selection area using the current WorldEdit selection."), writer, conf, true);

	// res create
	GetConfig("CommandHelp.SubCommands.res.SubCommands.create.Description", "Create Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.create.Info", Arrays.asList("&eUsage: &6/res create <residence name>"), writer, conf, true);

	// res remove
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Description", "Remove residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Info", Arrays.asList("&eUsage: &6/res remove <residence name>"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Args", "[residence]", writer, conf, true);

	// res padd
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Description", "Add player to residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Info", Arrays.asList("&eUsage: &6/res padd <residence name> [player]",
	    "Adds essential flags for player"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Args", "[residence] [playername]", writer, conf, true);

	// res pdel
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Description", "Remove player from residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Info", Arrays.asList("&eUsage: &6/res pdel <residence name> [player]",
	    "Removes essential flags from player"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Args", "[residence] [playername]", writer, conf, true);

	// res info
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Description", "Show info on a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Info",
	    Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Args", "[residence]", writer, conf, true);

	// res set
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Description", "Set general flags on a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Info", Arrays.asList("&eUsage: &6/res set <residence> [flag] [true/false/remove]",
	    "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Args", "[residence] [flag] [true/false/remove]", writer, conf, true);

	// res pset
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pset.Description", "Set flags on a specific player for a Residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pset.Info", Arrays.asList("&eUsage: &6/res pset <residence> [player] [flag] [true/false/remove]",
	    "&eUsage: &6/res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pset.Args", "[residence] [playername] [flag] [true/false/remove]", writer, conf, true);

	// res flags
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.Description", "List of flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.Info",
	    Arrays.asList("For flag values, usually true allows the action, and false denys the action."), writer, conf, true);

	FlagList.clear();
	// build
	FlagList.add("build");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.build.Description",
	    "allows or denys building", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.build.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// use
	FlagList.add("use");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.use.Description",
	    "allows or denys use of doors, lever, buttons, etc...", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.use.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// move
	FlagList.add("move");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.move.Description",
	    "allows or denys movement in the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.move.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// container
	FlagList.add("container");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.container.Description",
	    "allows or denys use of furnaces, chests, dispensers, etc...", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.container.Info",
	    Arrays.asList("&eUsage: &6/res set/pset  <residence> [flag] true/false/remove"), writer, conf, true);
	// trusted
	FlagList.add("trusted");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trusted.Description",
	    "gives build, use, move, container and tp flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trusted.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"), writer, conf, true);
	// place
	FlagList.add("place");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.place.Description",
	    "allows or denys only placement of blocks, overrides the build flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.place.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// destroy
	FlagList.add("destroy");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.destroy.Description",
	    "allows or denys only destruction of blocks, overrides the build flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.destroy.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// pvp
	FlagList.add("pvp");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pvp.Description",
	    "allow or deny pvp in the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pvp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// tp
	FlagList.add("tp");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tp.Description",
	    "allow or disallow teleporting to the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// enderpearl
	FlagList.add("enderpearl");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enderpearl.Description",
	    "allow or disallow teleporting to the residence with enderpearl", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enderpearl.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// admin
	FlagList.add("admin");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.admin.Description",
	    "gives a player permission to change flags on a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.admin.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"), writer, conf, true);
	// subzone
	FlagList.add("subzone");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.subzone.Description",
	    "allow a player to make subzones in the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.subzone.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"), writer, conf, true);
	// monsters
	FlagList.add("monsters");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.monsters.Description",
	    "allows or denys monster spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.monsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// cmonsters
	FlagList.add("cmonsters");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cmonsters.Description",
	    "allows or denys custom monster spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cmonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// smonsters
	FlagList.add("smonsters");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.smonsters.Description",
	    "allows or denys spawner or spawn egg monster spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.smonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// nmonsters
	FlagList.add("nmonsters");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nmonsters.Description",
	    "allows or denys natural monster spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nmonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// animals
	FlagList.add("animals");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animals.Description",
	    "allows or denys animal spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// canimals
	FlagList.add("canimals");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.canimals.Description",
	    "allows or denys custom animal spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.canimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// sanimals
	FlagList.add("sanimals");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sanimals.Description",
	    "allows or denys spawner or spawn egg animal spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sanimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// nanimals
	FlagList.add("nanimals");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nanimals.Description",
	    "allows or denys natural animal spawns", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nanimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// animalkilling
	FlagList.add("animalkilling");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animalkilling.Description",
	    "allows or denys animal killing", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animalkilling.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// mobkilling
	FlagList.add("mobkilling");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobkilling.Description",
	    "allows or denys mob killing", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobkilling.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// nofly
	FlagList.add("nofly");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nofly.Description",
	    "allows or denys fly in residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nofly.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// vehicledestroy
	FlagList.add("vehicledestroy");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.vehicledestroy.Description",
	    "allows or denys vehicle destroy", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.vehicledestroy.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// shear
	FlagList.add("shear");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shear.Description",
	    "allows or denys sheep shear", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shear.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// dye
	FlagList.add("dye");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dye.Description",
	    "allows or denys sheep dyeing", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dye.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// leash
	FlagList.add("leash");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.leash.Description",
	    "allows or denys aninal leash", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.leash.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// healing
	FlagList.add("healing");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.healing.Description",
	    "setting to true makes the residence heal its occupants", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.healing.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// feed
	FlagList.add("feed");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.feed.Description",
	    "setting to true makes the residence feed its occupants", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.feed.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// tnt
	FlagList.add("tnt");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tnt.Description",
	    "allow or deny tnt explosions", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tnt.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// creeper
	FlagList.add("creeper");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.creeper.Description",
	    "allow or deny creeper explosions", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.creeper.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// ignite
	FlagList.add("ignite");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.ignite.Description",
	    "allows or denys fire ignition", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.ignite.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// firespread
	FlagList.add("firespread");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.firespread.Description",
	    "allows or denys fire spread", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.firespread.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// bucket
	FlagList.add("bucket");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucket.Description",
	    "allow or deny bucket use", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucket.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// bucketfill
	FlagList.add("bucketfill");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketfill.Description",
	    "allow or deny bucket fill", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketfill.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// bucketempty
	FlagList.add("bucketempty");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketempty.Description",
	    "allow or deny bucket empty", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketempty.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// flow
	FlagList.add("flow");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.flow.Description",
	    "allows or denys liquid flow", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.flow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// lavaflow
	FlagList.add("lavaflow");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lavaflow.Description",
	    "allows or denys lava flow, overrides flow", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lavaflow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// waterflow
	FlagList.add("waterflow");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.waterflow.Description",
	    "allows or denys water flow, overrides flow", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.waterflow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// damage
	FlagList.add("damage");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.damage.Description",
	    "allows or denys all entity damage within the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.damage.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// piston
	FlagList.add("piston");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.piston.Description",
	    "allow or deny pistons from pushing or pulling blocks in the residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.piston.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// hidden
	FlagList.add("hidden");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hidden.Description",
	    "hides residence from list or listall commands", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hidden.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// cake
	FlagList.add("cake");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cake.Description",
	    "allows or denys players to eat cake", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cake.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// lever
	FlagList.add("lever");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lever.Description",
	    "allows or denys players to use levers", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lever.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// button
	FlagList.add("button");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.button.Description",
	    "allows or denys players to use buttons", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.button.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// diode
	FlagList.add("diode");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.diode.Description",
	    "allows or denys players to use redstone repeaters", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.diode.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// door
	FlagList.add("door");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.door.Description",
	    "allows or denys players to use doors and trapdoors", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.door.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// table
	FlagList.add("table");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.table.Description",
	    "allows or denys players to use workbenches", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.table.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// enchant
	FlagList.add("enchant");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enchant.Description",
	    "allows or denys players to use enchanting tables", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enchant.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// brew
	FlagList.add("brew");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.brew.Description",
	    "allows or denys players to use brewing stands", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.brew.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// bed
	FlagList.add("bed");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bed.Description",
	    "allows or denys players to use beds", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bed.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// pressure
	FlagList.add("pressure");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pressure.Description",
	    "allows or denys players to use pressure plates", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pressure.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// note
	FlagList.add("note");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.note.Description",
	    "allows or denys players to use note blocks", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.note.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// redstone
	FlagList.add("redstone");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.redstone.Description",
	    "Gives lever, diode, button, pressure, note flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.redstone.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// craft
	FlagList.add("craft");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.craft.Description",
	    "Gives table, enchant, brew flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.craft.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// trample
	FlagList.add("trample");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trample.Description",
	    "Allows or denys crop trampling in residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trample.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// trade
	FlagList.add("trade");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trade.Description",
	    "Allows or denys villager trading in residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trade.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// nomobs
	FlagList.add("nomobs");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nomobs.Description",
	    "Prevents monsters from entering residence residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nomobs.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// explode
	FlagList.add("explode");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.explode.Description",
	    "Allows or denys explosions in residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.explode.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// witherdamage
	FlagList.add("witherdamage");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.witherdamage.Description",
	    "Disables wither damage in residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.witherdamage.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// fireball
	FlagList.add("fireball");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.fireball.Description",
	    "Allows or denys fire balls in residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.fireball.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// command
	FlagList.add("command");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.command.Description",
	    "Allows or denys comamnd use in residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.command.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// overridepvp
	FlagList.add("overridepvp");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.overridepvp.Description",
	    "Overrides any plugin pvp protection", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.overridepvp.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// keepinv
	FlagList.add("keepinv");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepinv.Description",
	    "Players keeps inventory after death", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepinv.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// keepexp
	FlagList.add("keepexp");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepexp.Description",
	    "Players keeps exp after death", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepexp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"), writer, conf, true);
	// burn
	FlagList.add("burn");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.burn.Description",
	    "allows or denys Mob combustion in residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.burn.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// bank
	FlagList.add("bank");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bank.Description",
	    "allows or denys deposit/withdraw money from res bank", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bank.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// shop
	FlagList.add("shop");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shop.Description",
	    "adds residence to special residence shop list", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// day
	FlagList.add("day");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.day.Description",
	    "sets day time in residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.day.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// night
	FlagList.add("night");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.night.Description",
	    "sets night time in residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.night.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// chat
	FlagList.add("chat");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.chat.Description",
	    "Allows to join residence chat room", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.chat.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// nodurability
	FlagList.add("nodurability");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nodurability.Description",
	    "Prevents item durability loss", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nodurability.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// mobitemdrop
	FlagList.add("mobitemdrop");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobitemdrop.Description",
	    "Prevents mob droping items on death", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobitemdrop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// mobexpdrop
	FlagList.add("mobexpdrop");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobexpdrop.Description",
	    "Prevents mob droping exp on death", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobexpdrop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// dragongrief
	FlagList.add("dragongrief");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dragongrief.Description",
	    "Prevents ender dragon block griefing", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dragongrief.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// snowtrail
	FlagList.add("snowtrail");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowtrail.Description",
	    "Prevents snowman snow trails", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowtrail.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);
	// respawn
	FlagList.add("respawn");
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.respawn.Description",
	    "Automaticaly respawns player", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.respawn.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"), writer, conf, true);

	// Filling with custom flags info
	Set<String> sec = conf.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands").getKeys(false);
	for (String one : sec) {
	    if (FlagList.contains(one.toLowerCase()))
		continue;
	    String desc = conf.getString("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one + ".Description");
	    GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one.toLowerCase() + ".Description",
		desc, writer, conf, true);
	    List<String> info = conf.getStringList("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one + ".Info");
	    GetConfig("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one.toLowerCase() + ".Info",
		info, writer, conf, true);
	    FlagList.add(one.toLowerCase());
	}

	//res limits
	GetConfig("CommandHelp.SubCommands.res.SubCommands.limits.Description", "Show your limits.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.limits.Info",
	    Arrays.asList("&eUsage: &6/res limits", "Shows the limitations you have on creating and managing residences."), writer, conf, true);

	// res tpset
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpset.Description", "Set the teleport location of a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpset.Info",
	    Arrays.asList("&eUsage: &6/res tpset", "This will set the teleport location for a residence to where your standing.",
		"You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."),
	    writer, conf, true);

	// res tp
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Description", "Teleport to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Info",
	    Arrays.asList("&eUsage: &6/res tp [residence]", "Teleports you to a residence, you must have +tp flag access or be the owner.",
		"Your permission group must also be allowed to teleport by the server admin."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Args", "[residence]", writer, conf, true);

	// res rt
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rt.Description", "Teleports to random location in world", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rt.Info",
	    Arrays.asList("&eUsage: &6/res rt", "Teleports you to random location in defined world."), writer, conf, true);

	// res rc
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.Description", "Joins current or defined residence chat chanel", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.Info",
	    Arrays.asList("&eUsage: &6/res rc (residence)", "Teleports you to random location in defined world."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.leave.Description", "Leaves current residence chat chanel", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.leave.Info", Arrays.asList("&eUsage: &6/res rc leave",
	    "If you are in residence chat cnahel then you will leave it"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setcolor.Description", "Sets residence chat chanel text color", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setcolor.Info", Arrays.asList("&eUsage: &6/res rc setcolor [colorCode]",
	    "Sets residence chat chanel text color"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setprefix.Description", "Sets residence chat chanel prefix", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setprefix.Info", Arrays.asList("&eUsage: &6/res rc setprefix [newName]",
	    "Sets residence chat chanel prefix"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.kick.Description", "Kicks player from chanel", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.kick.Info", Arrays.asList("&eUsage: &6/res rc kick [player]",
	    "Kicks player from chanel"), writer, conf, true);

	// res expand
	GetConfig("CommandHelp.SubCommands.res.SubCommands.expand.Description", "Expands residence in direction you looking", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.expand.Info",
	    Arrays.asList("&eUsage: &6/res expand (residence) [amount]", "Expands residence in direction you looking.", "Residence name is optional"), writer, conf, true);

	// res contract
	GetConfig("CommandHelp.SubCommands.res.SubCommands.contract.Description", "Contracts residence in direction you looking", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.contract.Info",
	    Arrays.asList("&eUsage: &6/res contract (residence [amount])", "Contracts residence in direction you looking.", "Residence name is optional"), writer, conf,
	    true);

	// res shop
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.Description", "Manage residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.Info", Arrays.asList("Manages residence shop feature"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Description", "Shows list of res shops", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Info", Arrays.asList("&eUsage: &6/res shop list",
	    "Shows full list of all residences with shop flag"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Description", "Vote for residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Info", Arrays.asList("&eUsage: &6/res shop vote <residence> [amount]",
	    "Votes for current or defined residence"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Description", "Give like for residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Info", Arrays.asList("&eUsage: &6/res shop like <residence>",
	    "Gives like for residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Description", "Shows res shop votes", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Info", Arrays.asList("&eUsage: &6/res shop votes <residence> <page>",
	    "Shows full vote list of current or defined residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Description", "Shows res shop likes", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Info", Arrays.asList("&eUsage: &6/res shop likes <residence> <page>",
	    "Shows full like list of current or defined residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Description", "Sets residence shop description", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Info", Arrays.asList("&eUsage: &6/res shop setdesc [text]",
	    "Sets residence shop description. Color code supported. For new line use /n"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Description", "Create res shop board", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Info", Arrays.asList("&eUsage: &6/res shop createboard [place]",
	    "Creates res shop board from selected area. Place - position from which to start filling board"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Description", "Deletes res shop board", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Info", Arrays.asList("&eUsage: &6/res shop deleteboard",
	    "Deletes res shop board bi right clicking on one of signs"), writer, conf, true);

	// res tpconfirm
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Description", "Ignore unsafe teleportation warning", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Info",
	    Arrays.asList("&eUsage: &6/res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."),
	    writer, conf, true);

	// res subzone
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Description", "Create subzones in residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Info",
	    Arrays.asList("&eUsage: &6/res subzone <residence name> [subzone name]", "If residence name is left off, will attempt to use residence your standing in."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.Description", "Manage physical areas for a residence.", writer, conf, true);
	//res area
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Description", "List physical areas in a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res area list [residence] <page>"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Description", "List coordinates and other Info for areas", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Info", Arrays.asList("&eUsage: &6/res area listall [residence] <page>"), writer,
	    conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Description", "Add physical areas to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Info",
	    Arrays.asList("&eUsage: &6/res area add [residence] [areaID]", "You must first select two points first."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Description", "Remove physical areas from a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Info", Arrays.asList("&eUsage: &6/res area remove [residence] [areaID]"), writer,
	    conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Description", "Replace physical areas in a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Info", Arrays.asList("&eUsage: &6/res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Args", "[residence]", writer, conf, true);

	// res message
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Description", "Manage residence enter / leave messages", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Info", Arrays.asList("&eUsage: &6/res message <residence> [enter/leave] [message]",
	    "Set the enter or leave message of a residence.", "&eUsage: &6/res message <residence> remove [enter/leave]", "Removes a enter or leave message."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Args", "[residence] [enter/leave]", writer, conf, true);

	// res lease
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.Description", "Manage residence leases", writer, conf, true);
	GetConfig(
	    "CommandHelp.SubCommands.res.SubCommands.lease.Info", Arrays.asList("&eUsage: &6/res lease [renew/cost] [residence]",
		"/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.Args", "[renew/cost] [residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Description", "Set the lease time", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease set [residence] [#days/infinite]", "Sets the lease time to a specified number of days, or infinite."), writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Description", "Renew the lease time", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease renew <residence>", "Renews the lease time for current or specified residence."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Description", "Lease end date", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease expires <residence>", "Shows when expires residence lease time."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Description", "Shows renew cost", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease cost <residence>", "Shows how much money you need to renew residence lease."), writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Args", "[residence]", writer, conf, true);

	// res bank
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Description", "Manage money in a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Info",
	    Arrays.asList("&eUsage: &6/res bank [deposit/withdraw] <residence> [amount]", "You must be standing in a Residence or provide residence name",
		"You must have the +bank flag."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Args", "[deposit/withdraw] [residence]", writer, conf, true);

	// res confirm
	GetConfig("CommandHelp.SubCommands.res.SubCommands.confirm.Description", "Confirms removal of a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.confirm.Info", Arrays.asList("&eUsage: &6/res confirm", "Confirms removal of a residence."), writer, conf,
	    true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.gset.Description", "Set flags on a specific group for a Residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Description", "Change blacklist and ignorelist options", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Info",
	    Arrays.asList("&eUsage: &6/res lset <residence> [blacklist/ignorelist] [material]",
		"&eUsage: &6/res lset <residence> Info",
		"Blacklisting a material prevents it from being placed in the residence.",
		"Ignorelist causes a specific material to not be protected by Residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Args", "[residence] [blacklist/ignorelist] [material]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Description", "Remove all residences owned by a player.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Info", Arrays.asList("&eUsage: &6/res removeall [owner]",
	    "Removes all residences owned by a specific player.'",
	    "Requires /resadmin if you use it on anyone besides yourself."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Description", "List Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res list <player> <page>",
		"Lists all the residences a player owns (except hidden ones).",
		"If listing your own residences, shows hidden ones as well.",
		"To list everyones residences, use /res listall."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Description", "List Hidden Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Info",
	    Arrays.asList("&eUsage: &6/res listhidden <player> <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listall.Description", "List All Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listall.Info",
	    Arrays.asList("&eUsage: &6/res listall <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listallhidden.Description", "List All Hidden Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listallhidden.Info",
	    Arrays.asList("&eUsage: &6/res listhidden <page>",
		"Lists all hidden residences on the server."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Description", "List Residence Subzones", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Info",
	    Arrays.asList("&eUsage: &6/res sublist <residence> <page>",
		"List subzones within a residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.reset.Description", "Reset residence to default flags.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.reset.Info",
	    Arrays.asList("&eUsage: &6/res reset <residence>",
		"Resets the flags on a residence to their default.  You must be the owner or an admin to do this."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.reset.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Description", "Renames a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Info",
	    Arrays.asList("&eUsage: &6/res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
		"The name must not already be taken by another residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Description", "Kicks player from residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Info",
	    Arrays.asList("&eUsage: &6/res kick <player>", "You must be the owner or an admin to do this.",
		"Player should be online."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Description", "Mirrors Flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("&eUsage: &6/res mirror [Source Residence] [Target Residence]",
		"Mirrors flags from one residence to another.  You must be owner of both or a admin to do this."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Args", "[residence] [residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.Description", "Buy, Sell, or Rent Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("&eUsage: &6/res market ? for more Info"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Description", "Get economy Info on residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Info",
	    Arrays.asList("&eUsage: &6/res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Description", "Lists rentable and for sale residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res market list"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Description", "Sell a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Info",
	    Arrays.asList("&eUsage: &6/res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
		"Another player can buy the residence with /res market buy"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Description", "Set market sign", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Info",
	    Arrays.asList("&eUsage: &6/res market sign [residence]", "Sets market sign you are looking at."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Description", "Buy a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Info",
	    Arrays.asList("&eUsage: &6/res market buy [residence]", "Buys a Residence if its for sale."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Description", "Stops selling a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Info",
	    Arrays.asList("&eUsage: &6/res market unsell [residence]"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Description", "ent a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Info",
	    Arrays.asList("&eUsage: &6/res market rent [residence] <autorenew>",
		"Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Args", "[cresidence] [true/false]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Description", "Make a residence rentable.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Info",
	    Arrays.asList("&eUsage: &6/res market rentable [residence] [cost] [days] <repeat>",
		"Makes a residence rentable for [cost] money for every [days] number of days.  If <repeat> is true, the residence will automatically be able to be rented again after the current rent expires."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Description", "Remove a residence from rent or rentable.", writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Info",
	    Arrays.asList("&eUsage: &6/res market release [residence]", "If you are the renter, this command releases the rent on the house for you.",
		"If you are the owner, this command makes the residence not for rent anymore."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.current.Description", "Show residence your currently in.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.current.Info",
	    Arrays.asList("&eUsage: &6/res current"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.signupdate.Description", "Updated residence signs", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.signupdate.Info",
	    Arrays.asList("&eUsage: &6/res signupdate"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.Description", "Predefined permission lists", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.Info",
	    Arrays.asList("Predefined permissions that can be applied to a residence."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Description", "Add a list", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Info",
	    Arrays.asList("&eUsage: &6/res lists add <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Description", "Remove a list", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Info",
	    Arrays.asList("&eUsage: &6/res lists remove <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Description", "Apply a list to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Info",
	    Arrays.asList("&eUsage: &6/res lists apply <listname> <residence>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Description", "Set a flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Info",
	    Arrays.asList("&eUsage: &6/res lists set <listname> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Description", "Set a player flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Info",
	    Arrays.asList("&eUsage: &6/res lists pset <listname> <player> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Description", "Set a group flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res lists gset <listname> <group> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.view.Description", "View a list.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res lists view <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Description", "Make land server owned.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Info",
	    Arrays.asList("&eUsage: &6/resadmin server [residence]", "Make a residence server owned."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Args", "[cresidence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Description", "Change owner of a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Info",
	    Arrays.asList("&eUsage: &6/resadmin setowner [residence] [player]"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Args", "[cresidence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
	    Arrays.asList("&eUsage: &6/resreload"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.resload.Info",
	    Arrays.asList("&eUsage: &6/resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeworld.Description", "Remove all residences from world", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeworld.Info",
	    Arrays.asList("&eUsage: &6/res removeworld [worldname]", "Can only be used from console"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.signconvert.Description", "Converts signs from ResidenceSign plugin", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.signconvert.Info",
	    Arrays.asList("&eUsage: &6/res signconvert", "Will try to convert saved sign data from 3rd party plugin"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.version.Description", "how residence version", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.version.Info",
	    Arrays.asList("&eUsage: &6/res version"),
	    writer, conf, true);

	// Write back config
	try {
	    writer.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
