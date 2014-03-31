package me.josvth.playerstatistics.tasks;

import me.josvth.playerstatistics.PlayerStatistics;
import me.josvth.playerstatistics.statistics.Statistic;
import org.bukkit.scoreboard.DisplaySlot;

public class CarouselTask implements Runnable {

    private final PlayerStatistics plugin;

    private int index = 0;

    private Statistic currentDisplayed;

    public CarouselTask(PlayerStatistics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        if (index < plugin.getStatistics().size()) {

            if (currentDisplayed != null) {
                currentDisplayed.getObjective().setDisplaySlot(null);
            }

            final int startIndex = index;

            do {
                currentDisplayed = plugin.getStatistics().get(index);
                currentDisplayed.getObjective().setDisplaySlot(DisplaySlot.SIDEBAR);
                index = (index + 1) % plugin.getStatistics().size();
            } while (currentDisplayed.getScores().isEmpty() && startIndex != index);

        }

    }

}
