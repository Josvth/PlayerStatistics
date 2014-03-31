package me.josvth.playerstatistics.statistics;

import me.josvth.playerstatistics.PlayerStatistics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.HashMap;
import java.util.Map;

public abstract class Statistic implements Listener {

    private final String type;

    protected Objective objective;

    protected Map<String, Score> scores;

    private String name;
    private String displayName;

    public Statistic(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if (objective != null) {
            if (displayName == null) {
                objective.setDisplayName(objective.getName());
            } else {
                objective.setDisplayName(displayName);
            }
        }
    }

    public void initialize(PlayerStatistics plugin) {
        objective = plugin.getScoreboard().registerNewObjective(getName(), "dummy");
        if (displayName != null) {
            objective.setDisplayName(displayName);
        }
        scores = new HashMap<String, Score>();
    }

    public void load(ConfigurationSection section) {
        if (section.isConfigurationSection("scores")) {

            ConfigurationSection scoreSection = section.getConfigurationSection("scores");

            for (String key : scoreSection.getKeys(false)) {
                setScore(key, scoreSection.getInt(key, 0));
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

        for (Map.Entry<String, Score> entry : getScores().entrySet()) {
            scoreSection.set(entry.getKey(), entry.getValue().getScore());
        }

    }

    public void unload() {

    }

    public Objective getObjective() {
        return objective;
    }

    public Map<String, Score> getScores() {
        return scores;
    }

    public int getScore(String player) {

        Score score = scores.get(player);

        if (score == null) {
            return 0;
        }

        return score.getScore();

    }

    public void setScore(String player, int value) {

        Score score = scores.get(player);

        if (score == null) {
            score = objective.getScore(Bukkit.getOfflinePlayer(player));
            scores.put(player, score);
        }

        score.setScore(value);

    }

}
