package me.commandkind.lifesteal.classes;


public class Combat {

    private long inCombatSince;

    public Combat() {
        inCombatSince = System.currentTimeMillis();
    }

    public long getCombatTime() {
        return inCombatSince;
    }

    public void resetCombatTime() {
        inCombatSince = System.currentTimeMillis();
    }
}
