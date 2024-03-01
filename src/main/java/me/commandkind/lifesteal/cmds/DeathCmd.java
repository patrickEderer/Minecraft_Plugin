package me.commandkind.lifesteal.cmds;

import me.commandkind.lifesteal.classes.Deaths;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathCmd implements CommandExecutor, TabCompleter {

    Deaths deaths;
    Map<String, UUID> known;
    public boolean displayDeaths = false;

    public DeathCmd() {
    }

    public DeathCmd(Deaths deaths, Map<String, UUID> known) {
        this.deaths = deaths;
        this.known = known;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4-----&lDeaths&4-----"));
            if (deaths.getDeaths() != null) {
                for (Map.Entry<UUID, Integer> entry : deaths.getDeaths().entrySet()) {
                    player.sendMessage(ChatColor.RED + known.entrySet().stream().filter(knownEntry -> knownEntry.getValue().equals(entry.getKey())).findFirst().orElse(null).getKey() + ": " + entry.getValue());
                }
            }
            return true;
        } else if (args.length == 1) {
            if (player.isOp()) {
                switch (args[0]) {
                    case "toggle" -> {
                        displayDeaths = !displayDeaths;
                        player.sendMessage((displayDeaths ? ChatColor.GREEN : ChatColor.RED) + "Now " + (displayDeaths ? "showing" : "hiding") + " deaths!");
                        return true;
                    }
                    case "enable" -> {
                        deaths.setDisplayDeaths(true);
                        displayDeaths = true;
                        player.sendMessage(ChatColor.GREEN + "Now showing deaths!");
                    }
                    case "disable" -> {
                        deaths.setDisplayDeaths(false);
                        displayDeaths = false;
                        player.sendMessage(ChatColor.RED + "Now hiding deaths!");
                    }
                }
            } else {
                player.sendMessage("You don't have the permission to do that!");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender.isOp()) {
            return List.of("toggle", "enable", "disable").stream().filter(s -> s.startsWith(args[0])).toList();
        }
        return List.of();
    }
}
