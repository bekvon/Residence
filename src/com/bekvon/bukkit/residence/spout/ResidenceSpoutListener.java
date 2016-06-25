
package com.bekvon.bukkit.residence.spout;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.spout.ResidencePopup.PopupType;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTextField;

public class ResidenceSpoutListener implements Listener {

    protected HashMap<Player, GenericPopup> popups;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onButtonClick(ButtonClickEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player p = event.getPlayer();
	if (event.getScreen() instanceof ResidencePopup) {
	    ResidencePopup screen = (ResidencePopup) event.getScreen();
	    PopupType type = PopupType.valueOf(screen.getPopupType());
	    if (type == PopupType.FLAG_GUI) {
		ResidencePopup popup = screen;
		String flagval = null;
		String flag = null;
		String player = null;
		ClaimedResidence res = null;
		String group = null;
		boolean resadmin = (Boolean) popup.getMetaData().get("admin");
		Button button = event.getButton();
		if (button.getText().equalsIgnoreCase("Close")) {
		    event.getPlayer().getMainScreen().removeWidget(screen);
		    return;
		} else if (button.getText().equalsIgnoreCase("RemoveAll")) {
		    flagval = "removeall";
		} else if (button.getText().equalsIgnoreCase("SetTrue")) {
		    flagval = "true";
		} else if (button.getText().equalsIgnoreCase("SetFalse")) {
		    flagval = "false";
		} else if (button.getText().equalsIgnoreCase("Remove")) {
		    flagval = "remove";
		}
		player = ((GenericTextField) popup.getWidget("PlayerName")).getText();
		group = ((GenericTextField) popup.getWidget("GroupName")).getText();
		flag = ((GenericTextField) popup.getWidget("FlagName")).getText();
		res = Residence.getResidenceManager().getByName(((GenericLabel) popup.getWidget("ResidenceName")).getText());
		if (res == null || flagval == null || flagval.equalsIgnoreCase("") || ((flag == null || flag.equalsIgnoreCase("")) && !flagval.equalsIgnoreCase(
		    "removeall")))
		    return;
		if ((player == null || player.equalsIgnoreCase("")) && (group == null || group.equalsIgnoreCase(""))) {
		    res.getPermissions().setFlag(p, flag, flagval, resadmin);
		} else if (group != null && !group.equalsIgnoreCase("")) {
		    if (flagval.equalsIgnoreCase("removeall"))
			res.getPermissions().removeAllGroupFlags(p, group, resadmin);
		    else
			res.getPermissions().setGroupFlag(p, group, flag, flagval, resadmin);
		} else if (player != null && !player.equalsIgnoreCase("")) {
		    if (flagval.equalsIgnoreCase("removeall"))
			res.getPermissions().removeAllPlayerFlags(p, player, resadmin);
		    else
			res.getPermissions().setPlayerFlag(p, player, flag, flagval, resadmin, true);
		}
	    }
	}
    }
}
