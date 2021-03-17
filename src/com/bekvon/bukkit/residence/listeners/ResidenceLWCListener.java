package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
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
