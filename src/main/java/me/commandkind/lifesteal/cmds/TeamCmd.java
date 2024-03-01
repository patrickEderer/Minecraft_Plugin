package me.commandkind.lifesteal.cmds;

import me.commandkind.lifesteal.Lifesteal;
import me.commandkind.lifesteal.classes.PlayerData;
import me.commandkind.lifesteal.classes.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TeamCmd implements CommandExecutor, TabCompleter {

    Map<Integer, Team> teams;
    Map<String, UUID> known;
    Scoreboard scoreboard;
    Integer prefixCharLimit;
    Integer chatPrefixCharLimit;
    Map<UUID, PlayerData> playerData;
    Lifesteal lifesteal;

    public TeamCmd(Map<Integer, Team> teams, Map<String, UUID> known, Scoreboard scoreboard, Integer prefixCharLimit, Integer chatPrefixCharLimit, Map<UUID, PlayerData> playerData, Lifesteal lifesteal) {
        this.teams = teams;
        this.known = known;
        this.scoreboard = scoreboard;
        this.prefixCharLimit = prefixCharLimit;
        this.chatPrefixCharLimit = chatPrefixCharLimit;
        this.playerData = playerData;
        this.lifesteal = lifesteal;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        switch (args.length) {
            case 0 -> {
                player.sendMessage(ChatColor.RED + "Missing argument(s)!");
                return true;
            }
            case 1 -> {
                if (playerData.get(player.getUniqueId()).getStatus() == 2) {
                    switch (args[0]) {
                        case "kick" -> {
                            player.sendMessage(ChatColor.RED + "Missing argument: <player>");
                            return true;
                        }
                        case "prefix" -> {
                            player.sendMessage(ChatColor.RED + "Missing argument: <prefix>");
                            return true;
                        }
                        case "chatPrefix" -> {
                            player.sendMessage(ChatColor.RED + "Missing argument: <chatPrefix>");
                            return true;
                        }
                        case "discard" -> {
                            player.sendMessage(ChatColor.RED + "Are you sure you want to delete your team? If yes do /team discard confirm");
                            return true;
                        }
                    }
                } else if (playerData.get(player.getUniqueId()).getStatus() == 1) {
                    if (args[0].equals("leave")) {
                        teams.get(playerData.get(player.getUniqueId()).getTeam()).removePlayer(player.getUniqueId());
                        playerData.get(player.getUniqueId()).setTeam(0);
                        playerData.get(player.getUniqueId()).setStatus(0);
                        player.sendMessage(ChatColor.GREEN + "Success!");
                    }
                } else {
                    switch (args[0]) {
                        case "create" -> {
                            player.sendMessage(ChatColor.RED + "Missing argument: <team name>");
                            return true;
                        }
                        case "invites" -> {
                            player.sendMessage(ChatColor.GOLD + "-----Invites----");
                            for (int i : playerData.get(player.getUniqueId()).getInvites().keySet()) {
                                player.sendMessage(teams.get(i).getName());
                            }
                        }
                    }
                }
                if (args[0].equals("list")) {
                    player.sendMessage(ChatColor.GOLD + "-----Teams-----");
                    for (Team team : teams.values()) {
                        if (team.getName().equals("empty")) continue;
                        player.sendMessage(team.getName());
                    }
                }
            }
            case 2 -> {
                if (playerData.get(player.getUniqueId()).getStatus() == 2) {
                    switch (args[0]) {
                        case "kick" -> {
                            Player p = Bukkit.getPlayer(args[1]);
                            if (p != null) {
                                if (p.getUniqueId() == player.getUniqueId()) {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "You can't kick yourself.");
                                    return true;
                                }

                                if (teams.get(playerData.get(player.getUniqueId()).getTeam()).containsPlayer(p.getUniqueId())) {
                                    teams.get(playerData.get(player.getUniqueId()).getTeam()).removePlayer(p.getUniqueId());
                                    playerData.get(p.getUniqueId()).setTeam(0);
                                    playerData.get(p.getUniqueId()).setStatus(0);
                                    scoreboard.getTeam("default").addPlayer(p);
                                    p.sendMessage(ChatColor.RED + "You have been kicked from your team");
                                    player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Kicked " + p.getName() + " from your team.");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player is not in your team.");
                                }
                            } else {
                                if (known.containsKey(args[1])) {
                                    UUID uuid = known.get(args[1]);
                                    if (teams.get(playerData.get(player.getUniqueId()).getTeam()).containsPlayer(uuid)) {
                                        teams.get(playerData.get(player.getUniqueId()).getTeam()).removePlayer(uuid);
                                        playerData.get(uuid).setTeam(0);
                                        playerData.get(uuid).setStatus(0);
                                        player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Kicked " + args[1] + " from your team.");
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player is not in your team.");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player could not be found.");
                                }
                            }
                            return true;
                        }
                        case "invite" -> {
                            Player p = Bukkit.getPlayer(args[1]);
                            if (p != null) {
                                if (playerData.get(p.getUniqueId()).getStatus() == 0) {
                                    playerData.get(p.getUniqueId()).addInvite(playerData.get(player.getUniqueId()).getTeam());
                                    player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Invited " + p.getName() + " to your team!");
                                    p.sendMessage(ChatColor.GREEN + player.getName() + " invited you to join his team \"" + teams.get(playerData.get(player.getUniqueId()).getTeam()).getName() + "\"!");
                                    lifesteal.adventure().player(p).sendMessage(Component.text("[join]")
                                            .color(NamedTextColor.GREEN)
                                            .decorate(TextDecoration.BOLD)
                                            .clickEvent(ClickEvent.runCommand("/team join " + teams.get(playerData.get(player.getUniqueId()).getTeam()).getName()))
                                            .hoverEvent(HoverEvent.showText(Component.text("Click").color(NamedTextColor.GREEN))));
                                    p.sendMessage("or do /team join " + teams.get(playerData.get(player.getUniqueId()).getTeam()).getName() + " to join.");
                                } else {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player is already in a team.");
                                }
                            } else {
                                if (known.containsKey(args[1])) {
                                    UUID uuid = known.get(args[1]);
                                    if (playerData.get(uuid).getStatus() == 0) {
                                        playerData.get(uuid).addInvite(playerData.get(player.getUniqueId()).getTeam());
                                        player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Invited " + args[1] + " to your team!");
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player is already in a team.");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "This player could not be found.");
                                }
                            }
                            return true;
                        }
                        case "discard" -> {
                            if (args[1].equals("confirm")) {
                                int index = playerData.get(player.getUniqueId()).getTeam();
                                for (UUID uuid : teams.get(index).getPlayers()) {
                                    playerData.get(uuid).setStatus(0);
                                    playerData.get(uuid).setTeam(0);
                                    if (scoreboard.getTeam(teams.get(index).getName()) != null) {
                                        scoreboard.getTeam(teams.get(index).getName()).removePlayer(Bukkit.getOfflinePlayer(uuid));
                                        scoreboard.getTeam(teams.get(index).getName()).unregister();
                                    }
                                }
                                teams.remove(index);
                                teams.put(index, new Team(UUID.randomUUID(), "empty"));
                                playerData.get(player.getUniqueId()).setStatus(0);
                                player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Deleted your team.");
                            }
                            return true;
                        }
                    }
                } else if (playerData.get(player.getUniqueId()).getStatus() == 0) {
                    switch (args[0]) {
                        case "create" -> {
                            if (args[1].equals("empty") || args[1].equals("default") || args[1].equals("Combat")) {
                                player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "Invalid team name.");
                                return true;
                            }
                            if (!teams.isEmpty()) {
                                if (teams.entrySet().stream().anyMatch(entry -> entry.getValue().getName().equalsIgnoreCase(args[1]))) {
                                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "A team with that name already exists.");
                                    return true;
                                }
                            }
                            teams.put(teams.size() + 1, new Team(player.getUniqueId(), args[1]));
                            teams.get(teams.size()).addPlayer(player.getUniqueId());
                            scoreboard.registerNewTeam(args[1]);
                            scoreboard.getTeam(args[1]).addPlayer(player);
                            player.setScoreboard(scoreboard);
                            playerData.get(player.getUniqueId()).setStatus(2);
                            playerData.get(player.getUniqueId()).setTeam(teams.size());
                            player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Created team " + args[1] + ".");
                            return true;
                        }
                        case "join" -> {
                            if (playerData.get(player.getUniqueId()).getInvites().entrySet().stream().anyMatch(entry -> teams.get(entry.getKey()).getName().equals(args[1]))) {
                                Integer index = teams.entrySet().stream().filter(entry -> entry.getValue().getName().equals(args[1])).findFirst().orElse(null).getKey();
                                if (index == null) {
                                    player.sendMessage(ChatColor.RED + "Error! " + ChatColor.RESET + "Code: 1.\nPlease share this code with an admin!");
                                }
                                playerData.get(player.getUniqueId()).setStatus(1);
                                playerData.get(player.getUniqueId()).setTeam(index);
                                scoreboard.getTeam(teams.get(index).getName()).addPlayer(player);
                                teams.get(index).addPlayer(player.getUniqueId());
                                player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "You joined team " + args[1] + ".");
                                playerData.get(player.getUniqueId()).removeInvite(index);
                            } else {
                                player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "Team or invite not found.");
                            }
                            return true;
                        }
                    }
                }
                if (playerData.get(player.getUniqueId()).getStatus() != 0 && args[0].equals("list") && args[1].equals("team")) {
                    player.sendMessage(ChatColor.GOLD + "-----Leader-----");
                    Player leader = Bukkit.getPlayer(teams.get(playerData.get(player.getUniqueId()).getTeam()).getLeader());
                    if (leader != null) {
                        player.sendMessage(leader.getName());
                    } else {
                        player.sendMessage(known.entrySet().stream().filter(l -> teams.get(playerData.get(player.getUniqueId()).getTeam()).getLeader().equals(l.getValue())).findFirst().orElse(null).getKey());
                    }
                    player.sendMessage(ChatColor.GOLD + "-----Players-----");
                    for (UUID uuid : teams.get(playerData.get(player.getUniqueId()).getTeam()).getPlayers()) {
                        if (uuid.equals(teams.get(playerData.get(player.getUniqueId()).getTeam()).getLeader())) {
                            continue;
                        }
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            player.sendMessage(player.getName());
                        } else {
                            player.sendMessage(known.entrySet().stream().filter(p1 -> p1.getValue().equals(uuid)).findFirst().orElse(null).getKey());
                        }
                    }
                }
            }
        }
        if (args.length > 1 && playerData.get(player.getUniqueId()).getStatus() != 0) {
            if (args[0].equals("chat")) {
                StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ChatColor.RED + "Team " + ChatColor.RESET + player.getName() + ChatColor.RED + " -> ");
                for (String s : args) {
                    if (s.equals("chat")) continue;
                    stringBuilder.append(s + " ");
                }
                for (UUID uuid : teams.get(playerData.get(player.getUniqueId()).getTeam()).getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(stringBuilder.toString());
                    }
                }
            }
        }
        if (args.length >= 2 && playerData.get(player.getUniqueId()).getStatus() == 2) {
            if (args[0].equals("prefix")) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : args) {
                    if (s.equals("prefix")) {
                        continue;
                    }
                    stringBuilder.append(s);
                    stringBuilder.append(" ");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                String prefix = stringBuilder.toString();
                int length = 0;
                for (Character c : prefix.toCharArray()) {
                    if (c.equals('&')) {
                        length -= 2;
                    } else {
                        length++;
                    }
                }
                if (length > prefixCharLimit) {
                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "Max character limit of " + prefixCharLimit + " exceeded.");
                    return true;
                }
                teams.get(playerData.get(player.getUniqueId()).getTeam()).setPrefix(prefix);
                player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Set the team's prefix to " + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.RESET + ".\n" + ChatColor.GRAY + ChatColor.ITALIC + "Hint: when creating prefixes you can use minecraft color/format codes");
                return true;
            }
            if (args[0].equals("chatPrefix")) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String s : args) {
                    if (s.equals("chatPrefix")) {
                        continue;
                    }
                    stringBuilder.append(s);
                    stringBuilder.append(" ");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                String prefix = stringBuilder.toString();
                int length = 0;
                for (Character c : prefix.toCharArray()) {
                    if (c.equals('&')) {
                        length -= 1;
                    } else {
                        length++;
                    }
                }
                if (length > chatPrefixCharLimit) {
                    player.sendMessage(ChatColor.RED + "Failed! " + ChatColor.RESET + "Max character limit of " + chatPrefixCharLimit + " exceeded.");
                    return true;
                }
                teams.get(playerData.get(player.getUniqueId()).getTeam()).setChatPrefix(prefix);
                player.sendMessage(ChatColor.GREEN + "Success! " + ChatColor.RESET + "Set the team's chat prefix to " + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.RESET + ".\n" + ChatColor.GRAY + ChatColor.ITALIC + "Hint: when creating prefixes you can use minecraft color/format codes");
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (playerData.get(player.getUniqueId()).getStatus() == 2) {
                completions = new ArrayList<>(Arrays.asList("kick", "invite", "prefix", "chatPrefix", "discard", "list", "chat"));
            } else if (playerData.get(player.getUniqueId()).getStatus() == 1) {
                completions = new ArrayList<>(Arrays.asList("leave", "list", "chat"));
            } else {
                completions = new ArrayList<>(Arrays.asList("create", "join", "invites", "list"));
            }
        } else if (args.length == 2) {
            if (args[0].equals("create")) {
                return List.of("<name>");
            }
            if (args[0].equals("discard")) {
                return List.of("confirm");
            }
            if (args[0].equals("join")) {
                for (int i : playerData.get(player.getUniqueId()).getInvites().keySet()) {
                    completions.add(teams.get(i).getName());
                }
                return completions.stream().filter(s -> s.startsWith(args[0])).toList();
            }
            if (args[0].equals("list")) {
                return List.of("team");
            }
            if (args[0].equals("invite")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (playerData.get(p.getUniqueId()).getStatus() != 0) continue;
                    completions.add(p.getName());
                }
                return completions.stream().filter(s -> s.startsWith(args[1])).toList();
            }
            if (args[0].equals("kick")) {
                for (UUID uuid : teams.get(playerData.get(player.getUniqueId()).getTeam()).getPlayers()) {
                    if (uuid.equals(player.getUniqueId())) continue;
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null) continue;
                    completions.add(p.getName());
                }
                return completions.stream().filter(s -> s.startsWith(args[1])).toList();
            }
        }
        return completions.stream().filter(s -> s.startsWith(args[0])).toList();
    }
}