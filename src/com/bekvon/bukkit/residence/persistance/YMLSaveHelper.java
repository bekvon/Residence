/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        options.setAllowUnicode(true);
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
        FileOutputStream fout = new FileOutputStream(f);
        OutputStreamWriter osw = new OutputStreamWriter(fout, "UTF8");
        yml.dump(root, osw);
        osw.close();
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException
    {
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, "UTF8");
        root = (Map<String, Object>) yml.load(isr);
        isr.close();
    }

    public Map<String,Object> getRoot()
    {
        return root;
    }

}
