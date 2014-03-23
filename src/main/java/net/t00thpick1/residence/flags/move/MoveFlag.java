package net.t00thpick1.residence.flags.move;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;

public class MoveFlag extends Flag implements Listener {
    private MoveFlag(String flag, FlagType type, Flag parent) {
        super(flag, type, parent);
    }

    public static final MoveFlag FLAG = new MoveFlag(LocaleLoader.getString("Flags.Flags.Move"), FlagType.ANY, null);

    protected static HashMap<String, ClaimedResidence> currentRes;
    protected static HashMap<String, Location> lastOutsideLoc;

    public static void handleNewLocation(Player player, Location loc) {
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

            if (resOld != res && resOld != null) {
                String leaveMessage = resOld.getLeaveMessage();

                if (leaveMessage != null && !leaveMessage.equals("") && resOld != res.getParent()) {
                    player.sendMessage(formatString(leaveMessage, resOld.getName(), player));
                }
            }
            String enterMessage = res.getEnterMessage();

            if (enterMessage != null && !enterMessage.equals("") && !(resOld != null && res == resOld.getParent())) {
                player.sendMessage(formatString(enterMessage, res.getName(), player));
            }
        }
    }

    private static String formatString(String message, String areaName, Player player) {
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
