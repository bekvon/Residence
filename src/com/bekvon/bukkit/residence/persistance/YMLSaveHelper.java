/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Administrator
 */
public class YMLSaveHelper {

    File f;
    Yaml yml;
    Map<String,Object> root;

    public YMLSaveHelper(File ymlfile) throws IOException
    {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        yml = new Yaml(options);
        
        root = new LinkedHashMap<String,Object>();
        if(ymlfile == null)
            throw new IOException("YMLSaveHelper: null file...");
        f = ymlfile;
    }

    public void save() throws IOException
    {
        if(f.isFile())
            f.delete();
        FileWriter fout = new FileWriter(f);
        yml.dump(root, fout);
        fout.close();
    }

    public void load() throws IOException
    {
        FileInputStream fis = new FileInputStream(f);
        root = (Map<String, Object>) yml.load(fis);
        fis.close();
    }

    public Map<String,Object> getRoot()
    {
        return root;
    }

}
