package dev.m7wq.spigot.addons.bedwars.managers;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import dev.m7wq.spigot.addons.bedwars.BedWarsAddon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BedWarsManager {

    public Map<Player, List<Player>> party = new HashMap<>();


    public boolean isInArena(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);

        BedWars api = BedWarsAddon.getInstance().getAPI();

        return api.getArenaUtil().getArenaByPlayer(player)!=null;
    }

    public IArena getArena(UUID playerUUID){
        Player player = Bukkit.getPlayer(playerUUID);
        BedWars api = BedWarsAddon.getInstance().getAPI();

        return api.getArenaUtil().getArenaByPlayer(player);
    }

    public void joinArena(UUID uuid, String arenaName){

        Player player = Bukkit.getPlayer(uuid);

        player.performCommand("bw join "+arenaName);
    }

    public void spawn(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);

        player.performCommand("spawn");
    }

}
