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

import com.bekvon.bukkit.residence.containers.ABInterface;

/**
*
* @author hamzaxx
*/
public class ActionBar implements ABInterface {
    private String version = "";
    private Object packet;
    private Method getHandle;
    private Method sendPacket;
    private Field playerConnection;
    private Class<?> nmsChatSerializer;
    private Class<?> nmsIChatBaseComponent;
    private Class<?> packetType;
    private Constructor<?> constructor;
    private boolean simpleMessages = false;

    public ActionBar() {
	try {
	    String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
	    version = v[v.length - 1];
//	    version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	    packetType = Class.forName(getPacketPlayOutChat());
	    Class<?> typeCraftPlayer = Class.forName(getCraftPlayerClasspath());
	    Class<?> typeNMSPlayer = Class.forName(getNMSPlayerClasspath());
	    Class<?> typePlayerConnection = Class.forName(getPlayerConnectionClasspath());
	    nmsChatSerializer = Class.forName(getChatSerializerClasspath());
	    nmsIChatBaseComponent = Class.forName(getIChatBaseComponentClasspath());
	    getHandle = typeCraftPlayer.getMethod("getHandle");
	    playerConnection = typeNMSPlayer.getField("playerConnection");
	    sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName(getPacketClasspath()));
	    if (!version.contains("1_7")) {
		constructor = packetType.getConstructor(nmsIChatBaseComponent, byte.class);
	    } else {
		constructor = packetType.getConstructor(nmsIChatBaseComponent, int.class);
	    }
	} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException ex) {
	    simpleMessages = true;
	    Bukkit.getLogger().log(Level.SEVERE, "Your server can't fully suport action bar messages. They will be shown in chat instead.");
	}
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
	    if (!version.contains("1_7")) {
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
	if (version.equals("v1_8_R1") || version.contains("1_7")) {
	    return "net.minecraft.server." + version + ".ChatSerializer";
	}
	return "net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer";// 1_8_R2 moved to IChatBaseComponent
    }

    private String getPacketPlayOutChat() {
	return "net.minecraft.server." + version + ".PacketPlayOutChat";
    }

    @Override
    public void sendTitle(Player player, Object title, Object subtitle) {
	return;
    }
}
