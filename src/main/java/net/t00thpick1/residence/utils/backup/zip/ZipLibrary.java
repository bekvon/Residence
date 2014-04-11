/*
    Original Code from mcMMO
*/
package net.t00thpick1.residence.utils.backup.zip;

import net.t00thpick1.residence.Residence;

import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipLibrary {
    public static final File BACKUP_DIR = new File(Residence.getInstance().getDataFolder(), "Backup");

    public static void backup() throws IOException {
        try {
            BACKUP_DIR.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Generate the proper date for the backup filename
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        File fileZip = new File(BACKUP_DIR, dateFormat.format(date) + ".zip");

        // Create the Source List, and add directories/etc to the file.
        List<File> sources = new ArrayList<File>();

        File saveFolder = new File(Residence.getInstance().getDataFolder(), "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        if (!saveFolder.isDirectory()) {
            return;
        }
        File saveFile;
        for (World world : Residence.getInstance().getServer().getWorlds()) {
            saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
            if (saveFile.isFile()) {
                sources.add(saveFile);
            }
        }


        packZip(fileZip, sources);
        prune();
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

    public static void prune() {
        List<Integer> savedDays = new ArrayList<Integer>();
        HashMap<Integer, List<Integer>> savedYearsWeeks = new HashMap<Integer, List<Integer>>();
        List<File> toDelete = new ArrayList<File>();
        int amountTotal = 0;
        int amountDeleted = 0;

        if (BACKUP_DIR.listFiles() == null) {
            return;
        }

        // Check files in backup folder from oldest to newest
        for (File file : BACKUP_DIR.listFiles()) {
            if (!file.isFile() || file.isDirectory()) {
                continue;
            }

            amountTotal++;
            String fileName = file.getName();

            Date date = getDate(fileName.split("[.]")[0]);

            if (!fileName.contains(".zip") || date == null) {
                continue;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
            int year = cal.get(Calendar.YEAR);

            if (isPast24Hours(date)) {
                // Keep all files from the last 24 hours
                continue;
            }
            else if (isLastWeek(date) && !savedDays.contains(dayOfWeek) ) {
                // Keep daily backups of the past week
                savedDays.add(dayOfWeek);
                continue;
            }
            else {
                List<Integer> savedWeeks = savedYearsWeeks.get(year);
                if (savedWeeks == null) {
                    savedWeeks = new ArrayList<Integer>();
                    savedYearsWeeks.put(year, savedWeeks);
                }

                if (!savedWeeks.contains(weekOfYear)) {
                    // Keep one backup of each week
                    savedWeeks.add(weekOfYear);
                    continue;
                }
            }

            amountDeleted++;
            toDelete.add(file);
        }

        if (toDelete.isEmpty()) {
            return;
        }

        Residence.getInstance().getLogger().info("Cleaned backup files. Deleted " + amountDeleted + " of " + amountTotal + " files.");

        for (File file : toDelete) {
            if (!file.delete()) {
                Residence.getInstance().getLogger().info("Failed to delete: " + file.getName());
            }
        }
    }

    /**
     * Check if date is within last 24 hours
     *
     * @param date date to check
     *
     * @return true is date is within last 24 hours, false if otherwise
     */
    private static boolean isPast24Hours(Date date) {
        Date modifiedDate = new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS));
        return date.after(modifiedDate);
    }

    /**
     * Check if date is within the last week
     *
     * @param date date to check
     *
     * @return true is date is within the last week, false if otherwise
     */
    private static boolean isLastWeek(Date date) {
        Date modifiedDate = new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
        return date.after(modifiedDate);
    }

    private static Date getDate(String fileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date;

        try {
            date = dateFormat.parse(fileName);
        }
        catch (ParseException e) {
            return null;
        }

        return date;
    }
}
