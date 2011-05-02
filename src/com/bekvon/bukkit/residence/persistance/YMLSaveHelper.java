/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.persistance;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.util.config.Configuration;

/**
 *
 * @author Administrator
 */
public class YMLSaveHelper extends Configuration {

    public YMLSaveHelper(File infile) {
        super(infile);
        root = new LinkedHashMap<String,Object>();
    }

    public void addMap(String name, Map map)
    {
        root.put(name, map);
    }

    public Map getMap(String name)
    {
        return (Map) root.get(name);
    }

    public Map getRoot()
    {
        return root;
    }

    public void setRoot(Map<String,Object> newroot)
    {
        if(newroot != null);
            root = newroot;
    }
}
