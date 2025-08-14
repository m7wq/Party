package dev.m7wq.bungeecord;


import dev.m7wq.bungeecord.commands.PartyCommand;

import dev.m7wq.bungeecord.listeners.MainListener;
import dev.m7wq.bungeecord.listeners.ReceiveRequestListener;
import dev.m7wq.bungeecord.manager.PartyManager;

import lombok.Getter;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Command;

public final class Plugin extends net.md_5.bungee.api.plugin.Plugin {


    @Getter
    BungeeAudiences bungeeAudiences;

    @Getter
    PartyManager partyManager;



    @Override
    public void onEnable() {



        init();



    }

    void init(){

        partyManager = new PartyManager(this);
        bungeeAudiences = BungeeAudiences.create(this);


        Command command = new PartyCommand(this,partyManager,bungeeAudiences);

        getProxy().getPluginManager().registerCommand(this,command);

        getProxy().getPluginManager().registerListener(this, new ReceiveRequestListener(partyManager));
        getProxy().getPluginManager().registerListener(this, new MainListener(partyManager));


    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
