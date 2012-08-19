/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.spout;
import com.bekvon.bukkit.residence.Residence;
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

/**
 *
 * @author Administrator
 */
public class ResidenceSpout {
    protected static HashMap<Player, Widget> screens = new HashMap<Player, Widget>();

    public ResidenceSpout()
    {

    }

    public static void showResidenceFlagGUI(SpoutPlayer p, Residence plug, String resname, boolean resadmin)
    {
        ClaimedResidence res = Residence.getResidenceManager().getByName(resname);
        if (res.getPermissions().hasResidencePermission(p, false)) {

            Color fieldcolor = new Color(0F,0F,0.3F,1F);
            Color textPrimaryColor = new Color(1F,1F,1F,1F);
            Color textSecondaryColor = new Color(1F,1F,0,1F);
            Color hoverColor = new Color(1F,0,0,1F);
            
            ResidencePopup popup = new ResidencePopup(PopupType.FLAG_GUI);

            popup.getMetaData().put("admin", resadmin);
            
            popup.gridAttachWidget(plug, new GenericLabel("Admin: ").setTextColor(textPrimaryColor), 3, 1);
            popup.gridAttachWidget(plug, new GenericLabel(Boolean.toString(resadmin)).setTextColor(textSecondaryColor), 4, 1);
            popup.gridAttachWidget(plug, new GenericLabel("Residence: ").setTextColor(textPrimaryColor), 0, 0);
            popup.gridAttachWidget(plug, new GenericLabel("Flag: ").setTextColor(textPrimaryColor), 0, 1);
            popup.gridAttachWidget(plug, new GenericLabel("Player: ").setTextColor(textPrimaryColor), 0, 2);
            popup.gridAttachWidget(plug, new GenericLabel("Group: ").setTextColor(textPrimaryColor), 0, 3);
            popup.gridAttachWidget("ResidenceName",plug, new GenericLabel(resname).setTextColor(textSecondaryColor), 1, 0);
            popup.gridAttachWidget(plug, new GenericLabel("Owner: ").setTextColor(textPrimaryColor), 0, 4);
            popup.gridAttachWidget(plug, new GenericLabel("World: ").setTextColor(textPrimaryColor), 0, 5);
            popup.gridAttachWidget(plug, new GenericLabel(res.getOwner()).setTextColor(textSecondaryColor), 1, 4);
            popup.gridAttachWidget(plug, new GenericLabel(res.getWorld()).setTextColor(textSecondaryColor), 1, 5);

            GenericTextField flag = new GenericTextField();
            flag.setTooltip("The name of the flag...");
            flag.setColor(textSecondaryColor);
            flag.setFieldColor(fieldcolor);
            popup.gridAttachWidget("FlagName", plug, flag, 1, 1);

            GenericTextField playername = new GenericTextField();
            playername.setTooltip("The name of the player...");
            playername.setColor(textSecondaryColor);
            playername.setFieldColor(fieldcolor);
            popup.gridAttachWidget("PlayerName",plug, playername,1,2);

            GenericTextField groupname = new GenericTextField();
            groupname.setTooltip("The name of the group...");
            groupname.setColor(textSecondaryColor);
            groupname.setFieldColor(fieldcolor);
            popup.gridAttachWidget("GroupName",plug, groupname,1,3);

            GenericButton truebutton = new GenericButton("SetTrue");
            truebutton.setTooltip("Set the flag to true.");
            truebutton.setColor(textSecondaryColor);
            truebutton.setHoverColor(hoverColor);
            popup.gridAttachWidget("TrueButton",plug, truebutton,2,1);

            GenericButton falsebutton = new GenericButton("SetFalse");
            falsebutton.setTooltip("Set the flag to false.");
            falsebutton.setColor(textSecondaryColor);
            falsebutton.setHoverColor(hoverColor);
            popup.gridAttachWidget("FalseButton",plug, falsebutton,2,2);

            GenericButton removebutton = new GenericButton("Remove");
            removebutton.setTooltip("Remove the flag.");
            removebutton.setColor(textSecondaryColor);
            removebutton.setHoverColor(hoverColor);
            popup.gridAttachWidget("RemoveButton",plug, removebutton,2,3);
            
            GenericButton removeallbutton = new GenericButton("RemoveAll");
            removeallbutton.setTooltip("Remove all flags from the player or group...");
            removeallbutton.setColor(textSecondaryColor);
            removeallbutton.setHoverColor(hoverColor);
            popup.gridAttachWidget("RemoveAllButton",plug, removeallbutton,3,3);

            screens.put(p, popup);
            p.getMainScreen().attachPopupScreen(popup);
        } else {
            p.sendMessage(Residence.getLanguage().getPhrase("NoPermission"));
        }
    }
}
