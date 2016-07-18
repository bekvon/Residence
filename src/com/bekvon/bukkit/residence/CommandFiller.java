package com.bekvon.bukkit.residence;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.bekvon.bukkit.residence.containers.CommandAnnotation;

public class CommandFiller {

    public final String packagePath = "com.bekvon.bukkit.residence.commands";
    public Map<String, Boolean> CommandList = new HashMap<String, Boolean>();

    public Map<String, Boolean> fillCommands() {
	List<String> lm = new ArrayList<String>();
	HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();
	try {
	    lm = getClassesFromPackage(packagePath);
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}

	for (String one : lm) {
	    Class<?> newclass = getClass(one);
	    if (newclass != null)
		classes.put(one, newclass);
	}

	for (Entry<String, Class<?>> OneClass : classes.entrySet()) {
	    for (Method met : OneClass.getValue().getMethods()) {
		if (!met.isAnnotationPresent(CommandAnnotation.class))
		    continue;
		String cmd = OneClass.getKey();
//		if (hidenCommands.contains(cmd.toLowerCase()))
//		    continue;
		CommandList.put(cmd, met.getAnnotation(CommandAnnotation.class).value());
		break;
	    }
	}
	return CommandList;
    }

    public static List<String> getClassesFromPackage(String pckgname) throws ClassNotFoundException {
	List<String> result = new ArrayList<String>();
	try {
	    for (URL jarURL : ((URLClassLoader) Residence.class.getClassLoader()).getURLs()) {
		try {
		    result.addAll(getClassesInSamePackageFromJar(pckgname, jarURL.toURI().getPath()));
		} catch (URISyntaxException e) {
		}
	    }
	} catch (NullPointerException x) {
	    throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
	}

	return result;
    }

    private static List<String> getClassesInSamePackageFromJar(String packageName, String jarPath) {
	JarFile jarFile = null;
	List<String> listOfCommands = new ArrayList<String>();
	try {
	    jarFile = new JarFile(jarPath);
	    Enumeration<JarEntry> en = jarFile.entries();
	    while (en.hasMoreElements()) {
		JarEntry entry = en.nextElement();
		String entryName = entry.getName();
		packageName = packageName.replace(".", "/");
		if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(packageName)) {
		    String name = entryName.replace(packageName, "").replace(".class", "").replace("/", "");
		    if (name.contains("$"))
			name = name.split("\\$")[0];
		    listOfCommands.add(name);
		}
	    }
	} catch (Exception e) {
	} finally {
	    if (jarFile != null)
		try {
		    jarFile.close();
		} catch (Exception e) {
		}
	}
	return listOfCommands;
    }

    private Class<?> getClass(String cmd) {
	Class<?> nmsClass = null;
	try {
	    nmsClass = Class.forName(packagePath + "." + cmd.toLowerCase());
	} catch (ClassNotFoundException e) {
	} catch (IllegalArgumentException e) {
	} catch (SecurityException e) {
	}
	return nmsClass;
    }
}
