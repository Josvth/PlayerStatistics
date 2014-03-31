package me.josvth.playerstatistics.statistics;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class KillStatistic extends Statistic {

    private List<EntityType> entityTypes = new ArrayList<EntityType>();

    public KillStatistic() {
        super("kill");
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        if (entityTypes.isEmpty()) {
            setScore(event.getEntity().getKiller(), getScore(event.getEntity().getKiller()) + 1);
        } else if (entityTypes.contains(event.getEntityType())) {
            setScore(event.getEntity().getKiller(), getScore(event.getEntity().getKiller()) + 1);
        }
    }
    public static KillStatistic deserialize(ConfigurationSection section) {
        final KillStatistic statistic = new KillStatistic();

        if (section.isString("display-name")) {
            statistic.setDisplayName(section.getString("display-name"));
        }
        if (section.isString("entity")) {
            statistic.getEntityTypes().add(EntityType.fromName(section.getString("entity")));
        }
        if (section.isList("entities")) {
            for (String entity : section.getStringList("entities")) {
                statistic.getEntityTypes().add(EntityType.fromName(entity));
            }
        }

        return statistic;

    }

}
