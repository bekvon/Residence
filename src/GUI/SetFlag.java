package GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bekvon.bukkit.residence.NewLanguage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.Sorting;

import net.md_5.bungee.api.ChatColor;

public class SetFlag {

    private String residence;
    private Player player;
    private String targetPlayer = null;
    private Inventory inventory;
    private LinkedHashMap<String, Integer> permMap = new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, List<String>> description = new LinkedHashMap<String, List<String>>();

    public SetFlag(String residence, Player player) {
	this.residence = residence;
	this.player = player;
	fillFlagDescriptions();
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
	else if (click.isRightClick())
	    command = "false";
	else if (click.isLeftClick() && action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
	    command = "remove";

	String flag = "";
	int i = 0;
	for (Entry<String, Integer> one : permMap.entrySet()) {
	    flag = one.getKey();
	    if (i == slot) {
		break;
	    }
	    i++;
	}
	if (targetPlayer == null)
	    Bukkit.dispatchCommand(player, "res set " + residence + " " + flag + " " + command);
	else
	    Bukkit.dispatchCommand(player, "res pset " + residence + " " + targetPlayer + " " + flag + " " + command);

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
		if (onelisttemp.contains("-")) {
		    splited = onelisttemp.split("-")[0];
		    if (splited.toLowerCase().contains(one.getKey().toLowerCase())) {
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

	String name = targetPlayer == null ? "" : targetPlayer + " ";

	name = name + res.getName() + " flags";

	if (name.length() > 32) {
	    name = name.substring(0, Math.min(name.length(), 32));
	}

	Inventory GuiInv = Bukkit.createInventory(null, 54, name);
	int i = 0;

	permMap = (LinkedHashMap<String, Integer>) Sorting.sortByKeyASC(permMap);

	for (Entry<String, Integer> one : permMap.entrySet()) {
	    @SuppressWarnings("deprecation")
	    Material Mat = Material.getMaterial(35);
	    if (Mat == null)
		Mat = Material.STONE;

	    short meta = 6;
	    if (one.getValue() == 1)
		meta = 13;
	    else if (one.getValue() == 0)
		meta = 14;

	    ItemStack MiscInfo = new ItemStack(Mat, 1, (short) meta);
	    ItemMeta MiscInfoMeta = MiscInfo.getItemMeta();
	    MiscInfoMeta.setDisplayName(ChatColor.GREEN + one.getKey());

	    List<String> lore = new ArrayList<String>();

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
