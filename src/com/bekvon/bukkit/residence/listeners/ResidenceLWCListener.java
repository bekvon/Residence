package com.bekvon.bukkit.residence.listeners;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCEntityInteractEvent;
import com.griefcraft.scripting.event.LWCMagnetPullEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEntityEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEntityEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.scripting.event.LWCReloadEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;

public class ResidenceLWCListener implements com.griefcraft.scripting.Module {

    public static void register(Plugin plugin) {
	com.griefcraft.lwc.LWC.getInstance().getModuleLoader().registerModule(plugin, new ResidenceLWCListener());
    }

    public static void removeLwcFromResidence(final Player player, final ClaimedResidence res) {
//	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
//	    @Override
//	    public void run() {
	long time = System.currentTimeMillis();
	com.griefcraft.lwc.LWC lwc = com.griefcraft.lwc.LWC.getInstance();
	if (lwc == null)
	    return;
	if (res == null)
	    return;
	int i = 0;

	com.griefcraft.cache.ProtectionCache cache = lwc.getProtectionCache();

	List<Material> list = Residence.getInstance().getConfigManager().getLwcMatList();

	try {
	    for (CuboidArea area : res.getAreaArray()) {
		Location low = area.getLowLocation();
		Location high = area.getHighLocation();
		World world = low.getWorld();
		for (int x = low.getBlockX(); x <= high.getBlockX(); x++) {
		    for (int y = low.getBlockY(); y <= high.getBlockY(); y++) {
			for (int z = low.getBlockZ(); z <= high.getBlockZ(); z++) {
			    Block b = world.getBlockAt(x, y, z);
			    if (!b.getChunk().isLoaded())
				b.getChunk().load();
			    if (!list.contains(b.getType()))
				continue;
			    com.griefcraft.model.Protection prot = cache.getProtection(b);
			    if (prot == null)
				continue;
			    prot.remove();
			    i++;
			}
		    }
		}
	    }
	} catch (Exception e) {
	}
	if (i > 0)
	    Residence.getInstance().msg(player, lm.Residence_LwcRemoved, i, System.currentTimeMillis() - time);
	return;
//	    }
//	});
    }

    @Override
    public void load(LWC lwc) {
    }

    @Override
    public void onReload(LWCReloadEvent event) {
    }

    @Override
    public void onAccessRequest(LWCAccessEvent event) {
    }

    @Override
    public void onDropItem(LWCDropItemEvent event) {
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
    }

    @Override
    public void onRedstone(LWCRedstoneEvent event) {
    }

    @Override
    public void onDestroyProtection(LWCProtectionDestroyEvent event) {
    }

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
    }

    @Override
    public void onRegisterProtection(LWCProtectionRegisterEvent event) {
	Player player = event.getPlayer();
	FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	boolean hasuse = perms.playerHas(player, Flags.use, true);
	if (!perms.playerHas(player, Flags.container, hasuse) && !ResPerm.bypass_container.hasPermission(player, 10000L)) {
	    event.setCancelled(true);
	    Residence.getInstance().msg(player, lm.Flag_Deny, Flags.container);
	}
    }

    @Override
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event) {
    }

    @Override
    public void onPostRemoval(LWCProtectionRemovePostEvent event) {
    }

    @Override
    public void onSendLocale(LWCSendLocaleEvent event) {
    }

    @Override
    public void onEntityInteract(LWCEntityInteractEvent arg0) {
    }

    @Override
    public void onEntityInteractProtection(LWCProtectionInteractEntityEvent arg0) {
    }

    @Override
    public void onMagnetPull(LWCMagnetPullEvent arg0) {
    }

    @Override
    public void onRegisterEntity(LWCProtectionRegisterEntityEvent arg0) {
    }

}
