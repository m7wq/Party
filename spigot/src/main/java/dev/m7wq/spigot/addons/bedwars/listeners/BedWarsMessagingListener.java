package dev.m7wq.spigot.addons.bedwars.listeners;

import dev.m7wq.spigot.addons.bedwars.BedWarsAddon;
import dev.m7wq.spigot.addons.bedwars.managers.BedWarsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BedWarsMessagingListener implements PluginMessageListener {



    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        DataInputStream data = new DataInputStream(input);

        try {
            String subChannel = data.readUTF();

            if (!subChannel.equals("BEDWARS_WARP"))
                return;

            String[] players = data.readUTF().split(",");

            String owner = data.readUTF();

            BedWarsManager manager = BedWarsAddon.getInstance().getManager();

            UUID uuidOwner = UUID.fromString(owner);
            if (manager.isInArena(uuidOwner)){

                for (String string : players){
                    UUID uuid = UUID.fromString(string);
                    manager.party.getOrDefault(Bukkit.getPlayer(uuidOwner), new ArrayList<>()).add(Bukkit.getPlayer(uuid));
                    manager.joinArena(uuid, manager.getArena(uuidOwner).getArenaName());
                }
            }else{
                for (String string : players){
                    UUID uuid = UUID.fromString(string);
                    manager.spawn(uuid);
                }
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
