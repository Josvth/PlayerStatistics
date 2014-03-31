package me.josvth.playerstatistics.tasks;

import me.josvth.playerstatistics.PlayerStatistics;
import me.josvth.playerstatistics.statistics.Statistic;

public class SaverTask implements Runnable {

    private final PlayerStatistics plugin;

    public SaverTask(PlayerStatistics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getStatistics() != null) {
            for (Statistic statistic : plugin.getStatistics()) {
                if (plugin.getStatisticsConfiguration().isConfigurationSection(statistic.getName())) {
                    statistic.save(plugin.getStatisticsConfiguration().getConfigurationSection(statistic.getName()));
                } else {
                    statistic.save(plugin.getStatisticsConfiguration().createSection(statistic.getName()));
                }
            }
            plugin.getStatisticsConfiguration().save();
        }
    }
}
