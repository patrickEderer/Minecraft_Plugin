package me.commandkind.lifesteal.classes;

import fr.mrmicky.fastboard.FastBoard;
import me.commandkind.lifesteal.cmds.DeathCmd;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Deaths {

    Map<UUID, Integer> players = new HashMap<>();
    Map<String, UUID> known;
    Map<UUID, FastBoard> fastBoards;
    boolean displayDeaths = false;

    public Deaths(Map<String, UUID> known, Map<UUID, FastBoard> fastBoards) {
        this.known = known;
        this.fastBoards = fastBoards;
    }

    public void addPlayer(UUID uuid) {
        players.put(uuid, 0);
    }

    public void death(UUID uuid) {
        players.put(uuid, players.get(uuid) + 1);
    }

    public Map<UUID, Integer> getDeaths() {
        return players;
    }

    public void setDisplayDeaths(Boolean b) {
        displayDeaths = b;
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!fastBoards.containsKey(player.getUniqueId())) {
                if (displayDeaths) {
                    fastBoards.put(player.getUniqueId(), new FastBoard(player));
                    fastBoards.get(player.getUniqueId()).updateTitle(ChatColor.translateAlternateColorCodes('&', "&c&k1&4&lDeaths&c&k1"));
                }
            } else {
                if (displayDeaths) {
                    fastBoards.remove(player.getUniqueId());
                    fastBoards.put(player.getUniqueId(), new FastBoard(player));
                    List<String> lines = new ArrayList<>();
                    for (Map.Entry<UUID, Integer> entry : players.entrySet()) {
                        lines.add(ChatColor.RED + known.entrySet().stream().filter(knownEntry -> knownEntry.getValue().equals(entry.getKey())).findFirst().orElse(null).getKey() + ": " + ChatColor.RESET + entry.getValue());
                    }
                    fastBoards.get(player.getUniqueId()).updateLines(lines);
                    fastBoards.get(player.getUniqueId()).updateTitle(ChatColor.translateAlternateColorCodes('&', "&c&k1&4&lDeaths&c&k1"));
                } else {
                    if (!fastBoards.get(player.getUniqueId()).isDeleted()) {
                        fastBoards.get(player.getUniqueId()).delete();
                        fastBoards.remove(player.getUniqueId());
                    }
                }
            }
        }
    }
}
