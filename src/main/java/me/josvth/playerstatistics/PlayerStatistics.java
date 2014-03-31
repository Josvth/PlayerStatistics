package me.josvth.playerstatistics;

import com.conventnunnery.libraries.config.ConventYamlConfiguration;
import me.josvth.playerstatistics.statistics.*;
import me.josvth.playerstatistics.tasks.CarouselTask;
import me.josvth.playerstatistics.tasks.SaverTask;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PlayerStatistics extends JavaPlugin implements Listener {

    public static PlayerStatistics instance;

    private ConventYamlConfiguration generalConfiguration;
    private ConventYamlConfiguration statisticsConfiguration;

    private Map<String, Class<? extends Statistic>> registeredStatistics;

    private Scoreboard scoreboard;

    private List<Statistic> statistics;

    private BukkitTask saverTask;
    private BukkitTask carouselTask;

    @Override
    public void onEnable() {

        instance = this;

        // Load configuration
        generalConfiguration = new ConventYamlConfiguration(new File(getDataFolder(), "config.yml"), getDescription().getVersion());
        getGeneralConfiguration().setDefaults(getResource("config.yml"));
        getGeneralConfiguration().load();

        statisticsConfiguration = new ConventYamlConfiguration(new File(getDataFolder(), "statistics.yml"), getDescription().getVersion());
        getStatisticsConfiguration().setDefaults(getResource("statistics.yml"));
        getStatisticsConfiguration().load();

        registeredStatistics = new HashMap<String, Class<? extends Statistic>>();

        // Register statistics
        registeredStatistics.put("death", DeathStatistic.class);
        registeredStatistics.put("break", BreakStatistic.class);
        registeredStatistics.put("kill", KillStatistic.class);
        registeredStatistics.put("last-seen", LastSeenStatistic.class);
        registeredStatistics.put("online-time", OnlineTimeStatistic.class);

        scoreboard = getServer().getScoreboardManager().getNewScoreboard();
        statistics = new LinkedList<Statistic>();

        if (generalConfiguration.isConfigurationSection("statistics")) {

            final ConfigurationSection statisticsSection = generalConfiguration.getConfigurationSection("statistics");

            // Load statistics from config
            for (String key : statisticsSection.getKeys(false)) {

                final String type = statisticsSection.getString(key + ".type");
                final Class<? extends Statistic> clazz = registeredStatistics.get(statisticsSection.getString(key + ".type"));

                if (clazz != null) {

                    ConfigurationSection statisticSettings = statisticsSection.getConfigurationSection(key + ".settings");

                    if (statisticSettings == null) {
                        statisticSettings = statisticsSection.createSection("settings");
                    }

                    Statistic statistic = null;

                    try {
                        statistic = (Statistic) clazz.getMethod("deserialize", ConfigurationSection.class).invoke(null, statisticSettings);
                    } catch (Exception ignore) {
                        try {
                            statistic = clazz.getConstructor().newInstance();
                        } catch (Exception e) {
                            getLogger().log(Level.WARNING, "Could not initialize statistic: " + clazz.getName(), e);
                        }
                    }

                    if (statistic != null) {
                        statistic.setName(key);
                        statistics.add(statistic);
                        getServer().getPluginManager().registerEvents(statistic, this);
                    }

                } else {
                    getLogger().log(Level.WARNING, "Could not create statistic named '" + key + "' because the statistic class '" + type + "' does not exist.");
                }

            }

        }

        // Initialize statistics
        for (Statistic statistic : statistics) {
            statistic.initialize(this);
        }

        // Load statistics
        for (Statistic statistic : statistics) {
            if (getStatisticsConfiguration().isConfigurationSection(statistic.getName())) {
                statistic.load(getStatisticsConfiguration().getConfigurationSection(statistic.getName()));
            } else {
                statistic.load(getStatisticsConfiguration().createSection(statistic.getName()));
            }
        }

        // Start auto saver
        saverTask = getServer().getScheduler().runTaskTimer(this, new SaverTask(this), getGeneralConfiguration().getLong("saver-interval"), getGeneralConfiguration().getLong("saver-interval"));

        // Start carousel
        carouselTask = getServer().getScheduler().runTaskTimer(this, new CarouselTask(this), 0, getGeneralConfiguration().getLong("carousel-interval"));

        // Register join listener
        getServer().getPluginManager().registerEvents(this, this);

        // Set scoreboard for players
        for (Player player : getServer().getOnlinePlayers()) {
            player.setScoreboard(getScoreboard());
        }

    }

    @Override
    public void onDisable() {

        // Set to default scoreboard
        for (OfflinePlayer offlinePlayer : getScoreboard().getPlayers()) {
            if (offlinePlayer.isOnline()) {
                ((Player) offlinePlayer).setScoreboard(getServer().getScoreboardManager().getMainScoreboard());
            }
        }

        // Stop carousel
        if (carouselTask != null) {
            carouselTask.cancel();
        }

        // Stop auto saver
        if (saverTask != null) {
            saverTask.cancel();
        }

        // Save statistics
        new SaverTask(this).run();

        // Unload statistics
        for (Statistic statistic : statistics) {
            statistic.unload();
        }

        carouselTask = null;
        saverTask = null;
        statistics = null;
        scoreboard = null;

        registeredStatistics = null;

        statisticsConfiguration = null;
        generalConfiguration = null;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("stats")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }

            if (args.length > 0 && "off".equalsIgnoreCase(args[0])) {
                ((Player) sender).setScoreboard(getServer().getScoreboardManager().getMainScoreboard());
            } else {
                ((Player) sender).setScoreboard(getScoreboard());
            }

            return true;

        }

        return false;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setScoreboard(getScoreboard());
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public ConventYamlConfiguration getGeneralConfiguration() {
        return generalConfiguration;
    }

    public ConventYamlConfiguration getStatisticsConfiguration() {
        return statisticsConfiguration;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

}

