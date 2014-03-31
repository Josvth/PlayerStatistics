package me.josvth.playerstatistics.statistics;

import me.josvth.playerstatistics.PlayerStatistics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class DeathStatistic extends Statistic {

    public DeathStatistic() {
        super("death");
    }

    @Override
    public void initialize(PlayerStatistics plugin) {
        super.initialize(plugin);
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();

        setScore(player.getName(), getScore(player.getName()) + 1);

    }

    public static DeathStatistic deserialize(ConfigurationSection section) {

        final DeathStatistic statistic = new DeathStatistic();

        if (section.isString("display-name")) {
            statistic.setDisplayName(section.getString("display-name"));
        }

        return statistic;
    }

}
