package com.bekvon.bukkit.residence.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bekvon.bukkit.residence.NewLanguage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Sorting;

public class SetFlag {

    private String residence;
    private Player player;
    private String targetPlayer = null;
    private Inventory inventory;
    private LinkedHashMap<String, Integer> permMap = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, List<String>> description = new LinkedHashMap<String, List<String>>();
    private boolean admin = false;

    public SetFlag(String residence, Player player, boolean admin) {
	this.residence = residence;
	this.player = player;
	this.admin = admin;
	fillFlagDescriptions();
    }

    public void setAdmin(boolean state) {
	this.admin = state;
    }

    public boolean isAdmin() {
	return this.admin;
    }

    public void setTargePlayer(String player) {
	this.targetPlayer = player;
    }

    public String getResidence() {
	return this.residence;
    }

    public Player getPlayer() {
	return this.player;
    }

    public Inventory getInventory() {
	return this.inventory;
    }

    public void toggleFlag(int slot, ClickType click, InventoryAction action) {
	ItemStack item = this.inventory.getItem(slot);
	if (item == null)
	    return;
	String command = "true";
	if (click.isLeftClick() && action != InventoryAction.MOVE_TO_OTHER_INVENTORY)
	    command = "true";
	else if (click.isRightClick() && action != InventoryAction.MOVE_TO_OTHER_INVENTORY)
	    command = "false";
	else if (click.isLeftClick() && action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
	    command = "remove";
	else if (click.isRightClick() && action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
	    return;

	String flag = "";
	int i = 0;
	for (Entry<String, Integer> one : permMap.entrySet()) {
	    flag = one.getKey();
	    if (i == slot) {
		break;
	    }
	    i++;
	}
	if (targetPlayer == null) {
	    if (admin)
		Bukkit.dispatchCommand(player, "resadmin set " + residence + " " + flag + " " + command);
	    else
		Bukkit.dispatchCommand(player, "res set " + residence + " " + flag + " " + command);
	} else {
	    if (admin)
		Bukkit.dispatchCommand(player, "resadmin pset " + residence + " " + targetPlayer + " " + flag + " " + command);
	    else
		Bukkit.dispatchCommand(player, "res pset " + residence + " " + targetPlayer + " " + flag + " " + command);
	}
    }

    public void recalculateInv() {
	recalculateInv(Residence.getResidenceManager().getByName(residence));
    }

    private void fillFlagDescriptions() {
	List<String> list = NewLanguage.getMessageList("CommandHelp.SubCommands.res.SubCommands.flags.Info");
	for (Entry<String, Boolean> one : Residence.getPermissionManager().getAllFlags().getFlags().entrySet()) {
	    for (String onelist : list) {

		String onelisttemp = ChatColor.stripColor(onelist);

		String splited = "";
		if (!onelisttemp.contains("-"))
		    continue;

		splited = onelisttemp.split("-")[0];
		if (!splited.toLowerCase().contains(one.getKey().toLowerCase()))
		    continue;

		List<String> lore = new ArrayList<String>();

		int i = 0;
		String sentence = "";
		for (String oneWord : onelist.split(" ")) {
		    sentence += oneWord + " ";
		    if (i > 4) {
			lore.add(ChatColor.YELLOW + sentence);
			sentence = "";
			i = 0;
		    }
		    i++;
		}
		lore.add(ChatColor.YELLOW + sentence);
		description.put(one.getKey(), lore);
		break;

	    }
	}
    }

    public void recalculateInv(ClaimedResidence res) {

	Map<String, Boolean> globalFlags = Residence.getPermissionManager().getAllFlags().getFlags();

	Map<String, Boolean> resFlags = new HashMap<String, Boolean>();

	for (Entry<String, Boolean> one : res.getPermissions().getFlags().entrySet()) {
	    resFlags.put(one.getKey(), one.getValue());
	}

	if (targetPlayer != null) {
	    ArrayList<String> PosibleResPFlags = res.getPermissions().getposibleFlags();
	    Map<String, Boolean> temp = new HashMap<String, Boolean>();
	    for (String one : PosibleResPFlags) {
		if (globalFlags.containsKey(one))
		    temp.put(one, globalFlags.get(one));
	    }
	    globalFlags = temp;

	    Map<String, Boolean> pFlags = res.getPermissions().getPlayerFlags(targetPlayer);

	    if (pFlags != null)
		for (Entry<String, Boolean> one : pFlags.entrySet()) {
		    resFlags.put(one.getKey(), one.getValue());
		}
	}

	for (Entry<String, Boolean> one : globalFlags.entrySet()) {
	    if (resFlags.containsKey(one.getKey()))
		permMap.put(one.getKey(), resFlags.get(one.getKey()) ? 1 : 0);
	    else
		permMap.put(one.getKey(), 2);
	}
	String title = "";
	if (targetPlayer == null)
	    title = NewLanguage.getMessage("Language.Gui.Set.Title").replace("%1%", res.getName());
	else
	    title = NewLanguage.getMessage("Language.Gui.Pset.Title").replace("%1%", targetPlayer).replace("%2%", res.getName());

	if (title.length() > 32) {
	    title = title.substring(0, Math.min(title.length(), 32));
	}

	Inventory GuiInv = Bukkit.createInventory(null, 54, title);
	int i = 0;

	permMap = (LinkedHashMap<String, Integer>) Sorting.sortByKeyASC(permMap);

	FlagData flagData = FlagUtil.getFlagData();

	for (Entry<String, Integer> one : permMap.entrySet()) {
	    ItemStack MiscInfo = Residence.getConfigManager().getGuiRemove();

	    switch (one.getValue()) {
	    case 0:
		MiscInfo = Residence.getConfigManager().getGuiFalse();
		break;
	    case 1:
		MiscInfo = Residence.getConfigManager().getGuiTrue();
		break;
	    case 2:
		break;
	    }

	    if (flagData.contains(one.getKey()))
		MiscInfo = flagData.getItem(one.getKey());

	    if (one.getValue() == 1) {
		ItemMeta im = MiscInfo.getItemMeta();
		im.addEnchant(Enchantment.LUCK, 1, true);
		MiscInfo.setItemMeta(im);
	    } else
		MiscInfo.removeEnchantment(Enchantment.LUCK);

	    ItemMeta MiscInfoMeta = MiscInfo.getItemMeta();
	    MiscInfoMeta.setDisplayName(ChatColor.GREEN + one.getKey());

	    List<String> lore = new ArrayList<String>();

	    switch (one.getValue()) {
	    case 0:
		lore.add(ChatColor.GOLD + "Flag state: " + ChatColor.DARK_RED + "False");
		break;
	    case 1:
		lore.add(ChatColor.GOLD + "Flag state: " + ChatColor.GREEN + "True");
		break;
	    case 2:
		lore.add(ChatColor.GOLD + "Flag state: " + ChatColor.RED + "Removed");
		break;
	    }

	    if (description.containsKey(one.getKey()))
		lore.addAll(description.get(one.getKey()));

	    lore.addAll(NewLanguage.getMessageList("Language.Gui.Actions"));

	    MiscInfoMeta.setLore(lore);

	    MiscInfo.setItemMeta(MiscInfoMeta);
	    GuiInv.setItem(i, MiscInfo);
	    i++;
	    if (i > 53)
		break;
	}

	this.inventory = GuiInv;
    }
}
