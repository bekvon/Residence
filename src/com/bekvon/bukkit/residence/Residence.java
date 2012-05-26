/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.economy.BOSEAdapter;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.EssentialsEcoAdapter;
import com.bekvon.bukkit.residence.economy.IConomy5Adapter;
import com.bekvon.bukkit.residence.economy.IConomy6Adapter;
import com.bekvon.bukkit.residence.economy.RealShopEconomy;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.spout.ResidenceSpout;
import com.bekvon.bukkit.residence.spout.ResidenceSpoutListener;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import cosine.boseconomy.BOSEconomy;
import fr.crafter.tickleman.realeconomy.RealEconomy;
import fr.crafter.tickleman.realplugin.RealPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
//import net.minecraft.server.FontAllowedCharacters;
import net.minecraft.server.SharedConstants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;

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
    private static ResidenceSpoutListener slistener;
    private static TransactionManager tmanager;
    private static PermissionListManager pmanager;
    private static LeaseManager leasemanager;
    private static WorldItemManager imanager;
    private static WorldFlagManager wmanager;
    private static RentManager rentmanager;
    private static ChatManager chatmanager;
    private static Server server;
    private static HelpEntry helppages;
    private static Language language;
    private boolean firstenable = true;
    private static EconomyInterface economy;
    public final static int saveVersion = 1;
    private static File dataFolder;
    private static int leaseBukkitId=-1;
    private static int rentBukkitId=-1;
    private static int healBukkitId=-1;
    private static int autosaveBukkitId=-1;
    private static boolean initsuccess = false;
    private Map<String,String> deleteConfirm;
    private Runnable doHeals = new Runnable() {
        public void run() {
            plistener.doHeals();
        }
    };
    private Runnable rentExpire = new Runnable()
    {
        public void run() {
            rentmanager.checkCurrentRents();
            if(cmanager.showIntervalMessages())
                System.out.println("[Residence] - Rent Expirations checked!");
        }
    };
    private Runnable leaseExpire = new Runnable() {
        public void run() {
            leasemanager.doExpirations();
            if(cmanager.showIntervalMessages())
                System.out.println("[Residence] - Lease Expirations checked!");
        }
    };
    private Runnable autoSave = new Runnable() {
        public void run() {
            try {
                saveYml();
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
        }
    };

    public Residence() {
    }

    public void reloadPlugin()
    {
        this.setEnabled(false);
        this.setEnabled(true);
    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        server.getScheduler().cancelTask(healBukkitId);
        if (cmanager.useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        if(cmanager.enabledRentSystem())
        {
            server.getScheduler().cancelTask(rentBukkitId);
        }
        if(initsuccess)
        {
            try {
                saveYml();
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Disabled!");
        }
    }

    @Override
    public void onEnable() {
        try {
            initsuccess = false;
            deleteConfirm = new HashMap<String, String>();
            server = this.getServer();
            dataFolder = this.getDataFolder();
            if (!dataFolder.isDirectory()) {
                dataFolder.mkdirs();
            }

            if (!new File(dataFolder, "config.yml").isFile()) {
                this.writeDefaultConfigFromJar();
            }
            if(this.getConfig().getInt("ResidenceVersion", 0) == 0)
            {
                this.writeDefaultConfigFromJar();
                this.getConfig().load("config.yml");
                System.out.println("[Residence] Config Invalid, wrote default...");
            }
            cmanager = new ConfigManager(this.getConfig());
            String multiworld = cmanager.getMultiworldPlugin();
            if (multiworld != null) {
                Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                if (plugin != null) {
                    if (!plugin.isEnabled()) {
                        System.out.println("[Residence] - Enabling multiworld plugin: " + multiworld);
                        server.getPluginManager().enablePlugin(plugin);
                    }
                }
            }
            gmanager = new PermissionManager(this.getConfig());
            imanager = new WorldItemManager(this.getConfig());
            wmanager = new WorldFlagManager(this.getConfig());
            chatmanager = new ChatManager();
            rentmanager = new RentManager();
            try
            {
                File langFile = new File(new File(dataFolder, "Language"), cmanager.getLanguage() + ".yml");
                if(this.checkNewLanguageVersion())
                    this.writeDefaultLanguageFile();
                if(langFile.isFile())
                {
                    FileConfiguration langconfig = new YamlConfiguration();
                    langconfig.load(langFile);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                    HelpEntry.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    language = Language.parseText(langconfig, "Language");
                }
                else
                    System.out.println("[Residence] Language file does not exist...");
            }
            catch (Exception ex)
            {
                System.out.println("[Residence] Failed to load language file: " + cmanager.getLanguage() + ".yml, Error: " + ex.getMessage());
                Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                helppages = new HelpEntry("");
                language = new Language();
            }
            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                    System.out.println("[Residence] Scanning for economy systems...");
                    if(gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter)
                    {
                        ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
                        if(vault.economyOK())
                        {
                            economy = vault;
                            System.out.println("[Residence] Found Vault using economy system: " + vault.getEconomyName());
                        }
                    }
                    if(economy == null)
                        this.loadVaultEconomy();
                    if(economy == null)
                        this.loadBOSEconomy();
                    if(economy == null)
                        this.loadEssentialsEconomy();
                    if(economy == null)
                        this.loadRealEconomy();
                    if(economy == null)
                        this.loadIConomy();
                    if(economy == null)
                        System.out.println("[Residence] Unable to find an economy system...");
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
            if (firstenable) {
                if(!this.isEnabled())
                    return;
                FlagPermissions.initValidFlags();
                smanager = new SelectionManager(server);
                blistener = new ResidenceBlockListener();
                plistener = new ResidencePlayerListener();
                elistener = new ResidenceEntityListener();
                PluginManager pm = getServer().getPluginManager();
                pm.registerEvents(blistener, this);
                pm.registerEvents(plistener, this);
                pm.registerEvents(elistener, this);

                //pm.registerEvent(Event.Type.WORLD_LOAD, wlistener, Priority.NORMAL, this);
                if(cmanager.enableSpout())
                {
                    slistener = new ResidenceSpoutListener();
                    pm.registerEvents(slistener, this);
                }
                firstenable = false;
            }
            else
            {
                plistener.reload();
            }
            int autosaveInt = cmanager.getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = (autosaveInt * 60) * 20;
            autosaveBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);
            healBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doHeals, 20, 20);
            if (cmanager.useLeases()) {
                int leaseInterval = cmanager.getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = (leaseInterval * 60) * 20;
                leaseBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            if(cmanager.enabledRentSystem())
            {
                int rentint = cmanager.getRentCheckInterval();
                if(rentint < 1)
                    rentint = 1;
                rentint = (rentint * 60) * 20;
                rentBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, rentExpire, rentint, rentint);
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Enabled! Version " + this.getDescription().getVersion() + " by bekvon");
            initsuccess = true;
        } catch (Exception ex) {
            initsuccess = false;
            getServer().getPluginManager().disablePlugin(this);
            System.out.println("[Residence] - FAILED INITIALIZATION! DISABLED! ERROR:");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean validName(String name)
    {
        if(name.contains(":") || name.contains("."))
            return false;
        if(cmanager.getResidenceNameRegex() == null)
        {
        	return true;
        }else{
	        String namecheck = name.replaceAll(cmanager.getResidenceNameRegex(), "");
	        if(!name.equals(namecheck))
	            return false;
	        return Residence.validString(name);
        }
    }

    public static boolean validString(String string)
    {
        for(int i = 0; i < string.length(); i++)
        {
            if(SharedConstants.allowedCharacters.indexOf(string.charAt(i)) < 0)
            {
                return false;
            }
        }
        return true;
    }

    public static File getDataLocation()
    {
        return dataFolder;
    }

    public static ResidenceManager getResidenceManager() {
        return rmanager;
    }

    @Deprecated
    public static ResidenceManager getResidenceManger()
    {
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

    public static ConfigManager getConfigManager() {
        return cmanager;
    }

    public static TransactionManager getTransactionManager() {
        return tmanager;
    }

    public static WorldItemManager getItemManager()
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

    public static ResidencePlayerListener getPlayerListener()
    {
        return plistener;
    }

    public static ResidenceBlockListener getBlockListener()
    {
        return blistener;
    }

    public static ResidenceEntityListener getEntityListener()
    {
        return elistener;
    }

    public static ChatManager getChatManager()
    {
        return chatmanager;
    }

    public static Language getLanguage()
    {
        if(language==null)
            language = new Language();
        return language;
    }

    public static FlagPermissions getPermsByLoc(Location loc)
    {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if(res!=null)
            return res.getPermissions();
        else
            return wmanager.getPerms(loc.getWorld().getName());
    }

    private void loadIConomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("iConomy");
        if (p != null) {
            if(p.getDescription().getVersion().startsWith("6"))
            {
                economy = new IConomy6Adapter((com.iCo6.iConomy)p);
            }
            else if(p.getDescription().getVersion().startsWith("5"))
            {
                economy = new IConomy5Adapter();
            }
            else
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] UNKNOWN iConomy version!");
                return;
            }
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with iConomy! Version: " + p.getDescription().getVersion());
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] iConomy NOT found!");
        }
    }

    private void loadBOSEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("BOSEconomy");
        if (p != null) {
            economy = new BOSEAdapter((BOSEconomy)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with BOSEconomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] BOSEconomy NOT found!");
        }
    }

    private void loadEssentialsEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("Essentials");
        if (p != null) {
            economy = new EssentialsEcoAdapter((Essentials)p);
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with Essentials Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Essentials Economy NOT found!");
        }
    }

    private void loadRealEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("RealPlugin");
        if (p != null) {
            economy = new RealShopEconomy(new RealEconomy((RealPlugin)p));
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with RealShop Economy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] RealShop Economy NOT found!");
        }
    }

    private void loadVaultEconomy()
    {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if(p!=null)
        {
            ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
            if(vault.economyOK())
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault using economy: " + vault.getEconomyName());
                economy = vault;
            }
            else
            {
                Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Found Vault, but Vault reported no usable economy system...");
            }
        } else
        {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Vault NOT found!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(),args,sender);
        server.getPluginManager().callEvent(cevent);
        if(cevent.isCancelled())
            return true;
        if(command.getName().equals("resreload") && args.length==0)
        {
            if(sender instanceof Player)
            {
                Player player = (Player) sender;
                if(gmanager.isResidenceAdmin(player))
                {
                    this.reloadPlugin();
                    System.out.println("[Residence] Reloaded by "+player.getName()+".");
                }
            }
            else
            {
                this.reloadPlugin();
                System.out.println("[Residence] Reloaded by console.");
            }
            return true;
        }
        if(command.getName().equals("resload"))
        {
            if(!(sender instanceof Player) || (sender instanceof Player && gmanager.isResidenceAdmin((Player) sender)))
            {
                try {
                    this.loadYml();
                    sender.sendMessage(ChatColor.GREEN+"[Residence] Reloaded save file...");
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED+"[Residence] Unable to reload the save file, exception occured!");
                    sender.sendMessage(ChatColor.RED+ex.getMessage());
                    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return true;
        }
        else if(command.getName().equals("resworld"))
        {
            if(args.length == 2 && args[0].equalsIgnoreCase("remove"))
            {
                if(sender instanceof ConsoleCommandSender)
                {
                    rmanager.removeAllFromWorld(sender, args[1]);
                    return true;
                }
                else
                    sender.sendMessage(ChatColor.RED+"MUST be run from console.");
            }
            return false;
        }
        else if(command.getName().equals("rc"))
        {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String pname = player.getName();
                if(cmanager.chatEnabled())
                {
                    if(args.length==0)
                    {
                        plistener.tooglePlayerResidenceChat(player);
                    }
                    else
                    {
                        String area = plistener.getLastAreaName(pname);
                        if(area!=null)
                        {
                            ChatChannel channel = chatmanager.getChannel(area);
                            if(channel!=null)
                            {
                                String message="";
                                for(String arg : args)
                                {
                                    message = message + " " + arg;
                                }
                                channel.chat(pname, message);
                            }
                            else
                            {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidChannel"));
                            }
                        }
                        else
                        {
                            player.sendMessage(ChatColor.RED + language.getPhrase("NotInResidence"));
                        }
                    }
                }
                else
                    player.sendMessage(ChatColor.RED + language.getPhrase("ChatDisabled"));
            }
            return true;
        }
        else if(command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
            if ((args.length > 0 && args[args.length - 1].equalsIgnoreCase("?")) || (args.length > 1 && args[args.length - 2].equals("?"))) {
                if (helppages != null) {
                    String helppath = "res";
                    for (int i = 0; i < args.length; i++) {
                        if (args[i].equalsIgnoreCase("?")) {
                            break;
                        }
                        helppath = helppath + "." + args[i];
                    }
                    int page = 1;
                    if (!args[args.length - 1].equalsIgnoreCase("?")) {
                        try {
                            page = Integer.parseInt(args[args.length - 1]);
                        } catch (Exception ex) {
                            sender.sendMessage(ChatColor.RED+language.getPhrase("InvalidHelp"));
                        }
                    }
                    if (helppages.containesEntry(helppath)) {
                        helppages.printHelp(sender, page, helppath);
                        return true;
                    }
                }
            }
            int page = 1;
            try{
                if(args.length>0)
                    page = Integer.parseInt(args[args.length-1]);
            }catch(Exception ex){}
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                String pname = player.getName();
                boolean resadmin = false;
                if(command.getName().equals("resadmin"))
                {
                    resadmin = gmanager.isResidenceAdmin(player);
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED + language.getPhrase("NonAdmin"));
                        return true;
                    }
                }
                if (cmanager.allowAdminsOnly()) {
                    if (!resadmin) {
                        player.sendMessage(ChatColor.RED+language.getPhrase("AdminOnly"));
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
                    if (!group.selectCommandAccess() && !resadmin) {
                        player.sendMessage(ChatColor.RED + language.getPhrase("SelectDiabled"));
                        return true;
                    }
                    if (!group.canCreateResidences() && group.getMaxSubzoneDepth() <= 0 && !resadmin) {
                        player.sendMessage(ChatColor.RED + language.getPhrase("SelectDiabled"));
                        return true;
                    }
                    if (args.length == 2) {
                        if (args[1].equals("size") || args[1].equals("cost")) {
                            if (smanager.hasPlacedBoth(pname)) {
                                try {
                                    smanager.showSelectionInfo(player);
                                    return true;
                                } catch (Exception ex) {
                                    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                                    return true;
                                }
                            }
                        } else if (args[1].equals("vert")) {
                            smanager.vert(player, resadmin);
                            return true;
                        } else if (args[1].equals("sky")) {
                            smanager.sky(player, resadmin);
                            return true;
                        } else if (args[1].equals("bedrock")) {
                            smanager.bedrock(player, resadmin);
                            return true;
                        } else if (args[1].equals("coords")) {
                            Location playerLoc1 = smanager.getPlayerLoc1(pname);
                            if (playerLoc1 != null) {
                                player.sendMessage(ChatColor.GREEN + language.getPhrase("Primary.Selection") + ":"+ChatColor.AQUA+" (" + playerLoc1.getBlockX() + ", " + playerLoc1.getBlockY() + ", " + playerLoc1.getBlockZ() + ")");
                            }
                            Location playerLoc2 = smanager.getPlayerLoc2(pname);
                            if (playerLoc2 != null) {
                                player.sendMessage(ChatColor.GREEN + language.getPhrase("Secondary.Selection") + ":"+ChatColor.AQUA+" (" + playerLoc2.getBlockX() + ", " + playerLoc2.getBlockY() + ", " + playerLoc2.getBlockZ() + ")");
                            }
                            return true;
                        } else if (args[1].equals("chunk")) {
                            smanager.selectChunk(player);
                            return true;
                        } else if (args[1].equals("worldedit")) {
                        	smanager.worldEdit(player);
                        	return true;
                        }
                    } else if (args.length == 3) {
                        if (args[1].equals("expand")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (Exception ex) {
                                player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
                                return true;
                            }
                            smanager.modify(player, false, amount);
                            return true;
                        } else if (args[1].equals("shift")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[2]);
                            } catch (Exception ex) {
                                player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
                                return true;
                            }
                            smanager.modify(player, true, amount);
                            return true;
                        }
                    }
                    if(args.length>1 && args[1].equals("residence")) {
                        ClaimedResidence res = rmanager.getByName(args[2]);
                        if (res == null) {
                            player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        CuboidArea area = res.getArea(args[3]);
                        if (area != null) {
                            smanager.placeLoc1(pname, area.getHighLoc());
                            smanager.placeLoc2(pname, area.getLowLoc());
                            player.sendMessage(ChatColor.GREEN + language.getPhrase("SelectionArea", ChatColor.GOLD + args[3] + ChatColor.GREEN+"."+ChatColor.GOLD + args[2] + ChatColor.GREEN));
                        } else {
                            player.sendMessage(ChatColor.RED + language.getPhrase("AreaNonExist"));
                        }
                        return true;
                    } else {
                        try {
                            smanager.selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                            return true;
                        } catch (Exception ex) {
                            player.sendMessage(ChatColor.RED + language.getPhrase("SelectionFail"));
                            return true;
                        }
                    }
                } else if (args[0].equals("create")) {
                    if (args.length != 2) {
                        return false;
                    }
                    if (smanager.hasPlacedBoth(pname)) {
                        rmanager.addResidence(player, args[1], smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname), resadmin);
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
                        return true;
                    }
                } else if (args[0].equals("subzone") || args[0].equals("sz")) {
                    if (args.length != 2 && args.length != 3) {
                        return false;
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
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        res.addSubzone(player, smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname), zname, resadmin);
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("SelectPoints"));
                        return true;
                    }
                } else if(args[0].equals("gui"))
                {
                    if(slistener!=null)
                    {
                        if(args.length==1)
                            ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, rmanager.getNameByLoc(player.getLocation()), resadmin);
                        else if(args.length==2)
                            ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, args[1], resadmin);
                    }
                    return true;
                }
                else if (args[0].equals("sublist")) {
                    if(args.length==1 || args.length == 2 || args.length == 3)
                    {
                        ClaimedResidence res;
                        if(args.length==1)
                            res = rmanager.getByLoc(player.getLocation());
                        else
                            res = rmanager.getByName(args[1]);
                        if(res!=null)
                            res.printSubzoneList(player, page);
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                }
                else if (args[0].equals("remove") || args[0].equals("delete")) {
                    if (args.length == 1) {
                        String area = rmanager.getNameByLoc(player.getLocation());
                        if (area != null) {                       	
                            ClaimedResidence res = rmanager.getByName(area);
                            if (res.getParent() != null ){
                            	String words = area.split("//.")[(area.split("//.").length-1)];         	
                                if (!deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
                                    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteSubzoneConfirm", (ChatColor.YELLOW + words + ChatColor.RED)));
                                    deleteConfirm.put(player.getName(), area);
                                } else {
                                    rmanager.removeResidence(player, area, resadmin);
                                }
                                return true;
                            }else{
                                if (!deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
                                    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteConfirm", (ChatColor.YELLOW + area + ChatColor.RED)));
                                    deleteConfirm.put(player.getName(), area);
                                } else {
                                    rmanager.removeResidence(player, area, resadmin);
                                }
                                return true;
                            }
                        }                   
                        return false;
                    }
                    if (args.length != 2) {
                        return false;
                    }
                    if (!deleteConfirm.containsKey(player.getName()) || !args[1].equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
                    	String words = args[1].split("//.")[(args[1].split("//.").length-1)]; 
                    	if(words==null){
                            player.sendMessage(ChatColor.RED + language.getPhrase("DeleteConfirm", (ChatColor.YELLOW + args[1] + ChatColor.RED)));
                    	}else{
                    		player.sendMessage(ChatColor.RED + language.getPhrase("DeleteSubzoneConfirm", (ChatColor.YELLOW + words + ChatColor.RED)));
                    	}
                        deleteConfirm.put(player.getName(), args[1]);
                    } else {
                        rmanager.removeResidence(player, args[1], resadmin);
                    }
                    return true;
                }
                else if(args[0].equalsIgnoreCase("confirm"))
                {
                    if(args.length == 1)
                    {
                        String area = deleteConfirm.get(player.getName());
                        if(area==null)
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        }
                        else
                        {
                            rmanager.removeResidence(player, area, resadmin);
                            deleteConfirm.remove(player.getName());
                        }
                    }
                    return true;
                }
                else if (args[0].equalsIgnoreCase("removeall"))
                {
                    if(args.length!=2)
                        return false;
                    if(resadmin || args[1].endsWith(pname))
                    {
                        rmanager.removeAllByOwner(args[1]);
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("RemovePlayersResidences",ChatColor.YELLOW+args[1]+ChatColor.GREEN));
                    }
                    else
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                    }
                    return true;
                }
                else if (args[0].equals("area")) {
                    if (args.length == 4) {
                        if (args[1].equals("remove")) {
                            ClaimedResidence res = rmanager.getByName(args[2]);
                            if(res!=null)
                                res.removeArea(player, args[3], resadmin);
                            else
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        } else if (args[1].equals("add")) {
                            if (smanager.hasPlacedBoth(pname)) {
                                ClaimedResidence res = rmanager.getByName(args[2]);
                                if(res != null)
                                    res.addArea(player, new CuboidArea(smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname)),args[3], resadmin);
                                else
                                    player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            } else {
                                player.sendMessage(ChatColor.RED+language.getPhrase("SelectPoints"));
                            }
                            return true;
                        } else if (args[1].equals("replace")) {
                            if (smanager.hasPlacedBoth(pname)) {
                                ClaimedResidence res = rmanager.getByName(args[2]);
                                if(res != null)
                                    res.replaceArea(player, new CuboidArea(smanager.getPlayerLoc1(pname), smanager.getPlayerLoc2(pname)),args[3], resadmin);
                                else
                                    player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            } else {
                                player.sendMessage(ChatColor.RED+language.getPhrase("SelectPoints"));
                            }
                            return true;
                        }
                    }
                    if ((args.length == 3 || args.length == 4) && args[1].equals("list")) {
                        ClaimedResidence res = rmanager.getByName(args[2]);
                        if (res != null) {
                            res.printAreaList(player, page);
                        } else {
                            player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
                        }
                        return true;
                    }
                    else if((args.length == 3 || args.length == 4) && args[1].equals("listall"))
                    {
                        ClaimedResidence res = rmanager.getByName(args[2]);
                        if (res != null) {
                            res.printAdvancedAreaList(player, page);
                        } else {
                            player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
                        }
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
                            pmanager.applyListToResidence(player, args[2], args[3], resadmin);
                            return true;
                        }
                    }
                    else if  (args.length==5)
                    {
                        if(args[1].equals("set"))
                        {
                            pmanager.getList(pname, args[2]).setFlag(args[3], FlagPermissions.stringToFlagState(args[4]));
                            player.sendMessage(ChatColor.GREEN+language.getPhrase("FlagSet"));
                            return true;
                        }
                    }
                    else if(args.length==6)
                    {
                        if(args[1].equals("gset"))
                        {
                            pmanager.getList(pname, args[2]).setGroupFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
                            player.sendMessage(ChatColor.GREEN+language.getPhrase("FlagSet"));
                            return true;
                        }
                        else if(args[1].equals("pset"))
                        {
                            pmanager.getList(pname, args[2]).setPlayerFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
                            player.sendMessage(ChatColor.GREEN+language.getPhrase("FlagSet"));
                            return true;
                        }
                    }
                } else if (args[0].equals("default")) {
                    if (args.length == 2) {
                        ClaimedResidence res = rmanager.getByName(args[1]);
                        res.getPermissions().applyDefaultFlags(player, resadmin);
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
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        }
                        return true;
                    } else if (args.length == 2) {
                        rmanager.printAreaInfo(args[1], player);
                        return true;
                    }
                }
                else if(args[0].equals("check"))
                {
                    if(args.length == 3 || args.length == 4)
                    {
                        if(args.length == 4)
                        {
                            pname = args[3];
                        }
                        ClaimedResidence res = rmanager.getByName(args[1]);
                        if(res==null)
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        if(!res.getPermissions().hasApplicableFlag(pname, args[2]))
                            player.sendMessage(language.getPhrase("FlagCheckFalse",ChatColor.YELLOW + args[2] + ChatColor.RED+"."+ChatColor.YELLOW + pname +ChatColor.RED+"."+ChatColor.YELLOW+args[1]+ChatColor.RED));
                        else
                            player.sendMessage(language.getPhrase("FlagCheckTrue",ChatColor.GREEN+args[2]+ChatColor.YELLOW+"."+ChatColor.GREEN+pname+ChatColor.YELLOW+"."+ChatColor.YELLOW+args[1]+ChatColor.RED+"."+(res.getPermissions().playerHas(pname, res.getPermissions().getWorld(), args[2], false) ? ChatColor.GREEN+"TRUE" : ChatColor.RED+"FALSE")));
                        return true;
                    }
                }
                else if (args[0].equals("current")) {
                    if(args.length!=1)
                        return false;
                    String res = rmanager.getNameByLoc(player.getLocation());
                    if(res==null)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NotInResidence"));
                    }
                    else
                    {
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("InResidence",ChatColor.YELLOW + res + ChatColor.GREEN));
                    }
                    return true;
                } else if (args[0].equals("set")) {
                    if (args.length == 3) {
                        String area = rmanager.getNameByLoc(player.getLocation());
                        if (area != null) {
                            rmanager.getByName(area).getPermissions().setFlag(player, args[1], args[2], resadmin);
                        } else {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        }
                        return true;
                    } else if (args.length == 4) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if(area!=null)
                        {
                            area.getPermissions().setFlag(player, args[2], args[3], resadmin);
                        }
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                } else if (args[0].equals("pset")) {
                    if (args.length == 3 && args[2].equalsIgnoreCase("removeall"))
                    {
                        ClaimedResidence area = rmanager.getByLoc(player.getLocation());
                        if(area!=null)
                            area.getPermissions().removeAllPlayerFlags(player, args[1], resadmin);
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                    else if(args.length == 4 && args[3].equalsIgnoreCase("removeall"))
                    {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().removeAllPlayerFlags(player, args[2], resadmin);
                        }
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                    else if(args.length == 4) {
                        ClaimedResidence area = rmanager.getByLoc(player.getLocation());
                        if (area != null) {
                            area.getPermissions().setPlayerFlag(player, args[1], args[2], args[3], resadmin);
                        } else {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        }
                        return true;
                    } else if (args.length == 5) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setPlayerFlag(player, args[2], args[3], args[4], resadmin);
                        }
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                } else if (args[0].equals("gset")) {
                    if (args.length == 4) {
                        ClaimedResidence area = rmanager.getByLoc(player.getLocation());
                        if (area != null) {
                            area.getPermissions().setGroupFlag(player, args[1], args[2], args[3], resadmin);

                        } else {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidArea"));
                        }
                        return true;
                    } else if (args.length == 5) {
                        ClaimedResidence area = rmanager.getByName(args[1]);
                        if (area != null) {
                            area.getPermissions().setGroupFlag(player, args[2], args[3], args[4], resadmin);
                        }
                        else
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                }
                else if(args[0].equals("lset"))
                {
                    ClaimedResidence res = null;
                    Material mat = null;
                    String listtype = null;
                    boolean showinfo = false;
                    if (args.length == 2 && args[1].equals("info")) {
                        res = rmanager.getByLoc(player.getLocation());
                        showinfo = true;
                    }
                    else if(args.length == 3 && args[2].equals("info")) {
                        res = rmanager.getByName(args[1]);
                        showinfo = true;
                    }
                    if (showinfo) {
                        if (res == null) {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        player.sendMessage(ChatColor.RED+"Blacklist:");
                        res.getItemBlacklist().printList(player);
                        player.sendMessage(ChatColor.GREEN+"Ignorelist:");
                        res.getItemIgnoreList().printList(player);
                        return true;
                    }
                    else if(args.length == 4)
                    {
                        res = rmanager.getByName(args[1]);
                        listtype = args[2];
                        try
                        {
                            mat = Material.valueOf(args[3].toUpperCase());
                        }
                        catch (Exception ex)
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMaterial"));
                            return true;
                        }
                    }
                    else if(args.length==3)
                    {
                        res = rmanager.getByLoc(player.getLocation());
                        listtype = args[1];
                        try
                        {
                            mat = Material.valueOf(args[2].toUpperCase());
                        }
                        catch (Exception ex)
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMaterial"));
                            return true;
                        }
                    }
                    if(res!=null)
                    {
                        if(listtype.equalsIgnoreCase("blacklist"))
                        {
                            res.getItemBlacklist().playerListChange(player, mat, resadmin);
                        }
                        else if(listtype.equalsIgnoreCase("ignorelist"))
                        {
                            res.getItemIgnoreList().playerListChange(player, mat, resadmin);
                        }
                        else
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidList"));
                        }
                        return true;
                    }
                    else
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                }
                else if (args[0].equals("list")) {
                    if(args.length == 1)
                    {
                        rmanager.listResidences(player);
                        return true;
                    }
                    else if (args.length == 2) {
                        try {
                            Integer.parseInt(args[1]);
                            rmanager.listResidences(player, page);
                        } catch (Exception ex) {
                            rmanager.listResidences(player, args[1]);
                        }
                        return true;
                    }
                    else if(args.length == 3)
                    {
                        rmanager.listResidences(player, args[1], page);
                        return true;
                    }
                }
                else if (args[0].equals("listhidden")) {
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    if(args.length == 1)
                    {
                        rmanager.listResidences(player,1,true);
                        return true;
                    }
                    else if (args.length == 2) {
                        try {
                            Integer.parseInt(args[1]);
                            rmanager.listResidences(player, page, true);
                        } catch (Exception ex) {
                            rmanager.listResidences(player, args[1],1, true);
                        }
                        return true;
                    }
                    else if(args.length == 3)
                    {
                        rmanager.listResidences(player, args[1], page, true);
                        return true;
                    }
                }
                else if(args[0].equals("rename"))
                {
                    if(args.length==3)
                    {
                        rmanager.renameResidence(player, args[1], args[2], resadmin);
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
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        res.renameArea(player, args[2], args[3], resadmin);
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
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
                    if (res == null) {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NotInResidence"));
                    } else {
                        player.sendMessage(ChatColor.YELLOW+language.getPhrase("Moved")+"...");
                        player.teleport(res.getOutsideFreeLoc(player.getLocation()));
                    }
                    return true;
                } else if (args[0].equals("mirror")) {
                    if (args.length != 3) {
                        return false;
                    }
                    rmanager.mirrorPerms(player, args[2], args[1], resadmin);
                    return true;
                } else if (args[0].equals("listall")) {
                    if (args.length == 1) {
                        rmanager.listAllResidences(player, 1);
                    } else if (args.length == 2) {
                        try {
                            rmanager.listAllResidences(player, page);
                        } catch (Exception ex) {
                        }
                    } else {
                        return false;
                    }
                    return true;
                } else if(args[0].equals("listallhidden"))
                {
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    if (args.length == 1) {
                        rmanager.listAllResidences(player, 1, true);
                    } else if (args.length == 2) {
                        try {
                            rmanager.listAllResidences(player, page, true);
                        } catch (Exception ex) {
                        }
                    } else {
                        return false;
                    }
                    return true;
                } else if (args[0].equals("version")) {
                    player.sendMessage(ChatColor.GRAY+"------------------------------------");
                    player.sendMessage(ChatColor.RED+"This server running "+ChatColor.GOLD+"Residence"+ChatColor.RED+" version: "+ChatColor.BLUE + this.getDescription().getVersion());
                    player.sendMessage(ChatColor.GREEN+"Created by: "+ChatColor.YELLOW+"bekvon");
                    player.sendMessage(ChatColor.DARK_AQUA+"For a command list, and help, see the wiki:");
                    player.sendMessage(ChatColor.GREEN+"http://residencebukkitmod.wikispaces.com/");
                    player.sendMessage(ChatColor.AQUA+"Visit the Residence thread at:");
                    player.sendMessage(ChatColor.BLUE+"http://forums.bukkit.org/");
                    player.sendMessage(ChatColor.GRAY+"------------------------------------");
                    return true;
                }
                else if(args[0].equals("material"))
                {
                    if(args.length!=2)
                        return false;
                    try
                    {
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("GetMaterial",ChatColor.GOLD + args[1] + ChatColor.GREEN+"."+ChatColor.RED + Material.getMaterial(Integer.parseInt(args[1])).name()+ChatColor.GREEN));
                    }
                    catch (Exception ex)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMaterial"));
                    }
                    return true;
                }
                else if (args[0].equals("tpset")) {
                    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
                    if (res != null) {
                        res.setTpLoc(player, resadmin);
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                    }
                    return true;
                } else if (args[0].equals("tp")) {
                    if (args.length != 2) {
                        return false;
                    }
                    ClaimedResidence res = rmanager.getByName(args[1]);
                    if (res == null) {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                        return true;
                    }
                    res.tpToResidence(player, player, resadmin);
                    return true;
                } else if (args[0].equals("lease")) {
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
                                ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
                                if (res == null || leasemanager.leaseExpires(args[2])) {
                                    int cost = leasemanager.getRenewCost(res);
                                    player.sendMessage(ChatColor.YELLOW+language.getPhrase("LeaseRenewalCost",ChatColor.RED + args[2] + ChatColor.YELLOW+"."+ChatColor.RED + cost + ChatColor.YELLOW));
                                } else {
                                    player.sendMessage(ChatColor.RED+language.getPhrase("LeaseNotExpire"));
                                }
                                return true;
                            } else {
                                String area = rmanager.getNameByLoc(player.getLocation());
                                ClaimedResidence res = rmanager.getByName(area);
                                if (area == null || res == null) {
                                    player.sendMessage(ChatColor.RED+language.getPhrase("InvalidArea"));
                                    return true;
                                }
                                if (leasemanager.leaseExpires(area)) {
                                    int cost = leasemanager.getRenewCost(res);
                                    player.sendMessage(ChatColor.YELLOW+language.getPhrase("LeaseRenewalCost",ChatColor.RED + area + ChatColor.YELLOW+"."+ChatColor.RED + cost + ChatColor.YELLOW));
                                } else {
                                    player.sendMessage(ChatColor.RED+language.getPhrase("LeaseNotExpire"));
                                }
                                return true;
                            }
                        }
                    } else if (args.length == 4) {
                        if (args[1].equals("set")) {
                            if (!resadmin) {
                                player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
                                return true;
                            }
                            if (args[3].equals("infinite")) {
                                if (leasemanager.leaseExpires(args[2])) {
                                    leasemanager.removeExpireTime(args[2]);
                                    player.sendMessage(ChatColor.GREEN + language.getPhrase("LeaseInfinite"));
                                } else {
                                    player.sendMessage(ChatColor.RED + language.getPhrase("LeaseNotExpire"));
                                }
                                return true;
                            } else {
                                int days;
                                try {
                                    days = Integer.parseInt(args[3]);
                                } catch (Exception ex) {
                                    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidDays"));
                                    return true;
                                }
                                leasemanager.setExpireTime(player, args[2], days);
                                return true;
                            }
                        }
                    }
                    return false;
                } else if(args[0].equals("bank")) {
                    if(args.length!=3)
                        return false;
                    ClaimedResidence res = rmanager.getByName(plistener.getLastAreaName(pname));
                    if(res==null)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NotInResidence"));
                        return true;
                    }
                    int amount = 0;
                    try
                    {
                        amount = Integer.parseInt(args[2]);
                    }
                    catch (Exception ex)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidAmount"));
                        return true;
                    }
                    if(args[1].equals("deposit"))
                    {
                        res.getBank().deposit(player, amount, resadmin);
                    }
                    else if(args[1].equals("withdraw"))
                    {
                        res.getBank().withdraw(player, amount, resadmin);
                    }
                    else
                        return false;
                    return true;
                } else if (args[0].equals("market")) {
                    if(args.length == 1)
                        return false;
                    if(args[1].equals("list"))
                    {
                        if(!cmanager.enableEconomy())
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("MarketDisabled"));
                            return true;
                        }
                        player.sendMessage(ChatColor.BLUE+"---"+language.getPhrase("MarketList")+"---");
                        tmanager.printForSaleResidences(player);
                        if(cmanager.enabledRentSystem())
                        {
                            rentmanager.printRentableResidences(player);
                        }
                        return true;
                    }
                    else if (args[1].equals("autorenew")) {
                        if (!cmanager.enableEconomy()) {
                            player.sendMessage(ChatColor.RED+language.getPhrase("MarketDisabled"));
                            return true;
                        }
                        if (args.length != 4) {
                            return false;
                        }
                        boolean value;
                        if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t")) {
                            value = true;
                        } else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")) {
                            value = false;
                        } else {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidBoolean"));
                            return true;
                        }
                        if(rentmanager.isRented(args[2]) && rentmanager.getRentingPlayer(args[2]).equalsIgnoreCase(pname))
                        {
                            rentmanager.setRentedRepeatable(player, args[2], value, resadmin);
                        }
                        else if(rentmanager.isForRent(args[2]))
                        {
                            rentmanager.setRentRepeatable(player, args[2], value, resadmin);
                        }
                        else
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("RentReleaseInvalid",ChatColor.YELLOW + args[2] + ChatColor.RED));
                        }
                        return true;
                    }
                    else if(args[1].equals("rentable")) {
                        if (args.length < 5 || args.length > 6) {
                            return false;
                        }
                        if (!cmanager.enabledRentSystem()) {
                            player.sendMessage(ChatColor.RED + language.getPhrase("RentDisabled"));
                            return true;
                        }
                        int days;
                        int cost;
                        try {
                            cost = Integer.parseInt(args[3]);
                        } catch (Exception ex) {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidCost"));
                            return true;
                        }
                        try {
                            days = Integer.parseInt(args[4]);
                        } catch (Exception ex) {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidDays"));
                            return true;
                        }
                        boolean repeat = false;
                        if (args.length == 6) {
                            if (args[5].equalsIgnoreCase("t") || args[5].equalsIgnoreCase("true")) {
                                repeat = true;
                            } else if (!args[5].equalsIgnoreCase("f") && !args[5].equalsIgnoreCase("false")) {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidBoolean"));
                                return true;
                            }
                        }
                        rentmanager.setForRent(player, args[2], cost, days, repeat, resadmin);
                        return true;
                    }
                    else if(args[1].equals("rent"))
                    {
                        if(args.length<3 || args.length>4)
                            return false;
                        boolean repeat = false;
                        if (args.length == 4) {
                            if (args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("true")) {
                                repeat = true;
                            } else if (!args[3].equalsIgnoreCase("f") && !args[3].equalsIgnoreCase("false")) {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidBoolean"));
                                return true;
                            }
                        }
                        rentmanager.rent(player, args[2], repeat, resadmin);
                        return true;
                    }
                    else if(args[1].equals("release"))
                    {
                        if(args.length!=3)
                            return false;
                        if(rentmanager.isRented(args[2]))
                        {
                            rentmanager.removeFromForRent(player, args[2], resadmin);
                        }
                        else
                        {
                            rentmanager.unrent(player, args[2], resadmin);
                        }
                        return true;
                    }
                    else if(args.length == 2)
                    {
                        if (args[1].equals("info")) {
                            String areaname = rmanager.getNameByLoc(player.getLocation());
                            tmanager.viewSaleInfo(areaname, player);
                            if(cmanager.enabledRentSystem() && rentmanager.isForRent(areaname))
                            {
                                rentmanager.printRentInfo(player, areaname);
                            }
                            return true;
                        }
                    }
                    else if(args.length == 3) {
                        if (args[1].equals("buy")) {
                            tmanager.buyPlot(args[2], player, resadmin);
                            return true;
                        } else if (args[1].equals("info")) {
                            tmanager.viewSaleInfo(args[2], player);
                            if(cmanager.enabledRentSystem() && rentmanager.isForRent(args[2]))
                            {
                                rentmanager.printRentInfo(player, args[2]);
                            }
                            return true;
                        } else if (args[1].equals("unsell")) {
                            tmanager.removeFromSale(player, args[2], resadmin);
                            return true;
                        }
                    }
                    else if(args.length == 4) {
                        if (args[1].equals("sell")) {
                            int amount;
                            try {
                                amount = Integer.parseInt(args[3]);
                            } catch (Exception ex) {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidAmount"));
                                return true;
                            }
                            tmanager.putForSale(args[2], player, amount, resadmin);
                            return true;
                        }
                    }
                    return false;
                } else if (args[0].equals("message")) {
                    ClaimedResidence res = null;
                    int start = 0;
                    boolean enter = false;
                    if(args.length<2)
                        return false;
                    if (args[1].equals("enter")) {
                        enter = true;
                        res = rmanager.getByLoc(player.getLocation());
                        start = 2;
                    } else if (args[1].equals("leave")) {
                        res = rmanager.getByLoc(player.getLocation());
                        start = 2;
                    } else if (args[1].equals("remove")) {
                        if (args.length>2 && args[2].equals("enter")) {
                            res = rmanager.getByLoc(player.getLocation());
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, true, resadmin);
                            } else {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            }
                            return true;
                        } else if (args.length>2 && args[2].equals("leave")) {
                            res = rmanager.getByLoc(player.getLocation());
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, false, resadmin);
                            } else {
                                player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            }
                            return true;
                        }
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMessageType"));
                        return true;
                    } else if (args.length>2 && args[2].equals("enter")) {
                        enter = true;
                        res = rmanager.getByName(args[1]);
                        start = 3;
                    } else if (args.length>2 && args[2].equals("leave")) {
                        res = rmanager.getByName(args[1]);
                        start = 3;
                    }
                    else if (args.length>2 && args[2].equals("remove")) {
                        res = rmanager.getByName(args[1]);
                        if (args.length != 4) {
                            return false;
                        }
                        if (args[3].equals("enter")) {
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, true, resadmin);
                            }
                            return true;
                        } else if (args[3].equals("leave")) {
                            if (res != null) {
                                res.setEnterLeaveMessage(player, null, false, resadmin);
                            }
                            return true;
                        }
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMessageType"));
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidMessageType"));
                        return true;
                    }
                    if(start == 0)
                        return false;
                    String message = "";
                    for (int i = start; i < args.length; i++) {
                        message = message + args[i] + " ";
                    }
                    if (res != null) {
                        res.setEnterLeaveMessage(player, message, enter, resadmin);
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                    }
                    return true;
                }
                else if(args[0].equals("give"))
                {
                    rmanager.giveResidence(player, args[2], args[1], resadmin);
                    return true;
                }
                else if (args[0].equals("setowner")) {
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    ClaimedResidence area = rmanager.getByName(args[1]);
                    if (area != null) {
                        area.getPermissions().setOwner(args[2], true);
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("ResidenceOwnerChange",ChatColor.YELLOW+" " + args[1] + " "+ChatColor.GREEN+"."+ChatColor.YELLOW+args[2]+ChatColor.GREEN));
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                    }
                    return true;
                }
                else if(args[0].equals("server"))
                {
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    if(args.length==2)
                    {
                        ClaimedResidence res = rmanager.getByName(args[1]);
                        if(res == null)
                        {
                            player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                            return true;
                        }
                        res.getPermissions().setOwner("Server Land", false);
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("ResidenceOwnerChange",ChatColor.YELLOW + args[1] + ChatColor.GREEN+"."+ChatColor.YELLOW+"Server Land"+ChatColor.GREEN));
                        return true;
                    }
                    else
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                }
                else if(args[0].equals("clearflags"))
                {
                    if(!resadmin)
                    {
                        player.sendMessage(ChatColor.RED+language.getPhrase("NoPermission"));
                        return true;
                    }
                    ClaimedResidence area = rmanager.getByName(args[1]);
                    if (area != null) {
                        area.getPermissions().clearFlags();
                        player.sendMessage(ChatColor.GREEN+language.getPhrase("FlagsCleared"));
                    } else {
                        player.sendMessage(ChatColor.RED+language.getPhrase("InvalidResidence"));
                    }
                    return true;
                }
                else if(args[0].equals("tool"))
                {
                    player.sendMessage(ChatColor.YELLOW+language.getPhrase("SelectionTool")+":"+ChatColor.GREEN + Material.getMaterial(cmanager.getSelectionTooldID()));
                    player.sendMessage(ChatColor.YELLOW+language.getPhrase("InfoTool")+": "+ChatColor.GREEN + Material.getMaterial(cmanager.getInfoToolID()));
                    return true;
                }
            }
            return false;
        }
        return super.onCommand(sender, command, label, args);
    }


    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for(Entry<String, Object> entry : save.entrySet())
        {
            yml = new YMLSaveHelper(new File(worldFolder, "res_"+entry.getKey()+".yml"));
            yml.getRoot().put("Version", saveVersion);
            yml.getRoot().put("Seed", server.getWorld(entry.getKey()).getSeed());
            yml.getRoot().put("Residences", (Map) entry.getValue());
            yml.save();
        }
        yml = new YMLSaveHelper(new File(saveFolder,"forsale.yml"));
        yml.save();
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Economy", tmanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"leases.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Leases", leasemanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"permlists.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("PermissionLists", pmanager.save());
        yml.save();
        yml = new YMLSaveHelper(new File(saveFolder,"rent.yml"));
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("RentSystem", rentmanager.save());
        yml.save();
        if(cmanager.showIntervalMessages())
            System.out.println("[Residence] - Saved Residences...");
    }

    private boolean loadYml() throws Exception
    {
        File saveFolder = new File(dataFolder, "Save");
        try {
            File oldFile = new File(dataFolder, "res.yml");
            if(oldFile.isFile() && !saveFolder.isDirectory())
            {
                System.out.println("[Residence] Upgrading to new save system...");
                this.oldLoadYMLSave(oldFile);
                this.saveYml();
                oldFile.delete();
                oldFile = new File(dataFolder, "res.yml.bak");
                if(oldFile.isFile())
                    oldFile.delete();
            }
            else
            {
                File worldFolder = new File(saveFolder, "Worlds");
                if(!saveFolder.isDirectory())
                {
                    System.out.println("[Residence] Save directory does not exist...");
                    return true;
                }
                YMLSaveHelper yml;
                File loadFile;
                HashMap<String, Object> worlds = new HashMap<String, Object>();
                for (World world : server.getWorlds()) {
                    loadFile = new File(worldFolder, "res_" + world.getName() + ".yml");
                    if (loadFile.isFile()) {
                        yml = new YMLSaveHelper(loadFile);
                        yml.load();
                        Object obj = yml.getRoot().get("Seed");
                        Long seed = 0L;
                        if (obj != null) {
                            if (obj instanceof Long) {
                                seed = (Long) obj;
                            } else if (obj instanceof Integer) {
                                seed = ((Integer) obj).longValue();
                            }
                        }
                        if(seed==0 || seed == world.getSeed())
                            worlds.put(world.getName(), yml.getRoot().get("Residences"));
                        else
                        {
                            if(seed != 0)
                            {
                                File tempfile = new File(worldFolder,"res_worldseed_"+seed+".yml");
                                int i = 0;
                                while(tempfile == null || tempfile.isFile())
                                {
                                    tempfile = new File(worldFolder,"res_worldseed_"+seed+"_"+i+".yml");
                                    i++;
                                }
                                System.out.println("[Residence] Save Error: World Seed mis-match! world: " + world.getName() + " seed: " + world.getSeed() + " expected: " + seed + ".  Renaming to " + tempfile.getName());
                                loadFile.renameTo(tempfile);
                            }
                            else
                            {
                                File tempfile = new File(worldFolder,"res_unknown.yml");
                                int i = 0;
                                while(tempfile == null || tempfile.isFile())
                                {
                                    tempfile = new File(worldFolder,"res_unknown_"+i+".yml");
                                    i++;
                                }
                                System.out.println("[Residence] Save Error: World Seed missing! world: " + world.getName() + ". Renaming to " + tempfile.getName());
                                loadFile.renameTo(tempfile);
                            }
                        }
                    }
                }
                rmanager = ResidenceManager.load(worlds);
                loadFile = new File(saveFolder, "forsale.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    tmanager = TransactionManager.load((Map) yml.getRoot().get("Economy"), gmanager, rmanager);
                }
                loadFile = new File(saveFolder, "leases.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    leasemanager = LeaseManager.load((Map) yml.getRoot().get("Leases"), rmanager);
                }
                loadFile = new File(saveFolder, "permlists.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    pmanager = PermissionListManager.load((Map) yml.getRoot().get("PermissionLists"));
                }
                loadFile = new File(saveFolder, "rent.yml");
                if(loadFile.isFile())
                {
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    rentmanager = RentManager.load((Map) yml.getRoot().get("RentSystem"));
                }
                //System.out.print("[Residence] Loaded...");
            }
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw(ex);
        }
    }

    private boolean oldLoadYMLSave(File saveLoc) throws Exception {
        if (saveLoc.isFile()) {
            YMLSaveHelper yml = new YMLSaveHelper(saveLoc);
            yml.load();
            rmanager = ResidenceManager.loadMap((Map) yml.getRoot().get("Residences"), new ResidenceManager());
            tmanager = TransactionManager.load((Map) yml.getRoot().get("Economy"), gmanager, rmanager);
            leasemanager = LeaseManager.load((Map) yml.getRoot().get("Leases"), rmanager);
            pmanager = PermissionListManager.load((Map) yml.getRoot().get("PermissionLists"));
            rentmanager = RentManager.load((Map) yml.getRoot().get("RentSystem"));
            System.out.print("[Residence] Loaded Residences...");
            return true;
        } else {
            System.out.println("[Residence] Save File not found...");
            return false;
        }
    }

    private void writeDefaultConfigFromJar()
    {
        if(this.writeDefaultFileFromJar(new File(this.getDataFolder(), "config.yml"), "config.yml", true))
            System.out.println("[Residence] Wrote default config...");
    }

    private void writeDefaultLanguageFile()
    {
        String lang = cmanager.getLanguage();
        File outFile = new File(new File(this.getDataFolder(),"Language"), lang+".yml");
        outFile.getParentFile().mkdirs();
        if(this.writeDefaultFileFromJar(outFile, "languagefiles/"+lang+".yml", true))
        {
            System.out.println("[Residence] Wrote default Language file...");
        }
    }

    private boolean checkNewLanguageVersion() throws IOException, FileNotFoundException, InvalidConfigurationException
    {
        String lang = cmanager.getLanguage();
        File outFile = new File(new File(this.getDataFolder(),"Language"), lang+".yml");
        File checkFile = new File(new File(this.getDataFolder(),"Language"), "temp-"+lang+".yml");
        if(outFile.isFile())
        {
            FileConfiguration testconfig = new YamlConfiguration();
            testconfig.load(outFile);
            int oldversion = testconfig.getInt("Version", 0);
            if(!this.writeDefaultFileFromJar(checkFile, "languagefiles/"+lang+".yml", false))
                return false;
            FileConfiguration testconfig2 = new YamlConfiguration();
            testconfig2.load(checkFile);
            int newversion = testconfig2.getInt("Version", oldversion);
            if(checkFile.isFile())
                checkFile.delete();
            if(newversion>oldversion)
                return true;
            return false;
        }
        return true;
    }

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld)
    {
        try {
            File fileBackup = new File(this.getDataFolder(),"backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if(jarloc.isFile())
            {
                JarFile jar = new JarFile(jarloc);
                JarEntry entry = jar.getJarEntry(jarPath);
                if(entry!=null && !entry.isDirectory())
                {
                    InputStream in = jar.getInputStream(entry);
                    InputStreamReader isr = new InputStreamReader(in, "UTF8");
                    if(writeName.isFile())
                    {
                        if(backupOld)
                        {
                            if(fileBackup.isFile())
                                fileBackup.delete();
                            writeName.renameTo(fileBackup);
                        }
                        else
                            writeName.delete();
                    }
                    FileOutputStream out = new FileOutputStream(writeName);
                    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes,0,512);
                    while(readbytes>-1)
                    {
                    	osw.write(tempbytes,0,readbytes);
                        readbytes = isr.read(tempbytes,0,512);
                    }
                    osw.close();
                    isr.close();
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            System.out.println("[Residence] Failed to write file: " + writeName + " from the Residence jar file, Error:" + ex);
            return false;
        }
    }
}
