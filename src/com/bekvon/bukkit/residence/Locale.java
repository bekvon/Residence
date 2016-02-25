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

public class Locale {

    public static ArrayList<String> FlagList = new ArrayList<String>();
    private Residence plugin;

    public Locale(Residence plugin) {
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

	writer.addComment("Language.NewPlayerInfo", "The below lines represent various messages residence sends to the players.",
	    "Note that some messages have variables such as %1 that are inserted at runtime.");
	GetConfig("Language.NewPlayerInfo",
	    "&eIf you want to create protected area for your house, please use wooden axe to select opposite sides of your home and execute command &2/res create YourResidenceName",
	    writer, conf, true);
	GetConfig("Language.InvalidPlayer", "&cInvalid player name...", writer, conf, true);
	GetConfig("Language.InvalidResidence", "&cInvalid Residence...", writer, conf, true);
	GetConfig("Language.InvalidSubzone", "&cInvalid Subzone...", writer, conf, true);
	GetConfig("Language.InvalidDirection", "&cInvalid Direction...", writer, conf, true);
	GetConfig("Language.InvalidAmount", "&cInvalid Amount...", writer, conf, true);
	GetConfig("Language.InvalidCost", "&cInvalid Cost...", writer, conf, true);
	GetConfig("Language.InvalidDays", "&cInvalid number of days...", writer, conf, true);
	GetConfig("Language.InvalidMaterial", "&cInvalid Material...", writer, conf, true);
	GetConfig("Language.InvalidBoolean", "&cInvalid value, must be true(t) or false(f)", writer, conf, true);
	GetConfig("Language.InvalidArea", "&cInvalid Area...", writer, conf, true);
	GetConfig("Language.InvalidGroup", "&cInvalid Group...", writer, conf, true);
	GetConfig("Language.UseNumbers", "&cPlease use numbers...", writer, conf, true);
	GetConfig("Language.NotOwner", "&cYou are not owner of this residence", writer, conf, true);
	GetConfig("Language.LookAtSign", "&cYou are not looking at sign", writer, conf, true);
	writer.addComment("Language.CantPlaceLava", "Replace all text with '' to disable this message");
	GetConfig("Language.CantPlaceLava", "&cYou can't place lava outside residence and higher than %1 block level", writer, conf, true);
	writer.addComment("Language.CantPlaceWater", "Replace all text with '' to disable this message");
	GetConfig("Language.CantPlaceWater", "&cYou can't place Water outside residence and higher than %1 block level", writer, conf, true);
	GetConfig("Language.InvalidMessageType", "&cMessage type must be enter or remove.", writer, conf, true);
	// GetConfig("Language.InvalidList", "Invalid List...", writer,
	// conf, true);
	GetConfig("Language.InvalidFlag", "Invalid Flag...", writer, conf, true);
	GetConfig("Language.InvalidFlagState", "Invalid flag state, must be true(t), false(f), or remove(r)", writer, conf, true);
	GetConfig("Language.AreaExists", "Area name already exists.", writer, conf, true);
	GetConfig("Language.AreaCreate", "Residence Area created, ID %1", writer, conf, true);
	GetConfig("Language.AreaDiffWorld", "Area is in a different world from residence.", writer, conf, true);
	GetConfig("Language.AreaCollision", "Area collides with residence %1", writer, conf, true);
	GetConfig("Language.AreaSubzoneCollision", "Area collides with subzone %1", writer, conf, true);
	GetConfig("Language.AreaNonExist", "No such area exists.", writer, conf, true);
	GetConfig("Language.AreaInvalidName", "Invalid Area Name...", writer, conf, true);
	GetConfig("Language.AreaToSmallTotal", "Selected area smaller than allowed minimal (%1)", writer, conf, true);
	GetConfig("Language.AreaToSmallX", "Your x selection length is too small. %1 allowed %2", writer, conf, true);
	GetConfig("Language.AreaToSmallY", "Your selection height is too small. %1 allowed %2", writer, conf, true);
	GetConfig("Language.AreaToSmallZ", "Your z selection length is too small. %1 allowed %2", writer, conf, true);
	GetConfig("Language.AreaName", "Name", writer, conf, true);
	GetConfig("Language.AreaRename", "Renamed area %1 to %2", writer, conf, true);
	GetConfig("Language.AreaRemove", "Removed area %1...", writer, conf, true);
	GetConfig("Language.AreaRemoveLast", "Cannot remove the last area in a residence.", writer, conf, true);
	GetConfig("Language.AreaNotWithinParent", "Area is not within parent area.", writer, conf, true);
	GetConfig("Language.AreaUpdate", "Area Updated...", writer, conf, true);
	GetConfig("Language.AreaMaxPhysical", "You've reached the max physical areas allowed for your residence.", writer, conf, true);
	GetConfig("Language.AreaSizeLimit", "Area size is not within your allowed limits.", writer, conf, true);
	GetConfig("Language.AreaHighLimit", "You cannot protect this high up, your limit is %1", writer, conf, true);
	GetConfig("Language.AreaLowLimit", "You cannot protect this deep, your limit is %1", writer, conf, true);
	GetConfig("Language.NotInResidence", "You are not in a Residence.", writer, conf, true);
	GetConfig("Language.PlayerNotInResidence", "Player standing not in your Residence area.", writer, conf, true);
	GetConfig("Language.Kicked", "You were kicked from residence", writer, conf, true);
	GetConfig("Language.InResidence", "You are standing in Residence %1", writer, conf, true);
	GetConfig("Language.ResidenceOwnerChange", "Residence %1 owner changed to %2", writer, conf, true);
	GetConfig("Language.NonAdmin", "You are not a Residence admin.", writer, conf, true);
	GetConfig("Language.AdminOnly", "Only admins have access to this command.", writer, conf, true);
	GetConfig("Language.ChatDisabled", "Residence Chat Disabled...", writer, conf, true);
	GetConfig("Language.SubzoneRename", "Renamed subzone %1 to %2", writer, conf, true);
	GetConfig("Language.SubzoneRemove", "Subzone %1 removed.", writer, conf, true);
	GetConfig("Language.SubzoneCreate", "Created Subzone %1", writer, conf, true);
	GetConfig("Language.SubzoneCreateFail", "Unable to create subzone %1", writer, conf, true);
	GetConfig("Language.SubzoneExists", "Subzone %1 already exists.", writer, conf, true);
	GetConfig("Language.SubzoneCollide", "Subzone collides with subzone %1", writer, conf, true);
	GetConfig("Language.SubzoneMaxDepth", "You have reached the maximum allowed subzone depth.", writer, conf, true);
	GetConfig("Language.SubzoneSelectInside", "Both selection points must be inside the residence.", writer, conf, true);
	GetConfig("Language.SelectPoints", "Select two points first before using this command!", writer, conf, true);
	GetConfig("Language.SelectOverlap", "&cSelected points overlap with &e%1 &cregion!", writer, conf, true);
	GetConfig("Language.SelectionSuccess", "Selection Successful!", writer, conf, true);
	GetConfig("Language.SelectionFail", "Invalid select command...", writer, conf, true);
	GetConfig("Language.SelectionBedrock", "Selection expanded to your lowest allowed limit.", writer, conf, true);
	GetConfig("Language.SelectionSky", "Selection expanded to your highest allowed limit.", writer, conf, true);
	GetConfig("Language.SelectionArea", "Selected area %1 of residence %2", writer, conf, true);
	GetConfig("Language.SelectDiabled", "You don't have access to selections commands.", writer, conf, true);
	GetConfig("Language.NoPermission", "You dont have permission for this.", writer, conf, true);
	GetConfig("Language.OwnerNoPermission", "The owner does not have permission for this.", writer, conf, true);
	GetConfig("Language.ParentNoPermission", "You don't have permission to make changes to the parent zone.", writer, conf, true);
	GetConfig("Language.MessageChange", "Message Set...", writer, conf, true);
	GetConfig("Language.FlagSet", "&e%1 &2flag set for &e%2 &2to &e%3 &2state", writer, conf, true);
	GetConfig("Language.FlagSetFailed", "&cYou dont have access to &6%1 &cflag", writer, conf, true);
	GetConfig("Language.FlagCheckTrue", "Flag %1 applys to player %2 for residence %3, value = %4", writer, conf, true);
	GetConfig("Language.FlagCheckFalse", "Flag %1 does not apply to player %2 for residence.", writer, conf, true);
	GetConfig("Language.FlagsCleared", "Flags Cleared.", writer, conf, true);
	GetConfig("Language.FlagsDefault", "Flags set to default.", writer, conf, true);
	GetConfig("Language.DefaultUsage", "&eType &6/%1 ? &efor more info", writer, conf, true);
	GetConfig("Language.Usage", "Command Usage", writer, conf, true);
	GetConfig("Language.InvalidHelp", "Invalid Help Page...", writer, conf, true);
	GetConfig("Language.SubCommands", "Sub Commands", writer, conf, true);
	GetConfig("Language.InvalidList", "Unknown list type, must be blacklist or ignorelist.", writer, conf, true);
	GetConfig("Language.MaterialGet", "The material name for ID %1 is %2", writer, conf, true);
	GetConfig("Language.MarketDisabled", "Economy Disabled!", writer, conf, true);
	GetConfig("Language.MarketList", "Market List", writer, conf, true);
	GetConfig("Language.ResidenceList", "&a%1%2 &e- %3: %4", writer, conf, true);
	GetConfig("Language.Rented", " &a(Rented)", writer, conf, true);

	GetConfig("Language.SignsUpdated", "&e%1 signs updated!", writer, conf, true);

	GetConfig("Language.SignTopLine", "[market]", writer, conf, true);
	GetConfig("Language.SignDateFormat", "YY/MM/dd HH:mm", writer, conf, true);

	GetConfig("Language.SignForRentTopLine", "&8For Rent", writer, conf, true);
	GetConfig("Language.SignForRentPriceLine", "&8%1&f/&8%2&f/&8%3", writer, conf, true);
	GetConfig("Language.SignForRentResName", "&8%1", writer, conf, true);
	GetConfig("Language.SignForRentBottomLine", "&9Available", writer, conf, true);

	GetConfig("Language.SignRentedAutorenewTrue", "&2", writer, conf, true);
	GetConfig("Language.SignRentedAutorenewFalse", "&c", writer, conf, true);

	GetConfig("Language.SignRentedTopLine", "%1", writer, conf, true);
	GetConfig("Language.SignRentedPriceLine", "&8%1&f/&8%2&f/&8%3", writer, conf, true);
	GetConfig("Language.SignRentedResName", "&8%1", writer, conf, true);
	GetConfig("Language.SignRentedBottomLine", "&1%1", writer, conf, true);

	GetConfig("Language.SignForSaleTopLine", "&8For Sale", writer, conf, true);
	GetConfig("Language.SignForSalePriceLine", "&8%1", writer, conf, true);
	GetConfig("Language.SignForSaleResName", "&8%1", writer, conf, true);
	GetConfig("Language.SignForSaleBottomLine", "&5Available", writer, conf, true);

	GetConfig("Language.SelectionTool", "Selection Tool", writer, conf, true);
	GetConfig("Language.InfoTool", "Info Tool", writer, conf, true);
	GetConfig("Language.NoBankAccess", "You dont have bank access.", writer, conf, true);
	GetConfig("Language.NotEnoughMoney", "You dont have enough money.", writer, conf, true);
	GetConfig("Language.Bank", "Bank", writer, conf, true);
	GetConfig("Language.BankNoMoney", "Not enough money in the bank.", writer, conf, true);
	GetConfig("Language.BankDeposit", "You deposit %1 into the residence bank.", writer, conf, true);
	GetConfig("Language.BankWithdraw", "You withdraw %1 from the residence bank.", writer, conf, true);
	GetConfig("Language.MoneyCharged", "Charged %1 to your %2 account.", writer, conf, true);
	GetConfig("Language.MoneyAdded", "Got %1 to your %2 account.", writer, conf, true);
	GetConfig("Language.MoneyCredit", "Credited %1 to your %2 account.", writer, conf, true);
	// GetConfig("Language.RentDisabled", "Rent system is disabled.",
	// writer, conf, true);
	GetConfig("Language.RentReleaseInvalid", "Residence %1 is not rented or for rent.", writer, conf, true);
	GetConfig("Language.RentSellFail", "Cannot sell a Residence if it is for rent.", writer, conf, true);
	GetConfig("Language.RentedBy", "Rented by", writer, conf, true);
	GetConfig("Language.SellRentFail", "Cannot rent a Residence if it is for sale.", writer, conf, true);
	GetConfig("Language.OwnerBuyFail", "Cannot buy your own land!", writer, conf, true);
	GetConfig("Language.OwnerRentFail", "Cannot rent your own land!", writer, conf, true);
	GetConfig("Language.AlreadySellFail", "Residence already for sale!", writer, conf, true);
	GetConfig("Language.ResidenceBought", "You bought residence %1", writer, conf, true);
	GetConfig("Language.ResidenceBuy", "Residence %1 has bought residence %2 from you.", writer, conf, true);
	GetConfig("Language.ResidenceBuyTooBig", "This residence has areas bigger then your allowed max.", writer, conf, true);
	GetConfig("Language.ResidenceNotForSale", "Residence is not for sale.", writer, conf, true);
	GetConfig("Language.ResidenceForSale", "Residence %1 is now for sale for %2", writer, conf, true);
	GetConfig("Language.ResidenceStopSelling", "Residence is no longer for sale.", writer, conf, true);
	GetConfig("Language.ResidenceTooMany", "You already own the max number of residences your allowed to.", writer, conf, true);
	GetConfig("Language.ResidenceMaxRent", "You already are renting the maximum number of residences your allowed to.", writer, conf, true);
	GetConfig("Language.ResidenceAlreadyRent", "Residence is already for rent...", writer, conf, true);
	GetConfig("Language.ResidenceNotForRent", "Residence not for rent...", writer, conf, true);
	GetConfig("Language.ResidenceNotForRentOrSell", "&cResidence not for rent or sell...", writer, conf, true);
	GetConfig("Language.ResidenceNotRented", "Residence not rented.", writer, conf, true);
	GetConfig("Language.ResidenceUnrent", "Residence %1 has been unrented.", writer, conf, true);
	GetConfig("Language.ResidenceRemoveRentable", "Residence %1 is no longer rentable.", writer, conf, true);
	GetConfig("Language.ResidenceForRentSuccess", "Residence %1 is now for rent for %2 every %3 days.", writer, conf, true);
	GetConfig("Language.ResidenceRentSuccess", "You have rented Residence %1 for %2 days.", writer, conf, true);
	GetConfig("Language.ResidenceAlreadyRented", "Residence %1 has currently been rented to %2", writer, conf, true);
	GetConfig("Language.ResidenceAlreadyExists", "A residence named %1 already exists.", writer, conf, true);
	GetConfig("Language.ResidenceCreate", "You have created residence %1!", writer, conf, true);
	GetConfig("Language.ResidenceRename", "Renamed Residence %1 to %2", writer, conf, true);
	GetConfig("Language.ResidenceRemove", "Residence %1 has been removed...", writer, conf, true);
	GetConfig("Language.RentDisabled", "Rent is disabled...", writer, conf, true);
	GetConfig("Language.RentDisableRenew", "Residence %1 will now no longer re-rent upon expire.", writer, conf, true);
	GetConfig("Language.RentEnableRenew", "Residence %1 will now automatically re-rent upon expire.", writer, conf, true);
	GetConfig("Language.RentableDisableRenew", "%1 will no longer renew rentable status upon expire.", writer, conf, true);
	GetConfig("Language.RentableEnableRenew", "%1 will now automatically renew rentable status upon expire.", writer, conf, true);
	GetConfig("Language.LandForSale", "Land For Sale", writer, conf, true);
	GetConfig("Language.SellAmount", "Sell Amount", writer, conf, true);
	GetConfig("Language.LeaseExpire", "Lease Expire Time", writer, conf, true);
	GetConfig("Language.RentExpire", "Rent Expire Time", writer, conf, true);
	GetConfig("Language.RentableAutoRenew", "Rentable Auto Renew", writer, conf, true);
	GetConfig("Language.RentAutoRenew", "Rent Auto Renew", writer, conf, true);
	GetConfig("Language.RentableLand", "Rentable Land", writer, conf, true);
	GetConfig("Language.ListMaterialAdd", "%1 added to the residence %2", writer, conf, true);
	GetConfig("Language.ListMaterialRemove", "%1 removed from the residence %2", writer, conf, true);
	GetConfig("Language.ItemBlacklisted", "You are blacklisted from using this item here.", writer, conf, true);
	GetConfig("Language.RentedModifyDeny", "Cannot modify a rented residence.", writer, conf, true);
	GetConfig("Language.WorldPVPDisabled", "World PVP is disabled.", writer, conf, true);
	GetConfig("Language.NoPVPZone", "No PVP zone.", writer, conf, true);
	GetConfig("Language.FlagDeny", "You dont have %1 permission<s> here.", writer, conf, true);
	GetConfig("Language.FlagSetDeny", "Owner does not have access to flag %1", writer, conf, true);

	GetConfig("Language.FlagChangeDeny", "&cYou cant change &e%1 &cflag state while there is &e%2 &cplayer(s) inside.", writer, conf, true);

	GetConfig("Language.SelectPoint", "Placed %1 Selection Point", writer, conf, true);
	GetConfig("Language.ResidenceMoveDeny", "You dont have movement permission for Residence %1", writer, conf, true);
	GetConfig("Language.ResidenceFlagDeny", "You dont have %1 permission for Residence %2", writer, conf, true);
	GetConfig("Language.TeleportDeny", "You dont have teleport access.", writer, conf, true);
	GetConfig("Language.TeleportSuccess", "Teleported!", writer, conf, true);
	GetConfig("Language.TeleportConfirm",
	    "&eThis teleport is not safe, you will fall for &c%1 &eblocks. Use &2/res tpconfirm &eto perform teleportation anyways.", writer, conf, true);
	GetConfig("Language.TeleportStarted",
	    "&eTeleportation to %1 started, don't move for next %2 sec.", writer, conf, true);
	GetConfig("Language.TeleportCanceled",
	    "&eTeleportation canceled!", writer, conf, true);
	GetConfig("Language.NoTeleportConfirm", "There is no teleports waiting for confirmation!", writer, conf, true);
	GetConfig("Language.TeleportNear", "Teleported to near residence.", writer, conf, true);
	GetConfig("Language.TeleportNoFlag", "You dont have teleport access for that residence.", writer, conf, true);
	GetConfig("Language.SetTeleportLocation", "Teleport Location Set...", writer, conf, true);
	GetConfig("Language.HelpPageHeader", "Help Pages - %1 - Page <%2 of %3>", writer, conf, true);
	GetConfig("Language.ListExists", "List already exists...", writer, conf, true);
	GetConfig("Language.ListRemoved", "List removed...", writer, conf, true);
	GetConfig("Language.ListCreate", "Created list %1", writer, conf, true);
	GetConfig("Language.LeaseRenew", "Lease valid until %1", writer, conf, true);
	GetConfig("Language.LeaseRenewMax", "Lease renewed to maximum allowed", writer, conf, true);
	GetConfig("Language.LeaseNotExpire", "No such lease, or lease does not expire.", writer, conf, true);
	GetConfig("Language.LeaseRenewalCost", "Renewal cost for area %1 is %2", writer, conf, true);
	GetConfig("Language.LeaseInfinite", "Lease time set to infinite...", writer, conf, true);
	GetConfig("Language.PermissionsApply", "Permissions applied to residence.", writer, conf, true);
	GetConfig("Language.PhysicalAreas", "Physical Areas", writer, conf, true);
	GetConfig("Language.CurrentArea", "Current Area", writer, conf, true);
	GetConfig("Language.TotalWorth", "Total worth of residence:&3 %1 (%2)", writer, conf, true);
	GetConfig("Language.LeaseExpire", "Lease Expiration", writer, conf, true);
	GetConfig("Language.NotOnline", "Target player must be online.", writer, conf, true);
	GetConfig("Language.ResidenceGiveLimits", "Cannot give residence to target player, because it is outside the target players limits.", writer, conf, true);
	GetConfig("Language.ResidenceGive", "You give residence %1 to player %2", writer, conf, true);
	GetConfig("Language.ResidenceRecieve", "You have recieved residence %1 from player %2", writer, conf, true);
	// GetConfig("Language.#ResidenceListAll", "Residences - <Page %1
	// of %2> - removed, use GenericPage now", writer, conf, true);
	GetConfig("Language.ResidenceListAllEmpty", "No Residences exists on the server...", writer, conf, true);
	GetConfig("Language.InvalidPage", "Invalid Page...", writer, conf, true);
	GetConfig("Language.NextPage", "Next Page", writer, conf, true);
	GetConfig("Language.NextInfoPage", "&2| &eNext Page &2>>>", writer, conf, true);
	GetConfig("Language.PrevInfoPage", "&2<<< &ePrev Page &2|", writer, conf, true);
	GetConfig("Language.RemovePlayersResidences", "Removed all residences belonging to player %1", writer, conf, true);
	GetConfig("Language.GenericPage", "Page %1 of %2", writer, conf, true);
	GetConfig("Language.ResidenceRentedBy", "Rented by %1", writer, conf, true);
	GetConfig("Language.InvalidCharacters", "Invalid characters detected...", writer, conf, true);
	GetConfig("Language.InvalidNameCharacters", "Name contained unallowed characters...", writer, conf, true);
	GetConfig("Language.DeleteConfirm", "Are you sure you want to delete residence %1, use /res confirm to confirm.", writer, conf, true);
	GetConfig("Language.CantCreateSubzone", "&cYou dont have permission to create residence subzone.", writer, conf, true);
	GetConfig("Language.CantDeleteResidence", "&cYou dont have permission to delete residence.", writer, conf, true);
	GetConfig("Language.CantDeleteSubzone", "&cYou dont have permission to delete residence subzone.", writer, conf, true);
	GetConfig("Language.CantDeleteSubzoneNotOwnerOfParent", "&cYou are not owner of parent residence to delete this subzone.", writer, conf, true);
	GetConfig("Language.CantExpandResidence", "&cYou dont have permission to expand residence.", writer, conf, true);
	GetConfig("Language.CantExpandSubzone", "&cYou dont have permission to expand residence subzone.", writer, conf, true);
	GetConfig("Language.CantContractResidence", "&cYou dont have permission to contract residence.", writer, conf, true);
	GetConfig("Language.CantContractSubzone", "&cYou dont have permission to contract residence subzone.", writer, conf, true);
	GetConfig("Language.SelectTooHigh", "Warning, selection went above top of map, limiting.", writer, conf, true);
	GetConfig("Language.SelectTooLow", "Warning, selection went below bottom of map, limiting.", writer, conf, true);
	GetConfig("Language.WorldEditNotFound", "WorldEdit was not detected.", writer, conf, true);
	GetConfig("Language.NoResHere", "There is no residence in there.", writer, conf, true);
	GetConfig("Language.DeleteSubzoneConfirm", "Are you sure you want to delete subzone %1, use /res confirm to confirm.", writer, conf, true);
	GetConfig("Language.SubzoneOwnerChange", "Subzone %1 owner changed to %2", writer, conf, true);
	GetConfig("Language.CoordsTop", "X:%1 Y:%2 Z:%3", writer, conf, true);
	GetConfig("Language.CoordsBottom", "X:%1 Y:%2 Z:%3", writer, conf, true);
	GetConfig("Language.AdminToggle", "Automatic resadmin toggle turned %1", writer, conf, true);
	GetConfig("Language.NoSpawn", "You do not have move permissions at your spawn point.  Relocating", writer, conf, true);
	GetConfig("Language.CompassTargetReset", "Your compass has been reset", writer, conf, true);
	GetConfig("Language.CompassTargetSet", "Your compass now points to %1", writer, conf, true);
	writer.addComment("Language.Description", "The below lines are mostly a word bank for various uses.");
	GetConfig("Language.Description", "Description", writer, conf, true);
	GetConfig("Language.Land", "Land", writer, conf, true);
	GetConfig("Language.Cost", "Cost", writer, conf, true);
	GetConfig("Language.Selection", "Selection", writer, conf, true);
	GetConfig("Language.Total", "Total", writer, conf, true);
	GetConfig("Language.Size", "Size", writer, conf, true);
	GetConfig("Language.Expanding", "Expanding", writer, conf, true);
	GetConfig("Language.Contracting", "Contracting", writer, conf, true);
	GetConfig("Language.North", "North", writer, conf, true);
	GetConfig("Language.West", "West", writer, conf, true);
	GetConfig("Language.South", "South", writer, conf, true);
	GetConfig("Language.East", "East", writer, conf, true);
	GetConfig("Language.Shifting", "Shifting", writer, conf, true);
	GetConfig("Language.Up", "Up", writer, conf, true);
	GetConfig("Language.Down", "Down", writer, conf, true);
	GetConfig("Language.Error", "Error", writer, conf, true);
	GetConfig("Language.Flags", "Flags", writer, conf, true);
	GetConfig("Language.Your", "Your", writer, conf, true);
	GetConfig("Language.Group", "Group", writer, conf, true);
	GetConfig("Language.Others", "Others", writer, conf, true);
	GetConfig("Language.Primary", "Primary", writer, conf, true);
	GetConfig("Language.Secondary", "Secondary", writer, conf, true);
	GetConfig("Language.Selection", "Selection", writer, conf, true);
	GetConfig("Language.Moved", "Moved", writer, conf, true);
	GetConfig("Language.Status", "Status", writer, conf, true);
	GetConfig("Language.Available", "Available", writer, conf, true);
	GetConfig("Language.On", "On", writer, conf, true);
	GetConfig("Language.Off", "Off", writer, conf, true);
	GetConfig("Language.Name", "Name", writer, conf, true);
	GetConfig("Language.Lists", "Lists", writer, conf, true);
	GetConfig("Language.Residences", "Residences", writer, conf, true);
	GetConfig("Language.Residence", "Residence", writer, conf, true);
	GetConfig("Language.Count", "Count", writer, conf, true);
	GetConfig("Language.Owner", "Owner", writer, conf, true);
	GetConfig("Language.World", "World", writer, conf, true);
	GetConfig("Language.Subzones", "Subzones", writer, conf, true);
	GetConfig("Language.CoordsT", "Top Coords", writer, conf, true);
	GetConfig("Language.CoordsB", "Bottom Coords", writer, conf, true);
	GetConfig("Language.TurnOn", "on", writer, conf, true);
	GetConfig("Language.TurnOff", "off", writer, conf, true);
	GetConfig("Language.LimitsTop", "----------------------------------------", writer, conf, true);
	GetConfig("Language.LimitsPGroup", "&7- &ePermissions Group:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsRGroup", "&7- &eResidence Group:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsAdmin", "&7- &eResidence Admin:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsCanCreate", "&7- &eCan Create Residences:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxRes", "&7- &eMax Residences:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxEW", "&7- &eMax East/West Size:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxNS", "&7- &eMax North/South Size:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxUD", "&7- &eMax Up/Down Size:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMinMax", "&7- &eMin/Max Protection Height:&3 %1 to %2", writer, conf, true);
	GetConfig("Language.LimitsMaxSub", "&7- &eMax Subzone Depth:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxRents", "&7- &eMax Rents:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsEnterLeave", "&7- &eCan Set Enter/Leave Messages:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsNumberOwn", "&7- &eNumber of Residences you own:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsCost", "&7- &eResidence Cost Per Block:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsSell", "&7- &eResidence Sell Cost Per Block:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsFlag", "&7- &eFlag Permissions:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsMaxDays", "&7- &eMax Lease Days:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsLeaseTime", "&7- &eLease Time Given on Renew:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsRenewCost", "&7- &eRenew Cost Per Block:&3 %1", writer, conf, true);
	GetConfig("Language.LimitsBottom", "----------------------------------------", writer, conf, true);
	GetConfig("Language.Gui.Set.Title", "&6%1 flags", writer, conf, true);
	GetConfig("Language.Gui.Pset.Title", "&6%1 %2 flags", writer, conf, true);
	GetConfig("Language.Gui.Actions", Arrays.asList("&2Left click to enable", "&cRight click to disable", "&eShift + left click to remove"), writer, conf, true);

	GetConfig("Language.InformationPage.TopLine", "&e---< &a %1 &e >---", writer, conf, true);
	GetConfig("Language.InformationPage.Page", "&e-----< %1 >-----", writer, conf, true);
	GetConfig("Language.InformationPage.NextPage", "&e-----< %1 >-----", writer, conf, true);
	GetConfig("Language.InformationPage.NoNextPage", "&e-----------------------", writer, conf, true);
	GetConfig("Language.AutoSelection.Enabled", "&eAuto selection mode turned ON. To disable it write /res select auto", writer, conf, true);
	GetConfig("Language.AutoSelection.Disabled", "&eAuto selection mode turned OFF. To enable it again write /res select auto", writer, conf, true);

	GetConfig("Language.Chat.ChatChannelChange", "Changed residence chat channel to %1", writer, conf, true);
	GetConfig("Language.Chat.ChatChannelLeave", "Left residence chat", writer, conf, true);

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

	writer.addComment("CommandHelp", "");

	GetConfig("CommandHelp.Description", "Contains Help for Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.Description", "Main Residence Command", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.Info", Arrays.asList("Use /res [command] ? <page> to view more help Information."), writer, conf, true);

	// res select
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.Description", "Selection Commands", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.Info",
	    Arrays.asList("This command selects areas for usage with residence.", "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Description", "Display selected coordinates", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Info", Arrays.asList("Usage: /res select coords"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Description", "Display selected size", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Info", Arrays.asList("Usage: /res select size"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Description", "Turns on auto selection tool", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Info", Arrays.asList("Usage: /res select auto [playername]"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.auto.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Description", "Display selection cost", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Info", Arrays.asList("Usage: /res select cost"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Description", "Expand Selection Vertically", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Info",
	    Arrays.asList("Usage: /res select vert", "Will expand selection as high and as low as allowed."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Description", "Expand Selection to Sky", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Info", Arrays.asList("Usage: /res select sky",
	    "Expands as high as your allowed to go."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Description", "Expand Selection to Bedrock", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Info",
	    Arrays.asList("Usage: /res select bedrock", "Expands as low as your allowed to go."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Description", "Expand selection in a direction.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Info",
	    Arrays.asList("Usage: /res select expand <amount>", "Expands <amount> in the direction your looking."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Description", "Shift selection in a direction", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Info",
	    Arrays.asList("Usage: /res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Description", "Select the chunk your currently in.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Info",
	    Arrays.asList("Usage: /res select chunk", "Selects the chunk your currently standing in."), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Description", "Select a existing area in a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Info",
	    Arrays.asList("Usage /res select residence <residence>", "Selects a existing area in a residence."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Description", "Set selection using the current WorldEdit selection.",
	    writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Info",
	    Arrays.asList("Usage /res select worldedit", "Sets selection area using the current WorldEdit selection."), writer, conf, true);

	// res create
	GetConfig("CommandHelp.SubCommands.res.SubCommands.create.Description", "Create Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.create.Info", Arrays.asList("Usage: /res create <residence name>"), writer, conf, true);

	// res remove
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Description", "Remove residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Info", Arrays.asList("Usage: /res remove <residence name>"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.remove.Args", "[residence]", writer, conf, true);

	// res padd
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Description", "Add player to residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Info", Arrays.asList("Usage: /res padd <residence name> [player]",
	    "Adds essential flags for player"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.padd.Args", "[residence] [playername]", writer, conf, true);

	// res pdel
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Description", "Remove player from residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Info", Arrays.asList("Usage: /res pdel <residence name> [player]",
	    "Removes essential flags from player"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pdel.Args", "[residence] [playername]", writer, conf, true);

	// res info
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Description", "Show info on a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Info",
	    Arrays.asList("Usage: /res info <residence>", "Leave off <residence> to display info for the residence your currently in."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.info.Args", "[residence]", writer, conf, true);

	// res set
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Description", "Set general flags on a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Info", Arrays.asList("Usage: /res set <residence> [flag] [true/false/remove]",
	    "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.set.Args", "[residence] [flag] [true/false/remove]", writer, conf, true);

	// res pset
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pset.Description", "Set flags on a specific player for a Residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.pset.Info", Arrays.asList("Usage: /res pset <residence> [player] [flag] [true/false/remove]",
	    "Usage: /res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"), writer, conf, true);
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
	    Arrays.asList("Usage: /res limits", "Shows the limitations you have on creating and managing residences."), writer, conf, true);

	// res tpset
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpset.Description", "Set the teleport location of a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpset.Info",
	    Arrays.asList("Usage: /res tpset", "This will set the teleport location for a residence to where your standing.",
		"You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."),
	    writer, conf, true);

	// res tp
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Description", "Teleport to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Info",
	    Arrays.asList("Usage: /res tp [residence]", "Teleports you to a residence, you must have +tp flag access or be the owner.",
		"Your permission group must also be allowed to teleport by the server admin."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tp.Args", "[residence]", writer, conf, true);

	// res rt
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rt.Description", "Teleports to random location in world", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rt.Info",
	    Arrays.asList("Usage: /res rt", "Teleports you to random location in defined world."), writer, conf, true);

	// res expand
	GetConfig("CommandHelp.SubCommands.res.SubCommands.expand.Description", "Expands residence in direction you looking", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.expand.Info",
	    Arrays.asList("Usage: /res expand (residence) [amount]", "Expands residence in direction you looking.", "Residence name is optional"), writer, conf, true);

	// res contract
	GetConfig("CommandHelp.SubCommands.res.SubCommands.contract.Description", "Contracts residence in direction you looking", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.contract.Info",
	    Arrays.asList("Usage: /res contract (residence [amount])", "Contracts residence in direction you looking.", "Residence name is optional"), writer, conf,
	    true);

	// res shop
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.Description", "Manage residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.Info", Arrays.asList("Manages residence shop feature"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Description", "Shows list of res shops", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.list.Info", Arrays.asList("Usage: /res shop list",
	    "Shows full list of all residences with shop flag"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Description", "Vote for residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Info", Arrays.asList("Usage: /res shop vote <residence> [amount]",
	    "Votes for current or defined residence"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.vote.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Description", "Give like for residence shop", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Info", Arrays.asList("Usage: /res shop like <residence>",
	    "Gives like for residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.like.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Description", "Shows res shop votes", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Info", Arrays.asList("Usage: /res shop votes <residence> <page>",
	    "Shows full vote list of current or defined residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.votes.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Description", "Shows res shop likes", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Info", Arrays.asList("Usage: /res shop likes <residence> <page>",
	    "Shows full like list of current or defined residence shop"), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.likes.Args", "[residenceshop]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Description", "Sets residence shop description", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.setdesc.Info", Arrays.asList("Usage: /res shop setdesc [text]",
	    "Sets residence shop description. Color code supported. For new line use /n"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Description", "Create res shop board", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.createboard.Info", Arrays.asList("Usage: /res shop createboard [place]",
	    "Creates res shop board from selected area. Place - position from which to start filling board"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Description", "Deletes res shop board", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.shop.SubCommands.deleteboard.Info", Arrays.asList("Usage: /res shop deleteboard",
	    "Deletes res shop board bi right clicking on one of signs"), writer, conf, true);

	// res tpconfirm
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Description", "Ignore unsafe teleportation warning", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Info",
	    Arrays.asList("Usage: /res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."),
	    writer, conf, true);

	// res subzone
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Description", "Create subzones in residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Info",
	    Arrays.asList("Usage: /res subzone <residence name> [subzone name]", "If residence name is left off, will attempt to use residence your standing in."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.subzone.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.Description", "Manage physical areas for a residence.", writer, conf, true);
	//res area
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Description", "List physical areas in a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Info",
	    Arrays.asList("Usage: /res area list [residence] <page>"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Description", "List coordinates and other Info for areas", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Info", Arrays.asList("Usage: /res area listall [residence] <page>"), writer,
	    conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Description", "Add physical areas to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Info",
	    Arrays.asList("Usage: /res area add [residence] [areaID]", "You must first select two points first."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Description", "Remove physical areas from a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Info", Arrays.asList("Usage: /res area remove [residence] [areaID]"), writer,
	    conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Description", "Replace physical areas in a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Info", Arrays.asList("Usage: /res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Args", "[residence]", writer, conf, true);

	// res message
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Description", "Manage residence enter / leave messages", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Info", Arrays.asList("Usage: /res message <residence> [enter/leave] [message]",
	    "Set the enter or leave message of a residence.", "Usage: /res message <residence> remove [enter/leave]", "Removes a enter or leave message."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.message.Args", "[residence] [enter/leave]", writer, conf, true);

	// res lease
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.Description", "Manage residence leases", writer, conf, true);
	GetConfig(
	    "CommandHelp.SubCommands.res.SubCommands.lease.Info", Arrays.asList("Usage: /res lease [renew/cost] [residence]",
		"/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.Args", "[renew/cost] [residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Description", "Set the lease time", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Info",
	    Arrays.asList("Usage: /resadmin lease set [residence] [#days/infinite]", "Sets the lease time to a specified number of days, or infinite."), writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Description", "Renew the lease time", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Info",
	    Arrays.asList("Usage: /resadmin lease renew <residence>", "Renews the lease time for current or specified residence."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.renew.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Description", "Lease end date", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Info",
	    Arrays.asList("Usage: /resadmin lease expires <residence>", "Shows when expires residence lease time."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.expires.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Description", "Shows renew cost", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Info",
	    Arrays.asList("Usage: /resadmin lease cost <residence>", "Shows how much money you need to renew residence lease."), writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.cost.Args", "[residence]", writer, conf, true);

	// res bank
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Description", "Manage money in a Residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Info",
	    Arrays.asList("Usage: /res bank [deposit/withdraw] <residence> [amount]", "You must be standing in a Residence or provide residence name",
		"You must have the +bank flag."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.bank.Args", "[deposit/withdraw] [residence]", writer, conf, true);

	// res confirm
	GetConfig("CommandHelp.SubCommands.res.SubCommands.confirm.Description", "Confirms removal of a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.confirm.Info", Arrays.asList("Usage: /res confirm", "Confirms removal of a residence."), writer, conf,
	    true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.gset.Description", "Set flags on a specific group for a Residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Description", "Change blacklist and ignorelist options", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Info",
	    Arrays.asList("Usage: /res lset <residence> [blacklist/ignorelist] [material]",
		"Usage: /res lset <residence> Info",
		"Blacklisting a material prevents it from being placed in the residence.",
		"Ignorelist causes a specific material to not be protected by Residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lset.Args", "[residence] [blacklist/ignorelist] [material]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Description", "Remove all residences owned by a player.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Info", Arrays.asList("Usage: /res removeall [owner]",
	    "Removes all residences owned by a specific player.'",
	    "Requires /resadmin if you use it on anyone besides yourself."), writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.removeall.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Description", "List Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Info",
	    Arrays.asList("Usage: /res list <player> <page>",
		"Lists all the residences a player owns (except hidden ones).",
		"If listing your own residences, shows hidden ones as well.",
		"To list everyones residences, use /res listall."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.list.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Description", "List Hidden Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Info",
	    Arrays.asList("Usage: /res listhidden <player> <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listhidden.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listall.Description", "List All Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listall.Info",
	    Arrays.asList("Usage: /res listall <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.listallhidden.Description", "List All Hidden Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.listallhidden.Info",
	    Arrays.asList("Usage: /res listhidden <page>",
		"Lists all hidden residences on the server."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Description", "List Residence Subzones", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Info",
	    Arrays.asList("Usage: /res sublist <residence> <page>",
		"List subzones within a residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.sublist.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.default.Description", "Reset residence to default flags.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.default.Info",
	    Arrays.asList("Usage: /res default <residence>",
		"Resets the flags on a residence to their default.  You must be the owner or an admin to do this."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.default.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Description", "Renames a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Info",
	    Arrays.asList("Usage: /res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
		"The name must not already be taken by another residence."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.rename.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Description", "Kicks player from residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Info",
	    Arrays.asList("Usage: /res kick <player>", "You must be the owner or an admin to do this.",
		"Player should be online."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.kick.Args", "[playername]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Description", "Mirrors Flags", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("Usage: /res mirror [Source Residence] [Target Residence]",
		"Mirrors flags from one residence to another.  You must be owner of both or a admin to do this."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Args", "[residence] [residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.Description", "Buy, Sell, or Rent Residences", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("Usage: /res market ? for more Info"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Description", "Get economy Info on residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Info",
	    Arrays.asList("Usage: /res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.Info.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Description", "Lists rentable and for sale residences.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Info",
	    Arrays.asList("Usage: /res market list"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Description", "Sell a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Info",
	    Arrays.asList("Usage: /res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
		"Another player can buy the residence with /res market buy"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Description", "Set market sign", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Info",
	    Arrays.asList("Usage: /res market sign [residence]", "Sets market sign you are looking at."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sign.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Description", "Buy a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Info",
	    Arrays.asList("Usage: /res market buy [residence]", "Buys a Residence if its for sale."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Description", "Stops selling a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Info",
	    Arrays.asList("Usage: /res market unsell [residence]"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Description", "ent a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Info",
	    Arrays.asList("Usage: /res market rent [residence] <autorenew>",
		"Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Args", "[cresidence] [true/false]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Description", "Make a residence rentable.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Info",
	    Arrays.asList("Usage: /res market rentable [residence] [cost] [days] <repeat>",
		"Makes a residence rentable for [cost] money for every [days] number of days.  If <repeat> is true, the residence will automatically be able to be rented again after the current rent expires."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Description", "Remove a residence from rent or rentable.", writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Info",
	    Arrays.asList("Usage: /res market release [residence]", "If you are the renter, this command releases the rent on the house for you.",
		"If you are the owner, this command makes the residence not for rent anymore."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Args", "[residence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.current.Description", "Show residence your currently in.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.current.Info",
	    Arrays.asList("Usage: /res current"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.signupdate.Description", "Updated residence signs (Admin only)", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.signupdate.Info",
	    Arrays.asList("Usage: /res signupdate"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.Description", "Predefined permission lists", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.Info",
	    Arrays.asList("Predefined permissions that can be applied to a residence."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Description", "Add a list", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Info",
	    Arrays.asList("Usage: /res lists add <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Description", "Remove a list", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Info",
	    Arrays.asList("Usage: /res lists remove <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Description", "Apply a list to a residence", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Info",
	    Arrays.asList("Usage: /res lists apply <listname> <residence>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Description", "Set a flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Info",
	    Arrays.asList("Usage: /res lists set <listname> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Description", "Set a player flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Info",
	    Arrays.asList("Usage: /res lists pset <listname> <player> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Description", "Set a group flag", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res lists gset <listname> <group> <flag> <value>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.view.Description", "View a list.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res lists view <listname>"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Description", "Make land server owned.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Info",
	    Arrays.asList("Usage: /resadmin server [residence]", "Make a residence server owned."),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.server.Args", "[cresidence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Description", "Change owner of a residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Info",
	    Arrays.asList("Usage: /resadmin setowner [residence] [player]"),
	    writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.setowner.Args", "[cresidence]", writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
	    Arrays.asList("Usage: /resreload"),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file.", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.resload.Info",
	    Arrays.asList("Usage: /resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."),
	    writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.ressignconvert.Description", "Converts ResidenceSigns plugins saved signs.", writer, conf,
	    true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.ressignconvert.Info", Arrays.asList("Usage: /ressignconvert"), writer, conf, true);

	GetConfig("CommandHelp.SubCommands.res.SubCommands.version.Description", "how residence version", writer, conf, true);
	GetConfig("CommandHelp.SubCommands.res.SubCommands.version.Info",
	    Arrays.asList("Usage: /res version"),
	    writer, conf, true);

	// Write back config
	try {
	    writer.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
