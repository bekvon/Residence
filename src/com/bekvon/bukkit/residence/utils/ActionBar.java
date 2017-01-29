package com.bekvon.bukkit.residence.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ABInterface;
import com.bekvon.bukkit.residence.utils.VersionChecker.Version;

public class ActionBar implements ABInterface {
    private Version version = Version.v1_11_R1;
    private Object packet;
    private Method getHandle;
    private Method sendPacket;
    private Field playerConnection;
    private Class<?> nmsChatSerializer;
    private Class<?> nmsIChatBaseComponent;
    private Class<?> packetType;
    private Constructor<?> constructor;
    private boolean simpleMessages = false;
    private boolean simpleTitleMessages = false;

    private Constructor<?> nmsPacketPlayOutTitle;
    private Class<?> enumTitleAction;
    private Method fromString;
    private Residence plugin;

    public ActionBar(Residence plugin) {
	this.plugin = plugin;
	version = this.plugin.getVersionChecker().getVersion();
	try {
	    packetType = Class.forName(getPacketPlayOutChat());
	    Class<?> typeCraftPlayer = Class.forName(getCraftPlayerClasspath());
	    Class<?> typeNMSPlayer = Class.forName(getNMSPlayerClasspath());
	    Class<?> typePlayerConnection = Class.forName(getPlayerConnectionClasspath());
	    nmsChatSerializer = Class.forName(getChatSerializerClasspath());
	    nmsIChatBaseComponent = Class.forName(getIChatBaseComponentClasspath());
	    getHandle = typeCraftPlayer.getMethod("getHandle");
	    playerConnection = typeNMSPlayer.getField("playerConnection");
	    sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName(getPacketClasspath()));
	    if (plugin.getVersionChecker().isHigher(Version.v1_7_R4)) {
		constructor = packetType.getConstructor(nmsIChatBaseComponent, byte.class);
	    } else {
		constructor = packetType.getConstructor(nmsIChatBaseComponent, int.class);
	    }

	} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException ex) {
	    simpleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully suport action bar messages. They will be shown in chat instead.");
	}
	// Title
	try {
	    Class<?> typePacketPlayOutTitle = Class.forName(getPacketPlayOutTitleClasspath());
	    enumTitleAction = Class.forName(getEnumTitleActionClasspath());
	    nmsPacketPlayOutTitle = typePacketPlayOutTitle.getConstructor(enumTitleAction, nmsIChatBaseComponent);
	    fromString = Class.forName(getClassMessageClasspath()).getMethod("fromString", String.class);
	} catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
	    simpleTitleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully suport title messages. They will be shown in chat instead.");
	}
    }

    @Override
    public void sendTitle(Player receivingPacket, Object title, Object subtitle) {
	if (simpleTitleMessages) {
	    receivingPacket.sendMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(title)));
	    receivingPacket.sendMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(subtitle)));
	    return;
	}
	try {
	    if (title != null) {
		Object packetTitle = nmsPacketPlayOutTitle.newInstance(enumTitleAction.getField("TITLE").get(null),
		    ((Object[]) fromString.invoke(null, ChatColor.translateAlternateColorCodes('&', String.valueOf(title))))[0]);
		sendPacket(receivingPacket, packetTitle);
	    }
	    if (subtitle != null) {
		Object packetSubtitle = nmsPacketPlayOutTitle.newInstance(enumTitleAction.getField("SUBTITLE").get(null),
		    ((Object[]) fromString.invoke(null, ChatColor.translateAlternateColorCodes('&', String.valueOf(subtitle))))[0]);
		sendPacket(receivingPacket, packetSubtitle);
	    }
	} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException ex) {
	    simpleTitleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully support title messages. They will be shown in chat instead.");
	}
    }

    private void sendPacket(Player player, Object packet) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	Object handle = getHandle.invoke(player);
	Object connection = playerConnection.get(handle);
	sendPacket.invoke(connection, packet);
    }

    @Override
    public void send(CommandSender sender, String msg) {
	if (sender instanceof Player)
	    send((Player) sender, msg);
	else
	    sender.sendMessage(msg);
    }

    @Override
    public void send(Player receivingPacket, String msg) {
	if (simpleMessages) {
	    receivingPacket.sendMessage(msg);
	    return;
	}
	try {
	    Object serialized = nmsChatSerializer.getMethod("a", String.class).invoke(null, "{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', JSONObject
		.escape(msg)) + "\"}");
	    if (plugin.getVersionChecker().isHigher(Version.v1_7_R4)) {
		packet = constructor.newInstance(serialized, (byte) 2);
	    } else {
		packet = constructor.newInstance(serialized, 2);
	    }
	    Object player = getHandle.invoke(receivingPacket);
	    Object connection = playerConnection.get(player);
	    sendPacket.invoke(connection, packet);
	} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
	    simpleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully suport action bar messages. They will be shown in chat instead.");
	}
    }

    private String getCraftPlayerClasspath() {
	return "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer";
    }

    private String getPlayerConnectionClasspath() {
	return "net.minecraft.server." + version + ".PlayerConnection";
    }

    private String getNMSPlayerClasspath() {
	return "net.minecraft.server." + version + ".EntityPlayer";
    }

    private String getPacketClasspath() {
	return "net.minecraft.server." + version + ".Packet";
    }

    private String getIChatBaseComponentClasspath() {
	return "net.minecraft.server." + version + ".IChatBaseComponent";
    }

    private String getChatSerializerClasspath() {
	if (plugin.getVersionChecker().isLower(Version.v1_8_R2))
	    return "net.minecraft.server." + version + ".ChatSerializer";
	return "net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer";// 1_8_R2 moved to IChatBaseComponent
    }

    private String getPacketPlayOutChat() {
	return "net.minecraft.server." + version + ".PacketPlayOutChat";
    }

    private String getPacketPlayOutTitleClasspath() {
	return "net.minecraft.server." + version + ".PacketPlayOutTitle";
    }

    private String getEnumTitleActionClasspath() {
	return getPacketPlayOutTitleClasspath() + "$EnumTitleAction";
    }

    private String getClassMessageClasspath() {
	return "org.bukkit.craftbukkit." + version + ".util.CraftChatMessage";
    }
}
