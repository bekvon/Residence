package net.t00thpick1.residence;

import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.ResidenceManager;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.yaml.YAMLGroupManager;
import net.t00thpick1.residence.selection.SelectionManager;
import net.t00thpick1.residence.utils.Utilities;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ResidenceCommandExecutor implements CommandExecutor {
    private Residence plugin;
    private ResidenceManager rmanager = ResidenceAPI.getResidenceManager();
    private Map<String, String> deleteConfirm;

    public ResidenceCommandExecutor(Residence residence) {
        deleteConfirm = new HashMap<String, String>();
        plugin = residence;
        residence.getServer().getPluginCommand("residence").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean resadmin = false;
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
            if (label.equalsIgnoreCase("resadmin")) {
                if (player.hasPermission("residence.admin")) {
                    resadmin = true;
                } else {
                    sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                    return true;
                }
            }
        } else {
            resadmin = true;
        }

        if (args.length == 0) {
            args = new String[] { "?", "1" };
        }

        if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?")) {
            return commandHelp(args, resadmin, sender);
        }

        if (args.length > 1 && args[args.length - 2].equalsIgnoreCase("?")) {
            return commandHelp(args, resadmin, sender);
        }

        String cmd = args[0].toLowerCase();
        if (cmd.equalsIgnoreCase("remove") || cmd.equalsIgnoreCase("delete")) {
            if (!sender.hasPermission("residence.commands.remove")) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResRemove(args, resadmin, sender);
        }
        if (cmd.equalsIgnoreCase("confirm")) {
            if (!sender.hasPermission("residence.commands.remove")) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResConfirm(args, resadmin, sender);
        }
        if (cmd.equalsIgnoreCase("version")) {
            sender.sendMessage(ChatColor.GRAY + "------------------------------------");
            sender.sendMessage(ChatColor.RED + "This server running " + ChatColor.GOLD + "Residence" + ChatColor.RED + " version: " + ChatColor.BLUE + plugin.getDescription().getVersion());
            String names = null;
            List<String> authlist = plugin.getDescription().getAuthors();
            for (String auth : authlist) {
                if (names == null) {
                    names = auth;
                } else {
                    names = names + ", " + auth;
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Authors: " + ChatColor.YELLOW + names);
            sender.sendMessage(ChatColor.AQUA + "Visit the BukkitDev page at:");
            sender.sendMessage(ChatColor.BLUE + "http://dev.bukkit.org/server-mods/Residence");
            sender.sendMessage(ChatColor.GRAY + "------------------------------------");
            return true;
        }
        if (cmd.equalsIgnoreCase("setowner") && args.length == 3) {
            if (!player.hasPermission("residence.commands.setowner")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (!resadmin) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea area = rmanager.getByName(args[1]);
            if (area != null) {
                area.setOwner(args[2]);
                if (area.getParent() == null) {
                    sender.sendMessage(LocaleLoader.getString("Commands.SetOwner.ResidenceOwnerChange", args[1], args[2]));
                } else {
                    sender.sendMessage(LocaleLoader.getString("Commands.SetOwner.SubzoneOwnerChange", area.getName(), args[2]));
                }
            } else {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
            }
            return true;
        }
        if (player == null) {
            return true;
        }
        if (label.equalsIgnoreCase("resadmin")) {
            if (args.length == 1 && args[0].equals("on")) {
                plugin.activateAdminMode(player);
                player.sendMessage(LocaleLoader.getString("Commands.AdminToggle.On"));
                return true;
            } else if (args.length == 1 && args[0].equals("off")) {
                plugin.deactivateAdminMode(player);
                player.sendMessage(LocaleLoader.getString("Commands.AdminToggle.Off"));
                return true;
            }
        }
        if (cmd.equalsIgnoreCase("select")) {
            if (!player.hasPermission("residence.commands.select")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSelect(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("create")) {
            if (!player.hasPermission("residence.commands.create")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResCreate(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("subzone") || cmd.equalsIgnoreCase("sz")) {
            if (!player.hasPermission("residence.commands.subzone")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSubzone(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("gui")) {
            if (!player.hasPermission("residence.commands.gui")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResGui(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("sublist")) {
            if (!player.hasPermission("residence.commands.sublist")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSublist(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("compass")) {
            if (!player.hasPermission("residence.commands.compass")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResCompass(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("info")) {
            if (!player.hasPermission("residence.commands.info")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 1) {
                ResidenceArea area = ResidenceAPI.getResidenceManager().getByLocation(player.getLocation());
                if (area != null) {
                    printInformation(player, area);
                } else {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                }
                return true;
            } else if (args.length == 2) {
                ResidenceArea area = ResidenceAPI.getResidenceManager().getByName(args[1]);
                if (area != null) {
                    printInformation(player, area);
                } else {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                }
                return true;
            }
            return false;
        }
        if (cmd.equalsIgnoreCase("current")) {
            if (!player.hasPermission("residence.commands.current")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length != 1) {
                return false;
            }
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Current.InResidence", res.getName()));
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("set")) {
            if (!player.hasPermission("residence.commands.set")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSet(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("pset")) {
            if (!player.hasPermission("residence.commands.pset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResPset(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("rset")) {
            if (!player.hasPermission("residence.commands.rset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResRset(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("rentlink")) {
            if (!player.hasPermission("residence.commands.rentlink")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length < 2) {
                return false;
            }
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            ResidenceArea link = rmanager.getByName(args[1]);
            if (link == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !res.getOwner().equals(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.rentLink(link);
            player.sendMessage(LocaleLoader.getString("Commands.RentLink.Success", res.getName(), link.getName()));
            return true;
        }
        if (cmd.equalsIgnoreCase("rentlinks")) {
            if (!player.hasPermission("residence.commands.rentlinks")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            printRentLinks(player, res);

            return true;
        }
        if (cmd.equalsIgnoreCase("rename")) {
            if (!player.hasPermission("residence.commands.rename")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 3) {
                String oldName = args[1];
                String newName = args[2];
                ResidenceArea res = rmanager.getByName(oldName);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", oldName));
                    return true;
                }
                if (!resadmin && !res.getOwner().equals(player.getName())) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                    return true;
                }
                if (rmanager.getByName(newName) != null) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Create.AlreadyExists", newName));
                    return true;
                }
                if (!Utilities.validName(newName)) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Create.InvalidName", newName));
                    return true;
                }
                if (res.rename(newName)) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Rename.Success", oldName, newName));
                } else {
                    sender.sendMessage(LocaleLoader.getString("Commands.Rename.Failure"));
                }
                return true;
            }
            return false;
        }
        if (cmd.equalsIgnoreCase("unstuck")) {
            if (!player.hasPermission("residence.commands.unstuck")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length != 1) {
                return false;
            }
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Unstuck.Moved"));
                player.teleport(res.getOutsideFreeLoc(player.getLocation()));
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("mirror")) {
            if (!player.hasPermission("residence.commands.mirror")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length != 3) {
                return false;
            }
            ResidenceArea mirror = rmanager.getByName(args[2]);
            ResidenceArea mirrorTo = rmanager.getByName(args[1]);
            if (mirror == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (mirrorTo == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !mirrorTo.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            mirrorTo.copyFlags(mirror);
            return true;
        }
        if (cmd.equalsIgnoreCase("default")) {
            if (!player.hasPermission("residence.commands.default")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea res = null;
            if (args.length == 1) {
                res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            } else if (args.length == 2) {
                res = rmanager.getByName(args[1]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
            }
            if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.clearFlags();
            res.applyDefaultFlags();
            player.sendMessage(LocaleLoader.getString("Commands.Default.Success"));
            return true;
        }
        if (cmd.equalsIgnoreCase("flags")) {
            if (!player.hasPermission("residence.commands.flags")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            int page = 0;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page <= 0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[1]));
                    return true;
                }
                page--;
            }
            Flag[] flags = FlagManager.getFlags().toArray(new Flag[0]);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.Flags", page + 1));
            for (int i = 0; i < 8; i++) {
                int index = (page * 8) + i;
                if (index < flags.length) {
                    Flag flag = flags[index];
                    player.sendMessage(LocaleLoader.getString("Commands.Flags.Flag", flag.getName(), flag.getType()));
                }
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("limits")) {
            return commandResLimits(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("list")) {
            if (!player.hasPermission("residence.commands.list")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            int page = 1;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page <= 0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[1]));
                    return true;
                }
            }
            ResidenceArea[] reses = rmanager.getOwnedResidences(player.getName()).toArray(new ResidenceArea[0]);
            player.sendMessage(LocaleLoader.getString("Commands.List.List", page));
            for (int i = 0; i < 8; i++) {
                int index = (8 * (page - 1)) + i;
                if (reses.length > index) {
                    player.sendMessage(LocaleLoader.getString("Commands.List.Residence", reses[i].getName()));
                }
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("listall")) {
            if (!player.hasPermission("residence.commands.listall")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            int page = 1;
            if (args.length == 2) {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page <= 0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[1]));
                    return true;
                }
            }
            ResidenceArea[] reses = rmanager.getResidencesInWorld(player.getWorld()).toArray(new ResidenceArea[0]);
            player.sendMessage(LocaleLoader.getString("Commands.List.List", page));
            for (int i = 0; i < 8; i++) {
                int index = (8 * (page - 1)) + i;
                if (reses.length > index) {
                    player.sendMessage(LocaleLoader.getString("Commands.List.Residence", reses[index].getName()));
                }
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("tpset")) {
            if (!player.hasPermission("residence.commands.tpset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.setTeleportLocation(player.getLocation());
            return true;
        }
        if (cmd.equalsIgnoreCase("tp")) {
            if (!player.hasPermission("residence.commands.tp")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length != 2) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            player.teleport(res.getTeleportLocation());
            return true;
        }
        if (cmd.equalsIgnoreCase("market")) {
            if (!player.hasPermission("residence.commands.market")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResMarket(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("message")) {
            if (!player.hasPermission("residence.commands.message")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResMessage(args, resadmin, player);
        }
        if (cmd.equalsIgnoreCase("give") && args.length == 3) {
            if (!player.hasPermission("residence.commands.give")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            String newOwner = args[2];
            if (!resadmin || !res.getOwner().equals(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.setOwner(newOwner);
            return true;
        }
        if (cmd.equalsIgnoreCase("server")) {
            if (!player.hasPermission("residence.commands.server")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 2) {
                ResidenceArea res = rmanager.getByName(args[1]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
                res.setOwner(null);
                player.sendMessage(LocaleLoader.getString("Commands.SetOwner.ResidenceOwnerChange", args[1], "Server Land"));
                return true;
            } else {
                return false;
            }
        }
        if (cmd.equalsIgnoreCase("clearflags")) {
            if (!player.hasPermission("residence.commands.clearflags")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ResidenceArea area = null;
            if (args.length == 0) {
                area = rmanager.getByLocation(player.getLocation());
                if (area == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            } else {
                area = rmanager.getByName(args[1]);
                if (area == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
            }
            if (!resadmin && !area.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            area.clearFlags();
            player.sendMessage(LocaleLoader.getString("FlagsCleared"));
            return true;
        }
        if (cmd.equalsIgnoreCase("tool")) {
            if (!player.hasPermission("residence.commands.tool")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            player.sendMessage(LocaleLoader.getString("Commands.Tool.SelectionTool", ConfigManager.getInstance().getSelectionToolType()));
            player.sendMessage(LocaleLoader.getString("Commands.Tool.InfoTool", ConfigManager.getInstance().getInfoToolType()));
            return true;
        }
        return false;
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender) {
        HelpManager.help(sender, args);
        return true;
    }

    private boolean commandResSelect(String[] args, boolean resadmin, Player player) {
        SelectionManager selectionManager = Residence.getInstance().getSelectionManager();
        if (!resadmin && !player.hasPermission("residence.commands.select")) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        if (args.length == 2) {
            if (args[1].equals("size") || args[1].equals("cost")) {
                if (selectionManager.hasPlacedBoth(player)) {
                    selectionManager.showSelectionInfo(player);
                    return true;
                }
            } else if (args[1].equals("vert")) {
                selectionManager.vert(player, resadmin);
                return true;
            } else if (args[1].equals("sky")) {
                selectionManager.sky(player, resadmin);
                return true;
            } else if (args[1].equals("bedrock")) {
                selectionManager.bedrock(player, resadmin);
                return true;
            } else if (args[1].equals("coords")) {
                Location playerLoc1 = selectionManager.getPlayerLoc1(player);
                if (playerLoc1 != null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Select.PrimaryPoint", playerLoc1.getBlockX(), playerLoc1.getBlockY(), playerLoc1.getBlockZ()));
                }
                Location playerLoc2 = selectionManager.getPlayerLoc2(player);
                if (playerLoc2 != null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Select.SecondaryPoint", playerLoc2.getBlockX(), playerLoc2.getBlockY(), playerLoc2.getBlockZ()));
                }
                return true;
            } else if (args[1].equals("chunk")) {
                selectionManager.selectChunk(player);
                return true;
            } else if (args[1].equals("worldedit")) {
                if (selectionManager.worldEdit(player)) {
                    player.sendMessage(LocaleLoader.getString("Commands.Select.WorldEdit"));
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[1].equals("expand")) {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[2]));
                    return true;
                }
                selectionManager.modify(player, false, amount);
                return true;
            } else if (args[1].equals("shift")) {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[2]));
                    return true;
                }
                selectionManager.modify(player, true, amount);
                return true;
            }
        }
        if (args.length > 1 && args[1].equals("residence")) {
            String resName;
            ResidenceArea res = null;
            if (args.length > 2) {
                res = rmanager.getByName(args[2]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                    return true;
                }
            } else {
                res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            }
            resName = res.getName();
            selectionManager.placeLoc1(player, res.getHighLocation());
            selectionManager.placeLoc2(player, res.getLowLocation());
            player.sendMessage(LocaleLoader.getString("Commands.Select.SelectResidence", resName));
            return true;
        } else {
            try {
                selectionManager.selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                return true;
            } catch (Exception ex) {
                player.sendMessage(LocaleLoader.getString("Commands.Select.SelectionFail"));
                return true;
            }
        }
    }

    private boolean commandResCreate(String[] args, boolean resadmin, Player player) {
        if (args.length != 2) {
            return false;
        }
        SelectionManager selectionManager = Residence.getInstance().getSelectionManager();
        if (!selectionManager.hasPlacedBoth(player)) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.SelectPoints"));
            return true;
        }
        if (rmanager.getByName(args[1]) != null) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.AlreadyExists", args[1]));
            return true;
        }
        CuboidArea newArea = ResidenceAPI.createCuboidArea(selectionManager.getPlayerLoc1(player), selectionManager.getPlayerLoc2(player));
        if (!resadmin) {
            if (newArea.getXSize() > YAMLGroupManager.getMaxWidth(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooWide", YAMLGroupManager.getMaxWidth(player)));
                return true;
            }
            if (newArea.getXSize() < YAMLGroupManager.getMinWidth(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooNarrow", YAMLGroupManager.getMinWidth(player)));
                return true;
            }
            if (newArea.getZSize() > YAMLGroupManager.getMaxLength(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooLong", YAMLGroupManager.getMaxLength(player)));
                return true;
            }
            if (newArea.getZSize() < YAMLGroupManager.getMinLength(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooSkinny", YAMLGroupManager.getMinLength(player)));
                return true;
            }
            if (newArea.getYSize() > YAMLGroupManager.getMaxHeight(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooTall", YAMLGroupManager.getMaxHeight(player)));
                return true;
            }
            if (newArea.getYSize() < YAMLGroupManager.getMinHeight(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooShort", YAMLGroupManager.getMinHeight(player)));
                return true;
            }
            if (newArea.getLowLocation().getBlockY() < YAMLGroupManager.getMinY(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooLow", YAMLGroupManager.getMinY(player)));
                return true;
            }
            if (newArea.getHighLocation().getBlockY() > YAMLGroupManager.getMaxY(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooHigh", YAMLGroupManager.getMaxY(player)));
                return true;
            }
        }
        if (!resadmin && YAMLGroupManager.getMaxResidences(player.getName()) <= rmanager.getOwnedZoneCount(player.getName())) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.TooManyResidences", YAMLGroupManager.getMaxResidences(player.getName())));
            return true;
        }
        if (!Utilities.validName(args[1])) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.InvalidName", args[1]));
            return true;
        }
        rmanager.createResidence(args[1], player.getName(), newArea);
        player.sendMessage(LocaleLoader.getString("Commands.Create.Success", args[1]));
        return true;
    }

    private boolean commandResLimits(String[] args, boolean resadmin, Player player) {
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Residences", YAMLGroupManager.getMaxResidences(player.getName())));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Wide", YAMLGroupManager.getMaxWidth(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Narrow", YAMLGroupManager.getMinWidth(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Long", YAMLGroupManager.getMaxLength(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Skinny", YAMLGroupManager.getMinLength(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Tall", YAMLGroupManager.getMaxHeight(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Short", YAMLGroupManager.getMinHeight(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.Low", YAMLGroupManager.getMinY(player)));
        player.sendMessage(LocaleLoader.getString("Commands.Limits.High", YAMLGroupManager.getMaxY(player)));
        return true;
    }

    private boolean commandResSubzone(String[] args, boolean resadmin, Player player) {
        if (args.length != 2 && args.length != 3) {
            return false;
        }
        SelectionManager selectionManager = Residence.getInstance().getSelectionManager();
        if (!selectionManager.hasPlacedBoth(player)) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.SelectPoints"));
            return true;
        }
        String zname;
        ResidenceArea res;
        if (args.length == 2) {
            res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            zname = args[1];
        } else {
            res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            zname = args[2];
        }
        if (res.getSubzoneByName(zname) != null) {
            player.sendMessage(LocaleLoader.getString("Commands.Subzone.AlreadyExists", zname));
            return true;
        }
        if (!Utilities.validName(zname)) {
            player.sendMessage(LocaleLoader.getString("Commands.Subzone.InvalidName", zname));
            return true;
        }
        CuboidArea newArea = ResidenceAPI.createCuboidArea(selectionManager.getPlayerLoc1(player), selectionManager.getPlayerLoc2(player));
        if (!res.isAreaWithin(newArea)) {
            player.sendMessage(LocaleLoader.getString("Commands.Subzone.MustBeInside"));
            return true;
        }
        for (ResidenceArea subzone : res.getSubzoneList()) {
            if (subzone.checkCollision(newArea)) {
                player.sendMessage(LocaleLoader.getString("Commands.Subzone.Collide", subzone.getName()));
                return true;
            }
        }
        res.createSubzone(zname, player.getName(), newArea);
        return true;
    }

    private boolean commandResRemove(String[] args, boolean resadmin, CommandSender sender) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (args.length == 1) {
                ResidenceArea res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
                if (!resadmin && !res.getOwner().equals(player.getName())) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                    return true;
                }
                if (!res.getFullName().equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
                    if (res.getParent() != null) {
                        player.sendMessage(LocaleLoader.getString("Commands.Delete.SubzoneConfirm", res.getName()));
                    } else {
                        player.sendMessage(LocaleLoader.getString("Commands.Delete.Confirm", res.getName()));
                    }
                    deleteConfirm.put(player.getName(), res.getFullName());
                }
                return true;
            }
        }
        if (args.length != 2) {
            return false;
        }
        ResidenceArea res = rmanager.getByName(args[1]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
            return true;
        }
        if (!resadmin && !res.getOwner().equals(player.getName())) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        if (!res.getFullName().equalsIgnoreCase(deleteConfirm.get(sender.getName()))) {
            if (res.getParent() != null) {
                player.sendMessage(LocaleLoader.getString("Commands.Delete.SubzoneConfirm", res.getName()));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Delete.Confirm", res.getName()));
            }
            deleteConfirm.put(sender.getName(), res.getFullName());
        }
        return true;
    }

    private boolean commandResConfirm(String[] args, boolean resadmin, CommandSender sender) {
        if (args.length == 1) {
            String area = deleteConfirm.get(sender.getName());
            ResidenceArea res = rmanager.getByName(area);
            if (res == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", area));
                return true;
            }
            if (!resadmin && !res.getOwner().equals(((Player) sender).getName())) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (area == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", area));
                return true;
            }
            rmanager.remove(res);
            deleteConfirm.remove(sender.getName());
            if (res.getParent() == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.Delete.Removed", res.getName()));
            } else {
                sender.sendMessage(LocaleLoader.getString("Commands.Delete.SubzoneRemoved", res.getName()));
            }
        }
        return true;
    }

    private boolean commandResSet(String[] args, boolean resadmin, Player player) {
        if (args.length < 3 || args.length > 4) {
            return false;
        }
        ResidenceArea res = null;
        String flagName = null;
        String boolName = null;
        if (args.length == 3) {
            res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            flagName = args[1];
            boolName = args[2];
        } else if (args.length == 4) {
            res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            flagName = args[2];
            boolName = args[3];
        }
        if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        Flag flag = FlagManager.getFlag(flagName);
        if (flag == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
            return true;
        }
        if (!resadmin
                && !player.hasPermission("residence.flags.all")
                && !player.hasPermission("residence.flags." + flag.getName())) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        Boolean value;
        if (boolName.equalsIgnoreCase("true") || boolName.equalsIgnoreCase("t")) {
            value = true;
        } else if (boolName.equalsIgnoreCase("false") || boolName.equalsIgnoreCase("f")) {
            value = false;
        } else if (boolName.equalsIgnoreCase("remove") || boolName.equalsIgnoreCase("r")) {
            value = null;
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", boolName));
            return true;
        }
        res.setAreaFlag(flag, value);
        player.sendMessage(LocaleLoader.getString("Commands.Flags.FlagSet", flag.getName(), value));
        return true;
    }

    private boolean commandResPset(String[] args, boolean resadmin, Player player) {
        if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.removeAllPlayerFlags(args[1]);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagsRemoved", args[1]));
            return true;
        } else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.removeAllPlayerFlags(args[2]);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagsRemoved", args[2]));
            return true;
        } else {
            ResidenceArea res = null;
            String playerName = null;
            String flagName = null;
            String boolName = null;
            if (args.length == 4) {
                res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
                playerName = args[1];
                flagName = args[2];
                boolName = args[3];
            } else if (args.length == 5) {
                res = rmanager.getByName(args[1]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
                playerName = args[2];
                flagName = args[3];
                boolName = args[4];
            } else {
                return false;
            }
            if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            Flag flag = FlagManager.getFlag(flagName);
            if (flag == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
                return true;
            }
            if (!resadmin
                    && !player.hasPermission("residence.flags.all")
                    && !player.hasPermission("residence.flags." + flag.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            Boolean value;
            if (boolName.equalsIgnoreCase("true") || boolName.equalsIgnoreCase("t")) {
                value = true;
            } else if (boolName.equalsIgnoreCase("false") || boolName.equalsIgnoreCase("f")) {
                value = false;
            } else if (boolName.equalsIgnoreCase("remove") || boolName.equalsIgnoreCase("r")) {
                value = null;
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", boolName));
                return true;
            }
            res.setPlayerFlag(playerName, flag, value);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagSet", playerName, flag.getName(), value));
            return true;
        }
    }

    private boolean commandResRset(String[] args, boolean resadmin, Player player) {
        if (args.length < 3 || args.length > 4) {
            return false;
        }
        ResidenceArea res = null;
        String flagName = null;
        String boolName = null;
        if (args.length == 3) {
            res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            flagName = args[1];
            boolName = args[2];
        } else if (args.length == 4) {
            res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            flagName = args[2];
            boolName = args[3];
        }
        if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        Flag flag = FlagManager.getFlag(flagName);
        if (flag == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
            return true;
        }
        if (!resadmin
                && !player.hasPermission("residence.flags.all")
                && !player.hasPermission("residence.flags." + flag.getName())) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        Boolean value;
        if (boolName.equalsIgnoreCase("true") || boolName.equalsIgnoreCase("t")) {
            value = true;
        } else if (boolName.equalsIgnoreCase("false") || boolName.equalsIgnoreCase("f")) {
            value = false;
        } else if (boolName.equalsIgnoreCase("remove") || boolName.equalsIgnoreCase("r")) {
            value = null;
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", boolName));
            return true;
        }
        res.setRentFlag(flag, value);
        player.sendMessage(LocaleLoader.getString("Commands.Flags.RentFlagSet", flag.getName(), value));
        return true;
    }

    private boolean commandResMarket(String[] args, boolean resadmin, Player player) {
        if (args.length == 1) {
            return false;
        }
        String command = args[1].toLowerCase();
        if (command.equalsIgnoreCase("list")) {
            return commandResMarketList(args, resadmin, player);
        }
        if (command.equalsIgnoreCase("autorenew")) {
            return commandResMarketAutorenew(args, resadmin, player);
        }
        if (command.equalsIgnoreCase("rentable")) {
            return commandResMarketRentable(args, resadmin, player);
        }
        if (command.equalsIgnoreCase("rent")) {
            return commandResMarketRent(args, resadmin, player);
        }
        if (command.equalsIgnoreCase("release")) {
            if (args.length != 3) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isForRent() && (resadmin || res.getOwner().equals(player.getName()))) {
                res.removeFromMarket();
                player.sendMessage(LocaleLoader.getString("Commands.Market.Release.NoLongerForRent"));
                if (res.isRented()) {
                    player.sendMessage(LocaleLoader.getString("Commands.Market.Release.CurrentRenter", res.getRenter()));
                }
                return true;
            }
            return true;
        }
        if (command.equalsIgnoreCase("unrent")) {
            if (args.length != 3) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isRented() && (resadmin || res.getRenter().equals(player.getName()))) {
                res.evict();
                player.sendMessage(LocaleLoader.getString("Commands.Market.Release.NoLongerRenting"));
                return true;
            }
            return true;
        }
        if (command.equalsIgnoreCase("info")) {
            ResidenceArea res = null;
            if (args.length == 2) {
                res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            } else if (args.length == 3) {
                res = rmanager.getByName(args[2]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                    return true;
                }
            } else {
                return false;
            }
            printMarketInfo(player, res);
            return true;
        }
        if (command.equalsIgnoreCase("buy")) {
            if (args.length != 3) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            int cost = res.getCost();
            if (!res.buy(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Buy.TooPoor", cost));
                return true;
            }
            player.sendMessage(LocaleLoader.getString("Commands.Market.Buy.Success", cost));
            return true;
        }
        if (command.equalsIgnoreCase("unsell")) {
            if (args.length != 3) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (!res.isForSale()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Unsell.NotForSale"));
                return true;
            }
            if (!resadmin && !res.getOwner().equals(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.removeFromMarket();
            return true;
        }
        if (command.equalsIgnoreCase("sell")) {
            if (args.length != 4) {
                return false;
            }
            ResidenceArea res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isForSale()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Sell.AlreadyForSale"));
                return true;
            }
            if (!resadmin && !res.getOwner().equals(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (Exception ex) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidAmount", args[3]));
                return true;
            }
            res.setForSale(amount);
            player.sendMessage(LocaleLoader.getString("Commands.Market.Sell.Success", res.getName(), amount));
            return true;
        }
        return false;
    }

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player) {
        if (args.length < 3 || args.length > 4) {
            return false;
        }
        ResidenceArea res = rmanager.getByName(args[2]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
            return true;
        }
        if (!res.isForRent()) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.NotForRent"));
            return true;
        }
        if (res.isRented()) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.AlreadyRented"));
            return true;
        }
        boolean repeat = false;
        if (args.length == 4) {
            if (args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("true")) {
                repeat = true;
            } else if (!args[3].equalsIgnoreCase("f") && !args[3].equalsIgnoreCase("false")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", args[3]));
                return true;
            }
            if (repeat && !res.isAutoRenewEnabled()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.AutoRenewDisabled"));
            }
        }
        if (!res.rent(player.getName(), repeat)) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.TooPoor", res.getCost()));
            return true;
        }
        player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.Success", res.getName(), res.getCost(), Utilities.toDayString(res.getRentPeriod())));
        return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
        if (args.length < 5 || args.length > 6) {
            return false;
        }
        if (!ConfigManager.getInstance().isRent()) {
            return true;
        }
        int days;
        int cost;
        ResidenceArea res = rmanager.getByName(args[2]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
            return true;
        }
        try {
            cost = Integer.parseInt(args[3]);
        } catch (Exception ex) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[3]));
            return true;
        }
        try {
            days = Integer.parseInt(args[4]);
        } catch (Exception ex) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[4]));
            return true;
        }

        boolean autoRenew = res.isAutoRenewEnabled();
        if (args.length == 6) {
            if (args[5].equalsIgnoreCase("true") || args[5].equalsIgnoreCase("t")) {
                autoRenew = true;
            } else if (args[5].equalsIgnoreCase("false") || args[5].equalsIgnoreCase("f")) {
                autoRenew = false;
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", args[5]));
                return true;
            }
        }
        res.setForRent(cost, days * 24 * 60 * 60 * 1000, autoRenew);
        player.sendMessage(LocaleLoader.getString("Commands.Market.Rentable.Success", res.getName(), cost, days));
        return true;
    }

    private boolean commandResMarketAutorenew(String[] args, boolean resadmin, Player player) {
        if (!ConfigManager.getInstance().isEconomy()) {
            return true;
        }
        if (args.length != 4) {
            return false;
        }
        ResidenceArea res = rmanager.getByName(args[2]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
            return true;
        }
        boolean value;
        if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t")) {
            value = true;
        } else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")) {
            value = false;
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean", args[3]));
            return true;
        }
        if (res.isRented()) {
            if (resadmin || res.getRenter().equals(player.getName())) {
                res.setAutoRenew(value);
                player.sendMessage(LocaleLoader.getString("Commands.Market.AutoRenew.Renter.Success", value));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            }
        } else if (res.isForRent()) {
            if (resadmin || res.getOwner().equals(player.getName())) {
                res.setAutoRenewEnabled(value);
                player.sendMessage(LocaleLoader.getString("Commands.Market.AutoRenew.Owner.Success", value));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            }
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.NotForRent"));
        }
        return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player) {
        if (!ConfigManager.getInstance().isEconomy()) {
            return true;
        }
        player.sendMessage(LocaleLoader.getString("Commands.Market.MarketList"));
        printForSaleResidences(player);
        if (ConfigManager.getInstance().isRent()) {
            printRentableResidences(player);
        }
        return true;
    }

    private boolean commandResMessage(String[] args, boolean resadmin, Player player) {
        if (args.length < 2) {
            return false;
        }
        if (args[1].equals("enter")) {
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            return commandResMessageEnter(args, resadmin, player, 2, res);
        } else if (args[1].equals("leave")) {
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            return commandResMessageLeave(args, resadmin, player, 2, res);
        } else if (args[1].equals("remove")) {
            ResidenceArea res = rmanager.getByLocation(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (args.length > 2 && args[2].equals("enter")) {
                return commandResMessageEnter(args, resadmin, player, -1, res);
            } else if (args.length > 2 && args[2].equals("leave")) {
                return commandResMessageLeave(args, resadmin, player, -1, res);
            }
            player.sendMessage(LocaleLoader.getString("Commands.Message.InvalidMessageType"));
            return true;
        } else if (args.length > 2 && args[2].equals("enter")) {
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            return commandResMessageEnter(args, resadmin, player, 3, res);
        } else if (args.length > 2 && args[2].equals("leave")) {
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            return commandResMessageLeave(args, resadmin, player, 3, res);
        } else if (args.length > 2 && args[2].equals("remove")) {
            ResidenceArea res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (args.length != 4) {
                return false;
            }
            if (args[3].equals("enter")) {
                return commandResMessageEnter(args, resadmin, player, -1, res);
            } else if (args[3].equals("leave")) {
                return commandResMessageLeave(args, resadmin, player, -1, res);
            }
            player.sendMessage(LocaleLoader.getString("Commands.Message.InvalidMessageType"));
            return true;
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Message.InvalidMessageType"));
            return true;
        }
    }

    private boolean commandResMessageLeave(String[] args, boolean resadmin, Player player, int start, ResidenceArea res) {
        if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        if (start == -1) {
            res.setLeaveMessage(null);
            return true;
        }
        StringBuilder message = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            message.append(args[i]);
            message.append(" ");
        }
        res.setLeaveMessage(message.toString());
        return true;
    }

    private boolean commandResMessageEnter(String[] args, boolean resadmin, Player player, int start, ResidenceArea res) {
        if (!resadmin && !res.allowAction(player.getName(), FlagManager.ADMIN)) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        if (start == -1) {
            res.setEnterMessage(null);
            return true;
        }
        StringBuilder message = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            message.append(args[i]);
            message.append(" ");
        }
        res.setEnterMessage(message.toString());
        return true;
    }

    private boolean commandResSublist(String[] args, boolean resadmin, Player player) {
        if (args.length > 0 && args.length < 4) {
            ResidenceArea res;
            if (args.length == 1) {
                res = rmanager.getByLocation(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            } else {
                res = rmanager.getByName(args[1]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
            }
            int page = 0;
            if (args.length == 3) {
                try {
                    page = Integer.parseInt(args[2]);
                    if (page <= 0) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber", args[2]));
                    return true;
                }
            }
            String[] subzones = res.getSubzoneNameList().toArray(new String[0]);
            player.sendMessage(LocaleLoader.getString("Commands.Sublist.List", page));
            for (int i = 0; i < 8; i++) {
                int index = ((page - 1) * 8) + i;
                if (index < subzones.length) {
                    player.sendMessage(LocaleLoader.getString("Commands.Sublist.Subzone", subzones[index]));
                }
            }
            return true;
        }
        return false;
    }

    private boolean commandResCompass(String[] args, boolean resadmin, Player player) {
        if (args.length != 2) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage(LocaleLoader.getString("Commands.CompassTarget.Reset"));
            return true;
        }
        ResidenceArea res = rmanager.getByName(args[1]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
            return true;
        }
        if (res.getWorld().equals(player.getWorld())) {
            player.setCompassTarget(res.getCenter());
            player.sendMessage(LocaleLoader.getString("Commands.CompassTarget.Set", args[1]));
        }
        return true;
    }

    private boolean commandResGui(String[] args, boolean resadmin, Player player) {
        // TODO
        return true;
    }

    public static void printInformation(Player player, ResidenceArea res) {
        player.sendMessage(LocaleLoader.getString("Info.Residence", res.getName()));
        if (res.getParent() != null) {
            player.sendMessage(LocaleLoader.getString("Info.FullName", res.getFullName()));
        }
        player.sendMessage(LocaleLoader.getString("Info.Owner", res.getOwner()));
        if (res.isRented()) {
            player.sendMessage(LocaleLoader.getString("Info.Renter", res.getRenter()));
        }
        StringBuilder builder = new StringBuilder();
        Map<Flag, Boolean> flags = res.getAreaFlags();
        for (Entry<Flag, Boolean> flag : flags.entrySet()) {
            builder.append(" ");
            builder.append(flag.getValue() ? "+" : "-");
            builder.append(flag.getKey().getName());
        }
        if (builder.length() == 0) {
            builder.append(LocaleLoader.getString("Info.None"));
        }
        player.sendMessage(LocaleLoader.getString("Info.AreaFlags", builder.toString()));
        builder = new StringBuilder();
        flags = res.getPlayerFlags(player.getName());
        for (Entry<Flag, Boolean> flag : flags.entrySet()) {
            builder.append(" ");
            builder.append(flag.getValue() ? "+" : "-");
            builder.append(flag.getKey().getName());
        }
        if (builder.length() == 0) {
            builder.append(LocaleLoader.getString("Info.None"));
        }
        player.sendMessage(LocaleLoader.getString("Info.YourFlags", builder.toString()));
        builder = new StringBuilder();
        Map<String, Map<Flag, Boolean>> apFlags = res.getPlayerFlags();
        for (Entry<String, Map<Flag, Boolean>> playerSet : apFlags.entrySet()) {
            if (playerSet.getKey().equals(player.getName())) {
                continue;
            }
            builder.append(playerSet.getKey());
            builder.append(" [");
            Map<Flag, Boolean> pFlags = playerSet.getValue();
            for (Entry<Flag, Boolean> flag : pFlags.entrySet()) {
                builder.append(" ");
                builder.append(flag.getValue() ? "+" : "-");
                builder.append(flag.getKey().getName());
            }
            builder.append("] ");
        }
        if (builder.length() == 0) {
            builder.append(LocaleLoader.getString("Info.None"));
        }
        player.sendMessage(LocaleLoader.getString("Info.OtherFlags", builder.toString()));
        builder = new StringBuilder();
        flags = res.getRentFlags();
        for (Entry<Flag, Boolean> flag : flags.entrySet()) {
            builder.append(" ");
            builder.append(flag.getValue() ? "+" : "-");
            builder.append(flag.getKey().getName());
        }
        if (builder.length() == 0) {
            builder.append(LocaleLoader.getString("Info.None"));
        }
        player.sendMessage(LocaleLoader.getString("Info.RentFlags", builder.toString()));
        player.sendMessage(LocaleLoader.getString("Info.Size", res.getSize()));
        Location loc = res.getHighLocation();
        player.sendMessage(LocaleLoader.getString("Info.CoordsTop", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        loc = res.getLowLocation();
        player.sendMessage(LocaleLoader.getString("Info.CoordsBottom", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    public void printRentLinks(Player player, ResidenceArea res) {
        player.sendMessage(LocaleLoader.getString("Info.Residence", res.getName()));
        if (res.getParent() != null) {
            player.sendMessage(LocaleLoader.getString("Info.FullName", res.getFullName()));
        }
        player.sendMessage(LocaleLoader.getString("Info.RentLinks"));
        for (ResidenceArea rentLink : res.getRentLinks()) {
            player.sendMessage(LocaleLoader.getString("Info.RentLink", rentLink.getName()));
        }
    }

    public void printMarketInfo(Player player, ResidenceArea res) {
        player.sendMessage(LocaleLoader.getString("Info.Residence", res.getName()));
        if (res.getParent() != null) {
            player.sendMessage(LocaleLoader.getString("Info.FullName", res.getFullName()));
        }
        player.sendMessage(LocaleLoader.getString("Info.Owner", res.getOwner()));
        if (res.isRented()) {
            player.sendMessage(LocaleLoader.getString("Info.Renter", res.getRenter()));
            player.sendMessage(LocaleLoader.getString("Info.AutoRenew", res.isAutoRenew()));
        } else if (res.isForRent()) {
            player.sendMessage(LocaleLoader.getString("Info.ForRent", res.getCost(), res.getRentPeriod()));
            if (res.isAutoRenewEnabled()) {
                player.sendMessage(LocaleLoader.getString("Info.AutoRenewEnabled"));
            } else {
                player.sendMessage(LocaleLoader.getString("Info.AutoRenewDisabled"));
            }
        } else if (res.isForSale()) {
            player.sendMessage(LocaleLoader.getString("Info.ForSale", res.getCost()));
        }
    }

    public void printForSaleResidences(Player player) {
        // TODO Auto-generated method stub

    }

    public void printRentableResidences(Player player) {
        // TODO Auto-generated method stub

    }
}
