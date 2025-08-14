package dev.m7wq.bungeecord.listeners;

import dev.m7wq.bungeecord.Plugin;
import dev.m7wq.bungeecord.entity.PartyMember;
import dev.m7wq.bungeecord.enums.Role;
import dev.m7wq.bungeecord.manager.PartyManager;
import dev.m7wq.bungeecord.manager.PartyManager;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;



import net.md_5.bungee.event.EventHandler;

import java.util.List;

@AllArgsConstructor
public class MainListener implements Listener {

    PartyManager manager;


    @EventHandler
    public void onConnect(ServerConnectEvent e){
        if (!e.getPlayer().hasPermission("party.use"))
            e.getPlayer().setPermission("party.use",true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent e){

        PartyMember partyMember = manager.getPartyMember(e.getPlayer());

        if (partyMember==null)
            return;

        if (partyMember.getRole()==Role.LEADER) {
            manager.disbandUsage(e.getPlayer());;
        }else{

            manager.leaveUsage(e.getPlayer());;

        }
    }

    @EventHandler
    public void serverSwitch(ServerSwitchEvent e){
        manager.warpUsage(e.getPlayer());
    }




}
