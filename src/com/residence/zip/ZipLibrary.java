package com.residence.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;

public class ZipLibrary {
    private File BackupDir = new File(Residence.getDataLocation(), "Backup");

    private  void cleanFiles() {
	int x = Residence.getConfigManager().BackupAutoCleanUpDays() * 60 * 1000 * 24 * 60;
	Long time = System.currentTimeMillis();
	for (File file : BackupDir.listFiles()) {
	    long diff = time - file.lastModified();
	    if (diff > x) {
		file.delete();
	    }
	}
    }

    public void backup() throws IOException {
	try {
	    BackupDir.mkdir();
	} catch (Exception e) {
	    e.printStackTrace();
	    return;
	}
	if (Residence.getConfigManager().BackupAutoCleanUpUse())
	    cleanFiles();
	// Generate the proper date for the backup filename
	Date date = new Date();
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	File fileZip = new File(BackupDir, dateFormat.format(date) + ".zip");

	// Create the Source List, and add directories/etc to the file.
	List<File> sources = new ArrayList<File>();

	File saveFolder = new File(Residence.getDataLocation(), "Save");
	File worldFolder = new File(saveFolder, "Worlds");

	if (!saveFolder.isDirectory()) {
	    return;
	}

	File saveFile;
	if (Residence.getConfigManager().BackupWorldFiles())
	    for (World world : Residence.getServ().getWorlds()) {
		saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
		if (saveFile.isFile()) {
		    sources.add(saveFile);
		}
	    }

	if (Residence.getConfigManager().BackupforsaleFile())
	    sources.add(new File(saveFolder, "forsale.yml"));
	if (Residence.getConfigManager().BackupleasesFile())
	    sources.add(new File(saveFolder, "leases.yml"));
	if (Residence.getConfigManager().BackuppermlistsFile())
	    sources.add(new File(saveFolder, "permlists.yml"));
	if (Residence.getConfigManager().BackuprentFile())
	    sources.add(new File(saveFolder, "rent.yml"));

	if (Residence.getConfigManager().BackupflagsFile())
	    sources.add(new File(Residence.getDataLocation(), "flags.yml"));
	if (Residence.getConfigManager().BackupgroupsFile())
	    sources.add(new File(Residence.getDataLocation(), "groups.yml"));
	if (Residence.getConfigManager().BackupconfigFile())
	    sources.add(new File(Residence.getDataLocation(), "config.yml"));

	if (Residence.getConfigManager().UseZipBackup())
	    packZip(fileZip, sources);
    }

    private static void packZip(File output, List<File> sources) throws IOException {
	ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
	zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

	for (File source : sources) {
	    if (source.isDirectory()) {
		zipDir(zipOut, "", source);
	    } else {
		zipFile(zipOut, "", source);
	    }
	}

	zipOut.flush();
	zipOut.close();
    }

    private static String buildPath(String path, String file) {
	if (path == null || path.isEmpty()) {
	    return file;
	}

	return path + File.separator + file;
    }

    private static void zipDir(ZipOutputStream zos, String path, File dir) throws IOException {
	if (!dir.canRead()) {
	    return;
	}

	File[] files = dir.listFiles();
	path = buildPath(path, dir.getName());

	for (File source : files) {
	    if (source.isDirectory()) {
		zipDir(zos, path, source);
	    } else {
		zipFile(zos, path, source);
	    }
	}
    }

    private static void zipFile(ZipOutputStream zos, String path, File file) throws IOException {
	if (!file.canRead()) {
	    return;
	}

	zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

	FileInputStream fis = new FileInputStream(file);
	byte[] buffer = new byte[4092];
	int byteCount = 0;

	while ((byteCount = fis.read(buffer)) != -1) {
	    zos.write(buffer, 0, byteCount);
	}

	fis.close();
	zos.closeEntry();
    }
}
