package me.josvth.playerstatistics.statistics;

import me.josvth.playerstatistics.PlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;

import java.util.HashMap;
import java.util.Map;

public class OnlineTimeStatistic extends Statistic {

    private long timePeriod = -1;       // -1 total time
    private long timeScale = 3600000;   // Hours

    private long updateInterval = 600;

    private BukkitTask updateTask;

    private Map<OfflinePlayer, Long> playTimes;

    public OnlineTimeStatistic() {
        super("online-time");
    }

    public long getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(long timePeriod) {
        this.timePeriod = timePeriod;
    }

    public long getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(long timeScale) {
        this.timeScale = timeScale;
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
        playTimes = new HashMap<OfflinePlayer, Long>();
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

            long currentPeriod = -1;
            long previousTime = -1;

            @Override
            public void run() {

                final long intervalDuration;

                if (previousTime == -1) {
                    intervalDuration = updateInterval * 50;
                } else {
                    intervalDuration = System.currentTimeMillis() - previousTime;
                }

                previousTime = System.currentTimeMillis();

                if (timePeriod > 0) {
                    final long nextPeriod = System.currentTimeMillis() / timePeriod;

                    // We entered a new period
                    if (nextPeriod > currentPeriod || currentPeriod == -1) {
                        currentPeriod = nextPeriod;
                        for (Score score : getScores().values()) {
                            score.setScore(0);
                        }
                    }
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Long playTime = playTimes.get(player);
                    if (playTime == null) {
                        playTime = (long) 0;
                    }
                    playTimes.put(player, playTime + intervalDuration);
                    setScore(player, (int) (playTimes.get(player) / timeScale));
                }

            }

        }, 1, updateInterval);
    }

    public void load(ConfigurationSection section) {
        if (section.isConfigurationSection("scores")) {

            final ConfigurationSection scoreSection = section.getConfigurationSection("scores");

            for (String key : scoreSection.getKeys(false)) {
                playTimes.put(Bukkit.getOfflinePlayer(key), scoreSection.getLong(key, 0));
                Long playTime = playTimes.get(Bukkit.getOfflinePlayer(key));
                if (playTime == null) {
                    playTime = (long) 0;
                }
                setScore(Bukkit.getOfflinePlayer(key), (int) (playTime / timeScale));
            }
        }
    }

    public void save(ConfigurationSection section) {

        if (getScores().isEmpty()) {
            return;
        }

        ConfigurationSection scoreSection = section.getConfigurationSection("scores");

        if (scoreSection == null) {
            scoreSection = section.createSection("scores");
        }

        for (Map.Entry<OfflinePlayer, Long> entry : playTimes.entrySet()) {
            scoreSection.set(entry.getKey().getName(), entry.getValue());
        }

    }

    @Override
    public void unload() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    public static OnlineTimeStatistic deserialize(ConfigurationSection section) {

        final OnlineTimeStatistic statistic = new OnlineTimeStatistic();

        statistic.setDisplayName(section.getString("display-name"));
        statistic.setTimePeriod(section.getLong("time-period", -1));
        statistic.setTimeScale(section.getLong("time-scale", 3600000));
        statistic.setUpdateInterval(section.getLong("update-interval", 6000));

        return statistic;
    }

}
