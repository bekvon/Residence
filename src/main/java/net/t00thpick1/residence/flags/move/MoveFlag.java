package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.PermissionsArea;
import net.t00thpick1.residence.event.ResidenceChangedEvent;
import net.t00thpick1.residence.flags.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class MoveFlag extends Flag implements Listener {
    public static final String FLAG = LocaleLoader.getString("Flags.Flags.Move");

    public boolean allowAction(Player player, PermissionsArea area) {
        return area.allowAction(player, FLAG, super.allowAction(player, area));
    }

    protected static HashMap<String, ClaimedResidence> currentRes;
    protected static HashMap<String, Location> lastOutsideLoc;

    public void handleNewLocation(Player player, Location loc) {
        String pname = player.getName();

        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        if (res != null) {
            if (res.getSubzoneByLoc(loc) != null) {
                res = res.getSubzoneByLoc(loc);
            }
        }

        ClaimedResidence resOld = null;
        if (currentRes.containsKey(pname)) {
            resOld = currentRes.get(pname);
        }
        if (res == null) {
            lastOutsideLoc.put(pname, loc);
            if (resOld != null) {
                String leave = resOld.getLeaveMessage();

                ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(resOld, null, player);
                Residence.getInstance().getServer().getPluginManager().callEvent(chgEvent);

                if (leave != null && !leave.equals("")) {
                    player.sendMessage(formatString(leave, resOld.getName(), player));
                }
                currentRes.remove(pname);
            }
            return;
        }
        lastOutsideLoc.put(pname, loc);
        if (!currentRes.containsKey(pname) || resOld != res) {
            currentRes.put(pname, res);

            ClaimedResidence chgFrom = null;
            if (resOld != res && resOld != null) {
                String leaveMessage = resOld.getLeaveMessage();
                chgFrom = resOld;

                if (leaveMessage != null && !leaveMessage.equals("") && resOld != res.getParent()) {
                    player.sendMessage(formatString(leaveMessage, resOld.getName(), player));
                }
            }
            String enterMessage = res.getEnterMessage();

            ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(chgFrom, res, player);
            Residence.getInstance().getServer().getPluginManager().callEvent(chgEvent);

            if (enterMessage != null && !enterMessage.equals("") && !(resOld != null && res == resOld.getParent())) {
                player.sendMessage(formatString(enterMessage, res.getName(), player));
            }
        }
    }

    private String formatString(String message, String areaName, Player player) {
        return ChatColor.translateAlternateColorCodes('&', message.replaceAll("(%player%)", player.getName()).replaceAll("(%area%)", areaName));
    }

    public ClaimedResidence getCurrentResidence(String player) {
        return currentRes.get(player);
    }

    public static void initialize() {
        currentRes = new HashMap<String, ClaimedResidence>();
        lastOutsideLoc = new HashMap<String, Location>();
        TPFlag.initialize();
        StateAssurance.initialize();
        PlayerMovement.initialize();
        VehicleMoveFlag.initialize();
    }

    public static void clean() {
        currentRes = null;
        lastOutsideLoc = null;
    }
}
