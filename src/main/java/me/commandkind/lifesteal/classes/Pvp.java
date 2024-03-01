package me.commandkind.lifesteal.classes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Pvp {

    boolean pvp = false;
    Long timer = 0L;
    boolean timeOn;
    Long onTime;
    boolean enableBossbar = false;
    boolean random = false;
    List<Long> randomSaves = new ArrayList<>(List.of(0L));

    BossBar bossBar = Bukkit.createBossBar(
            "",
            BarColor.RED,
            BarStyle.SOLID
    );

    public Pvp() {
    }

    public boolean getPvp() {
        return pvp;
    }

    public void setPvp(Boolean b) {
        pvp = b;
        timer = 0L;
        timeOn = false;
        bossBar.setProgress(1);
        if (b) {
            bossBar.setTitle(ChatColor.RED + "" + ChatColor.BOLD + "PvP on");
            bossBar.setVisible(true);
        } else {
            bossBar.setVisible(false);
        }
    }

    public void setTimer(Long time) {
        onTime = time;
        bossBar.setVisible(true);
        timer = System.currentTimeMillis() + time;
        this.timeOn = true;
        pvp = true;
    }

    public void enableBossbar(Boolean b) {
        enableBossbar = b;
    }

    public void setRandom(Boolean b) {
        random = b;
    }

    public boolean getBossbarEnable() {
        return enableBossbar;
    }

    public void tick() {
        if (timeOn) {
            int seconds = Math.round(((float) (timer - System.currentTimeMillis()) / 1000) % 60);
            int minutes = (int) Math.floor((float) (((timer - System.currentTimeMillis())) / 60000) % 60);
            int hours = (int) Math.floor((float) (((timer - System.currentTimeMillis())) / 3600000));
            bossBar.setTitle(ChatColor.RED + "PvP is now enabled for " + hours + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" + (seconds >= 10 ? seconds : "0" + seconds));
            if ((timer - System.currentTimeMillis()) <= 0) {
                bossBar.setProgress(0);
                bossBar.setVisible(false);
                timeOn = false;
                pvp = false;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.RED + "Pvp is now off!");
                }
                if (random) {
                    Bukkit.getPlayer("commandkind").sendMessage("test");
                    randomSaves.set(0, (long) (System.currentTimeMillis() + (Math.random() * 36000000L)));
                }
            } else {
                bossBar.setVisible(true);
                bossBar.setProgress(((double) ((timer - System.currentTimeMillis()))) / onTime);
                pvp = true;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossBar.addPlayer(player);
            }
        } else {
            if (pvp) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    bossBar.addPlayer(player);
                }
            }
        }
        if (random) {
            if (randomSaves.get(0) != 0L) {
                if (System.currentTimeMillis() >= randomSaves.get(0)) {
                    randomSaves.set(0, 0L);
                    timeOn = true;
                    setTimer((long) (Math.floor(Math.random() * 6) * 600000L) + 600000L);
                }
            }
        }
    }
}