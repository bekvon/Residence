/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.IConomy4Adapter;
import com.bekvon.bukkit.residence.economy.IConomyAdapter;
import com.bekvon.bukkit.residence.economy.MineConomyAdapter;
import com.bekvon.bukkit.residence.economy.RentManager;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.itemlist.ItemManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.PermissionList;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.iConomy.iConomy;
import com.spikensbror.bukkit.mineconomy.MineConomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Gary Smoak - bekvon
 *
 */
public class Residence extends JavaPlugin {

    private static ResidenceManager rmanager;
    private static SelectionManager smanager;
    private static PermissionManager gmanager;
    private static ConfigManager cmanager;
    private static ResidenceBlockListener blistener;
    private static ResidencePlayerListener plistener;
    private static ResidenceEntityListener elistener;
    private static TransactionManager tmanager;
    private static PermissionListManager pmanager;
    private static LeaseManager leasemanager;
    private static ItemManager imanager;
    private static WorldFlagManager wmanager;
    private static RentManager rentmanager;
    private static Server server;
    private boolean firstenable = true;
    private static EconomyInterface economy;
    private final int saveVersion = 1;
    private int leaseBukkitId;
    private int leaseInterval;
    private Runnable leaseExpire = new Runnable() {
        public void run() {
            leasemanager.doExpirations();
            System.out.println("[Residence] - Lease Expirations checked!");
        }
    };
    private static boolean enableecon;
    private static String econsys;
    private static File ymlSaveLoc;
    private static int autosaveInt;
    private static int autosaveBukkitId;
    private Runnable autoSave = new Runnable() {
        public void run() {
            saveYml();
        }
    };

    public Residence() {
    }

    public void reloadPlugin()
    {
        this.setEnabled(false);
        this.setEnabled(true);
    }

    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        if (cmanager.useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        saveYml();
        Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Disabled!");
    }

    public void onEnable() {
        try {
            server = this.getServer();
            if (!new File(this.getDataFolder(), "config.yml").isFile()) {
                this.writeDefaultConfigFromJar();
            }
            this.getConfiguration().load();
            if(this.getConfiguration().getInt("ResidenceVersion", 0) == 0)
            {
                this.writeDefaultConfigFromJar();
                this.getConfiguration().load();
                System.out.println("[Residence] Config Invalid, wrote default...");
            }
            cmanager = new ConfigManager(this.getConfiguration());
            gmanager = new PermissionManager(this.getConfiguration());
            imanager = new ItemManager(this.getConfiguration());
            wmanager = new WorldFlagManager(this.getConfiguration());
            rentmanager = new RentManager();
            enableecon = this.getConfiguration().getBoolean("Global.EnableEconomy", true);
            econsys = this.getConfiguration().getString("Global.EconomySystem", "iConomy");
            ymlSaveLoc = new File(this.getDataFolder(), "res.yml");
            economy = null;
            if (firstenable) {
                if (!this.getDataFolder().isDirectory()) {
                    this.getDataFolder().mkdirs();
                }
                if (enableecon && econsys != null) {
                    if (econsys.toLowerCase().equals("iconomy")) {
                        this.loadIConomy();
                    } else if (econsys.toLowerCase().equals("mineconomy")) {
                        this.loadMineConomy();
                    }
                }
                String multiworld = cmanager.getMultiworldPlugin();
                if (multiworld != null) {
                    Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                    if (plugin != null) {
                        if (!plugin.isEnabled()) {
                            server.getPluginManager().enablePlugin(plugin);
                            System.out.println("[Residence] - Enabling multiworld plugin: " + multiworld);
                        }
                    }
                }
                this.loadYml();
                if (rmanager == null) {
                    rmanager = new ResidenceManager();
                }
                if (leasemanager == null) {
                    leasemanager = new LeaseManager(rmanager);
                }
                if (tmanager == null) {
                    tmanager = new TransactionManager(rmanager, gmanager);
                }
                if (pmanager == null) {
                    pmanager = new PermissionListManager();
                }
                smanager = new SelectionManager();
                blistener = new ResidenceBlockListener();
                plistener = new ResidencePlayerListener(this.getConfiguration().getInt("Global.MoveCheckInterval", 1000));
                elistener = new ResidenceEntityListener();
                PluginManager pm = getServer().getPluginManager();
                pm.registerEvent(Event.Type.BLOCK_BREAK, blistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.BLOCK_PLACE, blistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.BLOCK_IGNITE, blistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.BLOCK_BURN, blistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.BLOCK_FROMTO, blistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.PLAYER_INTERACT, plistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.PLAYER_MOVE, plistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.PLAYER_QUIT, plistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.CREATURE_SPAWN, elistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.ENTITY_DAMAGE, elistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.ENTITY_EXPLODE, elistener, Priority.Lowest, this);
                pm.registerEvent(Event.Type.EXPLOSION_PRIME, elistener, Priority.Lowest, this);
                firstenable = false;
            }
            autosaveInt = this.getConfiguration().getInt("SaveInterval", 10);
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = (autosaveInt * 60) * 20;
            autosaveBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);
            leaseInterval = this.getConfiguration().getInt("LeaseCheckInterval", 10);
            if (leaseInterval < 1) {
                leaseInterval = 1;
            }
            leaseInterval = (leaseInterval * 60) * 20;
            if (cmanager.useLeases()) {
                leaseBukkitId = server.getScheduler().scheduleAsyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Enabled! Version " + this.getDescription().getVersion() + " by bekvon");
        } catch (Exception ex) {
            System.out.println("[Residence] - FAILED INITIALIZATION! DISABLED! ERROR:");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            this.setEnabled(false);
        }
    }

