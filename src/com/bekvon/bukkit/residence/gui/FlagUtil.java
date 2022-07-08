package com.bekvon.bukkit.residence.gui;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.GUI.CMIGui;
import net.Zrips.CMILib.GUI.CMIGuiButton;
import net.Zrips.CMILib.GUI.GUIManager.GUIClickType;
import net.Zrips.CMILib.GUI.GUIManager.GUIRows;
import net.Zrips.CMILib.Items.CMIAsyncHead;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
	this.plugin = plugin;
    }

    public void load() {
	ConfigReader c = null;
	try {
	    c = new ConfigReader(Residence.getInstance(), "flags.yml");
	} catch (Exception e1) {
	    e1.printStackTrace();
	}

	if (c != null) {
	    if (!c.getC().isConfigurationSection("Global.FlagPermission"))
		return;

	    Set<String> allFlags = c.getC().getConfigurationSection("Global.FlagPermission").getKeys(false);

	    for (String oneFlag : allFlags) {
		if (!c.getC().contains("Global.FlagGui." + oneFlag))
		    continue;
		String value = c.get("Global.FlagGui." + oneFlag, "WHITE_WOOL");
		value = value.replace("-", ":");

		CMIAsyncHead ahead = new CMIAsyncHead() {
		    @Override
		    public void afterAsyncUpdate(ItemStack item) {
			Bukkit.getScheduler().runTask(CMI.getInstance(), () -> {
			    flagData.addFlagButton(oneFlag.toLowerCase(), item);
			});
		    }
		};

		CMIItemStack i = CMILib.getInstance().getItemManager().getItem(value, ahead);
		
		if (i == null)
		    i = new CMIItemStack(CMIMaterial.STONE);
		
		ItemStack item = i.getItemStack();
		flagData.addFlagButton(oneFlag.toLowerCase(), item);
	    }
	}
    }

    public void openPsetFlagGui(Player player, String targetPlayer, ClaimedResidence res, boolean resadmin, int page) {
	if (player == null || !player.isOnline())
	    return;

	setFlagInfo flag = new setFlagInfo(res, player, targetPlayer, resadmin);
	flag.recalculate();

	PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);
	openUI(flag, pi, player, Residence.getInstance().msg(lm.Gui_Set_Title, res.getName()));
	return;
    }

    private void openUI(setFlagInfo flag, PageInfo pi, Player player, String title) {

	CMIGui gui = new CMIGui(player);
	gui.setTitle(title);
	gui.setInvSize(GUIRows.r6);

	if (pi.getTotalPages() > 1) {
	    CMIGuiButton forward = new CMIGuiButton(53, CMILib.getInstance().getConfigManager().getGUINextPage()) {
		@Override
		public void click(GUIClickType type) {
		    if (pi.getCurrentPage() == pi.getTotalPages())
			openUI(flag, new PageInfo(45, flag.getButtons().size(), 1), player, title);
		    else
			openUI(flag, new PageInfo(45, flag.getButtons().size(), pi.getCurrentPage() + 1), player, title);
		}
	    };
	    forward.setName(Residence.getInstance().msg(lm.General_nextPageGui));
	    gui.addButton(forward);
	}

	if (pi.getTotalPages() > 1) {
	    CMIGuiButton back = new CMIGuiButton(45, CMILib.getInstance().getConfigManager().getGUIPreviousPage()) {
		@Override
		public void click(GUIClickType type) {

		    if (pi.getCurrentPage() == 1)
			openUI(flag, new PageInfo(45, flag.getButtons().size(), pi.getTotalPages()), player, title);
		    else
			openUI(flag, new PageInfo(45, flag.getButtons().size(), pi.getCurrentPage() - 1), player, title);
		}
	    };
	    back.setName(Residence.getInstance().msg(lm.General_prevPageGui));
	    gui.addButton(back);
	}

	for (CMIGuiButton one : flag.getButtons()) {
	    if (pi.isContinue())
		continue;
	    if (pi.isBreak())
		break;
	    gui.addButton(one);
	}
	gui.fillEmptyButtons();
	gui.open();
    }

    public void openSetFlagGui(Player player, ClaimedResidence res, boolean resadmin, int page) {
	if (player == null || !player.isOnline())
	    return;
	setFlagInfo flag = new setFlagInfo(res, player, resadmin);
	flag.recalculate();
	PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);
	openUI(flag, pi, player, plugin.msg(lm.Gui_Set_Title, res.getName()));
    }

    public FlagData getFlagData() {
	return flagData;
    }
}
