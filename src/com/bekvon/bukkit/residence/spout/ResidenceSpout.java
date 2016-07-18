/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.spout;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.spout.ResidencePopup.PopupType;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.player.SpoutPlayer;

public class ResidenceSpout {
    protected static HashMap<Player, Widget> screens = new HashMap<Player, Widget>();
    Residence plugin;

    public ResidenceSpout(Residence plug) {
	this.plugin = plug;
    }

    public void showResidenceFlagGUI(SpoutPlayer player, String resname, boolean resadmin) {
	ClaimedResidence res = Residence.getResidenceManager().getByName(resname);
	if (res.getPermissions().hasResidencePermission(player, false)) {

	    Color fieldcolor = new Color(0F, 0F, 0.3F, 1F);
	    Color textPrimaryColor = new Color(1F, 1F, 1F, 1F);
	    Color textSecondaryColor = new Color(1F, 1F, 0, 1F);
	    Color hoverColor = new Color(1F, 0, 0, 1F);

	    ResidencePopup popup = new ResidencePopup(PopupType.FLAG_GUI);

	    popup.getMetaData().put("admin", resadmin);

	    popup.gridAttachWidget(plugin, new GenericLabel("Admin: ").setTextColor(textPrimaryColor), 3, 1);
	    popup.gridAttachWidget(plugin, new GenericLabel(Boolean.toString(resadmin)).setTextColor(textSecondaryColor), 4, 1);
	    popup.gridAttachWidget(plugin, new GenericLabel("Residence: ").setTextColor(textPrimaryColor), 0, 0);
	    popup.gridAttachWidget(plugin, new GenericLabel("Flag: ").setTextColor(textPrimaryColor), 0, 1);
	    popup.gridAttachWidget(plugin, new GenericLabel("Player: ").setTextColor(textPrimaryColor), 0, 2);
	    popup.gridAttachWidget(plugin, new GenericLabel("Group: ").setTextColor(textPrimaryColor), 0, 3);
	    popup.gridAttachWidget("ResidenceName", plugin, new GenericLabel(resname).setTextColor(textSecondaryColor), 1, 0);
	    popup.gridAttachWidget(plugin, new GenericLabel("Owner: ").setTextColor(textPrimaryColor), 0, 4);
	    popup.gridAttachWidget(plugin, new GenericLabel("World: ").setTextColor(textPrimaryColor), 0, 5);
	    popup.gridAttachWidget(plugin, new GenericLabel(res.getOwner()).setTextColor(textSecondaryColor), 1, 4);
	    popup.gridAttachWidget(plugin, new GenericLabel(res.getWorld()).setTextColor(textSecondaryColor), 1, 5);

	    GenericTextField flag = new GenericTextField();
	    flag.setTooltip("The name of the flag...");
	    flag.setColor(textSecondaryColor);
	    flag.setFieldColor(fieldcolor);
	    popup.gridAttachWidget("FlagName", plugin, flag, 1, 1);

	    GenericTextField playername = new GenericTextField();
	    playername.setTooltip("The name of the player...");
	    playername.setColor(textSecondaryColor);
	    playername.setFieldColor(fieldcolor);
	    popup.gridAttachWidget("PlayerName", plugin, playername, 1, 2);

	    GenericTextField groupname = new GenericTextField();
	    groupname.setTooltip("The name of the group...");
	    groupname.setColor(textSecondaryColor);
	    groupname.setFieldColor(fieldcolor);
	    popup.gridAttachWidget("GroupName", plugin, groupname, 1, 3);

	    GenericButton truebutton = new GenericButton("SetTrue");
	    truebutton.setTooltip("Set the flag to true.");
	    truebutton.setColor(textSecondaryColor);
	    truebutton.setHoverColor(hoverColor);
	    popup.gridAttachWidget("TrueButton", plugin, truebutton, 2, 1);

	    GenericButton falsebutton = new GenericButton("SetFalse");
	    falsebutton.setTooltip("Set the flag to false.");
	    falsebutton.setColor(textSecondaryColor);
	    falsebutton.setHoverColor(hoverColor);
	    popup.gridAttachWidget("FalseButton", plugin, falsebutton, 2, 2);

	    GenericButton removebutton = new GenericButton("Remove");
	    removebutton.setTooltip("Remove the flag.");
	    removebutton.setColor(textSecondaryColor);
	    removebutton.setHoverColor(hoverColor);
	    popup.gridAttachWidget("RemoveButton", plugin, removebutton, 2, 3);

	    GenericButton removeallbutton = new GenericButton("RemoveAll");
	    removeallbutton.setTooltip("Remove all flags from the player or group...");
	    removeallbutton.setColor(textSecondaryColor);
	    removeallbutton.setHoverColor(hoverColor);
	    popup.gridAttachWidget("RemoveAllButton", plugin, removeallbutton, 3, 3);

	    screens.put(player, popup);
	    player.getMainScreen().attachPopupScreen(popup);
	} else {
	    Residence.msg(player, lm.General_NoPermission);
	}
    }
}
