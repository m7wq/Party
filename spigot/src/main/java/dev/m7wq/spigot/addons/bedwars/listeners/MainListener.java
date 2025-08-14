package dev.m7wq.spigot.addons.bedwars.listeners;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import dev.m7wq.spigot.Plugin;
import dev.m7wq.spigot.addons.bedwars.BedWarsAddon;
import dev.m7wq.spigot.addons.bedwars.managers.BedWarsManager;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MainListener implements Listener {

    Plugin plugin;

    private void warpRequest(Player player) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(output);

        data.writeUTF("WARP_REQUEST");
        data.writeUTF(player.getUniqueId().toString()); // FOR PLAYER (OWNER)

        player.sendPluginMessage(plugin,"BungeeCord",output.toByteArray());
    }


    @EventHandler
    public void onArenaJoin(PlayerJoinArenaEvent e) throws IOException {

        warpRequest(e.getPlayer());




    }

    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent e) throws IOException {
        warpRequest(e.getPlayer());
    }

    @EventHandler
    public void onTeamAssign(TeamAssignEvent e) {
        IArena arena = e.getArena();
        String group = arena.getGroup();
        int teamSize = getTeamSize(group);

        BedWarsManager manager = BedWarsAddon.getInstance().getManager();

        for (Player leader : arena.getPlayers()) {



            List<Player> party = manager.party.getOrDefault(leader,new ArrayList<>());
            if (party.isEmpty()) continue;

            ITeam leaderTeam = arena.getTeam(leader);
            if (leaderTeam == null) continue;

            int added = 1;
            for (Player member : party) {
                if (added >= teamSize) break;

                if (arena.isPlayer(member)) {
                    leaderTeam.reJoin(member, 0);
                    added++;
                }
            }
        }
    }

    private int getTeamSize(String group) {
        switch (group.toLowerCase()) {
            case "solo": return 1;
            case "doubles":
            case "2v2v2v2": return 2;
            case "trios":
            case "3v3v3v3": return 3;
            case "squads":
            case "4v4v4v4": return 4;
            default: return 1;
        }
    }


}
