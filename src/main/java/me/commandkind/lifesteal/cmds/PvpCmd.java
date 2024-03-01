package me.commandkind.lifesteal.cmds;

import me.commandkind.lifesteal.classes.Pvp;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PvpCmd implements CommandExecutor, TabCompleter {

    Pvp pvp;

    public PvpCmd(Pvp pvp) {
        this.pvp = pvp;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                return pvp.getPvp();
            }
            sender.sendMessage(ChatColor.RED + "Missing argument: <toggle/disable/enable>");
            return true;
        }
        if (args.length == 1) {
            switch (args[0]) {
                case "toggle" -> {
                    pvp.setPvp(!pvp.getPvp());

                    Player player = (Player) sender;
                    player.sendMessage(((pvp.getPvp() ? ChatColor.GREEN : ChatColor.RED)) + "Pvp turned " + ((pvp.getPvp() ? "on" : "off")) + "!");
                    player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
                case "enable" -> {
                    pvp.setPvp(true);
                    pvp.setRandom(false);

                    Player player = (Player) sender;
                    player.sendMessage(ChatColor.GREEN + "Pvp turned on!");
                }
                case "disable" -> {
                    pvp.setPvp(false);
                    pvp.setRandom(false);

                    Player player = (Player) sender;
                    player.sendMessage(ChatColor.RED + "Pvp turned off!");
                }
                case "random" -> {
                    sender.sendMessage(ChatColor.GREEN + "Success! " + "Pvp will now toggle on and off at random intervals!");
                    pvp.setTimer((long) (Math.floor(Math.random() * 6) * 60000L) + 600000L);
                    pvp.setRandom(true);
                    return true;
                }
                case "bossbar" -> {
                    sender.sendMessage(ChatColor.RED + "Missing argument <on / off>");
                    return true;
                }
            }
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "enable" -> {
                    if (args[1].length() != 8) {
                        sender.sendMessage(ChatColor.RED + "False format. HH:MM:SS");
                        return true;
                    }
                    long time = 0L;
                    String s = args[1];
                    time += (s.charAt(0) - '0') * 36000L;
                    time += (s.charAt(1) - '0') * 3600L;
                    time += (s.charAt(3) - '0') * 600L;
                    time += (s.charAt(4) - '0') * 60L;
                    time += (s.charAt(6) - '0') * 10L;
                    time += (s.charAt(7) - '0');
                    time *= 1000L;
                    pvp.setTimer(time);
                }
                case "bossbar" -> {
                    if (args[1].equals("on")) {
                        pvp.enableBossbar(true);
                        sender.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "The combat bossbar will now be shown!");
                    }
                    if (args[1].equals("off")) {
                        pvp.enableBossbar(false);
                        sender.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "The combat bossbar will now be hidden!");
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions;
        if (args.length == 1) {
            completions = new ArrayList<>(Arrays.asList("toggle", "enable", "disable", "random", "bossbar"));
        } else {
            if (args[0].equals("bossbar")) {
                return new ArrayList<>(List.of("on", "off").stream().filter(s -> s.startsWith(args[1])).toList());
            }
            completions = new ArrayList<>();
        }
        return completions.stream().filter(s -> s.startsWith(args[0])).toList();
    }
}