    public static ResidenceManager getResidenceManger() {
        return rmanager;
    }

    public static SelectionManager getSelectionManager() {
        return smanager;
    }

    public static PermissionManager getPermissionManager() {
        return gmanager;
    }

    public static EconomyInterface getEconomyManager() {
        return economy;
    }

    public static Server getServ() {
        return server;
    }

    public static LeaseManager getLeaseManager() {
        return leasemanager;
    }

    public static ConfigManager getConfig() {
        return cmanager;
    }

    public static TransactionManager getTransactionManager() {
        return tmanager;
    }

    public static ItemManager getItemManager()
    {
        return imanager;
    }

    public static WorldFlagManager getWorldFlags()
    {
        return wmanager;
    }

    public static RentManager getRentManager()
    {
        return rentmanager;
    }

    private void loadIConomy() 
    {
        Plugin p = getServer().getPluginManager().getPlugin("iConomy");
        if (p != null) {
            if(p.getDescription().getVersion().startsWith("5"))
            {
                economy = new IConomyAdapter((iConomy)p);
            }
            else
            {
                economy = new IConomy4Adapter((com.nijiko.coelho.iConomy.iConomy)p);
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with iConomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] iConomy NOT found!");
        }
    }
    
    private void loadMineConomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("MineConomy");
        if (p != null) {
            economy = new MineConomyAdapter((MineConomy)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with MineConomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] MineConomy NOT found!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("resreload") && args.length==0)
        {
            if(sender instanceof Player)
            {
                Player player = (Player) sender;
                if(gmanager.isResidenceAdmin(player))
                {
                    this.setEnabled(false);
                    this.setEnabled(true);
                    System.out.println("[Residence] Reloaded by "+player.getName()+".");
                }
            }
            else if(sender instanceof ConsoleCommandSender)
            {
                this.setEnabled(false);
                this.setEnabled(true);
                System.out.println("[Residence] Reloaded by console.");
            }
            return true;
        }
        if (command.getName().equals("res") || command.getName().equals("residence")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                String pname = player.getName();
                if (cmanager.allowAdminsOnly()) {
                    if (!gmanager.isResidenceAdmin(player)) {
                        player.sendMessage("§cOnly admins have access to this command.");
                        return true;
                    }
                }
                if(args.length==0)
                    return false;
                if (args.length == 0) {
                    args = new String[1];
                    args[0] = "?";
                }
                if (args[0].equals("select")) {
                    if(!group.canCreateResidences() && group.getMaxSubzoneDepth() <= 0 && !Residence.getPermissionManager().isResidenceAdmin(player))
                    {
                        player.sendMessage("§cYou can not create residences or subzones, so you dont have selection access.");
                        return true;
                    }
                    if (args.length == 1 || (args.length == 2 && args[1].equals("?"))) {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§aselect §6[x] [y] [z]§3 - select in x,y,z radius");
                        player.sendMessage("§aselect §6vert§3 - expands selection from highest to lowest allowed");
                        player.sendMessage("§aselect §6sky§3 - expands selection to highest allowed");
                        player.sendMessage("§aselect §6bedrock§3 - expands selection to lowest allowed");
                        player.sendMessage("§aselect §6size§3 - get size of selection.");
                        player.sendMessage("§aselect §6coords§3 - get selected coords.");
                        player.sendMessage("§aselect §6expand <size>§3 - expand selection the direction your looking.");
                        player.sendMessage("§aselect §6shift <distance>§3 - shift selection the direction your looking.");
                        player.sendMessage("§aselect §6chunk§3 - select the current chunk your in.");
                        player.sendMessage("§aselect §6residence <ResidenceName> <AreaID>§3 - select existing area.");
                        player.sendMessage("§9You can use a " + Material.getMaterial(cmanager.getSelectionTooldID()).name() + " tool to select.");
                        return true;
                    } else if (args.length == 2) {
                        if (args[1].equals("size")) {
                            if (smanager.hasPlacedBoth(pname)) {
                                try {
                                    smanager.displaySelectionSize(player);
                                    return true;
                                } catch (Exception ex) {
                                    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                                    return true;
                                }
                            }
                        } else if (args[1].equals("vert")) {
                            smanager.vert(player);
                            return true;
                        }
                        else if(args[1].equals("sky"))
                        {
                            smanager.sky(player);
                            return true;
                        }
                        else if(args[1].equals("bedrock"))
                        {
                            smanager.bedrock(player);
                            return true;
                        }
                        else if (args[1].equals("coords")) {
                            player.sendMessage("§aSelections:");
                            Location playerLoc1 = smanager.getPlayerLoc1(pname);
                            if (playerLoc1 != null) {
                                player.sendMessage("§aPrimary Selection:§b (" + playerLoc1.getBlockX() + ", " + playerLoc1.getBlockY() + ", " + playerLoc1.getBlockZ() + ")");
                            }
                            Location playerLoc2 = smanager.getPlayerLoc2(pname);
                            if (playerLoc2 != null) {
                                player.sendMessage("§aSecondary Selection:§b (" + playerLoc2.getBlockX() + ", " + playerLoc2.getBlockY() + ", " + playerLoc2.getBlockZ() + ")");
                            }
                            return true;
                        }
                        else if(args[1].equals("chunk"))
                        {
                            smanager.selectChunk(player);
                            return true;
                        }
                    } else if (args.length == 3) {
                        if (args[1].equals("expand")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (Exception ex) {
                                player.sendMessage("§cInvalid amount...");
                                return true;
                            }
                            smanager.modify(player, false, amount);
                            return true;
                        } else if (args[1].equals("shift")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (Exception ex) {
                                player.sendMessage("§cInvalid amount...");
                                return true;
                            }
                            smanager.modify(player, true, amount);
                            return true;
                        }
                    }
                    if (args.length != 4) {
                        player.sendMessage("§c/res select ? for more info.");
                        return true;
                    }
                    if (args[1].equals("residence")) {
                        ClaimedResidence res = rmanager.getByName(args[2]);
                        if (res == null) {
                            player.sendMessage("§cInvalid Residence...");
                            return true;
                        }
                        CuboidArea area = res.getArea(args[3]);
                        if(area!=null)
                        {
                            smanager.placeLoc1(pname, area.getHighLoc());
                            smanager.placeLoc2(pname, area.getLowLoc());
                            player.sendMessage("§aSelected area §6" + args[3] + "§a of residence §6" + args[2] + "§a.");
                        }
                        else
                        {
                            player.sendMessage("§cInvalid residence physical area ID.");
                        }
                        return true;
                    }
                    else
                    {
                        try {
                            smanager.selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                            return true;
                        } catch (Exception ex) {
                            player.sendMessage("§cInvalid select command.");
                            return true;
                        }
                    }
                } else if (args[0].equals("create")) {
                    if (args.length != 2) {
                        return false;
                    }
                    if(args.length==1 || (args.length == 2 && args[1].equals("?")))
                    {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§acreate §6<ResidenceName>§3");
                        return true;
                    }
                    if (smanager.hasPlacedBoth(pname)) {
                        rmanager.addResidence(player, args[1], smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname));
                        return true;
                    } else {
                        player.sendMessage("§cYou have not selected two points yet!");
                        return true;
                    }
                } else if (args[0].equals("subzone") || args[0].equals("sz")) {
                    if (args.length != 2 && args.length != 3) {
                        return false;
                    }
                    if(args.length==1 || (args.length == 2 && args[1].equals("?")))
                    {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§asubzone / sz §6<ParentZoneName> [SubZoneName]§3");
                        return true;
                    }
                    String zname;
                    String parent;
                    if (args.length == 2) {
                        parent = rmanager.getNameByLoc(player.getLocation());
                        zname = args[1];
                    } else {
                        parent = args[1];
                        zname = args[2];
                    }
                    if (smanager.hasPlacedBoth(pname)) {
                        ClaimedResidence res = rmanager.getByName(parent);
                        if(res==null)
                        {
                            player.sendMessage("§cInvalid Residence...");
                            return true;
                        }
                        res.addSubzone(player, smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname), zname);
                        return true;
                    } else {
                        player.sendMessage("§cYou have not selected two points yet!");
                        return true;
                    }
                } else if (args[0].equals("remove") || args[0].equals("delete")) {
                    if (args.length == 1) {
                        String area = rmanager.getNameByLoc(player.getLocation());
                        if (area != null) {
                            rmanager.removeResidence(player, area);
                            return true;
                        }
                        return false;
                    }
                    if (args.length != 2) {
                        return false;
                    }
                    rmanager.removeResidence(player, args[1]);
                    return true;
                } else if (args[0].equals("area")) {
                    if (args.length == 4) {
                        if (args[1].equals("remove")) {
                            ClaimedResidence res = rmanager.getByName(args[2]);
                            res.removeArea(player, args[3]);
                            return true;
                        } else if (args[1].equals("add")) {
                            if (smanager.hasPlacedBoth(pname)) {
                                rmanager.addPhysicalArea(player, args[2], args[3],smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname));
                            } else {
                                player.sendMessage("Select two points first!");
                            }
                            return true;
                        }
                    } else {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§barea §6<add/remove> <residence> <areaID>§3 - Allows physical areas to be added or removed from a residence.  Select an area first before adding.");
                        return true;
                    }
                } else if (args[0].equals("lists")) {
                    if(args.length==2)
                    {
                        if(args[1].equals("list"))
                        {
                            pmanager.printLists(player);
                            return true;
                        }
                    }
                    else if(args.length == 3) {
                        if (args[1].equals("view")) {
                            pmanager.printList(player, args[2]);
                            return true;
                        } else if (args[1].equals("remove")) {
                            pmanager.removeList(player, args[2]);
                            return true;
                        } else if (args[1].equals("add")) {
                            pmanager.makeList(player, args[2]);
                            return true;
                        }
                    } else if (args.length == 4) {
                        if (args[1].equals("apply")) {
                            pmanager.applyListToResidence(player, args[2], args[3]);
                            return true;
                        }
                    }
                    else if  (args.length==5)
                    {
                        if(args[1].equals("set"))
                        {
                            pmanager.getList(pname, args[2]).set(args[3], PermissionList.stringToFlagState(args[4]));
                            player.sendMessage("§aSet...");
                            return true;
                        }
                    }
                    else if(args.length==6)
                    {
                        if(args[1].equals("gset"))
                        {
                            pmanager.getList(pname, args[2]).setGroup(args[3], args[4], PermissionList.stringToFlagState(args[5]));
                            player.sendMessage("§aSet...");
                            return true;
                        }
                        else if(args[1].equals("pset"))
                        {
                            pmanager.getList(pname, args[2]).setPlayer(args[3], args[4], PermissionList.stringToFlagState(args[5]));
                            player.sendMessage("§aSet...");
                            return true;
                        }
                    }
                    else {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§9/res lists §3- Manage predefined permission lists.");
                        player.sendMessage("§badd §6<listname>§3 - Add a permission list.");
                        player.sendMessage("§bremove §6<listname>§3 - Add a permission list.");
                        player.sendMessage("§blist §3 - Display your lists.");
                        player.sendMessage("§bapply §6<listname> <residence>§3 - Apply list to residence.");
                        player.sendMessage("§bset §6<listname> <flag> <value>§3 - Set residence flags.");
                        player.sendMessage("§bpset / gset §6<listname> <player/group> <flag> <value>§3 - Set group/player flags.");
                        player.sendMessage("§bview §6<listname>§3 - View list.");
                        return true;
                    }
                } else if (args[0].equals("default")) {
                    if (args.length == 2) {
                        ClaimedResidence res = rmanager.getByName(args[1]);
                        res.getPermissions().applyDefaultFlags(player);
                        return true;
                    }
                } else if (args[0].equals("limits")) {
                    if (args.length == 1) {
                        gmanager.getGroup(player).printLimits(player);
                        return true;
                    }
                } else if (args[0].equals("info")) {
                    if (args.length == 1) {
                        String area = rmanager.getNameByLoc(player.getLocation());
                        if (area != null) {
                            rmanager.printAreaInfo(area, player);
                        } else {
                            player.sendMessage("§cInvalid Residence.");
                        }
                        return true;
                    } else if (args.length == 2) {
                        rmanager.printAreaInfo(args[1], player);
                        return true;
                    }
                } else if (args[0].equals("current")) {
                    if(args.length!=1)
                        return false;
                    String res = rmanager.getNameByLoc(player.getLocation());
                    if(res==null)
                    {
                        player.sendMessage("§cYou are not in a residence.");
                    }
                    else
                    {
                        player.sendMessage("§aYou are standing in residence §e" + res + "§a.");
                    }
                    return true;
                } else if (args[0].equals("set")) {
                    if (args.length == 3) {
                        String area = rmanager.getNameByLoc(player.getLocation());
                        if (area != null) {
                            rmanager.getByName(area).getPermissions().setFlag(player, args[1], args[2]);
                        } else {
                            player.sendMessage("§cInvalid Residence.");
                        }
                        return true;
                    } else if (args.length == 4) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if(area!=null)
                        {
                            area.getPermissions().setFlag(player, args[2], args[3]);
                        }
                        else
                            player.sendMessage("§cInvalid Residence.");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equals("?")) {
                            player.sendMessage("§d----------Command Help:----------");
                            player.sendMessage("§bset §6<residence> [flag] [true/false/remove]");
                            player.sendMessage("§2These are general flags can be true/false or neither.");
                            player.sendMessage("§cFlags:§a move,build,use,pvp,fire,damage,explosions,monsters,flow,tp");
                            player.sendMessage("§amove§3 - globally allow everyone move rights");
                            player.sendMessage("§abuild§3 - globally allow everyone build rights");
                            player.sendMessage("§ause§3 - globally allow everyone use rights");
                            player.sendMessage("§acontainer§3 - allows / disallows container access.");
                            player.sendMessage("§apvp§3 - allows or dissallows pvp.");
                            player.sendMessage("§aignite§3 - allows / disallows fire starting.");
                            player.sendMessage("§asubzone§3 - allows / disallows subzoning.");
                            player.sendMessage("§afirespread§3 - allows / disallows fire spread.");
                            player.sendMessage("§adamage§3 - allows / disallows damage while in zone.");
                            player.sendMessage("§aexplosions§3 - allows / disallows explosions.");
                            player.sendMessage("§amonsters§3 - allows / disallows monster spawns.");
                            player.sendMessage("§aflow§3 - allows / disallows liquid movement in zone.");
                            player.sendMessage("§atp§3 - allows / disallows teleports to your residence.");
                            player.sendMessage("§9<residence> can be ommited, it will use the residence your in.");
                            return true;
                        }
                    }
                    player.sendMessage("§c/res set ? for more info.");
                    return true;
                } else if (args[0].equals("pset")) {
                    if (args.length == 4) {
                        ClaimedResidence area = rmanager.getByLoc(player.getLocation());
                        if (area != null) {
                            area.getPermissions().setPlayerFlag(player, args[1], args[2], args[3]);
                        } else {
                            player.sendMessage("§cInvalid area.");
                        }
                        return true;
                    } else if (args.length == 5) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setPlayerFlag(player, args[2], args[3], args[4]);
                        }
                        else
                            player.sendMessage("§cInvalid residence.");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equals("?") || args[1].equals("help")) {
                            player.sendMessage("§d----------Command Help:----------");
                            player.sendMessage("§bpset §6<residence> [player] [flag] [true/false/remove]");
                            player.sendMessage("§2These are command for allowing / denying player permissions.");
                            player.sendMessage("§cFlags:§a move, build, use, tp, admin");
                            player.sendMessage("§amove§3 - allows movement when area set to private.");
                            player.sendMessage("§abuild§3 - allows building / destroying blocks.");
                            player.sendMessage("§ause§3 - allows using lever, doors, chests.");
                            player.sendMessage("§acontainer§3 - allows / disallows container access.");
                            player.sendMessage("§aadmin§3 - allows user to give / remove area flags.");
                            player.sendMessage("§asubzone§3 - allows / disallows subzoning.");
                            player.sendMessage("§atp§3 - allows / disallows player to tp to your residence.");
                            player.sendMessage("§9<residence> can be ommited, it will use the residence your in.");
                            return true;
                        }
                    }
                    player.sendMessage("§c/res pset ? for more info.");
                    return true;
                } else if (args[0].equals("gset")) {
                    if (args.length == 4) {
                        ClaimedResidence area = rmanager.getByLoc(player.getLocation());
                        if (area != null) {
                            area.getPermissions().setGroupFlag(player, args[1], args[2], args[3]);

                        } else {
                            player.sendMessage("§cInvalid area.");
                        }
                        return true;
                    } else if (args.length == 5) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setGroupFlag(player, args[2], args[3], args[4]);
                        }
                        else
                            player.sendMessage("§cInvalid residence.");
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equals("?") || args[1].equals("help")) {
                            player.sendMessage("§d----------Command Help:----------");
                            player.sendMessage("§bgset §6<residence> [group] [flag] [true/false/remove]");
                            player.sendMessage("§9gset follows the same rules and flags as pset, except it works for groups.  type /res pset ? for more info.");
                            return true;
                        }
                    }
                    player.sendMessage("§c/res pset ? for more info.");
                    return true;
                } else if (args[0].equals("list")) {
                    rmanager.listResidences(player);
                    return true;
                } else if (args[0].equals("?") || args[0].equals("help")) {
                    player.sendMessage("§d----------Command Help:----------");
                    player.sendMessage("§aselect §6[x] [y] [z]§3 - /res select ?");
                    player.sendMessage("§acreate §6[name]§3 - create residence [name] after selection.");
                    player.sendMessage("§asubzone §6<residence> [name]§3 - create subzone [name].");
                    player.sendMessage("§ainfo §6<residence>§3 - view info on residence.");
                    player.sendMessage("§aset / pset / gset§3 - sets flags, /res set ? for details");
                    player.sendMessage("§alist / listall §3- list your/all residences.");
                    player.sendMessage("§alimits §3- view global residence limits.");
                    player.sendMessage("§aunstuck §3- attempt to move out of the residence your in.");
                    player.sendMessage("§atp §6<residence> §3/ §atpset §3- tp to a residence / set tp loc.");
                    player.sendMessage("§amessage §6<residence> [enter/leave] [message]§3 - area message.");
                    player.sendMessage("§amirror §6[source] [target]§3 - clone residence permissions.");
                    player.sendMessage("§amarket§3 - buy / sell residence /res market ? for details.");
                    player.sendMessage("§alease§3 - lease management /res lease ? for details.");
                    player.sendMessage("§alists§3 - predefined permission lists /res lists ? for details.");
                    player.sendMessage("§aarea§3 - Add/Remove physical areas to the residence.");
                    player.sendMessage("§arename / renamearea§3 - rename a residence or area.");
                    player.sendMessage("§aversion§3 - show version.");
                    return true;
                }
                else if(args[0].equals("rename"))
                {
                    if(args.length==3)
                    {
                        rmanager.renameResidence(player, args[1], args[2]);
                        return true;
                    }
                    if(args.length==1 || (args.length == 2 && args[1].equals("?")))
                    {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§arename §6<FullOldName> <NewName>§3 - Renames a residence.");
                        return true;
                    }
                }
                else if(args[0].equals("renamearea"))
                {
                    if(args.length==4)
                    {
                        ClaimedResidence res = rmanager.getByName(args[1]);
                        if(res==null)
                        {
                            player.sendMessage("§cInvalid Residence...");
                            return true;
                        }
                        res.renameArea(player, args[2], args[3]);
                        return true;
                    }
                    if(args.length==1 || (args.length == 2 && args[1].equals("?")))
                    {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§arenamearea §6<ResidenceName> <OldAreaName> <NewAreaName>§3 - renames a area in a residence.");
                        return true;
                    }
                }
                else if (args[0].equals("unstuck")) {
                    if (args.length != 1) {
                        return false;
                    }
                    group = gmanager.getGroup(player);
                    if(!group.hasUnstuckAccess())
                    {
                        player.sendMessage("You don't have access to this command.");
                        return true;
                    }
                    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
                    if (res == null) {
                        player.sendMessage("§cYou are not in a residence.");
                    } else {
                        player.sendMessage("§eMoved...");
                        player.teleport(res.getOutsideFreeLoc(player.getLocation()));
                    }
                    return true;
                } else if (args[0].equals("mirror")) {
                    if (args.length != 3) {
                        return false;
                    }
                    rmanager.mirrorPerms(player, args[1], args[2]);
                    if(args.length==1 || (args.length == 2 && args[1].equals("?")))
                    {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§amirror §6<SourceResidence> <TargetResidence>§3 - mirrors permissions.");
                        return true;
                    }
                    return true;
                } else if (args[0].equals("listall")) {
                    rmanager.listAllResidences(player);
                    return true;
                } else if (args[0].equals("version")) {
                    player.sendMessage("------------------------------------");
                    player.sendMessage("§cThis server running Residence version: " + this.getDescription().getVersion());
                    player.sendMessage("§aCreated by: §ebekvon");
                    player.sendMessage("§bVisit my thread on http://forums.bukkit.org/ for more info, and to see an easier to read and more informative list of commands.");
                    player.sendMessage("------------------------------------");
                    return true;
                }
                else if(args[0].equals("material"))
                {
                    if(args.length!=2)
                        return false;
                    try
                    {
                        player.sendMessage("§aThe Material Name for ID §6" + args[1] + "§a is §c" + Material.getMaterial(Integer.parseInt(args[1])).name());
                    }
                    catch (Exception ex)
                    {
                        player.sendMessage("§cInvalid ID...");
                    }
                    return true;
                }
                else if (args[0].equals("tpset")) {
                    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
                    if (res != null) {
                        res.setTpLoc(player);
                    } else {
                        player.sendMessage("§cInvalid Residence...");
                    }
                    return true;
                } else if (args[0].equals("tp")) {
                    if (args.length != 2) {
                        return false;
                    }
                    ClaimedResidence res = rmanager.getByName(args[1]);
                    if (res == null) {
                        player.sendMessage("§cInvalid Residence...");
                        return true;
                    }
                    res.tpToResidence(player, player);
                    return true;
                } else if (args[0].equals("lease")) {
                    if (args.length == 1 || args.length == 2) {
                        if (args.length == 1 || args[1].equals("?")) {
                            player.sendMessage("§d----------Command Help:----------");
                            player.sendMessage("§b/res lease§3 - residence lease commands.");
                            player.sendMessage("§arenew §6[residence]§3 - renew residence.");
                            player.sendMessage("§acost §6[residence]§3 - get cost of renewal.");
                            return true;
                        }
                    }
                    if (args.length == 2 || args.length == 3) {
                        if (args[1].equals("renew")) {
                            if (args.length == 3) {
                                leasemanager.renewArea(args[2], player);
                            } else {
                                leasemanager.renewArea(rmanager.getNameByLoc(player.getLocation()), player);
                            }
                            return true;
                        } else if (args[1].equals("cost")) {
                            if (args.length == 3) {
                                if (leasemanager.leaseExpires(args[2])) {
                                    int cost = leasemanager.getRenewCost(args[2]);
                                    player.sendMessage("§eRenewal cost for area " + args[2] + " is: §c" + cost);
                                } else {
                                    player.sendMessage("§cInvalid area, or lease doesn't expire.");
                                }
                                return true;
                            } else {
                                String area = rmanager.getNameByLoc(player.getLocation());
                                if (area == null) {
                                    player.sendMessage("§cInvalid Area.");
                                    return true;
                                }
                                if (leasemanager.leaseExpires(area)) {
                                    int cost = leasemanager.getRenewCost(area);
                                    player.sendMessage("§eRenewal cost for area " + area + " is: §c" + cost);
                                } else {
                                    player.sendMessage("§cInvalid area, or lease doesn't expire.");
                                }
                                return true;
                            }
                        }
                    }
                    return false;
                } else if (args[0].equals("market")) {
                    if (args.length == 1 || args.length == 2) {
                        if (args.length == 1 || args[1].equals("?")) {
                            player.sendMessage("§d----------Command Help:----------");
                            player.sendMessage("§b/res market§3 - residence market commands.");
                            player.sendMessage("§abuy §6[residence]§3 - buy a residence");
                            player.sendMessage("§asell §6[residence] [amount]§3 - set residence for sale.");
                            player.sendMessage("§aunsell §6[residence]§3 - stop selling residence.");
                            player.sendMessage("§ainfo §6[residence]§3 - view market info for residence.");
                            return true;
                        }
                    }
                    if (args.length == 3) {
                        if (args[1].equals("buy")) {
                            tmanager.buyPlot(args[2], player);
                            return true;
                        } else if (args[1].equals("info")) {
                            tmanager.viewSaleInfo(args[2], player);
                            return true;
                        } else if (args[1].equals("unsell")) {
                            tmanager.removeFromSale(player, args[2]);
                            return true;
                        }
                    }
                    if (args.length == 4) {
                        if (args[1].equals("sell")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[3]);
                            } catch (Exception ex) {
                                player.sendMessage("Invalid sell amount.");
                                return true;
                            }
                            tmanager.putForSale(args[2], player, amount);
                            return true;
                        }
                    }
                    return false;
                } else if (args[0].equals("message")) {
                    if (args.length == 1 || (args.length == 2 && args[1].equals("?"))) {
                        player.sendMessage("§d----------Command Help:----------");
                        player.sendMessage("§amessage §6<ResidenceName> <enter/leave> <message>§3 - Set a enter or leave message.");
                        player.sendMessage("§amessage §6<ResidenceName> <remove> <enter/leave>§3 - Remove a enter or leave message.");
                        player.sendMessage("§cMessage Variables§3 - Variables you can use in a message.");
                        player.sendMessage("§6 %player§3 - Name of the player who entered / left.");
                        player.sendMessage("§6 %owner§3 - Residence owner.");
                        player.sendMessage("§6 %residence§3 - Name of the residence.");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage("§c/res message <residence> [enter/leave] [message]");
                        return true;
                    }
                    ClaimedResidence res = null;
                    int start = 0;
                    boolean enter = false;
                    if (args[1].equals("enter")) {
                        enter = true;
                        res = rmanager.getByLoc(player.getLocation());
                        start = 2;
                    } else if (args[1].equals("leave")) {
                        res = rmanager.getByLoc(player.getLocation());
                        start = 2;
                    } else if (args[2].equals("enter")) {
                        enter = true;
                        res = rmanager.getByName(args[1]);
                        start = 3;
                    } else if (args[2].equals("leave")) {
                        res = rmanager.getByName(args[1]);
                        start = 3;
                    } else if (args[1].equals("remove")) {
                        if (args[2].equals("enter")) {
                            res = rmanager.getByLoc(player.getLocation());
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, true);
                            } else {
                                player.sendMessage("Invalid Residence.");
                            }
                            return true;
                        } else if (args[2].equals("leave")) {
                            res = rmanager.getByLoc(player.getLocation());
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, false);
                            } else {
                                player.sendMessage("Invalid Residence.");
                            }
                            return true;
                        }
                        player.sendMessage("§cError, message to remove must be 'enter' or 'leave'.");
                        return true;
                    } else if (args[2].equals("remove")) {
                        res = rmanager.getByName(args[1]);
                        if (args.length != 4) {
                            return false;
                        }
                        if (args[3].equals("enter")) {
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, true);
                            }
                            return true;
                        } else if (args[3].equals("leave")) {
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, false);
                            }
                            return true;
                        }
                        player.sendMessage("§cError, message to remove must be 'enter' or 'leave'.");
                        return true;
                    } else {
                        player.sendMessage("§cMessage type must be either 'enter' or 'leave' or 'remove'");
                        return true;
                    }
                    String message = "";
                    for (int i = start; i < args.length; i++) {
                        message = message + args[i] + " ";
                    }
                    if (res != null) {
                        res.setEnterLeaveMessage(player, message, enter);
                    } else {
                        player.sendMessage("§cInvalid Area.");
                    }
                    return true;
                }
                else if(args[0].equals("give"))
                {
                    if(args.length!=3)
                    {
                        player.sendMessage("§cUsage: /res give <residence> <player>");
                        return true;
                    }
                    rmanager.giveResidence(player, args[2], args[1]);
                    return true;
                }
                player.sendMessage("§c/res ? for more info.");
            }
            return true;
        } else if (command.getName().equals("resadmin")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!gmanager.isResidenceAdmin(player)) {
                    player.sendMessage("§cYou dont have access to admin commands.");
                    return true;
                }
                if (args.length == 0 || (args.length == 1 && args[0].equals("?"))) {
                    player.sendMessage("§d----------Command Help:----------");
                    player.sendMessage("§b/resadmin§3 - additional admin residence commands.");
                    player.sendMessage("§alease set §6[residence] [#days/infinite]§3 - set a lease.");
                    player.sendMessage("§asetowner §6[residence] [player]§3 - change residence owner.");
                    player.sendMessage("§3Admins also have access to all the normal /res commands for any residence, regardless of permissions.  Admins are also immune to deny flags.");
                    return true;
                } else if (args.length == 4) {
                    if (args[0].equals("lease")) {
                        if (args[1].equals("set")) {
                            if (args[3].equals("infinite")) {
                                if (leasemanager.leaseExpires(args[2])) {
                                    leasemanager.removeExpireTime(args[2]);
                                    player.sendMessage("§aLease infinite!");
                                } else {
                                    player.sendMessage("§cInvalid area, or lease already infinite.");
                                }
                                return true;
                            } else {
                                int days;
                                try {
                                    days = Integer.parseInt(args[3]);
                                } catch (Exception ex) {
                                    player.sendMessage("§cInvalid number of days!");
                                    return true;
                                }
                                leasemanager.setExpireTime(player, args[2], days);
                                return true;
                            }
                        }
                    }
                } else if (args.length == 3) {
                    if (args[0].equals("setowner")) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setOwner(args[2], true);
                            player.sendMessage("§cOwner of §a" + args[1] + "§c set to §a" + args[2]);
                        } else {
                            player.sendMessage("§cInvalid residence.");
                        }
                        return true;
                    }
                    else if(args[0].equals("removeowner"))
                    {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setOwner("Server Owned", false);
                            player.sendMessage("§cOwner of §a" + args[1] + "§c set to §aServer Owned");
                        } else {
                            player.sendMessage("§cInvalid residence.");
                        }
                        return true;
                    }
                    else if(args[0].equals("clearflags"))
                    {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().clearFlags();
                            player.sendMessage("§aFlags Cleared.");
                        } else {
                            player.sendMessage("§cInvalid residence.");
                        }
                        return true;
                    }
                }
            }
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    private void saveYml() {

        YMLSaveHelper yml = new YMLSaveHelper(ymlSaveLoc);
        yml.getRoot().put("SaveVersion", saveVersion);
        yml.addMap("Residences", rmanager.save());
        yml.addMap("Economy", tmanager.save());
        yml.addMap("Leases", leasemanager.save());
        yml.addMap("PermissionLists", pmanager.save());
        yml.addMap("RentSystem", rentmanager.save());
        File backupFile = new File(ymlSaveLoc.getParentFile(), ymlSaveLoc.getName() + ".bak");
        if(ymlSaveLoc.isFile())
        {
            if(backupFile.isFile())
                backupFile.delete();
            ymlSaveLoc.renameTo(backupFile);
        }
        yml.save();
        System.out.println("[Residence] Saved Residences...");
    }

    private void loadYml() {
        if (ymlSaveLoc.isFile()) {
            YMLSaveHelper yml = new YMLSaveHelper(ymlSaveLoc);
            yml.load();
            int sv = yml.getInt("SaveVersion", 0);
            if(sv==0)
            {
                ymlSaveLoc.renameTo(new File(ymlSaveLoc.getParentFile(),"pre-upgrade-res.yml"));
                yml = upgradeSave(yml);
            }
            rmanager = ResidenceManager.load(yml.getMap("Residences"));
            tmanager = TransactionManager.load(yml.getMap("Economy"), gmanager, rmanager);
            leasemanager = LeaseManager.load(yml.getMap("Leases"), rmanager);
            pmanager = PermissionListManager.load(yml.getMap("PermissionLists"));
            rentmanager = RentManager.load(yml.getMap("RentSystem"));
            System.out.print("[Residence] Loaded Residences...");
        } else {
            System.out.println("[Residence] Save File not found...");
        }
    }

    private void writeDefaultConfigFromJar()
    {
        try {
            File configLoc = new File(this.getDataFolder(),"config.yml");
            File configBackup = new File(this.getDataFolder(),"old-config.yml");
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if(jarloc.isFile())
            {
                JarFile jar = new JarFile(jarloc);
                JarEntry entry = jar.getJarEntry("config.yml");
                if(entry!=null && !entry.isDirectory())
                {
                    InputStream in = jar.getInputStream(entry);
                    if(configLoc.isFile())
                    {
                        if(configBackup.isFile())
                            configBackup.delete();
                        configLoc.renameTo(configBackup);
                    }
                    FileOutputStream out = new FileOutputStream(configLoc);
                    byte[] tempbytes = new byte[512];
                    int readbytes = in.read(tempbytes,0,512);
                    while(readbytes>-1)
                    {
                        out.write(tempbytes,0,readbytes);
                        readbytes = in.read(tempbytes,0,512);
                    }
                    out.close();
                    in.close();
                    System.out.println("[Residence] Wrote default config...");
                    return;
                }
            }
            System.out.println("[Residence] Unable to write default config file!");
        } catch (Exception ex) {
            System.out.println("[Residence] Failed to write default config file:" + ex);
        }
    }

    public YMLSaveHelper upgradeSave(YMLSaveHelper yml) {
        try {
            Map<String, Object> root = yml.getRoot();
            Map<String, Object> resmap = (Map<String, Object>) root.get("residences");
            Map<String, Object> newmap = new HashMap<String, Object>();
            for (Entry<String, Object> entry : resmap.entrySet()) {
                Map<String, Object> resvals = (Map<String, Object>) entry.getValue();
                newmap.put(entry.getKey(), upgradeResidence(resvals));
            }
            Map<String,Object> newroot = new HashMap<String,Object>();
            newroot.put("Residences", newmap);
            newroot.put("Leases", root.get("leasetimes"));
            newroot.put("Economy", root.get("forsale"));
            newroot.put("PermissionLists", new HashMap<String,Object>());
            newroot.put("SaveVersion", 1);
            yml.setRoot(newroot);
            yml.save();
            System.out.print("[Residence] Upgraded Save File!");
            return yml;
        } catch (Exception ex) {
            System.out.println("[Residence] FAILED to upgrade save file...");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            return yml;
        }
    }

    public Map<String,Object> upgradeResidence(Map<String, Object> resvals) {
        Map<String,Object> newmap = new HashMap<String,Object>();
        Map<String, Object> areas = new HashMap<String, Object>();
        Map<String, Object> mainarea = new HashMap<String, Object>();
        mainarea.put("X1", resvals.get("x1"));
        mainarea.put("Y1", resvals.get("y1"));
        mainarea.put("Z1", resvals.get("z1"));
        mainarea.put("X2", resvals.get("x2"));
        mainarea.put("Y2", resvals.get("y2"));
        mainarea.put("Z2", resvals.get("z2"));
        areas.put("main", mainarea);
        newmap.put("Areas", areas);
        Map<String,Object> oldperms = (Map<String, Object>) resvals.get("permissions");
        Map<String,Object> perms = new HashMap<String,Object>();
        perms.put("AreaFlags", upgradeFlags((Map<String, Object>) oldperms.get("areaflags")));
        Map<String,Object> pflags = (Map<String, Object>) oldperms.get("playerflags");
        Map<String,Object> newpflags = new HashMap<String,Object>();
        for(Entry<String, Object> player : pflags.entrySet())
        {
            newpflags.put(player.getKey(), upgradeFlags((Map<String, Object>) player.getValue()));
        }
        perms.put("PlayerFlags", newpflags);
        Map<String,Object> gflags = (Map<String, Object>) oldperms.get("groupflags");
        Map<String,Object> newgflags = new HashMap<String,Object>();
        for(Entry<String, Object> group : gflags.entrySet())
        {
            newpflags.put(group.getKey(), upgradeFlags((Map<String, Object>) group.getValue()));
        }
        perms.put("GroupFlags", newgflags);
        perms.put("Owner", oldperms.get("owner"));
        perms.put("World", resvals.get("world"));
        newmap.put("Permissions", perms);
        newmap.put("EnterMessage", resvals.get("entermessage"));
        newmap.put("LeaveMessage", resvals.get("leavemessage"));
        Map<String,Object> sz = (Map<String, Object>) resvals.get("subzones");
        Map<String,Object> newsz = new HashMap<String,Object>();
        for(Entry<String, Object> entry : sz.entrySet())
        {
            newsz.put(entry.getKey(),upgradeResidence((Map<String, Object>) entry.getValue()));
        }
        newmap.put("Subzones", newsz);
        return newmap;
    }

    public Map<String,Object> upgradeFlags(Map<String,Object> inflags)
    {
        Map<String,Object> newmap = new HashMap<String,Object>();
        for(Entry<String, Object> flag : inflags.entrySet())
        {
            String flagname = flag.getKey();
            boolean flagval = (Boolean)flag.getValue();
            if(flagname.equals("fire"))
            {
                newmap.put("ignite", flagval);
                newmap.put("firespread", flagval);
            }
            else if(flagname.equals("explosions"))
            {
                newmap.put("creeper", flagval);
                newmap.put("tnt", flagval);
            }
            else if(flagname.equals("use"))
            {
                 newmap.put("use", flagval);
                 newmap.put("container", flagval);
            }
            else
            {
                newmap.put(flagname, flagval);
            }
        }
        return newmap;
    }
}
