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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bekvon.bukkit.residence.containers.ConfigReader;

public class LocaleManager {

    public static ArrayList<String> FlagList = new ArrayList<String>();
    private Residence plugin;

    public LocaleManager(Residence plugin) {
	this.plugin = plugin;
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
	ConfigReader c = new ConfigReader(conf, writer);
	c.getC().options().copyDefaults(true);

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

	c.getW().options().header(header.toString());

	c.get("Language.Invalid.Player", "&cInvalid player name...");
	c.get("Language.Invalid.World", "&cInvalid world...");
	c.get("Language.Invalid.Residence", "&cInvalid Residence...");
	c.get("Language.Invalid.Subzone", "&cInvalid Subzone...");
	c.get("Language.Invalid.Direction", "&cInvalid Direction...");
	c.get("Language.Invalid.Amount", "&cInvalid Amount...");
	c.get("Language.Invalid.Cost", "&cInvalid Cost...");
	c.get("Language.Invalid.Days", "&cInvalid number of days...");
	c.get("Language.Invalid.Material", "&cInvalid Material...");
	c.get("Language.Invalid.Boolean", "&cInvalid value, must be &6true(t) &cor &6false(f)");
	c.get("Language.Invalid.Area", "&cInvalid Area...");
	c.get("Language.Invalid.Group", "&cInvalid Group...");
	c.get("Language.Invalid.MessageType", "&cMessage type must be enter or remove.");
	c.get("Language.Invalid.Flag", "&cInvalid Flag...");
	c.get("Language.Invalid.FlagState", "&cInvalid flag state, must be &6true(t)&c, &6false(f)&c, or &6remove(r)");
	c.get("Language.Invalid.List", "&eUnknown list type, must be &6blacklist &eor &6ignorelist.");
	c.get("Language.Invalid.Page", "&eInvalid Page...");
	c.get("Language.Invalid.Help", "&cInvalid Help Page...");
	c.get("Language.Invalid.NameCharacters", "&cName contained unallowed characters...");

	c.get("Language.Area.Exists", "&cArea name already exists.");
	c.get("Language.Area.Create", "&eResidence Area created, ID &6%1");
	c.get("Language.Area.DiffWorld", "&cArea is in a different world from residence.");
	c.get("Language.Area.Collision", "&cArea collides with residence &6%1");
	c.get("Language.Area.SubzoneCollision", "&cArea collides with subzone &6%1");
	c.get("Language.Area.NonExist", "&cNo such area exists.");
	c.get("Language.Area.InvalidName", "&cInvalid Area Name...");
	c.get("Language.Area.ToSmallTotal", "&cSelected area smaller than allowed minimal (&6%1&c)");
	c.get("Language.Area.ToSmallX", "&cYour &6X &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more.");
	c.get("Language.Area.ToSmallY", "&cYour selection height (&6%1&c) is too small. &eAllowed &6%2 &eand more.");
	c.get("Language.Area.ToSmallZ", "&cYour &6Z &cselection length (&6%1&c) is too small. &eAllowed &6%2 &eand more.");
	c.get("Language.Area.Rename", "&eRenamed area &6%1 &eto &6%2");
	c.get("Language.Area.Remove", "&eRemoved area &6%1...");
	c.get("Language.Area.Name", "&eName: &2%1");
	c.get("Language.Area.RemoveLast", "&cCannot remove the last area in a residence.");
	c.get("Language.Area.NotWithinParent", "&cArea is not within parent area.");
	c.get("Language.Area.Update", "&eArea Updated...");
	c.get("Language.Area.MaxPhysical", "&eYou've reached the max physical areas allowed for your residence.");
	c.get("Language.Area.SizeLimit", "&eArea size is not within your allowed limits.");
	c.get("Language.Area.HighLimit", "&cYou cannot protect this high up, your limit is &6%1");
	c.get("Language.Area.LowLimit", "&cYou cannot protect this deep, your limit is &6%1");

	c.get("Language.Select.Points", "&eSelect two points first before using this command!");
	c.get("Language.Select.Overlap", "&cSelected points overlap with &6%1 &cregion!");
	c.get("Language.Select.WorldGuardOverlap", "&cSelected points overlap with &6%1 &cWorldGuard region!");
	c.get("Language.Select.Success", "&eSelection Successful!");
	c.get("Language.Select.Fail", "&cInvalid select command...");
	c.get("Language.Select.Bedrock", "&eSelection expanded to your lowest allowed limit.");
	c.get("Language.Select.Sky", "&eSelection expanded to your highest allowed limit.");
	c.get("Language.Select.Area", "&eSelected area &6%1 &eof residence &6%2");
	c.get("Language.Select.Tool", "&e- Selection Tool: &6%1");
	c.get("Language.Select.PrimaryPoint", "&ePlaced &6Primary &eSelection Point %1");
	c.get("Language.Select.SecondaryPoint", "&ePlaced &6Secondary &eSelection Point %1");
	c.get("Language.Select.Primary", "&ePrimary selection: &6%1");
	c.get("Language.Select.Secondary", "&eSecondary selection: &6%1");
	c.get("Language.Select.TooHigh", "&cWarning, selection went above top of map, limiting.");
	c.get("Language.Select.TooLow", "&cWarning, selection went below bottom of map, limiting.");
	c.get("Language.Select.TotalSize", "&eSelection total size: &6%1");
	c.get("Language.Select.AutoEnabled", "&eAuto selection mode turned &6ON&e. To disable it write &6/res select auto");
	c.get("Language.Select.AutoDisabled", "&eAuto selection mode turned &6OFF&e. To enable it again write &6/res select auto");
	c.get("Language.Select.Disabled", "&cYou don't have access to selections commands");

	c.get("Language.Sign.Updated", "&6%1 &esigns updated!");
	c.get("Language.Sign.TopLine", "[market]");
	c.get("Language.Sign.DateFormat", "YY/MM/dd HH:mm");
	c.get("Language.Sign.ForRentTopLine", "&8For Rent");
	c.get("Language.Sign.ForRentPriceLine", "&8%1&f/&8%2&f/&8%3");
	c.get("Language.Sign.ForRentResName", "&8%1");
	c.get("Language.Sign.ForRentBottomLine", "&9Available");
	c.get("Language.Sign.RentedAutorenewTrue", "&2%1");
	c.get("Language.Sign.RentedAutorenewFalse", "&c%1");
	c.get("Language.Sign.RentedTopLine", "%1");
	c.get("Language.Sign.RentedPriceLine", "&8%1&f/&8%2&f/&8%3");
	c.get("Language.Sign.RentedResName", "&8%1");
	c.get("Language.Sign.RentedBottomLine", "&1%1");
	c.get("Language.Sign.ForSaleTopLine", "&8For Sale");
	c.get("Language.Sign.ForSalePriceLine", "&8%1");
	c.get("Language.Sign.ForSaleResName", "&8%1");
	c.get("Language.Sign.ForSaleBottomLine", "&5Available");
	c.get("Language.Sign.LookAt", "&cYou are not looking at sign");

	c.get("Language.Flag.Set", "&eFlag (&6%1&e) set for &6%2 &eto &6%3 &estate");
	c.get("Language.Flag.SetFailed", "&cYou dont have access to &6%1 &cflag");
	c.get("Language.Flag.CheckTrue", "&eFlag &6%1 &eapplies to player &6%2 &efor residence &6%3&e, value = &6%4");
	c.get("Language.Flag.CheckFalse", "&eFlag &6%1 &edoes not apply to player &6%2 &efor residence.");
	c.get("Language.Flag.Cleared", "&eFlags Cleared.");
	c.get("Language.Flag.RemovedAll", "&eAll flags removed for &6%1 &ein &6%2 &eresidence.");
	c.get("Language.Flag.RemovedGroup", "&eAll flags removed for &6%1 &egroup in &6%2 &eresidence.");
	c.get("Language.Flag.Default", "&eFlags set to default.");
	c.get("Language.Flag.Deny", "&cYou dont have &6%1 &cpermission<s> here.");
	c.get("Language.Flag.SetDeny", "&cOwner does not have access to flag &6%1");
	c.get("Language.Flag.ChangeDeny", "&cYou cant change &6%1 &cflag state while there is &6%2 &cplayer(s) inside.");

	c.get("Language.Bank.NoAccess", "&cYou dont have bank access.");
	c.get("Language.Bank.Name", " &eBank: &6%1");
	c.get("Language.Bank.NoMoney", "&cNot enough money in the bank.");
	c.get("Language.Bank.Deposit", "&eYou deposit &6%1 &einto the residence bank.");
	c.get("Language.Bank.Withdraw", "&eYou withdraw &6%1 from the residence bank.");

	c.get("Language.Subzone.Rename", "&eRenamed subzone &6%1 &eto &6%2");
	c.get("Language.Subzone.Remove", "&eSubzone &6%1 &eremoved.");
	c.get("Language.Subzone.Create", "&eCreated Subzone &6%1");
	c.get("Language.Subzone.CreateFail", "&cUnable to create subzone &6%1");
	c.get("Language.Subzone.Exists", "&cSubzone &6%1 &calready exists.");
	c.get("Language.Subzone.Collide", "&cSubzone collides with subzone &6%1");
	c.get("Language.Subzone.MaxDepth", "&cYou have reached the maximum allowed subzone depth.");
	c.get("Language.Subzone.SelectInside", "&eBoth selection points must be inside the residence.");
	c.get("Language.Subzone.CantCreate", "&cYou dont have permission to create residence subzone.");
	c.get("Language.Subzone.CantDelete", "&cYou dont have permission to delete residence subzone.");
	c.get("Language.Subzone.CantDeleteNotOwnerOfParent", "&cYou are not owner of parent residence to delete this subzone.");
	c.get("Language.Subzone.CantContract", "&cYou dont have permission to contract residence subzone.");
	c.get("Language.Subzone.CantExpand", "&cYou dont have permission to expand residence subzone.");
	c.get("Language.Subzone.DeleteConfirm", "&eAre you sure you want to delete subzone &6%1&e, use &6/res confirm &eto confirm.");
	c.get("Language.Subzone.OwnerChange", "&eSubzone &6%1 &eowner changed to &6%2");

	c.get("Language.Residence.Hidden", " &e(&6Hidden&e)");
	c.get("Language.Residence.Bought", "&eYou bought residence &6%1");
	c.get("Language.Residence.Buy", "&6%1 &ehas bought residence &6%2 &efrom you.");
	c.get("Language.Residence.BuyTooBig", "&cThis residence has areas bigger then your allowed max.");
	c.get("Language.Residence.NotForSale", "&cResidence is not for sale.");
	c.get("Language.Residence.ForSale", "&eResidence &6%1 &eis now for sale for &6%2");
	c.get("Language.Residence.StopSelling", "&cResidence is no longer for sale.");
	c.get("Language.Residence.TooMany", "&cYou already own the max number of residences your allowed to.");
	c.get("Language.Residence.MaxRent", "&cYou already are renting the maximum number of residences your allowed to.");
	c.get("Language.Residence.AlreadyRent", "&cResidence is already for rent...");
	c.get("Language.Residence.NotForRent", "&cResidence not for rent...");
	c.get("Language.Residence.NotForRentOrSell", "&cResidence not for rent or sell...");
	c.get("Language.Residence.NotRented", "&cResidence not rented.");
	c.get("Language.Residence.Unrent", "&eResidence &6%1 &ehas been unrented.");
	c.get("Language.Residence.RemoveRentable", "&eResidence &6%1 &eis no longer rentable.");
	c.get("Language.Residence.ForRentSuccess", "&eResidence &6%1 &eis now for rent for &6%2 &eevery &6%3 &edays.");
	c.get("Language.Residence.RentSuccess", "&eYou have rented Residence &6%1 &efor &6%2 &edays.");
	c.get("Language.Residence.EndingRent", "&eRent time is ending for &6%1 &eon &6%2");
	c.get("Language.Residence.AlreadyRented", "&eResidence &6%1 &ehas currently been rented to &6%2");
	c.get("Language.Residence.AlreadyExists", "&cA residence named &6%1 &calready exists.");
	c.get("Language.Residence.Create", "&eYou have created residence &6%1&e!");
	c.get("Language.Residence.Rename", "&eRenamed Residence &6%1 &eto &6%2");
	c.get("Language.Residence.Remove", "&eResidence &6%1 &ehas been removed...");
	c.get("Language.Residence.MoveDeny", "&cYou dont have movement permission for Residence &6%1");
	c.get("Language.Residence.TeleportNoFlag", "&cYou dont have teleport access for that residence.");
	c.get("Language.Residence.FlagDeny", "&cYou dont have &6%1 &cpermission for Residence &6%2");
	c.get("Language.Residence.GiveLimits", "&cCannot give residence to target player, because it is outside the target players limits.");
	c.get("Language.Residence.Give", "&eYou give residence &6%1 &eto player &6%2");
	c.get("Language.Residence.Recieve", "&eYou have recieved residence &6%1 &efrom player &6%2");
	c.get("Language.Residence.List", " &a%1%2 &e- &6World&e: &6%3");
	c.get("Language.Residence.TeleportNear", "&eTeleported to near residence.");
	c.get("Language.Residence.SetTeleportLocation", "&eTeleport Location Set...");
	c.get("Language.Residence.PermissionsApply", "&ePermissions applied to residence.");
	c.get("Language.Residence.NotOwner", "&cYou are not owner of this residence");
	c.get("Language.Residence.RemovePlayersResidences", "&eRemoved all residences belonging to player &6%1");
	c.get("Language.Residence.NotIn", "&cYou are not in a Residence.");
	c.get("Language.Residence.PlayerNotIn", "&cPlayer standing not in your Residence area.");
	c.get("Language.Residence.Kicked", "&eYou were kicked from residence");
	c.get("Language.Residence.In", "&eYou are standing in Residence &6%1");
	c.get("Language.Residence.OwnerChange", "&eResidence &6%1 &eowner changed to &6%2");
	c.get("Language.Residence.NonAdmin", "&cYou are not a Residence admin.");
	c.get("Language.Residence.Line", "&eResidence: &6%1 ");
	c.get("Language.Residence.RentedBy", "&eRented by: &6%1");
	c.get("Language.Residence.MessageChange", "&eMessage Set...");
	c.get("Language.Residence.CantDeleteResidence", "&cYou dont have permission to delete residence.");
	c.get("Language.Residence.CantExpandResidence", "&cYou dont have permission to expand residence.");
	c.get("Language.Residence.CantContractResidence", "&cYou dont have permission to contract residence.");
	c.get("Language.Residence.NoResHere", "&cThere is no residence in there.");
	c.get("Language.Residence.OwnerNoPermission", "&cThe owner does not have permission for this.");
	c.get("Language.Residence.ParentNoPermission", "&cYou don't have permission to make changes to the parent zone.");
	c.get("Language.Residence.ChatDisabled", "&eResidence Chat Disabled...");
	c.get("Language.Residence.DeleteConfirm", "&eAre you sure you want to delete residence &6%1&e, use &6/res confirm &eto confirm.");

	c.get("Language.Residence.CanBeRented", "&6%1&e can be rented for &6%2 &eper &6%3 &edays. &6/res market rent");
	c.get("Language.Residence.CanBeBought", "&6%1&e can be bought for &6%2&e. &6/res market buy");

	c.get("Language.Rent.Disabled", "&cRent is disabled...");
	c.get("Language.Rent.DisableRenew", "&eResidence &6%1 &ewill now no longer re-rent upon expire.");
	c.get("Language.Rent.EnableRenew", "&eResidence &6%1 &ewill now automatically re-rent upon expire.");
	c.get("Language.Rent.NotByYou", "&cResidence is rented not by you.");
	c.get("Language.Rent.isForRent", "&2Residence available for renting.");
	c.get("Language.Rent.MaxRentDays", "&cYou cant rent for more than &6%1 &cdays at once.");
	c.get("Language.Rent.OneTime", "&cCan't extend rent time for this residence.");
	c.get("Language.Rent.Extended", "&eRent extended for aditional &6%1 &edays for &6%2 &eresidence");
	c.get("Language.Rent.Expire", "&eRent Expire Time: &6%1");
	c.get("Language.Rent.ModifyDeny", "&cCannot modify a rented residence.");
	c.get("Language.Rent.Days", "&eRent days: &6%1");
	c.get("Language.Rent.Rented", " &6(Rented)");
	c.get("Language.Rent.RentList", " &6%1&e. &6%2 &e(&6%3&e/&6%4&e/&6%5&e) - &6%6 &6%7");

	c.get("Language.command.addedAllow", "&eAdded new allowed command for &6%1 &eresidence");
	c.get("Language.command.removedAllow", "&eRemoved allowed command for &6%1 &eresidence");
	c.get("Language.command.addedBlock", "&eAdded new blocked command for &6%1 &eresidence");
	c.get("Language.command.removedBlock", "&eRemoved blocked command for &6%1 &eresidence");
	c.get("Language.command.Blocked", "&eBlocked commands: &6%1");
	c.get("Language.command.Allowed", "&eAllowed commands: &6%1");

	c.get("Language.Rentable.Land", "&eRentable Land: &6");
	c.get("Language.Rentable.AllowRenewing", "&eCan Renew: &6%1");
	c.get("Language.Rentable.StayInMarket", "&eRentable stay in market: &6%1");
	c.get("Language.Rentable.AllowAutoPay", "&eRentable allows auto pay: &6%1");
	c.get("Language.Rentable.DisableRenew", "&6%1 &ewill no longer renew rentable status upon expire.");
	c.get("Language.Rentable.EnableRenew", "&6%1 &ewill now automatically renew rentable status upon expire.");

	c.get("Language.Economy.LandForSale", "&eLand For Sale:");
	c.get("Language.Economy.NotEnoughMoney", "&cYou dont have enough money.");
	c.get("Language.Economy.MoneyCharged", "&eCharged &6%1 &eto your &6%2 &eaccount.");
	c.get("Language.Economy.MoneyAdded", "&eGot &6%1 &eto your &6%2 &eaccount.");
	c.get("Language.Economy.MoneyCredit", "&eCredited &6%1 &eto your &6%2 &eaccount.");
	c.get("Language.Economy.RentReleaseInvalid", "&eResidence &6%1 &eis not rented or for rent.");
	c.get("Language.Economy.RentSellFail", "&cCannot sell a Residence if it is for rent.");
	c.get("Language.Economy.SellRentFail", "&cCannot rent a Residence if it is for sale.");
	c.get("Language.Economy.OwnerBuyFail", "&cCannot buy your own land!");
	c.get("Language.Economy.OwnerRentFail", "&cCannot rent your own land!");
	c.get("Language.Economy.AlreadySellFail", "&eResidence already for sale!");
	c.get("Language.Economy.LeaseRenew", "&eLease valid until &6%1");
	c.get("Language.Economy.LeaseRenewMax", "&eLease renewed to maximum allowed");
	c.get("Language.Economy.LeaseNotExpire", "&eNo such lease, or lease does not expire.");
	c.get("Language.Economy.LeaseRenewalCost", "&eRenewal cost for area &6%1 &eis &6%2");
	c.get("Language.Economy.LeaseInfinite", "&eLease time set to infinite...");
	c.get("Language.Economy.MarketDisabled", "&cEconomy Disabled!");
	c.get("Language.Economy.SellAmount", "&eSell Amount: &2%1");
	c.get("Language.Economy.SellList", " &6%1&e. &6%2 &e(&6%3&e) - &6%4");
	c.get("Language.Economy.LeaseExpire", "&eLease Expire Time: &2%1");

	c.get("Language.Expanding.North", "&eExpanding North &6%1 &eblocks");
	c.get("Language.Expanding.West", "&eExpanding West &6%1 &eblocks");
	c.get("Language.Expanding.South", "&eExpanding South &6%1 &eblocks");
	c.get("Language.Expanding.East", "&eExpanding East &6%1 &eblocks");
	c.get("Language.Expanding.Up", "&eExpanding Up &6%1 &eblocks");
	c.get("Language.Expanding.Down", "&eExpanding Down &6%1 &eblocks");

	c.get("Language.Contracting.North", "&eContracting North &6%1 &eblocks");
	c.get("Language.Contracting.West", "&eContracting West &6%1 &eblocks");
	c.get("Language.Contracting.South", "&eContracting South &6%1 &eblocks");
	c.get("Language.Contracting.East", "&eContracting East &6%1 &eblocks");
	c.get("Language.Contracting.Up", "&eContracting Up &6%1 &eblocks");
	c.get("Language.Contracting.Down", "&eContracting Down &6%1 &eblocks");

	c.get("Language.Shifting.North", "&eShifting North &6%1 &eblocks");
	c.get("Language.Shifting.West", "&eShifting West &6%1 &eblocks");
	c.get("Language.Shifting.South", "&eShifting South &6%1 &eblocks");
	c.get("Language.Shifting.East", "&eShifting East &6%1 &eblocks");
	c.get("Language.Shifting.Up", "&eShifting Up &6%1 &eblocks");
	c.get("Language.Shifting.Down", "&eShifting Down &6%1 &eblocks");

	c.get("Language.Limits.PGroup", "&7- &ePermissions Group:&3 %1");
	c.get("Language.Limits.RGroup", "&7- &eResidence Group:&3 %1");
	c.get("Language.Limits.Admin", "&7- &eResidence Admin:&3 %1");
	c.get("Language.Limits.CanCreate", "&7- &eCan Create Residences:&3 %1");
	c.get("Language.Limits.MaxRes", "&7- &eMax Residences:&3 %1");
	c.get("Language.Limits.MaxEW", "&7- &eMax East/West Size:&3 %1");
	c.get("Language.Limits.MaxNS", "&7- &eMax North/South Size:&3 %1");
	c.get("Language.Limits.MaxUD", "&7- &eMax Up/Down Size:&3 %1");
	c.get("Language.Limits.MinMax", "&7- &eMin/Max Protection Height:&3 %1 to %2");
	c.get("Language.Limits.MaxSub", "&7- &eMax Subzone Depth:&3 %1");
	c.get("Language.Limits.MaxRents", "&7- &eMax Rents:&3 %1");
	c.get("Language.Limits.MaxRentDays", " &eMax Rent days:&3 %1");
	c.get("Language.Limits.EnterLeave", "&7- &eCan Set Enter/Leave Messages:&3 %1");
	c.get("Language.Limits.NumberOwn", "&7- &eNumber of Residences you own:&3 %1");
	c.get("Language.Limits.Cost", "&7- &eResidence Cost Per Block:&3 %1");
	c.get("Language.Limits.Sell", "&7- &eResidence Sell Cost Per Block:&3 %1");
	c.get("Language.Limits.Flag", "&7- &eFlag Permissions:&3 %1");
	c.get("Language.Limits.MaxDays", "&7- &eMax Lease Days:&3 %1");
	c.get("Language.Limits.LeaseTime", "&7- &eLease Time Given on Renew:&3 %1");
	c.get("Language.Limits.RenewCost", "&7- &eRenew Cost Per Block:&3 %1");

	c.get("Language.Gui.Set.Title", "&6%1 flags");
	c.get("Language.Gui.Pset.Title", "&6%1 %2 flags");
	c.get("Language.Gui.Actions", Arrays.asList("&2Left click to enable", "&cRight click to disable", "&eShift + left click to remove"));

	c.get("Language.InformationPage.TopLine", "&e---< &a %1 &e >---");
	c.get("Language.InformationPage.Page", "&e-----< &6%1 &e>-----");
	c.get("Language.InformationPage.NextPage", "&e-----< &6%1 &e>-----");
	c.get("Language.InformationPage.NoNextPage", "&e-----------------------");

	c.get("Language.Chat.ChatChannelChange", "&eChanged residence chat channel to &6%1!");
	c.get("Language.Chat.ChatChannelLeave", "&eLeft residence chat");

	c.get("Language.Chat.JoinFirst", "&4Join residence chat channel first...");
	c.get("Language.Chat.InvalidChannel", "&4Invalid Channel...");
	c.get("Language.Chat.InvalidColor", "&4Incorrect color code");
	c.get("Language.Chat.NotInChannel", "&4Player is not in channel");
	c.get("Language.Chat.Kicked", "&6%1 &ewas kicked from &6%2 &echannel");
	c.get("Language.Chat.InvalidPrefixLength", "&4Prefix is to long. Allowed length: %1");
	c.get("Language.Chat.ChangedColor", "&eResidence chat channel color changed to %1");
	c.get("Language.Chat.ChangedPrefix", "&eResidence chat channel prefix changed to %1");

	c.get("Language.Shop.ListTopLine", "&6%1 &eShop list - Page &6%2 &eof &6%3 %4");
	c.get("Language.Shop.List", " &e%1. &6%2 &e(&6%3&e) %4");
	c.get("Language.Shop.ListVoted", "&e%1 (&6%2&e)");
	c.get("Language.Shop.ListLiked", "&eLikes: &0%1");

	c.get("Language.Shop.VotesTopLine", "&6%1 &e%2 residence vote list &6- &ePage &6%3 &eof &6%4 %5");
	c.get("Language.Shop.VotesList", " &e%1. &6%2 &e%3 &7%4");

	c.get("Language.Shop.NoDesc", "&6No description");
	c.get("Language.Shop.Desc", "&6Description:\n%1");
	c.get("Language.Shop.DescChange", "&6Description changed to: %1");
	c.get("Language.Shop.NewBoard", "&6Successfully added new shop sign board");
	c.get("Language.Shop.DeleteBoard", "&6Right click sign of board you want to delete");
	c.get("Language.Shop.DeletedBoard", "&6Sign board removed");
	c.get("Language.Shop.IncorrectBoard", "&cThis is not sign board, try performing command again and clicking correct sign");
	c.get("Language.Shop.InvalidSelection", "&cLeft click with selection tool top left sign and then right click bottom right");
	c.get("Language.Shop.VoteChanged", "&6Vote changed from &e%1 &6to &e%2 &6for &e%3 &6residence");
	c.get("Language.Shop.Voted", "&6You voted, and gave &e%1 &6votes to &e%2 &6residence");
	c.get("Language.Shop.Liked", "&6You liked &e%1 &6residence");
	c.get("Language.Shop.AlreadyLiked", "&6You already liked &e%1 &6residence");
	c.get("Language.Shop.NoVotes", "&cThere is no registered votes for this residence");
	c.get("Language.Shop.CantVote", "&cResidence don't have shop flag set to true");
	c.get("Language.Shop.VotedRange", "&6Vote range is from &e%1 &6to &e%2");
	c.get("Language.Shop.SignLines.1", "&e--== &8%1 &e==--");
	c.get("Language.Shop.SignLines.2", "&9%1");
	c.get("Language.Shop.SignLines.3", "&4%1");
	c.get("Language.Shop.SignLines.4", "&8%1&e (&8%2&e)");
	c.get("Language.Shop.SignLines.Likes4", "&9Likes: &8%2");

	c.get("Language.RandomTeleport.TpLimit", "&eYou can't teleport so fast, please wait &6%1 &esec and try again");
	c.get("Language.RandomTeleport.TeleportSuccess", "&eTeleported to X:&6%1&e, Y:&6%2&e, Z:&6%3 &elocation");
	c.get("Language.RandomTeleport.IncorrectLocation", "&6Could not find correct teleport location, please wait &e%1 &6sec and try again.");
	c.get("Language.RandomTeleport.TeleportStarted", "&eTeleportation started, don't move for next &6%4 &esec.");
	c.get("Language.RandomTeleport.WorldList", "&ePossible worlds: &6%1");

	c.get("Language.General.DisabledWorld", "&cResidence plugin is disabled in this world");
	c.get("Language.General.UseNumbers", "&cPlease use numbers...");
	writer.addComment("Language.CantPlaceLava", "Replace all text with '' to disable this message");
	c.get("Language.General.CantPlaceLava", "&cYou can't place lava outside residence and higher than &6%1 &cblock level");
	writer.addComment("Language.CantPlaceWater", "Replace all text with '' to disable this message");
	c.get("Language.General.CantPlaceWater", "&cYou can't place Water outside residence and higher than &6%1 &cblock level");
	c.get("Language.General.NoPermission", "&cYou dont have permission for this.");
	c.get("Language.General.DefaultUsage", "&eType &6/%1 ? &efor more info");
	c.get("Language.General.MaterialGet", "&eThe material name for ID &6%1 &eis &6%2");
	c.get("Language.General.MarketList", "&e---- &6Market List &e----");
	c.get("Language.General.Separator", "&e----------------------------------------------------");
	c.get("Language.General.AdminOnly", "&cOnly admins have access to this command.");
	c.get("Language.General.InfoTool", "&e- Info Tool: &6%1");
	c.get("Language.General.ListMaterialAdd", "&6%1 &eadded to the residence &6%2");
	c.get("Language.General.ListMaterialRemove", "&6%1 &eremoved from the residence &6%2");
	c.get("Language.General.ItemBlacklisted", "&cYou are blacklisted from using this item here.");
	c.get("Language.General.WorldPVPDisabled", "&cWorld PVP is disabled.");
	c.get("Language.General.NoPVPZone", "&cNo PVP zone.");
	c.get("Language.General.InvalidHelp", "&cInvalid help page.");

	c.get("Language.General.TeleportDeny", "&cYou dont have teleport access.");
	c.get("Language.General.TeleportSuccess", "&eTeleported!");
	c.get("Language.General.TeleportConfirm",
	    "&cThis teleport is not safe, you will fall for &6%1 &cblocks. Use &6/res tpconfirm &cto perform teleportation anyways.");
	c.get("Language.General.TeleportStarted",
	    "&eTeleportation to &6%1 &estarted, don't move for next &6%2 &esec.");
	c.get("Language.General.TeleportCanceled",
	    "&eTeleportation canceled!");
	c.get("Language.General.NoTeleportConfirm", "&eThere is no teleports waiting for confirmation!");
	c.get("Language.General.HelpPageHeader", "&eHelp Pages - &6%1 &e- Page <&6%2 &eof &6%3&e>");
	c.get("Language.General.ListExists", "&cList already exists...");
	c.get("Language.General.ListRemoved", "&eList removed...");
	c.get("Language.General.ListCreate", "&eCreated list &6%1");
	c.get("Language.General.PhysicalAreas", "&ePhysical Areas");
	c.get("Language.General.CurrentArea", "&eCurrent Area: &6%1");
	c.get("Language.General.TotalSize", "&eTotal size: &6%1");
	c.get("Language.General.TotalWorth", "&eTotal worth of residence: &6%1 &e(&6%2&e)");
	c.get("Language.General.NotOnline", "&eTarget player must be online.");
	c.get("Language.General.NextPage", "&eNext Page");
	c.get("Language.General.NextInfoPage", "&2| &eNext Page &2>>>");
	c.get("Language.General.PrevInfoPage", "&2<<< &ePrev Page &2|");
	c.get("Language.General.GenericPage", "&ePage &6%1 &eof &6%2");
	c.get("Language.General.WorldEditNotFound", "&cWorldEdit was not detected.");
	c.get("Language.General.CoordsTop", "&eX:&6%1 &eY:&6%2 &eZ:&6%3");
	c.get("Language.General.CoordsBottom", "&eX:&6%1 &eY:&6%2 &eZ:&6%3");
	c.get("Language.General.AdminToggleTurnOn", "&eAutomatic resadmin toggle turned &6On");
	c.get("Language.General.AdminToggleTurnOff", "&eAutomatic resadmin toggle turned &6Off");
	c.get("Language.General.NoSpawn", "&eYou do not have &6move &epermissions at your spawn point. Relocating");
	c.get("Language.General.CompassTargetReset", "&eYour compass has been reset");
	c.get("Language.General.CompassTargetSet", "&eYour compass now points to &6%1");
	c.get("Language.General.Ignorelist", "&2Ignorelist:&6");
	c.get("Language.General.Blacklist", "&cBlacklist:&6");
	c.get("Language.General.LandCost", "&eLand cost: &6%1");
	c.get("Language.General.True", "&2True");
	c.get("Language.General.False", "&cFalse");
	c.get("Language.General.Removed", "&6Removed");
	c.get("Language.General.FlagState", "&eFlag state: %1");
	c.get("Language.General.Land", "&eLand: &6%1");
	c.get("Language.General.Cost", "&eCost: &6%1 &eper &6%2 &edays");
	c.get("Language.General.Status", "&eStatus: %1");
	c.get("Language.General.Available", "&2Available");
	c.get("Language.General.Size", " &eSize: &6%1");
	c.get("Language.General.ResidenceFlags", "&eResidence flags: &6%1");
	c.get("Language.General.PlayersFlags", "&ePlayers flags: &6%1");
	c.get("Language.General.GroupFlags", "&eGroup flags: &6%1");
	c.get("Language.General.OthersFlags", "&eOthers flags: &6%1");
	c.get("Language.General.Moved", "&eMoved...");
	c.get("Language.General.Name", "&eName: &6%1");
	c.get("Language.General.Lists", "&eLists: &6");
	c.get("Language.General.Residences", "&eResidences&6");
	c.get("Language.General.Owner", "&eOwner: &6%1");
	c.get("Language.General.World", "&eWorld: &6%1");
	c.get("Language.General.Subzones", "&eSubzones");
	writer.addComment("Language.General.NewPlayerInfo", "The below lines represent various messages residence sends to the players.",
	    "Note that some messages have variables such as %1 that are inserted at runtime.");
	c.get("Language.General.NewPlayerInfo",
	    "&eIf you want to create protected area for your house, please use wooden axe to select opposite sides of your home and execute command &2/res create YourResidenceName");

	writer.addComment("CommandHelp", "");

	c.get("CommandHelp.Description", "Contains Help for Residence");
	c.get("CommandHelp.SubCommands.res.Description", "Main Residence Command");
	c.get("CommandHelp.SubCommands.res.Info", Arrays.asList("&2Use &6/res [command] ? <page> &2to view more help Information."));

	// res select
	c.get("CommandHelp.SubCommands.res.SubCommands.select.Description", "Selection Commands");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.Info",
	    Arrays.asList("This command selects areas for usage with residence.", "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Description", "Display selected coordinates");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Info", Arrays.asList("&eUsage: &6/res select coords"));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Description", "Display selected size");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Info", Arrays.asList("&eUsage: &6/res select size"));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Description", "Turns on auto selection tool");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Info", Arrays.asList("&eUsage: &6/res select auto [playername]"));
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Args", "[playername]");

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Description", "Display selection cost");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Info", Arrays.asList("&eUsage: &6/res select cost"));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Description", "Expand Selection Vertically");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Info",
	    Arrays.asList("&eUsage: &6/res select vert", "Will expand selection as high and as low as allowed."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Description", "Expand Selection to Sky");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Info", Arrays.asList("&eUsage: &6/res select sky",
	    "Expands as high as your allowed to go."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Description", "Expand Selection to Bedrock");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Info",
	    Arrays.asList("&eUsage: &6/res select bedrock", "Expands as low as your allowed to go."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Description", "Expand selection in a direction.");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Info",
	    Arrays.asList("&eUsage: &6/res select expand <amount>", "Expands <amount> in the direction your looking."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Description", "Shift selection in a direction");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Info",
	    Arrays.asList("&eUsage: &6/res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Description", "Select the chunk your currently in.");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Info",
	    Arrays.asList("&eUsage: &6/res select chunk", "Selects the chunk your currently standing in."));

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Description", "Select a existing area in a residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Info",
	    Arrays.asList("&eUsage: &6/res select residence <residence>", "Selects a existing area in a residence."));
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Description", "Set selection using the current WorldEdit selection.");
	c.get("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Info",
	    Arrays.asList("&eUsage: &6/res select worldedit", "Sets selection area using the current WorldEdit selection."));

	// res create
	c.get("CommandHelp.SubCommands.res.SubCommands.create.Description", "Create Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.create.Info", Arrays.asList("&eUsage: &6/res create <residence name>"));

	// res remove
	c.get("CommandHelp.SubCommands.res.SubCommands.remove.Description", "Remove residences.");
	c.get("CommandHelp.SubCommands.res.SubCommands.remove.Info", Arrays.asList("&eUsage: &6/res remove <residence name>"));
	c.get("CommandHelp.SubCommands.res.SubCommands.remove.Args", "[residence]");

	// res padd
	c.get("CommandHelp.SubCommands.res.SubCommands.padd.Description", "Add player to residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.padd.Info", Arrays.asList("&eUsage: &6/res padd <residence name> [player]",
	    "Adds essential flags for player"));
	c.get("CommandHelp.SubCommands.res.SubCommands.padd.Args", "[residence] [playername]");

	// res pdel
	c.get("CommandHelp.SubCommands.res.SubCommands.pdel.Description", "Remove player from residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.pdel.Info", Arrays.asList("&eUsage: &6/res pdel <residence name> [player]",
	    "Removes essential flags from player"));
	c.get("CommandHelp.SubCommands.res.SubCommands.pdel.Args", "[residence] [playername]");

	// res give
	c.get("CommandHelp.SubCommands.res.SubCommands.give.Description", "Give residence to player.");
	c.get("CommandHelp.SubCommands.res.SubCommands.give.Info", Arrays.asList("&eUsage: &6/res give <residence name> [player]",
	    "Gives your owned residence to target player"));
	c.get("CommandHelp.SubCommands.res.SubCommands.give.Args", "[residence] [playername]");

	// res info
	c.get("CommandHelp.SubCommands.res.SubCommands.info.Description", "Show info on a residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.info.Info",
	    Arrays.asList("&eUsage: &6/res info <residence>", "Leave off <residence> to display info for the residence your currently in."));
	c.get("CommandHelp.SubCommands.res.SubCommands.info.Args", "[residence]");

	// res set
	c.get("CommandHelp.SubCommands.res.SubCommands.set.Description", "Set general flags on a Residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.set.Info", Arrays.asList("&eUsage: &6/res set <residence> [flag] [true/false/remove]",
	    "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"));
	c.get("CommandHelp.SubCommands.res.SubCommands.set.Args", "[residence] [flag] [true/false/remove]");

	// res pset
	c.get("CommandHelp.SubCommands.res.SubCommands.pset.Description", "Set flags on a specific player for a Residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.pset.Info", Arrays.asList("&eUsage: &6/res pset <residence> [player] [flag] [true/false/remove]",
	    "&eUsage: &6/res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"));
	c.get("CommandHelp.SubCommands.res.SubCommands.pset.Args", "[residence] [playername] [flag] [true/false/remove]");

	// res flags
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.Description", "List of flags");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.Info",
	    Arrays.asList("For flag values, usually true allows the action, and false denys the action."));

	FlagList.clear();
	// build
	FlagList.add("build");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.build.Description",
	    "allows or denys building");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.build.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// use
	FlagList.add("use");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.use.Description",
	    "allows or denys use of doors, lever, buttons, etc...");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.use.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// move
	FlagList.add("move");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.move.Description",
	    "allows or denys movement in the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.move.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// container
	FlagList.add("container");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.container.Description",
	    "allows or denys use of furnaces, chests, dispensers, etc...");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.container.Info",
	    Arrays.asList("&eUsage: &6/res set/pset  <residence> [flag] true/false/remove"));
	// trusted
	FlagList.add("trusted");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trusted.Description",
	    "gives build, use, move, container and tp flags");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trusted.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"));
	// place
	FlagList.add("place");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.place.Description",
	    "allows or denys only placement of blocks, overrides the build flag");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.place.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// destroy
	FlagList.add("destroy");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.destroy.Description",
	    "allows or denys only destruction of blocks, overrides the build flag");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.destroy.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// pvp
	FlagList.add("pvp");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pvp.Description",
	    "allow or deny pvp in the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pvp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// tp
	FlagList.add("tp");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tp.Description",
	    "allow or disallow teleporting to the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// enderpearl
	FlagList.add("enderpearl");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enderpearl.Description",
	    "allow or disallow teleporting to the residence with enderpearl");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enderpearl.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// admin
	FlagList.add("admin");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.admin.Description",
	    "gives a player permission to change flags on a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.admin.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"));
	// subzone
	FlagList.add("subzone");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.subzone.Description",
	    "allow a player to make subzones in the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.subzone.Info",
	    Arrays.asList("&eUsage: &6/res pset <residence> [flag] true/false/remove"));
	// monsters
	FlagList.add("monsters");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.monsters.Description",
	    "allows or denys monster spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.monsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// cmonsters
	FlagList.add("cmonsters");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cmonsters.Description",
	    "allows or denys custom monster spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cmonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// smonsters
	FlagList.add("smonsters");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.smonsters.Description",
	    "allows or denys spawner or spawn egg monster spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.smonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// nmonsters
	FlagList.add("nmonsters");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nmonsters.Description",
	    "allows or denys natural monster spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nmonsters.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// animals
	FlagList.add("animals");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animals.Description",
	    "allows or denys animal spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// canimals
	FlagList.add("canimals");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.canimals.Description",
	    "allows or denys custom animal spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.canimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// sanimals
	FlagList.add("sanimals");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sanimals.Description",
	    "allows or denys spawner or spawn egg animal spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sanimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// nanimals
	FlagList.add("nanimals");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nanimals.Description",
	    "allows or denys natural animal spawns");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nanimals.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// animalkilling
	FlagList.add("animalkilling");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animalkilling.Description",
	    "allows or denys animal killing");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.animalkilling.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// mobkilling
	FlagList.add("mobkilling");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobkilling.Description",
	    "allows or denys mob killing");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobkilling.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// nofly
	FlagList.add("nofly");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nofly.Description",
	    "allows or denys fly in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nofly.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// vehicledestroy
	FlagList.add("vehicledestroy");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.vehicledestroy.Description",
	    "allows or denys vehicle destroy");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.vehicledestroy.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// shear
	FlagList.add("shear");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shear.Description",
	    "allows or denys sheep shear");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shear.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// dye
	FlagList.add("dye");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dye.Description",
	    "allows or denys sheep dyeing");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dye.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// leash
	FlagList.add("leash");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.leash.Description",
	    "allows or denys aninal leash");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.leash.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// hook
	FlagList.add("hook");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hook.Description",
	    "allows or denys fishing rod hooking entities");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hook.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// healing
	FlagList.add("healing");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.healing.Description",
	    "setting to true makes the residence heal its occupants");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.healing.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// feed
	FlagList.add("feed");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.feed.Description",
	    "setting to true makes the residence feed its occupants");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.feed.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// tnt
	FlagList.add("tnt");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tnt.Description",
	    "allow or deny tnt explosions");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.tnt.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// creeper
	FlagList.add("creeper");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.creeper.Description",
	    "allow or deny creeper explosions");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.creeper.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// ignite
	FlagList.add("ignite");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.ignite.Description",
	    "allows or denys fire ignition");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.ignite.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// firespread
	FlagList.add("firespread");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.firespread.Description",
	    "allows or denys fire spread");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.firespread.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// bucket
	FlagList.add("bucket");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucket.Description",
	    "allow or deny bucket use");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucket.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// bucketfill
	FlagList.add("bucketfill");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketfill.Description",
	    "allow or deny bucket fill");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketfill.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// bucketempty
	FlagList.add("bucketempty");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketempty.Description",
	    "allow or deny bucket empty");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bucketempty.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// flow
	FlagList.add("flow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.flow.Description",
	    "allows or denys liquid flow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.flow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// lavaflow
	FlagList.add("lavaflow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lavaflow.Description",
	    "allows or denys lava flow, overrides flow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lavaflow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// waterflow
	FlagList.add("waterflow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.waterflow.Description",
	    "allows or denys water flow, overrides flow");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.waterflow.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// damage
	FlagList.add("damage");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.damage.Description",
	    "allows or denys all entity damage within the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.damage.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// piston
	FlagList.add("piston");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.piston.Description",
	    "allow or deny pistons from pushing or pulling blocks in the residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.piston.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// pistonprotection
	FlagList.add("pistonprotection");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pistonprotection.Description",
	    "Enables or disabled piston block move in or out of residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pistonprotection.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// hidden
	FlagList.add("hidden");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hidden.Description",
	    "hides residence from list or listall commands");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.hidden.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// cake
	FlagList.add("cake");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cake.Description",
	    "allows or denys players to eat cake");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.cake.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// lever
	FlagList.add("lever");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lever.Description",
	    "allows or denys players to use levers");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.lever.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// button
	FlagList.add("button");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.button.Description",
	    "allows or denys players to use buttons");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.button.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// diode
	FlagList.add("diode");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.diode.Description",
	    "allows or denys players to use redstone repeaters");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.diode.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// door
	FlagList.add("door");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.door.Description",
	    "allows or denys players to use doors and trapdoors");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.door.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// table
	FlagList.add("table");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.table.Description",
	    "allows or denys players to use workbenches");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.table.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// enchant
	FlagList.add("enchant");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enchant.Description",
	    "allows or denys players to use enchanting tables");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.enchant.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// brew
	FlagList.add("brew");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.brew.Description",
	    "allows or denys players to use brewing stands");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.brew.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// bed
	FlagList.add("bed");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bed.Description",
	    "allows or denys players to use beds");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bed.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// pressure
	FlagList.add("pressure");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pressure.Description",
	    "allows or denys players to use pressure plates");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.pressure.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// note
	FlagList.add("note");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.note.Description",
	    "allows or denys players to use note blocks");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.note.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// redstone
	FlagList.add("redstone");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.redstone.Description",
	    "Gives lever, diode, button, pressure, note flags");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.redstone.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// craft
	FlagList.add("craft");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.craft.Description",
	    "Gives table, enchant, brew flags");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.craft.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// trample
	FlagList.add("trample");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trample.Description",
	    "Allows or denys crop trampling in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trample.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// dryup
	FlagList.add("dryup");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dryup.Description",
	    "Prevents land from drying up");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dryup.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// trade
	FlagList.add("trade");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trade.Description",
	    "Allows or denys villager trading in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.trade.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// nomobs
	FlagList.add("nomobs");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nomobs.Description",
	    "Prevents monsters from entering residence residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nomobs.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// explode
	FlagList.add("explode");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.explode.Description",
	    "Allows or denys explosions in residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.explode.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
