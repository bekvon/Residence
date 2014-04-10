/*
    Original Code from mcMMO
*/
package net.t00thpick1.residence.utils.backup.prune;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.utils.backup.zip.ZipLibrary;

import org.bukkit.scheduler.BukkitRunnable;

public class CleanBackupsTask extends BukkitRunnable {
    private static final File BACKUP_DIR = ZipLibrary.BACKUP_DIR;

    @Override
    public void run() {
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
    private boolean isPast24Hours(Date date) {
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
    private boolean isLastWeek(Date date) {
        Date modifiedDate = new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
        return date.after(modifiedDate);
    }

    private Date getDate(String fileName) {
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
