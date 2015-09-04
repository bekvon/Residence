package com.bekvon.bukkit.residence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Locale {
    public static String GetConfigString(String path, String text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
	conf.addDefault(path, text);
	text = conf.getString(path);
	if (colorize)
	    text = Colors(text);
	copySetting(conf, writer, path);
	return text;
    }

    public static List<String> GetConfigArray(String path, List<String> text, CommentedYamlConfiguration writer, YamlConfiguration conf, Boolean colorize) {
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

    // Language file
    public static void LoadLang(String lang) {

	File f = new File(Residence.instance.getDataFolder(), "Language" + File.separator + lang + ".yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
	CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
	conf.options().copyDefaults(true);

	StringBuilder header = new StringBuilder();
	header.append("NOTE: If you want to modify this file, it is HIGHLY recommended that you make a copy");
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

	GetConfigArray("Language.NewPlayerInfo", Arrays.asList("&e******************************************************",
	    "Please use wooden axe to select opposite sides of your home and execute command &2/res create yourHouseName",
	    "&e******************************************************"), writer, conf, true);

	GetConfigString("Language.NewPlayerInfo", "&ePlease use wooden axe to select opposite sides of your home and execute command &2/res create your name", writer,
	    conf, true);
	GetConfigString("Language.InvalidResidence", "Invalid Residence...", writer, conf, true);
	GetConfigString("Language.InvalidSubzone", "Invalid Subzone...", writer, conf, true);
	GetConfigString("Language.InvalidDirection", "Invalid Direction...", writer, conf, true);
	GetConfigString("Language.InvalidChannel", "Invalid Channel...", writer, conf, true);
	GetConfigString("Language.InvalidAmount", "Invalid Amount...", writer, conf, true);
	GetConfigString("Language.InvalidCost", "Invalid Cost...", writer, conf, true);
	GetConfigString("Language.InvalidDays", "Invalid number of days...", writer, conf, true);
	GetConfigString("Language.InvalidMaterial", "Invalid Material...", writer, conf, true);
	GetConfigString("Language.InvalidBoolean", "Invalid value, must be true(t) or false(f)", writer, conf, true);
	GetConfigString("Language.InvalidArea", "Invalid Area...", writer, conf, true);
	GetConfigString("Language.InvalidGroup", "Invalid Group...", writer, conf, true);
	GetConfigString("Language.InvalidMessageType", "Message type must be enter or remove.", writer, conf, true);
	// GetConfigString("Language.InvalidList", "Invalid List...", writer,
	// conf, true);
	GetConfigString("Language.InvalidFlag", "Invalid Flag...", writer, conf, true);
	GetConfigString("Language.InvalidFlagState", "Invalid flag state, must be true(t), false(f), or remove(r)", writer, conf, true);
	GetConfigString("Language.AreaExists", "Area name already exists.", writer, conf, true);
	GetConfigString("Language.AreaCreate", "Residence Area created, ID %1", writer, conf, true);
	GetConfigString("Language.AreaDiffWorld", "Area is in a different world from residence.", writer, conf, true);
	GetConfigString("Language.AreaCollision", "Area collides with residence %1", writer, conf, true);
	GetConfigString("Language.AreaSubzoneCollision", "Area collides with subzone %1", writer, conf, true);
	GetConfigString("Language.AreaNonExist", "No such area exists.", writer, conf, true);
	GetConfigString("Language.AreaInvalidName", "Invalid Area Name...", writer, conf, true);
	GetConfigString("Language.AreaToSmallTotal", "Selected area smaller than allowed minimal (%1)", writer, conf, true);
	GetConfigString("Language.AreaToSmallX", "Your x selection length is too small. %1 allowed %2", writer, conf, true);
	GetConfigString("Language.AreaToSmallY", "Your selection height is too small. %1 allowed %2", writer, conf, true);
	GetConfigString("Language.AreaToSmallZ", "Your z selection length is too small. %1 allowed %2", writer, conf, true);
	GetConfigString("Language.AreaRename", "Renamed area %1 to %2", writer, conf, true);
	GetConfigString("Language.AreaRemove", "Removed area %1...", writer, conf, true);
	GetConfigString("Language.AreaRemoveLast", "Cannot remove the last area in a residence.", writer, conf, true);
	GetConfigString("Language.AreaNotWithinParent", "Area is not within parent area.", writer, conf, true);
	GetConfigString("Language.AreaUpdate", "Area Updated...", writer, conf, true);
	GetConfigString("Language.AreaMaxPhysical", "You've reached the max physical areas allowed for your residence.", writer, conf, true);
	GetConfigString("Language.AreaSizeLimit", "Area size is not within your allowed limits.", writer, conf, true);
	GetConfigString("Language.AreaHighLimit", "You cannot protect this high up, your limit is %1", writer, conf, true);
	GetConfigString("Language.AreaLowLimit", "You cannot protect this deep, your limit is %1", writer, conf, true);
	GetConfigString("Language.NotInResidence", "You are not in a Residence.", writer, conf, true);
	GetConfigString("Language.Kicked", "You were kicked from residence", writer, conf, true);
	GetConfigString("Language.InResidence", "You are standing in Residence %1", writer, conf, true);
	GetConfigString("Language.ResidenceOwnerChange", "Residence %1 owner changed to %2", writer, conf, true);
	GetConfigString("Language.NonAdmin", "You are not a Residence admin.", writer, conf, true);
	GetConfigString("Language.AdminOnly", "Only admins have access to this command.", writer, conf, true);
	GetConfigString("Language.ChatDisabled", "Residence Chat Disabled...", writer, conf, true);
	GetConfigString("Language.SubzoneRename", "Renamed subzone %1 to %2", writer, conf, true);
	GetConfigString("Language.SubzoneRemove", "Subzone %1 removed.", writer, conf, true);
	GetConfigString("Language.SubzoneCreate", "Created Subzone %1", writer, conf, true);
	GetConfigString("Language.SubzoneCreateFail", "Unable to create subzone %1", writer, conf, true);
	GetConfigString("Language.SubzoneExists", "Subzone %1 already exists.", writer, conf, true);
	GetConfigString("Language.SubzoneCollide", "Subzone collides with subzone %1", writer, conf, true);
	GetConfigString("Language.SubzoneMaxDepth", "You have reached the maximum allowed subzone depth.", writer, conf, true);
	GetConfigString("Language.SubzoneSelectInside", "Both selection points must be inside the residence.", writer, conf, true);
	GetConfigString("Language.SelectPoints", "Select two points first before using this command!", writer, conf, true);
	GetConfigString("Language.SelectOverlap", "&cSelected points overlap with &e%1 &cregion!", writer, conf, true);
	GetConfigString("Language.SelectionSuccess", "Selection Successful!", writer, conf, true);
	GetConfigString("Language.SelectionFail", "Invalid select command...", writer, conf, true);
	GetConfigString("Language.SelectionBedrock", "Selection expanded to your lowest allowed limit.", writer, conf, true);
	GetConfigString("Language.SelectionSky", "Selection expanded to your highest allowed limit.", writer, conf, true);
	GetConfigString("Language.SelectionArea", "Selected area %1 of residence %2", writer, conf, true);
	GetConfigString("Language.SelectDiabled", "You don't have access to selections commands.", writer, conf, true);
	GetConfigString("Language.NoPermission", "You dont have permission for this.", writer, conf, true);
	GetConfigString("Language.OwnerNoPermission", "The owner does not have permission for this.", writer, conf, true);
	GetConfigString("Language.ParentNoPermission", "You don't have permission to make changes to the parent zone.", writer, conf, true);
	GetConfigString("Language.MessageChange", "Message Set...", writer, conf, true);
	GetConfigString("Language.FlagSet", "&e%1 &2flag set for &e%2 &2to &e%3 &2state", writer, conf, true);
	GetConfigString("Language.FlagCheckTrue", "Flag %1 applys to player %2 for residence %3, value = %4", writer, conf, true);
	GetConfigString("Language.FlagCheckFalse", "Flag %1 does not apply to player %2 for residence.", writer, conf, true);
	GetConfigString("Language.FlagsCleared", "Flags Cleared.", writer, conf, true);
	GetConfigString("Language.FlagsDefault", "Flags set to default.", writer, conf, true);
	GetConfigString("Language.Usage", "Command Usage", writer, conf, true);
	GetConfigString("Language.InvalidHelp", "Invalid Help Page...", writer, conf, true);
	GetConfigString("Language.SubCommands", "Sub Commands", writer, conf, true);
	GetConfigString("Language.InvalidList", "Unknown list type, must be blacklist or ignorelist.", writer, conf, true);
	GetConfigString("Language.MaterialGet", "The material name for ID %1 is %2", writer, conf, true);
	GetConfigString("Language.MarketDisabled", "Economy Disabled!", writer, conf, true);
	GetConfigString("Language.MarketList", "Market List", writer, conf, true);
	GetConfigString("Language.SelectionTool", "Selection Tool", writer, conf, true);
	GetConfigString("Language.InfoTool", "Info Tool", writer, conf, true);
	GetConfigString("Language.NoBankAccess", "You dont have bank access.", writer, conf, true);
	GetConfigString("Language.NotEnoughMoney", "You dont have enough money.", writer, conf, true);
	GetConfigString("Language.BankNoMoney", "Not enough money in the bank.", writer, conf, true);
	GetConfigString("Language.BankDeposit", "You deposit %1 into the residence bank.", writer, conf, true);
	GetConfigString("Language.BankWithdraw", "You withdraw %1 from the residence bank.", writer, conf, true);
	GetConfigString("Language.MoneyCharged", "Charged %1 to your %2 account.", writer, conf, true);
	GetConfigString("Language.MoneyAdded", "Got %1 to your %2 account.", writer, conf, true);
	GetConfigString("Language.MoneyCredit", "Credited %1 to your %2 account.", writer, conf, true);
	// GetConfigString("Language.RentDisabled", "Rent system is disabled.",
	// writer, conf, true);
	GetConfigString("Language.RentReleaseInvalid", "Residence %1 is not rented or for rent.", writer, conf, true);
	GetConfigString("Language.RentSellFail", "Cannot sell a Residence if it is for rent.", writer, conf, true);
	GetConfigString("Language.SellRentFail", "Cannot rent a Residence if it is for sale.", writer, conf, true);
	GetConfigString("Language.OwnerBuyFail", "Cannot buy your own land!", writer, conf, true);
	GetConfigString("Language.OwnerRentFail", "Cannot rent your own land!", writer, conf, true);
	GetConfigString("Language.AlreadySellFail", "Residence already for sale!", writer, conf, true);
	GetConfigString("Language.ResidenceBought", "You bought residence %1", writer, conf, true);
	GetConfigString("Language.ResidenceBuy", "Residence %1 has bought residence %2 from you.", writer, conf, true);
	GetConfigString("Language.ResidenceBuyTooBig", "This residence has areas bigger then your allowed max.", writer, conf, true);
	GetConfigString("Language.ResidenceNotForSale", "Residence is not for sale.", writer, conf, true);
	GetConfigString("Language.ResidenceForSale", "Residence %1 is now for sale for %2", writer, conf, true);
	GetConfigString("Language.ResidenceStopSelling", "Residence is no longer for sale.", writer, conf, true);
	GetConfigString("Language.ResidenceTooMany", "You already own the max number of residences your allowed to.", writer, conf, true);
	GetConfigString("Language.ResidenceMaxRent", "You already are renting the maximum number of residences your allowed to.", writer, conf, true);
	GetConfigString("Language.ResidenceAlreadyRent", "Residence is already for rent...", writer, conf, true);
	GetConfigString("Language.ResidenceNotForRent", "Residence not for rent...", writer, conf, true);
	GetConfigString("Language.ResidenceNotRented", "Residence not rented.", writer, conf, true);
	GetConfigString("Language.ResidenceUnrent", "Residence %1 has been unrented.", writer, conf, true);
	GetConfigString("Language.ResidenceRemoveRentable", "Residence %1 is no longer rentable.", writer, conf, true);
	GetConfigString("Language.ResidenceForRentSuccess", "Residence %1 is now for rent for %2 every %3 days.", writer, conf, true);
	GetConfigString("Language.ResidenceRentSuccess", "You have rented Residence %1 for %2 days.", writer, conf, true);
	GetConfigString("Language.ResidenceAlreadyRented", "Residence %1 has currently been rented to %2", writer, conf, true);
	GetConfigString("Language.ResidenceAlreadyExists", "A residence named %1 already exists.", writer, conf, true);
	GetConfigString("Language.ResidenceCreate", "You have created residence %1!", writer, conf, true);
	GetConfigString("Language.ResidenceRename", "Renamed Residence %1 to %2", writer, conf, true);
	GetConfigString("Language.ResidenceRemove", "Residence %1 has been removed...", writer, conf, true);
	GetConfigString("Language.RentDisabled", "Rent is disabled...", writer, conf, true);
	GetConfigString("Language.RentDisableRenew", "Residence %1 will now no longer re-rent upon expire.", writer, conf, true);
	GetConfigString("Language.RentEnableRenew", "Residence %1 will now automatically re-rent upon expire.", writer, conf, true);
	GetConfigString("Language.RentableDisableRenew", "%1 will no longer renew rentable status upon expire.", writer, conf, true);
	GetConfigString("Language.RentableEnableRenew", "%1 will now automatically renew rentable status upon expire.", writer, conf, true);
	GetConfigString("Language.LandForSale", "Land For Sale", writer, conf, true);
	GetConfigString("Language.SellAmount", "Sell Amount", writer, conf, true);
	GetConfigString("Language.LeaseExpire", "Lease Expire Time", writer, conf, true);
	GetConfigString("Language.RentExpire", "Rent Expire Time", writer, conf, true);
	GetConfigString("Language.RentableAutoRenew", "Rentable Auto Renew", writer, conf, true);
	GetConfigString("Language.RentAutoRenew", "Rent Auto Renew", writer, conf, true);
	GetConfigString("Language.RentableLand", "Rentable Land", writer, conf, true);
	GetConfigString("Language.ListMaterialAdd", "%1 added to the residence %2", writer, conf, true);
	GetConfigString("Language.ListMaterialRemove", "%1 removed from the residence %2", writer, conf, true);
	GetConfigString("Language.ItemBlacklisted", "You are blacklisted from using this item here.", writer, conf, true);
	GetConfigString("Language.RentedModifyDeny", "Cannot modify a rented residence.", writer, conf, true);
	GetConfigString("Language.WorldPVPDisabled", "World PVP is disabled.", writer, conf, true);
	GetConfigString("Language.NoPVPZone", "No PVP zone.", writer, conf, true);
	GetConfigString("Language.FlagDeny", "You dont have %1 permission<s> here.", writer, conf, true);
	GetConfigString("Language.FlagSetDeny", "Owner does not have access to flag %1", writer, conf, true);
	GetConfigString("Language.SelectPoint", "Placed %1 Selection Point", writer, conf, true);
	GetConfigString("Language.ResidenceChat", "Residence chat toggled %1", writer, conf, true);
	GetConfigString("Language.ResidenceMoveDeny", "You dont have movement permission for Residence %1", writer, conf, true);
	GetConfigString("Language.ResidenceFlagDeny", "You dont have %1 permission for Residence %2", writer, conf, true);
	GetConfigString("Language.TeleportDeny", "You dont have teleport access.", writer, conf, true);
	GetConfigString("Language.TeleportSuccess", "Teleported!", writer, conf, true);
	GetConfigString("Language.TeleportConfirm",
	    "&eThis teleport is not safe, you will fall for &c%1 &eblocks. Use &2/res tpconfirm &eto perform teleportation anyways.", writer, conf, true);
	GetConfigString("Language.TeleportStarted",
	    "&eTeleportation to %1 started, don't move for next %2 sec.", writer, conf, true);
	GetConfigString("Language.TeleportCanceled",
	    "&eTeleportation canceled!", writer, conf, true);
	GetConfigString("Language.NoTeleportConfirm", "There is no teleports waiting for confirmation!", writer, conf, true);
	GetConfigString("Language.TeleportNear", "Teleported to near residence.", writer, conf, true);
	GetConfigString("Language.TeleportNoFlag", "You dont have teleport access for that residence.", writer, conf, true);
	GetConfigString("Language.SetTeleportLocation", "Teleport Location Set...", writer, conf, true);
	GetConfigString("Language.HelpPageHeader", "Help Pages - %1 - Page <%2 of %3>", writer, conf, true);
	GetConfigString("Language.ListExists", "List already exists...", writer, conf, true);
	GetConfigString("Language.ListRemoved", "List removed...", writer, conf, true);
	GetConfigString("Language.ListCreate", "Created list %1", writer, conf, true);
	GetConfigString("Language.LeaseRenew", "Lease valid until %1", writer, conf, true);
	GetConfigString("Language.LeaseRenewMax", "Lease renewed to maximum allowed", writer, conf, true);
	GetConfigString("Language.LeaseNotExpire", "No such lease, or lease does not expire.", writer, conf, true);
	GetConfigString("Language.LeaseRenewalCost", "Renewal cost for area %1 is %2", writer, conf, true);
	GetConfigString("Language.LeaseInfinite", "Lease time set to infinite...", writer, conf, true);
	GetConfigString("Language.PermissionsApply", "Permissions applied to residence.", writer, conf, true);
	GetConfigString("Language.PhysicalAreas", "Physical Areas", writer, conf, true);
	GetConfigString("Language.CurrentArea", "Current Area", writer, conf, true);
	GetConfigString("Language.TotalWorth", "Total worth of residence:&3 %1 (%2)", writer, conf, true);
	GetConfigString("Language.LeaseExpire", "Lease Expiration", writer, conf, true);
	GetConfigString("Language.NotOnline", "Target player must be online.", writer, conf, true);
	GetConfigString("Language.ResidenceGiveLimits", "Cannot give residence to target player, because it is outside the target players limits.", writer, conf, true);
	GetConfigString("Language.ResidenceGive", "You give residence %1 to player %2", writer, conf, true);
	GetConfigString("Language.ResidenceRecieve", "You have recieved residence %1 from player %2", writer, conf, true);
	// GetConfigString("Language.#ResidenceListAll", "Residences - <Page %1
	// of %2> - removed, use GenericPage now", writer, conf, true);
	GetConfigString("Language.ResidenceListAllEmpty", "No Residences exists on the server...", writer, conf, true);
	GetConfigString("Language.InvalidPage", "Invalid Page...", writer, conf, true);
	GetConfigString("Language.NextPage", "Next Page", writer, conf, true);
	GetConfigString("Language.NextInfoPage", "&2| &eNext Page &2>>>", writer, conf, true);
	GetConfigString("Language.PrevInfoPage", "&2<<< &ePrev Page &2|", writer, conf, true);
	GetConfigString("Language.RemovePlayersResidences", "Removed all residences belonging to player %1", writer, conf, true);
	GetConfigString("Language.GenericPage", "Page %1 of %2", writer, conf, true);
	GetConfigString("Language.ResidenceRentedBy", "Rented by %1", writer, conf, true);
	GetConfigString("Language.InvalidCharacters", "Invalid characters detected...", writer, conf, true);
	GetConfigString("Language.InvalidNameCharacters", "Name contained unallowed characters...", writer, conf, true);
	GetConfigString("Language.DeleteConfirm", "Are you sure you want to delete residence %1, use /res confirm to confirm.", writer, conf, true);
	GetConfigString("Language.SelectTooHigh", "Warning, selection went above top of map, limiting.", writer, conf, true);
	GetConfigString("Language.SelectTooLow", "Warning, selection went below bottom of map, limiting.", writer, conf, true);
	GetConfigString("Language.WorldEditNotFound", "WorldEdit was not detected.", writer, conf, true);
	GetConfigString("Language.NoResHere", "There is no residence in there.", writer, conf, true);
	GetConfigString("Language.DeleteSubzoneConfirm", "Are you sure you want to delete subzone %1, use /res confirm to confirm.", writer, conf, true);
	GetConfigString("Language.SubzoneOwnerChange", "Subzone %1 owner changed to %2", writer, conf, true);
	GetConfigString("Language.CoordsTop", "X:%1 Y:%2 Z:%3", writer, conf, true);
	GetConfigString("Language.CoordsBottom", "X:%1 Y:%2 Z:%3", writer, conf, true);
	GetConfigString("Language.AdminToggle", "Automatic resadmin toggle turned %1", writer, conf, true);
	GetConfigString("Language.NoSpawn", "You do not have move permissions at your spawn point.  Relocating", writer, conf, true);
	GetConfigString("Language.CompassTargetReset", "Your compass has been reset", writer, conf, true);
	GetConfigString("Language.CompassTargetSet", "Your compass now points to %1", writer, conf, true);
	writer.addComment("Language.Description", "The below lines are mostly a word bank for various uses.");
	GetConfigString("Language.Description", "Description", writer, conf, true);
	GetConfigString("Language.Land", "Land", writer, conf, true);
	GetConfigString("Language.Cost", "Cost", writer, conf, true);
	GetConfigString("Language.Selection", "Selection", writer, conf, true);
	GetConfigString("Language.Total", "Total", writer, conf, true);
	GetConfigString("Language.Size", "Size", writer, conf, true);
	GetConfigString("Language.Expanding", "Expanding", writer, conf, true);
	GetConfigString("Language.Contracting", "Contracting", writer, conf, true);
	GetConfigString("Language.North", "North", writer, conf, true);
	GetConfigString("Language.West", "West", writer, conf, true);
	GetConfigString("Language.South", "South", writer, conf, true);
	GetConfigString("Language.East", "East", writer, conf, true);
	GetConfigString("Language.Shifting", "Shifting", writer, conf, true);
	GetConfigString("Language.Up", "Up", writer, conf, true);
	GetConfigString("Language.Down", "Down", writer, conf, true);
	GetConfigString("Language.Error", "Error", writer, conf, true);
	GetConfigString("Language.Flags", "Flags", writer, conf, true);
	GetConfigString("Language.Your", "Your", writer, conf, true);
	GetConfigString("Language.Group", "Group", writer, conf, true);
	GetConfigString("Language.Others", "Others", writer, conf, true);
	GetConfigString("Language.Primary", "Primary", writer, conf, true);
	GetConfigString("Language.Secondary", "Secondary", writer, conf, true);
	GetConfigString("Language.Selection", "Selection", writer, conf, true);
	GetConfigString("Language.Moved", "Moved", writer, conf, true);
	GetConfigString("Language.Status", "Status", writer, conf, true);
	GetConfigString("Language.Available", "Available", writer, conf, true);
	GetConfigString("Language.On", "On", writer, conf, true);
	GetConfigString("Language.Off", "Off", writer, conf, true);
	GetConfigString("Language.Name", "Name", writer, conf, true);
	GetConfigString("Language.Lists", "Lists", writer, conf, true);
	GetConfigString("Language.Residences", "Residences", writer, conf, true);
	GetConfigString("Language.Residence", "Residence", writer, conf, true);
	GetConfigString("Language.Count", "Count", writer, conf, true);
	GetConfigString("Language.Owner", "Owner", writer, conf, true);
	GetConfigString("Language.World", "World", writer, conf, true);
	GetConfigString("Language.Subzones", "Subzones", writer, conf, true);
	GetConfigString("Language.CoordsT", "Top Coords", writer, conf, true);
	GetConfigString("Language.CoordsB", "Bottom Coords", writer, conf, true);
	GetConfigString("Language.TurnOn", "on", writer, conf, true);
	GetConfigString("Language.TurnOff", "off", writer, conf, true);
	GetConfigString("Language.LimitsTop", "----------------------------------------", writer, conf, true);
	GetConfigString("Language.LimitsPGroup", "&7- &ePermissions Group:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsRGroup", "&7- &eResidence Group:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsAdmin", "&7- &eResidence Admin:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsCanCreate", "&7- &eCan Create Residences:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMaxRes", "&7- &eMax Residences:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMaxEW", "&7- &eMax East/West Size:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMaxNS", "&7- &eMax North/South Size:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMaxUD", "&7- &eMax Up/Down Size:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMinMax", "&7- &eMin/Max Protection Height:&3 %1 to %2", writer, conf, true);
	GetConfigString("Language.LimitsMaxSub", "&7- &eMax Subzone Depth:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsEnterLeave", "&7- &eCan Set Enter/Leave Messages:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsNumberOwn", "&7- &eNumber of Residences you own:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsCost", "&7- &eResidence Cost Per Block:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsSell", "&7- &eResidence Sell Cost Per Block:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsFlag", "&7- &eFlag Permissions:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsMaxDays", "&7- &eMax Lease Days:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsLeaseTime", "&7- &eLease Time Given on Renew:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsRenewCost", "&7- &eRenew Cost Per Block:&3 %1", writer, conf, true);
	GetConfigString("Language.LimitsBottom", "----------------------------------------", writer, conf, true);

	writer.addComment("CommandHelp", "");

	GetConfigString("CommandHelp.Description", "Contains Help for Residence", writer, conf, true);
	GetConfigString("CommandHelp.SubCommands.res.Description", "Main Residence Command", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.Info", Arrays.asList("See the residence wiki for more help.", "Wiki: residencebukkitmod.wikispaces.com",
	    "Use /res [command] ? <page> to view more help information."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.Description", "Selection Commands", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.Info",
	    Arrays.asList("This command selects areas for usage with residence.", "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Description", "Display selected coordinates", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.coords.Info", Arrays.asList("Usage: /res select coords"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Description", "Display selected size", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.size.Info", Arrays.asList("Usage: /res select size"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Description", "Display selection cost", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.cost.Info", Arrays.asList("Usage: /res select cost"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Description", "Expand Selection Vertically", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.vert.Info",
	    Arrays.asList("Usage: /res select vert", "Will expand selection as high and as low as allowed."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Description", "Expand Selection to Sky", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.sky.Info", Arrays.asList("Usage: /res select sky",
	    "Expands as high as your allowed to go."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Description", "Expand Selection to Bedrock", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.bedrock.Info",
	    Arrays.asList("Usage: /res select bedrock", "Expands as low as your allowed to go."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Description", "Expand selection in a direction.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.expand.Info",
	    Arrays.asList("Usage: /res select expand <amount>", "Expands <amount> in the direction your looking."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Description", "Shift selection in a direction", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.shift.Info",
	    Arrays.asList("Usage: /res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Description", "Select the chunk your currently in.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.chunk.Info",
	    Arrays.asList("Usage: /res select chunk", "Selects the chunk your currently standing in."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Description", "Select a existing area in a residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.residence.Info",
	    Arrays.asList("Usage /res select <Residence> <AreaID>", "Selects a existing area in a residence."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Description", "Set selection using the current WorldEdit selection.",
	    writer, conf,
	    true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.select.SubCommands.worldedit.Info",
	    Arrays.asList("Usage /res select worldedit", "Sets selection area using the current WorldEdit selection."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.create.Description", "Create Residences", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.create.Info", Arrays.asList("Usage: /res create <residence name>"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.remove.Description", "Remove residences.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.remove.Info", Arrays.asList("Usage: /res remove <residence name>"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.removeall.Description", "Remove all residences owned by a player.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.removeall.Info", Arrays.asList("Usage: /res removeall [owner]",
	    "Removes all residences owned by a specific player.'",
	    "Requires /resadmin if you use it on anyone besides yourself."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.confirm.Description", "Confirms removal of a residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.confirm.info", Arrays.asList("Usage: /res confirm", "Confirms removal of a residence."), writer, conf,
	    true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.subzone.Description", "Create subzones in residences.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.subzone.Info",
	    Arrays.asList("Usage: /res subzone <residence name> [subzone name]", "If residence name is left off, will attempt to use residence your standing in."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.Description", "Manage physical areas for a residence.", writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Description", "List physical areas in a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.list.Info",
	    Arrays.asList("Usage: /res subzone <residence name> [subzone name]", "If residence name is left off, will attempt to use residence your standing in."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Description", "List coordinates and other info for areas", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.listall.Info", Arrays.asList("Usage: /res area listall [residence] <page>"), writer,
	    conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Description", "Add physical areas to a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.add.Info",
	    Arrays.asList("Usage: /res area add [residence] [areaID]", "You must first select two points first."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Description", "Remove physical areas from a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.remove.Info", Arrays.asList("Usage: /res area remove [residence] [areaID]"), writer,
	    conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Description", "Replace physical areas in a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.area.SubCommands.replace.Info", Arrays.asList("Usage: /res area replace [residence] [areaID]",
	    "You must first select two points first.", "Replacing a area will charge the difference in size if the new area is bigger."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.info.Description", "Show info on a residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.info.info",
	    Arrays.asList("Usage: /res info <residence>", "Leave off <residence> to display info for the residence your currently in."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.limits.Description", "Show your limits.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.limits.info",
	    Arrays.asList("Usage: /res limits", "Shows the limitations you have on creating and managing residences."), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.message.Description", "Manage residence enter / leave messages", writer, conf, true);
	GetConfigArray(
	    "CommandHelp.SubCommands.res.SubCommands.message.info", Arrays.asList("Usage: /res message <residence> [enter/leave] [message]",
		"Set the enter or leave message of a residence.", "Usage: /res message <residence> remove [enter/leave]", "Removes a enter or leave message."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lease.Description", "Manage residence leases", writer, conf, true);
	GetConfigArray(
	    "CommandHelp.SubCommands.res.SubCommands.lease.info", Arrays.asList("Usage: /res lease [renew/cost] [residence]",
		"/res lease cost will show the cost of renewing a residence lease.", "/res lease renew will renew the residence provided you have enough money."),
	    writer, conf, true);
	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Description", "Set the lease time (admin only)", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lease.SubCommands.set.Info",
	    Arrays.asList("Usage: /resadmin lease set [residence] [#days/infinite]", "Sets the lease time to a specified number of days, or infinite."), writer, conf,
	    true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.bank.Description", "Manage money in a Residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.bank.Info",
	    Arrays.asList("Usage: /res bank [deposit/withdraw] [amount]", "You must be standing in a Residence", "You must have the +bank flag."), writer, conf,
	    true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.tp.Description", "Teleport to a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.tp.Info",
	    Arrays.asList("Usage: /res tp [residence]", "Teleports you to a residence, you must have +tp flag access or be the owner.",
		"Your permission group must also be allowed to teleport by the server admin."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Description", "Ignore unsafe teleportation warning and teleport anyways", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.tpconfirm.Info",
	    Arrays.asList("Usage: /res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.tpset.Description", "Set the teleport location of a Residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.tpset.Info",
	    Arrays.asList("Usage: /res tpset", "This will set the teleport location for a residence to where your standing.",
		"You must be standing in the residence to use this command.", "You must also be the owner or have the +admin flag for the residence."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.set.Description", "Set general flags on a Residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.set.Info", Arrays.asList("Usage: /res set <residence> [flag] [true/false/remove]",
	    "To see a list of flags, use /res flags ?", "These flags apply to any players who do not have the flag applied specifically to them. (see /res pset ?)"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.pset.Description", "Set flags on a specific player for a Residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.pset.Info", Arrays.asList("Usage: /res pset <residence> [player] [flag] [true/false/remove]",
	    "Usage: /res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.gset.Description", "Set flags on a specific group for a Residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"), writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lset.Description", "Change blacklist and ignorelist options", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lset.Info",
	    Arrays.asList("Usage: /res lset <residence> [blacklist/ignorelist] [material]",
		"Usage: /res lset <residence> info",
		"Blacklisting a material prevents it from being placed in the residence.",
		"Ignorelist causes a specific material to not be protected by Residence."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.flags.Description", "List of flags", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.flags.Info",
	    Arrays.asList("For flag values, usually true allows the action, and false denys the action.",
		"&2build &3- &eallows or denys building",
		"&2use &3- &eallows or denys use of doors, lever, buttons, etc...",
		"&2move &3- &eallows or denys movement in the residence.",
		"&2container &3- &eallows or denys use of furnaces, chests, dispensers, etc...",
		"&2trusted &3- &eGives build, use, move, container and tp flags",
		"&2place &3- &eallows or denys only placement of blocks, overrides the build flag.",
		"&2destroy &3- &eallows or denys only destruction of blocks, overrides the build flag.",
		"&2pvp &3- &eallow or deny pvp in the residence",
		"&2tp &3- &eallow or disallow teleporting to the residence.",
		"&2admin &3- &egives a player permission to change flags on a residence.",
		"&2subzone &3- &eallow a player to make subzones in the residence.",
		"&2monsters &3- &eallows or denys monster spawns",
		"&2animals &3- &eallows or denys animal spawns.",
		"&2animalkilling &3- &eallows or denys animal killing.",
		"&2mobkilling &3- &eallows or denys mob killing.",
		"&2nofly &3- &eallows or denys fly in residence.",
		"&2vehicledestroy &3- &eallows or denys vehicle destroy.",
		"&2shear &3- &eallows or denys sheep shear.",
		"&2leash &3- &eallows or denys aninal leash.",
		"&2healing &3- &esetting to true makes the residence heal its occupants",
		"&2tnt &3- &eallow or deny tnt explosions",
		"&2creeper &3- &eallow or deny creeper explosions",
		"&2ignite &3- &eallows or denys fire ignition.",
		"&2firespread &3- &eallows or denys fire spread.",
		"&2bucket &3- &eallow or deny bucket use.",
		"&2flow &3- &eallows or denys liquid flow.",
		"&2lavaflow &3- &eallows or denys lava flow, overrides flow",
		"&2waterflow &3- &eallows or denys water flow, overrides flow",
		"&2damage &3- &eallows or denys all entity damage within the residence.",
		"&2piston &3- &eallow or deny pistons from pushing or pulling blocks in the residence.",
		"&2hidden &3- &ehides residence from list or listall commands.",
		"&2cake &3- &eallows or denys players to eat cake",
		"&2lever &3- &eallows or denys players to use levers",
		"&2button &3- &eallows or denys players to use buttons",
		"&2diode &3- &eallows or denys players to use redstone repeaters",
		"&2door &3- &eallows or denys players to use doors and trapdoors",
		"&2table &3- &eallows or denys players to use workbenches",
		"&2enchant &3- &eallows or denys players to use enchanting tables",
		"&2brew &3- &eallows or denys players to use brewing stands",
		"&2bed &3- &eallows or denys players to use beds",
		"&2button &3- &eallows or denys players to use buttons",
		"&2pressure &3- &eallows or denys players to use pressure plates",
		"&2note &3- &eallows or denys players to use note blocks",
		"&2redstone &3- &eGives lever, diode, button, pressure, note flags",
		"&2craft &3- &eGives table, enchant, brew flags",
		"&2burn &3- &eallows or denys Mob combustion in residences"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.list.Description", "List Residences", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.list.Info",
	    Arrays.asList("Usage: /res list <player> <page>",
		"Lists all the residences a player owns (except hidden ones).",
		"If listing your own residences, shows hidden ones as well.",
		"To list everyones residences, use /res listall."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.listhidden.Description", "List Hidden Residences (ADMIN ONLY)", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.listhidden.Info",
	    Arrays.asList("Usage: /res listhidden <player> <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.listall.Description", "List All Residences", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.listall.Info",
	    Arrays.asList("Usage: /res listall <page>",
		"Lists hidden residences for a player."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.listallhidden.Description", "List All Hidden Residences (ADMIN ONLY)", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.listallhidden.Info",
	    Arrays.asList("Usage: /res listhidden <page>",
		"Lists all hidden residences on the server."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.sublist.Description", "List Residence Subzones", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.sublist.Info",
	    Arrays.asList("Usage: /res sublist <residence> <page>",
		"List subzones within a residence."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.default.Description", "Reset residence to default flags.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.default.Info",
	    Arrays.asList("Usage: /res default <residence>",
		"Resets the flags on a residence to their default.  You must be the owner or an admin to do this."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.rename.Description", "Renames a residence.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.rename.Info",
	    Arrays.asList("Usage: /res rename [OldName] [NewName]", "You must be the owner or an admin to do this.",
		"The name must not already be taken by another residence."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.mirror.Description", "Mirrors Flags", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("Usage: /res mirror [Source Residence] [Target Residence]",
		"Mirrors flags from one residence to another.  You must be owner of both or a admin to do this."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.Description", "Buy, Sell, or Rent Residences", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.mirror.Info",
	    Arrays.asList("Usage: /res market ? for more info"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.info.Description", "Get economy info on residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.info.Info",
	    Arrays.asList("Usage: /res market info [residence]", "Shows if the Residence is for sale or for rent, and the cost."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Description", "Lists rentable and for sale residences.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.list.Info",
	    Arrays.asList("Usage: /res market list"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Description", "Sell a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.sell.Info",
	    Arrays.asList("Usage: /res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
		"Another player can buy the residence with /res market buy"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Description", "Buy a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.buy.Info",
	    Arrays.asList("Usage: /res market buy [residence]", "Buys a Residence if its for sale."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Description", "Stops selling a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.unsell.Info",
	    Arrays.asList("Usage: /res market unsell [residence]"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Description", "ent a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rent.Info",
	    Arrays.asList("Usage: /res market rent [residence] <autorenew>",
		"Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Description", "Make a residence rentable.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.rentable.Info",
	    Arrays.asList("Usage: /res market rentable [residence] [cost] [days] <repeat>",
		"Makes a residence rentable for [cost] money for every [days] number of days.  If <repeat> is true, the residence will automatically be able to be rented again after the current rent expires."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Description", "Remove a residence from rent or rentable.", writer, conf,
	    true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.market.SubCommands.release.Info",
	    Arrays.asList("Usage: /res market release [residence]", "If you are the renter, this command releases the rent on the house for you.",
		"If you are the owner, this command makes the residence not for rent anymore."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.current.Description", "Show residence your currently in.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.current.Info",
	    Arrays.asList("Usage: /res current"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.Description", "Predefined permission lists", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.Info",
	    Arrays.asList("Predefined permissions that can be applied to a residence."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Description", "Add a list", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.add.Info",
	    Arrays.asList("Usage: /res lists add <listname>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Description", "Remove a list", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.remove.Info",
	    Arrays.asList("Usage: /res lists remove <listname>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Description", "Apply a list to a residence", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.apply.Info",
	    Arrays.asList("Usage: /res lists apply <listname> <residence>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Description", "Set a flag", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.set.Info",
	    Arrays.asList("Usage: /res lists set <listname> <flag> <value>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Description", "Set a player flag", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.pset.Info",
	    Arrays.asList("Usage: /res lists pset <listname> <player> <flag> <value>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Description", "Set a group flag", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res lists gset <listname> <group> <flag> <value>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.view.Description", "View a list.", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.lists.SubCommands.gset.Info",
	    Arrays.asList("Usage: /res lists view <listname>"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.server.Description", "Make land server owned (admin only).", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.server.Info",
	    Arrays.asList("Usage: /resadmin server [residence]", "Make a residence server owned."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.setowner.Description", "Change owner of a residence (admin only).", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.setowner.Info",
	    Arrays.asList("Usage: /resadmin setowner [residence] [player]"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence (admin only).", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
	    Arrays.asList("Usage: /resreload"),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file (UNSAFE, admin only).", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.resload.Info",
	    Arrays.asList("Usage: /resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."),
	    writer, conf, true);

	GetConfigString("CommandHelp.SubCommands.res.SubCommands.version.Description", "how residence version", writer, conf, true);
	GetConfigArray("CommandHelp.SubCommands.res.SubCommands.version.Info",
	    Arrays.asList("Usage: /res version"),
	    writer, conf, true);
	    // GetConfigArray("CommandHelp.SubCommands.res.select.Info",
	    // Arrays.asList("", "", ""), writer, conf, true);

	// Write back config
	try {
	    writer.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }
}
