package me.commandkind.lifesteal.handlers;

import fr.mrmicky.fastboard.FastBoard;
import me.commandkind.lifesteal.Lifesteal;
import me.commandkind.lifesteal.classes.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerHandler implements Listener {
    Plugin plugin;
    Pvp pvp;
    Map<UUID, Combat> combats = new HashMap<>();
    Map<String, UUID> known = new HashMap<>();
    Map<Integer, Team> teams;
    Map<UUID, PlayerData> playerData;
    Scoreboard scoreboard;
    String netherName;
    String endName;
    Deaths deaths;
    Map<UUID, FastBoard> fastBoards;

    public PlayerHandler(Lifesteal plugin, Pvp pvp, Map<UUID, Combat> combats, Map<String, UUID> known, Map<Integer, Team> teams, Map<UUID, PlayerData> playerData, Scoreboard scoreboard, String netherName, String endName, Deaths deaths, Map<UUID, FastBoard> fastBoards) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.pvp = pvp;
        this.combats = combats;
        this.plugin = plugin;
        this.known = known;
        this.teams = teams;
        this.playerData = playerData;
        this.scoreboard = scoreboard;
        this.netherName = netherName;
        this.endName = endName;
        this.deaths = deaths;
        this.fastBoards = fastBoards;
    }

    @EventHandler
    public void onChatJoinClick(AsyncPlayerChatEvent event) {
        if (ChatColor.stripColor(event.getMessage()).equalsIgnoreCase("[join]")) {
            event.getPlayer().sendMessage("test");
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (teams.entrySet().stream().anyMatch(entry -> entry.getValue().containsPlayer(event.getPlayer().getUniqueId()))) {
            Team team = teams.entrySet().stream().filter(entry -> entry.getValue().containsPlayer(event.getPlayer().getUniqueId())).findFirst().orElse(null).getValue();
            if (team.getChatPrefix().isEmpty()) {
                event.setFormat(event.getPlayer().getName() + ChatColor.RED + " -> " + ChatColor.RESET + event.getMessage());
            }
            event.setMessage("[" + ChatColor.translateAlternateColorCodes('&', team.getChatPrefix()) + ChatColor.RESET + "] " + event.getPlayer().getName() + ChatColor.DARK_RED + " -> " + ChatColor.RESET + event.getMessage());
            event.setFormat(String.format(event.getMessage()));
        } else {
            event.setFormat(event.getPlayer().getName() + ChatColor.RED + " -> " + ChatColor.RESET + event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        known.put(player.getName(), player.getUniqueId());

        if (!playerData.containsKey(player.getUniqueId())) {
            playerData.put(player.getUniqueId(), new PlayerData());
            scoreboard.getTeam("default").addPlayer(player);
            player.sendMessage(ChatColor.GREEN + "Welcome!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Welcome back!");
            if (playerData.get(player.getUniqueId()).getStatus() != 0) {
                scoreboard.getTeam(teams.get(playerData.get(player.getUniqueId()).getTeam()).getName()).addPlayer(player);
            } else {
                scoreboard.getTeam("default").addPlayer(player);
            }
        }

        if (!deaths.getDeaths().containsKey(player.getUniqueId())) {
            deaths.addPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (!pvp.getPvp()) {
            event.setCancelled(true);
        } else {
            if (!combats.containsKey(event.getEntity().getUniqueId())) {
                event.getEntity().sendMessage(ChatColor.RED + "You are now in combat!");
                combats.put((event.getEntity().getUniqueId()), new Combat());
            } else {
                combats.get(event.getEntity().getUniqueId()).resetCombatTime();
            }
            if (!combats.containsKey(event.getDamager().getUniqueId())) {
                event.getDamager().sendMessage(ChatColor.RED + "You are now in combat!");
                combats.put((event.getDamager().getUniqueId()), new Combat());
            } else {
                combats.get(event.getDamager().getUniqueId()).resetCombatTime();
            }
        }
    }

    @EventHandler
    public void arrowAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            return;
        }
        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }
        if (!(((Arrow) event.getDamager()).getShooter() instanceof Player damager)) {
            return;
        }
        if (!pvp.getPvp()) {
            event.setCancelled(true);
        } else {
            if (!combats.containsKey(event.getEntity().getUniqueId())) {
                event.getEntity().sendMessage(ChatColor.RED + "You are now in combat!");
                combats.put((event.getEntity().getUniqueId()), new Combat());
            } else {
                combats.get(event.getEntity().getUniqueId()).resetCombatTime();
            }
            if (!combats.containsKey(damager.getUniqueId())) {
                damager.sendMessage(ChatColor.RED + "You are now in combat!");
                combats.put((damager.getUniqueId()), new Combat());
            } else {
                combats.get(damager.getUniqueId()).resetCombatTime();
            }
        }
    }

    @EventHandler
    public void tntExplosion(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            return;
        }
        if (!(event.getDamager() instanceof TNTPrimed)) {
            return;
        }
        if (!(((TNTPrimed) event.getDamager()).getSource() instanceof Player damager)) {
            return;
        }
        if (!combats.containsKey(event.getEntity().getUniqueId())) {
            event.getEntity().sendMessage(ChatColor.RED + "You are now in combat!");
            combats.put((event.getEntity().getUniqueId()), new Combat());
        } else {
            combats.get(event.getEntity().getUniqueId()).resetCombatTime();
        }
        if (!combats.containsKey(damager.getUniqueId())) {
            damager.sendMessage(ChatColor.RED + "You are now in combat!");
            combats.put((damager.getUniqueId()), new Combat());
        } else {
            combats.get(damager.getUniqueId()).resetCombatTime();
        }
    }

    @EventHandler
    public void crystalExplosion(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            return;
        }
        if (!(event.getDamager() instanceof EnderCrystal)) {
            return;
        }
        if (!pvp.getPvp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnchorExplosionDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (!event.getDamager().getType().equals(Material.RESPAWN_ANCHOR)) {
            return;
        }
        if (!pvp.getPvp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        combats.remove(player.getUniqueId());

        deaths.death(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        outOfCombat(event.getPlayer(), scoreboard, playerData, teams);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (pvp.getPvp()) {
            if (combats.containsKey(player.getUniqueId())) {
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (itemStack == null) {
                        continue;
                    }
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
                for (ItemStack itemStack : player.getInventory().getExtraContents()) {
                    if (itemStack == null) {
                        continue;
                    }
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
                player.getInventory().clear();
                combats.remove(player.getUniqueId());
                player.setHealth(0);
            }
        }
    }

    @EventHandler
    public void onSummonEntity(EntitySpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)) {
            if (!event.getLocation().getWorld().getName().equals(endName)) {
                if (!pvp.getPvp()) {
                    int i = 0;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (event.getLocation().distance(player.getLocation()) < 15) {
                            if (i >= 1) {
                                event.setCancelled(true);
                            }
                            i++;
                        }
                    }
                }
            } else {
                if (!pvp.getPvp()) {
                    event.setCancelled(true);
                }
            }
        }
    }


    public void outOfCombat(Player player, Scoreboard scoreboard, Map<UUID, PlayerData> playerData, Map<Integer, Team> teams) {
        player.sendMessage(ChatColor.GREEN + "You are no longer in combat!");
        if (scoreboard.getTeam("Combat") != null) {
            if (scoreboard.getTeam("Combat").hasPlayer(player)) {
                scoreboard.getTeam("Combat").removePlayer(player);
            }
        }
        if (playerData.get(player.getUniqueId()).getStatus() != 0) {
            scoreboard.getTeam(teams.get(playerData.get(player.getUniqueId()).getTeam()).getName()).addPlayer(player);
        } else {
            scoreboard.getTeam("default").addPlayer(player);
        }
    }
}
