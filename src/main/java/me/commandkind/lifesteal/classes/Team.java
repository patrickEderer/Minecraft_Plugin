package me.commandkind.lifesteal.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

    List<UUID> players = new ArrayList<>();
    UUID leader;
    String name;
    String prefix = "";
    String chatPrefix = "";

    public Team(UUID leader, String name) {
        this.leader = leader;
        this.name = name;
    }


    public boolean containsPlayer(UUID uuid) {
        return players.contains(uuid);
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public void setChatPrefix(String chatPrefix) {
        this.chatPrefix = chatPrefix;
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }
}
