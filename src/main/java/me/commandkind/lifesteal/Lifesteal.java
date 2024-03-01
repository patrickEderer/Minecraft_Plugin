package me.commandkind.lifesteal;

import fr.mrmicky.fastboard.FastBoard;
import me.commandkind.lifesteal.classes.*;
import me.commandkind.lifesteal.cmds.DeathCmd;
import me.commandkind.lifesteal.cmds.PvpCmd;
import me.commandkind.lifesteal.cmds.TeamCmd;
import me.commandkind.lifesteal.handlers.PlayerHandler;
import me.commandkind.lifesteal.util.ConfigUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public final class Lifesteal extends JavaPlugin {

    private BukkitAudiences adventure;

    public BukkitAudiences adventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    public Map<Integer, Team> teams = new HashMap<>();
    public Map<String, UUID> known = new HashMap<>();
    public Map<UUID, PlayerData> playerData = new HashMap<>();
    public Map<UUID, FastBoard> fastBoards = new HashMap<>();

    @Override
    public void onEnable() {
        // Load Msg
        getLogger().info("Loading...");

        this.adventure = BukkitAudiences.create(this);

        //main code
        Pvp pvp = new Pvp();
        Map<UUID, Combat> combats = new HashMap<>();
        Deaths deaths = new Deaths(known, fastBoards);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        long combatTime;
        int prefixCharLimit;
        int chatPrefixCharLimit;
        int inviteExpireTime;
        String netherName;
        String endName;

        saveDefaultConfig();
        saveConfig();

        ConfigUtil dataConfig = new ConfigUtil(this, "data.yml");

        if (dataConfig.getConfig().isConfigurationSection("teams")) {
            for (String ID : dataConfig.getConfig().getConfigurationSection("teams").getKeys(false)) {
                int id = Integer.parseInt(ID);
                String name = dataConfig.getConfig().getString("teams." + id + ".name");
                assert name != null;
                teams.put(id, new Team(UUID.fromString(dataConfig.getConfig().getString("teams." + id + ".leader")), name));
                if (!name.equals("empty")) {
                    scoreboard.registerNewTeam(name);
                    scoreboard.getTeam(name).setPrefix(dataConfig.getConfig().getString("teams." + id + ".prefix"));
                    teams.get(id).setPrefix(dataConfig.getConfig().getString("teams." + id + ".prefix"));
                    teams.get(id).setChatPrefix(dataConfig.getConfig().getString("teams." + id + ".chatPrefix"));
                    for (String uuid : dataConfig.getConfig().getStringList("teams." + id + ".players")) {
                        teams.get(id).addPlayer(UUID.fromString(uuid));
                        scoreboard.getTeam(name).addPlayer(Bukkit.getOfflinePlayer(uuid));
                    }
                }
            }
        }
        if (dataConfig.getConfig().isConfigurationSection("known")) {
            for (String name : dataConfig.getConfig().getConfigurationSection("known").getKeys(false)) {
                known.put(name, UUID.fromString(dataConfig.getConfig().getString("known." + name)));
            }
        }
        if (dataConfig.getConfig().isConfigurationSection("playerData")) {
            for (String id : dataConfig.getConfig().getConfigurationSection("playerData").getKeys(false)) {
                UUID uuid = UUID.fromString(id);
                playerData.put(uuid, new PlayerData());
                playerData.get(uuid).setStatus(dataConfig.getConfig().getInt("playerData." + id + ".status"));
                playerData.get(uuid).setTeam(dataConfig.getConfig().getInt("playerData." + id + ".team"));
            }
        }

        ConfigUtil config = new ConfigUtil(this, "config.yml");
        if (config.getConfig().isLong("pvp.CombatTime")) {
            combatTime = (config.getConfig().getLong("pvp.CombatTime") * 1000);
        } else {
            combatTime = 5000L;
        }
        if (config.getConfig().isLong("pvp.NetherWorldName")) {
            netherName = config.getConfig().getString("pvp.NetherWorldName");
        } else {
            netherName = "world_nether";
        }
        if (config.getConfig().isLong("pvp.EndWorldName")) {
            endName = config.getConfig().getString("pvp.EndWorldName");
        } else {
            endName = "end_nether";
        }
        if (config.getConfig().isInt("team.MaxPrefixLength")) {
            prefixCharLimit = config.getConfig().getInt("team.MaxPrefixLength");
        } else {
            prefixCharLimit = 20;
        }
        if (config.getConfig().isInt("team.MaxChatPrefixLength")) {
            chatPrefixCharLimit = config.getConfig().getInt("team.MaxChatPrefixLength");
        } else {
            chatPrefixCharLimit = 5;
        }
        if (config.getConfig().isInt("team.InviteExpireTime")) {
            inviteExpireTime = config.getConfig().getInt("team.InviteExpireTime");
        } else {
            inviteExpireTime = 60;
        }

        scoreboard.registerNewTeam("Combat");
        scoreboard.getTeam("Combat").setPrefix("§4§l[Combat] ");

        scoreboard.registerNewTeam("default");

        Map<UUID, BossBar> bossBars = new HashMap<>();
//      ticks
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();

            if (!combats.isEmpty()) {
                Iterator<UUID> iterator = combats.keySet().iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    if (uuid == null) continue;
                    if (!bossBars.containsKey(uuid)) {
                        bossBars.put(uuid, Bukkit.createBossBar(
                                ChatColor.DARK_RED + "Combat",
                                BarColor.RED,
                                BarStyle.SOLID,
                                BarFlag.DARKEN_SKY
                        ));
                    }
                    bossBars.get(uuid).setVisible(pvp.getBossbarEnable());
                    if (!((currentTime - combatTime) >= combats.get(uuid).getCombatTime())) {
                        bossBars.get(uuid).setProgress(1.0 - ((double) (currentTime - combats.get(uuid).getCombatTime()) / combatTime));
                    }
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;
                    bossBars.get(uuid).addPlayer(player);

                    scoreboard.getTeam("Combat").addPlayer(player);

                    if (((currentTime - combatTime) >= combats.get(uuid).getCombatTime())) {
                        player.sendMessage(ChatColor.GREEN + "You are no longer in combat!");
                        scoreboard.getTeam("Combat").removePlayer(player);
                        updateTeam(player, scoreboard, playerData, teams, new HashMap<>());
                        bossBars.get(uuid).setVisible(false);
                        bossBars.remove(uuid);
                        iterator.remove();
                    }
                }
            }
        }, 0, 1);

        //slow updates
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Team team : teams.values()) {
                if (!team.getName().equals("empty")) {
                    if (!team.getPrefix().isEmpty()) {
                        scoreboard.getTeam(team.getName()).setPrefix("[" + ChatColor.translateAlternateColorCodes('&', team.getPrefix()) + ChatColor.RESET + "] ");
                    } else {
                        scoreboard.getTeam(team.getName()).setPrefix("");
                    }
                }
            }
            pvp.tick();
        }, 0, 10);

        //seconds updates
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : playerData.keySet()) {
                for (Integer invite : playerData.get(uuid).getInvites().keySet()) {
                    if (playerData.get(uuid).getInvites().get(invite) >= inviteExpireTime) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.sendMessage(ChatColor.RED + "Invite from team \"" + teams.get(invite).getName() + "\" has expired!");
                        }

                        playerData.get(uuid).removeInvite(invite);
                    } else {
                        playerData.get(uuid).tick();
                    }
                }
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTeam(player, scoreboard, playerData, teams, combats);
            }
            deaths.tick();
        }, 0, 20);

        // Commands
        getCommand("pvp").setExecutor(new PvpCmd(pvp));
        getCommand("team").setExecutor(new TeamCmd(teams, known, scoreboard, prefixCharLimit, chatPrefixCharLimit, playerData, this));
        getCommand("deaths").setExecutor(new DeathCmd(deaths, known));

        // Tasks
        new PlayerHandler(this, pvp, combats, known, teams, playerData, scoreboard, netherName, endName, deaths, fastBoards);


        getLogger().info("Loaded!");
    }

    public void updateTeam(Player player, Scoreboard scoreboard, Map<UUID, PlayerData> playerData, Map<Integer, Team> teams, Map<UUID, Combat> combats) {
        player.setScoreboard(scoreboard);
        if (!combats.isEmpty()) {
            if (combats.containsKey(player.getUniqueId())) {
                return;
            }
        }
        if (playerData.get(player.getUniqueId()).getStatus() != 0) {
            scoreboard.getTeam(teams.get(playerData.get(player.getUniqueId()).getTeam()).getName()).addPlayer(player);
        } else {
            scoreboard.getTeam("default").addPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        ConfigUtil configOld = new ConfigUtil(this, "data.yml");
        configOld.getFile().delete();
        ConfigUtil config = new ConfigUtil(this, "data.yml");

        for (int i : teams.keySet()) {
            config.getConfig().set("teams." + i + ".name", teams.get(i).getName());
            config.getConfig().set("teams." + i + ".leader", String.valueOf(teams.get(i).getLeader()));
            config.getConfig().set("teams." + i + ".prefix", teams.get(i).getPrefix());
            config.getConfig().set("teams." + i + ".chatPrefix", teams.get(i).getChatPrefix());

            List<String> players = new ArrayList<>();
            for (UUID uuid : teams.get(i).getPlayers()) {
                players.add(String.valueOf(uuid));
            }
            config.getConfig().set("teams." + i + ".players", players);
        }
        for (String name : known.keySet()) {
            config.getConfig().set("known." + name, String.valueOf(known.get(name)));
        }
        for (UUID uuid : playerData.keySet()) {
            String id = String.valueOf(uuid);
            config.getConfig().set("playerData." + id + ".status", playerData.get(uuid).getStatus());
            config.getConfig().set("playerData." + id + ".team", playerData.get(uuid).getTeam());
        }
        config.save();
    }
}
