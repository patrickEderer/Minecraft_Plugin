package me.commandkind.lifesteal.classes;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    int team = 0;
    Map<Integer, Integer> invites = new HashMap<>();
    int status = 0;

    public PlayerData() {
    }

    public void setTeam(int team) {
        this.team = team;
    }
    public void addInvite(int invite) {
        invites.put(invite, 0);
    }
    public void removeInvite(int invite) {
        invites.remove(invite);
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() {
        return status;
    }
    public Map<Integer, Integer> getInvites() {
        return invites;
    }
    public int getTeam() {
        return team;
    }

    public void tick() {
        for (Integer invite : invites.keySet()) {
            invites.put(invite, invites.get(invite) + 1);
        }
    }
}
