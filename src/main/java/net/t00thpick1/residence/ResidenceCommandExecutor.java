package net.t00thpick1.residence;

import net.t00thpick1.residence.api.Flag;
import net.t00thpick1.residence.api.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.protection.CuboidArea;
import net.t00thpick1.residence.protection.EconomyManager;
import net.t00thpick1.residence.protection.GroupManager;
import net.t00thpick1.residence.protection.ResidenceManager;
import net.t00thpick1.residence.selection.SelectionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ResidenceCommandExecutor implements CommandExecutor {
    private Residence plugin;
    private ResidenceManager rmanager = Residence.getInstance().getResidenceManager();
    private Map<String, String> deleteConfirm;

    public ResidenceCommandExecutor(Residence residence) {
        plugin = residence;
        residence.getServer().getPluginCommand("residence").setExecutor(this);
        residence.getServer().getPluginCommand("resadmin").setExecutor(this);
        residence.getServer().getPluginCommand("resreload").setExecutor(this);
        residence.getServer().getPluginCommand("resload").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean resadmin = false;
        Player player = null;
        String pname = null;

        if (sender instanceof Player) {
            player = (Player) sender;
            pname = player.getName();
            if (command.getName().equals("resadmin")) {
                if (player.hasPermission("residence.admin")) {
                    resadmin = true;
                } else {
                    sender.sendMessage(LocaleLoader.getString("Commands.Generic.NonAdmin"));
                    return true;
                }
            }

            if (!player.hasPermission("residence.commands")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
        } else {
            resadmin = true;
        }

        if (args.length == 0) {
            args = new String[] { "?" };
        }

        if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?")) {
            return commandHelp(args, resadmin, sender);
        }

        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }

        String cmd = args[0].toLowerCase();
        if (cmd.equalsIgnoreCase("remove") || cmd.equalsIgnoreCase("delete")) {
            if (!sender.hasPermission("residence.commands.remove")) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResRemove(args, resadmin, sender, page);
        }
        if (cmd.equalsIgnoreCase("confirm")) {
            if (!sender.hasPermission("residence.commands.remove")) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResConfirm(args, resadmin, sender, page);
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
            if (!resadmin) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ClaimedResidence area = Residence.getInstance().getResidenceManager().getByName(args[1]);
            if (area != null) {
                area.setOwner(args[2]);
                if (area.getParent() == null) {
                    sender.sendMessage(LocaleLoader.getString("ResidenceOwnerChange", args[1], args[2]));
                } else {
                    sender.sendMessage(LocaleLoader.getString("SubzoneOwnerChange", area.getName(), args[2]));
                }
            } else {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
            }
            return true;
        }
        if (player == null) {
            return true;
        }
        if (command.getName().equals("resadmin")) {
            if (args.length == 1 && args[0].equals("on")) {
                plugin.activateAdminMode(player);
                player.sendMessage(LocaleLoader.getString("AdminToggleOn"));
                return true;
            } else if (args.length == 1 && args[0].equals("off")) {
                plugin.deactivateAdminMode(player);
                player.sendMessage(LocaleLoader.getString("AdminToggleOff"));
                return true;
            }
        }
        if (cmd.equalsIgnoreCase("select")) {
            if (!player.hasPermission("residence.commands.select")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSelect(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("create")) {
            if (!player.hasPermission("residence.commands.create")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResCreate(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("subzone") || cmd.equalsIgnoreCase("sz")) {
            if (!player.hasPermission("residence.commands.subzone")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSubzone(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("gui")) {
            if (!player.hasPermission("residence.commands.gui")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResGui(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("sublist")) {
            if (!player.hasPermission("residence.commands.sublist")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSublist(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("compass")) {
            if (!player.hasPermission("residence.commands.compass")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResCompass(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("info")) {
            if (!player.hasPermission("residence.commands.info")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 1) {
                ClaimedResidence area = Residence.getInstance().getResidenceManager().getByLoc(player.getLocation());
                if (area != null) {
                    area.printInformation(player);
                } else {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                }
                return true;
            } else if (args.length == 2) {
                ClaimedResidence area = Residence.getInstance().getResidenceManager().getByName(args[1]);
                if (area != null) {
                    area.printInformation(player);
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
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InResidence", res.getName()));
            }
            return true;
        }
        if (cmd.equalsIgnoreCase("set")) {
            if (!player.hasPermission("residence.commands.set")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResSet(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("pset")) {
            if (!player.hasPermission("residence.commands.pset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResPset(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("gset")) {
            if (!player.hasPermission("residence.commands.gset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            return commandResGset(args, resadmin, player, page);
        }
        if (cmd.equalsIgnoreCase("rename")) {
            if (!player.hasPermission("residence.commands.rename")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 3) {
                String oldName = args[1];
                String newName = args[2];
                ClaimedResidence res = rmanager.getByName(oldName);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", oldName));
                    return true;
                }
                if (!resadmin && !res.getOwner().equalsIgnoreCase(pname)) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                    return true;
                }
                if (rmanager.getByName(newName) != null) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Create.AlreadyExists", newName));
                    return true;
                }
                if (res.rename(newName)) {
                    sender.sendMessage(LocaleLoader.getString("Commands.Rename.Success"));
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
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
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
            ClaimedResidence mirror = rmanager.getByName(args[2]);
            ClaimedResidence mirrorTo = rmanager.getByName(args[1]);
            if (mirror == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (mirrorTo == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !mirrorTo.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            mirrorTo.copyPermissions(mirror);
            return true;
        }
        if (cmd.equalsIgnoreCase("list")) {
            if (!player.hasPermission("residence.commands.list")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            // TODO
            return true;
        }
        if (cmd.equalsIgnoreCase("listall")) {
            if (!player.hasPermission("residence.commands.listall")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            // TODO
            return true;
        }
        if (cmd.equalsIgnoreCase("listallhidden")) {
            if (!player.hasPermission("residence.commands.listallhidden")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            // TODO
            return true;
        }
        if (cmd.equalsIgnoreCase("tpset")) {
            if (!player.hasPermission("residence.commands.tpset")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
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
            ClaimedResidence res = rmanager.getByName(args[1]);
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
            return commandResMarket(args, resadmin, player, page);
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
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            String newOwner = args[2];
            if (!resadmin || !res.getOwner().equalsIgnoreCase(pname)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.setOwner(newOwner);
            return true;
        }
        if (cmd.equalsIgnoreCase("server")) {
            if (!resadmin || !player.hasPermission("residence.commands.server")) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (args.length == 2) {
                ClaimedResidence res = rmanager.getByName(args[1]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                    return true;
                }
                res.setOwner("Server Land");
                player.sendMessage(LocaleLoader.getString("ResidenceOwnerChange", "Server Land"));
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
            ClaimedResidence area = rmanager.getByName(args[1]);
            if (area == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !area.allowAction(player, FlagManager.ADMIN)) {
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
        // TODO
        return false;
    }

    private boolean commandResSelect(String[] args, boolean resadmin, Player player, int page) {
        SelectionManager selectionManager = Residence.getInstance().getSelectionManager();
        if (!resadmin && !player.hasPermission("residence.select")) {
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
                    player.sendMessage(LocaleLoader.getString("Selection.PrimaryPoint", playerLoc1.getBlockX(), playerLoc1.getBlockY(), playerLoc1.getBlockZ()));
                }
                Location playerLoc2 = selectionManager.getPlayerLoc2(player);
                if (playerLoc2 != null) {
                    player.sendMessage(LocaleLoader.getString("Selection.SecondaryPoint", playerLoc2.getBlockX(), playerLoc2.getBlockY(), playerLoc2.getBlockZ()));
                }
                return true;
            } else if (args[1].equals("chunk")) {
                selectionManager.selectChunk(player);
                return true;
            } else if (args[1].equals("worldedit")) {
                if (selectionManager.worldEdit(player)) {
                    player.sendMessage(LocaleLoader.getString("Commands.Select.Success"));
                }
                return true;
            }
        } else if (args.length == 3) {
            if (args[1].equals("expand")) {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber"));
                    return true;
                }
                selectionManager.modify(player, false, amount);
                return true;
            } else if (args[1].equals("shift")) {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidNumber"));
                    return true;
                }
                selectionManager.modify(player, true, amount);
                return true;
            }
        }
        if (args.length > 1 && args[1].equals("residence")) {
            String resName;
            ClaimedResidence res = null;
            if (args.length > 2) {
                res = rmanager.getByName(args[2]);
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                    return true;
                }
            } else {
                res = rmanager.getByLoc(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
            }
            resName = res.getName();
            selectionManager.placeLoc1(player, res.getHighLocation());
            selectionManager.placeLoc2(player, res.getLowLocation());
            player.sendMessage(LocaleLoader.getString("Commands.Select.SelectionArea", resName));
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

    private boolean commandResCreate(String[] args, boolean resadmin, Player player, int page) {
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
        CuboidArea newArea = new CuboidArea(selectionManager.getPlayerLoc1(player), selectionManager.getPlayerLoc2(player));
        if (!resadmin) {
            if (newArea.getXSize() > GroupManager.getMaxWidth(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooWide"));
                return true;
            }
            if (newArea.getXSize() < GroupManager.getMinWidth(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooNarrow"));
                return true;
            }
            if (newArea.getZSize() > GroupManager.getMaxLength(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooLong"));
                return true;
            }
            if (newArea.getZSize() < GroupManager.getMinLength(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooSkinny"));
                return true;
            }
            if (newArea.getYSize() > GroupManager.getMaxHeight(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooTall"));
                return true;
            }
            if (newArea.getYSize() < GroupManager.getMinHeight(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooShort"));
                return true;
            }
            if (newArea.getLowLocation().getBlockY() < GroupManager.getMinY(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooLow"));
                return true;
            }
            if (newArea.getHighLocation().getBlockY() > GroupManager.getMaxY(player)) {
                player.sendMessage(LocaleLoader.getString("Commands.Create.TooHigh"));
                return true;
            }
        }
        if (!resadmin && GroupManager.getMaxResidences(player.getName()) > rmanager.getOwnedZoneCount(player.getName())) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.TooManyResidences", args[1]));
            return true;
        }
        rmanager.createResidence(args[1], player.getName(), newArea);
        return true;
    }

    private boolean commandResSubzone(String[] args, boolean resadmin, Player player, int page) {
        if (args.length != 2 && args.length != 3) {
            return false;
        }
        SelectionManager selectionManager = Residence.getInstance().getSelectionManager();
        if (!selectionManager.hasPlacedBoth(player)) {
            player.sendMessage(LocaleLoader.getString("Commands.Create.SelectPoints"));
            return true;
        }
        String zname;
        ClaimedResidence res;
        if (args.length == 2) {
            res = rmanager.getByLoc(player.getLocation());
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
        if (res.getSubzone(zname) != null) {
            player.sendMessage(LocaleLoader.getString("Commands.Subzone.AlreadyExists", zname));
            return true;
        }
        CuboidArea newArea = new CuboidArea(selectionManager.getPlayerLoc1(player), selectionManager.getPlayerLoc2(player));
        if (!res.isAreaWithin(newArea)) {
            player.sendMessage(LocaleLoader.getString("Commands.Subzone.MustBeInside"));
            return true;
        }
        for (ClaimedResidence subzone : res.getSubzoneList()) {
            if (subzone.checkCollision(newArea)) {
                return false;
            }
        }
        res.addSubzone(zname, player.getName(), selectionManager.getPlayerLoc1(player), selectionManager.getPlayerLoc2(player));
        return true;
    }

    private boolean commandResRemove(String[] args, boolean resadmin, CommandSender sender, int page) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
            if (args.length == 1) {
                ClaimedResidence res = rmanager.getByLoc(player.getLocation());
                if (res == null) {
                    player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                    return true;
                }
                if (!resadmin && !res.getOwner().equalsIgnoreCase(player.getName())) {
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
        ClaimedResidence res = rmanager.getByName(args[1]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
            return true;
        }
        if (!resadmin && !res.getOwner().equalsIgnoreCase(player.getName())) {
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

    private boolean commandResConfirm(String[] args, boolean resadmin, CommandSender sender, int page) {
        if (args.length == 1) {
            String area = deleteConfirm.get(sender.getName());
            ClaimedResidence res = rmanager.getByName(area);
            if (res == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", area));
                return true;
            }
            if (!resadmin && !res.getOwner().equalsIgnoreCase(sender.getName())) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            if (area == null) {
                sender.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", area));
                return true;
            }
            rmanager.remove(res);
            deleteConfirm.remove(sender.getName());
            sender.sendMessage(LocaleLoader.getString("Commands.Remove.Removed"));
        }
        return true;
    }

    private boolean commandResSet(String[] args, boolean resadmin, Player player, int page) {
        if (args.length < 3 || args.length > 4) {
            return false;
        }
        ClaimedResidence res = null;
        String flagName = null;
        String boolName = null;
        if (args.length == 3) {
            res = rmanager.getByLoc(player.getLocation());
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
        if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
            return true;
        }
        Flag flag = FlagManager.getFlag(flagName);
        if (flag == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
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
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
            return true;
        }
        res.setFlag(flag, value);
        player.sendMessage(LocaleLoader.getString("Commands.Flags.FlagSet", flag.getName(), value));
        return true;
    }

    private boolean commandResPset(String[] args, boolean resadmin, Player player, int page) {
        if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.removeAllPlayerFlags();
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagsRemoved"));
            return true;
        } else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            res.removeAllPlayerFlags();
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagsRemoved"));
            return true;
        } else if (args.length == 4) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            String playerName = args[1];
            String flagName = args[2];
            String boolName = args[3];
            Flag flag = FlagManager.getFlag(flagName);
            if (flag == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
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
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
            res.setPlayerFlag(playerName, flag, value);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagSet", playerName, flag.getName(), value));
            return true;
        } else if (args.length == 5) {
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            String playerName = args[2];
            String flagName = args[3];
            String boolName = args[4];
            Flag flag = FlagManager.getFlag(flagName);
            if (flag == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
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
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
            res.setPlayerFlag(playerName, flag, value);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.PlayerFlagSet", playerName, flag.getName(), value));
            return true;
        }
        return false;
    }

    private boolean commandResGset(String[] args, boolean resadmin, Player player, int page) {
        if (args.length == 4) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            String groupName = args[1];
            String flagName = args[2];
            String boolName = args[3];
            Flag flag = FlagManager.getFlag(flagName);
            if (flag == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
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
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
            res.setGroupFlag(groupName, flag, value);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.GroupFlagSet", groupName, flag.getName(), value));
            return true;
        } else if (args.length == 5) {
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            String groupName = args[2];
            String flagName = args[3];
            String boolName = args[4];
            Flag flag = FlagManager.getFlag(flagName);
            if (flag == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Flags.InvalidFlag", flagName));
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
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
            res.setGroupFlag(groupName, flag, value);
            player.sendMessage(LocaleLoader.getString("Commands.Flags.GroupFlagSet", groupName, flag.getName(), value));
            return true;
        }
        return false;
    }

    private boolean commandResMarket(String[] args, boolean resadmin, Player player, int page) {
        if (args.length == 1) {
            return false;
        }
        String command = args[1].toLowerCase();
        if (command.equalsIgnoreCase("list")) {
            return commandResMarketList(args, resadmin, player, page);
        }
        if (command.equalsIgnoreCase("autorenew")) {
            return commandResMarketAutorenew(args, resadmin, player, page);
        }
        if (command.equalsIgnoreCase("rentable")) {
            return commandResMarketRentable(args, resadmin, player, page);
        }
        if (command.equalsIgnoreCase("rent")) {
            return commandResMarketRent(args, resadmin, player, page);
        }
        if (command.equalsIgnoreCase("release")) {
            if (args.length != 3) {
                return false;
            }
            ClaimedResidence res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isForRent() && (resadmin || player.getName().equalsIgnoreCase(res.getOwner()))) {
                res.removeFromMarket();
                player.sendMessage(LocaleLoader.getString("Commands.Market.Release.NoLongerForRent"));
                if (res.isRented()) {
                    player.sendMessage(LocaleLoader.getString("Commands.Market.Release.CurrentRenter"));
                }
                return true;
            }
            return true;
        }
        if (command.equalsIgnoreCase("unrent")) {
            if (args.length != 3) {
                return false;
            }
            ClaimedResidence res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isRented() && (resadmin || player.getName().equalsIgnoreCase(res.getRenter()))) {
                res.evict();
                player.sendMessage(LocaleLoader.getString("Commands.Market.Release.NoLongerRenting"));
                return true;
            }
            return true;
        }
        if (command.equalsIgnoreCase("info")) {
            ClaimedResidence res = null;
            if (args.length == 2) {
                res = rmanager.getByLoc(player.getLocation());
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
            res.printMarketInfo(player);
            return true;
        }
        if (command.equalsIgnoreCase("buy")) {
            if (args.length != 3) {
                return false;
            }
            ClaimedResidence res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (!res.buy(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Buy.TooPoor"));
                return true;
            }
            player.sendMessage(LocaleLoader.getString("Commands.Market.Buy.Purchased"));
            return true;
        }
        if (command.equalsIgnoreCase("unsell")) {
            if (args.length != 3) {
                return false;
            }
            ClaimedResidence res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (!res.isForSale()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Unsell.NotForSale"));
                return true;
            }
            if (!resadmin && !res.getOwner().equalsIgnoreCase(player.getName())) {
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
            ClaimedResidence res = rmanager.getByName(args[2]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
                return true;
            }
            if (res.isForSale()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Sell.AlreadyForSale"));
                return true;
            }
            if (!resadmin && !res.getOwner().equalsIgnoreCase(player.getName())) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NoPermission"));
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (Exception ex) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidAmount"));
                return true;
            }
            res.setForSale(amount);
            return true;
        }
        return false;
    }

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player, int page) {
        if (args.length < 3 || args.length > 4) {
            return false;
        }
        ClaimedResidence res = rmanager.getByName(args[2]);
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
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
            if (repeat && !res.isAutoRenewEnabled()) {
                player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.AutoRenewDisabled"));
            }
        }
        if (!res.rent(player.getName(), repeat)) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.TooPoor"));
        }
        return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player, int page) {
        if (args.length < 5 || args.length > 6) {
            return false;
        }
        if (!ConfigManager.getInstance().isRent()) {
            return true;
        }
        int days;
        int cost;
        ClaimedResidence res = rmanager.getByName(args[2]);
        if (res == null) {
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[2]));
            return true;
        }
        try {
            cost = Integer.parseInt(args[3]);
        } catch (Exception ex) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.InvalidCost"));
            return true;
        }
        try {
            days = Integer.parseInt(args[4]);
        } catch (Exception ex) {
            player.sendMessage(LocaleLoader.getString("Commands.Market.Rent.InvalidLength"));
            return true;
        }

        boolean autoRenew = res.isAutoRenewEnabled();
        if (args.length == 6) {
            if (args[5].equalsIgnoreCase("true") || args[5].equalsIgnoreCase("t")) {
                autoRenew = true;
            } else if (args[5].equalsIgnoreCase("false") || args[5].equalsIgnoreCase("f")) {
                autoRenew = false;
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
                return true;
            }
        }
        res.setForRent(cost, days * 24 * 60 * 60 * 1000, autoRenew);
        return true;
    }

    private boolean commandResMarketAutorenew(String[] args, boolean resadmin, Player player, int page) {
        if (!ConfigManager.getInstance().isEconomy()) {
            return true;
        }
        if (args.length != 4) {
            return false;
        }
        ClaimedResidence res = rmanager.getByName(args[2]);
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
            player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidBoolean"));
            return true;
        }
        if (res.isRented() && (resadmin || res.getRenter().equalsIgnoreCase(player.getName()))) {
            res.setAutoRenew(value);
        } else if (res.isForRent() && (resadmin || res.getOwner().equalsIgnoreCase(player.getName()))) {
            res.setAutoRenewEnabled(value);
        } else {
            player.sendMessage(LocaleLoader.getString("Commands.Market.InvalidRentState"));
        }
        return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player, int page) {
        if (!ConfigManager.getInstance().isEconomy()) {
            return true;
        }
        player.sendMessage(LocaleLoader.getString("MarketList"));
        EconomyManager.printForSaleResidences(player);
        if (ConfigManager.getInstance().isRent()) {
            EconomyManager.printRentableResidences(player);
        }
        return true;
    }

    private boolean commandResMessage(String[] args, boolean resadmin, Player player) {
        if (args.length < 2) {
            return false;
        }
        if (args[1].equals("enter")) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            return commandResMessageEnter(args, resadmin, player, 2, res);
        } else if (args[1].equals("leave")) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.NotInResidence"));
                return true;
            }
            return commandResMessageLeave(args, resadmin, player, 2, res);
        } else if (args[1].equals("remove")) {
            ClaimedResidence res = rmanager.getByLoc(player.getLocation());
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
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            return commandResMessageEnter(args, resadmin, player, 3, res);
        } else if (args.length > 2 && args[2].equals("leave")) {
            ClaimedResidence res = rmanager.getByName(args[1]);
            if (res == null) {
                player.sendMessage(LocaleLoader.getString("Commands.Generic.InvalidResidence", args[1]));
                return true;
            }
            return commandResMessageLeave(args, resadmin, player, 3, res);
        } else if (args.length > 2 && args[2].equals("remove")) {
            ClaimedResidence res = rmanager.getByName(args[1]);
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

    private boolean commandResMessageLeave(String[] args, boolean resadmin, Player player, int start, ClaimedResidence res) {
        if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
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
        return false;
    }

    private boolean commandResMessageEnter(String[] args, boolean resadmin, Player player, int start, ClaimedResidence res) {
        if (!resadmin && !res.allowAction(player, FlagManager.ADMIN)) {
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

    private boolean commandResSublist(String[] args, boolean resadmin, Player player, int page) {
        if (args.length == 1 || args.length == 2 || args.length == 3) {
            ClaimedResidence res;
            if (args.length == 1) {
                res = rmanager.getByLoc(player.getLocation());
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
            res.getSubzoneNameList();
            // TODO Print this out
            return true;
        }
        return false;
    }

    private boolean commandResCompass(String[] args, boolean resadmin, Player player, int page) {
        if (args.length != 2) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage(LocaleLoader.getString("Commands.CompassTarget.Reset"));
            return true;
        }
        ClaimedResidence res = rmanager.getByName(args[1]);
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

    private boolean commandResGui(String[] args, boolean resadmin, Player player, int page) {
        // TODO
        return true;
    }
}
