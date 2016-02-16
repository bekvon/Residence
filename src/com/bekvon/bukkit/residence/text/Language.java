/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

/**
 * 
 * @author Administrator
 */
public class Language {
    public Map<String, String> text;

    public Language() {
	text = new HashMap<String, String>();
    }

    public void setText(String key, String intext) {
	text.put(key, intext);
    }

    private String getText(String key) {
	String t = text.get(key);
	if (t == null) {
	    t = "<missing language key: " + key + ">";
	}
	return t;
    }

    public String getPhrase(String key) {
	String[] split = key.split("\\.");
	return getPhrase(split);
    }

    public String getPhraseList(String key) {
	String tx = text.get(key.replace(",", "\n"));
	return tx;
    }

    public String getPhrase(String[] keys) {
	return this.getPhrase(keys, (String[]) null);
    }

    public String getPhrase(String key, String words) {
	return this.getPhrase(key.split("\\."), words);
    }

    public String getPhrase(String[] keys, String words) {
	if (words == null) {
	    return this.getPhrase(keys, (String[]) null);
	} else {
	    return this.getPhrase(keys, words.split("\\|"));
	}
    }

    public String getPhrase(String[] keys, String[] words) {
	String sentence = "";
	for (String key : keys) {
	    if (sentence.length() == 0) {
		sentence = this.getText(key);
	    } else {
		sentence = sentence + " " + this.getText(key);
	    }
	}
	if (words != null) {
	    for (int i = 0; i < words.length; i++) {
		sentence = sentence.replace("%" + (i + 1), words[i]);
	    }
	}

	sentence = ChatColor.translateAlternateColorCodes('&', sentence);

	return sentence;
    }

    public static Language parseText(FileConfiguration node, String topkey) {
	Language newholder = new Language();
	Set<String> keys = node.getConfigurationSection(topkey).getKeys(false);
	for (String key : keys) {
	    newholder.text.put(key, node.getString(topkey + "." + key));
	}
	return newholder;
    }
}
