package me.josvth.playerstatistics.statistics;

import me.josvth.playerstatistics.PlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LastSeenStatistic extends Statistic {

    private SimpleDateFormat format = new SimpleDateFormat("HH:mm dd-MM-YYYY");

    private long updateInterval = 6000;

    private BukkitTask updateTask;

    private Map<String, OfflinePlayer> offlinePlayerMap;

    public LastSeenStatistic() {
        super("last-seen");
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public void setFormat(SimpleDateFormat format) {
        this.format = format;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    @Override
    public void initialize(PlayerStatistics plugin) {
        super.initialize(plugin);
        offlinePlayerMap = new HashMap<String, OfflinePlayer>();
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {
                for (OfflinePlayer offlinePlayer : Bukkit.getServer().getOfflinePlayers()) {
                    final OfflinePlayer previousOfflinePlayer = offlinePlayerMap.get(offlinePlayer.getName());
                    if (previousOfflinePlayer != null) {
                        getObjective().getScoreboard().resetScores(previousOfflinePlayer);
                    }

                    final String playerShort = offlinePlayer.getName().substring(0, 16 - format.toPattern().length() - 1);

                    if (offlinePlayer.isOnline()) {
                        final OfflinePlayer newOfflinePlayer = Bukkit.getOfflinePlayer(playerShort + " " + ChatColor.GREEN + "Online");
                        offlinePlayerMap.put(offlinePlayer.getName(), newOfflinePlayer);
                        setScore(newOfflinePlayer, 1);
                    } else {
                        final OfflinePlayer newOfflinePlayer = Bukkit.getOfflinePlayer(playerShort + " " + format.format(new Date(System.currentTimeMillis())));
                        offlinePlayerMap.put(offlinePlayer.getName(), newOfflinePlayer);
                        setScore(newOfflinePlayer, 0);
                    }
                }
            }

        }, 1, updateInterval);
    }

    @Override
    public void load(ConfigurationSection section) {
        // Do nothing
    }

    @Override
    public void save(ConfigurationSection section) {
        // Do nothing
    }

    @Override
    public void unload() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public static LastSeenStatistic deserialize(ConfigurationSection section) {

        final LastSeenStatistic statistic = new LastSeenStatistic();

        if (section.isString("display-name")) {
            statistic.setDisplayName(section.getString("display-name"));
        }
        if (section.isString("format")) {
            statistic.setFormat(new SimpleDateFormat(section.getString("format", "HH:mm dd-MM-YYYY")));
        }
        if (section.isLong("update-interval")) {
            statistic.setUpdateInterval(section.getLong("update-interval", 6000));
        }

        return statistic;
    }

}
