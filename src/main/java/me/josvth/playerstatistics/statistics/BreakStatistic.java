package me.josvth.playerstatistics.statistics;

import me.josvth.playerstatistics.PlayerStatistics;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

public class BreakStatistic extends Statistic {

    private List<Material> materials = new ArrayList<Material>();

    public BreakStatistic() {
        super("break");
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    @Override
    public void initialize(PlayerStatistics plugin) {
        super.initialize(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (materials.isEmpty()) {
            setScore(event.getPlayer().getName(), getScore(event.getPlayer().getName()) + 1);
        } else if (materials.contains(event.getBlock().getType())) {
            setScore(event.getPlayer().getName(), getScore(event.getPlayer().getName()) + 1);
        }
    }

    public static BreakStatistic deserialize(ConfigurationSection section) {

        final BreakStatistic statistic = new BreakStatistic();

        if (section.isString("display-name")) {
            statistic.setDisplayName(section.getString("display-name"));
        }
        if (section.isString("material")) {
            statistic.getMaterials().add(Material.getMaterial(section.getString("material")));
        }
        if (section.isList("materials")) {
            for (String material : section.getStringList("materials")) {
                statistic.getMaterials().add(Material.getMaterial(material));
            }
        }

        return statistic;
    }

}
