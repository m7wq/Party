package dev.m7wq.bungeecord.listeners;

import dev.m7wq.bungeecord.manager.PartyManager;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

@AllArgsConstructor
public class ReceiveRequestListener implements Listener {

    PartyManager partyManager;

    @EventHandler
    public void receiveRequest(PluginMessageEvent e){

        if (!e.getTag().equalsIgnoreCase("BungeeCord")) return;

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()))) {
            String subChannel = in.readUTF();

            if (!subChannel.equalsIgnoreCase("WARP_REQUEST"))
                return;

            String owner = in.readUTF();

            UUID uuid = UUID.fromString(owner);

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

            partyManager.warpUsage(player);



        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