//	// witherdamage
//	FlagList.add("witherdamage");
//	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.witherdamage.Description",
//	    "Disables wither damage in residences");
//	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.witherdamage.Info",
//	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// fireball
	FlagList.add("fireball");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.fireball.Description",
	    "Allows or denys fire balls in residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.fireball.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// command
	FlagList.add("command");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.command.Description",
	    "Allows or denys comamnd use in residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.command.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// overridepvp
	FlagList.add("overridepvp");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.overridepvp.Description",
	    "Overrides any plugin pvp protection");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.overridepvp.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// keepinv
	FlagList.add("keepinv");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepinv.Description",
	    "Players keeps inventory after death");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepinv.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// keepexp
	FlagList.add("keepexp");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepexp.Description",
	    "Players keeps exp after death");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.keepexp.Info",
	    Arrays.asList("&eUsage: &6/res set/pset <residence> [flag] true/false/remove"));
	// burn
	FlagList.add("burn");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.burn.Description",
	    "allows or denys Mob combustion in residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.burn.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// bank
	FlagList.add("bank");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bank.Description",
	    "allows or denys deposit/withdraw money from res bank");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.bank.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// shop
	FlagList.add("shop");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shop.Description",
	    "Adds residence to special residence shop list");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.shop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// day
	FlagList.add("day");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.day.Description",
	    "Sets day time in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.day.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// night
	FlagList.add("night");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.night.Description",
	    "Sets night time in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.night.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// sun
	FlagList.add("sun");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sun.Description",
	    "Sets weather to sunny in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.sun.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// rain
	FlagList.add("rain");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.rain.Description",
	    "Sets weather to rainny in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.rain.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// chat
	FlagList.add("chat");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.chat.Description",
	    "Allows to join residence chat room");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.chat.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// nodurability
	FlagList.add("nodurability");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nodurability.Description",
	    "Prevents item durability loss");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.nodurability.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// mobitemdrop
	FlagList.add("mobitemdrop");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobitemdrop.Description",
	    "Prevents mob droping items on death");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobitemdrop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// mobexpdrop
	FlagList.add("mobexpdrop");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobexpdrop.Description",
	    "Prevents mob droping exp on death");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.mobexpdrop.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// dragongrief
	FlagList.add("dragongrief");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dragongrief.Description",
	    "Prevents ender dragon block griefing");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.dragongrief.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// snowtrail
	FlagList.add("snowtrail");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowtrail.Description",
	    "Prevents snowman snow trails");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowtrail.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// snowball
	FlagList.add("snowball");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowball.Description",
	    "Prevents snowball knockback");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.snowball.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// iceform
	FlagList.add("iceform");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.iceform.Description",
	    "Prevents from ice forming");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.iceform.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// icemelt
	FlagList.add("icemelt");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.icemelt.Description",
	    "Prevents ice from melting");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.icemelt.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// respawn
	FlagList.add("respawn");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.respawn.Description",
	    "Automaticaly respawns player");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.respawn.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));
	// riding
	FlagList.add("riding");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.riding.Description",
	    "Prevent riding a horse");
	c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands.riding.Info",
	    Arrays.asList("&eUsage: &6/res set <residence> [flag] true/false/remove"));

	// Filling with custom flags info
	Set<String> sec = conf.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands").getKeys(false);
	for (String one : sec) {
	    if (FlagList.contains(one.toLowerCase()))
		continue;
	    String desc = conf.getString("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one + ".Description");
	    c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one.toLowerCase() + ".Description",
		desc);
	    List<String> info = conf.getStringList("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one + ".Info");
	    c.get("CommandHelp.SubCommands.res.SubCommands.flags.SubCommands." + one.toLowerCase() + ".Info",
		info);
	    FlagList.add(one.toLowerCase());
	}

	//res limits
	c.get("CommandHelp.SubCommands.res.SubCommands.limits.Description", "Show your limits.");
	c.get("CommandHelp.SubCommands.res.SubCommands.limits.Info",
	    Arrays.asList("&eUsage: &6/res limits", "Shows the limitations you have on creating and managing residences."));

	// res tpset
	c.get("CommandHelp.SubCommands.res.SubCommands.tpset.Description", "Set the teleport location of a Residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.tpset.Info",
	    Arrays.asList("&eUsage: &6/res tpset", "This will set the teleport location for a residence to where your standing.",
		"You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."));

	// res tp
	c.get("CommandHelp.SubCommands.res.SubCommands.tp.Description", "Teleport to a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.tp.Info",
	    Arrays.asList("&eUsage: &6/res tp [residence]", "Teleports you to a residence, you must have +tp flag access or be the owner.",
		"Your permission group must also be allowed to teleport by the server admin."));
	c.get("CommandHelp.SubCommands.res.SubCommands.tp.Args", "[residence]");

	// res rt
	c.get("CommandHelp.SubCommands.res.SubCommands.rt.Description", "Teleports to random location in world");
	c.get("CommandHelp.SubCommands.res.SubCommands.rt.Info",
	    Arrays.asList("&eUsage: &6/res rt", "Teleports you to random location in defined world."));

	// res rc
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.Description", "Joins current or defined residence chat chanel");
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.Info",
	    Arrays.asList("&eUsage: &6/res rc (residence)", "Teleports you to random location in defined world."));

	// res command
	c.get("CommandHelp.SubCommands.res.SubCommands.command.Description", "Manages allowed or blocked commands in residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.command.Info",
	    Arrays.asList("&eUsage: &6/res command <residence> <allow/block/list> <command>",
		"Shows list, adds or removes allowed or disabled commands in residence",
		"Use _ to include command with multiple variables"));

	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.leave.Description", "Leaves current residence chat chanel");
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.leave.Info", Arrays.asList("&eUsage: &6/res rc leave",
	    "If you are in residence chat cnahel then you will leave it"));

	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setcolor.Description", "Sets residence chat chanel text color");
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setcolor.Info", Arrays.asList("&eUsage: &6/res rc setcolor [colorCode]",
	    "Sets residence chat chanel text color"));

	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setprefix.Description", "Sets residence chat chanel prefix");
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.setprefix.Info", Arrays.asList("&eUsage: &6/res rc setprefix [newName]",
	    "Sets residence chat chanel prefix"));

	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.kick.Description", "Kicks player from chanel");
	c.get("CommandHelp.SubCommands.res.SubCommands.rc.SubCommands.kick.Info", Arrays.asList("&eUsage: &6/res rc kick [player]",
	    "Kicks player from chanel"));

	// res expand
	c.get("CommandHelp.SubCommands.res.SubCommands.expand.Description", "Expands residence in direction you looking");
	c.get("CommandHelp.SubCommands.res.SubCommands.expand.Info",
	    Arrays.asList("&eUsage: &6/res expand (residence) [amount]", "Expands residence in direction you looking.", "Residence name is optional"));

	// res contract
	c.get("CommandHelp.SubCommands.res.SubCommands.contract.Description", "Contracts residence in direction you looking");
	c.get("CommandHelp.SubCommands.res.SubCommands.contract.Info",
	    Arrays.asList("&eUsage: &6/res contract (residence [amount])", "Contracts residence in direction you looking.", "Residence name is optional"));

	// res shop
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.Description", "Manage residence shop");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.Info", Arrays.asList("Manages residence shop feature"));

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Description", "Shows list of res shops");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Info", Arrays.asList("&eUsage: &6/res shop list",
	    "Shows full list of all residences with shop flag"));

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Description", "Vote for residence shop");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Info", Arrays.asList("&eUsage: &6/res shop vote <residence> [amount]",
	    "Votes for current or defined residence"));
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Description", "Give like for residence shop");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Info", Arrays.asList("&eUsage: &6/res shop like <residence>",
	    "Gives like for residence shop"));
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Args", "[residenceshop]");

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Description", "Shows res shop votes");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Info", Arrays.asList("&eUsage: &6/res shop votes <residence> <page>",
	    "Shows full vote list of current or defined residence shop"));
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Args", "[residenceshop]");

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Description", "Shows res shop likes");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Info", Arrays.asList("&eUsage: &6/res shop likes <residence> <page>",
	    "Shows full like list of current or defined residence shop"));
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Args", "[residenceshop]");

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Description", "Sets residence shop description");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Info", Arrays.asList("&eUsage: &6/res shop setdesc [text]",
	    "Sets residence shop description. Color code supported. For new line use /n"));

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Description", "Create res shop board");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Info", Arrays.asList("&eUsage: &6/res shop createboard [place]",
	    "Creates res shop board from selected area. Place - position from which to start filling board"));

	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Description", "Deletes res shop board");
	c.get("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Info", Arrays.asList("&eUsage: &6/res shop deleteboard",
	    "Deletes res shop board bi right clicking on one of signs"));

	// res tpconfirm
	c.get("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Description", "Ignore unsafe teleportation warning");
	c.get("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Info",
	    Arrays.asList("&eUsage: &6/res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."));

	// res subzone
	c.get("CommandHelp.SubCommands.res.SubCommands.subzone.Description", "Create subzones in residences.");
	c.get("CommandHelp.SubCommands.res.SubCommands.subzone.Info",
	    Arrays.asList("&eUsage: &6/res subzone <residence name> [subzone name]", "If residence name is left off, will attempt to use residence your standing in."));
	c.get("CommandHelp.SubCommands.res.SubCommands.subzone.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.area.Description", "Manage physical areas for a residence.");
	//res area
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Description", "List physical areas in a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res area list [residence] <page>"));
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Description", "List coordinates and other Info for areas");
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Info", Arrays.asList("&eUsage: &6/res area listall [residence] <page>"));
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Description", "Add physical areas to a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Info",
	    Arrays.asList("&eUsage: &6/res area add [residence] [areaID]", "You must first select two points first."));
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Description", "Remove physical areas from a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Info", Arrays.asList("&eUsage: &6/res area remove [residence] [areaID]"));
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Description", "Replace physical areas in a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Info", Arrays.asList("&eUsage: &6/res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."));
	c.get("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Args", "[residence]");

	// res message
	c.get("CommandHelp.SubCommands.res.SubCommands.message.Description", "Manage residence enter / leave messages");
	c.get("CommandHelp.SubCommands.res.SubCommands.message.Info", Arrays.asList("&eUsage: &6/res message <residence> [enter/leave] [message]",
	    "Set the enter or leave message of a residence.", "&eUsage: &6/res message <residence> remove [enter/leave]", "Removes a enter or leave message."));
	c.get("CommandHelp.SubCommands.res.SubCommands.message.Args", "[residence] [enter/leave]");

	// res lease
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.Description", "Manage residence leases");
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.Info", Arrays.asList("&eUsage: &6/res lease [renew/cost] [residence]",
	    "/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.Args", "[renew/cost] [residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Description", "Set the lease time");
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease set [residence] [#days/infinite]", "Sets the lease time to a specified number of days, or infinite."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Description", "Renew the lease time");
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease renew <residence>", "Renews the lease time for current or specified residence."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Description", "Lease end date");
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease expires <residence>", "Shows when expires residence lease time."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Description", "Shows renew cost");
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Info",
	    Arrays.asList("&eUsage: &6/resadmin lease cost <residence>", "Shows how much money you need to renew residence lease."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Args", "[residence]");

	// res bank
	c.get("CommandHelp.SubCommands.res.SubCommands.bank.Description", "Manage money in a Residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.bank.Info",
	    Arrays.asList("&eUsage: &6/res bank [deposit/withdraw] <residence> [amount]", "You must be standing in a Residence or provide residence name",
		"You must have the +bank flag."));
	c.get("CommandHelp.SubCommands.res.SubCommands.bank.Args", "[deposit/withdraw] [residence]");

	// res confirm
	c.get("CommandHelp.SubCommands.res.SubCommands.confirm.Description", "Confirms removal of a residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.confirm.Info", Arrays.asList("&eUsage: &6/res confirm", "Confirms removal of a residence."));

	c.get("CommandHelp.SubCommands.res.SubCommands.gset.Description", "Set flags on a specific group for a Residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lset.Description", "Change blacklist and ignorelist options");
	c.get("CommandHelp.SubCommands.res.SubCommands.lset.Info",
	    Arrays.asList("&eUsage: &6/res lset <residence> [blacklist/ignorelist] [material]",
		"&eUsage: &6/res lset <residence> Info",
		"Blacklisting a material prevents it from being placed in the residence.",
		"Ignorelist causes a specific material to not be protected by Residence."));
	c.get("CommandHelp.SubCommands.res.SubCommands.lset.Args", "[residence] [blacklist/ignorelist] [material]");

	c.get("CommandHelp.SubCommands.res.SubCommands.removeall.Description", "Remove all residences owned by a player.");
	c.get("CommandHelp.SubCommands.res.SubCommands.removeall.Info", Arrays.asList("&eUsage: &6/res removeall [owner]",
	    "Removes all residences owned by a specific player.'",
	    "Requires /resadmin if you use it on anyone besides yourself."));
	c.get("CommandHelp.SubCommands.res.SubCommands.removeall.Args", "[playername]");

	c.get("CommandHelp.SubCommands.res.SubCommands.list.Description", "List Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res list <player> <page>",
		"Lists all the residences a player owns (except hidden ones).",
		"If listing your own residences, shows hidden ones as well.",
		"To list everyones residences, use /res listall."));
	c.get("CommandHelp.SubCommands.res.SubCommands.list.Args", "[playername]");

	c.get("CommandHelp.SubCommands.res.SubCommands.listhidden.Description", "List Hidden Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.listhidden.Info",
	    Arrays.asList("&eUsage: &6/res listhidden <player> <page>",
		"Lists hidden residences for a player."));
	c.get("CommandHelp.SubCommands.res.SubCommands.listhidden.Args", "[playername]");

	c.get("CommandHelp.SubCommands.res.SubCommands.listall.Description", "List All Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.listall.Info",
	    Arrays.asList("&eUsage: &6/res listall <page>", "Lists hidden residences for a player."));

	c.get("CommandHelp.SubCommands.res.SubCommands.listallhidden.Description", "List All Hidden Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.listallhidden.Info",
	    Arrays.asList("&eUsage: &6/res listhidden <page>",
		"Lists all hidden residences on the server."));

	c.get("CommandHelp.SubCommands.res.SubCommands.sublist.Description", "List Residence Subzones");
	c.get("CommandHelp.SubCommands.res.SubCommands.sublist.Info",
	    Arrays.asList("&eUsage: &6/res sublist <residence> <page>",
		"List subzones within a residence."));
	c.get("CommandHelp.SubCommands.res.SubCommands.sublist.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.reset.Description", "Reset residence to default flags.");
	c.get("CommandHelp.SubCommands.res.SubCommands.reset.Info",
	    Arrays.asList("&eUsage: &6/res reset <residence>",
		"Resets the flags on a residence to their default.  You must be the owner or an admin to do this."));
	c.get("CommandHelp.SubCommands.res.SubCommands.reset.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.rename.Description", "Renames a residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.rename.Info",
	    Arrays.asList("&eUsage: &6/res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
		"The name must not already be taken by another residence."));
	c.get("CommandHelp.SubCommands.res.SubCommands.rename.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.kick.Description", "Kicks player from residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.kick.Info",
	    Arrays.asList("&eUsage: &6/res kick <player>", "You must be the owner or an admin to do this.",
		"Player should be online."));
	c.get("CommandHelp.SubCommands.res.SubCommands.kick.Args", "[playername]");

	c.get("CommandHelp.SubCommands.res.SubCommands.mirror.Description", "Mirrors Flags");
	c.get("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("&eUsage: &6/res mirror [Source Residence] [Target Residence]",
		"Mirrors flags from one residence to another.  You must be owner of both or a admin to do this."));
	c.get("CommandHelp.SubCommands.res.SubCommands.mirror.Args", "[residence] [residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.Description", "Buy, Sell, or Rent Residences");
	c.get("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("&eUsage: &6/res market ? for more Info"));

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Description", "Get economy Info on residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Info",
	    Arrays.asList("&eUsage: &6/res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Description", "Lists rentable and for sale residences.");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Info",
	    Arrays.asList("&eUsage: &6/res market list [rent/sell]"));

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.SubCommands.rent.Description", "Lists rentable residences.");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.SubCommands.rent.Info",
	    Arrays.asList("&eUsage: &6/res market list rent"));

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.SubCommands.sell.Description", "Lists for sale residences.");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.SubCommands.sell.Info",
	    Arrays.asList("&eUsage: &6/res market list sell"));

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Description", "Sell a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Info",
	    Arrays.asList("&eUsage: &6/res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
		"Another player can buy the residence with /res market buy"));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Description", "Set market sign");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Info",
	    Arrays.asList("&eUsage: &6/res market sign [residence]", "Sets market sign you are looking at."));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Description", "Buy a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Info",
	    Arrays.asList("&eUsage: &6/res market buy [residence]", "Buys a Residence if its for sale."));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Description", "Stops selling a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Info",
	    Arrays.asList("&eUsage: &6/res market unsell [residence]"));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Description", "ent a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Info",
	    Arrays.asList("&eUsage: &6/res market rent [residence] <AutoPay>",
		"Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Args", "[cresidence] [true/false]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Description", "Make a residence rentable.");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Info",
	    Arrays.asList("&eUsage: &6/res market rentable [residence] [cost] [days] <AllowRenewing> <StayInMarket> <AllowAutoPay>",
		"Makes a residence rentable for [cost] money for every [days] number of days.",
		"If <AllowRenewing> is true, the residence will be able to be rented again before rent expires.",
		"If <StayInMarket> is true, the residence will stay in market after last renter will be removed.",
		"If <AllowAutoPay> is true, money for rent will be automaticaly taken from players balance if he chosen that option when renting"));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.allowrenewing.Description", "Sets residence AllowRenewing to given value");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.allowrenewing.Info",
	    Arrays.asList("&eUsage: &6/res market allowrenewing <residence> [true/false]"));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.allowrenewing.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.payrent.Description", "Pays rent for defined residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.payrent.Info",
	    Arrays.asList("&eUsage: &6/res market payrent <residence>"));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.payrent.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Description", "Remove a residence from rent or rentable.");
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Info",
	    Arrays.asList("&eUsage: &6/res market release [residence]", "If you are the renter, this command releases the rent on the house for you.",
		"If you are the owner, this command makes the residence not for rent anymore."));
	c.get("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Args", "[residence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.current.Description", "Show residence your currently in.");
	c.get("CommandHelp.SubCommands.res.SubCommands.current.Info",
	    Arrays.asList("&eUsage: &6/res current"));

	c.get("CommandHelp.SubCommands.res.SubCommands.signupdate.Description", "Updated residence signs");
	c.get("CommandHelp.SubCommands.res.SubCommands.signupdate.Info",
	    Arrays.asList("&eUsage: &6/res signupdate"));

	c.get("CommandHelp.SubCommands.res.SubCommands.reload.Description", "reload lanf or config files");
	c.get("CommandHelp.SubCommands.res.SubCommands.reload.Info",
	    Arrays.asList("&eUsage: &6/res reload [config/lang]"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.Description", "Predefined permission lists");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.Info",
	    Arrays.asList("Predefined permissions that can be applied to a residence."));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Description", "Add a list");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Info",
	    Arrays.asList("&eUsage: &6/res lists add <listname>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Description", "Remove a list");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Info",
	    Arrays.asList("&eUsage: &6/res lists remove <listname>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Description", "Apply a list to a residence");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Info",
	    Arrays.asList("&eUsage: &6/res lists apply <listname> <residence>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Description", "Set a flag");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Info",
	    Arrays.asList("&eUsage: &6/res lists set <listname> <flag> <value>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Description", "Set a player flag");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Info",
	    Arrays.asList("&eUsage: &6/res lists pset <listname> <player> <flag> <value>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Description", "Set a group flag");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res lists gset <listname> <group> <flag> <value>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.view.Description", "View a list.");
	c.get("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("&eUsage: &6/res lists view <listname>"));

	c.get("CommandHelp.SubCommands.res.SubCommands.server.Description", "Make land server owned.");
	c.get("CommandHelp.SubCommands.res.SubCommands.server.Info",
	    Arrays.asList("&eUsage: &6/resadmin server [residence]", "Make a residence server owned."));
	c.get("CommandHelp.SubCommands.res.SubCommands.server.Args", "[cresidence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.setowner.Description", "Change owner of a residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.setowner.Info",
	    Arrays.asList("&eUsage: &6/resadmin setowner [residence] [player]"));
	c.get("CommandHelp.SubCommands.res.SubCommands.setowner.Args", "[cresidence]");

	c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence.");
	c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
	    Arrays.asList("&eUsage: &6/resreload"));

	c.get("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file.");
	c.get("CommandHelp.SubCommands.res.SubCommands.resload.Info",
	    Arrays.asList("&eUsage: &6/resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."));

	c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Description", "Remove all residences from world");
	c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Info",
	    Arrays.asList("&eUsage: &6/res removeworld [worldname]", "Can only be used from console"));

	c.get("CommandHelp.SubCommands.res.SubCommands.signconvert.Description", "Converts signs from ResidenceSign plugin");
	c.get("CommandHelp.SubCommands.res.SubCommands.signconvert.Info",
	    Arrays.asList("&eUsage: &6/res signconvert", "Will try to convert saved sign data from 3rd party plugin"));

	c.get("CommandHelp.SubCommands.res.SubCommands.version.Description", "how residence version");
	c.get("CommandHelp.SubCommands.res.SubCommands.version.Info",
	    Arrays.asList("&eUsage: &6/res version"));

	// Write back config
	try {
	    c.getW().save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
